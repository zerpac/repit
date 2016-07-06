/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.repit.rwt.client;

import ch.repit.rwt.client.security.Action;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author tc149752
 */
public class AttributeDef {

    private String name;
    private String label;
    private AttributeType type;
    private Object defaultValue = null;
    private List<Feature> features;

    
    public AttributeDef(String name, AttributeType type, Feature... features) {
        this(name, name, type, null, features);
    }
    
    public AttributeDef(String name, AttributeType type, Object defaultValue, Feature... features) {
        this(name, name, type, defaultValue, features);
    }

    public AttributeDef(String name, String label, AttributeType type, Feature... features) {
        this(name, label, type, null, features);
    }

    public AttributeDef(String name, String label, AttributeType type, Object defaultValue, Feature... features) {
        this.name = name;
        this.type = type;
        this.label = label;
        this.features = Arrays.asList(features);
        this.defaultValue = defaultValue;
    }

    public String getName() {
        return name;
    }
    public String getLabel() {
        return label;
    }

    public AttributeType getType() {
        return type;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public boolean isMandatory() {
        return features.contains(Feature.MANDATORY);
    }

    public boolean isUnique() {
        return features.contains(Feature.UNIQUE);
    }

    public boolean isAuditable() {
        return !features.contains(Feature.NOT_AUDITABLE);
    }
    
    public boolean isReadonly() {
        return features.contains(Feature.READONLY);
    }

    public Action getEditActionThreshold() {
        if (features.contains(Feature.REQUIRE_ADMIN_TO_EDIT))
            return Action.ADMIN;
        if (features.contains(Feature.REQUIRE_MANAGE_TO_EDIT))
            return Action.MANAGE;
        return Action.UPDATE;
    }

    public enum Feature {
        UNIQUE,
        MANDATORY,
        NOT_AUDITABLE,
        READONLY,
        REQUIRE_MANAGE_TO_EDIT,
        REQUIRE_ADMIN_TO_EDIT;
    }

}

