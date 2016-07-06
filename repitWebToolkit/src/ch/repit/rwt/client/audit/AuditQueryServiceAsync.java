/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.repit.rwt.client.audit;

import ch.repit.rwt.client.Bento;
import com.google.gwt.user.client.rpc.AsyncCallback;
import java.util.List;

/**
 *
 * @author tc149752
 */
public interface AuditQueryServiceAsync {


    public void listAuditLogs(Bento criteria, AsyncCallback<List<AuditLogDTO>> callback);

    public void deleteAuditLogs(Bento criteria, AsyncCallback<Void> callback);

}
