/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.repit.site.client.calendar;

import ch.repit.rwt.client.AttributeDef;
import ch.repit.rwt.client.AttributeDef.Feature;
import ch.repit.rwt.client.AttributeType;
import ch.repit.rwt.client.Bento;
import ch.repit.rwt.client.BentoDef;
import ch.repit.rwt.client.ObjectRef;
import ch.repit.rwt.client.annotations.IncludeInAlerts;
import ch.repit.rwt.client.annotations.IncludeInReports;
import ch.repit.rwt.client.persistence.CacheManager;
import ch.repit.rwt.client.ui.Page;
import ch.repit.rwt.client.util.Formaters;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;


@IncludeInAlerts
@IncludeInReports
public class BookingDef extends BentoDef {

    public static final String TYPE = "Booking";

    public static final String ATTR_USER = "userRef";
    public static final String ATTR_FROM_DAY = "fromDay";
    public static final String ATTR_TO_DAY = "toDay";
    public static final String ATTR_FROM_HOUR = "fromHour";
    public static final String ATTR_TO_HOUR = "toHour";
    public static final String ATTR_NUMBER_PEOPLE = "numberPeople";  // deprectated... (derived from number member...)
    public static final String ATTR_DESCRIPTION = "description";
    public static final String ATTR_RESERVATION = "isReservation";

    public static final String ATTR_PREF_LIVING = "prefLivingRoom";
    public static final String ATTR_PREF_DORMING = "prefDormingRoom";

    // will be in next step...
    public static final String ATTR_NUMBER_MEMBRE_ADULT = "numberMemberAdult";
    public static final String ATTR_NUMBER_MEMBRE_KIDS = "numberMemberKids";
    public static final String ATTR_NUMBER_NONMEMBRE_ADULT = "numberNonMemberAdult";
    public static final String ATTR_NUMBER_NONMEMBRE_KIDS = "numberNonMemberKids";
    public static final String ATTR_PHONE_AMOUNT = "phoneAmount";
    public static final String ATTR_INTERNET_AMOUNT = "internetAmount";
    public static final String ATTR_MISC_AMOUNTS = "variousAmount";
    public static final String ATTR_CAISSIER_COMMS = "variousAmountDescription";


    private static Set<AttributeDef> attrDefs = new HashSet<AttributeDef>();
    static {
        attrDefs.add(new AttributeDef(ATTR_USER, AttributeType.STRING, Feature.MANDATORY, Feature.REQUIRE_MANAGE_TO_EDIT));
        attrDefs.add(new AttributeDef(ATTR_FROM_DAY, AttributeType.DAY, Feature.MANDATORY));
        attrDefs.add(new AttributeDef(ATTR_FROM_HOUR, AttributeType.STRING, ""));
        attrDefs.add(new AttributeDef(ATTR_TO_DAY, AttributeType.DAY, Feature.MANDATORY));
        attrDefs.add(new AttributeDef(ATTR_TO_HOUR, AttributeType.STRING, ""));
        attrDefs.add(new AttributeDef(ATTR_RESERVATION, AttributeType.BOOLEAN, Boolean.FALSE, Feature.MANDATORY));
        attrDefs.add(new AttributeDef(ATTR_NUMBER_PEOPLE, AttributeType.INTEGER));  // mandatory if not reservation, performed client-side during validation...
        attrDefs.add(new AttributeDef(ATTR_PREF_LIVING, AttributeType.STRING_LIST));
        attrDefs.add(new AttributeDef(ATTR_PREF_DORMING, AttributeType.STRING_LIST));
        attrDefs.add(new AttributeDef(ATTR_DESCRIPTION, AttributeType.STRING));
    }

    public BookingDef() {
        super(attrDefs);
    }

    public String getType() {
        return TYPE;
    }

    public String getTypeLabel() {
        return "Inscription";
    }

    @Override
    public LabelGender getLabelGender() {
        return LabelGender.FEMININ;
    }

    public String getJdoClassName() {
        return "ch.repit.site.server.booking.Booking";
    }

    @Override
    public String getValidator() {
        return "ch.repit.site.server.booking.BookingValidator";
    }

    @Override
    public OwnerPolicy getOwnerPolicy() {
        return OwnerPolicy.CREATOR_IF_NOT_SPECIFIED;
    }


    @Override
    public String getDistinguishedAttribute() {
        return null;
    }

    /**
     * Waring: only client side !!! (because of formaters...)
     * @param bento
     * @return
     */
    @Override
    public String getCommonName(Bento bento) {
        String userName = bento.get(ATTR_USER).getString();
        Bento userb = null;
        if (userName != null)
            userb = CacheManager.get().getCachedObject(new ObjectRef(userName));
        if (userb != null)
            userName = userb.getDef().getCommonName(userb);
        else
            userName = "(" + userName + ")";

        boolean resa = bento.get(ATTR_RESERVATION).getBoolean();

        return (resa?"Réservation":"Inscription") + " de " + userName +
                Formaters.get().formatDateRange(bento.get(ATTR_FROM_DAY).getDay(),
                                                bento.get(ATTR_TO_DAY).getDay() );
    }

    @Override
    public Page getViewPage(ObjectRef oref) {
        if (oref == null)
            return new BookingDetailsPage(null, null, null);
        else
            return new CalendarEntryPage(oref);
    }

    
    ////////////////

    private static SortedMap<String,String> hourPeriodMap = null;
    public static SortedMap<String,String> getHourPeriodMap() {
        if (hourPeriodMap == null) {
            hourPeriodMap = new TreeMap<String,String>(); 
            hourPeriodMap.put("- inconnue -", "");
            hourPeriodMap.put("1. matin (avant 11h)", "0");
            hourPeriodMap.put("2. midi (entre 11h et 15h)", "1");
            hourPeriodMap.put("3. après-midi (entre 15h et 19h)", "2");
            hourPeriodMap.put("4. soir (après 19h)", "3");
        }
        return hourPeriodMap;
    }
    
    private static SortedMap<String,String> livingRooms = null;
    public static SortedMap<String,String> listLivingRooms() {
        if (livingRooms == null) {
            livingRooms = new TreeMap<String,String>();
            livingRooms.put("Vieux salon", "vieuxSalon");
            livingRooms.put("Salon Dorchaux", "salonDorchaux");
        }
        return livingRooms;
    }


    private static Map<String,String> dormingRooms = null;
    public static Map<String,String> listDormingRooms() {
        if (dormingRooms == null) {
            dormingRooms = new HashMap<String,String>();
            dormingRooms.put("Dame de Coeur (3-4 lits)", "dameDeCoeur");
            dormingRooms.put("Chambre Dorchaux (4 lits)", "chambreDorchaux");
            dormingRooms.put("Grande chambre grange (4 lits)", "grandeChambreGrange");
            dormingRooms.put("Chambre du berger (2 lits)", "chambreBerger");
        }
        return dormingRooms;
    }

}


