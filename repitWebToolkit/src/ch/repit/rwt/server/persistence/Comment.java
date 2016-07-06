/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.repit.rwt.server.persistence;

import com.google.appengine.api.datastore.Key;
import java.util.Date;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 * Generic comment class, planned for blogs and inscriptions
 * NOT USED ANYMORE, now comments are persisted as lists of strings, as it
 * caused problems to have identical attribute names for list of object refs
 */
@Deprecated
@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class Comment {

    @PrimaryKey
    @Persistent //(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Key key;

    @Persistent
    private String commentText;

    @Persistent
    private Date commentDate;

    @Persistent
    private String commenterName;  // TBD: should be an id or ref ?


    // constructor for creates
    public Comment() { }

    public Key getKey() {
        return key;
    }
    public void setKey(Key key) {
        this.key = key;
    }

    public Date getCommentDate() {
        return commentDate;
    }

    public void setCommentDate(Date commentDate) {
        this.commentDate = commentDate;
    }

    public String getCommentText() {
        return commentText;
    }

    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }

    public String getCommenterName() {
        return commenterName;
    }

    public void setCommenterName(String commenterName) {
        this.commenterName = commenterName;
    }

    @Override
    public String toString() {
        return "Comment:(" + key + ";" + commentDate + ";" + commenterName + ";" + commentText + ")";
    }
}
