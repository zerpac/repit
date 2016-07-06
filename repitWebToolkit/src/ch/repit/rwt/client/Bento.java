/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.repit.rwt.client;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;


// extends map or contain map ???
// - should not remove attributes...
// => leave extends map for now...
public class Bento extends HashMap<String,BentoAttribute>  implements Serializable
{
    private String    type;
    private Long      objectId = null;
    private Date      lastUpdate = null;
    private ObjectRef ownerRef;
    private BentoStatus status;
    private BentoComment[] comments;
    private FileDescriptor[] attachedFiles;

    private Bento() {}  // DO NOT USE, 4 SERIALIZATION

    // to be called by JDO
    public Bento(String bentoType, Long objectID, ObjectRef owner, Date lastUpdate) {
        this.type = bentoType;
        this.objectId   = objectID;
        this.lastUpdate = lastUpdate;
        this.ownerRef      = owner;
    }

    // to be called when creating obj from client
    public Bento(String bentoType) {
        this.type = bentoType;
    }

    public String getType() {
        return type;
    }
    
    public ObjectRef getRef() {
        if (objectId == null || type == null)
            return null;
        return new ObjectRef(type, objectId);
    }

    public ObjectRef getOwnerRef() {
        return ownerRef;
    }

    public void setOwnerRef(ObjectRef owner) {
        this.ownerRef = owner;
    }

    public BentoStatus getStatus() {
        return status;
    }

    public void setStatus(BentoStatus status) {
        this.status = status;
    }

    // needed server-side
    public Long getId() {
        return objectId;
    }
    // needed server-side
    public Date getLastUpdate() {
        return lastUpdate;
    }

    // shortcut
    public String getDisplayValue(String attrName) {
        BentoAttribute attr = get(attrName);
        if (attr != null)
            return attr.getDisplayValue();
        else
            return "";
    }

    public BentoComment[] getComments() {
        return comments;
    }

    // not to be used on the client side. Comment directly with Persistence Service
    public void setComments(BentoComment[] comments) {
        this.comments = comments;
    }

    public FileDescriptor[] getAttachedFiles() {
        return attachedFiles;
    }
    
    // not to be used on the client side. Comment directly with Persistence Service
    public void setAttachedFiles(FileDescriptor[] attachedFiles) {
        this.attachedFiles = attachedFiles;
    }

    public BentoDef getDef() {
        return BentoDefFactory.get().getDef(type);
    }

    
}
