/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.repit.rwt.client.ui.form;

import ch.repit.rwt.client.BentoAttribute;
import ch.repit.rwt.client.ObjectRef;
import ch.repit.rwt.client.logs.LogManager;
import ch.repit.rwt.client.persistence.CacheManager;

import ch.repit.rwt.client.ui.Dialog;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteHandler;
import com.google.gwt.user.client.ui.FormPanel.SubmitEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitHandler;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Not only the FileUpload, but also all the form around it and the submission
 * of the file.
 */
public class FileUploadField extends Field {

    public static final String UPLOAD_FILE_KEY = "rwtUploadFile";
    public static final String UPLOAD_OBJECTREF_KEY = "rwtUploadObjectRef";


    private FlexTable  uploadTable;
    private FormPanel  formWidget;
    private FileUpload fileUpload;
    private Hidden     hiddenId;

    private FormPage holdingPage;
    private Button currentSubmitButton;

    public FileUploadField(String label, FormPage holdingPage) {
        super(null, label);

        this.holdingPage = holdingPage;
        uploadTable = new FlexTable();
        addNewUploadWidget();
    }

    private void addNewUploadWidget() {

        // FileUpload widget must be in a FormPanel, see FormPanel JavaDocs
        formWidget = new FormPanel();
        formWidget.setAction("/fileUploadServlet");   // TBD: NOT VERY PORTABLE...
        formWidget.setEncoding(FormPanel.ENCODING_MULTIPART);
        formWidget.setMethod(FormPanel.METHOD_POST);
        VerticalPanel panel = new VerticalPanel();
        fileUpload = new FileUpload();
        fileUpload.setName(UPLOAD_FILE_KEY); 
        panel.add(fileUpload);
        hiddenId = new Hidden(UPLOAD_OBJECTREF_KEY);
        panel.add(hiddenId);
        HTML fileDisplay = new HTML();
        panel.add(fileDisplay);

        int newRowIndex = uploadTable.getRowCount();
        UploadHandler uploadHandler = new UploadHandler(newRowIndex, fileUpload, fileDisplay);
        formWidget.addSubmitHandler(uploadHandler);
        formWidget.addSubmitCompleteHandler(uploadHandler);
        formWidget.setWidget(panel);
        
        // add the form at the begining of the table
        uploadTable.insertRow(newRowIndex);
        uploadTable.insertCell(newRowIndex, 0);
        uploadTable.setWidget(newRowIndex, 0, formWidget);
        uploadTable.insertCell(newRowIndex, 1);
        final Button submit = new Button("Transférer");
        submit.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent arg0) {
                if (holdingPage.getObject().getRef() == null) {
                    Dialog.alert("Veuillez d'abord créer l'objet (ou un brouillon) avant d'y ajouter des fichiers");
                }
                else {
                    setObjectRef(holdingPage.getObject().getRef());
                    submit.setVisible(false);
                    formWidget.submit();
                    addNewUploadWidget();
                }
            }
        } );
        uploadTable.setWidget(newRowIndex, 1, submit);
        currentSubmitButton = submit;
    }


    @Override
    protected boolean isReadOnly() {
        return fileUpload.isVisible();
    }

    @Override
    protected void setReadOnly(boolean readOnly) {
        currentSubmitButton.setVisible(readOnly);
        fileUpload.setVisible(readOnly);
    }

    @Override
    protected void setAttribute(BentoAttribute attribute) {
        // void
    }

    @Override
    protected void readAttribute(BentoAttribute attribute) {
        // void
    }

    @Override
    public Object getValue() {
        // return widget.getFilename();
        return null;
    }

    @Override
    protected Widget getWidget() {
        if (holdingPage.isCreate())
            return new Label("Veuillez d'abord créer l'objet ou un brouillon");
        else
            return uploadTable;
    }


    
    //
    // specific methods
    //

    public void setObjectRef(ObjectRef objectRef) {
        if (objectRef != null) {
         //   this.objectRef = objectRef;
            hiddenId.setValue(objectRef.toString());
        }
    }

    @Override
    protected void setDefaultValue(Object defaultValue) {
        // does not make sense
    }


    private class UploadHandler implements SubmitHandler, SubmitCompleteHandler {

        int tableRowIndex = 0;
        FileUpload fileUpload;
        HTML fileDisplay;

        UploadHandler(int tableRowIndex, FileUpload fileUpload, HTML fileDisplay) {
            this.tableRowIndex = tableRowIndex;
            this.fileUpload = fileUpload;
            this.fileDisplay = fileDisplay;
        }


        public void onSubmit(SubmitEvent event) {
            // for instance: event.cancel();
            LogManager.get().handling("Envoi du fichier en cours");
            // TBD: set wait icon instead of button
            uploadTable.setHTML(tableRowIndex, 1,
                    "<img src='"+GWT.getModuleBaseURL()+"icons/loading.gif' width='20px'/> transmission en cours");
            fileUpload.setVisible(false); // ATTENTION: do not remove from table !!!
            fileDisplay.setHTML(fileUpload.getFilename());
        }

        
        public void onSubmitComplete(SubmitCompleteEvent event) {
            LogManager.get().handled("Envoi du fichier terminé (" + event.getResults() +")");
            if ("ok".equals(event.getResults())) {
                LogManager.get().handled("Envoi du fichier terminé");
                uploadTable.setHTML(tableRowIndex, 1,
                        "<img src='"+GWT.getModuleBaseURL()+"icons/alerts/success_medium.gif'/> fichier transmis");
                holdingPage.setUpdateExpected();
                CacheManager.get().refreshCache();
            } else {
                LogManager.get().warning("Echec de l'envoi du fichier: " + event.getResults());
                uploadTable.setHTML(tableRowIndex, 1,
                        "<img src='"+GWT.getModuleBaseURL()+"icons/alerts/error_medium.gif'/> erreur: " + event.getResults());
            }
        }

    }

}
