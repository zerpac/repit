package ch.repit.rwt.server.notification;

import ch.repit.rwt.client.BentoStatus;
import ch.repit.rwt.client.ObjectRef;
import ch.repit.rwt.client.persistence.ObjectNotFoundException;
import ch.repit.rwt.server.RwtRemoteServiceServlet;
import ch.repit.rwt.server.persistence.CommentMapper;
import ch.repit.rwt.server.persistence.DataObject;
import ch.repit.rwt.server.persistence.JdoHelper;
import ch.repit.rwt.server.persistence.PMF;
import ch.repit.rwt.server.util.Logging;
import ch.repit.rwt.server.user.User;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Implements a task to be runned in the background, to send alert notification emails.
 */
public class EmailSendingTask extends RwtRemoteServiceServlet {

    private static final String TITLE_TAG = "%%TITLE%%";
    private static final String ACTIONS_TAG = "%%ACTIONS%%";
    public static final String DOMAIN_TAG = "%%DOMAIN%%";

    private static Logging LOG = new Logging(EmailSendingTask.class.getName());


    private InternetAddress from;
    private String mailTemplate;
    private String domain;


    private TwitterNotif twitt;

    @Override
    public void init(ServletConfig conf) throws ServletException
    {
        super.init(conf);
        String method = "init";
        LOG.enter(method);

        // read the html template file
        String mailTemplateLocation = conf.getInitParameter("mailTemplateLocation");
        InputStream is = conf.getServletContext().getResourceAsStream(mailTemplateLocation);
        StringBuffer mailTemplateSB = new StringBuffer();
        try {
            if (is != null) {
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader reader = new BufferedReader(isr);
                String text = "";
                while ((text = reader.readLine()) != null) {
                    mailTemplateSB.append(text);
                }

                mailTemplate = mailTemplateSB.toString();
                LOG.debug(method, "correctly initialized EmailSender");
            }
        } catch (IOException e) {
            LOG.error(method, "unable to read mail template", e);
            throw new ServletException(e);
        }

        // create the mail sender address
        domain = conf.getInitParameter("domain");
        String senderEmail = conf.getInitParameter("senderEmail");
        try {
            from = new InternetAddress(senderEmail, domain);
        } catch (UnsupportedEncodingException ex) {
            LOG.error(method, "unable to create from email address", ex);
            throw new ServletException(ex);
        }

        // inits twitter notifs
        twitt = new TwitterNotif();
        twitt.init(conf);

        LOG.leave(method);
    }




    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        String method = "service";
        LOG.enter(method);

        // 1. reads the params
        String mailTitle = request.getParameter("mailTitle");
        String mailInnerProtoContent = request.getParameter("mailContent");
        if (mailTitle == null || mailInnerProtoContent == null) {
            LOG.error(method, "missing args:" + mailTitle + "; " + mailInnerProtoContent);
            return;
        }

        String objectRefStr = request.getParameter("objectRef");
        List<String> connectedUsers = new ArrayList();
        ObjectRef objectRef = null;
        if (objectRefStr != null && objectRefStr.length() > 0) {
            objectRef = new ObjectRef(objectRefStr);
            PersistenceManager pm = PMF.get().getPersistenceManager();
            try {
                DataObject dataObject = JdoHelper.get().getDataObject(pm, objectRef);
                // fetch user list for mild interest
                if (dataObject.getOwner() != null)
                    connectedUsers.add(dataObject.getOwner());
                List<String> coms = dataObject.getComments();
                if (coms != null && coms.size() > 0) {
                    for (String com : coms)
                        connectedUsers.add(CommentMapper.string2Bento(com).getCommenterName());
                }
            } catch (ObjectNotFoundException e) {
                // oups, was probably a final deletion (no need to inform anyone connected)
            } finally {
                pm.close();
            }
        }

        String userQueryFilter = request.getParameter("userQueryFilter");
        boolean isCreate = false;
        if (userQueryFilter != null && userQueryFilter.length() > 0) {
            if (objectRef==null)
                userQueryFilter = " && " + userQueryFilter + " == true ";
            else {
                if ("CREATE".equals(userQueryFilter))
                    isCreate = true;
                userQueryFilter = "";
            }
        }

        // perform proto replacements, introduced to lower tasks size
        String mailInnerContent = mailInnerProtoContent.replaceAll("\\[MI\\]",
                        "<div style='margin-left: 80px;" +
                                   " margin-right: 60px; " +
                                   " padding: 10px;" +
                                   " text-align: left;" +
                                   " color: olive;" +
                                   " font-size: small;" +
                                   " font-weight:bold;'>");
        mailInnerContent = mailInnerContent.replaceAll("\\[SI\\]",
                       "<div style='margin: 0px;" +
                                   "color: black;" +
                                   "padding-left: 10px; " +
                                   "padding-right: 10px;" +
                                   "padding-bottom: 0px;" +
                                   "font-size: small; " +
                                   "font-weight:normal;" +
                                   "border-left-style: solid; " +
                                   "border-width: 2px; " +
                                   "border-color: olive;'>");
        mailInnerContent = mailInnerContent.replaceAll("\\[/SI\\]", "</div>");
        mailInnerContent = mailInnerContent.replaceAll("\\[/MI\\]", "</div>");

        // 2. list interested users
        List<InternetAddress> recipients = new ArrayList<InternetAddress>();
        PersistenceManager pm = PMF.get().getPersistenceManager();
        try {
            Query jdoQuery = pm.newQuery(User.class);
            jdoQuery.setFilter(" status == '"+BentoStatus.ACTIVE.name()+"' " + userQueryFilter);
            List<User> users = (List<User>)jdoQuery.execute();
            if (users != null) {
                for (User u : users) {
                    if (    (objectRef == null) ||
                            (u.isNotifAll() == Boolean.TRUE) ||
                            (u.isNotifMines() == Boolean.TRUE && connectedUsers.contains(u.getObjectRef().toString())) ||
                            (u.isNotifCreates() == Boolean.TRUE && isCreate) ) {
                        String email = u.getEmail();
                        if (email != null && email.trim().length() > 0)
                            recipients.add(new InternetAddress(email.trim(), u.getDisplayName()));
                    }
                }
            }

            // if none interested, quit here
            if (recipients.size() == 0) {
                LOG.debug(method, "None to report to");
                return;
            }

            // creates the text
            String mailContent = mailTemplate;
            mailContent = mailContent.replaceAll(TITLE_TAG, mailTitle);
            mailContent = mailContent.replaceFirst(ACTIONS_TAG, mailInnerContent);
            mailContent = mailContent.replaceAll(DOMAIN_TAG, domain);

            // 3. create message
            Properties props = new Properties();
            Session session = Session.getDefaultInstance(props, null);
            MimeMessage msg = new MimeMessage(session);
            msg.setFrom(from);
            msg.setSubject(mailTitle.replaceAll("&eacute;", "é"), "UTF8");  // 22.2.10: removed domain, already in sender...
            msg.setText("Nothing in text, see html...");  // ???
            Multipart mp = new MimeMultipart();
            MimeBodyPart htmlPart = new MimeBodyPart();
            htmlPart.setContent(mailContent, "text/html");
            mp.addBodyPart(htmlPart);
            msg.setContent(mp);

            // 4. send emails
            //    (WARNING: 30 seconds per task. If too many recipients, may be too much!)
            for (InternetAddress recipient : recipients) {
                msg.setRecipient(Message.RecipientType.TO, recipient);
                Transport.send(msg);
                LOG.debug(method, "message sent to " + recipient.getAddress());
            }

        } catch (Exception e) {
            LOG.error(method, "unexpected exception (sic!)", e);
            // nothing is propagated
        } finally {
            pm.close();
        }


        // sends the twitter feed
        String tweet = request.getParameter("tweet");
        if (tweet!=null && tweet.length()>1) {
            tweet = tweet.replaceAll(DOMAIN_TAG, domain);
            twitt.notif(tweet);
        }

        LOG.leave(method);
    }

}
