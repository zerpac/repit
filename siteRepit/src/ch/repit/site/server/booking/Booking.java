/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.repit.site.server.booking;


import ch.repit.rwt.client.Day;
import ch.repit.rwt.client.ObjectRef;
import ch.repit.rwt.client.persistence.ObjectNotFoundException;
import ch.repit.rwt.server.persistence.JdoHelper;
import ch.repit.rwt.client.util.Formaters;
import ch.repit.rwt.server.persistence.BaseDataObject;
import java.util.ArrayList;
import java.util.List;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;


@PersistenceCapable(identityType = IdentityType.APPLICATION)
@Inheritance(customStrategy = "complete-table")
public class Booking extends BaseDataObject {
 
    @Persistent
    private String userRef;
    
    @Persistent
    private Integer fromDay;
    @Persistent
    private Integer toDay;
    @Persistent
    private String fromHour;
    @Persistent
    private String toHour;
    @Persistent
    private Integer numberPeople;

    @Persistent
    private String description;
    @Persistent
    private Boolean isReservation;

    @Persistent
    private List<String> prefLivingRoom = new ArrayList<String>();
    @Persistent
    private List<String> prefDormingRoom = new ArrayList<String>();
    

    // constructor for creates
    public Booking() { }

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

    public Boolean isIsReservation() {
        return isReservation;
    }

    public void setIsReservation(Boolean isReservation) {
        this.isReservation = isReservation;
    }

    public Integer getNumberPeople() {
        return numberPeople;
    }

    public void setNumberPeople(Integer numberPeople) {
        this.numberPeople = numberPeople;
    }

    public String getUserRef() {
        return userRef;
    }

    public void setUserRef(String userRef) {
        this.userRef = userRef;
    }

    public String getFromHour() {
        return fromHour;
    }

    public void setFromHour(String fromHour) {
        this.fromHour = fromHour;
    }

    public String getToHour() {
        return toHour;
    }

    public void setToHour(String toHour) {
        this.toHour = toHour;
    }

    public List<String> getPrefDormingRoom() {
        return prefDormingRoom;
    }

    public void setPrefDormingRoom(List<String> prefDormingRoom) {
        this.prefDormingRoom = prefDormingRoom;
    }

    public List<String> getPrefLivingRoom() {
        return prefLivingRoom;
    }

    public void setPrefLivingRoom(List<String> prefLivingRoom) {
        this.prefLivingRoom = prefLivingRoom;
    }

    @Override
    public String getDisplayName() {
        String user = getUserRef();
        try {
            user = JdoHelper.get().getDataObject(new ObjectRef(getUserRef())).getDisplayName();
        } catch (ObjectNotFoundException e) {
            // just ignore it
        }
        return (isIsReservation()?"R&eacute;servation":"Inscription") +
                " de " + user + (isIsReservation()?" (chalet complet) ":(" (" + getNumberPeople() + " pers.) "))
                + Formaters.get().formatDateRange(new Day(getFromDay()), new Day(getToDay()));
    }

}
