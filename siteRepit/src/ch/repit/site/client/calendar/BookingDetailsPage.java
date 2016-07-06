/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.repit.site.client.calendar;

import ch.repit.rwt.client.Bento;
import ch.repit.rwt.client.BentoDef;
import ch.repit.rwt.client.BentoDefFactory;
import ch.repit.rwt.client.BentoStatus;
import ch.repit.rwt.client.Day;
import ch.repit.rwt.client.ObjectRef;
import ch.repit.rwt.client.persistence.CacheManager;
import ch.repit.rwt.client.security.Action;
import ch.repit.rwt.client.security.SecurityManager;
import ch.repit.rwt.client.ui.Page;
import ch.repit.rwt.client.ui.form.CheckBoxField;
import ch.repit.rwt.client.ui.form.DateField;
import ch.repit.rwt.client.ui.form.Field;
import ch.repit.rwt.client.ui.form.Field.FieldChangeHandler;
import ch.repit.rwt.client.ui.form.FieldRow;
import ch.repit.rwt.client.ui.form.FieldValidator;
import ch.repit.rwt.client.ui.form.FormPage;
import ch.repit.rwt.client.ui.form.IntegerField;
import ch.repit.rwt.client.ui.form.MultiSelectCheckBoxesField;
import ch.repit.rwt.client.ui.form.MultiSelectField;
import ch.repit.rwt.client.ui.form.SelectField;
import ch.repit.rwt.client.ui.form.TextField;
import ch.repit.rwt.client.util.DateUtil;
import ch.repit.rwt.client.user.UserDef;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.ButtonBase;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Widget;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 *
 * @author tc149752
 */
public class BookingDetailsPage extends FormPage {

    private Day startFromCal, endFromCal;
    private CalendarPage calPage;

    // needed for x-ref
    private Field numberPeople;
    private int prefLineFrom, prefLineTo;

    public BookingDetailsPage(ObjectRef bookingRef, Page topPage, CalendarPage calPage) {
        super(BookingDef.TYPE,  bookingRef, topPage);
        this.calPage = calPage;
    }

    public BookingDetailsPage(ObjectRef bookingRef, Page topPage, CalendarPage calPage,
                              Day startOrEnd1, Day startOrEnd2) {
        super(BookingDef.TYPE,  bookingRef, topPage);
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
            setTitle("Création Inscription");
            if (startFromCal != null)
                getObject().get(BookingDef.ATTR_FROM_DAY).set(startFromCal);
            if (endFromCal != null)
                getObject().get(BookingDef.ATTR_TO_DAY).set(endFromCal);
        }
        else {
            setTitle("Edition Inscription");
        }
        super.setShowPath(true);

        this.addSectionHead("Détails");

        // only editable in create with MANAGE action
        SelectField booker = new SelectField(BookingDef.ATTR_USER, false);
        SortedMap<String,String> bookerMap = new TreeMap<String,String>();
        boolean readonly;
        BentoDef userDef = BentoDefFactory.get().getDef(UserDef.TYPE);
        for (Bento a : CacheManager.get().getCachedObjects(UserDef.TYPE)) {
            if (!isCreate()                                   // then read only, thus we can have a full list (even with trash + archive)
                    || (a.getStatus()==BentoStatus.ACTIVE     // create new bookings only for active users
                        && a.get(UserDef.ATTR_ROLESREF).getStringList() != null   // thus we exclude Repit which has 0 roles
                        && a.get(UserDef.ATTR_ROLESREF).getStringList().size() > 0) )
                bookerMap.put(userDef.getCommonName(a), a.getRef().toString());
        }
        booker.setValueMap(bookerMap);
        // create and
        if (isCreate() && SecurityManager.get().getAuthorizer().isAllowed(Action.MANAGE, BookingDef.TYPE))
            readonly = false;
        else 
            readonly = true;
        this.addSingleFieldRow("Membre inscrit", "Seul le gestionnaire des inscriptions peut saisir une inscription pour le compte d'un autre membre", booker);
        booker.setReadOnly(readonly); // setReadOnly must be after addSingleFieldRow

        // dates
        DateField fromDate = new DateField(BookingDef.ATTR_FROM_DAY, "date d'arrivée");
        SelectField fromHour = new SelectField(BookingDef.ATTR_FROM_HOUR, "heure d'arrivée approx.", false);
        fromHour.setValueMap(BookingDef.getHourPeriodMap());
        this.addFieldRow(new FieldRow("Début du séjour", fromDate, fromHour));

        DateField toDate = new DateField(BookingDef.ATTR_TO_DAY, "date du départ");
        SelectField toHour = new SelectField(BookingDef.ATTR_TO_HOUR, "heure de départ approx.", false);
        toHour.setValueMap(BookingDef.getHourPeriodMap());
        this.addFieldRow(new FieldRow("Fin du séjour", toDate, toHour));
       
        numberPeople = new IntegerField(BookingDef.ATTR_NUMBER_PEOPLE, "nombre de personnnes");
        final CheckBoxField reserve = new CheckBoxField(BookingDef.ATTR_RESERVATION, "réservation");
        reserve.addChangeHandler(new FieldChangeHandler() {
            public void onChange() {
                switchResaMode(reserve.getValue() == Boolean.TRUE);
            }
        });
        this.addFieldRow(new FieldRow("Occupation", reserve, numberPeople));
        // number people validator: not empty and in range if not resa
        numberPeople.addValidator(new FieldValidator() {
            public boolean onValidate(List<String> messages, Object value, String attributeTitle) {
                if (!(reserve.getValue() == Boolean.TRUE)) {
                    String np = (String)numberPeople.getValue();
                    // other vali has resp to check if integer...
                    if (np == null || np.length() == 0) {
                        messages.add("'nombre de personnnes' doit être renseigné");
                        return false;
                    }
                    try {
                        int npi = Integer.parseInt(np);
                        if (npi < 1) {
                            messages.add("Le nombre de personnes ne peut être 0 ou négatif");
                            return false;
                        } else if (npi > 20) {
                            messages.add("Au dela de 20 personnes, veuillez faire un réservation");
                            return false;
                        }
                    } catch (NumberFormatException e) {
                        messages.add("Nombre de personnes incorrect...");
                        return false;
                    }
                }
                return true;
            }
        });

        // preferences
        prefLineFrom = this.addSectionHead("Préférences", "A titre indicatif. Veuillez contrôler la disponibilité au moment de l'inscription.");
        MultiSelectField livingRoomPref = new MultiSelectCheckBoxesField(BookingDef.ATTR_PREF_LIVING);
        livingRoomPref.setAvailableValues(BookingDef.listLivingRooms());
        FieldRow prefs = new FieldRow("Salon souhaité", "", livingRoomPref);
        this.addFieldRow(prefs);
        MultiSelectField dormingRoomPref = new MultiSelectCheckBoxesField(BookingDef.ATTR_PREF_DORMING);
        dormingRoomPref.setAvailableValues(BookingDef.listDormingRooms());
        FieldRow prefRooms = new FieldRow("Chambres souhaitées", "", dormingRoomPref);
        prefLineTo = this.addFieldRow(prefRooms);

        // description
        this.addSectionHead("Divers");
        TextField desc = new TextField(BookingDef.ATTR_DESCRIPTION, null, 3);
        desc.setColumns(60);
        this.addSingleFieldRow("Description", null, desc);

        super.init();
    }

    
    @Override
    public void setObject(Bento bento) {

        // sets the default user as this user for creates
        if (isCreate()) {
            Bento me = CacheManager.get().getCachedObject(SecurityManager.get().getAuthorizer().getPrincipal().getUserRef());
            bento.get(BookingDef.ATTR_USER).set(me.getRef().toString());
        }

        super.setObject(bento);

        // if object in the past, readonly-it
        Day d = bento.get(BookingDef.ATTR_TO_DAY).getDay();
        if (!isCreate() && d.before(DateUtil.today()) &&
                !SecurityManager.get().getAuthorizer().isAllowed(Action.MANAGE, BookingDef.TYPE)) {
            super.setReadOnly(true);
        }

        // set the number people field visible accoring to resa
        if (bento != null && bento.get(BookingDef.ATTR_RESERVATION) != null &&
                bento.get(BookingDef.ATTR_RESERVATION).getBoolean() != null) {
            switchResaMode( bento.get(BookingDef.ATTR_RESERVATION).getBoolean() == Boolean.TRUE );
        }
    }


    private void switchResaMode(boolean isResa) {
        if (numberPeople != null)
            numberPeople.setVisible(!isResa);
        for (int line = prefLineFrom; line <= prefLineTo; line++)
            this.setVisible(line, !isResa);
    }


    @Override
    protected void fillToolbarWidgets(List<Widget> leftWidgets,
                                      List<Widget> middleWidgets,
                                      List<Widget> rightWidgets) {
        super.fillToolbarWidgets(leftWidgets, middleWidgets, rightWidgets);

        if (isCreate() && SecurityManager.get().getAuthorizer().isAllowed(Action.CREATE, CalendarEventDef.TYPE)) { 
            Hyperlink newEvent = new Hyperlink("En fait, créer un CalEvent...", "createCalEvent");
            newEvent.addClickHandler(new ClickHandler() {
                public void onClick(ClickEvent arg0) {
                    getPageNav().back();
                    getPageNav().displayPage(new CalendarEventDetailsPage(null, null, calPage, startFromCal, endFromCal));
                } } );
            middleWidgets.add(newEvent);
        }
    }





    @Override
    protected void validateAfterRead(Bento bento, List<String> validationErrors) {
        super.validateAfterRead(bento, validationErrors);
   
        Day from = bento.get(BookingDef.ATTR_FROM_DAY).getDay();
        Day to = bento.get(BookingDef.ATTR_TO_DAY).getDay();
        //boolean isReserve = dto.get(BookingDef.ATTR_RESERVATION).getBoolean();

        if (from == null || from.getInteger() == null)
            validationErrors.add("Date de début non spécifiée");

        if (to == null || to.getInteger() == null)
            validationErrors.add("Date de fin non spécifiée");

        // check the date order
        if (from != null && to != null && from.after(to)) {
            validationErrors.add("Date de fin avant date de début");
        }

        // check that no reserve bookings in the middle
        //     THIS IS NOW PERFORMED ONLY SERVER-SIDE
    }

    
    @Override
    public void doSave(Bento bento) {
        // sets the owner if not us
        if (isCreate() && SecurityManager.get().getAuthorizer().isAllowed(Action.MANAGE, BookingDef.TYPE)) {
            bento.setOwnerRef(new ObjectRef(bento.get(BookingDef.ATTR_USER).getString()));
        }

        super.doSave(bento);
    }


    @Override
    protected void doSaveOnSuccess(ObjectRef objectRef) {
        if (calPage != null) {
            calPage.removeCalSelection();
        }
        super.doSaveOnSuccess(objectRef);
    }

}
