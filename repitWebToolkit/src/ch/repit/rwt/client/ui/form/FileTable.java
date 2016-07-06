/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.repit.rwt.client.ui.form;

import ch.repit.rwt.client.Bento;
import ch.repit.rwt.client.ContentTypeFamily;
import ch.repit.rwt.client.FileDescriptor;
import ch.repit.rwt.client.ObjectRef;
import ch.repit.rwt.client.logs.LogManager;
import ch.repit.rwt.client.persistence.CacheManager;
import ch.repit.rwt.client.persistence.ObjectNotFoundException;
import ch.repit.rwt.client.security.Action;
import ch.repit.rwt.client.security.SecurityManager;
import ch.repit.rwt.client.security.AuthorizationException;
import ch.repit.rwt.client.security.Authorizer;
import ch.repit.rwt.client.ui.BentoPage;
import ch.repit.rwt.client.ui.Dialog;
import ch.repit.rwt.client.ui.Dialog.DialogResponseHandler;
import ch.repit.rwt.client.ui.ListPage;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.StatusCodeException;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Widget;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

/**
 *
 * @author tc149752
 */
public class FileTable extends ListPage<FileDescriptor> {

    private ObjectRef bentoRef;
    private AsyncCallback<Void> deleteCallback;
    private boolean canDeleteFiles;

    public FileTable(final BentoPage holdingPage) {
        super(null);

        super.addColumn("Aperçu", "_preview", false);
        super.addColumn("Fichier", "fileName", true);
        super.addColumn("Type", "_contentTypeFamily", true);
        super.addColumn("Taille", "fileSize", true);

        super.addColumn("", "_action", false);

        deleteCallback = new AsyncCallback<Void>() {
            public void onFailure(Throwable caught) {
                if (caught instanceof StatusCodeException) {
                    LogManager.get().warningConnectionLost();
                    LogManager.get().warning("Impossible de contacter le serveur. Veuillez vérifier votre connexion Internet et recommencer");
                } else if (caught instanceof ObjectNotFoundException) {
                    LogManager.get().warning("L'objet a été supprimé pendant que vous l'éditiez", caught);
                    CacheManager.get().refreshCache();
                    getPageNav().back();
                } else if (caught instanceof AuthorizationException) {
                    LogManager.get().warning("Vous n'avez pas les droits suffisants pour effectuer cette opération", caught);
                    // could reauthenticate...
                    getPageNav().back();
                } else {
                    LogManager.get().error("Echec de la suppression du fichier pour une raison inconnue!", caught);
                }
            }
            public void onSuccess(Void arg0) {
                LogManager.get().info("Fichier supprimé");
                holdingPage.setUpdateExpected();
                CacheManager.get().refreshCache();
            }
        };
    }


    public void resetBento(Bento fileBento) {
        if (fileBento != null) {
            bentoRef = fileBento.getRef();
            
            // can delete ?
            Authorizer auth = SecurityManager.get().getAuthorizer();
            canDeleteFiles = auth.isAllowed(Action.UPDATE, fileBento);

            if (fileBento.getAttachedFiles() != null)
                resetData(Arrays.asList(fileBento.getAttachedFiles()));
            else
                resetData(new ArrayList());
        }
    }


    @Override
    protected boolean formatObject(final FileDescriptor file, Map formatedValue) {
        formatedValue.put("fileName", new Anchor(file.getFileName(), file.getViewUrl(bentoRef), "otherTab"));
        formatedValue.put("fileSize", "" + (int)(file.getSizeBytes()/1000) + " KB");

        // delete button
        if (canDeleteFiles) {
            Button b = new Button("Supprimer");
            b.addClickHandler(new ClickHandler() {
                public void onClick(ClickEvent arg0) {
                    Dialog.confirm("Voulez-vous vraiment supprimer ce fichier?", new DialogResponseHandler() {
                        public void onYes() {

                            CacheManager.get().getPersistenceService().deleteFile(bentoRef, file.getId(), deleteCallback);
                        }
                        public void onNo() {  }
                    });
                }
            } );
            formatedValue.put("_action", b);
        }

        // preview, currently only link
        ContentTypeFamily ctf = ContentTypeFamily.getContentTypeFamily(file.getContentType());
        if (ctf != null) {
            formatedValue.put("_contentTypeFamily", ctf.getName());
        } else {
            formatedValue.put("_contentTypeFamily", "("+file.getContentType()+")");
        }

        if (file.isImage()) {
            formatedValue.put("_preview", 
                    Dialog.buildImageLink( file.getViewUrl(bentoRef),file.getPreviewUrl(bentoRef)) );
        } else {
            Anchor anchor = new Anchor();
            anchor.setHref(file.getViewUrl(bentoRef));
            anchor.setTarget("otherTab");
            if (file.isImage()) {
                anchor.setHTML("<img src='" + file.getPreviewUrl(bentoRef) + "'/>");
            } else {
                anchor.setHTML("<img src='" + ctf.getIconHref() + "'/>");
            }
            formatedValue.put("_preview", anchor);
        }

        return true;
    }

    @Override
    protected void onRowClicked(FileDescriptor data, String columnsAttributeName) {
        // TBD: display file in another window
    }

    @Override
    protected int sortCompare(FileDescriptor o1, FileDescriptor o2, String sortAttribute, boolean ascending) {
        if ("fileName".equals(sortAttribute)) {
            return o1.getFileName().compareTo(o2.getFileName()) * (ascending?1:-1) ;
        }
        if ("contentType".equals(sortAttribute)) {
            return o1.getContentType().compareTo(o2.getContentType()) * (ascending?1:-1);
        }
        if ("fileSize".equals(sortAttribute)) {
            return o1.getSizeBytes().compareTo(o2.getSizeBytes()) * (ascending?1:-1);
        }
        return 0;
    }

    public Widget getTable() {
        return super.doContentlayout();
    }



}
