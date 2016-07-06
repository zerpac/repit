package ch.repit.rwt.server.audit;

import ch.repit.rwt.client.Bento;
import ch.repit.rwt.client.audit.AuditLogDTO;
import ch.repit.rwt.client.audit.AuditQueryService;
import ch.repit.rwt.client.security.Action;
import ch.repit.rwt.client.security.SecurityException;
import ch.repit.rwt.server.RwtRemoteServiceServlet;
import ch.repit.rwt.server.util.Logging;
import java.util.List;

/**
 * 
 * @author tc149752
 */
public class AuditQueryServiceImpl extends RwtRemoteServiceServlet implements AuditQueryService
{
    private static Logging LOG = new Logging(AuditQueryServiceImpl.class.getName());

    public List<AuditLogDTO> listAuditLogs(Bento criteria) throws SecurityException {
        String method = "listAuditLogs";
        LOG.enter(method);
        assert criteria != null : "You must specify a criteria for audit";

        // no authorization control, up to client to control (it is readonly...)

        // delegate call to manager
        List<AuditLogDTO> queryResult = AuditManager.get().findAuditLogs(criteria);

        LOG.leave(method);
        return queryResult;
    }
    
    public void deleteAuditLogs(Bento criteria) throws SecurityException  {
        String method = "deleteAuditLogs";
        LOG.enter(method);
        assert criteria != null : "You must specify a criteria for deletion";

        // this is high-level stuff
        getAuthorizer().isAllowed(Action.ADMIN, "*");

        // delegate call to manager
        AuditManager.get().deleteAuditLogs(criteria, super.getAuthorizer().getPrincipal().getUserRef());

        LOG.leave(method);
    }


}
