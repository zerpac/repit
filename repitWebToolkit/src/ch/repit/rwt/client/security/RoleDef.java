/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.repit.rwt.client.security;

import ch.repit.rwt.client.AttributeDef;
import ch.repit.rwt.client.AttributeDef.Feature;
import ch.repit.rwt.client.AttributeType;
import ch.repit.rwt.client.BentoDef;
import ch.repit.rwt.client.ObjectRef;
import ch.repit.rwt.client.security.ui.RoleDetailsPage;
import ch.repit.rwt.client.ui.Page;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;



public class RoleDef extends BentoDef {

    public static final String TYPE = "Role";

    public static final String ATTR_CATEGORY = "category";
    public static final String ATTR_DESCRIPTION = "description";
    public static final String ATTR_PERM_ALL = "globalScopePermissions";
    public static final String ATTR_PERM_OWN = "ownObjectsPermissions";
    public static final String ATTR_NAME = "name";
    public static final String ATTR_INHERITED_ROLES = "inheritedRoles";

    private static Set<AttributeDef> attrDefs = new HashSet<AttributeDef>();
    static {
        attrDefs.add(new AttributeDef(ATTR_NAME, AttributeType.STRING,
                Feature.MANDATORY, Feature.UNIQUE, Feature.REQUIRE_ADMIN_TO_EDIT));
        attrDefs.add(new AttributeDef(ATTR_CATEGORY, AttributeType.STRING,
                Feature.MANDATORY, Feature.REQUIRE_ADMIN_TO_EDIT));
        attrDefs.add(new AttributeDef(ATTR_DESCRIPTION, AttributeType.STRING));
        attrDefs.add(new AttributeDef(ATTR_PERM_ALL, AttributeType.STRING_LIST));
        attrDefs.add(new AttributeDef(ATTR_PERM_OWN, AttributeType.STRING_LIST));
        attrDefs.add(new AttributeDef(ATTR_INHERITED_ROLES, AttributeType.STRING_LIST));
    }


    /**
     * To be used server-side, where categories are just an attribute
     */
    public RoleDef() {
        super(attrDefs);
    }

    public String getType() {
        return TYPE;
    }
    public String getTypeLabel() {
        return "Rôle";
    }

    public String getJdoClassName() {
        return "ch.repit.rwt.server.security.Role";
    }

    @Override
    public OwnerPolicy getOwnerPolicy() {
        return OwnerPolicy.NONE;
    }
    
    @Override
    public List<Action> supportedActionsAll() {
        List<Action> result = super.supportedActionsAll();
        result.remove(Action.COMMENT);
        return result;
    }

    @Override
    public String getDistinguishedAttribute() {
        return ATTR_NAME;
    }

    @Override
    public Page getViewPage(ObjectRef oref) {
        return new RoleDetailsPage(null);
    }

    
    public enum Category {

        Hidden     ("Hidden",    "cach\u00E9"),   //   "Technique"),
        Secondary  ("Secondary", "divers"),       //   "Fonction", "Honorifique"),
        Primary    ("Primary",   "comit\u00E9");  //   "Comité");

        private String name, label;

        Category(String name, String label) {
            this.name = name;
            this.label = label;
        }

        public String getName() {
            return name;
        }
        public String getLabel() {
            return label;
        }

        public static TreeMap<String,String> getAllValuesMap() {
            TreeMap<String,String> result = new TreeMap();
            for (Category c : Category.values()) {
                result.put(c.getLabel(),c.getName());
            }
            return result;
        }
    }

    
}


