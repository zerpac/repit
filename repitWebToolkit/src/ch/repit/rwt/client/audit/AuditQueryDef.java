package ch.repit.rwt.client.audit;

import ch.repit.rwt.client.AttributeDef;
import ch.repit.rwt.client.AttributeType;
import ch.repit.rwt.client.BentoDef;
import ch.repit.rwt.client.security.Action;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;



public class AuditQueryDef extends BentoDef {

    public static final String TYPE = "AuditQuery";

    public static final String ATTR_ACTION = "action";
    public static final String ATTR_OBJECTREF = "objectRef";
    public static final String ATTR_OBJECTTYPE = "objectType";
    public static final String ATTR_AUTHOR = "author";
    public static final String ATTR_FROMDAYS = "fromDays";
    public static final String ATTR_TODAYS = "toDays";
    public static final String ATTR_FROMDATE = "fromDate";
    public static final String ATTR_TODATE = "toDate";
    public static final String ATTR_SIZELIMIT = "querySizeLimit";


    private static Set<AttributeDef> attrDefs = new HashSet<AttributeDef>();
    static {
        attrDefs.add(new AttributeDef(ATTR_ACTION, AttributeType.STRING));
        attrDefs.add(new AttributeDef(ATTR_OBJECTREF, AttributeType.STRING));
        attrDefs.add(new AttributeDef(ATTR_OBJECTTYPE, AttributeType.STRING));
        attrDefs.add(new AttributeDef(ATTR_AUTHOR, AttributeType.STRING));
        attrDefs.add(new AttributeDef(ATTR_FROMDAYS, AttributeType.INTEGER));
        attrDefs.add(new AttributeDef(ATTR_TODAYS, AttributeType.INTEGER));
        attrDefs.add(new AttributeDef(ATTR_FROMDATE, AttributeType.DATE));
        attrDefs.add(new AttributeDef(ATTR_TODATE, AttributeType.DATE));
        attrDefs.add(new AttributeDef(ATTR_SIZELIMIT, AttributeType.INTEGER, new Integer(100)));
    }

    public AuditQueryDef() {
        super(attrDefs);
    }

    public String getType() {
        return TYPE;
    }
    public String getTypeLabel() {
        return "Requête d'audit";
    }


    public String getJdoClassName() {
        return null;
    }

    @Override
    public List<Action> supportedActionsAll() {
        return new ArrayList<Action>();
    }
    @Override
    public List<Action> supportedActionsOwn() {
        return new ArrayList<Action>();
    }

    @Override
    public String getDistinguishedAttribute() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}


