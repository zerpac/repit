/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.repit.rwt.server.util;

import ch.repit.rwt.client.util.Formaters;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 *
 * @author tc149752
 */
public class ServerFormaters extends Formaters {

    /*
    private static final DateFormat dateFormater = new SimpleDateFormat("d MMM yyyy");
    private static final DateFormat dateTimeFormater = new SimpleDateFormat("d MMM yyyy HH:mm:ss");
    private static final DateFormat dayMonthFormater = new SimpleDateFormat("d MMM");
*/

    private Map<DatePattern,DateFormat> dateFormatCache = new HashMap();

    public ServerFormaters() {}

    @Override
    public String formatDate(Date date, DatePattern pattern) {
        if (date == null)
            return "(date vide)";
        DateFormat df = dateFormatCache.get(pattern);
        if (df == null)
            dateFormatCache.put(pattern, df = new SimpleDateFormat(pattern.getPattern(),Locale.FRANCE));
        return df.format(date);
    }



    /*
    public static String format(Date date) {
        return dateFormater.format(date);
    }

    public static String formatWithTime(Date date) {
        return dateTimeFormater.format(date);
    }


    public static String format(Date from, Date to) {
        String fromStr = dateFormater.format(from);
        String toStr = dateFormater.format(to);
        if (fromStr.equals(toStr))
            return " du " + fromStr;
        else {
            // dont display mont and year twice if equals
            if (from.getYear() == to.getYear()) {
                if (from.getMonth() == to.getMonth()) {
                    fromStr = "" + from.getDate();
                } else {
                    fromStr = dayMonthFormater.format(from);
                }
            }
            return " du " + fromStr + " au " + toStr;
        }   
    }
*/

}
