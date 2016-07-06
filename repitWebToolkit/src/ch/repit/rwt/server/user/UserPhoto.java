/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.repit.rwt.server.user;

import ch.repit.rwt.server.file.File;
import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.Key;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class UserPhoto implements File {

    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Key key;


    @Persistent
    private String fileName;
    @Persistent
    private String contentType;
    @Persistent
    private Long sizeBytes;

    @Persistent
    private Blob content;
    @Persistent
    private Blob preview;

    
    // constructor for creates
    public UserPhoto() { }

    public Key getFileKey() {
        return key;
    }
  
    public Blob getFileContent() {
        return content;
    }

    public void setFileContent(Blob content) {
        this.content = content;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileContentType() {
        return contentType;
    }

    public void setFileContentType(String contentType) {
        this.contentType = contentType;
    }

    public Long getFileSizeBytes() {
        return sizeBytes;
    }

    public void setFileSizeBytes(Long sizeBytes) {
        this.sizeBytes = sizeBytes;
    }

    public Blob getFilePreview() {
        return preview;
    }

    public void setFilePreview(Blob preview) {
        this.preview = preview;
    }

}
