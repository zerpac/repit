/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.repit.rwt.client.user;

import ch.repit.rwt.client.AttributeDef;
import ch.repit.rwt.client.AttributeDef.Feature;
import ch.repit.rwt.client.AttributeType;
import ch.repit.rwt.client.ObjectRef;
import ch.repit.rwt.client.annotations.IncludeInReports;
import ch.repit.rwt.client.ui.Page;


@IncludeInReports
// @JdoClass ( "ch.repit.server.users.User" ); just an idea...
public class UserDef extends UserLightDef {


    public static final String ATTR_PRIVATEPHONE = "privatePhone";
    public static final String ATTR_MOBILEPHONE = "mobilePhone";
    public static final String ATTR_WORKPHONE = "workPhone";
    public static final String ATTR_FAX = "fax";
    public static final String ATTR_ADDRESSLINE = "addressLine";
    public static final String ATTR_ZIP = "zipCode";
    public static final String ATTR_LOCALITY = "locality";
    public static final String ATTR_COUNTRY = "country";
    
    static {
        attrDefs.add(new AttributeDef(ATTR_PRIVATEPHONE, "t\u00E9l. perso.", AttributeType.STRING));
        attrDefs.add(new AttributeDef(ATTR_MOBILEPHONE, "natel", AttributeType.STRING));
        attrDefs.add(new AttributeDef(ATTR_WORKPHONE, "t\u00E9l. prof.", AttributeType.STRING));
        attrDefs.add(new AttributeDef(ATTR_FAX, "fax", AttributeType.STRING));
        attrDefs.add(new AttributeDef(ATTR_ADDRESSLINE, "rue et num\u00E9ro", AttributeType.STRING));
        attrDefs.add(new AttributeDef(ATTR_ZIP, "code postal", AttributeType.STRING, Feature.MANDATORY));
        attrDefs.add(new AttributeDef(ATTR_LOCALITY, "localit\u00E9", AttributeType.STRING, Feature.MANDATORY));
        attrDefs.add(new AttributeDef(ATTR_COUNTRY, "pays", AttributeType.STRING));        // set suisse as default?
    }

    public UserDef() {
        super(attrDefs);
    }

    @Override
    public String getTypeLabel() {
        return "Membre";
    }

    @Override
    public String getJdoClassName() {
        return "ch.repit.rwt.server.user.User";
    }
    
    @Override
    public Page getViewPage(ObjectRef oref) {
        return new UserDetailsPage(oref);
    }


    // custom
    @Override
    public boolean hasPreferences() {
        return true;
    }
}


