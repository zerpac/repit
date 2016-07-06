/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.repit.site.client.calendar;

import ch.repit.site.client.calendar.CalendarEventDef.CalEventType;
import ch.repit.rwt.client.Bento;
import ch.repit.rwt.client.Day;
import ch.repit.rwt.client.ObjectRef;
import ch.repit.rwt.client.ui.Page;
import ch.repit.rwt.client.ui.form.DateField;
import ch.repit.rwt.client.ui.form.FieldRow;
import ch.repit.rwt.client.ui.form.FormPage;
import ch.repit.rwt.client.ui.form.SelectField;
import ch.repit.rwt.client.ui.form.TextField;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 *
 * @author tc149752
 */
class CalendarEventDetailsPage extends FormPage {

    private Day startFromCal, endFromCal;
    private CalendarPage calPage;


    public CalendarEventDetailsPage(ObjectRef calEventRef, 
                                    Page topPage,
                                    CalendarPage calPage) {
        super(CalendarEventDef.TYPE,  calEventRef, topPage);
        this.calPage = calPage;
    }

    public CalendarEventDetailsPage(ObjectRef calEventRef,
                                    Page topPage,
                                    CalendarPage calPage,
                                    Day startOrEnd1, Day startOrEnd2) {
        super(CalendarEventDef.TYPE,  calEventRef, topPage);
        if (startOrEnd1.before(startOrEnd2)) {
            startFromCal = startOrEnd1;
            endFromCal = startOrEnd2;
        } else {
            startFromCal = startOrEnd2;
            endFromCal = startOrEnd1;
        }
        this.calPage = calPage;
    }

    @Override
    protected void init() {

        if (isCreate()) {
            setTitle("Création événement");
            if (startFromCal != null)
                getObject().get(CalendarEventDef.ATTR_FROM_DAY).set(startFromCal);
            if (endFromCal != null)
                getObject().get(CalendarEventDef.ATTR_TO_DAY).set(endFromCal);
        }
        super.setShowPath(true);

        this.addSectionHead("Détails");

        // name 
        TextField title = new TextField(CalendarEventDef.ATTR_TITLE, null);
        title.setColumns(20);
        this.addSingleFieldRow("Titre", "Le titre est court, mettez les détails dans la description", title);

        // event type
        SelectField eventType = new SelectField(CalendarEventDef.ATTR_EVENT_TYPE, false);
        SortedMap valueMap = new TreeMap();
        for (CalEventType t : CalEventType.values()) {
            String resForb = "";
            if (!t.isReservationAllowed())
                resForb = " (*)";
            valueMap.put(t.getLabel() + resForb, t.name());
        }
        eventType.setValueMap(valueMap);
        this.addSingleFieldRow("Type d'événement", "l'(*) indique que les réservations sont interdites durant cette période, ainsi que les séjours de plus d'une semaine", eventType);

        // dates
        DateField fromDate = new DateField(CalendarEventDef.ATTR_FROM_DAY, "début");
        DateField toDate = new DateField(CalendarEventDef.ATTR_TO_DAY, "fin");
        this.addFieldRow(new FieldRow("Durée de l'événement", fromDate, toDate));
        
        TextField desc = new TextField(CalendarEventDef.ATTR_DESCRIPTION, null, 3);
        desc.setColumns(60);
        this.addSingleFieldRow("Description", null, desc);

        super.init();
    }

    @Override
    protected void validateAfterRead(Bento bento, List<String> validationErrors) {
        super.validateAfterRead(bento, validationErrors);

        Day from = bento.get(BookingDef.ATTR_FROM_DAY).getDay();
        Day to   = bento.get(BookingDef.ATTR_TO_DAY).getDay();

        if (from == null || from.getInteger() == null)
            validationErrors.add("Date de début non spécifiée");

        if (to == null || to.getInteger() == null)
            validationErrors.add("Date de fin non spécifiée");

        // check the date order
        if (from != null && to != null && from.after(to)) {
            validationErrors.add("Date de fin avant date de début");
        }
    }

    @Override
    protected void doSaveOnSuccess(ObjectRef objectRef) {
        if (calPage != null) {
            calPage.removeCalSelection();
        }
        super.doSaveOnSuccess(objectRef);
    }
}
