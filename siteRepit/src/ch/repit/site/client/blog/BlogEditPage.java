package ch.repit.site.client.blog;


import ch.repit.rwt.client.Bento;
import ch.repit.rwt.client.BentoDefFactory;
import ch.repit.rwt.client.BentoStatus;
import ch.repit.rwt.client.ObjectRef;
import ch.repit.rwt.client.logs.LogManager;
import ch.repit.rwt.client.persistence.CacheEvent;
import ch.repit.rwt.client.persistence.CacheEventHandler;
import ch.repit.rwt.client.persistence.CacheManager;
import ch.repit.rwt.client.security.Action;
import ch.repit.rwt.client.security.Authorizer;
import ch.repit.rwt.client.security.SecurityManager;
import ch.repit.rwt.client.ui.form.FileTable;
import ch.repit.rwt.client.ui.Page;
import ch.repit.rwt.client.ui.form.FieldRow;
import ch.repit.rwt.client.ui.form.FileUploadField;
import ch.repit.rwt.client.ui.form.FormPage;
import ch.repit.rwt.client.ui.form.RichTextField;
import ch.repit.rwt.client.ui.form.TextField;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ButtonBase;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.Widget;
import java.util.Date;
import java.util.List;


/**
 * Edit page for a blog
 */
public class BlogEditPage extends FormPage {

    private FileTable filePage;

    private RichTextField body;
    private TextField bodyHtml;

    public BlogEditPage(BlogDef blogDef, ObjectRef blogRef, Page topPage) {
        super(blogDef.getType(), blogRef, topPage);
    }

    @Override
    protected void init() {

        super.setShowPath(true);

        // sujet
        TextField subject = new TextField(BlogDef.ATTR_SUBJECT, null);
        subject.setColumns(60);
        this.addSingleFieldRow("Sujet", null, subject);

        // body
        body = new RichTextField(BlogDef.ATTR_BODY, null);
        Authorizer auth = SecurityManager.get().getAuthorizer();
        if (auth.isAllowed(Action.MANAGE, super.getBentoDef().getType())) {
           // final CheckBox editorType = new CheckBox("édition directe");
            final RadioButton advancedEdit = new RadioButton("editeMode", "éditeur avancé");
            advancedEdit.setValue(true);
            ValueChangeHandler vch = new ValueChangeHandler() {
                public void onValueChange(ValueChangeEvent event) {
                    if (advancedEdit.getValue()) {
                        body.setVisible(true);
                        body.setIgnore(false);
                        bodyHtml.setVisible(false);
                        bodyHtml.setIgnore(true);
                        body.setText((String)bodyHtml.getValue());
                    } else {
                        body.setVisible(false);
                        body.setIgnore(true);
                        bodyHtml.setVisible(true);
                        bodyHtml.setIgnore(false);
                        bodyHtml.setText((String)body.getValue());
                    }
                }
            };
            advancedEdit.addValueChangeHandler(vch);

            RadioButton htmlEdit = new RadioButton("editeMode", "édition HTML brut");
            htmlEdit.setValue(false);
            htmlEdit.addValueChangeHandler(vch);
            HorizontalPanel radioPanel = new HorizontalPanel();
            radioPanel.add(advancedEdit);
            radioPanel.add(htmlEdit);
            radioPanel.addStyleName("rwt-formAttributePanel");
            this.addWidgetRow("Mode de saisie", "Pour editer le texte avec les options comme dans Word, ou directement en HTML", radioPanel);

            bodyHtml = new TextField(BlogDef.ATTR_BODY, null, 20);
            bodyHtml.setColumns(100);
            bodyHtml.setVisible(false);
            bodyHtml.setIgnore(true);
            FieldRow editors = new FieldRow("Contenu", body, bodyHtml);
            this.addFieldRow(editors);
            }
        else {
            this.addSingleFieldRow("Contenu", null, body);
        }

        // attachments
        FileUploadField fuf = new FileUploadField("emplacement du fichier", this);
        this.addSingleFieldRow("Ajouter un fichier", null, fuf);

        if (!isCreate()) {
            filePage = new FileTable(this);
            Widget table = filePage.getTable();
            table.addStyleName("rwt-formAttributePanel");
            super.addWidgetRow("Liste des fichiers", null, table);
        }

        super.init();
    }


    @Override
    public void setObject(Bento bento) {
        super.setObject(bento);
        if (filePage != null)
            filePage.resetBento(bento);
    }



    @Override
    protected void fillToolbarWidgets(List<Widget> leftWidgets,
                                      List<Widget> middleWidgets,
                                      List<Widget> rightWidgets) {
        super.fillToolbarWidgets(leftWidgets, middleWidgets, rightWidgets);

        // draft
        if (getObject().getStatus() == BentoStatus.DRAFT) {
            ButtonBase saveDraftButton = (ButtonBase)leftWidgets.get(0);
            saveDraftButton.setText("Enregistrer brouillon");

            Button publishButton = new Button("Publier");
            publishButton.addClickHandler(publishClickHandler);
            leftWidgets.add(1, publishButton);

            // replace trash by permanentDelete, for drafts only
            if (!isCreate()) {
                Button deleteButton = new Button("Effacer définitivement");
                deleteButton.addClickHandler(permanentDeleteClickHandler);
                leftWidgets.add(deleteButton);
                Widget toRemove = null;
                for (Widget b : leftWidgets)
                    if (b instanceof ButtonBase && ((ButtonBase)b).getText().equals("Supprimer"))
                        toRemove = b;
                leftWidgets.remove(toRemove);
            }
        }

        // active
        else if (getObject().getStatus() == BentoStatus.ACTIVE) {
            Button archiveButton = new Button("Archiver");
            archiveButton.addClickHandler(new ClickHandler() {
                public void onClick(ClickEvent arg0) {
                    getObject().setStatus(BentoStatus.ARCHIVE);
                    doSave(getObject());
                }
            });
            leftWidgets.add(archiveButton);
        }
    }

    @Override
    protected void doSaveOnSuccess(final ObjectRef objectRef) {
        if (getObject().get(BlogDef.ATTR_PUBDATE).getDate() == null) { // i.e, it is a draft...
            LogManager.get().handled("Brouillon enregistré");
            if (getObject().getId() == null)
                super.setNewlyCreatedObjectRef(objectRef);
            CacheEventHandler ceh = new CacheEventHandler() {
                public void onCacheEvent(CacheEvent event) {
                    CacheManager.get().unregisterEventHandler(this);
                    // Do this to redisplay the page in edit mode, no more in create
                    getPageNav().back();
                    getPageNav().displayPage(new BlogPage((BlogDef)BentoDefFactory.get().getDef(objectRef.getType()), objectRef, null));
                }
            };
            CacheManager.get().registerEventHandler(ceh);
            super.setUpdateExpected();
            CacheManager.get().refreshCache();
        } else
            super.doSaveOnSuccess(objectRef);
    }


    private ClickHandler publishClickHandler = new ClickHandler() {
        public void onClick(ClickEvent arg0) {

            // no dirty control, as we may publish a non modified blog

            // all before read validations, this perform save
            if (validateForm()) {
                // read the modifs
                Bento readObj = readForm();

                // validate after reading the obj
                if (validateObject(readObj)) {

                    // set publication date
                    readObj.get(BlogDef.ATTR_PUBDATE).set(new Date());
                    getObject().get(BlogDef.ATTR_PUBDATE).set(new Date()); // HACK
                    readObj.setStatus(BentoStatus.ACTIVE);

                    // delegates server call to implementation
                    doSave(readObj);
                }
            }
        }
    };

}
