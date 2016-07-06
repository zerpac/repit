    /*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.repit.rwt.client.user;

// TBD!
//import ch.repit.client.calendar.BookingDef;
//import ch.repit.client.calendar.BookingListPage;
import ch.repit.rwt.client.Bento;
import ch.repit.rwt.client.BentoDefFactory;
import ch.repit.rwt.client.BentoStatus;
import ch.repit.rwt.client.ObjectRef;
import ch.repit.rwt.client.audit.AuditListPage;
import ch.repit.rwt.client.audit.AuditQueryDef;
import ch.repit.rwt.client.persistence.CacheManager;
import ch.repit.rwt.client.security.Action;
import ch.repit.rwt.client.security.Authorizer;
import ch.repit.rwt.client.security.RoleDef;
import ch.repit.rwt.client.security.SecurityManager;
import ch.repit.rwt.client.ui.BentoPage;
import ch.repit.rwt.client.ui.form.FormPage;
import ch.repit.rwt.client.ui.form.FieldValidator;
import ch.repit.rwt.client.ui.form.FieldRow;
import ch.repit.rwt.client.ui.Page;
import ch.repit.rwt.client.ui.form.CheckBoxField;
import ch.repit.rwt.client.ui.form.Field.FieldChangeHandler;
import ch.repit.rwt.client.ui.form.FileTable;
import ch.repit.rwt.client.ui.form.FileUploadField;
import ch.repit.rwt.client.ui.form.MultiSelectField;
import ch.repit.rwt.client.ui.form.SelectField;
import ch.repit.rwt.client.ui.form.RegexpFieldValidator;
import ch.repit.rwt.client.ui.form.TextField;
import ch.repit.rwt.client.util.CountryCodes;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ButtonBase;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Widget;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 *
 * @author tc149752
 */
public class UserDetailsPage extends BentoPage {

    ObjectRef userRef;

    public UserDetailsPage(ObjectRef userRef)
    {
        super(UserDef.TYPE, userRef, null);
        this.userRef = userRef;
    }

    @Override
    protected void init() {
        super.init();
        
        if (isCreate()) {
            setTitle("Création Utilisateur");
        }
        else {
            setTitle(getBentoDef().getCommonName(getObject()));
        }
        super.setShowPath(true);

        UserIdentityForm userIdentityForm = new UserIdentityForm(userRef, this);
        super.addTab("Identité", userIdentityForm);

        if (!isCreate()) {
            Authorizer auth = SecurityManager.get().getAuthorizer();

            UserLightDef userDef = (UserLightDef)BentoDefFactory.get().getDef(UserLightDef.TYPE);

            if (userDef instanceof UserDef && getObject().getStatus() == BentoStatus.ACTIVE
                    && auth.isAllowed(Action.UPDATE, getObject())) {
                UserPrefsForm upf = new UserPrefsForm(new ObjectRef(UserPrefDef.TYPE, getObject().getId()), this);
                super.addTab("Préférences", upf);
            }

/* TBD! - how to have custom tab in standard page ?
 * MAYBE better not to mix... if noone complains...
            // published Inscriptions: only those of current user
            super.addTab("Inscriptions", new BookingListPage(this, false) {
                @Override
                protected boolean formatObject(Bento dto, Map formatedValue) {
                    if (new ObjectRef(dto.get(BookingDef.ATTR_USER).getString())
                            .equals(getObject().getRef()))
                        return super.formatObject(dto, formatedValue);
                    return false;
                }
            });
*/
            // TBD: published blogs: only those of current user
            // super.addTab("Blogs publiés", null);
            // NTH, kind of hard because different blog types dont fit the same table...

            if (auth.isAllowed(Action.AUDIT, getObject())) {
                Bento queryHistory = BentoDefFactory.get().getDef(AuditQueryDef.TYPE).createBento();
                queryHistory.get(AuditQueryDef.ATTR_OBJECTREF).set(getObject().getRef().toString());
                queryHistory.get(AuditQueryDef.ATTR_FROMDAYS).set(365);
                super.addTab("Historique", new AuditListPage(this, queryHistory));
            }

            if (auth.isAllowed(Action.MANAGE, getObject())) {
                Bento queryUserActions = BentoDefFactory.get().getDef(AuditQueryDef.TYPE).createBento();
                queryUserActions.get(AuditQueryDef.ATTR_AUTHOR).set(getObject().getRef().toString());
                queryUserActions.get(AuditQueryDef.ATTR_FROMDAYS).set(365);
                super.addTab("Actions", new AuditListPage(this, queryUserActions));
            }
        }
    }


    private class UserIdentityForm extends FormPage
    {
        private FileTable filePage;
        private TextField zip;
        private SelectField country;

        private UserIdentityForm(ObjectRef userRef, Page aThis) {
            super(UserLightDef.TYPE, userRef, aThis);

            UserLightDef userDef = (UserLightDef)BentoDefFactory.get().getDef(UserLightDef.TYPE);

            this.addSectionHead("Identité");

            // login
            TextField login = new TextField("login");
            this.addSingleFieldRow("Login", "Identifiant unique utilisé pour se connecter au site", login);

            // name
            TextField firstName = new TextField("firstName", "prénom");
            TextField lastName = new TextField("lastName", "nom de famille");
            FieldRow nameRow = new FieldRow("Nom");
            nameRow.addField(firstName);
            nameRow.addField(lastName);
            this.addFieldRow(nameRow);

            // mail and phones
            this.addSectionHead("Contact");
            TextField email = new TextField("email");
            email.setColumns(45);
            email.addValidator(new RegexpFieldValidator
                    ("^[\\w._%+-]+@[\\w.-]+\\.[a-zA-Z]{2,6}$" , "L'email est invalide"));
            this.addSingleFieldRow("E-mail", "Adresse de courrier électronique pour réception des notifications", email);

            if (userDef instanceof UserDef) {
                FieldValidator phoneValidator = new RegexpFieldValidator("^[\\+\\d][\\d ]{9,16}$",
                        "Numéro invalide, veuillez ne saisir que des chiffres et des espace, et éventuellement un '+' en tête.");
                TextField phone = new TextField("privatePhone", "domicile");
                phone.addValidator(phoneValidator);
                phone.setColumns(20);
                TextField mobile = new TextField("mobilePhone", "natel");
                mobile.addValidator(phoneValidator);
                mobile.setColumns(20);
                TextField telProf = new TextField("workPhone", "professionnel");
                telProf.addValidator(phoneValidator);
                telProf.setColumns(20);
                FieldRow phone1 = new FieldRow("Téléphones");
                phone1.addField(phone);
                phone1.addField(mobile);
                phone1.addField(telProf);
                phone1.setHelp("Uniquement des chiffres et des espaces");
                this.addFieldRow(phone1);
                TextField fax = new TextField("fax");
                fax.setColumns(20);
                fax.addValidator(phoneValidator);
                this.addSingleFieldRow("Fax", null, fax);

                // address
                this.addSectionHead("Adresse");
                TextField address = new TextField("addressLine", null, 3);
                address.setColumns(60);
                this.addSingleFieldRow("Ligne d'adresse", "Rue et numéro, plus toute autre information nécessaire à vous faire parvenir des couriers", address);
                TextField locality = new TextField("locality", "localité");
                locality.setColumns(40);
                zip = new TextField("zipCode", "NPA");
                zip.setColumns(6);
                FieldRow zipLocality = new FieldRow("Localité");
                zipLocality.addField(zip);
                zipLocality.addField(locality);
                zipLocality.setHelp("Pour saisir un code postal étranger, changer d'abord le pays");
                this.addFieldRow(zipLocality);
                country = new SelectField("country", false);
                country.setValueList(CountryCodes.listCountries());
                this.addSingleFieldRow("Pays", "Suisse par défaut, si non renseigné", country);

                zip.addValidator(new FieldValidator() {
                    public boolean onValidate(List<String> messages, Object value, String attributeTitle) {
                        // si pays = suisse, NPA = 4 chiffres !!!
                        if (country != null && CountryCodes.SUISSE.equals(country.getValue())) {
                            String zipStr = (String)zip.getValue();
                            if (zipStr == null || zipStr.length() == 0) {
                                messages.add("Le NPA ne peut pas être vide en Suisse");
                                return false;
                            } else {
                                try {
                                    int zipi = Integer.parseInt(zipStr);
                                    if (zipi < 1000 || zipi > 9999) {
                                        messages.add("Le NPA en Suisse doit être compris entre 1000 et 9999");
                                        return false;
                                    }
                                } catch (NumberFormatException e) {
                                    messages.add("Le NPA doit être numérique en Suisse");
                                    return false;
                                }
                            }
                        }
                        return true;
                    }
                });
            }

            // roles
            this.addSectionHead("Rôles et fonctions");
            MultiSelectField rolesSelect = new MultiSelectField("rolesRef", null, "disponibles", "assignés");
            addSingleFieldRow("Roles", "Certains rôles donnent accès à des fonctions supplémentaires du site.", rolesSelect);
           
            Map<String,String> rolesValueMap = new HashMap<String,String>();
            if (CacheManager.get().getCachedObjects(RoleDef.TYPE) != null)
                for (Bento rto : CacheManager.get().getCachedObjects(RoleDef.TYPE))
                    if (rto != null)
                        rolesValueMap.put(rto.getDisplayValue("name"), rto.getRef().toString());
            rolesSelect.setAvailableValues(rolesValueMap);
            
            // picture
            this.addSectionHead("Photo");
            if (SecurityManager.get().getAuthorizer().isAllowed(Action.UPDATE, getObject())) {
                FileUploadField fuf = new FileUploadField("emplacement local", this);
                this.addSingleFieldRow("Envoyer une nouvelle photo",
                        "Veuillez spécifier l'emplacement local (sur votre ordinateur) d'un fichier de type image (jpeg, png, gif, etc), puis cliquer sur Transférer. Attention, l'image précédente sera perdue.",
                        fuf);
            }
            
            filePage = new FileTable(this);
            Widget table = filePage.getTable();
            table.addStyleName("rwt-formAttributePanel");
            super.addWidgetRow("Photo actuelle", null, table);

        }


        @Override
        protected void fillToolbarWidgets(List<Widget> leftWidgets,
                                      List<Widget> middleWidgets,
                                      List<Widget> rightWidgets) {
        super.fillToolbarWidgets(leftWidgets, middleWidgets, rightWidgets);

            Authorizer auth = SecurityManager.get().getAuthorizer();

            // remove the suppress button for itself
            if (auth.getPrincipal().getUserRef() != null
                    && super.getObject().getRef() != null 
                    && auth.getPrincipal().getUserRef().equals(super.getObject().getRef()))
            {
                ButtonBase btr = null;
                for (Widget b : leftWidgets) {
                    if (b instanceof ButtonBase && ((ButtonBase)b).getText().equals("Supprimer"))
                        btr = (ButtonBase)b;
                }
                if (btr != null)
                    leftWidgets.remove(btr);
            }

            // adds an archive button
            else if (auth.isAllowed(Action.MANAGE, getObject())) {
                if (getObject().getStatus() == BentoStatus.ACTIVE) {
                    Button archiveButton = new Button("Archiver");
                    archiveButton.addClickHandler(new ClickHandler() {
                        public void onClick(ClickEvent arg0) {
                            getObject().setStatus(BentoStatus.ARCHIVE);
                            doSave(getObject());
                        }
                    });
                    leftWidgets.add(archiveButton);
                } else if (getObject().getStatus() == BentoStatus.ARCHIVE) {
                    Button unarchiveButton = new Button("Réactiver");
                    unarchiveButton.addClickHandler(new ClickHandler() {
                        public void onClick(ClickEvent arg0) {
                            getObject().setStatus(BentoStatus.ACTIVE);
                            doSave(getObject());
                        }
                    });
                    leftWidgets.add(unarchiveButton);
                }
            }
        }

        @Override
        public void setObject(Bento bento) {
            super.setObject(bento);
            filePage.resetBento(bento);
        }

    }




    private class UserPrefsForm extends FormPage {
        
        int row1, row2;

        private UserPrefsForm(ObjectRef userPrefRef, Page aThis) {
            
            super(UserPrefDef.TYPE, userPrefRef, aThis);

            this.addSectionHead("Communications officielles");
            CheckBoxField offComCB = new CheckBoxField(UserPrefDef.ATTR_OFFICIAL_COMM_VIA_EMAIL);
            this.addSingleFieldRow("Recevoir les convocations par eMail",
                    "Cocher pour recevoir les courriers officiels uniquement par emails, et plus par courrier postal, dans la mesure du possible.",offComCB);

            this.addSectionHead("Rapports réguliers");
            CheckBoxField weeklyReport = new CheckBoxField(UserPrefDef.ATTR_WEEKLY_REPORT);
            this.addSingleFieldRow("Mail hebdomadaire", "Cocher pour recevoir un mail chaque mercredi soir résumant les actions de la semaine écoulée.", weeklyReport);
            CheckBoxField dailyReport = new CheckBoxField(UserPrefDef.ATTR_DAILY_REPORT);
            this.addSingleFieldRow("Mail quotidien", "Cocher pour recevoir un mail chaque soir résumant les actions de la journée écoulée. Si rien ne s'est passé, le mail n'est pas envoyé.", dailyReport);

            this.addSectionHead("Notifications immédiates");
            final CheckBoxField allAlerts = new CheckBoxField(UserPrefDef.ATTR_ALERT_ALL, "Cocher pour activer");
            final CheckBoxField createAlerts = new CheckBoxField(UserPrefDef.ATTR_ALERT_CREATES, "Cocher pour activer");
            final CheckBoxField mineAlerts = new CheckBoxField(UserPrefDef.ATTR_ALERT_MINES, "Cocher pour activer");

            this.addSingleFieldRow("Toutes les notifications (non recommandé!)", "Pour recevoir un mail lors de chaque modification sur le site (non recommandé!)", allAlerts);
            row1 = this.addSingleFieldRow("Les nouveaux objects", "Pour recevoir un mail lors de chaque création sur le site", createAlerts);
            row2 = this.addSingleFieldRow("Les modifications me concernant", "Pour recevoir un mail lors de chaque mise à jour de vos objects ou d'objects que vous avez commenté", mineAlerts);

            allAlerts.addChangeHandler(new FieldChangeHandler() {
                public void onChange() {
                    switchAlerts(allAlerts.getValue() == Boolean.FALSE);
                }
            });
        }


        // no one should be able to delete prefs (would delete the user !!!)
        @Override
        protected void fillToolbarWidgets(List<Widget> leftWidgets,
                                      List<Widget> middleWidgets,
                                      List<Widget> rightWidgets) {
        super.fillToolbarWidgets(leftWidgets, middleWidgets, rightWidgets);

            // remove the suppress button for all
            ButtonBase btr = null;
            for (Widget b : leftWidgets) {
                if (b instanceof ButtonBase && ((ButtonBase)b).getText().equals("Supprimer"))
                    btr = (ButtonBase)b;
            }
            if (btr != null)
                leftWidgets.remove(btr);
            
        }

        
        private void switchAlerts(boolean showDetails) {
            this.setVisible(row1, showDetails);
            this.setVisible(row2, showDetails);
        }


    }

}
