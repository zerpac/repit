/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.repit.site.client.calendar;

import ch.repit.site.client.calendar.CalendarEventDef.CalEventType;
import ch.repit.rwt.client.Bento;
import ch.repit.rwt.client.BentoStatus;
import ch.repit.rwt.client.ui.BentoListPage;
import ch.repit.rwt.client.ui.Page;
import ch.repit.rwt.client.util.Formaters;
import java.util.Map;

/**
 *
 * @author tc149752
 */
public class CalendarEventListPage extends BentoListPage {
    
    public CalendarEventListPage(Page topPage, BentoStatus... statuses)
    {
        super (topPage, CalendarEventDef.TYPE, statuses);
        super.setShowPath(false);

        // set title depending on status
        String title = "liste des événements";
        if (listStatus().size() == 1) {
            if (listStatus().contains(BentoStatus.ACTIVE))
                title += " futurs";
            else if (listStatus().contains(BentoStatus.TRASH))
                title += " supprimés";
        }
        setTitle(title);

        // set the columns
        super.addColumn("Intitulé", CalendarEventDef.ATTR_TITLE, true);
        super.addDateColumn("Débutant le", CalendarEventDef.ATTR_FROM_DAY, true, Formaters.DatePattern.DATE);
        super.addDateColumn("Se terminant le", CalendarEventDef.ATTR_TO_DAY, true, Formaters.DatePattern.DATE);
        super.addColumn("Type d'événement", "_eventTypeName", true);
        super.addNumberCommentsColumn();
        super.addColumn("Description", "_descStart", false);
    }

    @Override
    protected boolean formatObject(Bento bento, Map formatedValue) {
      
        String desc = bento.getDisplayValue(CalendarEventDef.ATTR_DESCRIPTION);
        if (desc == null)
            desc = "--";
        if (desc.length() > 40)
            desc = desc.substring(0, 40) + " (...)";
        formatedValue.put("_descStart", desc);
        
        formatedValue.put("_eventTypeName",
                CalEventType.valueOf(bento.get(CalendarEventDef.ATTR_EVENT_TYPE).getString()).getLabel());

        return super.formatObject(bento, formatedValue);
    }


    @Override
    protected int sortCompare(Bento bento1, Bento bento2, String sortAttribute, boolean ascending) {
        if (sortAttribute.equals("_eventTypeName")) {
            String ev1 = CalEventType.valueOf(bento1.get(CalendarEventDef.ATTR_EVENT_TYPE).getString()).getLabel();
            String ev2 = CalEventType.valueOf(bento2.get(CalendarEventDef.ATTR_EVENT_TYPE).getString()).getLabel();
            return ev1.compareTo(ev2) * (ascending?1:-1);
        }
        return super.sortCompare(bento1, bento2, sortAttribute, ascending);
    }

    @Override
    protected void onRowClicked(Bento bento, String attributeClicked) {
        getPageNav().displayPage(new CalendarEntryPage(bento.getRef()));
    }

}
