/*
 * InscriptionManager.java
 *
 * Created on 10. avril 2002, 22:28
 */

package ch.repit.site.client.calendar;

import ch.repit.rwt.client.Bento;
import ch.repit.rwt.client.BentoDef;
import ch.repit.rwt.client.BentoDefFactory;
import ch.repit.rwt.client.BentoStatus;
import ch.repit.rwt.client.Day;
import ch.repit.rwt.client.ObjectRef;
import ch.repit.rwt.client.persistence.CacheManager;
import ch.repit.rwt.client.util.DateUtil;
import ch.repit.rwt.client.user.UserDef;
import com.google.gwt.core.client.GWT;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;



/**
 * Links to previous "inscription" implementation, as it was complexe and working..
 */
public class BookingManager {
    
    private static BookingManager s_instance = new BookingManager();

    /**
     * this is a facade object in front of the bento representation of a booking. It
     * was designed to bring as little change as possible to the display of repit 1.1
     * web site.
     */
    public class Booking implements Comparable
    {
        public static final int TYPE_INSCRIPTION = 0;
        public static final int TYPE_RESERVATION = 1;
        public static final int TYPE_INFORMATION = 2;  // NOT NEEDED ANYMORE

        private Bento bookingBento;

        public Booking(Bento bookingBento) {
            this.bookingBento = bookingBento;
        }


        /**
         * @param mondayOfWeek is the first day of the week we're interested in
         */
        public boolean isOverlapping(Day mondayOfWeek) {
            // returns true if:
            // endDate > mondayOfWeek & startDate < SundayOfWeek
            Day sundayOfWeek = DateUtil.addDay(mondayOfWeek, 6);
            boolean overlap = this.isOverlapping(mondayOfWeek, sundayOfWeek);
            return overlap;
        }


        /**
         * returns true if endDate > mondayOfWeek & startDate < SundayOfWeek
         */
        public boolean isOverlapping(Day from, Day to) {
            
            boolean overlap = (!getEndDate().before(from) &&
                               !getStartDate().after(to));
            return overlap;
        }


        /**
         * Retrieve the day of the week the inscription starts (1Mon-7Sun), or 0
         * if started before; and the day it stops (1Mon-7Sun), or 8 if ends after.
         * Result is in an int array of size 2.
         */
        public int[] getStartAndEnd(Day mondayOfWeek) {
            int[] result = new int[2];
            Day day = mondayOfWeek;
            result[0] = 0;
            while (!day.after(getStartDate())) {
                result[0]++;
                day = DateUtil.addDay(day, 1);
            }
            result[1] = result[0];
            while (result[1] < 8 && !day.after(getEndDate())) {
                result[1]++;
                day = DateUtil.addDay(day, 1);
            }
            return result;
        }

        /**
         * returns true if there is at least one day between the 2 inscriptions
         */
        public boolean isSeparate(Booking other) {
            return ( (this.getStartDate().after(other.getEndDate())) ||
                     (this.getEndDate().before(other.getStartDate())) );
        }

        public Day getStartDate() {
            return bookingBento.get(BookingDef.ATTR_FROM_DAY).getDay();
        }

        public Day getEndDate() {
            return bookingBento.get(BookingDef.ATTR_TO_DAY).getDay();
        }


        // TBD: shouldn't we delegate to BentoDef.getCommonName ???
        public String getLabel() {
            String result = "";

            // who
            BentoDef ud = BentoDefFactory.get().getDef(UserDef.TYPE);
            Bento user = CacheManager.get().getCachedObject(new ObjectRef(bookingBento.getDisplayValue(BookingDef.ATTR_USER)));
            if (user != null) {
                result += ud.getCommonName(user);
            }

            // how much
            if (getBento().get(BookingDef.ATTR_RESERVATION).getBoolean())
                result += " (réservation)";
            else {
                int num = bookingBento.get(BookingDef.ATTR_NUMBER_PEOPLE).getInteger();
                result += " (" + num + " personne" + (num>1?"s":"") + ")";
            }

            // description 
            String desc = getBento().get(BookingDef.ATTR_DESCRIPTION).getString();
            if (desc != null) {
                if (desc.length() > 40)
                    desc = desc.substring(0, 40) + " [...]";
                result += " <small><i>" + desc + "</i></small> ";
            }

            // nombre de commentaires
            if (getBento().getComments() != null) {
                int commCount = getBento().getComments().length;
                if (commCount > 0) {
                    result += " <small><i>(" + commCount + " <img src='"
                            +GWT.getModuleBaseURL()+"icons/comment-icon.png' />)</i></small>";
                }
            }

            return result;
        }


        public Bento getBento() {
            return bookingBento;
        }

        public int compareTo(Object o) {
            Booking other = (Booking)o;
            if (other.getStartDate().before(this.getStartDate()))
                return 1;
            else if (other.getStartDate().after(this.getStartDate()))
                return -1;
            else
                return 0;
        }

        @Override
        public String toString() {
            return "Booking{" + bookingBento + "}";
        }

        /**
         *
         * @return A null string if OK, or a reason indicating why the inscription is not valid
         */
        /*
        public String checkValidity() {

            String result = null;

            Iterator others = InscriptionManager.getInstance().getInscriptionOvelappingDates(this.getStartDate(), this.getEndDate());

            // INSCRIPTION:
            if (this.getType() == TYPE_INSCRIPTION) {

                int nbInscr = 0;

                while (others.hasNext() && result == null) {
                    Booking i = (Booking)others.next();

                    //  a. are there reservations during this period
                    if (i.getType() == Booking.TYPE_RESERVATION)
                        result = "Le chalet est déjà réservé (par " + i.getMember().getIdentity() + ") durant tout ou partie des dates demandées";

                    //  b. (optional) are there too many members already up ? (hard to check...)

                    else if (i.getType() == Inscription.TYPE_INSCRIPTION) {
                        nbInscr++;
                        if (nbInscr >= 4) {
                            result = "Le chalet est plein durant tout ou partie des dates demandées";
                        }
                    }


                    //  c. (optional) if during vacations event, at most one week
                }

            }

            // RESERVATION:
            else if (this.getType() == TYPE_RESERVATION) {

                //  c. Are there already any kind of inscriptions at the dates (reserv, inscr, event)?
                if (others.hasNext())
                    result = "Impossible de réserver le chalet, il y a déjà d'autres inscriptions ou vacances officielles à ces dates";
            }

            return result;
        }
        */

    }


    /** subclass holding several inscriptions that would fit on the same line
     * because they don't span. There must be at least one day between them.
     * The inscriptions are sorted
     */
    public class BookingLine {
        
        private SortedSet set;
        
        BookingLine(Booking i) {
            set = new TreeSet();
            set.add(i);
        }
        
        public void add(Booking i) {
            set.add(i);
        }
        
        public boolean canBeInserted(Booking inscr) {
            Iterator i = set.iterator();
            boolean result = true;
            while (result && i.hasNext()) 
                result = result & ((Booking)i.next()).isSeparate(inscr);
            return result;
        }
        
        public Iterator iterator() {
            return set.iterator();
        }

        @Override
        public String toString() {
            return "CalendarBookingLine{" + set + "}";
        }
    }
    
    private BookingManager() {     }
    
    public static BookingManager get() {
        return s_instance;
    }


    
    
    /**
     * Returns a List, to be displayed on individual lines
     */
    public List<BookingLine> getBookingsByWeek(Day mondayOfWeek) {
        List<BookingLine> match = new ArrayList<BookingLine>();
        List<Bento> bookings = CacheManager.get().getCachedObjects(BookingDef.TYPE);
        if (bookings != null)
            for (Bento b : bookings) {
                if (b.getStatus()!=BentoStatus.TRASH) {
                    Booking inscr = new Booking(b);    // TBD: this is costly, as most of the time not needed!
                    if (inscr.isOverlapping(mondayOfWeek)) {
                        Iterator m = match.iterator();
                        boolean ok = false;
                        while (!ok && m.hasNext()) {
                            BookingLine il = (BookingLine)m.next();
                            if (il.canBeInserted(inscr)) {
                                il.add(inscr);
                                ok = true;
                            }
                        }
                        if (!ok)
                            match.add(new BookingLine(inscr));
                    }
                }
            }
        return match;
    }


    public List<Bento> getCalendarEventsForDay(Day day) {
        List<Bento> calEvents = CacheManager.get().getCachedObjects(CalendarEventDef.TYPE);
        List<Bento> results = new ArrayList<Bento>();
        if (calEvents != null)
            for (Bento b : calEvents) {
                if (b.getStatus()!=BentoStatus.TRASH) {
                    Day from = b.get(CalendarEventDef.ATTR_FROM_DAY).getDay();
                    Day to = b.get(CalendarEventDef.ATTR_TO_DAY).getDay();
                    if ( !day.before(from) && !day.after(to) )  {
                        results.add(b);
                    }
                }
            }
        return results;
    }


    /**
     * @param fromDate
     * @param toDate
     * @return an list of all inscriptions (from all types) that overlapp the specified period.
     */
    public List<Booking> getBookingsAtDate(Day fromDate, Day toDate) {
        List<Bento> allBookings = CacheManager.get().getCachedObjects(BookingDef.TYPE);
        List<Booking> results = new ArrayList<Booking>();
        if (allBookings != null)
            for (Bento b : allBookings) {
                if (b.getStatus()!=BentoStatus.TRASH) {
                    Day from = b.get(BookingDef.ATTR_FROM_DAY).getDay();
                    Day to   = b.get(BookingDef.ATTR_TO_DAY).getDay();
                    if ( !toDate.before(from) && !fromDate.after(to) )  {
                        results.add(new Booking(b));
                    }
                }
            }
        return results;
    }
    
}
