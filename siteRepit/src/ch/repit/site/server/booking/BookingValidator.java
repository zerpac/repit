/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.repit.site.server.booking;

import ch.repit.site.client.calendar.BookingDef;
import ch.repit.site.client.calendar.CalendarEventDef;
import ch.repit.site.client.calendar.CalendarEventDef.CalEventType;
import ch.repit.rwt.client.Bento;
import ch.repit.rwt.client.BentoDef;
import ch.repit.rwt.client.BentoDefFactory;
import ch.repit.rwt.client.BentoStatus;
import ch.repit.rwt.client.Day;
import ch.repit.rwt.client.persistence.ValidationException;
import ch.repit.rwt.server.persistence.DataObject;
import ch.repit.rwt.server.persistence.PMF;
import ch.repit.rwt.server.persistence.Validator;
import ch.repit.rwt.server.util.Logging;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

/**
 *
 * @author tc149752
 */
public class BookingValidator implements Validator {

    private static Logging LOG = new Logging(BookingValidator.class.getName());


    
    public void validate(Bento object) throws ValidationException {

        String method="validate";

        if (object == null) {
            throw new ValidationException("Object is null");
        }

        if (!object.getType().equals(BookingDef.TYPE)) {
            throw new ValidationException("Object not of type Booking");
        }

        Day from = object.get(BookingDef.ATTR_FROM_DAY).getDay();
        Day to   = object.get(BookingDef.ATTR_TO_DAY).getDay();
        boolean isReserve = object.get(BookingDef.ATTR_RESERVATION).getBoolean();

        // check the date order
        if (from.after(to)) {
            throw new ValidationException("Date de fin avant date de début");
        }

        PersistenceManager pm = PMF.get().getPersistenceManager();
        try {

            // 1. check that there are no reservation at the dates
            BookingDef bookingDef = (BookingDef)BentoDefFactory.get().getDef(BookingDef.TYPE);
            List spaningBookings = fetchObjects(pm, bookingDef, from, to, object.getId());

            if (spaningBookings != null && !spaningBookings.isEmpty()) {
                if (isReserve) {
                    throw new ValidationException("Il y a déjà des inscriptions durant votre réservation");
                }

                else {
                    for (Object obj : spaningBookings) {
                        Booking b = (Booking)obj;
                        if (b.isIsReservation()) {
                            // TBD: ... and other owner...
                            throw new ValidationException("Il y a une réservation durant votre inscription");
                        }
                    }
                }
            }

            // 2. check the Calendar events (Manage should be able to overrule)
            CalendarEventDef calEventDef = (CalendarEventDef)BentoDefFactory.get().getDef(CalendarEventDef.TYPE);
            List spaningEvents = fetchObjects(pm, calEventDef, from, to, null);

            if (spaningEvents != null && !spaningEvents.isEmpty()) {
                
                for (Object obj : spaningEvents) {
                    CalendarEvent event = (CalendarEvent)obj;
                    CalEventType eventType = CalEventType.valueOf(event.getEventType());
                    if (!eventType.isReservationAllowed()) {
                        if (isReserve) {
                            // not authorized to enter reserve during this event
                            throw new ValidationException("Réservation interdite durant l'évenement " + event.getEventTitle() +
                                    ". Veuillez obtenir l'accord du comité et passer par les inscriptions téléphoniques");
                        }

                        /* Disabled (and bugged anyways, at least NPE since migration to Days)
                        else {
                            // not authorized to make bookings longer than a week over this event
                            // TBD: should we disable this ???
                            Day laterFrom = from;
                            Day tmp;
                            if ( (tmp = new Day(event.getFromDay())) != null && tmp.after(from))
                                laterFrom = tmp;
                            Day earlierTo = to;
                            if ( (tmp = new Day(event.getToDate())) != null && tmp.before(to))
                                earlierTo = tmp;
                            if ( (earlierTo.getDate().getTime() - laterFrom.getDate().getTime()) / (1000*60*60*24) > 7 ) {
                                throw new ValidationException("Inscription de plus de 7 jours interdite pendant '" + event.getEventTitle() +
                                    "'. Veuillez obtenir l'accord du comité et passer par les inscriptions téléphoniques (ou faire 2 inscriptions, et être prêt à renoncer à un des deux séjours)");
                            }
                        }
                         */
                    }
                }
            }

        } finally {
            pm.close();
        }

        LOG.info(method, "Object validated");
    }


    /*
     * Fetch list of active objects of specified type defined in the specified date range
     */
    private List<DataObject> fetchObjects(PersistenceManager pm, BentoDef bentoDef,
                                          Day from, Day to, Long excludeId) {
        
        String method="fetchBentoList";
        List results = new ArrayList();

        try {
            Map params = new HashMap();
            Query jdoQuery = pm.newQuery(Class.forName(bentoDef.getJdoClassName()));

            // !!! "Only one inequality filter per query is supported." !!!
            String filter = " toDay > inputFromDay " +
                    " && status == '"+BentoStatus.ACTIVE+"'";
            
            params.put("inputFromDay", from.getInteger());
            jdoQuery.setFilter(filter);
            jdoQuery.declareParameters("java.util.Date inputFromDay");
            List<DataObject> objectList = (List<DataObject>)pm.newQuery(jdoQuery).executeWithMap(params);

            // filter manually the other date
            if (objectList!=null && !objectList.isEmpty()) {
                for (DataObject dobj : objectList) {
                    if (excludeId == null || !excludeId.equals(dobj.getId())) {
                        if (dobj instanceof Booking) {
                            Booking b = (Booking)dobj;
                            if (new Day(b.getFromDay()).before(to)) {
                                results.add(b);
                            }
                        } else if (dobj instanceof CalendarEvent) {
                            CalendarEvent b = (CalendarEvent)dobj;
                            if (new Day(b.getFromDay()).before(to)) {
                                results.add(b);
                            }
                        }
                    }
                }
            }

        } catch (ClassNotFoundException ex) {
            LOG.error(method, "Unable to load class " + bentoDef.getJdoClassName(), ex);
            throw new RuntimeException("Unable to load class " + bentoDef.getJdoClassName(), ex);
        } 

        return results;
    }


}
