/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.repit.rwt.server.security;

import ch.repit.rwt.client.audit.AuditLogDTO.AuditableAction;
import ch.repit.rwt.client.security.AuthenticationException;
import ch.repit.rwt.client.security.AuthenticationService;
import ch.repit.rwt.client.security.Principal;
import ch.repit.rwt.server.RwtRemoteServiceServlet;
import ch.repit.rwt.server.audit.AuditLog;
import ch.repit.rwt.server.audit.AuditLogAttribute;
import ch.repit.rwt.server.audit.AuditManager;
import ch.repit.rwt.server.persistence.PMF;
import ch.repit.rwt.server.util.Logging;
import javax.jdo.PersistenceManager;


/**
 *
 * @author tc149752
 */
public class AuthenticationServiceImpl extends RwtRemoteServiceServlet implements AuthenticationService {

    private static Logging LOG = new Logging(AuthenticationServiceImpl.class.getName());


    public Principal getConnectedPrincipal(String browserInfo) throws AuthenticationException {
        String method = "getConnectedPrincipal";
        LOG.enter(method);

        // if browserInfo is null, it means we wish to reauthenticate
        Principal principal = super.getAuthorizer(browserInfo==null).getPrincipal();

        if (principal != null && browserInfo != null) {
            // logs the login
            PersistenceManager pm = PMF.get().getPersistenceManager();
            try {
                AuditLog auditLog = new AuditLog(AuditableAction.LOGIN, principal.getUserRef());
                auditLog.getAuditLogAttributes().add(new AuditLogAttribute
                    ("IP", null, super.getThreadLocalRequest().getRemoteAddr()));
                auditLog.getAuditLogAttributes().add(new AuditLogAttribute
                    ("Browser", null, browserInfo));
                AuditManager.get().writeAuditLog(pm, auditLog);
            } finally {
                pm.close();
            }
        }

        LOG.leave(method);
        return principal;
    }

}
