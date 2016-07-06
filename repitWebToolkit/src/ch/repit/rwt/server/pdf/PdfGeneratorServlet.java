/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.repit.rwt.server.pdf;

import ch.repit.rwt.client.security.AuthenticationException;
import ch.repit.rwt.client.security.Principal;
import ch.repit.rwt.server.RwtRemoteServiceServlet;
import ch.repit.rwt.server.util.Logging;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import java.io.OutputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class PdfGeneratorServlet extends RwtRemoteServiceServlet {

    private static Logging LOG = new Logging(PdfGeneratorServlet.class.getName());

    private Map<String,Class> pdfGenerators = new HashMap<String,Class>();


    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        String method = "init";
        LOG.enter(method);

        for (Enumeration e = config.getInitParameterNames(); e.hasMoreElements(); ) {
            String genName = (String)e.nextElement();
            String genClassName = (String)config.getInitParameter(genName);
            try {
                Class reporterClass = Class.forName(genClassName);
              //  AbstractPdfReport reporter = (AbstractPdfReport)reporterClass.newInstance();
                pdfGenerators.put(genName, reporterClass);
            } catch (ClassNotFoundException ex) {
                LOG.error(method, "Unable to load class " + genClassName, ex);
                throw new ServletException("Unable to load class " + genClassName, ex);
            }
        }

        LOG.leave(method);
    }


    @Override
    protected void doGet(final HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException
    {
        String method = "doGet";

        // 1. check permissions
        try {
            final Principal principal = getAuthorizer(request).getPrincipal();
            if (principal == null) {
                LOG.info(method, "missing generator or wrong invocation");
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
            }
            
            // 2. check parameters
            String pathInfo = request.getPathInfo();
            LOG.debug(method, "pathInfo="+pathInfo);
            if ( (pathInfo == null) || !pdfGenerators.containsKey(pathInfo.substring(1)) ) {
                LOG.info(method, "missing generator or wrong invocation");
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            }
            String reportName = pathInfo.substring(1);
            final AbstractPdfReport pdfReporter;
            try {
                pdfReporter = (AbstractPdfReport) (((Class)pdfGenerators.get(reportName)).newInstance());
            } catch (InstantiationException ex) {
                LOG.error(method, "Unable to load class " + reportName, ex);
                throw new ServletException("Unable to load class " + reportName, ex);
            } catch (IllegalAccessException ex) {
                LOG.error(method, "Unable to load class " + reportName, ex);
                throw new ServletException("Unable to load class " + reportName, ex);
            }

            // 3. generate document
            Document document = new Document();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                PdfWriter writer = PdfWriter.getInstance(document, baos);

                // meta data
                document.addAuthor(principal.getDisplayName() + " (" + principal.getNickName() + ")");
                document.addCreator("repit.ch, by Thomas Caprez");
                pdfReporter.initDocument(document, request.getParameterMap());

                // header footer
                writer.setPageEvent(new PdfPageEventHelper() {
                    @Override
                    public void onEndPage(PdfWriter writer, Document document) {
                        String header = pdfReporter.generateHeader(request.getParameterMap(), principal.getDisplayName(), document.getPageNumber());
                        String footer = pdfReporter.generateFooter(request.getParameterMap(), principal.getDisplayName(), document.getPageNumber());
                        if (header != null | footer != null) {
                            PdfContentByte cb = writer.getDirectContent();
                            if (header != null)
                                ColumnText.showTextAligned(cb,
                                      Element.ALIGN_CENTER, new Phrase(header, pdfReporter.smallFontOlive),
                                      (document.right() - document.left()) / 2
                                      + document.leftMargin(), document.top() + 10, 0);
                            if (footer != null)
                                ColumnText.showTextAligned(cb,
                                      Element.ALIGN_CENTER, new Phrase(footer, pdfReporter.smallFontOlive),
                                      (document.right() - document.left()) / 2
                                      + document.leftMargin(), document.bottom() -16, 0);
                        }
                    }
                });

                // content generation
                document.open();
                pdfReporter.generatePdf(document, request.getParameterMap());

                // TEST
                /*
                for (int k = 1; k <= 300; ++k) {
                    document.add( new Phrase("Quick brown fox jumps over the lazy dog. "));
                } */

                document.close();
            } catch(DocumentException e) {
                LOG.error(method, "Unexpected itext exception", e);
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }

            // 4. send response
            response.setHeader("Expires", "0");
            response.setHeader("Cache-Control", "must-revalidate, post-check=0, pre-check=0");
            response.setHeader("Pragma", "public");
            response.setCharacterEncoding("utf-8");
            response.setContentType("application/pdf");
            response.setContentLength(baos.size());
            OutputStream os = response.getOutputStream();
            baos.writeTo(os);
            os.flush();
            os.close();

            LOG.leave(method);
        } catch (AuthenticationException e) {
            LOG.error(method, "AuthenticationException", e);
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
        }
        
    }

}
