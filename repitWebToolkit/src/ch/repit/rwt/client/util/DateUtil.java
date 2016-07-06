/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.repit.rwt.client.util;

import ch.repit.rwt.client.Day;
import ch.repit.rwt.client.logs.LogManager;
import java.util.Date;

/**
 *
 * @author tc149752
 */
public class DateUtil {

    public static Day today() {
        Date now = new Date();
        return new Day(now);
    }

    @Deprecated
    public static Date addMonth(Date date, int monthToAdd) {
        Date copy = new Date(date.getTime());
        copy.setMonth(copy.getMonth() + monthToAdd);
        return copy;
    }

    public static Day addMonth(Day day, int monthToAdd) {
        Date copy = day.getDate();
        Date newDate = addMonth(copy, monthToAdd);
        Day newDay = new Day(newDate);
        return newDay;
    }

    public static Day addDay(Day day, int daysToAdd) {
        Date dt = day.getDate();
        dt.setDate(dt.getDate() + daysToAdd);
        Day d = new Day(dt);
        return d;
    }
}
