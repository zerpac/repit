/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.repit.rwt.server.persistence;

import ch.repit.rwt.client.BentoComment;
import ch.repit.rwt.server.util.Logging;
import java.util.Date;

/**
 *
 * @author tc149752
 */
public class CommentMapper {

    private static Logging LOG = new Logging(CommentMapper.class.getName());

    private static final String SEP = "#";

    public static String bento2String(BentoComment bentoComment) {
        return "" + bentoComment.getCommentDate().getTime() + SEP +
                bentoComment.getCommenterName() + SEP +
                bentoComment.getCommentText();
    }

    public static BentoComment string2Bento(String commentString) {
        LOG.debug("string2Bento", "commentString=" + commentString);
        BentoComment bentoComment = new BentoComment();
        String[] split = commentString.split("[#§\\xa7]", 3);  // use to be §, but was suddenly not read (escaped by xa7...)
        bentoComment.setCommentDate(new Date(Long.parseLong(split[0])));
        bentoComment.setCommenterName(split[1]);
        bentoComment.setCommentText(split[2]);
        LOG.debug("string2Bento", "bentoComment=" + bentoComment);
        return bentoComment;
    }


}
