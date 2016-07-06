package ch.repit.rwt.server.audit;

import ch.repit.rwt.client.Bento;
import ch.repit.rwt.client.ObjectRef;
import ch.repit.rwt.client.audit.AuditLogDTO;
import ch.repit.rwt.server.persistence.PMF;
import ch.repit.rwt.server.util.Logging;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

/**
 * Singleton class that manages audit logs
 */
public class AuditManager {
    private static Logging LOG = new Logging(AuditManager.class.getName());

    private static final AuditManager s_instance = new AuditManager();

    public static AuditManager get() {
        return s_instance;
    }

    private AuditManager() {  }

    
    
    public List<AuditLogDTO> findAuditLogs(Bento auditQuery) {
        String method = "findAuditLogs";
        LOG.enter(method);
        assert auditQuery != null : "audit query cannot be null";

        List<AuditLogDTO> logsDTOList = new ArrayList<AuditLogDTO>();

        PersistenceManager pm = PMF.get().getPersistenceManager();
        try {
            List<AuditLog> LogsDOList = getAuditLogs(pm, auditQuery);

            // transform the result
            if (LogsDOList != null && !LogsDOList.isEmpty()) {
                for (AuditLog a : LogsDOList) {
                    logsDTOList.add(a.toDTO());
                }
            }
        } finally {
            pm.close();
        }
        LOG.leave(method);
        return logsDTOList;
    }

    
    public void deleteAuditLogs(Bento auditQuery, ObjectRef principalRef) {
        String method = "deleteAuditLogs";
        LOG.enter(method);
        assert auditQuery != null : "audit query cannot be null";

        PersistenceManager pm = PMF.get().getPersistenceManager();
        try {
            List<AuditLog> LogsDOList = getAuditLogs(pm, auditQuery);

            // Logs the deletion
            int deletionCount = LogsDOList.size();
            AuditLog auditMsg = new AuditLog(AuditLogDTO.AuditableAction.DELETE, principalRef);
            List<AuditLogAttribute> attr = new ArrayList<AuditLogAttribute>();
            attr.add(new AuditLogAttribute("deleteCount", null, "" + deletionCount));
            // could store delete query attribute... 
            auditMsg.setAuditLogAttributes(attr);

            // actually delete them
            pm.deletePersistentAll(LogsDOList);

            writeAuditLog(pm, auditMsg);
        } finally {
            pm.close();
        }
        LOG.leave(method);
    }



    /**
     * Writes an audit log entry (so simple that maybe not needed !!!)
     */
    public void writeAuditLog(PersistenceManager pm, AuditLog auditLog) {
        String method = "writeAuditLog";
        LOG.enter(method);
        assert auditLog != null : "input auditLog param cannot be null";

        // adds session properties (bug NTH)

        // write the adit log
        pm.makePersistent(auditLog);

        // send alert notif if it makes sense

    // private void notif(String objectDisplayName, String objectType, AuditableAction action) {
/* GOOD IDEA, BUT MISSING MANY THINGS (display name, action obj etc)
        try {
            String actionText = "<div style='margin: 0px;color: black;padding-left: 10px; padding-right: 10px;padding-bottom: 0px;font-size: small; font-weight:normal;border-left-style: solid; border-width: 2px; border-color: olive;'>";
            actionText += "<b>" + auditLog.getObjectRef() + "</b> a été <b>" + auditLog.action.getLabel() + "</b> par <b>" +
                    super.getAuthorizer().getPrincipal().getDisplayName() + "</b>";
            actionText += "</div>";
            emailSender.sendEmail("notif" +  objectType +
                    action.toString().substring(0,1)+action.toString().toLowerCase().substring(1),
                    "Alerte " + objectType, action.getLabel());
        } catch (Exception e) {
            // TBD ???
        }
*/
        LOG.leave(method);
    }


    private List<AuditLog> getAuditLogs(PersistenceManager pm, Bento auditQuery)
    {
        LOG.debug("getAuditLogs", "Bento auditQuery =" + auditQuery);

        List<AuditLog> LogsDOList;
        Map params = new HashMap();
        Query jdoQuery = pm.newQuery(AuditLog.class);
        String filter = "";
        String and = "";

        // transform the auditQuery into a JDO query filter
        String tmp;
        if ((tmp = auditQuery.getDisplayValue("action")) != null && tmp.length() > 0) {
            filter += and + "action == '" + tmp + "'";
            and = " && ";
        }
        if ((tmp = auditQuery.getDisplayValue("author")) != null && tmp.length() > 0) {
            filter += and + "author == '" + tmp + "'";
            and = " && ";
        }
        if ((tmp = auditQuery.getDisplayValue("objectType")) != null && tmp.length() > 0) {
            filter += and + "objectType == '" + tmp + "'";
            and = " && ";
        }
        if ((tmp=auditQuery.getDisplayValue("objectRef")) != null && tmp.length() > 0) {
            filter += and + "object == '" + tmp + "'";
            and = " && ";
        }
        
        // cope with date fields, preference to dateFrom and to
        Date fromDate = auditQuery.get("fromDate")==null?null:auditQuery.get("fromDate").getDate();
        if (fromDate == null && auditQuery.get("fromDays") != null) {
            Integer fd = auditQuery.get("fromDays").getInteger();
            if (fd != null) {
                fromDate = new Date();
                fromDate.setDate(fromDate.getDate() - fd);
            }
        }
        if (fromDate != null) {
            filter += and + "eventDate > fromDate";
            and = " && ";
            params.put("fromDate", fromDate);
          //  jdoQuery.declareParameters("java.util.Date fromDate");
        }

        Date toDate = auditQuery.get("toDate")==null?null:auditQuery.get("toDate").getDate();
        if (toDate == null && auditQuery.get("toDays") != null) {
            Integer fd = auditQuery.get("toDays").getInteger();
            if (fd != null) {
                toDate = new Date();
                toDate.setDate(toDate.getDate() - fd);
            }
        }
        if (toDate != null) {
            filter += and + "eventDate < toDate";
            and = " && ";
            params.put("toDate", toDate);
          //  jdoQuery.declareParameters("java.util.Date toDate");
        }

        // the limit + order by
        jdoQuery.setOrdering("eventDate descending");
        Integer querySizeLimit = auditQuery.get("querySizeLimit").getInteger();
        if (querySizeLimit != null)
            jdoQuery.setRange(0, querySizeLimit);

        // execute the JDO query
        LOG.debug("getAuditLogs", "Using filter for audit query: " + filter);
        if (filter.length() > 0) {
            jdoQuery.setFilter(filter);
        }
        if (!params.isEmpty()) {
            String par = "";
            String comma = "";
            for (Object p : params.keySet()) {
                par += comma + "java.util.Date " + (String)p + " ";
                comma = ", ";
            }
            jdoQuery.declareParameters(par);
            LogsDOList = (List<AuditLog>)jdoQuery.executeWithMap(params);
        } else {
            LogsDOList = (List<AuditLog>)jdoQuery.execute();
        }
        return LogsDOList;
    }

}
