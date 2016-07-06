/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.repit.site.client.blog;

import ch.repit.rwt.client.BentoStatus;
import ch.repit.rwt.client.ui.Page;
import ch.repit.rwt.client.security.Action;
import ch.repit.rwt.client.security.Authorizer;
import ch.repit.rwt.client.security.SecurityManager;
import ch.repit.rwt.client.ui.HelpPage;

/**
 *
 * @author tc149752
 */
public class BlogsPage extends Page {

 
    public BlogsPage() {
        super();
        setPrintable(false);
        setShowPath(false);
        setTitle("Blogs");
    }

    @Override
    public void init() {
        Authorizer auth = SecurityManager.get().getAuthorizer();

        // blogs publics
        addTab("Blogs actifs", new BlogsListPage(this, BlogPublicDef.TYPE, BentoStatus.ACTIVE));
 
        // blogs official
        addTab("Archives", new BlogsListPage(this, BlogPublicDef.TYPE, BentoStatus.ARCHIVE));

        if (auth.isAllowed(Action.CREATE, BlogPublicDef.TYPE) ||
            auth.isAllowed(Action.CREATE, BlogOfficialDef.TYPE))
            addTab("Brouillons", new BlogsListPage(this, BlogPublicDef.TYPE, BentoStatus.DRAFT));

        if (auth.isAllowed(Action.VIEW_TRASH, BlogOfficialDef.TYPE, true) ||
            auth.isAllowed(Action.VIEW_TRASH, BlogPublicDef.TYPE, true))
            addTab("Corbeille", new BlogsListPage(this, BlogPublicDef.TYPE, BentoStatus.TRASH));

        // help tab
        addTab("Aide", new HelpPage(this, "<h3>Qu'est ce qu'un blog?</h3>" +
                "<p>Pour la définition formelle, voir <a href='http://fr.wikipedia.org/wiki/Blog' target='other'>wikipedia</a>." +
                "<p>Dans notre cas, un article de blog a un auteur, et est composé de texte (mis en forme), de documents attachés, " +
                "et de commentaires des autres utilisateurs. L'auteur d'un article de blog peut l'éditer après sa publication." +
                "<h3>Cycle de vie d'un blog</h3>" +
                "<p>Un article de blog passe par les étapes suivantes:" +
                "<ul><li><b>Brouillon</b> : L'article de blog existe mais n'est visible que par son auteur (ou par le comité pour les blogs comité)" +
                "<li><b>Publié</b> : L'article de blog est visible par tous les membres et peut recevoir des commentaires " +
                "<li><b>Archivé</b> : L'article de blog est visible par tous les membres, mais ne peut plus être commenté. Il peut être réactivé par un utilisateur détenteur des droits suffisants" +
                "<li><b>Effacé</b> : L'article de blog est mis dans la corbeille. Il ne peut plus être édité ni commenté. Il peut être récupéré" +
                "<li><b>Supprimé</b> : L'article de blog n'existe plus. Afin de prévenir les erreurs, seul l'administrateur " +
                "peut supprimer définitivement des articles publié. Un brouillon par contre peut être directement " +
                "supprimé par son auteur." +
                "</ul>"
                ));
    }


}
