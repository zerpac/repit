/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.repit.rwt.client;

import ch.repit.rwt.client.security.Action;
import ch.repit.rwt.client.ui.Page;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Base class for bento definitions.
 */
public abstract class BentoDef {

    private Map<String,AttributeDef> attributeDefsMap;

    
    /**
     * Sublcasses must call this contructor and provide the list of attributeDef
     * @param attributeDefs
     */
    protected BentoDef(Set<AttributeDef> attributeDefs) {
        Map<String,AttributeDef> tempMap = new HashMap<String,AttributeDef>();
        for (AttributeDef ad : attributeDefs) {
            tempMap.put(ad.getName(), ad);
        }
        attributeDefsMap = Collections.unmodifiableMap(tempMap);
    }


    public abstract String getType();

    public abstract String getTypeLabel();


    
    /**
     * Returns the distinguished attribute defining the object. Should idealy be unique,
     * but this is not mandatory (used only for display). The goal is to never have to display
     * object ID.
     * @return the readable attribute defining the object
     * @deprecated use getCommonName instead
     */
    public abstract String getDistinguishedAttribute();


    /**
     * Formats the common display name of the bento in parameter
     * @param bento
     * @return
     */
    public String getCommonName(Bento bento) {
        // default impl, until all BentoDef have implemented it
        return bento.getDisplayValue(this.getDistinguishedAttribute());
    }


    /**
     * Returns the JDO class implementation of this object. It is a String because
     * BentoDef are defined on client side (thus everywhere), whereas JDO class is
     * only available server-side. Must exist and implement DataObject
     * @return
     */
    public abstract String getJdoClassName();

    /**
     * Optionnal additional filter to set when listing all objects of this type.
     * Empty implentation returns null.
     * @return
     */
    public String getJdoFilter() {
        return null;
    }

    /**
     * Classname of server-side validator. Must implement the Validator interface.
     * Empty implentation returns null.
     * @return
     */
    public String getValidator() {
        return null;
    }

    /**
     * Factory method to create a new bento object
     * @return an empty bento object, ready to have its attributes filled
     */
    public final Bento createBento() {
        Bento b = fillBentoAttributes(new Bento(getType()));
        b.setStatus(getDefaultStatus());  // by default obj created active (this is discussable)
        return b;
    }

    /**
     * Factory method used server side when reading bento objects
     * @param id
     * @param owner
     * @param lastUpdate
     * @return an empty bento object, ready to have its attributes filled
     */
    public final Bento createBento(Long id, ObjectRef owner, Date lastUpdate) {
        return fillBentoAttributes(new Bento(getType(), id, owner, lastUpdate));
    }

    /**
     * Factory metohd used client-side when submitting only updated part of an
     * existing object that was edited (NOT; MODEL ATTR ARE COPIED TO THE RESULTING OBJ)
     * @param model the object to copy the id, owner and lastUpdate
     * @return a bento object, with its attributes filled
     */
    public final Bento createBento(Bento model) {
        Bento result = new Bento(model.getType(), model.getId(), model.getOwnerRef(), model.getLastUpdate());
        result.setStatus(model.getStatus());
        fillBentoAttributes(result);
        // here we copy the model attributes
        for (BentoAttribute attr : result.values()) {
            String name = attr.getName();
            BentoAttribute modelAttr = model.get(name);
            if (modelAttr != null) {
                switch (attr.getAttributeType()) {
                    case STRING:
                        attr.set(modelAttr.getString());
                        break;
                    case STRING_LIST:
                        attr.set(modelAttr.getStringList());
                        break;
                    case DAY:
                        attr.set(modelAttr.getDay());
                        break;
                    case INTEGER:
                        attr.set(modelAttr.getInteger());
                        break;
                    case DATE:
                        attr.set(modelAttr.getDate());
                        break;
                    case BOOLEAN:
                        attr.set(modelAttr.getBoolean());
                        break;
                    default:
                }
            }
        }
        return result;
    }

    
    private Bento fillBentoAttributes(Bento bento) {
        Bento tmp = bento;
        for (AttributeDef attrDef : attributeDefsMap.values()) {
            BentoAttribute attr = new BentoAttribute(attrDef.getName(), attrDef.getType());
            if (attrDef.getDefaultValue() != null) {
                switch (attrDef.getType()) {
                    case STRING:
                        attr.set((String)attrDef.getDefaultValue());
                        break;
                    case INTEGER:
                        attr.set((Integer)attrDef.getDefaultValue());
                        break;
                    case BOOLEAN:
                        attr.set((Boolean)attrDef.getDefaultValue());
                        break;
                    default:  // other types (List, date) not relevant for default...
                        break;
                }
            }
            tmp.put(attrDef.getName(), attr);
        }
        return tmp;
    }


    /**
     * Provides the list of AttributeDefinition of this bento
     * @return an unmodifiable list of AtrributeDef.
     */
    public Set<String> listAttributeNames() {
        return attributeDefsMap.keySet();
    }

    public AttributeDef getAttributeDef(String attributeName) {
        return attributeDefsMap.get(attributeName);
    }




    /**
     * Owner policy defines who own an object when it is created. It can be the
     * CREATOR of the object, the OBJECT itself in case of users, or NONE meaning
     * the object has no owner. In the latter, there will be no Own action proposed
     * in the role definition
     * CREATOR_IF_NOT_SPECIFIED was added, kind of kills the purpose of this in terms
     * of securoty, but was required to allow delegation of creation.
     * @return the object Owner policy, CREATOR by default
     */
    public OwnerPolicy getOwnerPolicy() {
        return OwnerPolicy.CREATOR;
    }

    public enum OwnerPolicy {
        CREATOR,
        OBJECT,
        NONE,
        CREATOR_IF_NOT_SPECIFIED;
    }

    /**
     * Used in mail reports, to specify correct action.
     * @return
     */
    public LabelGender getLabelGender() {
        return LabelGender.MASCULIN;
    }

    public enum LabelGender {
        MASCULIN,
        MASCULIN_VOYELLE,
        FEMININ;
    }


    private List<Action> supportedActionsAll;
    private List<Action> supportedActionsOwn;

    
    /**
     * Allows to refine the actions that make sense for this object. Useful for the
     * Role definition
     * @return by default the full action list
     */
    public List<Action> supportedActionsAll() {
        if (supportedActionsAll == null) {
            supportedActionsAll = new ArrayList<Action>((List<Action>)Arrays.asList(Action.values()));
            supportedActionsAll.remove(Action.ADMIN);
            supportedActionsAll.remove(Action.DRAFT);    // to be added explicitely...
        }
        return supportedActionsAll;
    }


    /**
     * Allows to refine the actions that make sense for this object. Useful for the
     * Role definition
     * @return by default the full action list
     */
    public List<Action> supportedActionsOwn() {
        if (supportedActionsOwn == null) {
            supportedActionsOwn = new ArrayList<Action>((List<Action>)Arrays.asList(Action.values()));
            supportedActionsOwn.remove(Action.ADMIN);
            supportedActionsOwn.remove(Action.MANAGE);
            supportedActionsOwn.remove(Action.CREATE);  // not sure...
            supportedActionsOwn.remove(Action.COMMENT);
            supportedActionsAll.remove(Action.DRAFT);    // to be added explicitely...
        }
        return supportedActionsOwn;
    }


    public Page getViewPage(ObjectRef oref) {
        return null;
    }


    /**
     * Return the default status that new objects of this type have at creation
     */
    public BentoStatus getDefaultStatus() {
        return BentoStatus.ACTIVE;
    }
}
