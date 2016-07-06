package ch.repit.rwt.client.user;

import ch.repit.rwt.client.AttributeDef;
import ch.repit.rwt.client.AttributeDef.Feature;
import ch.repit.rwt.client.AttributeType;
import ch.repit.rwt.client.BentoDef;
import ch.repit.rwt.client.security.Action;
import java.util.HashSet;
import java.util.List;
import java.util.Set;



public class UserPrefDef extends BentoDef {


    public static final String TYPE = "UserPref";

    public static final String ATTR_WEEKLY_REPORT = "weeklyReport";
    public static final String ATTR_DAILY_REPORT = "dailyReport";

    public static final String ATTR_ALERT_ALL = "notifAll";
    public static final String ATTR_ALERT_MINES = "notifMines";
    public static final String ATTR_ALERT_CREATES = "notifCreates";

    public static final String ATTR_OFFICIAL_COMM_VIA_EMAIL = "officialCommEmail";


    private static Set<AttributeDef> attrDefs = new HashSet<AttributeDef>();
    static {

        // globally, only usable if user has an email

        attrDefs.add(new AttributeDef(ATTR_WEEKLY_REPORT, AttributeType.BOOLEAN, Boolean.TRUE)); 
        attrDefs.add(new AttributeDef(ATTR_DAILY_REPORT, AttributeType.BOOLEAN, Boolean.FALSE));

        // new ones
        attrDefs.add(new AttributeDef(ATTR_ALERT_ALL, AttributeType.BOOLEAN, Boolean.FALSE));
        attrDefs.add(new AttributeDef(ATTR_ALERT_MINES, AttributeType.BOOLEAN, Boolean.FALSE));
        attrDefs.add(new AttributeDef(ATTR_ALERT_CREATES, AttributeType.BOOLEAN, Boolean.FALSE));
        
        attrDefs.add(new AttributeDef(ATTR_OFFICIAL_COMM_VIA_EMAIL, AttributeType.BOOLEAN));
        
        // all this is deprecated
        /*
        attrDefs.add(new AttributeDef("notifBlogOfficialCreate", AttributeType.BOOLEAN, Boolean.TRUE, Feature.READONLY)); // official, thus readonly
        attrDefs.add(new AttributeDef("notifBlogOfficialUpdate", AttributeType.BOOLEAN, Boolean.TRUE, Feature.READONLY));
        attrDefs.add(new AttributeDef("notifBlogOfficialComment", AttributeType.BOOLEAN, Boolean.FALSE));
        attrDefs.add(new AttributeDef("notifBlogPublicCreate", AttributeType.BOOLEAN, Boolean.FALSE));
        attrDefs.add(new AttributeDef("notifBlogPublicUpdate", AttributeType.BOOLEAN, Boolean.FALSE));
        attrDefs.add(new AttributeDef("notifBlogPublicComment", AttributeType.BOOLEAN, Boolean.FALSE));
        attrDefs.add(new AttributeDef("notifBookingCreate", AttributeType.BOOLEAN, Boolean.TRUE));
        attrDefs.add(new AttributeDef("notifBookingUpdate", AttributeType.BOOLEAN, Boolean.FALSE));
        attrDefs.add(new AttributeDef("notifBookingDelete", AttributeType.BOOLEAN, Boolean.TRUE));
        attrDefs.add(new AttributeDef("notifBookingComment", AttributeType.BOOLEAN, Boolean.FALSE));
         */
    }

    public UserPrefDef() {
        super(attrDefs);
    }

    public String getType() {
        return TYPE;
    }

    public String getTypeLabel() {
        return "Membre";
    }

    public String getJdoClassName() {
        return "ch.repit.rwt.server.user.User";
    }

    @Override
    public OwnerPolicy getOwnerPolicy() {
        return OwnerPolicy.OBJECT;  // NONE ???
    }

    @Override
    public List<Action> supportedActionsAll() {
        List<Action> result = super.supportedActionsAll();
        result.remove(Action.COMMENT);
        result.remove(Action.CREATE);
        result.remove(Action.TRASH);
        result.remove(Action.VIEW_TRASH);
        return result;
    }

    @Override
    public List<Action> supportedActionsOwn() {
        List<Action> result = super.supportedActionsOwn();
        result.remove(Action.COMMENT);
        result.remove(Action.CREATE);
        result.remove(Action.TRASH);
        result.remove(Action.VIEW_TRASH);
        return result;
    }

    @Override
    public String getDistinguishedAttribute() {
        return "theme";
    }


}


