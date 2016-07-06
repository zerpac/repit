/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.repit.rwt.server.util;

import ch.repit.rwt.client.BentoDef;
import ch.repit.rwt.client.BentoDefFactory;
import ch.repit.rwt.client.BentoStatus;
import ch.repit.rwt.server.RwtRemoteServiceServlet;
import ch.repit.rwt.server.persistence.DataObject;
import ch.repit.rwt.server.persistence.JdoHelper;
import ch.repit.rwt.server.persistence.PMF;
import java.io.IOException;
import java.util.List;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author tc149752
 */
public class UpgradeDataTask extends RwtRemoteServiceServlet
{

    private static Logging LOG = new Logging(UpgradeDataTask.class.getName());


    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        String method = "service";
        LOG.enter(method);

        // for each bento type persited, will load an write all objects
        /*
        for ( BentoDef bentoDef : BentoDefFactory.get().getDefs() ) {
            if (bentoDef.getJdoClassName() != null) {
                rewriteObjects(bentoDef);
                LOG.info(method, "Done rewriting " + bentoDef.getType());
            }
        }
*/
        LOG.info(method, "DID NOTHING!!! ");
        LOG.leave(method);
    }


/*
    private void rewriteObjects(BentoDef bentoDef) {
        String method = "rewriteObjects";
        LOG.enter(method, bentoDef.getType());

        PersistenceManager pm = PMF.get().getPersistenceManager();
        try {
            Class typeClass = JdoHelper.get().getJdoClass(bentoDef.getType());
            Query query = pm.newQuery(typeClass);
            List<DataObject> dataObjList;

            // adds additional filter (if any)
            if (bentoDef.getJdoFilter() != null) 
                query.setFilter(bentoDef.getJdoFilter());

            // get the list
            dataObjList = (List<DataObject>)query.execute();

            // rewrite each object that does not have a status set
            if (dataObjList != null) {
                for (DataObject obj : dataObjList) {
                    // update the status
                    String currentStatusStr = obj.getStatus();
                    if (currentStatusStr == null || currentStatusStr.length() == 0) {
                        // here we need to rewrite this object
                        BentoStatus currentStatus;
                        if (obj.isActive()) {
                            if (obj.isDraft())
                                currentStatus = BentoStatus.DRAFT;
                            else
                                currentStatus = BentoStatus.ACTIVE;
                        } else {
                            currentStatus = BentoStatus.TRASH;
                            // no archive yet
                        }

                        obj.setStatus(currentStatus.name());
                        LOG.info(method, "upgraded object " + bentoDef.getType() +":" + obj.getId()
                                + " to status:" + currentStatus.name());
                    }
                    pm.makePersistent(obj);
                }
            }

        } finally {
            pm.close();
        }

        LOG.leave(method);
    }
*/

}
