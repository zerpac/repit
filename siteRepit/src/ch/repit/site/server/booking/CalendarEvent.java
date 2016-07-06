/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.repit.site.server.booking;


import ch.repit.rwt.client.Day;
import ch.repit.rwt.client.util.Formaters;
import ch.repit.rwt.server.persistence.BaseDataObject;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;


@PersistenceCapable(identityType = IdentityType.APPLICATION)
@Inheritance(customStrategy = "complete-table")
public class CalendarEvent extends BaseDataObject {

    @Persistent
    private String eventTitle;

    @Persistent
    private String eventType;

    @Persistent
    private Integer fromDay;
    @Persistent
    private Integer toDay;

    @Persistent
    private String description;


    // constructor for creates
    public CalendarEvent() { }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getFromDay() {
        return fromDay;
    }

    public void setFromDay(Integer fromDay) {
        this.fromDay = fromDay;
    }

    public Integer getToDay() {
        return toDay;
    }

    public void setToDay(Integer toDay) {
        this.toDay = toDay;
    }

    public String getEventTitle() {
        return eventTitle;
    }

    public void setEventTitle(String eventTitle) {
        this.eventTitle = eventTitle;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

   
    @Override
    public String getDisplayName() {
        // TBD-NTH : add type in display name
        return "Ev&eacute;nement \"" + getEventTitle() + "\"" +
                Formaters.get().formatDateRange(new Day(getFromDay()), new Day(getToDay()));
    }

}
