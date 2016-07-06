/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.repit.rwt.client.audit;

import java.io.Serializable;

/**
 *
 * @author tc149752
 */
public class AuditLogAttributeDTO  implements Serializable {

    private String attributeName;
    private String oldValue;    // as String
    private String newValue;    // as String

    public AuditLogAttributeDTO() {}

    public AuditLogAttributeDTO(String attributeName, String oldValue, String newValue) {
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

    @Override
    public String toString() {
        return attributeName + "{old:" + oldValue + ";new:" + newValue + "}";
    }
}
