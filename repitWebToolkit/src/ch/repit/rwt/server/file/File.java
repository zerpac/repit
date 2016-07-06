/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.repit.rwt.server.file;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.Key;

public interface File {

    public Key getFileKey();

    public Blob getFileContent();

    public void setFileContent(Blob content);

    public String getFileName();

    public void setFileName(String fileName);

    public String getFileContentType();

    public void setFileContentType(String contentType);

    public Long getFileSizeBytes();

    public void setFileSizeBytes(Long sizeBytes);

    public Blob getFilePreview();

    public void setFilePreview(Blob preview);

}
