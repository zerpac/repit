/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.repit.rwt.server.user;


import ch.repit.rwt.client.ObjectRef;
import ch.repit.rwt.client.user.UserDef;
import ch.repit.rwt.client.ContentTypeFamily;
import ch.repit.rwt.server.file.FileHolder;
import ch.repit.rwt.server.persistence.BaseDataObject;
import ch.repit.rwt.server.security.PrincipalDO;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;


/**
 * Similar to User, but without postal address and photo
 */
@PersistenceCapable(identityType = IdentityType.APPLICATION)
@Inheritance(customStrategy = "complete-table")
public class UserLight extends BaseDataObject implements FileHolder, PrincipalDO {
    
    @Persistent
    private String login;
    
    @Persistent
    private String firstName;
    @Persistent
    private String lastName;
    @Persistent
    private String email;

    @Persistent
    private List<String> rolesRef;

    // photos
    @Persistent
    private UserPhoto userPhoto;
    
//    // UserPref part
//    @Persistent
//    private Boolean weeklyReport;
//    @Persistent
//    private Boolean dailyReport;
//
//    @Persistent
//    private Boolean notifAll;
//    @Persistent
//    private Boolean notifMines;
//    @Persistent
//    private Boolean notifCreates;
    

    // constructor for creates
    public UserLight() { }


    public void      setLogin(String login)         {   this.login = login; }
    public String    getLogin()                     {   return login;   }
    public void      setLastName(String lastName)   {   this.lastName = lastName;  }
    public String    getLastName()                  { return lastName;  }
    public void      setFirstName(String firstName) {  this.firstName = firstName;  }
    public String    getFirstName()                 {  return firstName;  }
    public void      setEmail(String email)         {  this.email = email; }
    public String    getEmail()                     {   return email;  }

    public void setRolesRef(List<String> roles) {
        rolesRef = roles;
    }

    /*
     * Methods from PrincipalDO
     */

    public String getNickName() {
        return getLogin();
    }

    @Override
    public String getDisplayName() {
        return getFirstName() + " " + getLastName();
    }

    public List<String> getRolesRef() {
        return rolesRef;
    }

    public ObjectRef getObjectRef() {
        return new ObjectRef(UserDef.TYPE, getId());
    }
 


    /*
     * Methods from UserPref
     */
//
//    public Boolean isDailyReport() {
//        return dailyReport;
//    }
//
//    public void setDailyReport(Boolean dailyReport) {
//        this.dailyReport = dailyReport;
//    }
//
//    public Boolean isNotifAll() {
//        return notifAll;
//    }
//
//    public void setNotifAll(Boolean notifAll) {
//        this.notifAll = notifAll;
//    }
//
//    public Boolean isNotifCreates() {
//        return notifCreates;
//    }
//
//    public void setNotifCreates(Boolean notifCreates) {
//        this.notifCreates = notifCreates;
//    }
//
//    public Boolean isNotifMines() {
//        return notifMines;
//    }
//
//    public void setNotifMines(Boolean notifMines) {
//        this.notifMines = notifMines;
//    }
//
//    public Boolean isOfficialCommEmail() {
//        return officialCommEmail;
//    }
//
//    public void setOfficialCommEmail(Boolean officialCommEmail) {
//        this.officialCommEmail = officialCommEmail;
//    }
//
//    public Boolean isWeeklyReport() {
//        return weeklyReport;
//    }
//
//    public void setWeeklyReport(Boolean weeklyReport) {
//        this.weeklyReport = weeklyReport;
//    }

    public List<UserPhoto> getFiles() {
        List<UserPhoto> result = new ArrayList<UserPhoto>();
        if (userPhoto != null && userPhoto.getFileKey() != null)
            result.add(userPhoto);
        return result;
    }

    public UserPhoto createFileInstance() {
        if (userPhoto == null)
            userPhoto = new UserPhoto();  
        return userPhoto;
    } 


    private transient Set<ContentTypeFamily> allowedFileTypes = null;

    public Set<ContentTypeFamily> listAllowedFileTypes() {
        if (allowedFileTypes == null || allowedFileTypes.size() == 0) {
            allowedFileTypes = new HashSet<ContentTypeFamily>();
            allowedFileTypes.add(ContentTypeFamily.IMAGE);
        }
        return allowedFileTypes;
    }

    public int getImagePreviewSize() {
        return 50;
    }

    public long getFileMaxSizeInBytes() {
        return 200000;
    }
}
