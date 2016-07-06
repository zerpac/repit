/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.repit.rwt.client.user;

import ch.repit.rwt.client.AttributeDef;
import ch.repit.rwt.client.AttributeDef.Feature;
import ch.repit.rwt.client.AttributeType;
import ch.repit.rwt.client.Bento;
import ch.repit.rwt.client.BentoDef;
import ch.repit.rwt.client.ObjectRef;
import ch.repit.rwt.client.annotations.IncludeInReports;
import ch.repit.rwt.client.security.Action;
import ch.repit.rwt.client.ui.Page;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * This is for users without postal address and notification prefs
 */
@IncludeInReports
public class UserLightDef extends BentoDef {


    public static final String TYPE = "User";  // keep same type, they are NEVER used together in sam app

    public static final String ATTR_LOGIN = "login";
    public static final String ATTR_FIRSTNAME = "firstName";
    public static final String ATTR_LASTNAME = "lastName";
    public static final String ATTR_EMAIL = "email";
    public static final String ATTR_ROLESREF = "rolesRef";

    static Set<AttributeDef> attrDefs = new HashSet<AttributeDef>();
    static {
        attrDefs.add(new AttributeDef(ATTR_LOGIN, "identifiant", AttributeType.STRING,
            Feature.MANDATORY, Feature.UNIQUE, Feature.REQUIRE_ADMIN_TO_EDIT));
        attrDefs.add(new AttributeDef(ATTR_FIRSTNAME, "pr\u00E9nom", AttributeType.STRING, Feature.MANDATORY));
        attrDefs.add(new AttributeDef(ATTR_LASTNAME, "nom de famille", AttributeType.STRING, Feature.MANDATORY));
        attrDefs.add(new AttributeDef(ATTR_EMAIL, "email", AttributeType.STRING));
        attrDefs.add(new AttributeDef(ATTR_ROLESREF, "fonctions", AttributeType.STRING_LIST, Feature.REQUIRE_MANAGE_TO_EDIT));  // how to set "membre" as default value?
    }

    protected UserLightDef(Set<AttributeDef> attributeDefs) {
        super(attributeDefs);
    }

    public UserLightDef() {
        super(attrDefs);
    }

    public String getType() {
        return TYPE;
    }
    public String getTypeLabel() {
        return "Utilisateur";
    }

    public String getJdoClassName() {
        return "ch.repit.rwt.server.user.UserLight";
    }

    @Override
    public OwnerPolicy getOwnerPolicy() {
        return OwnerPolicy.OBJECT;
    }

    @Override
    public List<Action> supportedActionsAll() {
        List<Action> result = super.supportedActionsAll();
        result.remove(Action.COMMENT);
        return result;
    }

    @Override
    public List<Action> supportedActionsOwn() {
        List<Action> result = super.supportedActionsOwn();
        result.remove(Action.DRAFT);
        return result;
    }

    @Override
    // TBD: could be replace by a derived attr...
    public String getCommonName(Bento bento) {
        return bento.getDisplayValue(ATTR_FIRSTNAME) +  " " + bento.getDisplayValue(ATTR_LASTNAME);
    }

    @Override
    public String getDistinguishedAttribute() {
        return ATTR_LOGIN;
    }
    
    @Override
    public Page getViewPage(ObjectRef oref) {
        return new UserDetailsPage(oref);
    }


    // custom

    public boolean hasPreferences() {
        return false;
    }
}


