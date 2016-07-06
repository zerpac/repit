/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.repit.rwt.client.audit;

import ch.repit.rwt.client.BentoDef.LabelGender;
import ch.repit.rwt.client.ObjectRef;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * @author tc149752
 */
public class AuditLogDTO implements Serializable {

    private Date eventDate;
    private ObjectRef author;
    private ObjectRef object;  
    private AuditableAction action;
    private List<AuditLogAttributeDTO> modifiedAttributes;

    public AuditLogDTO() {}

    public AuditLogDTO(Date eventDate, ObjectRef author, ObjectRef object, AuditableAction action) {
        this.eventDate = eventDate;
        this.author = author;
        this.object = object;
        this.action = action;
    }

    public AuditableAction getAction() {
        return action;
    }

    public Date getEventDate() {
        return eventDate;
    }

    public List<AuditLogAttributeDTO> getModifiedAttributes() {
        if (modifiedAttributes == null)
            modifiedAttributes = new ArrayList<AuditLogAttributeDTO>();
        return modifiedAttributes;
    }

    public ObjectRef getObject() {
        return object;
    }


    public ObjectRef getAuthor() {
        return author;
    }

    public void addModifiedAttribute(String attributeName, String oldValue, String newValue) {
        getModifiedAttributes().add(new AuditLogAttributeDTO(attributeName, oldValue, newValue));
    }

    
    public enum AuditableAction implements Serializable  {
        CREATE ("cr&eacute;&eacute;", "cr&eacute;&eacute;e", "Create"),
        UPDATE ("modifi&eacute;", "modifi&eacute;e", "Update"),
        TRASH  ("supprim&eacute;", "supprim&eacute;e","Delete"),
        UNTRASH("r&eacute;cup&eacute;r&eacute;", "r&eacute;cup&eacute;r&eacute;e","Update"),
        DELETE ("effac&eacute; d&eacute;finitivement", "effac&eacute;e d&eacute;finitivement", null),
        COMMENT("comment&eacute;", "comment&eacute;e", "Comment"),
        LOGIN  ("connection", "connection", null),
        LOGOUT ("d&eacute;connection", "d&eacute;connection", null);

        private String label, labelF;
        private String alertLabel;

        private AuditableAction(String labelM, String labelF, String alertLabel) {
            this.label = labelM;
            this.labelF = labelF;
            this.alertLabel = alertLabel;
        }

        public String getLabel() {
            return label;
        }
        public String getAlertLabel() {
            return alertLabel;
        }

        public String getLabel(LabelGender labelGender) {
            if (labelGender != null && labelGender.equals(LabelGender.FEMININ) )
                return labelF;
            else
                return label;
        }
    }


}
