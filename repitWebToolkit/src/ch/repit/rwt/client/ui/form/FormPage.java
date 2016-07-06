/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.repit.rwt.client.ui.form;

import ch.repit.rwt.client.AttributeDef;
import ch.repit.rwt.client.ui.Page;
import ch.repit.rwt.client.logs.LogManager;
import ch.repit.rwt.client.persistence.CacheManager;
import ch.repit.rwt.client.Bento;
import ch.repit.rwt.client.BentoAttribute;
import ch.repit.rwt.client.BentoStatus;
import ch.repit.rwt.client.ObjectRef;
import ch.repit.rwt.client.persistence.ObjectNotFoundException;
import ch.repit.rwt.client.persistence.ObjectStaleException;
import ch.repit.rwt.client.persistence.UniqueKeyViolationException;
import ch.repit.rwt.client.persistence.ValidationException;
import ch.repit.rwt.client.security.Action;
import ch.repit.rwt.client.security.AuthorizationException;
import ch.repit.rwt.client.security.Authorizer;
import ch.repit.rwt.client.security.SecurityManager;
import ch.repit.rwt.client.ui.BentoPage;
import ch.repit.rwt.client.ui.Dialog;
import ch.repit.rwt.client.ui.form.Field.FieldChangeHandler;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.StatusCodeException;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ButtonBase;
import com.google.gwt.user.client.ui.DecoratedPopupPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;
import java.util.ArrayList;
import java.util.List;
 

/**
 * Base class for form pages, with Save, Cancel and Delete buttons pre-implemented.
 */
public abstract class FormPage extends BentoPage {

    public static final int DEFAULT_VISIBLE_LENGTH = 40;

    private FlexTable tableForm;
    private int line=0;
    private int col=0;
    private boolean showTitle = true;
    private boolean showHelp = true;
    private List<ButtonBase> saveButtons = new ArrayList();

    // Create a basic popup widget
    private DecoratedPopupPanel simplePopup = new DecoratedPopupPanel(true);
    
    
    List<FieldRow> formFieldRows = new ArrayList<FieldRow>();

    public FormPage(String objectType, ObjectRef objectRef, Page topPage)  {
        super(objectType, objectRef, topPage);
        tableForm = new FlexTable();
        tableForm.setWidth("100%");
        tableForm.setCellSpacing(0);
        simplePopup.setWidth("150px");
    }

    @Override
    protected void init() {
        super.init();
    }

    protected void showTitleAndHelp(boolean title, boolean help) {
        this.showTitle = title;
        this.showHelp = help;
    }

    @Override
    protected Widget doContentlayout() {
        return tableForm;
    }

    @Override
    protected void doUnLayout() {
        super.doUnLayout();
        saveButtons.clear();
    }

    public int addSingleFieldRow(String rowTitle, String help, Field field) {
        FieldRow fieldRow = new FieldRow(rowTitle);
        fieldRow.setHelp(help);
        fieldRow.addField(field);
        return addFieldRow(fieldRow);
    }

    public int addFieldRow(FieldRow fieldRow) {
        formFieldRows.add(fieldRow);
        Authorizer auth = SecurityManager.get().getAuthorizer();
        // sets the change hadler to activate save button (if needed, move to displayObject)
        for (Field field : fieldRow.getFields()) {
            // defines if the field is mandatory, readonly, etc based on principal and AttributeDef
            String attrName = field.getAttributeName();
            if (attrName != null && getBentoDef().getAttributeDef(attrName) != null) {
                AttributeDef attrDef = getBentoDef().getAttributeDef(attrName);
                field.setMandatory(attrDef.isMandatory());
                if (isCreate() ||    // for create we allow all fields to be set (CHECK!!!)
                        auth.isAllowed(attrDef.getEditActionThreshold(), getObject())) {
                    field.setReadOnly(attrDef.isReadonly());
                    field.addChangeHandler(new FieldChangeHandler() {
                        public void onChange() {
                            // this is if the user wants to leave the page without saving or canceling
                            getPageNav().setPageEdited(true);
                        }
                    });
                } else {
                    field.setReadOnly(true);
                }
                field.setDefaultValue(attrDef.getDefaultValue());
            }
        }
        return addWidgetRow(fieldRow.getTitle(), fieldRow.getHelp(), fieldRow.doLayout());
    }



    public int addSectionHead(String title, String help) {
        col = 0;
        line++;

        appendHelp(help);
        tableForm.getCellFormatter().addStyleName(line, 0, "rwt-formSectionHead");
        col++;

        tableForm.setText(line, 1, title);
        tableForm.getFlexCellFormatter().setColSpan(line, 1, 2 );
        tableForm.getCellFormatter().addStyleName(line, 1, "rwt-formSectionHead");
        tableForm.getRowFormatter().addStyleName(line, "rwt-formRow" + (line%2==0?"Even":"Odd"));
        return line;
    }
    
    public int addSectionHead(String title) {
        return addSectionHead(title,null);
    }

    public int addWidgetRow(String rowTitle, final String help, Widget widget) {
        col = 0;
        line++;

        // help link
        appendHelp(help);
        col++;
        
        // title
        tableForm.setText(line, col, rowTitle);
        tableForm.getCellFormatter().addStyleName(line, col, "rwt-formRowTitle");
        col++;

        // content
        tableForm.setWidget(line, col, widget);
        tableForm.getCellFormatter().addStyleName(line, col, "rwt-formAttributesPanel"); 
        col++;

        tableForm.getRowFormatter().addStyleName(line, "rwt-formRow" + (line%2==0?"Even":"Odd"));

        return line;
    }


    @Override
    public void setObject(Bento bento) {
        Bento oldBento = super.getPreviousObject();
        super.setObject(bento);
        if (bento != null) {
            for (FieldRow row : formFieldRows) {
                for (Field field : row.getFields()) {
                    String attrName = field.getAttributeName();
                    if (attrName != null) {
                        BentoAttribute attr = bento.get(attrName);
                        if (attr != null)
                            // compare with previous object (if exists) and sets only if different (this prevents
                            // unnecessary losses of info in edited field when attribute is not actually changed
                            if (oldBento == null || !attr.equals(oldBento.get(attrName))) {
                                field.setAttribute(attr);
                            }
                    }
                }
            }
            if (!isCreate() && 
                    (bento.getStatus()==BentoStatus.ARCHIVE || bento.getStatus()==BentoStatus.TRASH) ) {
                setReadOnly(true);
            }
        }

    }


    public void setVisible(int row, boolean visible) {
        tableForm.getRowFormatter().setVisible(row, visible);
    }

    
    /**
     * Allows to set a form globally readonly
     * @param readOnly
     */
    public void setReadOnly(boolean readOnly) {
        for (FieldRow row : formFieldRows) {
            for (Field field : row.getFields()) {
                field.setReadOnly(readOnly);
            }
        }
    }

    /**
     * Called by pageNav the first time the page is edited
     */
    public void setPageEdited(){
        for (ButtonBase b : saveButtons)
            b.setEnabled(true);
    }

    /**
     * Return a copy of the object, to be able to perform validation and cancellation
     */
    protected Bento readForm() {
        Bento result = null;
        if (getObject() != null) 
            result = super.getBentoDef().createBento(getObject());
        else
            result = super.getBentoDef().createBento();
        for (FieldRow row : formFieldRows) {
            for (Field field : row.getFields()) {
                if (!field.isIgnored()
                        && (isCreate() || !field.isReadOnly()) ) {
                    String attrName = field.getAttributeName();
                    BentoAttribute attr = result.get(attrName);
                    if (attr != null) {
                        field.readAttribute(attr);
                    }
                }
            }
        }
        return result;
    }

    
    /**
     * Can be both a create or an update, depending on the implementation
     * @param object the object given by displayObject, with modifications from the user
     */
    public void doSave(final Bento bento) {
        LogManager.get().handling("Enregistrement en cours...");
        AsyncCallback<ObjectRef> callback = new AsyncCallback<ObjectRef>() {
            public void onFailure(Throwable caught) {
                if (caught instanceof StatusCodeException) {
                    LogManager.get().warningConnectionLost();
                    LogManager.get().warning("Impossible de contacter le serveur. Veuillez vérifier votre connexion Internet et recommencer");
                } else if (caught instanceof UniqueKeyViolationException) {
                    LogManager.get().warning("L'identifiant unique spécifié existe déjà. Veuillez en choisir un autre", caught);
                } else if (caught instanceof ValidationException) {
                    LogManager.get().warning("Erreur de validation: " + caught.getMessage(), caught);
                } else if (caught instanceof ObjectNotFoundException) {
                    LogManager.get().warning("L'objet a été supprimé pendant que vous l'éditiez", caught);
                    CacheManager.get().refreshCache();
                    getPageNav().back();
                } else if (caught instanceof ObjectStaleException) {
                    LogManager.get().warning("La mise à jour a échoué, car l'objet a été modifié pendant que vous l'éditiez. Veuillez recommencer vos modifications", caught);
                    CacheManager.get().refreshCache();
                    getPageNav().back();
                } else if (caught instanceof AuthorizationException) {
                    LogManager.get().warning("Vous n'avez pas les droits suffisants pour effectuer cette opération", caught);
                    // could reauthenticate...
                    getPageNav().back();
                } else {
                    LogManager.get().error("Echec de l'enregistrement de l'objet pour une raison inconnue!", caught);
                }
            }
            public void onSuccess(ObjectRef objectRef) {
                LogManager.get().infoConnectionOk();
                setUpdateExpected();
                doSaveOnSuccess(objectRef);
            }
        };
        if (bento.getRef() == null || (bento.getRef() != null && bento.getRef().getId() == null)) {
            // = isCreate, but also if bento has nothing to do with this obj...
            CacheManager.get().getPersistenceService().createBento(bento, callback);
        } else
            CacheManager.get().getPersistenceService().updateBento(bento, callback);
    }

    
    protected void doSaveOnSuccess(ObjectRef objectRef) {
        LogManager.get().handled("Enregistrement terminé.");
        CacheManager.get().refreshCache();
        getPageNav().back();
    }


    /**
     * Refresh the edited object. called when cancel is pushed or any other navigation link clicked
     */
    public void doCancelEdition() {
        setObject(getObject()); // refresh the modified fields with original value
        // TBD: now (aout 30) this will not work anymore, as bento did not change...
    }

    /**
     * called when saved, before reading the form
     */
    protected void validateBeforeRead(List<String> validationErrors) { }

    /**
     * called when saved, after reading the form, but before setting the object
     */
    protected void validateAfterRead(Bento dto, List<String> validationErrors) {  }

    
    @Override
    protected void fillToolbarWidgets(List<Widget> leftWidgets,
                                      List<Widget> middleWidgets,
                                      List<Widget> rightWidgets)
    {
        super.fillToolbarWidgets(leftWidgets, middleWidgets, rightWidgets);
        Authorizer auth = SecurityManager.get().getAuthorizer();

        if (super.getObject() != null
                && (isCreate()
                    || super.getObject().getStatus() != BentoStatus.TRASH )) {
            if (auth.isAllowed(isCreate()?Action.CREATE:Action.UPDATE, super.getObject())) {
                if (getObject().getStatus() != BentoStatus.ARCHIVE) {
                    Button saveButton = new Button(isCreate()?"Créer":"Enregistrer");
                    saveButton.addClickHandler(saveClickHandler);
                    if (!isCreate()) {
                        saveButton.setEnabled(false);
                        saveButtons.add(saveButton);
                    }
                    leftWidgets.add(saveButton);
                }
                // added the condition, as the cancel button is not really interesting...
                if (isCreate()) {
                    Button cancelButton = new Button("Annuler");
                    cancelButton.addClickHandler(cancelClickHandler);
                    leftWidgets.add(cancelButton);
                }
            }
            if (!isCreate() && auth.isAllowed(Action.TRASH, super.getObject())) {
                Button trashButton = new Button("Supprimer");
                trashButton.addClickHandler(trashClickHandler);
                leftWidgets.add(trashButton);
            }
            // TBD: add Réactiver button and action if object is archived
        } else {
            if (auth.isAllowed(Action.TRASH, super.getObject())) {
                Button untrashButton = new Button("Récupérer");
                untrashButton.addClickHandler(super.untrashClickHandler);
                leftWidgets.add(untrashButton);
            }
            if (auth.isAllowed(Action.ADMIN, super.getObject())) {
                Button deleteButton = new Button("Effacer définitivement");
                deleteButton.addClickHandler(permanentDeleteClickHandler);
                leftWidgets.add(deleteButton);
            }
        }
    }



    protected boolean validateForm() {
        List<String> validationErrors = new ArrayList<String>();

        // validate all fields
        for (FieldRow row : formFieldRows) {
            for (Field field : row.getFields()) {
                field.validate(validationErrors, row.getTitle());
            }
        }

        // validate before read
        if (validationErrors.isEmpty()) {
            validateBeforeRead(validationErrors);
        }

        // display errors
        if (!validationErrors.isEmpty()) {
            String errMsg = "Erreur(s) de validation simple:<ul>";
            for (String err : validationErrors)
                errMsg += "<li>" + err;
            LogManager.get().warning(errMsg + "</ul>");
            return false;
        }
        return true;
    }


    protected boolean validateObject(Bento dto) {
        List<String> validationErrors = new ArrayList<String>();

        // validate after read
        validateAfterRead(dto, validationErrors);

        // display errors
        if (!validationErrors.isEmpty()) {
            String errMsg = "Erreur(s) de validation:<ul>";
            for (String err : validationErrors)
                errMsg += "<li>" + err;
            LogManager.get().warning(errMsg + "</ul>");
            return false;
        }
        return true;
    }

    
    protected ClickHandler saveClickHandler = new ClickHandler() {
        public void onClick(ClickEvent arg0) {
            // only does something if the object is dirty
            if (isCreate() || getPageNav().isPageEdited()) {
                
                // all before read validations, this perform save
                if (validateForm()) {
                    // read the modifs
                    Bento readObj = readForm();
                  //  readForm(getObject());
                    // validate after reading the obj
                    if (validateObject(readObj)) {
                        
                        // delegates server call to implementation
                        doSave(readObj);
                    }
                }
            }
            else LogManager.get().info("Aucunes modifications, pas besoin d'enregistrer");
        }
    };


    private ClickHandler cancelClickHandler = new ClickHandler() {
        public void onClick(ClickEvent arg0) {
            getPageNav().confirmLeavePage(new Dialog.DialogResponseHandler() {
                public void onYes() {
                    getPageNav().back();
                }
                public void onNo() {}
            });
        }
    };


    protected void setNewlyCreatedObjectRef(ObjectRef objectRef) {
        this.objectRef = objectRef;
    }

    private void appendHelp(final String helpText) {
        if (helpText!=null && helpText.length()>0) {
            Anchor helpLink = new Anchor("<img src='" + GWT.getModuleBaseURL() + "icons/help.gif'/>", true);
            helpLink.addClickHandler(new ClickHandler() {
                public void onClick(ClickEvent event) {
                    // Reposition the popup relative to the button
                    Widget source = (Widget) event.getSource();
                    int left = source.getAbsoluteLeft() + 10;
                    int top = source.getAbsoluteTop() + 10;
                    simplePopup.setPopupPosition(left, top);
                    simplePopup.setWidget(new HTML(helpText));

                    // Show the popup
                    simplePopup.show();
                }
            });
            tableForm.setWidget(line, col, helpLink);
            tableForm.getCellFormatter().addStyleName(line, col, "rwt-formRowHelpLink");
        }
    }

}
