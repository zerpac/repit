package ch.repit.site.client;


import ch.repit.site.client.blog.BlogsPage;
import ch.repit.site.client.blog.OfficialDocsPage;
import ch.repit.site.client.calendar.BookingsPage;
import ch.repit.rwt.client.persistence.CacheEvent;
import ch.repit.rwt.client.security.AuthenticationHandler;
import ch.repit.rwt.client.security.Principal;
import ch.repit.rwt.client.persistence.CacheEvent.CacheEventType;
import ch.repit.rwt.client.persistence.CacheEventHandler;
import ch.repit.rwt.client.persistence.CacheManager;
import ch.repit.rwt.client.security.Action;
import ch.repit.rwt.client.security.SecurityManager;
import ch.repit.rwt.client.security.RoleDef;
import ch.repit.rwt.client.admin.AdminPage;
import ch.repit.rwt.client.security.Authorizer;
import ch.repit.rwt.client.ui.Page;
import ch.repit.rwt.client.ui.Site;
import ch.repit.rwt.client.user.UserListPage;

import ch.repit.site.client.contact.YpeListPage;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Hyperlink;


/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Repit implements EntryPoint, AuthenticationHandler, CacheEventHandler
{
    private Site site;
    private Page firstPage;
    private Hyperlink firstLink;

    public Repit() {
        // register custom BentoDef
        new RepitBentoInitializer().registerCustomDefs();

        // logo generated with http://cooltext.com
        site = new Site(GWT.getModuleBaseURL()+"images/repit_logo3.png");
    }


    /** 
     * This is the entry point method.
     */
    public void onModuleLoad() {
        if(!site.handleReload()) {
            site.initModule();
            // and display a sandclock
            site.displaySandclock("authentification...");
            // perform user auth (with GAE)
            SecurityManager.get().authenticateUser(this);
        }
    }
    

    public void onUserAuthenticated(Principal principal)
    {

        firstPage = new BookingsPage();

       // site.displaySandclock("authentification OK!");
        site.initPrincipal(principal);

        // activate the cache
        CacheManager.get().registerEventHandler(this);
        CacheManager.get().activate();
        site.displaySandclock("chargement du cache minimal...");

        Authorizer auth = SecurityManager.get().getAuthorizer();

        // adds the top pages
        firstLink = site.addMenuItem("Inscriptions", firstPage);
        site.addMenuItem("Blogs", new BlogsPage());
        site.addMenuItem("Documents Officiels", new OfficialDocsPage());
        site.addMenuItem("Membres", new UserListPage());
        site.addMenuItem("Pages Jaunes", new YpeListPage());

        site.addMenuSeparator();
        if (auth.isAllowed(Action.UPDATE, RoleDef.TYPE))
            site.addMenuItem("Admin", new AdminPage());

        // adds the top links....
        // gmail link if mail is "repit.ch"
        if (principal.getEmail() != null && principal.getEmail().endsWith(principal.getAuthDomain())) {
            site.insertTopLineElement(new Anchor("gmail", "http://mail.repit.ch", "_gmailTab"),true, false);
        }

        // twitter link
        // 20.2.2013: supression du lien  site.insertTopLineElement(new Anchor("twitter", "http://twitter.com/repitch", "_twitterTab"),true, false);
    }

    public void onAuthenticationFailed(Throwable exception) {
        site.displaySandclock("Echec de l'authentification!");
        site.doFinalLayout();
    }

    public void onCacheEvent(CacheEvent event) {
        if (event.getEventType() == CacheEventType.FULL_RELOAD) {
            CacheManager.get().unregisterEventHandler(this);
            site.displaySandclock("cache minimal chargé!");

            // if direct displays
            if (!site.handleHash())  {
                // sets the initial page displayed
                site.getPageNav().displayTopPage(firstPage);
                if (firstLink != null)
                    firstLink.addStyleDependentName("selected");
            }

            site.doFinalLayout();
        }
    }
}
