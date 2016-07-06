/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.repit.rwt.server.notification;

import ch.repit.rwt.client.Bento;
import ch.repit.rwt.client.BentoDef;
import ch.repit.rwt.client.BentoDefFactory;
import ch.repit.rwt.client.ObjectRef;
import ch.repit.rwt.client.annotations.IncludeInReports;
import ch.repit.rwt.client.audit.AuditLogDTO;
import ch.repit.rwt.client.audit.AuditLogDTO.AuditableAction;
import ch.repit.rwt.client.audit.AuditQueryDef;
import ch.repit.rwt.client.persistence.ObjectNotFoundException;
import ch.repit.rwt.client.util.Formaters;
import ch.repit.rwt.server.RwtRemoteServiceServlet;
import ch.repit.rwt.server.audit.AuditManager;
import ch.repit.rwt.server.persistence.DataObject;
import ch.repit.rwt.server.persistence.JdoHelper;
import ch.repit.rwt.server.persistence.PMF;
import ch.repit.rwt.server.util.Logging;
import ch.repit.rwt.server.user.User;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.jdo.PersistenceManager;
import javax.jdo.Query; 
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This is invoked by cron, to send daily reports
 */ 
public class ScheduledReportsServlet extends RwtRemoteServiceServlet {

    private static Logging LOG = new Logging(ScheduledReportsServlet.class.getName());

    private static final String DATE_RANGE_TAG = "%%DATE_RANGE%%";


    private static List<AuditableAction> interestActionList = new ArrayList<AuditableAction>();
    private static List<String> interestObjectTypes = new ArrayList<String>();

    static {
        interestActionList.add(AuditableAction.COMMENT);
        interestActionList.add(AuditableAction.CREATE);
        interestActionList.add(AuditableAction.UNTRASH);
        interestActionList.add(AuditableAction.TRASH);
        interestActionList.add(AuditableAction.UPDATE);

        for (BentoDef bentoDef : BentoDefFactory.get().getDefs()) {
            if (bentoDef.getClass().isAnnotationPresent(IncludeInReports.class))
                interestObjectTypes.add(bentoDef.getType());
        }
    }

    private String titleTemplate = null;
    private int daysRange;
    private String frequency;

    private Notifier emailSender;

    @Override
    public void init(ServletConfig conf) throws ServletException
    {
        super.init(conf);
        String method = "init";
        LOG.enter(method);

        emailSender = new Notifier();

        titleTemplate = "Rapport %%DATE_RANGE%%";

        frequency = conf.getInitParameter("frequency"); // or weeklyReport 
        if (frequency.equals("dailyReport"))
            daysRange = 0;
        else
            daysRange = 7;

        LOG.leave(method);
    }

 
    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        String method = "service";
        LOG.enter(method);

        // 1. fetch actions of this day
        Bento auditQuery = BentoDefFactory.get().getDef(AuditQueryDef.TYPE).createBento();
        auditQuery.get(AuditQueryDef.ATTR_FROMDAYS).set(daysRange + 1);
        auditQuery.get(AuditQueryDef.ATTR_SIZELIMIT).set(1000);
        List<AuditLogDTO> auditLogs = AuditManager.get().findAuditLogs(auditQuery);

        // 1.2. build map of objects and actions on them (we ignore who did what + if several time same action on same object)
        Map<ObjectRef,Map<AuditableAction,List<ObjectRef>>> auditInReportMap = new HashMap();
        Map<ObjectRef,List<AuditableAction>> auditInReportListForOrder = new HashMap();
        if (auditLogs != null && !auditLogs.isEmpty()) {
            for (AuditLogDTO log : auditLogs) {

                // check if this entry is interesting
                if (interestActionList.contains(log.getAction()) &&
                    interestObjectTypes.contains(log.getObject().getType()))
                {
                    Map<AuditableAction,List<ObjectRef>> objectLogsList = auditInReportMap.get(log.getObject());
                    List<AuditableAction> actionList = auditInReportListForOrder.get(log.getObject());
                    if (objectLogsList == null) {
                        auditInReportMap.put(log.getObject(), objectLogsList = new HashMap<AuditableAction,List<ObjectRef>>());
                        auditInReportListForOrder.put(log.getObject(), actionList = new ArrayList());
                    }
                    List<ObjectRef> auths = objectLogsList.get(log.getAction());
                    if (auths == null) {
                        auths = new ArrayList<ObjectRef>();
                        objectLogsList.put(log.getAction(), auths);
                    }
                    if (!auths.contains(log.getAuthor()))
                        auths.add(0,log.getAuthor());
                    if (!actionList.contains(log.getAction()))
                        actionList.add(0,log.getAction());
                }
            }
        }

        // remove those that were created and deleted (not interesting)
        List<ObjectRef> toRemove = new ArrayList();
        for (ObjectRef itemRef : auditInReportMap.keySet()) {
            Set<AuditableAction> actions = auditInReportMap.get(itemRef).keySet();
            if ( actions.contains(AuditableAction.CREATE)
                    && actions.contains(AuditableAction.TRASH)
                    && !actions.contains(AuditableAction.UNTRASH) )
                toRemove.add(itemRef);
        }
        for (ObjectRef itemRef : toRemove) {
            auditInReportMap.remove(itemRef);
            auditInReportListForOrder.remove(itemRef);
        }

        // cache user names
        PersistenceManager pm2 = PMF.get().getPersistenceManager();
        Map<ObjectRef, String> userRefNameMap = new HashMap();
        try {
            Query jdoQuery = pm2.newQuery(User.class);
            List<User> users = (List<User>)jdoQuery.execute();
            if (users != null) {
                for (User u : users) {
                    userRefNameMap.put(u.getObjectRef(), u.getDisplayName());
                }
            }
        } finally {
            pm2.close();
        }

        // 1.3. creates the email content
        StringBuffer actions = new StringBuffer();
        boolean skipped = false;
        for (ObjectRef objRef : auditInReportMap.keySet()) {
            // fetch object name
            if (actions.length() < 9000) {
                try {
                    DataObject dob = (DataObject)JdoHelper.get().getDataObject(objRef);
                    String objName = dob.getDisplayName();
                    String mailItem = objName;
                    BentoDef bentoDef = BentoDefFactory.get().getDef(objRef.getType());
                    // adds the actions on this obj
                    Map<AuditableAction,List<ObjectRef>> actionsMap = auditInReportMap.get(objRef);
                    List<AuditableAction> actionList = auditInReportListForOrder.get(objRef);
                    for (AuditableAction  action : actionList) {
                        String mailSubItem = "<b>" + action.getLabel(bentoDef.getLabelGender()) + "</b> par ";
                        String comma = "";
                        for (ObjectRef actionRef : actionsMap.get(action)) {
                            mailSubItem += comma + "<b>"+userRefNameMap.get(actionRef)+ "</b>";
                            comma = ", ";
                        }

                        mailItem += emailSender.formatMailSubItem(mailSubItem);
                    }
                    actions.append(emailSender.formatMailItem(mailItem,objRef));
                } catch (ObjectNotFoundException e) {
                    // obj was probably removed
                    LOG.info(method, "Object probably removed, will not appear in report", e);
                }
            } else
                skipped = true;
        }
        
        // 1.4 if nothing to report, quit here
        if (actions.length() == 0) {
            LOG.debug(method, "Nothing to report");
            return;
        }

        if (skipped) {
            actions.append(emailSender.formatMailItem("... et d'autres encore ..."));
        }

        // 3. personnalize the mail template
        Date from = new Date();
        Date to = new Date(from.getTime());
        from.setTime(from.getTime() - daysRange * 1000 * 60 * 60 * 24);
        String title = titleTemplate.replace(DATE_RANGE_TAG, Formaters.get().formatDateRange(from, to));

        // 4. send emails
        emailSender.deferNotif(frequency, title, actions.toString(), null, null);
        
        LOG.leave(method);
    }


}
