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
public class OfficialDocsPage extends Page {

 
    public OfficialDocsPage() {
        super();
        setPrintable(false);
        setShowPath(false);
        setTitle("Documents officiels");
    }

    @Override
    public void init() {
        Authorizer auth = SecurityManager.get().getAuthorizer();

        // blogs official
        addTab("Documents actifs", new BlogsListPage(this, BlogOfficialDef.TYPE , BentoStatus.ACTIVE));

        addTab("Archives", new BlogsListPage(this, BlogOfficialDef.TYPE , BentoStatus.ARCHIVE));

        if (auth.isAllowed(Action.CREATE, BlogOfficialDef.TYPE))
            addTab("Brouillons", new BlogsListPage(this,  BlogOfficialDef.TYPE , BentoStatus.DRAFT));

        if (auth.isAllowed(Action.VIEW_TRASH, BlogOfficialDef.TYPE, true))
            addTab("Corbeille", new BlogsListPage(this, BlogOfficialDef.TYPE , BentoStatus.TRASH));

        // help tab
        addTab("Aide", new HelpPage(this, "<h3>Les documents officiels</h3>" +
                "<p>sont dévolus aux informations officielles; l'ensemble des membres du comité détient les droits " +
                "de mise à jour sur un article, droits qui sont sont perdus par le membre quittant le comité. " +
                "</p>" +
                "<h3>Cycle de vie</h3>" +
                "<p>Un document officiel passe par les étapes suivantes:" +
                "<ul><li><b>Brouillon</b> : existe mais n'est visible que par <b>le comité</b>" +
                "<li><b>Publié</b> : est visible par tous les membres et peut recevoir des commentaires " +
                "<li><b>Archivé</b> : est visible par tous les membres, mais ne peut plus être commenté. Il peut être réactivé par un utilisateur détenteur des droits suffisants" +
                "<li><b>Effacé</b> : est mis dans la corbeille. Il ne peut plus être édité ni commenté. Il peut être récupéré" +
                "<li><b>Supprimé</b> : n'existe plus. <i>Afin de prévenir les erreurs, seul l'administrateur " +
                "peut supprimer définitivement des articles publié. Un brouillon par contre peut être directement " +
                "supprimé par son auteur.</i>" +
                "</ul>"));
    }


}
