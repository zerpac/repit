/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.repit.rwt.client.audit;

import ch.repit.rwt.client.Bento;
import ch.repit.rwt.client.BentoDefFactory;
import ch.repit.rwt.client.ObjectRef;
import ch.repit.rwt.client.audit.AuditLogDTO.AuditableAction;
import ch.repit.rwt.client.logs.LogManager;
import ch.repit.rwt.client.persistence.CacheManager;
import ch.repit.rwt.client.ui.ListPage;
import ch.repit.rwt.client.ui.Page;
import ch.repit.rwt.client.util.Formaters;
import ch.repit.rwt.client.util.Formaters.DatePattern;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.StatusCodeException;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ButtonBase;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Widget;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 *
 * @author tc149752
 */
public class AuditListPage extends ListPage<AuditLogDTO> {

    private static final String DATE_COL = "eventDate";
    private static final String AUTHOR_COL = "author";
    private static final String ACTION_COL = "action";
    private static final String OBJECT_COL = "object";
    private static final String ATTRIBUTE_COL = "modifiedAttributes";

    private List<Bento> auditQuery;
    
    public AuditListPage(Page topPage, Bento... auditQuery) {
        super(topPage);

        this.auditQuery = Arrays.asList(auditQuery);

        setPrintable(true);
        
        super.setTitle("Journal d'audit");

        super.addColumn("Date", DATE_COL, true);
        super.addColumn("Auteur", AUTHOR_COL, true);
        super.addColumn("Action", ACTION_COL, true);
        super.addColumn("Type", "_objectType", true);
        super.addColumn("Objet", OBJECT_COL, true);
        super.addColumn("Modifications", ATTRIBUTE_COL, false);
        setSortColumn(DATE_COL, false);
    }


    @Override
    protected Widget doContentlayout() {
        runQuery();
        return super.doContentlayout();
    }

    @Override
    protected void fillToolbarWidgets(List<Widget> leftWidgets,
                                      List<Widget> middleWidgets,
                                      List<Widget> rightWidgets) {
        super.fillToolbarWidgets(leftWidgets, middleWidgets, rightWidgets);
        Button fetchLogs = new Button("Relancer requête", new ClickHandler() {
            public void onClick(ClickEvent arg0) {
                runQuery();
            }
        } );
        leftWidgets.add(fetchLogs);
    }


    public void runQuery() {
        LogManager.get().handling("Requête de logs d'audit lancée");
        // launch request
        AuditQueryServiceAsync querySrv = GWT.create(AuditQueryService.class);
        final List<AuditLogDTO> fullLogsList = new ArrayList();
        for (Bento query : auditQuery) {
            querySrv.listAuditLogs(query, new AsyncCallback<List<AuditLogDTO>>() {
                public void onFailure(Throwable caught) {
                    if (caught instanceof StatusCodeException) {
                        LogManager.get().warningConnectionLost();
                        LogManager.get().warning("Impossible de contacter le serveur. Veuillez vérifier votre connexion Internet et recommencer");
                    } else
                        LogManager.get().error("Echec de la requête de logs d'audit", caught);
                }
                public void onSuccess(List<AuditLogDTO> logsList) {
                    fullLogsList.addAll(logsList);
                    resetData(fullLogsList);
                    LogManager.get().handled("Requête de logs d'audit terminée");
                }
            } );
        }
    }


    @Override
    protected boolean formatObject(AuditLogDTO auditLog, Map formatedValue) {
        if (auditLog == null)
            return false;

        BentoDefFactory fac = BentoDefFactory.get();
        formatedValue.put(DATE_COL, Formaters.get().formatDate(auditLog.getEventDate(), DatePattern.FULL));
        Bento author = CacheManager.get().getCachedObject(auditLog.getAuthor());
        String authorDisplay = "(" + auditLog.getAuthor() + ")";
        if (author != null) 
            authorDisplay = fac.getDef(author.getType()).getCommonName(author);
        formatedValue.put(AUTHOR_COL, authorDisplay);
        formatedValue.put(ACTION_COL, auditLog.getAction().name());
        if (auditLog.getObject() != null) {
            ObjectRef oref = auditLog.getObject();
            Bento obj = CacheManager.get().getCachedObject(auditLog.getObject());
            if (obj != null) {
                formatedValue.put(OBJECT_COL, fac.getDef(obj.getType()).getCommonName(obj) );
            } else {
                formatedValue.put(OBJECT_COL, "(" + oref.toString() + ")");
            }
            formatedValue.put("_objectType", oref.getType());
        }
        if (auditLog.getModifiedAttributes() != null && !auditLog.getModifiedAttributes().isEmpty()) {
            boolean hideOld = (auditLog.getAction()==AuditableAction.CREATE
                    || auditLog.getAction()==AuditableAction.LOGIN
                    || auditLog.getAction()==AuditableAction.COMMENT);
            int i=0;
            FlexTable attrTable = new FlexTable();
            attrTable.setWidth("100%");
            HTML attrLabel = new HTML("attribut");
            attrLabel.addStyleName("repit-formFieldLabel");
            attrTable.setWidget(0, i++, attrLabel);
            if (!hideOld) {
                HTML oldLabel = new HTML("ancien");
                oldLabel.addStyleName("repit-formFieldLabel");
                attrTable.setWidget(0, i++, oldLabel);
            }
            HTML newLabel = new HTML(hideOld?"valeur":"nouveau");
            newLabel.addStyleName("repit-formFieldLabel");
            attrTable.setWidget(0, i++, newLabel);
            int row=1;
            for (AuditLogAttributeDTO audit : auditLog.getModifiedAttributes()) {
                int column=0;
                row++;
                attrTable.setText(row, column, audit.getAttributeName());
                attrTable.getCellFormatter().addStyleName(row, column++, "rwt-list-cellMultiLine");
                if (!hideOld) {
                    attrTable.setText(row, column, audit.getOldValue());
                    attrTable.getCellFormatter().addStyleName(row, column++, "rwt-list-cellMultiLine");
                }
                attrTable.setText(row, column, audit.getNewValue());
                attrTable.getCellFormatter().addStyleName(row, column++, "rwt-list-cellMultiLine");
            }
            formatedValue.put(ATTRIBUTE_COL, attrTable);
        }
            
        return true;
    }
    

    @Override
    protected void onRowClicked(AuditLogDTO data, String columnsAttributeName) { }  // rows not clickable


    @Override
    protected int sortCompare(AuditLogDTO o1, AuditLogDTO o2, String sortAttribute, boolean ascending) {

        int inverse = ascending?1:-1;

        if (sortAttribute.equals(DATE_COL))
            return ( o1.getEventDate().compareTo(o2.getEventDate()) * inverse);
        if (sortAttribute.equals("_objectType"))
            return ( o1.getObject().getType().compareTo(o2.getObject().getType()) * inverse);
        if (sortAttribute.equals(AUTHOR_COL))
            return ( o1.getAuthor().getId().compareTo(o2.getAuthor().getId()) * inverse);  // will only group...
        if (sortAttribute.equals(OBJECT_COL))
            return ( o1.getObject().getId().compareTo(o2.getObject().getId()) * inverse);  // will only group...
        if (sortAttribute.equals(ACTION_COL))
            return ( o1.getAction().compareTo(o2.getAction()) * inverse);

        return 0;
    }

}
