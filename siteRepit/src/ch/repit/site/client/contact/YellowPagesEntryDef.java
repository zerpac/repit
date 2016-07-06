package ch.repit.site.client.contact;

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

@IncludeInReports
public class YellowPagesEntryDef extends BentoDef {

    public static final String TYPE = "YellowPagesEntry";

    public static final String ATTR_RAISONSOCIALE = "raisonSociale";
    public static final String ATTR_CONTACT_FIRSTNAME = "contactFirstName";
    public static final String ATTR_CONTACT_LASTNAME = "contactLastName";
    public static final String ATTR_EMAIL = "email";
    public static final String ATTR_PHONE = "phone";
    public static final String ATTR_FAX = "fax";
    public static final String ATTR_MOBILE = "mobilePhone";
    public static final String ATTR_WEBSITE = "webSite";
    public static final String ATTR_ADDRESSLINE = "addressLine";
    public static final String ATTR_ZIPCODE = "zipCode";
    public static final String ATTR_LOCALITY = "locality";
    public static final String ATTR_COUNTRY = "country";
    public static final String ATTR_DESCRIPTION = "description";


    private static Set<AttributeDef> attrDefs = new HashSet<AttributeDef>();
    static {
        attrDefs.add(new AttributeDef(ATTR_RAISONSOCIALE, AttributeType.STRING, Feature.MANDATORY));
        attrDefs.add(new AttributeDef(ATTR_CONTACT_FIRSTNAME, AttributeType.STRING));
        attrDefs.add(new AttributeDef(ATTR_CONTACT_LASTNAME, AttributeType.STRING));
        attrDefs.add(new AttributeDef(ATTR_EMAIL, AttributeType.STRING));
        attrDefs.add(new AttributeDef(ATTR_PHONE, AttributeType.STRING));
        attrDefs.add(new AttributeDef(ATTR_FAX, AttributeType.STRING));
        attrDefs.add(new AttributeDef(ATTR_MOBILE, AttributeType.STRING));
        attrDefs.add(new AttributeDef(ATTR_WEBSITE, AttributeType.STRING));
        attrDefs.add(new AttributeDef(ATTR_ADDRESSLINE, AttributeType.STRING));
        attrDefs.add(new AttributeDef(ATTR_ZIPCODE, AttributeType.STRING, "1862", Feature.MANDATORY));
        attrDefs.add(new AttributeDef(ATTR_LOCALITY, AttributeType.STRING, "Les Mosses", Feature.MANDATORY));
        attrDefs.add(new AttributeDef(ATTR_COUNTRY, AttributeType.STRING, "CH - SUISSE", Feature.MANDATORY));
        attrDefs.add(new AttributeDef(ATTR_DESCRIPTION, AttributeType.STRING));
    }

    public YellowPagesEntryDef() {
        super(attrDefs);
    }

    @Override
    public String getType() {
        return TYPE;
    }
    public String getTypeLabel() {
        return "Contact des pages jaunes";
    }


    @Override
    public String getDistinguishedAttribute() {
        return ATTR_RAISONSOCIALE;
    }

    @Override
    public String getCommonName(Bento bento) {
        return bento.getDisplayValue(ATTR_RAISONSOCIALE);
    }

    @Override
    public String getJdoClassName() {
        return "ch.repit.site.server.contact.YellowPagesEntry";
    }



    @Override
    public List<Action> supportedActionsOwn() {
        List<Action> result = super.supportedActionsOwn();
        result.remove(Action.DRAFT);
        return result;
    }

    @Override
    public Page getViewPage(ObjectRef oref) {
        return new YPEntryPage(oref);
    }
    
}
