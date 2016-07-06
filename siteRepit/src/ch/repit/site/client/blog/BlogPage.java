/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.repit.site.client.blog;

import ch.repit.rwt.client.Bento;
import ch.repit.rwt.client.BentoDefFactory;
import ch.repit.rwt.client.BentoStatus;
import ch.repit.rwt.client.ObjectRef;
import ch.repit.rwt.client.audit.AuditListPage;
import ch.repit.rwt.client.audit.AuditQueryDef;
import ch.repit.rwt.client.security.Action;
import ch.repit.rwt.client.security.Authorizer;
import ch.repit.rwt.client.security.SecurityManager;
import ch.repit.rwt.client.ui.BentoPage;
import ch.repit.rwt.client.ui.Page;

/**
 *
 * @author tc149752
 */
public class BlogPage extends BentoPage {

    public BlogPage(BlogDef blogDef, ObjectRef blogRef, Page topPage) {
        super(blogDef.getType(), blogRef, topPage);
    }


    @Override
    protected void init() {
        super.init();
        Authorizer auth = SecurityManager.get().getAuthorizer();
        
        setShowPath(true);
        if (isCreate()) {
            setTitle("Création article de blog");
        }
        else {
            setTitle(getObject().getDisplayValue(BlogDef.ATTR_SUBJECT));
            if (getObject().get(BlogDef.ATTR_PUBDATE).getDate() != null)
                addTab("Afficher", new BlogDisplayPage(getObject().getRef(), this));
        }

        // cannot edit trash
        if (isCreate() 
                || ( auth.isAllowed(Action.UPDATE, getObject())
                    && (getObject().getStatus()==BentoStatus.ACTIVE 
                        || getObject().getStatus()==BentoStatus.DRAFT)
                   )
           )
            addTab("Editer", new BlogEditPage((BlogDef)getBentoDef(), getObject().getRef(), this));

        // only for drafts
        if (!isCreate() && getObject().getStatus()==BentoStatus.DRAFT)
            addTab("Prévisualiser", new BlogDisplayPage(getObject().getRef(), this));

        // not for drafts
        if (!isCreate() && auth.isAllowed(Action.AUDIT, getObject()) &&
                getObject().getStatus()!=BentoStatus.DRAFT) {
            Bento queryUserActions = BentoDefFactory.get().getDef(AuditQueryDef.TYPE).createBento();
            queryUserActions.get(AuditQueryDef.ATTR_OBJECTREF).set(getObject().getRef().toString());
            addTab("Historique", new AuditListPage(this, queryUserActions));
        }
    }


}
