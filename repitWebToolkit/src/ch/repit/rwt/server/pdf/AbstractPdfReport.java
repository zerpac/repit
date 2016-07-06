package ch.repit.rwt.server.pdf;

import ch.repit.rwt.server.util.Logging;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.pdf.BaseFont;
import java.io.IOException;
import java.util.Map;



public abstract class AbstractPdfReport
{
    private static Logging LOG = new Logging(AbstractPdfReport.class.getName());

    private static final int NORMAL_FONT_SIZE = 11;   // 12 just not on one page
    private static final int SMALL_FONT_SIZE  = 9;    // 10 just not on one page
    private static final int MINI_FONT_SIZE  = 8;


    protected Font normalFont;
    protected Font normalFontOlive;
    protected Font normalFontBold;
    protected Font smallFont;
    protected Font smallFontBold;
    protected Font smallFontOlive;
    protected Font miniFont;  // used for stickers
    protected Font miniFontBold;  // used for stickers
    protected Font miniFontOlive;

    protected BaseColor repitOliveLight = new BaseColor(0xbb,0xdd,0xbb);
    protected BaseColor repitOliveDark = new BaseColor(0x80,0x80,0x00);


    // do we need an init method ?
    public AbstractPdfReport() {
        try {
            BaseFont bf = BaseFont.createFont();
            normalFont = new Font(bf, NORMAL_FONT_SIZE);
            normalFontOlive = new Font(bf, NORMAL_FONT_SIZE, Font.ITALIC, repitOliveDark);
            normalFontBold = new Font(bf, NORMAL_FONT_SIZE, Font.BOLD);
            smallFont = new Font(bf, SMALL_FONT_SIZE);
            smallFontOlive = new Font(bf, SMALL_FONT_SIZE, Font.ITALIC, repitOliveDark);
            smallFontBold = new Font(bf, SMALL_FONT_SIZE, Font.BOLD);
            miniFont = new Font(bf, MINI_FONT_SIZE);
            miniFontBold = new Font(bf, MINI_FONT_SIZE, Font.BOLD);
            miniFontOlive = new Font(bf, MINI_FONT_SIZE, Font.ITALIC, repitOliveDark);
        } catch (DocumentException ex) {
            LOG.error("AbstractPdfReport", "DocumentException", ex);
        } catch (IOException ex) {
            LOG.error("AbstractPdfReport", "IOException", ex);
        }
    }



    public void generatePdf(Document pdfDoc, Map options) throws DocumentException {
        addTitle(pdfDoc, options);
        addContent(pdfDoc, options);
    }


    protected String generateHeader(Map options, String principalName, int pageNumber) {
        return null;
    }

    protected void addTitle(Document pdfDoc, Map options) {
        // nothing yet
    }

    protected abstract void addContent(Document pdfDoc, Map options) throws DocumentException;

    protected String generateFooter(Map options, String principalName, int pageNumber) {
        return null;
    }

    /**
     * can be implemented eg to set margins
     */
    protected void initDocument(Document document, Map options) {
        // nothing yet
    }


}
