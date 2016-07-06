/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.repit.rwt.server.file;

import ch.repit.rwt.client.BentoStatus;
import ch.repit.rwt.client.ContentTypeFamily;
import ch.repit.rwt.client.ObjectRef;
import ch.repit.rwt.client.audit.AuditLogDTO.AuditableAction;
import ch.repit.rwt.client.persistence.ObjectNotFoundException;
import ch.repit.rwt.client.security.Action;
import ch.repit.rwt.client.security.AuthenticationException;
import ch.repit.rwt.client.security.Authorizer;
import ch.repit.rwt.client.security.SecurityException;
import ch.repit.rwt.client.ui.form.FileUploadField;
import ch.repit.rwt.server.RwtRemoteServiceServlet;
import ch.repit.rwt.server.audit.AuditLog;
import ch.repit.rwt.server.audit.AuditLogAttribute;
import ch.repit.rwt.server.audit.AuditManager;
import ch.repit.rwt.server.notification.Notifier;
import ch.repit.rwt.server.persistence.JdoHelper;
import ch.repit.rwt.server.persistence.PMF;
import ch.repit.rwt.server.util.Logging;

import com.google.appengine.api.datastore.Blob;

import com.google.appengine.api.images.Image;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.Transform;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import java.util.Date;
import javax.jdo.PersistenceManager;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * cf http://code.google.com/p/puntosoft/source/browse/trunk/src/ar/com/puntosoft/shared/showcase/server/servlets/FileUpload.java
 * @author tc149752
 */
public class FileUploadServlet extends RwtRemoteServiceServlet  {

    private static Logging LOG = new Logging(FileUploadServlet.class.getName());
    
    private Notifier notifier;



    @Override
    public void init(ServletConfig conf) throws ServletException {
        super.init(conf);

        String method = "init";
        LOG.enter(method);

        // init notifier
        notifier = new Notifier();


        LOG.leave(method);
    }

    
    @Override
    public void service(HttpServletRequest request, HttpServletResponse response)
      	throws ServletException, IOException
    {
        String method = "service";
        LOG.enter(method);

        response.setContentType("text/html");

        // check authentication
        Authorizer authorizer = null;
        try {
            authorizer = super.getAuthorizer(request);
        } catch (AuthenticationException ex) {
            LOG.debug(method, "not authenticated", ex);
            response.getWriter().print("not authenticated");
            return;
        }

        ObjectRef uploadObjectRef = null;

        // Create a new file upload handler
        ServletFileUpload upload = new ServletFileUpload();
        Blob fileContent = null;
        Blob preview = null;
        String fileName = null;
        long fileSize = -1;
        String contentType = null;

        // A. Parse the request
        try {
            FileItemIterator iter = upload.getItemIterator(request);
            while (iter.hasNext()) {
                FileItemStream item = iter.next();
                String name = item.getFieldName();
                InputStream stream = item.openStream();
                if (item.isFormField()) {
                    String value = Streams.asString(stream);
                    LOG.debug(method, "Form field " + name + " with value " +value+ " detected.");
                    if ( FileUploadField.UPLOAD_OBJECTREF_KEY.equals(name) && value != null ) {
                        uploadObjectRef = new ObjectRef(value);
                    }
                }
                else { 
                    LOG.debug(method, "Upload file type " + item.getContentType() + ", name: " + name);
                    // Process the input stream

                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    int len;
                    byte[] buffer = new byte[8192];
                    while ((len = stream.read(buffer, 0, buffer.length)) != -1) {
                        out.write(buffer, 0, len);
                        // stop reading the file once the higher limit (5MB) is reached...

                        if (out.size() > 5000000) { 
                            LOG.info(method, "file higher than upper limit of 5Mb, aborting upload");
                            response.getWriter().print("Fichier trop volumineux, annulation.");
                            return;
                        }
                    }
                    LOG.debug(method, "Total Bytes: " + out.size());

                    fileSize = out.size();
                    fileContent = new Blob(out.toByteArray());
                    contentType = item.getContentType();

                    // cleanup the file name
                    fileName = item.getName();
                    if (fileName.contains("\\")) {
                       // String[] fs = fileName.split("\\");
                       // fileName = fs[fs.length-1];
                        fileName = fileName.substring(fileName.lastIndexOf("\\") + 1);
                    }
                    fileName = fileName.replaceAll("\\s", "_");
                }
            }
        } catch (Exception e) {  // TBD
            LOG.error(method, "Unexpected exception", e);
            response.getWriter().print("Unexpected exception, see server logs");
            return;
        }



        // B. if present, store the file found
        
        // objectRef is required in params
        if (uploadObjectRef == null || uploadObjectRef.getType() == null || uploadObjectRef.getId() == null) {
            LOG.error(method, FileUploadField.UPLOAD_OBJECTREF_KEY + " must be defined");
            //result = createError("Code error: missing key "+FileUploadField.UPLOAD_OBJECTREF_KEY, null, null);
            //response.getWriter().print("missing key "+FileUploadField.UPLOAD_OBJECTREF_KEY);
            throw new ServletException("missing key "+FileUploadField.UPLOAD_OBJECTREF_KEY);
        }

        // authorize the upload
        try {
            authorize(Action.UPDATE, uploadObjectRef, request);
        } catch (SecurityException ex) {
            LOG.warning(method, "Not authorized to upload file", ex);
            response.getWriter().print("Pas autorisé à envoyer un fichier");
            return;
        }

        // write the file in DB
        if (fileContent != null) {

            PersistenceManager pm = PMF.get().getPersistenceManager();
            try {
                // read containing DB object
                FileHolder fileHolder = (FileHolder)JdoHelper.get().getDataObject(pm, uploadObjectRef);
                Date previousUpdateDate = fileHolder.getLastUpdate();

                // check if file is allowed
                ContentTypeFamily contentTypeFamily = ContentTypeFamily.getContentTypeFamily(contentType);
                if (contentTypeFamily == null || !fileHolder.listAllowedFileTypes().contains(contentTypeFamily)) {
                    LOG.info(method, "File type not allowed: " + contentTypeFamily + " (" + contentType +")");
                    LOG.debug(method, "listAllowedFileTypes: " + fileHolder.listAllowedFileTypes());
                    response.getWriter().print("Type de fichier non admis: " + contentType);
                    return;
                }

                // check file size
                if (fileContent.getBytes().length > fileHolder.getFileMaxSizeInBytes()) {
                    LOG.info(method, "File bigger than max size allowed");
                    response.getWriter().print("Fichier trop volumineux, maximum: " + (fileHolder.getFileMaxSizeInBytes() / 1000) + " KB");
                    return;
                }

                // if it is an image, try to create the preview
                if (contentTypeFamily == ContentTypeFamily.IMAGE) {
                    try {
                        ImagesService imagesService = ImagesServiceFactory.getImagesService();
                        Image oldImage = ImagesServiceFactory.makeImage(fileContent.getBytes());
                        int previewFrameSize = fileHolder.getImagePreviewSize();
                        Transform resize = ImagesServiceFactory.makeResize(previewFrameSize, previewFrameSize);
                        Image newImage = imagesService.applyTransform(resize, oldImage);
                        byte[] newImageData = newImage.getImageData();
                        preview = new Blob(newImageData);
                    } catch (Exception e) {
                        // dont want anything here stop the file saving...
                        LOG.error(method, "exception while generating preview, ignoring...", e);
                    }
                }

                // update DB file object
                File file = fileHolder.createFileInstance();
                file.setFileContent(fileContent);
                file.setFileName(fileName);  // ???
                file.setFileContentType(contentType);
                file.setFileSizeBytes(fileSize);
                if (preview !=null)
                    file.setFilePreview(preview);

                // add file to containing object
                fileHolder.setUpdated();
 
                // Log only the comment text
                AuditLog auditLog = new AuditLog(AuditableAction.UPDATE,
                            authorizer.getPrincipal().getUserRef(), uploadObjectRef);
                auditLog.getAuditLogAttributes().add(new AuditLogAttribute
                    ("fileName", null, file.getFileName()));
                auditLog.getAuditLogAttributes().add(new AuditLogAttribute
                    ("contentType", null, file.getFileContentType()));
                
                // save the object with new file
                pm.makePersistent(fileHolder);

                // set an entry in audit log, if not a draft
                if (!fileHolder.getStatus().equals(BentoStatus.DRAFT.name())) {
                    AuditManager.get().writeAuditLog(pm, auditLog);

                    // notify of the update
                    if ( (System.currentTimeMillis() - previousUpdateDate.getTime()) > (600 * 1000) )
                        notifier.notify(
                                authorizer.getPrincipal(),
                                fileHolder.getDisplayName(),
                                uploadObjectRef,
                                AuditableAction.UPDATE);
                }

                response.getWriter().print("ok");
                LOG.debug(method, "File written in DB");

            } catch (ObjectNotFoundException e) {
                LOG.error(method, "ObjectNotFoundException " + uploadObjectRef, e);
                response.getWriter().print("containing object not found");
                return;
            } finally {
                pm.close();
            }
        }

        LOG.leave(method);
    }


    public String getStackTrace(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw, true);
        t.printStackTrace(pw);
        pw.flush();
        sw.flush();
        return sw.toString();
    }

    
    /* was used when complexe datsa struct were returned as error.
     * Now we only return a string not starting with "ok"
    private String createError(String msg, String ist, String soll) {
        String result = null;
        try {
            JSONWriter jw = new JSONStringer().object()
                .key("success").value(false)
                .key("errors").value(msg);
            if (ist != null)
                jw.key("is").value(ist);
            if (soll != null)
                jw.key("should").value(soll);
            result = jw.endObject().toString();
        } catch (JSONException e) {
            LOG.error("createResult", "Unable to create JSON object", e);
        }
        return result;
    }
    */
}
