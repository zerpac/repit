/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.repit.site.client.contact;

import ch.repit.rwt.client.Bento;
import ch.repit.rwt.client.BentoDefFactory;
import ch.repit.rwt.client.ObjectRef;
import ch.repit.rwt.client.audit.AuditListPage;
import ch.repit.rwt.client.audit.AuditQueryDef;
import ch.repit.rwt.client.security.Action;
import ch.repit.rwt.client.security.Authorizer;
import ch.repit.rwt.client.security.SecurityManager;
import ch.repit.rwt.client.ui.BentoPage;
import ch.repit.rwt.client.ui.form.FormPage;
import ch.repit.rwt.client.ui.form.FieldValidator;
import ch.repit.rwt.client.ui.form.FieldRow;
import ch.repit.rwt.client.ui.Page;
import ch.repit.rwt.client.ui.form.SelectField;
import ch.repit.rwt.client.ui.form.RegexpFieldValidator;
import ch.repit.rwt.client.ui.form.TextField;
import ch.repit.rwt.client.util.CountryCodes;
import java.util.List;


/**
 *
 * @author tc149752
 */
public class YPEntryPage extends BentoPage {

    private ObjectRef ypeRef;

    public YPEntryPage(ObjectRef ypeRef)
    {
        super(YellowPagesEntryDef.TYPE, ypeRef, null);
        this.ypeRef = ypeRef;
    }

    @Override
    protected void init() {
        super.init();
        
        if (isCreate()) {
            setTitle("Création Contact Pages Jaunes");
        }
        else {
            setTitle("Edition Contact " + getBentoDef().getCommonName(getObject()));
        }
        super.setShowPath(true);

        YellowPagesEntryForm userIdentityForm = new YellowPagesEntryForm(ypeRef, this);
        super.addTab("Contact", userIdentityForm);

        if (!isCreate()) {
            Authorizer auth = SecurityManager.get().getAuthorizer();

            if (auth.isAllowed(Action.AUDIT, getObject())) {
                Bento queryHistory = BentoDefFactory.get().getDef(AuditQueryDef.TYPE).createBento();
                queryHistory.get(AuditQueryDef.ATTR_OBJECTREF).set(getObject().getRef().toString());
                queryHistory.get(AuditQueryDef.ATTR_FROMDAYS).set(90);
                super.addTab("Historique", new AuditListPage(this, queryHistory));
            }
        }
    }

    
    private class YellowPagesEntryForm extends FormPage
    {
        private TextField zip;
        private SelectField country;

        private YellowPagesEntryForm(ObjectRef ypeRef, Page topPage) {
            super( YellowPagesEntryDef.TYPE, ypeRef, topPage);

            this.addSectionHead("Identité");

            // login
            TextField raisonSociale = new TextField(YellowPagesEntryDef.ATTR_RAISONSOCIALE);
            this.addSingleFieldRow("Raison sociale", "Nom de la société", raisonSociale);

            // name
            TextField firstName = new TextField(YellowPagesEntryDef.ATTR_CONTACT_FIRSTNAME, "prénom");
            TextField lastName = new TextField(YellowPagesEntryDef.ATTR_CONTACT_LASTNAME, "nom de famille");
            FieldRow nameRow = new FieldRow("Personne de contact");
            nameRow.addField(firstName);
            nameRow.addField(lastName);
            this.addFieldRow(nameRow);

            // mail and phones
            this.addSectionHead("Moyens de communication");
            TextField email = new TextField(YellowPagesEntryDef.ATTR_EMAIL);
            email.setColumns(45);
            email.addValidator(new RegexpFieldValidator
                    ("^[\\w._%+-]+@[\\w.-]+\\.[a-zA-Z]{2,6}$" , "L'email est invalide"));
            this.addSingleFieldRow("Email", null, email);

            FieldValidator phoneValidator = new RegexpFieldValidator("^[\\+\\d][\\d ]{9,16}$",
                    "Numéro invalide, veuillez ne saisir que des chiffres et des espace, et éventuellement un '+' en tête.");
            TextField phone = new TextField(YellowPagesEntryDef.ATTR_PHONE, "téléphone");
            phone.addValidator(phoneValidator);
            phone.setColumns(20);
            TextField mobile = new TextField(YellowPagesEntryDef.ATTR_MOBILE, "natel");
            mobile.addValidator(phoneValidator);
            mobile.setColumns(20);
            FieldRow phone1 = new FieldRow("Téléphones");
            phone1.addField(phone);
            phone1.addField(mobile);
            phone1.setHelp("Uniquement des chiffres et des espaces");
            this.addFieldRow(phone1);
            TextField fax = new TextField(YellowPagesEntryDef.ATTR_FAX);
            fax.setColumns(20);
            fax.addValidator(phoneValidator);
            this.addSingleFieldRow("Fax", null, fax);
            
            // address
            this.addSectionHead("Adresse Postale");
            TextField address = new TextField( YellowPagesEntryDef.ATTR_ADDRESSLINE, null, 3);
            address.setColumns(60);
            this.addSingleFieldRow("Ligne d'adresse", "Rue et numéro", address);
            TextField locality = new TextField(YellowPagesEntryDef.ATTR_LOCALITY, "localité");
            locality.setColumns(40);
            zip = new TextField(YellowPagesEntryDef.ATTR_ZIPCODE, "NPA");
            zip.setColumns(6);
            FieldRow zipLocality = new FieldRow("Localité");
            zipLocality.addField(zip);
            zipLocality.addField(locality);
            zipLocality.setHelp("Pour saisir un code postal étranger, changer d'abord le pays");
            this.addFieldRow(zipLocality);
            country = new SelectField(YellowPagesEntryDef.ATTR_COUNTRY, false);
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


            this.addSectionHead("Divers");

            // web site
            TextField url = new TextField(YellowPagesEntryDef.ATTR_WEBSITE, null);
            this.addSingleFieldRow("Site internet", null, url);
            // TBD: vali...
            
            // Description
            TextField desc = new TextField(YellowPagesEntryDef.ATTR_DESCRIPTION, null, 3);
            desc.setColumns(60);
            this.addSingleFieldRow("Notes", "Toute information pertinente et durable concernant ce contact " +
                    "(travaux réalisés, horaires d'ouverture, menus à essayer ou éviter, etc.)", desc);
        }


    }

}
