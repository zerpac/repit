/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.repit.site.client.calendar;

import ch.repit.rwt.client.Bento;
import ch.repit.rwt.client.BentoDefFactory;
import ch.repit.rwt.client.ObjectRef;
import ch.repit.rwt.client.audit.AuditListPage;
import ch.repit.rwt.client.audit.AuditQueryDef;
import ch.repit.rwt.client.security.Action;
import ch.repit.rwt.client.security.Authorizer;
import ch.repit.rwt.client.security.SecurityManager;
import ch.repit.rwt.client.ui.BentoPage;

/**
 * Base page for both Calendar event and bookings (maybe should define super class for those pals)
 */
public class CalendarEntryPage extends BentoPage {

    public CalendarEntryPage(ObjectRef objectRef) {
        super(objectRef!=null?objectRef.getType():BookingDef.TYPE, objectRef, null);
    }


    @Override
    protected void init() {
        super.init();

        setShowPath(true);

        if (BookingDef.TYPE.equals(getObject().getType())) {
            setTitle("Edition inscription");
            super.addTab("Détails", new BookingDetailsPage(getObject().getRef(), this, null));
        } else if (CalendarEventDef.TYPE.equals(getObject().getType())) {
            setTitle(getObject().getDisplayValue(CalendarEventDef.ATTR_TITLE));
            super.addTab("Détails", new CalendarEventDetailsPage(getObject().getRef(), this, null));
        }

        Authorizer auth = SecurityManager.get().getAuthorizer();
        if (auth.isAllowed(Action.AUDIT, getObject())) {
            Bento queryUserActions = BentoDefFactory.get().getDef(AuditQueryDef.TYPE).createBento();
            queryUserActions.get(AuditQueryDef.ATTR_OBJECTREF).set(getObject().getRef().toString());
            super.addTab("Historique", new AuditListPage(this, queryUserActions));
        }
    }

}
