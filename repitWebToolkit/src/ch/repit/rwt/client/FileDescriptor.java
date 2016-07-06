/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.repit.rwt.client;

import java.io.Serializable;

/**
 *
 * @author tc149752
 */
public class FileDescriptor implements Serializable {

    private long   id;
    private String fileName;
    private String contentType;
    private Long   sizeBytes;

    public FileDescriptor() { }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Long getSizeBytes() {
        return sizeBytes;
    }

    public void setSizeBytes(Long sizeBytes) {
        this.sizeBytes = sizeBytes;
    }

    public String getViewUrl(ObjectRef containingBento) {
        return "getFile/" + containingBento + "/" + getId() + "/" + getFileName();
    }

    public String getPreviewUrl(ObjectRef containingBento) {
        return "getFile/" + containingBento + "/" + getId() + "/preview/" + getFileName() + ".png"; // TBD: is this correct?
    }

    public boolean isImage() {
        return getContentType().startsWith("image/");
        // kind of redundant with ContentTypeFamily...
    }

    @Override
    public String toString() {
        return "FileDescriptor{" + id + ";" + fileName + "}";
    }
}
