package ch.repit.rwt.client.ui;

import ch.repit.rwt.client.logs.LogManager;
import ch.repit.rwt.client.persistence.CacheEvent;
import ch.repit.rwt.client.persistence.CacheEventHandler;
import ch.repit.rwt.client.persistence.CacheManager;
import ch.repit.rwt.client.Bento;
import ch.repit.rwt.client.BentoComment;
import ch.repit.rwt.client.BentoDef;
import ch.repit.rwt.client.BentoDefFactory;
import ch.repit.rwt.client.BentoStatus;
import ch.repit.rwt.client.ObjectRef;
import ch.repit.rwt.client.persistence.ObjectNotFoundException;
import ch.repit.rwt.client.persistence.ValidationException;
import ch.repit.rwt.client.security.Action;
import ch.repit.rwt.client.security.AuthorizationException;
import ch.repit.rwt.client.security.SecurityManager;
import ch.repit.rwt.client.ui.Dialog.DialogResponseHandler;
import ch.repit.rwt.client.util.Formaters;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.StatusCodeException;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;
import java.util.Date;


/**
 * This class is to be used for pages that include user comments (and object history)
 */
public abstract class BentoPage extends Page implements CacheEventHandler {

    private Bento object;
    private Bento previousObject;
    protected ObjectRef objectRef;
    private BentoDef bentoDef;
    private VerticalPanel commentsPanel;
    private VerticalPanel commentsInputPanel;
    boolean firstDisplay = true;

    protected boolean isUpdateExpected = false;
    
    // Object sub-page can only have an ObjectPage as parent
    protected BentoPage(String objectType, ObjectRef objectRef, Page topPage) {
        super(topPage);
        bentoDef = BentoDefFactory.get().getDef(objectType);
        this.objectRef = objectRef;
        commentsPanel = new VerticalPanel();
        commentsPanel.setWidth("70%");
        commentsInputPanel = new VerticalPanel();
        commentsInputPanel.setWidth("70%");
    }


    @Override
    protected void init() {
        setObject(getObject());
    }


    public void setUpdateExpected() {
        isUpdateExpected = true;
        if (getTopPage() != null && getTopPage() instanceof BentoPage)
            ((BentoPage)getTopPage()).setUpdateExpected();
    }
    
    @Override
    protected VerticalPanel doLayout() {
        VerticalPanel panel = super.doLayout();
        panel.add(commentsPanel);
        panel.setCellHorizontalAlignment(commentsPanel, VerticalPanel.ALIGN_CENTER);
        panel.add(commentsInputPanel);
        panel.setCellHorizontalAlignment(commentsInputPanel, VerticalPanel.ALIGN_CENTER);
        CacheManager.get().registerEventHandler(this);
        return panel;
    }
    

    @Override
    protected void doUnLayout() {
        // unregister the event handler
        CacheManager.get().unregisterEventHandler(this);
        super.doUnLayout();
    }
    

    public BentoDef getBentoDef() {
        return bentoDef;
    }

    public Bento getObject() {
        if (object == null && objectRef == null)  {
            object = bentoDef.createBento();
        } else if (objectRef != null) {
            Bento cacheObj = CacheManager.get().getCachedObject(objectRef);
            if (cacheObj != null)
                object = cacheObj;
        }
        return object;
    }
    public Bento getPreviousObject() {
        return previousObject;
    }

    public boolean isCreate() {
        return (objectRef == null || (object != null && object.getId() == null));
    }
    
    public void setObject(final Bento object) {

        this.previousObject = this.object;
        this.object = object;

        // when saving newly created drafts
        if (object != null) 
             objectRef = object.getRef();

        // sets the comments only if parent page does not do it already
        if ( (object != null && !isCreate()   // i.e. object is already created
                && SecurityManager.get().getAuthorizer().isAllowed(Action.COMMENT, object)) &&
                (getTopPage() == null || !(getTopPage() instanceof BentoPage) ||
                !((BentoPage)getTopPage()).getBentoDef().getType().equals(object.getType()) ) 
                && object.getStatus()!=BentoStatus.TRASH
                && object.getStatus()!=BentoStatus.ARCHIVE )
        {
            // empties the comment table
            commentsPanel.clear();
            if (object.getComments() != null) {

                boolean canDeleteComments = object.getStatus()!=BentoStatus.TRASH 
                        && object.getStatus()!=BentoStatus.ARCHIVE
                        && SecurityManager.get().getAuthorizer().isAllowed(Action.UPDATE, object);
                        // OR Manage right (in case we have to delete comments from trashed obj ???)

                for (final BentoComment comment : object.getComments()) {
                    if (comment != null) {
                        HorizontalPanel hp = new HorizontalPanel();
                        hp.setWidth("100%");
                        hp.setStylePrimaryName("rwt-pageComment");

                        // put commenter photo
                        Bento author = CacheManager.get().getCachedObject(new ObjectRef(comment.getCommenterName()));
                        String authorDisplay = "(" + comment.getCommenterName() + ")";
                        Image photo;
                        if (author != null && author.getAttachedFiles() != null && author.getAttachedFiles().length > 0) {
                            photo = new Image(author.getAttachedFiles()[0].getPreviewUrl(author.getRef()));
                        } else {
                            // TBD: default
                            photo = new Image(GWT.getModuleBaseURL()+"rwtimg/anonymous.png");
                        }
                       // photo.setPixelSize(40, 50);
                        photo.setStylePrimaryName("rwt-userPhoto");
                        if (author != null) {
                            authorDisplay = BentoDefFactory.get().getDef(author.getType()).getCommonName(author);
                        }
                        hp.add(photo);
                        hp.setCellWidth(photo, "60px");

                        // add text
                        VerticalPanel vp = new VerticalPanel();
                        vp.setWidth("100%");
                        vp.setHeight("100%");
                        vp.add(new HTML("<font color='olive'>" + authorDisplay + "</font>&nbsp;&nbsp;" 
                                + comment.getCommentText().replaceAll("\n", "<br/>")));
                        HorizontalPanel hp2 = new HorizontalPanel();
                        hp2.setWidth("100%");
                        hp2.add(new HTML("<font color='olive'>" + Formaters.get().formatDate(comment.getCommentDate()) + "</font>"));

                        // add delete button if enough rights
                        if (canDeleteComments) {
                            final PushButton deleteComment = new PushButton(new Image(GWT.getModuleBaseURL()+"icons/delete.png"));
                            deleteComment.setStylePrimaryName("rwt-pushButton");
                            deleteComment.addClickHandler(new ClickHandler() {
                                public void onClick(ClickEvent arg0) {
                                    // confirm delete
                                    Dialog.confirm("Voulez-vous vraiment supprimer ce commentaire?", new DialogResponseHandler() {
                                        public void onYes() {
                                            deleteComment.setEnabled(false);
                                            AsyncCallback<Void> callback = new AsyncCallback<Void>() {
                                                public void onFailure(Throwable caught) {
                                                    if (caught instanceof StatusCodeException) {
                                                        LogManager.get().warningConnectionLost();
                                                        LogManager.get().warning("Impossible de contacter le serveur. Veuillez vérifier votre connexion Internet et recommencer");
                                                    } else
                                                        LogManager.get().warning("Echec de la suppression du commentaire", caught);
                                                    deleteComment.setEnabled(true);
                                                }
                                                public void onSuccess(Void arg0) {
                                                    LogManager.get().handled("Commentaire supprimé");
                                                    // stale cache
                                                    setUpdateExpected();
                                                    CacheManager.get().refreshCache();
                                                }
                                            };
                                            CacheManager.get().getPersistenceService().deleteComment(object.getRef(), comment, callback);
                                        }
                                        public void onNo() { }
                                    });
                                }
                            } );
                            hp2.add(deleteComment);
                            hp2.setCellHorizontalAlignment(deleteComment, HorizontalPanel.ALIGN_RIGHT);
                            hp2.setCellVerticalAlignment(deleteComment, HorizontalPanel.ALIGN_TOP);
                        }
                        vp.add(hp2);
                        vp.setCellVerticalAlignment(hp2, HorizontalPanel.ALIGN_BOTTOM);
                        hp.add(vp);
                        commentsPanel.add(hp);
                    }
                }
            }

            // adds the add comment if not already there
            if (firstDisplay) {
                if (object.getStatus()!=BentoStatus.TRASH
                        && object.getStatus()!=BentoStatus.ARCHIVE) {
                    addEnterCommentField();
                }
                firstDisplay = false;
            }
        } 
    }



    // TBD: what if the not current page still has a handler ???
    public void onCacheEvent(CacheEvent event) {

        LogManager.get().debug("DEBUG - onCacheEvent ; Page=" + this.getTitle() + "; obj=" + this.getObject() + " ; event=" + event + " ; isUpdateExpected=" + isUpdateExpected);

        // dont know when this can occur...
        if (event.getEventType() == CacheEvent.CacheEventType.FULL_RELOAD) {
            Bento newObj = CacheManager.get().getCachedObject(objectRef);
            if (newObj != null)
                setObject(newObj);
        }

        // current object was updated
        else if (event.getEventType() == CacheEvent.CacheEventType.UPDATES &&
                 event.getConcernedObjects().contains(objectRef)) {
            if (!isUpdateExpected &&  (getTopPage() == null || !(getTopPage() instanceof BentoPage) ||
                !((BentoPage)getTopPage()).getBentoDef().getType().equals(object.getType()) ) )
                LogManager.get().info("L'objet affiché vient d'être modifié par un autre utilisateur");
            setObject(CacheManager.get().getCachedObject(getObject().getRef()));
            isUpdateExpected = false;
        }

        // current object was deleted
        if (event.getEventType() == CacheEvent.CacheEventType.DELETES &&
                 event.getConcernedObjects().contains(objectRef)) {
            LogManager.get().info("L'objet affiché vient d'être supprimé par un autre utilisateur");
            getPageNav().back();
        }
    }


    private void addEnterCommentField() {
        HorizontalPanel hp = new HorizontalPanel();
        hp.setWidth("100%");
        hp.setStylePrimaryName("rwt-pageComment");
        TextArea dummyText = new TextArea();
        dummyText.setVisibleLines(1);
        dummyText.setCharacterWidth(60);
        dummyText.setText("Tapez votre commentaire...");
        dummyText.addClickHandler(new TypeCommentClickHandler());
        hp.add(dummyText);
        commentsInputPanel.add(hp);
    }

    protected void doChangeState(Bento bento, BentoStatus to, final String actionMessage) {
        LogManager.get().handling(actionMessage + " en cours...");
        AsyncCallback<ObjectRef> callback = new AsyncCallback<ObjectRef>() {
            public void onFailure(Throwable caught) {
                if (caught instanceof StatusCodeException) {
                    LogManager.get().warningConnectionLost();
                    LogManager.get().warning("Impossible de contacter le serveur. Veuillez vérifier votre connexion Internet et recommencer");
                } else if (caught instanceof ObjectNotFoundException) {
                    LogManager.get().warning("L'objet a été effacé", caught);
                    CacheManager.get().refreshCache();
                    getPageNav().back();
                } else if (caught instanceof AuthorizationException) {
                    LogManager.get().warning("Vous n'avez pas les droits suffisants", caught);
                    // could reauthenticate...
                    getPageNav().back();
                } else {
                    LogManager.get().error("Echec de la "+actionMessage, caught);
                }
            }
            public void onSuccess(ObjectRef arg0) {
                LogManager.get().handled(actionMessage+" terminée.");
                LogManager.get().infoConnectionOk();
                // stale cache
                CacheManager.get().refreshCache();
                // redirect to user list
                getPageNav().back();
            }
        };
        // work on copy of object, in case it fails
        Bento copy = bento.getDef().createBento(bento);
        copy.setStatus(to);
        CacheManager.get().getPersistenceService().updateBento(copy, callback);
    }



    protected void doPermanentDelete(Bento object) {
        LogManager.get().handling("Effacement définitif en cours...");
        AsyncCallback<Void> callback = new AsyncCallback<Void>() {
            public void onFailure(Throwable caught) {
                if (caught instanceof StatusCodeException) {
                    LogManager.get().warningConnectionLost();
                    LogManager.get().warning("Impossible de contacter le serveur. Veuillez vérifier votre connexion Internet et recommencer");
                } else if (caught instanceof ObjectNotFoundException) {
                    LogManager.get().warning("L'objet a déjà été effacé", caught);
                    CacheManager.get().refreshCache();
                    getPageNav().back();
                } else if (caught instanceof AuthorizationException) {
                    LogManager.get().warning("Vous n'avez pas le droit d'effacer définitivement cet objet", caught);
                    // could reauthenticate...
                    getPageNav().back();
                } else {
                    LogManager.get().error("Echec de l'effacement définitif !", caught);
                }
                // TBD: what to do next ???
            }
            public void onSuccess(Void arg0) {
                LogManager.get().handled("Effacement définitif terminé.");
                // stale cache
                setUpdateExpected();
                // redirect to user list
                getPageNav().back();
            }
        };
        CacheManager.get().getPersistenceService().permanentDeleteBento(object.getRef(), callback);
    }



    protected ClickHandler trashClickHandler = new ClickHandler() {
        public void onClick(ClickEvent arg0) {
            Dialog.confirm("Voulez-vous vraiment supprimer cet objet?", new DialogResponseHandler() {
                public void onYes() {
                    doChangeState(getObject(), BentoStatus.TRASH, "suppression");
                }
                public void onNo() {  }
            });
        }
    };

    protected ClickHandler untrashClickHandler = new ClickHandler() {
        public void onClick(ClickEvent arg0) {
            doChangeState(getObject(), BentoStatus.ACTIVE, "récupération");
        }
    };

    protected ClickHandler permanentDeleteClickHandler = new ClickHandler() {
        public void onClick(ClickEvent arg0) {
            Dialog.confirm("Voulez-vous vraiment effacer cet objet pour toujours? Il ne sera pas possible de le récupérer.",
                    new DialogResponseHandler() {
                public void onYes() {
                    doPermanentDelete(getObject());
                }
                public void onNo() {  }
            });
        }
    };


    private class TypeCommentClickHandler implements ClickHandler {
        
        private TypeCommentClickHandler() {
        }

        public void onClick(ClickEvent arg0) {

            commentsInputPanel.clear();

            // adds the comment input
            HorizontalPanel hp = new HorizontalPanel();
            hp.setWidth("100%");
            hp.setStylePrimaryName("rwt-pageComment");

            // put commenter photo
            Bento me = CacheManager.get().getCachedObject(SecurityManager.get().getAuthorizer().getPrincipal().getUserRef());
            Image ownPhoto;
            if (me.getAttachedFiles() != null && me.getAttachedFiles().length > 0) {
                ownPhoto = new Image(me.getAttachedFiles()[0].getPreviewUrl(me.getRef()));
            } else {
                // TBD: default
                ownPhoto = new Image(GWT.getModuleBaseURL()+"images/anonymous.png");
            }
           // ownPhoto.setPixelSize(40, 50);
            ownPhoto.setStylePrimaryName("rwt-userPhoto");
            hp.add(ownPhoto);
            hp.setCellWidth(ownPhoto, "60px");

            // add text
            Panel vp = new FlowPanel();
            vp.setWidth("100%");
            TextArea commentTextWidget = new TextArea();
            commentTextWidget.setVisibleLines(3);
            commentTextWidget.setCharacterWidth(60);
            vp.add(commentTextWidget);
            Button commentButton = new Button("Commenter");
         //   commentButton.setStylePrimaryName("rwt-Button");
            commentButton.addClickHandler(new AddCommentClickHandler(commentTextWidget, commentButton));
         //   HorizontalPanel hp2 = new HorizontalPanel();
         //   hp2.setWidth("100%"); // ???
         //   hp2.add(commentButton);
         //   hp2.setCellHorizontalAlignment(commentButton, HorizontalPanel.ALIGN_RIGHT);
         //   hp2.setCellVerticalAlignment(commentButton, HorizontalPanel.ALIGN_BOTTOM);
            vp.add(commentButton);
            hp.add(vp);
            commentsInputPanel.add(hp);

            commentTextWidget.setFocus(true);
        }
    }





    private class AddCommentClickHandler implements ClickHandler {
        
        private TextArea commentBox;

        private Button commentButton;

        private AddCommentClickHandler(TextArea commentBox, Button commentButton) {
            this.commentBox = commentBox;
            this.commentButton = commentButton;
        }

        public void onClick(ClickEvent arg0) {
            // validates the comment has a content
            String commentText = commentBox.getText();
            if (commentText == null || commentText.trim().length() == 0)
                return;

            // validates if has no html and max length is 500
            String tmpComment = commentText.replaceAll("<", "&lt;").replaceAll(">", "&gt;");
            final String modifiedCommentText;
            if (tmpComment.length() > 400)
                modifiedCommentText = tmpComment.substring(0, 400);
            else
                modifiedCommentText = tmpComment;
            LogManager.get().debug("commentText=" + commentText + " ; modifiedCommentText="+modifiedCommentText);

            if (!modifiedCommentText.equals(commentText)) {
                Dialog.confirm("Commentaire trop long ou contenant du HTML. Les balises HTML ne sont pas supportées dans les commentaires. Celui-ci apparaitra sous la forme suivante: <br><i>" +
                        modifiedCommentText + "</i><br>Souhaitez-vous soumettre ce commentaire sous cette forme?",
                        new DialogResponseHandler() {

                    public void onYes() {
                        publishComment(modifiedCommentText);
                    }

                    public void onNo() {
                    }
                });
            }

            else {
                publishComment(modifiedCommentText);
            }

        }

        public void publishComment(String modifiedCommentText) {

            // ok, add it
            AsyncCallback<Void> callback = new AsyncCallback<Void>() {
                public void onFailure(Throwable caught) {
                    if (caught instanceof StatusCodeException) {
                        LogManager.get().warningConnectionLost();
                        LogManager.get().warning("Impossible de contacter le serveur. Veuillez vérifier votre connexion Internet et recommencer");
                    } else
                        LogManager.get().warning("Echec de l'ajout du commentaire", caught);
                    commentButton.setEnabled(true);
                }
                public void onSuccess(Void arg0) {
                    // remove text box
                    commentsInputPanel.clear();
                    addEnterCommentField();
                    LogManager.get().handled("Commentaire ajouté");
                    // stale cache
                    setUpdateExpected();
                    CacheManager.get().refreshCache();
                }
            };
            commentButton.setEnabled(false);
            BentoComment comment = new BentoComment();
            comment.setCommentDate(new Date());
            comment.setCommentText(modifiedCommentText);
            CacheManager.get().getPersistenceService().addComment(getObject().getRef(), comment, callback);
        }

    }

}
