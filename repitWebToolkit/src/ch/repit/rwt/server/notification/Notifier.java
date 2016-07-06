/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.repit.rwt.server.notification;

import ch.repit.rwt.client.BentoDef;
import ch.repit.rwt.client.BentoDefFactory;
import ch.repit.rwt.client.ObjectRef;
import ch.repit.rwt.client.annotations.IncludeInAlerts;
import ch.repit.rwt.client.audit.AuditLogDTO.AuditableAction;
import ch.repit.rwt.client.security.Principal;
import ch.repit.rwt.client.util.Formaters;
import ch.repit.rwt.server.util.Logging;
//import com.google.appengine.api.labs.taskqueue.Queue;
//import com.google.appengine.api.labs.taskqueue.QueueFactory;
//import static com.google.appengine.api.labs.taskqueue.TaskOptions.Builder.*;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import static com.google.appengine.api.taskqueue.TaskOptions.Builder.*;

import java.util.Date;


/**
 *
 * @author tc149752
 */
public class Notifier {

    private static Logging LOG = new Logging(Notifier.class.getName());


    public void notify(Principal principal,
                       String objectDisplayName,
                       ObjectRef objectRef,
                       AuditableAction action,
                       String... otherLines)
    {
        if (objectRef != null && action.getAlertLabel() != null)
        {
            // check that object type is notifiable
            BentoDef bd = BentoDefFactory.get().getDef(objectRef.getType());
            if (bd.getClass().isAnnotationPresent(IncludeInAlerts.class)) {
                try {

                    // format mail action text
                    String actionText =  formatMailSubItem
                        ("<b>" + action.getLabel(bd.getLabelGender()) + "</b> par <b>" +
                         principal.getDisplayName() + "</b>");
                    if (otherLines != null && otherLines.length > 0) 
                        for (String line : otherLines) 
                            actionText = actionText + formatMailSubItem(line);
                    actionText = formatMailItem(objectDisplayName + actionText, objectRef);

                    // format tweet
                    String anonName = principal.getDisplayName();
                    anonName = anonName.substring(0, anonName.indexOf(" ") + 2) + ".";
                    Date today = new Date();
                    String tweet = Formaters.get().formatDateRange(today, today) + ": "
                            + bd.getTypeLabel() + " " + action.getLabel(bd.getLabelGender())
                            + " par " + anonName
                            + " -> http://"+EmailSendingTask.DOMAIN_TAG+"/?tw="+objectRef.toString();


                    deferNotif(action==AuditableAction.CREATE?"CREATE":"",
                            "Notification " + bd.getTypeLabel() + " " + action.getLabel(bd.getLabelGender()),
                            actionText,
                            objectRef,
                            tweet);

                } catch (Exception e) {
                    LOG.warning("notif", "Caught unexpected exception, will be ignored", e);
                }
            }
        }
    }

    void deferNotif(String userQueryParam, String title, String messageContentHtml, ObjectRef objectRef, String tweet)
    {
        String method = "sendEmail";
        LOG.enter(method);

        Queue queue = QueueFactory.getDefaultQueue();
        queue.add(withUrl("/tasks/mailSender")
                        .param("mailTitle", title)
                        .param("mailContent", messageContentHtml)
                        .param("userQueryFilter", userQueryParam)
                        .param("objectRef", (objectRef==null)?"":objectRef.toString())
                        .param("tweet", (tweet==null)?"":tweet)
                 );

        LOG.leave(method);
    }



    // these 2 methods are here to reduce msg size, as it has to be sent to servlet
    public String formatMailItem(String itemText) {
        return formatMailItem(itemText, null);
    }
    public String formatMailItem(String itemText, ObjectRef oref) {
        String result = "[MI]";
        if (oref != null)
            result += "<a href='http://"+EmailSendingTask.DOMAIN_TAG+"/?directLink="+oref.toString()+"'>";
        result += itemText;
        if (oref != null)
            result += "</a>";
        result += "[/MI]";
        return result;
    }


    public String formatMailSubItem(String itemText) {
        String result = "[SI]";
        result += itemText;
        result += "[/SI]";
        return result;
    }

}
