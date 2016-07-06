/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.repit.rwt.server.audit;

import com.google.appengine.api.datastore.Key;
import javax.jdo.annotations.EmbeddedOnly;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;


/**
 *
 * @author tc149752
 */
@PersistenceCapable(identityType = IdentityType.APPLICATION)
//TBD: try to set @EmbeddedOnly
public class AuditLogAttribute {
    
    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Key _key;

    @Persistent
    private String attributeName;

    @Persistent
    private String oldValue;

    @Persistent
    private String newValue;

    public AuditLogAttribute() {  }

    public AuditLogAttribute(String attributeName, String oldValue, String newValue) {
        this.attributeName = attributeName;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    public String getAttributeName() {
        return attributeName;
    }

    public String getNewValue() {
        return newValue;
    }

    public String getOldValue() {
        return oldValue;
    }

    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    
}

