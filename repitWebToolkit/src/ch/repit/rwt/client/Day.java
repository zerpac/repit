/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.repit.rwt.client;

import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author tc149752
 */
public class Day implements Serializable {

    private Integer dayRep;  // yyyyMMdd, eg 20090422

    private transient int dayOfMonth, month, year;

    private Day() { }  // req for serialization...

    public Day(Integer dayRep) {
        this.dayRep = dayRep;
        if (dayRep != null) {
            dayOfMonth = dayRep % 100;
            month = (dayRep / 100) % 100;
            year = dayRep / 10000;
        }
    }

    public Day(Date date) {
        if (date != null) {
            dayOfMonth = date.getDate();
            month = date.getMonth()+1;
            year = date.getYear()+1900;
            dayRep = year  * 10000 +
                     month * 100 +
                     dayOfMonth;
        }
        else
            dayRep = null;
    }

    /**
     * from 1 (Monday) to 7 (Sunday)
     */
    public int getDayOfWeek() {
        Date d = getDate();
        if (d == null)
            return 0;
        if (d.getDay() == 0)
            // sunday
            return 7;
        return d.getDay();
    }

    /**
     * From 1 to max 31
     */
    public int getDayOfMonth() {
        return dayOfMonth;
    }

    /**
     * from 1 to 52
     */
    public int getWeekOfYear() {
        throw new UnsupportedOperationException("Is this needed?");
    }


    /**
     * From 1 to max 12
     */
    public int getMonth() {
        return month;
    }

    /**
     * in full notation (2009, etc, not 87, 09)
     */
    public int getYear() {
        return year;
    }


    public Date getDate() {
        if (dayRep == null)
            return null;
        else
            return new Date(getYear()-1900,getMonth()-1,getDayOfMonth(),12,0,0);
    }

    public Integer getInteger() {
        return dayRep;
    }


    @Override
    public String toString() {
        return "" + dayRep;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Day other = (Day) obj;
        if (this.dayRep != other.dayRep && (this.dayRep == null || !this.dayRep.equals(other.dayRep))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 83 * hash + (this.dayRep != null ? this.dayRep.hashCode() : 0);
        return hash;
    }

    public boolean before(Day otherDay) {
        if (getInteger() == null)
            if (otherDay == null || otherDay.getInteger() == null)
                return false;
            else
                return true;
        else
            if (otherDay == null || otherDay.getInteger() == null)
                return false;
            else
                return ( getInteger() <  otherDay.getInteger() );
    }

    public boolean after(Day otherDay) {
        if (getInteger() == null)
            return false;
        else
            if (otherDay == null || otherDay.getInteger() == null)
                return true;
            else
                return ( getInteger() > otherDay.getInteger() );
    }

}
