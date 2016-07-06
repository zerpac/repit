package ch.repit.site.client.calendar;

import ch.repit.rwt.client.AttributeDef;
import ch.repit.rwt.client.AttributeDef.Feature;
import ch.repit.rwt.client.AttributeType;
import ch.repit.rwt.client.Bento;
import ch.repit.rwt.client.BentoDef;
import ch.repit.rwt.client.ObjectRef;
import ch.repit.rwt.client.annotations.IncludeInReports;
import ch.repit.rwt.client.security.Action;
import ch.repit.rwt.client.ui.Page;
import ch.repit.rwt.client.util.Formaters;
import ch.repit.rwt.client.util.Formaters.DatePattern;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@IncludeInReports
public class CalendarEventDef extends BentoDef {


    public static final String TYPE = "CalendarEvent";

    public static final String ATTR_TITLE = "eventTitle";
    public static final String ATTR_FROM_DAY = "fromDay";
    public static final String ATTR_TO_DAY = "toDay";
    public static final String ATTR_DESCRIPTION = "description";
    public static final String ATTR_EVENT_TYPE = "eventType";

    private static Set<AttributeDef> attrDefs = new HashSet<AttributeDef>();
    static {
        attrDefs.add(new AttributeDef(ATTR_TITLE, AttributeType.STRING, Feature.MANDATORY));
        attrDefs.add(new AttributeDef(ATTR_FROM_DAY, AttributeType.DAY, Feature.MANDATORY));
        attrDefs.add(new AttributeDef(ATTR_TO_DAY, AttributeType.DAY, Feature.MANDATORY));
        attrDefs.add(new AttributeDef(ATTR_DESCRIPTION, AttributeType.STRING));
        attrDefs.add(new AttributeDef(ATTR_EVENT_TYPE, AttributeType.STRING, Feature.MANDATORY));
    }

    public CalendarEventDef() {
        super(attrDefs);
    }

    public String getType() {
        return TYPE;
    }
    public String getTypeLabel() {
        return "Evénement";
    }

    public String getJdoClassName() {
        return "ch.repit.site.server.booking.CalendarEvent";
    }

    @Override
    public OwnerPolicy getOwnerPolicy() {
        return OwnerPolicy.CREATOR;
    }


    @Override
    public String getDistinguishedAttribute() {
        return ATTR_TITLE;
    }

    @Override
    public String getCommonName(Bento bento) {
        return bento.get(ATTR_TITLE).getString() + " (" +
                Formaters.get().formatDate(bento.get(ATTR_FROM_DAY).getDay(), DatePattern.MONTH_YEAR) + ")";
    }

    @Override
    public List<Action> supportedActionsOwn() {
        return new ArrayList<Action>();
    }

    @Override
    public Page getViewPage(ObjectRef oref) {
        if (oref == null)
            return new CalendarEventDetailsPage(null, null, null);
        else
            return new CalendarEntryPage(oref);
    }

    // ------------------------------------

    public enum CalEventType {

        VACATION("Jour férié, vacances scolaires", false),
        LOCAL_EVENT("Evénement dans la région", true),
        REPIT_EVENT("Assemblée / corvée", false);

        private String label;
        private boolean reservationAllowed;

        CalEventType(String label, boolean reservationAllowed) {
            this.label = label;
            this.reservationAllowed = reservationAllowed;
        }

        public boolean isReservationAllowed() {
            return reservationAllowed;
        }

        public String getLabel() {
            return label;
        }

    }
}


