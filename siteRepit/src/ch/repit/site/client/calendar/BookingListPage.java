/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.repit.site.client.calendar;

import ch.repit.rwt.client.Bento;
import ch.repit.rwt.client.BentoStatus;
import ch.repit.rwt.client.ObjectRef;
import ch.repit.rwt.client.persistence.CacheManager;
import ch.repit.rwt.client.ui.BentoListPage;
import ch.repit.rwt.client.ui.Page;
import ch.repit.rwt.client.util.Formaters;
import java.util.Map;

/**
 *
 * @author tc149752
 */
public class BookingListPage extends BentoListPage {
    

    public BookingListPage(Page topPage, BentoStatus... statuses)
    {
        super (topPage, BookingDef.TYPE, statuses);
        super.setShowPath(false);

        // set title depending on status
        String title = "liste des inscriptions";
        if (listStatus().size() == 1) {
            if (listStatus().contains(BentoStatus.ACTIVE))
                title += " futures";
            else if (listStatus().contains(BentoStatus.TRASH))
                title += " supprimées";
        }
        setTitle(title);

        // set the columns
        super.addColumn("Inscription de", "_user", true);
        super.addDateColumn("Du", BookingDef.ATTR_FROM_DAY, true, Formaters.DatePattern.DATE);
        super.addDateColumn("Au", BookingDef.ATTR_TO_DAY, true, Formaters.DatePattern.DATE);
        super.addColumn("Occupation", "_occupation", true);
        super.addNumberCommentsColumn();
        super.addColumn("Description", "_descStart", false);
    }

    @Override
    protected boolean formatObject(Bento bento, Map formatedValue) {
        formatedValue.put("_occupation",
                bento.get(BookingDef.ATTR_RESERVATION).getBoolean()?
                    "Réservation":
                    (""+bento.get(BookingDef.ATTR_NUMBER_PEOPLE).getInteger()) );

        String desc = bento.getDisplayValue(BookingDef.ATTR_DESCRIPTION);
        if (desc == null)
            desc = "--";
        if (desc.length() > 40)
            desc = desc.substring(0, 40) + "(...)";
        formatedValue.put("_descStart", desc);

        if (bento.get(BookingDef.ATTR_USER) != null && bento.get(BookingDef.ATTR_USER).getString() != null) {
            String userName = bento.get(BookingDef.ATTR_USER).getString();
            Bento userb = null;
            if (userName != null)
                userb = CacheManager.get().getCachedObject(new ObjectRef(userName));
            if (userb != null)
                userName = userb.getDef().getCommonName(userb);
            else
                userName = "(" + userName + ")";
            formatedValue.put("_user",userName);
        }
        return super.formatObject(bento, formatedValue);
    }


    @Override
    protected int sortCompare(Bento dto1, Bento dto2, String sortAttribute, boolean ascending) {
        if (sortAttribute.equals("_user")) {
            if (dto1.getDisplayValue(BookingDef.ATTR_USER) != null && dto2.getDisplayValue(BookingDef.ATTR_USER) != null)
                return (dto1.getDisplayValue(BookingDef.ATTR_USER).compareTo(dto2.getDisplayValue(BookingDef.ATTR_USER)) * (ascending?1:-1) );
            return 0;
        } else if (sortAttribute.equals("_occupation")) {
            return (dto1.get(BookingDef.ATTR_RESERVATION).getBoolean()?10000:dto1.get(BookingDef.ATTR_NUMBER_PEOPLE).getInteger()
                    .compareTo(dto2.get(BookingDef.ATTR_RESERVATION).getBoolean()?10000:dto2.get(BookingDef.ATTR_NUMBER_PEOPLE).getInteger()
                    ) * (ascending?1:-1) );
        } else
            return super.sortCompare(dto1, dto2, sortAttribute, ascending);
    }



    @Override
    protected void onRowClicked(Bento bento, String attributeClicked) {
        getPageNav().displayPage(new CalendarEntryPage(bento.getRef()));
    }

}
