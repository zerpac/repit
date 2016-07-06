package ch.repit.rwt.server.audit;

import ch.repit.rwt.client.ObjectRef;
import ch.repit.rwt.client.audit.AuditLogDTO;
import ch.repit.rwt.client.audit.AuditLogDTO.AuditableAction;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 * JDO class containing audit events
 */
@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class AuditLog {

    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Long id;

    @Persistent
    private Date eventDate;

    @Persistent
    private String action;  // enum AuditableAction

    @Persistent
    private String author;  // objectRef 

    @Persistent
    private String objectType;  // only used for searches

    @Persistent
    private String object;  // objectRef

    @Persistent
    private List<AuditLogAttribute> auditLogAttributes = new ArrayList<AuditLogAttribute>();


    public AuditLog(AuditableAction action, ObjectRef author) {
        eventDate = new Date();
        this.action = action.name();
        if (author != null)
           this.author = author.toString();
    }
    public AuditLog(AuditableAction action, ObjectRef author, ObjectRef object) {
        eventDate = new Date();
        this.action = action.name();
        if (author != null)
            this.author = author.toString();
        if (object != null) {
            this.object = object.toString();
            this.objectType = object.getType();
        }
    }


    public List<AuditLogAttribute> getAuditLogAttributes() {
        return auditLogAttributes;
    }

    public void setAuditLogAttributes(List<AuditLogAttribute> logAttrs) {
        auditLogAttributes = logAttrs;
    }

    public void setObject(ObjectRef object) {
        this.object = object.toString();
        this.objectType = object.getType();
    }

    public AuditLogDTO toDTO() {
        AuditLogDTO dto = new AuditLogDTO(eventDate, new ObjectRef(author), new ObjectRef(object),
                AuditableAction.valueOf(action));
        List<AuditLogAttribute> attrs = getAuditLogAttributes();
        if (attrs != null) {
            for (int i = 0; i < attrs.size(); i++) {
                dto.addModifiedAttribute(attrs.get(i).getAttributeName(),
                        attrs.get(i).getOldValue(), attrs.get(i).getNewValue());
            }
        }
        return dto;
    }

    @Override
    public String toString() { 
        return this.toDTO().toString();
    }

    public void setAction(AuditableAction action) {
        this.action = action.name();
    }

    public String getObjectRef() {
        return object;
    }

}
