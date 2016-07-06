/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.repit.rwt.client.audit;

import com.google.gwt.user.client.rpc.RemoteService;
import ch.repit.rwt.client.security.SecurityException;
import ch.repit.rwt.client.Bento;

import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import java.util.List;

/**
 *
 * @author tc149752
 */
@RemoteServiceRelativePath("auditQueryService")
public interface AuditQueryService extends RemoteService {

    public List<AuditLogDTO> listAuditLogs(Bento criteria) throws SecurityException;

    public void deleteAuditLogs(Bento criteria) throws SecurityException;

}
 