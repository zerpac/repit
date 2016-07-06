/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.repit.rwt.server.file;

import ch.repit.rwt.client.ObjectRef;
import ch.repit.rwt.client.persistence.ObjectNotFoundException;
import ch.repit.rwt.server.RwtRemoteServiceServlet;
import ch.repit.rwt.server.persistence.JdoHelper;
import ch.repit.rwt.server.persistence.PMF;
import ch.repit.rwt.server.util.Logging;

import com.google.appengine.api.datastore.Blob;

import java.io.IOException;

import java.util.List;
import javax.jdo.PersistenceManager;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author tc149752
 */
public class FileDownloadServlet extends RwtRemoteServiceServlet {

    private static Logging LOG = new Logging(FileDownloadServlet.class.getName());


    @Override
    public void init(ServletConfig conf) throws ServletException {
        super.init(conf);

        String method = "init";
        LOG.enter(method);
        // TBD...

        LOG.leave(method);
    }

    

    /** 
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void service(HttpServletRequest request, HttpServletResponse response)
      	throws ServletException, IOException
    {
        String method = "service";
        LOG.enter(method);

        byte[] fileContent = null;
        ObjectRef containingObjectRef = null;
        long fileId;
        boolean preview = false;
        
        // 1. Read the file id form the request
        String[] fileIds = request.getPathInfo().split("/");
        if (fileIds != null && fileIds.length > 2)
            try {
                containingObjectRef = new ObjectRef(fileIds[1]);
                fileId = Long.parseLong(fileIds[2]);
                if (fileIds.length > 3 && "preview".equals(fileIds[3]))
                    preview = true;
            } catch (Exception e) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
        else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        if (containingObjectRef == null || containingObjectRef.getType() == null || 
                containingObjectRef.getId() == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
            
        LOG.debug(method, "will read file from object " + containingObjectRef + " with ID " + fileId);

        // TBD: check authorization


        // 2. read the file content from the DB
        // TBD: reuse method from JdoHelper
        PersistenceManager pm = PMF.get().getPersistenceManager();
        try {
            // read DB file object
            FileHolder fileHolder = (FileHolder)JdoHelper.get().getDataObject(pm, containingObjectRef);

            File file = null;
            for (File f : (List<File>)fileHolder.getFiles())
                if (f.getFileKey().getId() == fileId)
                    file = f;

            if (file == null) {
                LOG.debug(method, "File does not exist");
                response.sendError(HttpServletResponse.SC_NO_CONTENT);
                return;
            }

            Blob blob = null;
            if (preview && file.getFileContentType().startsWith("image/") && file.getFilePreview() != null)
                blob = file.getFilePreview();
            else
                blob = file.getFileContent();
            
            if (blob != null)
                fileContent = blob.getBytes();
            else  {
                LOG.debug(method, "File has no content");
                response.sendError(HttpServletResponse.SC_NO_CONTENT);
                return;
            }

            LOG.debug(method, "File is ok, length=" + fileContent.length);
   
            response.setContentType(file.getFileContentType());
            ServletOutputStream responseOutputStream = response.getOutputStream();
            responseOutputStream.write(fileContent);
            responseOutputStream.flush();
            responseOutputStream.close();

        } catch (ObjectNotFoundException e) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        } finally {
            pm.close();
        }

        LOG.leave(method);
    }


}
