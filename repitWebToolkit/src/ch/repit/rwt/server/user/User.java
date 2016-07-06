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


// @PersistenceCapable(identityType = IdentityType.APPLICATION, detachable="true")
//                                 "detachable" did not work for some obscur reason
@PersistenceCapable(identityType = IdentityType.APPLICATION)
@Inheritance(customStrategy = "complete-table")
public class User extends BaseDataObject implements FileHolder, PrincipalDO {
    
    // @Unique  does not work with GAE...
    @Persistent
    private String login;
    // TBD: use com.google.appengine.api.users.User instead?
    
    @Persistent
    private String firstName;
    @Persistent
    private String lastName;
    @Persistent
    private String email;

    @Persistent
    private String privatePhone;
    @Persistent
    private String mobilePhone;
    @Persistent
    private String workPhone;
    @Persistent
    private String fax;

    @Persistent
    private String addressLine;
    @Persistent
    private String zipCode;
    @Persistent
    private String locality;
    @Persistent
    private String country;

    @Persistent
    private List<String> rolesRef;

    // photos
    @Persistent
    private UserPhoto userPhoto;

    // UserPref part
    @Persistent
    private Boolean weeklyReport;
    @Persistent
    private Boolean dailyReport;

    @Persistent
    private Boolean notifAll;
    @Persistent
    private Boolean notifMines;
    @Persistent
    private Boolean notifCreates;
    @Persistent
    private Boolean officialCommEmail;

    // constructor for creates
    public User() { }


    public void      setLogin(String login)         {   this.login = login; }
    public String    getLogin()                     {   return login;   }
    public void      setLastName(String lastName)   {   this.lastName = lastName;  }
    public String    getLastName()                  { return lastName;  }
    public void      setFirstName(String firstName) {  this.firstName = firstName;  }
    public String    getFirstName()                 {  return firstName;  }
    public void      setEmail(String email)         {  this.email = email; }
    public String    getEmail()                     {   return email;  }
    public void      setLocality(String locality)   {   this.locality = locality;   }
    public String    getLocality()                  {  return locality;   }

    public String getAddressLine() {
        return addressLine;
    }

    public void setAddressLine(String addressLine) {
        this.addressLine = addressLine;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getFax() {
        return fax;
    }

    public void setFax(String fax) {
        this.fax = fax;
    }

    public String getMobilePhone() {
        return mobilePhone;
    }

    public void setMobilePhone(String mobilePhone) {
        this.mobilePhone = mobilePhone;
    }

    public String getPrivatePhone() {
        return privatePhone;
    }

    public void setPrivatePhone(String privatePhone) {
        this.privatePhone = privatePhone;
    }

    public String getWorkPhone() {
        return workPhone;
    }

    public void setWorkPhone(String workPhone) {
        this.workPhone = workPhone;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

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

    public Boolean isDailyReport() {
        return dailyReport;
    }

    public void setDailyReport(Boolean dailyReport) {
        this.dailyReport = dailyReport;
    }

    public Boolean isNotifAll() {
        return notifAll;
    }

    public void setNotifAll(Boolean notifAll) {
        this.notifAll = notifAll;
    }

    public Boolean isNotifCreates() {
        return notifCreates;
    }

    public void setNotifCreates(Boolean notifCreates) {
        this.notifCreates = notifCreates;
    }

    public Boolean isNotifMines() {
        return notifMines;
    }

    public void setNotifMines(Boolean notifMines) {
        this.notifMines = notifMines;
    }

    public Boolean isOfficialCommEmail() {
        return officialCommEmail;
    }

    public void setOfficialCommEmail(Boolean officialCommEmail) {
        this.officialCommEmail = officialCommEmail;
    }
    
    public Boolean isWeeklyReport() {
        return weeklyReport;
    }

    public void setWeeklyReport(Boolean weeklyReport) {
        this.weeklyReport = weeklyReport;
    }

    public List<UserPhoto> getFiles() {
        List<UserPhoto> result = new ArrayList<UserPhoto>();
        if (userPhoto != null && userPhoto.getFileKey() != null)
            result.add(userPhoto);
        return result;
    }

    public UserPhoto createFileInstance() {
        if (userPhoto == null)
            userPhoto = new UserPhoto();  // TBD: or return same instance ???
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
