/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.repit.rwt.client;

import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author tc149752
 */
public class BentoComment implements Serializable {

    private String commentText;
    private Date commentDate;
    private String commenterName;
    private long id;

    public BentoComment() { }

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
        return "BentoComment{" + commentDate + ";" + commentText + ";" + commenterName + "}";
    }

    public void setId(long id) {
        this.id = id;
    }
    public long getId() {
        return id;
    }
}
