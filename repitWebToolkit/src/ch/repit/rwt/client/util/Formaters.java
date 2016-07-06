/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.repit.rwt.client.util;

import ch.repit.rwt.client.Day;
//import com.google.gwt.core.client.GWT;
import com.google.gwt.core.shared.GWT;
import java.util.Date;

/**
 *
 * @author tc149752
 */
public abstract class Formaters {

    private static Formaters formaters = null;

    public static Formaters get() {
        if (formaters == null) {
            if (GWT.isClient())
                formaters = new ClientFormaters();
        }
        return formaters;
    }

    /**
     * only works in server mode. Hack to register the server class, that will neither
     * compile in client code nor be loadable via introspection
     * @param serverFormaters
     */
    public static void register(Formaters serverFormaters) {
        if (!GWT.isClient())
            formaters = serverFormaters;
    }

    
    /**
     * Implementation depends on wether client or server side
     */
    public abstract String formatDate(Date date, DatePattern pattern);


    /**
     * Formats the date relative to the current date: only time if today, etc...
     * @param date
     * @return
     */
    public String formatDate(Date date) {
        if (date == null)
            return "";

        long currentDateTime = (new Date()).getTime();

        // in future, or older than six month, display only date but with year
        if ( currentDateTime < date.getTime() ||
            (currentDateTime - date.getTime() > (long)(365L * 24L * 60L * 60L * 1000L))) // if formated date older than one year
            return formatDate(date, DatePattern.MONTH_YEAR);

        // older than one week, display only date without year
        else if (currentDateTime - date.getTime() > (7 * 24 * 60 * 60 * 1000)) // if formated date older than 7 days
            return formatDate(date, DatePattern.DAY_MONTH);

        // one week to one day, display date (without year) and hour + min
        else if (date.getDate() != (new Date()).getDate())
            return formatDate(date, DatePattern.DAY_MONTH_TIME);

        // today, display only hour
        else
            return "Aujourd'hui à " + formatDate(date, DatePattern.TIME);
        
    }

    public String formatDate(Day date) {
        if (date == null)
            return "";
        return formatDate(date.getDate());
    }
    public String formatDate(Day date, DatePattern pattern) {
        if (date == null)
            return "";
        return formatDate(date.getDate(),pattern);
    }


    public String formatDateRange(Day from, Day to) {
        return formatDateRange( (from==null)?(Date)null:from.getDate(),
                                (to==null)?(Date)null:to.getDate());
    }

    public String formatDateRange(Date from, Date to) {
        String fromStr = formatDate(from, DatePattern.DATE);
        String toStr = formatDate(to, DatePattern.DATE);
        if (fromStr.equals(toStr))
            return " du " + fromStr;
        else {
            // dont display mont and year twice if equals
            if (from.getYear() == to.getYear()) {
                if (from.getMonth() == to.getMonth()) {
                    fromStr = formatDate(from, DatePattern.DAY);
                } else {
                    fromStr = formatDate(from, DatePattern.DAY_MONTH);
                }
            }
            return " du " + fromStr + " au " + toStr;
        }
    }


    public enum DatePattern {

        FULL       ("dd.MM.yyyy HH:mm:ss"),
        TIME       ("HH:mm"),
        DATE_TIME  ("d MMM yyyy HH:mm"),
        DAY_MONTH_TIME  ("d MMM HH:mm"),
        DATE       ("d MMM yyyy"),
        DAY        ("d"),
        DAY_MONTH  ("d MMM"),
        MONTH_YEAR ("MMMM yyyy"),
        YEAR       ("yyyy");


        private String pattern;

        DatePattern(String pattern) {
            this.pattern = pattern;
        }

        public String getPattern() {
            return pattern;
        }
    }


}
