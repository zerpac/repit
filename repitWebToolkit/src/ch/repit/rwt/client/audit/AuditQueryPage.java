/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.repit.rwt.client.audit;

import ch.repit.rwt.client.Bento;
import ch.repit.rwt.client.BentoDef;
import ch.repit.rwt.client.BentoDefFactory;
import ch.repit.rwt.client.audit.AuditLogDTO.AuditableAction;
import ch.repit.rwt.client.logs.LogManager;
import ch.repit.rwt.client.persistence.CacheManager;
import ch.repit.rwt.client.security.SecurityManager;
import ch.repit.rwt.client.security.Action;
import ch.repit.rwt.client.ui.Dialog;
import ch.repit.rwt.client.ui.Page;
import ch.repit.rwt.client.ui.form.DateField;
import ch.repit.rwt.client.ui.form.FieldRow;
import ch.repit.rwt.client.ui.form.FormPage;
import ch.repit.rwt.client.ui.form.IntegerField;
import ch.repit.rwt.client.ui.form.SelectField;
import ch.repit.rwt.client.user.UserDef;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Widget;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author tc149752
 */
public class AuditQueryPage extends FormPage {

    public AuditQueryPage(Page topPage)
    {
        super(AuditQueryDef.TYPE, null, topPage);
    }


    @Override
    protected void init() {
        this.addSectionHead("Critères de recherche de logs d'audit");

        SelectField action = new SelectField(AuditQueryDef.ATTR_ACTION, false);
        TreeMap<String,String> actionsMap = new TreeMap<String,String>();
        actionsMap.put("-- non spécifié --", "");       // TBD : should be setable as default!
        for (AuditableAction a : AuditableAction.values())
            actionsMap.put(a.name(), a.name());
        action.setValueMap(actionsMap);
        this.addSingleFieldRow("Type d'action", "", action);

        SelectField objectType = new SelectField(AuditQueryDef.ATTR_OBJECTTYPE, false);
        TreeMap<String,String> objectTypeMap = new TreeMap<String,String>();
        objectTypeMap.put("-- non spécifié --", "");    // TBD : should be setable as default!
        for (String a : BentoDefFactory.get().getTypes())
            objectTypeMap.put(a, a);
        objectType.setValueMap(objectTypeMap);
        this.addSingleFieldRow("Type d'objet", "", objectType);

        // TBD: could add a dynamic Select box displayed and filled once type is selected...

        SelectField author = new SelectField(AuditQueryDef.ATTR_AUTHOR, false);
        TreeMap<String,String> authorMap = new TreeMap<String,String>();
        authorMap.put("-- non spécifié --", "");        // TBD : should be setable as default!
        BentoDef userDef = BentoDefFactory.get().getDef(UserDef.TYPE);
        for (Bento a : CacheManager.get().getCachedObjects(UserDef.TYPE))
            authorMap.put(userDef.getCommonName(a), a.getRef().toString());
        author.setValueMap(authorMap);
        this.addSingleFieldRow("Auteur", "", author);

        IntegerField fromDays = new IntegerField(AuditQueryDef.ATTR_FROMDAYS, "moins de");
        IntegerField toDays = new IntegerField(AuditQueryDef.ATTR_TODAYS, "plus de");
        this.addFieldRow(new FieldRow("Age des logs en jours", fromDays, toDays));

        DateField fromDate = new DateField(AuditQueryDef.ATTR_FROMDATE, "plus récent que");
        DateField toDate = new DateField(AuditQueryDef.ATTR_TODATE, "plus ancien que");
        this.addFieldRow(new FieldRow("Plage de date de la recherche", fromDate, toDate));
        // TBD: add validator for date to > from (who cares...)

        IntegerField querySizeLimit = new IntegerField(AuditQueryDef.ATTR_SIZELIMIT, "maximum");
        this.addSingleFieldRow("Limiter le nombre de résultats", "seuls les plus récents seront retournés", querySizeLimit);

        super.init();
    }


    // override default buttons
    @Override
    protected void fillToolbarWidgets(List<Widget> leftWidgets,
                                      List<Widget> middleWidgets,
                                      List<Widget> rightWidgets) {
        // dont call super, we dont need save, etc
        Button executeQuery = new Button("Lancer requête", new ClickHandler() {
            public void onClick(ClickEvent arg0) {
                if (validateForm()) {
                    // read the modifs
                    setObject(readForm());  // no secondary validation
                    if (getObject() != null) {
                        getPageNav().displayPage(new AuditListPage(null, getObject()));
                    } else {
                        LogManager.get().error("Requête de logs d'audit est null... ");
                    }
                }
            }
        } );
        leftWidgets.add(executeQuery);

        if (SecurityManager.get().getAuthorizer().isAllowed(Action.ADMIN, "*")) {
            Button deleteLogsQuery = new Button("Supprimer les logs", new ClickHandler() {
                public void onClick(ClickEvent arg0) {
                    // only does something if the user is dirty
                    if (getPageNav().isPageEdited()) {
                        if (validateForm()) {
                            Dialog.confirm("Voulez-vous vraiment supprimer les logs? Il n'y pas de retour en arrière possible!",
                                new Dialog.DialogResponseHandler() {
                                    public void onYes() {
                                        // read the modifs
                                        setObject(readForm());  // no secondary validation
                                        if (getObject() != null) {
                                            LogManager.get().handling("Suppression des logs d'audit lancée");
                                            // launch request
                                            AuditQueryServiceAsync querySrv = GWT.create(AuditQueryService.class);
                                            querySrv.deleteAuditLogs(getObject(), new AsyncCallback<Void>() {
                                                public void onFailure(Throwable arg0) {
                                                    LogManager.get().error("Echec de la suppression des logs d'audit", arg0);
                                                }
                                                public void onSuccess(Void arg0) {
                                                    LogManager.get().handled("Suppression des logs d'audit terminée");
                                                }
                                            } );
                                        } else {
                                            LogManager.get().error("Requête de logs d'audit est null... ");
                                        }
                                    }
                                    public void onNo() {}
                                });
                        }
                    } else {
                        LogManager.get().info("Prévention des accidents, veuillez modiifer au moins un champ pour supprimer tous les logs");
                    }
                }
            } );
            leftWidgets.add(deleteLogsQuery);
        }
    }

}
