package ch.repit.site.client.blog;

import ch.repit.rwt.client.Bento;
import ch.repit.rwt.client.BentoDefFactory;
import ch.repit.rwt.client.BentoStatus;
import ch.repit.rwt.client.ContentTypeFamily;
import ch.repit.rwt.client.FileDescriptor;
import ch.repit.rwt.client.ObjectRef;
import ch.repit.rwt.client.persistence.CacheManager;
import ch.repit.rwt.client.security.Action;
import ch.repit.rwt.client.security.Authorizer;
import ch.repit.rwt.client.security.SecurityManager;
import ch.repit.rwt.client.ui.BentoPage;
import ch.repit.rwt.client.ui.Dialog;
import ch.repit.rwt.client.ui.Page;
import ch.repit.rwt.client.util.Formaters;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import java.util.List;


/**
 * Display page for a blog
 */
public class BlogDisplayPage extends BentoPage {

    private Image photo;
    private HTML subjectHtml, detailsHtml, bodyHtml;
    private FlowPanel attachmentPanel;
    private Panel widget;


    /**
     * Assumes the blog exists (e.g. blogRef not null)
     */
    public BlogDisplayPage(ObjectRef blogRef, Page topPage) {
        super(blogRef.getType(), blogRef, topPage);
        setPrintable(true);
        super.setOnlyTopToolbar(true);

        widget = new VerticalPanel();

        // post details
        HorizontalPanel hp = new HorizontalPanel();
        VerticalPanel postDetails = new VerticalPanel();
        postDetails.add(subjectHtml = new HTML());
        subjectHtml.setStylePrimaryName("repit-blogDisplaySubject");
        postDetails.add(detailsHtml = new HTML());
        detailsHtml.setStylePrimaryName("repit-blogDisplayDetails");
        hp.add(photo = new Image());
       // photo.setPixelSize(40, 50);
        photo.setStylePrimaryName("repit-blogDisplayPhoto");
        hp.add(postDetails);
        widget.add(hp);

        // post
        widget.add(bodyHtml = new HTML());
        bodyHtml.setStylePrimaryName("repit-blogDisplayBody");
        bodyHtml.setWidth("95%");
        attachmentPanel = new FlowPanel();
        widget.add(attachmentPanel);
        widget.setWidth("100%");
        attachmentPanel.addStyleName("repit-blogDisplayFiles");
    }


    @Override
    protected Widget doContentlayout() {
        return widget;
    }


    @Override
    public void setObject(Bento blog) {
        super.setObject(blog);

        // subject
        subjectHtml.setHTML(blog.getDisplayValue(BlogDef.ATTR_SUBJECT));

        // details
        String details = "soumis par ";
        if (blog.get(BlogDef.ATTR_PUBDATE).getDate() == null)
            details = "pas encore publié, en cours de création par ";
        Bento author = CacheManager.get().getCachedObject(blog.getOwnerRef());
        if (author != null && author.getAttachedFiles() != null && author.getAttachedFiles().length > 0) {
            photo.setUrl(author.getAttachedFiles()[0].getPreviewUrl(author.getRef()));
        } else {
            photo.setUrl(GWT.getModuleBaseURL()+"images/anonymous.png");
            photo.setPixelSize(50, 50);
        }
        String authorDisplay = "(" + blog.getOwnerRef() + ")";
        if (author != null) {
            authorDisplay = BentoDefFactory.get().getDef(author.getType()).getCommonName(author);
        }
        details += authorDisplay;
        if (blog.get(BlogDef.ATTR_PUBDATE).getDate() != null) {
            details += " le " + Formaters.get().formatDate
                    (blog.get(BlogDef.ATTR_PUBDATE).getDate(), Formaters.DatePattern.DATE) + ".";
        }
        detailsHtml.setHTML(details);

        // body
        bodyHtml.setHTML(blog.getDisplayValue(BlogDef.ATTR_BODY));

        // files
        attachmentPanel.clear();
        if (blog.getAttachedFiles() != null) {
            for (FileDescriptor file : blog.getAttachedFiles()) {
                Widget filePrev = null;
                if (file.isImage()) {
                    filePrev = Dialog.buildImageLink(file.getViewUrl(blog.getRef()),file.getPreviewUrl(blog.getRef()));
                } else {
                    ContentTypeFamily ctf = ContentTypeFamily.getContentTypeFamily(file.getContentType());
                    String hrefHtml = file.getFileName();
                    if (ctf != null) {
                        hrefHtml = "<img src='" + ctf.getIconHref() + "'/> " + hrefHtml;
                    }
                    // else: unknown file icon...
                    Anchor l = new Anchor(hrefHtml, true, file.getViewUrl(blog.getRef()), "otherTab");
                    filePrev = l;
                }
                filePrev.setStylePrimaryName("repit-blogDisplayFile");
                attachmentPanel.add(filePrev);
            }
        }
    }


    @Override
    protected void fillToolbarWidgets(List<Widget> leftWidgets,
                                      List<Widget> middleWidgets,
                                      List<Widget> rightWidgets) {
        super.fillToolbarWidgets(leftWidgets, middleWidgets, rightWidgets);

        if (getObject().getStatus()==BentoStatus.TRASH) {
            Authorizer auth = SecurityManager.get().getAuthorizer();

            if (auth.isAllowed(Action.TRASH, super.getObject())) {
                Button untrashButton = new Button("Récupérer");
                untrashButton.addClickHandler(super.untrashClickHandler);
                leftWidgets.add(untrashButton);
            }
            if (auth.isAllowed(Action.ADMIN, super.getObject())) { // MANAGE???
                Button deleteButton = new Button("Effacer définitivement");
                deleteButton.addClickHandler(permanentDeleteClickHandler);
                leftWidgets.add(deleteButton);
            }
        }

        // for archived docs, add possibility to reactivate
        else if (getObject().getStatus() == BentoStatus.ARCHIVE) {
            Authorizer auth = SecurityManager.get().getAuthorizer();
            if (auth.isAllowed(Action.UPDATE, super.getObject())) {
                Button reacButton = new Button("Réactiver");
                reacButton.addClickHandler(new ClickHandler() {
                    public void onClick(ClickEvent arg0) {
                        doChangeState(getObject(), BentoStatus.ACTIVE, "réactivation");
                    }
                });
                leftWidgets.add(reacButton);
            }
            if (auth.isAllowed(Action.TRASH, super.getObject())) {
                Button trashButton = new Button("Supprimer");
                trashButton.addClickHandler(trashClickHandler);
                leftWidgets.add(trashButton);
            }
        }
    }



}
