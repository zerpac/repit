
package ch.repit.site.server.booking;

import ch.repit.rwt.client.BentoDef;
import ch.repit.rwt.client.BentoDefFactory;
import ch.repit.rwt.client.BentoStatus;
import ch.repit.rwt.client.Day;
import ch.repit.rwt.client.util.DateUtil;
import ch.repit.rwt.server.util.Logging;
import ch.repit.rwt.server.RwtRemoteServiceServlet;
import ch.repit.rwt.server.persistence.DataObject;
import ch.repit.rwt.server.persistence.JdoHelper;
import ch.repit.rwt.server.persistence.PMF;
import ch.repit.site.client.calendar.BookingDef;
import ch.repit.site.client.calendar.CalendarEventDef;
import java.io.IOException;
import java.util.List;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Cron task to be activated once a month to set ARCHIVE status to ACTIVE bookings and events in the past
 */
public class CalendarArchiverTask extends RwtRemoteServiceServlet
{

    private static Logging LOG = new Logging(CalendarArchiverTask.class.getName());


    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        String method = "service";
        LOG.enter(method);

        BentoDefFactory fac = BentoDefFactory.get();

        // date limit: last day of previous month
        Day today = DateUtil.today();
        /* code for last day of last month
        Day limit = today;
        do {
            limit = DateUtil.addDay(limit, -1);
        } while (limit.getMonth() == today.getMonth());
         */
        Day yesterday = DateUtil.addDay(today, -1);
        LOG.debug(method, "Will archive any event older than " + yesterday.toString());

        // for each bento type persisted, will load an write all objects
        BentoDef bookingDef = fac.getDef(BookingDef.TYPE);
        archiveObjects(bookingDef, yesterday);
        BentoDef eventDef = fac.getDef(CalendarEventDef.TYPE);
        archiveObjects(eventDef, yesterday);

        LOG.leave(method);
    }



    private void archiveObjects(BentoDef bentoDef, Day limit) {
        String method = "rewriteObjects";
        LOG.enter(method, bentoDef.getType());

        PersistenceManager pm = PMF.get().getPersistenceManager();
        try {
            Class typeClass = JdoHelper.get().getJdoClass(bentoDef.getType());
            Query query = pm.newQuery(typeClass);
            List<DataObject> calEvents;

            // adds date filter
            String filter = "status == '"+BentoStatus.ACTIVE+"' && toDay < limit";
            query.setFilter(filter);
            query.declareParameters("java.lang.Integer limit");
            calEvents = (List<DataObject>)query.execute(limit.getInteger());

            // rewrite each object that does not have a status set
            if (calEvents != null) {
                for (DataObject calEvent : calEvents) {

                    // update the status
                    calEvent.setStatus(BentoStatus.ARCHIVE.name());

                    // update last update
                    calEvent.setUpdated();

                    // save
                    pm.makePersistent(calEvent);

                    LOG.info(method, "Archived " + bentoDef.getType()+ ":" +calEvent.getId()
                            + " ("+calEvent.getDisplayName()+")");
                }
            }

        } finally {
            pm.close();
        }

        LOG.leave(method);
    }


}
