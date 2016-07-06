/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.repit.rwt.client.user;

import ch.repit.rwt.client.Bento;
import ch.repit.rwt.client.BentoAttribute;
import ch.repit.rwt.client.BentoDefFactory;
import ch.repit.rwt.client.BentoStatus;
import ch.repit.rwt.client.FileDescriptor;
import ch.repit.rwt.client.ObjectRef;
import ch.repit.rwt.client.pdf.StickersPrintingPage;
import ch.repit.rwt.client.ui.BentoListPage;
import ch.repit.rwt.client.ui.Page;
import ch.repit.rwt.client.util.CountryCodes;
import ch.repit.rwt.client.persistence.CacheManager;
import ch.repit.rwt.client.security.Action;
import ch.repit.rwt.client.security.RoleDef;
import ch.repit.rwt.client.security.Authorizer;
import ch.repit.rwt.client.security.SecurityManager;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.ButtonBase;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;
import java.util.List;
import java.util.Map;


/**
 *
 * @author tc149752
 */
public class UserListPage extends Page {


    public UserListPage() {
        super();

        UserLightDef userDef = (UserLightDef)BentoDefFactory.get().getDef(UserLightDef.TYPE);

        if (userDef instanceof UserDef)
            setTitle("Membres");
        else
            setTitle("Utilisateur");

        super.setShowPath(false);

        super.addTab("Actifs", new UserListDisplay(this, BentoStatus.ACTIVE));
        if (SecurityManager.get().getAuthorizer().isAllowed(Action.MANAGE, UserDef.TYPE))
            super.addTab("Archives", new UserListDisplay(this, BentoStatus.ARCHIVE));
        if (SecurityManager.get().getAuthorizer().isAllowed(Action.VIEW_TRASH, UserDef.TYPE))
            super.addTab("Corbeille", new UserListDisplay(this, BentoStatus.TRASH));

        if (userDef instanceof UserDef)
            super.addTab("Etiquettes", new StickersPrintingPage(this));
    }


    private class UserListDisplay extends BentoListPage
    {
        private UserLightDef userDef;


        UserListDisplay(Page topPage, BentoStatus status)  {
            super (topPage, UserDef.TYPE, status);

            userDef = (UserLightDef)BentoDefFactory.get().getDef(UserLightDef.TYPE);

            setPrintable(!(userDef instanceof UserDef));

            // set the columns
            Authorizer auth = SecurityManager.get().getAuthorizer();
            if (auth.isAllowed(Action.ADMIN, UserDef.TYPE) && getPageNav().isDebugEnabled())
                super.addColumn("Login",      UserDef.ATTR_LOGIN,        true);

            super.addColumn("Nom",          UserDef.ATTR_LASTNAME,     true);
            super.addColumn("Prénom",       UserDef.ATTR_FIRSTNAME,    true);
            if (userDef instanceof UserDef) {
                super.addColumn("Adresse",      "_address",     false);
                super.addColumn("Téléphones",   "_phones",        false);
            }
            super.addColumn("Email",        UserDef.ATTR_EMAIL,        true);    // TBD: display with phones ???
            super.addColumn("Rôles",        "_roles",       false);
            super.addColumn("Photo",        "_photoPreview", false);
        }


        @Override
        protected void fillToolbarWidgets(List<Widget> leftWidgets,
                                      List<Widget> middleWidgets,
                                      List<Widget> rightWidgets) {
            super.fillToolbarWidgets(leftWidgets, middleWidgets, rightWidgets);

            if (userDef instanceof UserDef
                    && listSupportedStatus().contains(BentoStatus.ACTIVE)) {
                Anchor pdfButton = new Anchor("<img src='"+GWT.getModuleBaseURL()+"icons/pdficon_small.gif'/>",
                                true,
                                "/pdf/listeDesMembres.pdf",
                                "pdfTab");
                pdfButton.setTitle("générer un fichier PDF, pour une impression... impressionante!");
                rightWidgets.add(0,pdfButton);
            }
        }

        
        @Override
        protected boolean formatObject(Bento bento, Map formatedValue) {

            // the photo
            FileDescriptor[] photos = bento.getAttachedFiles();
            if (photos != null && photos.length > 0) {
                formatedValue.put("_photoPreview",
                    new Image(photos[0].getPreviewUrl(bento.getRef())));
            }

            // fetch roles labels
            BentoAttribute rolesAttr = bento.get(UserDef.ATTR_ROLESREF);
            HTML html = new HTML();
            if (rolesAttr.getStringList() != null) {
                String val = "";
                for (String item : rolesAttr.getStringList()) {
                    if (item != null) {
                        ObjectRef roleRef = new ObjectRef(item);
                        Bento role = CacheManager.get().getCachedObject(roleRef);
                        if (role != null) {
                            String roleLabel = role.getDisplayValue(RoleDef.ATTR_NAME);
                            // if comitee, put in bold and first
                            String cat = role.getDisplayValue(RoleDef.ATTR_CATEGORY);
                            if (RoleDef.Category.Primary.getName().equals(cat)) {
                                roleLabel = "<b>" + roleLabel + "</b>";
                                val = roleLabel +"<br/>" + val;
                            } else if (RoleDef.Category.Secondary.getName().equals(cat))
                                val += roleLabel +"<br/>";
                            // if "Hidden", we dont display the role
                        } else
                            val += "("+ item +")<br/>";
                    }
                }
                html.setHTML(val);
            } else
                html.setHTML("");
            html.addStyleName("rwt-list-cellMultiLine");
            formatedValue.put("_roles", html);

            // format address for display in table
            if (userDef instanceof UserDef) {
                String addressHtml = "";
                if (bento.get(UserDef.ATTR_ADDRESSLINE) != null && bento.get(UserDef.ATTR_ADDRESSLINE).getString() != null ) // + lenght...
                    addressHtml += bento.get(UserDef.ATTR_ADDRESSLINE).getString().replaceAll("\n", "<br/>") + "<br/>";
                if (bento.get(UserDef.ATTR_ZIP) != null && bento.get(UserDef.ATTR_ZIP).getString() != null )
                    addressHtml += "<b>" + bento.get(UserDef.ATTR_ZIP).getString() + "</b> ";
                if (bento.get(UserDef.ATTR_LOCALITY) != null && bento.get(UserDef.ATTR_LOCALITY).getString() != null )
                    addressHtml += bento.get(UserDef.ATTR_LOCALITY).getString() + "<br/>";
                if (bento.get(UserDef.ATTR_COUNTRY) != null && bento.get(UserDef.ATTR_COUNTRY).getString() != null )
                    if (!CountryCodes.SUISSE.equals(bento.get(UserDef.ATTR_COUNTRY).getString()))
                        addressHtml += bento.get(UserDef.ATTR_COUNTRY).getString() + "<br/>";
                HTML ahw = new HTML(addressHtml);
                ahw.addStyleName("rwt-list-cellMultiLine");
                formatedValue.put("_address", ahw);

                // phones
                String phonesHtml = "";
                if (bento.get(UserDef.ATTR_PRIVATEPHONE) != null
                        && bento.get(UserDef.ATTR_PRIVATEPHONE).getString() != null )
                    phonesHtml += "tél privé: " + bento.get(UserDef.ATTR_PRIVATEPHONE).getString()+ "<br/>";
                if (bento.get(UserDef.ATTR_MOBILEPHONE) != null
                        && bento.get(UserDef.ATTR_MOBILEPHONE).getString() != null )
                    phonesHtml += "natel: " + bento.get(UserDef.ATTR_MOBILEPHONE).getString()+ "<br/>";
                if (bento.get(UserDef.ATTR_WORKPHONE) != null
                        && bento.get(UserDef.ATTR_WORKPHONE).getString() != null )
                    phonesHtml += "tél prof: " + bento.get(UserDef.ATTR_WORKPHONE).getString()+ "<br/>";
                if (bento.get(UserDef.ATTR_FAX) != null
                        && bento.get(UserDef.ATTR_FAX).getString() != null )
                    phonesHtml += "fax: " + bento.get(UserDef.ATTR_FAX).getString()+ "<br/>";
                HTML phonesH = new HTML(phonesHtml);
                phonesH.addStyleName("rwt-list-cellMultiLine");
                formatedValue.put("_phones", phonesH);
            }
            
            return super.formatObject(bento, formatedValue);
        }


        @Override
        protected void onRowClicked(Bento user, String columnsAttributeName) {
            getPageNav().displayPage(new UserDetailsPage(user.getRef()));
        }

        
    }

}
