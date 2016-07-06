/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.repit.rwt.client;

import java.io.Serializable;

/**
 *
 * @author tc149752
 */
public class ObjectRef implements Serializable {

    private String objectType;
    private Long objectId;

    protected ObjectRef() {}  // Serializable...
    
    public ObjectRef(String objectType, Long objectId) {
        this.objectId = objectId;
        this.objectType = objectType;
    }

    public ObjectRef(String objectRefString) {
        if (objectRefString != null && objectRefString.contains(":")) {
            String[] split = objectRefString.split(":");
            this.objectId = Long.parseLong(split[1]);
            this.objectType = split[0];
        }
    }

    public Long getId() {
        return objectId;
    }

    public String getType() {
        return objectType;
    }


    @Override
    public String toString() {
        return objectType + ":" + objectId;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ObjectRef other = (ObjectRef) obj;
        if ((this.objectType == null) ? (other.objectType != null) : !this.objectType.equals(other.objectType)) {
            return false;
        }
        if (this.objectId != other.objectId && (this.objectId == null || !this.objectId.equals(other.objectId))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + (this.objectType != null ? this.objectType.hashCode() : 0);
        hash = 97 * hash + (this.objectId != null ? this.objectId.hashCode() : 0);
        return hash;
    }

}
