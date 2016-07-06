/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.repit.site.client.contact;

import ch.repit.rwt.client.Bento;
import ch.repit.rwt.client.BentoStatus;
import ch.repit.rwt.client.ui.BentoListPage;
import ch.repit.rwt.client.ui.Page;
import ch.repit.rwt.client.util.CountryCodes;
import ch.repit.rwt.client.security.Action;
import ch.repit.rwt.client.security.SecurityManager;

import com.google.gwt.user.client.ui.HTML;
import java.util.Map;


/**
 *
 * @author tc149752
 */
public class YpeListPage extends Page {

    public YpeListPage()
    {
        super();

        setTitle("Pages Jaunes");
        super.setShowPath(false);
        super.addTab("Contacts", new YpeListDisplay(this, BentoStatus.ACTIVE, BentoStatus.ARCHIVE));

        if (SecurityManager.get().getAuthorizer().isAllowed(Action.VIEW_TRASH, YellowPagesEntryDef.TYPE))
            super.addTab("Corbeille", new YpeListDisplay(this, BentoStatus.TRASH));
    }


    private class YpeListDisplay extends BentoListPage
    {
        YpeListDisplay(Page topPage, BentoStatus... status)  {
            super (topPage, YellowPagesEntryDef.TYPE, status);

            // set the columns
            super.addColumn("Raison sociale", YellowPagesEntryDef.ATTR_RAISONSOCIALE, true);
            super.addColumn("Adresse", "_address", false);
            super.addColumn("Téléphones", "_phones", false);
            super.addColumn("Nom", YellowPagesEntryDef.ATTR_CONTACT_LASTNAME, true);
            super.addColumn("Prénom", YellowPagesEntryDef.ATTR_CONTACT_FIRSTNAME, true);
            super.addColumn("Email", YellowPagesEntryDef.ATTR_EMAIL, true);
            super.addColumn("Site Internet", YellowPagesEntryDef.ATTR_WEBSITE, true);
            super.addNumberCommentsColumn();
        }

        
        @Override
        protected boolean formatObject(Bento bento, Map formatedValue) {

            // format address for display in table
            String addressHtml = "";
            if (bento.get(YellowPagesEntryDef.ATTR_ADDRESSLINE) != null &&
                    bento.get(YellowPagesEntryDef.ATTR_ADDRESSLINE).getString() != null ) // + lenght...
                addressHtml += bento.get(YellowPagesEntryDef.ATTR_ADDRESSLINE).getString().replaceAll("\n", "<br/>") + "<br/>";
            if (bento.get(YellowPagesEntryDef.ATTR_ZIPCODE) != null &&
                    bento.get(YellowPagesEntryDef.ATTR_ZIPCODE).getString() != null )
                addressHtml += "<b>" + bento.get(YellowPagesEntryDef.ATTR_ZIPCODE).getString() + "</b> ";
            if (bento.get(YellowPagesEntryDef.ATTR_LOCALITY) != null
                    && bento.get(YellowPagesEntryDef.ATTR_LOCALITY).getString() != null )
                addressHtml += bento.get(YellowPagesEntryDef.ATTR_LOCALITY).getString() + "<br/>";
            if (bento.get(YellowPagesEntryDef.ATTR_COUNTRY) != null
                    && bento.get(YellowPagesEntryDef.ATTR_COUNTRY).getString() != null )
                if (!CountryCodes.SUISSE.equals(bento.get(YellowPagesEntryDef.ATTR_COUNTRY).getString()))
                    addressHtml += bento.get(YellowPagesEntryDef.ATTR_COUNTRY).getString() + "<br/>";
            HTML ahw = new HTML(addressHtml);
            ahw.addStyleName("repit-listDisplayCellMultiLine");
            formatedValue.put("_address", ahw);

            // phones
            String phonesHtml = "";
            if (bento.get(YellowPagesEntryDef.ATTR_PHONE) != null
                    && bento.get(YellowPagesEntryDef.ATTR_PHONE).getString() != null ) 
                phonesHtml += "tél: " + bento.get(YellowPagesEntryDef.ATTR_PHONE).getString()+ "<br/>";
            if (bento.get(YellowPagesEntryDef.ATTR_FAX) != null
                    && bento.get(YellowPagesEntryDef.ATTR_FAX).getString() != null )
                phonesHtml += "fax: " + bento.get(YellowPagesEntryDef.ATTR_FAX).getString()+ "<br/>";
            if (bento.get(YellowPagesEntryDef.ATTR_MOBILE) != null
                    && bento.get(YellowPagesEntryDef.ATTR_MOBILE).getString() != null )
                phonesHtml += "natel: " + bento.get(YellowPagesEntryDef.ATTR_MOBILE).getString()+ "<br/>";
            HTML phonesH = new HTML(phonesHtml);
            phonesH.addStyleName("repit-listDisplayCellMultiLine");
            formatedValue.put("_phones", phonesH);

            return super.formatObject(bento, formatedValue);
        }

        
        @Override
        protected void onRowClicked(Bento obj, String attrClicked) {
            getPageNav().displayPage(new YPEntryPage(obj.getRef()));
        }

        
    }

}
