/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.repit.rwt.client.ui;

import ch.repit.rwt.client.Bento;
import ch.repit.rwt.client.BentoDef;
import ch.repit.rwt.client.BentoDefFactory;
import ch.repit.rwt.client.ObjectRef;
import ch.repit.rwt.client.logs.LogEvent;
import ch.repit.rwt.client.logs.LogEventHandler;
import ch.repit.rwt.client.logs.LogManager;
import ch.repit.rwt.client.logs.ui.LogConsolePage;
import ch.repit.rwt.client.logs.ui.LogToaster;
import ch.repit.rwt.client.persistence.CacheManager;
import ch.repit.rwt.client.security.Action;
import ch.repit.rwt.client.security.Principal;
import ch.repit.rwt.client.security.SecurityManager;
import ch.repit.rwt.client.user.UserDetailsPage;
import ch.repit.rwt.client.user.UserListPage;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Base class for a site with top-level pages
 */
public class Site implements LogEventHandler {

    private List<Hyperlink> m_topLinks = new ArrayList();
    private PageNavigation pageNav;
    private VerticalPanel menuPanel;
    private DockPanel dock;
    private String logoURL;

    private HorizontalPanel sandclockPanel = null;
    private HTML sandclockLabel = null;
    private HorizontalPanel rightLinks = null;
    private HorizontalPanel leftLinks = null;
    private HTML connectionStatus = new HTML();

    private LogConsolePage consolePage;
    
    public Site(String logoURL)
    {
        // init console early, so that is receives all events sent also during init
        consolePage = new LogConsolePage();
        this.logoURL = logoURL;
    }

    
    public void initModule() {
        // does this early...
        String debugStr = Window.Location.getParameter("DEBUG");
        boolean debug = "true".equals(debugStr);
        PageNavigation.get().setDebugEnabled(debug);

        dock = new DockPanel();
        dock.setHorizontalAlignment(DockPanel.ALIGN_LEFT);
        dock.setSize("100%", "100%");

        // The center panel, the contains application-dependant panels
        SimplePanel pageHolder = new SimplePanel();
        pageHolder.setStylePrimaryName("rwt-pageHolder");

        // PageNavigation init
        pageNav = PageNavigation.get();
        pageNav.setCenterPanel(pageHolder);

        // for debug
        if (debug)
            pageNav.displayInPopup(consolePage, false);

        // init console early, so that is receives all events sent also during init
        final LogToaster logToaster = new LogToaster();

        // top menu bar, with links
        HorizontalPanel topPanel = new HorizontalPanel();
        topPanel.setStylePrimaryName("rwt-topLine");
        topPanel.setWidth("100%");
        leftLinks = new HorizontalPanel();
        topPanel.add(leftLinks);
        topPanel.setCellHorizontalAlignment(leftLinks, HorizontalPanel.ALIGN_LEFT);
        rightLinks = new HorizontalPanel();
        topPanel.add(rightLinks);
        topPanel.setCellHorizontalAlignment(rightLinks, HorizontalPanel.ALIGN_RIGHT);

        // logout
        Anchor logoutLink = new Anchor("Déconnexion", "/Logout.jsp");
        logoutLink.setStylePrimaryName("rwt-topLineLink");
        rightLinks.add(logoutLink);

        // console
        Anchor console = new Anchor("Console");
        console.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent arg0) {
                getPageNav().displayInPopup(consolePage, false);
            }
        } );
        console.setStylePrimaryName("rwt-topLineLink");
        rightLinks.insert(console, 0);

        // the logo and status panel
        HorizontalPanel statusPanel = new HorizontalPanel();
        statusPanel.setWidth("100%");
        Image logo = new Image(logoURL);
        logo.setPixelSize(180, 70);
        statusPanel.add(logo);
        statusPanel.add(logToaster);

        // The left menu panel, with logo and applicative submenus
        menuPanel = new VerticalPanel();
        menuPanel.setSpacing(0);
        menuPanel.setHorizontalAlignment(DockPanel.ALIGN_CENTER);
        menuPanel.setStylePrimaryName("rwt-menus");
        
        dock.add(topPanel, DockPanel.NORTH);
        dock.add(statusPanel, DockPanel.NORTH);
        dock.add(menuPanel, DockPanel.WEST);
        dock.setCellWidth(menuPanel, "200px");
        dock.setCellHeight(menuPanel, "100%");
        dock.add(pageHolder, DockPanel.CENTER);
    }

    public PageNavigation getPageNav() {
        return pageNav;
    }

    public Hyperlink addMenuItem( String linkLabel,  Page page) {
        Hyperlink link  = createMenuItemLink(linkLabel, page);
        menuPanel.add(link);
        return link;
    }

    public void addMenuSeparator() {
        menuPanel.add(new HTML("<br>"));
    }

    public void initPrincipal(final Principal principal) 
    {
        // if not admin, HIDE the console...
        if (!SecurityManager.get().getAuthorizer().isAllowed(Action.ADMIN, "*")) {
            rightLinks.remove(0);
        } 

        // admin links if the guy is admin
        if (SecurityManager.get().getAuthorizer().isAllowed(Action.ADMIN, "*")) {
            Anchor appEngineLink = new Anchor("app engine", "https://appengine.google.com/dashboard", "_appEngineTab");
            insertTopLineElement(appEngineLink,true,true);

            Anchor googleAppLink = new Anchor("google apps", "https://www.google.com/a/cpanel/repit.ch/Dashboard", "_googleAppTab");
            insertTopLineElement(googleAppLink,true,true);
        }

        Anchor googleCodeLink = new Anchor("google code", "http://repit.googlecode.com", "_googleCodeTab");
        insertTopLineElement(googleCodeLink,true,false);

        // name and prefs link
        Anchor prefsLink = new Anchor("Préférences");
        prefsLink.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent arg0) {
                getPageNav().confirmLeavePage(new Dialog.DialogResponseHandler() {
                    public void onYes() {
                        for (Hyperlink h : m_topLinks)
                            if (h.getText().equals("Membres"))
                                h.addStyleDependentName("selected");
                            else
                                h.removeStyleDependentName("selected");
                        pageNav.displayTopPage(new UserListPage());
                        pageNav.displayPage(new UserDetailsPage(principal.getUserRef()));
                    }
                    public void onNo() {}
                });
            }
        } );
        insertTopLineElement(prefsLink,false,true);

        Label nameLbl = new Label( principal.getNickName() + "@" + principal.getAuthDomain() );
        nameLbl.addStyleDependentName("red");
        insertTopLineElement(nameLbl,false,true);

        // debug
        if (SecurityManager.get().getAuthorizer().isAllowed(Action.ADMIN, "*")) {
            final CheckBox debugCb = new CheckBox("DEBUG"); 
            String debugStr = Window.Location.getParameter("DEBUG");
            boolean debug = "true".equals(debugStr);
            debugCb.setValue(debug);
            debugCb.addClickHandler(new ClickHandler() {
                public void onClick(ClickEvent arg0) {
                    String queryString = Window.Location.getQueryString();
                    if (debugCb.getValue()) {
                        if (queryString == null || queryString.length() == 0)
                            queryString = "?";
                        else
                            queryString += "&";
                        queryString += "DEBUG=true";
                    } else {
                        queryString = queryString.replaceAll("&DEBUG=true", "");
                        queryString = queryString.replaceAll("DEBUG=true", "");
                    }
                    String url = Window.Location.getPath() +
                            queryString + 
                            Window.Location.getHash();
                    LogManager.get().debug("changing debug state: url=" + url);
                    Window.open(url, "_self", "");
                }
            });
            insertTopLineElement(debugCb,false,true);
        }

        // connection status
        connectionStatus.setHTML("<img src='"+GWT.getModuleBaseURL()+"icons/alerts/warning_small.gif'/>" +
                " Impossible de contacter le serveur <img src='"+GWT.getModuleBaseURL()+"icons/alerts/warning_small.gif'/>");
        connectionStatus.setStylePrimaryName("rwt-topLineLink");
        connectionStatus.addStyleDependentName("yellow");
        rightLinks.insert(connectionStatus, 0);
        connectionStatus.setVisible(false);
        LogManager.get().registerEventHandler(this);

        LogManager.get().info("Locale: " + LocaleInfo.getCurrentLocale().getLocaleName());
    }


    public void doFinalLayout() {
        RootPanel rootPanel = RootPanel.get();
        if (sandclockPanel != null) {
            rootPanel.remove(sandclockPanel);
        }
        rootPanel.add(dock);
    }


    public void insertTopLineElement(Widget widget, boolean left, boolean first) {
        widget.addStyleName("rwt-topLineLink");
        if (left) {
            if (first)
                leftLinks.insert(widget, 0);
            else
                leftLinks.add(widget);
        } else {
            if (first)
                rightLinks.insert(widget, 0);
            else
                rightLinks.add(widget);
        }
    }
    
    public void displaySandclock(String label) {
        if (sandclockPanel == null) {
            sandclockPanel = new HorizontalPanel();  // TBD: or use SimplePanel???
            sandclockPanel.setWidth("100%");
            VerticalPanel innerSandclockPanel = new VerticalPanel();
            innerSandclockPanel.setStylePrimaryName("rwtLoading-frame");
            innerSandclockPanel.add(new HTML("<img src='" + logoURL + "'/>"));
            HorizontalPanel hp2 = new HorizontalPanel();
            HTML loding = new HTML("<img src='"+GWT.getModuleBaseURL()+"icons/loading.gif'/>&nbsp;");
            hp2.add(loding);
            hp2.setCellVerticalAlignment(loding, HorizontalPanel.ALIGN_BOTTOM);
            innerSandclockPanel.add(hp2);
            sandclockLabel = new HTML("");
            hp2.add(sandclockLabel);
            sandclockPanel.add(innerSandclockPanel);
            sandclockPanel.setCellHorizontalAlignment(innerSandclockPanel, HorizontalPanel.ALIGN_CENTER);
            sandclockPanel.setCellVerticalAlignment(innerSandclockPanel, HorizontalPanel.ALIGN_MIDDLE);
            RootPanel rootPanel = RootPanel.get();
            rootPanel.add(sandclockPanel);
        }
        sandclockLabel.setHTML(sandclockLabel.getHTML()+"<br/>" + label);
    }


    public boolean handleReload() {
        boolean stopLoad = false;
        // loads french locale by default, if not done already
        String locale = LocaleInfo.getCurrentLocale().getLocaleName();
        String hashParam = Window.Location.getParameter("directLink");
        if (hashParam==null)
            hashParam = Window.Location.getParameter("tw");  // shorter for twitter
        if (hashParam != null && hashParam.length() > 0)
            hashParam = "#" + hashParam;
        else
            hashParam = null;

        if (locale == null || !locale.startsWith("fr") || hashParam != null) {
            stopLoad = true;
            displaySandclock("redirection...");
            String wref = Window.Location.getPath();

            String qstr = ""; //Window.Location.getQueryString();
            Map<String,List<String>> qparams = new HashMap<String,List<String>>(Window.Location.getParameterMap());
            if (qparams == null)
                qparams = new HashMap<String,List<String>>();

            // adds the locale
            List<String> fr = new ArrayList<String>();
            fr.add("fr");
            qparams.put("locale", fr);

            // removes the direct link
            qparams.remove("directLink");
            qparams.remove("tw");

            if (!qparams.isEmpty()) {
                qstr = "?";
                String and = "";
                for (String k : qparams.keySet()) {
                    qstr += and + k + "=" + qparams.get(k).get(0);
                    and = "&";
                }
            }

            Window.open( wref + qstr + ((hashParam!=null)?hashParam:""),
                    "_self", "");
        }
        return stopLoad;
    }


    public boolean handleHash() {
        String hashRaw = Window.Location.getHash();

        if (hashRaw == null || hashRaw.length() == 0)
            return false;

        String hash = hashRaw.substring(1);  // removes leading #

        ObjectRef oref = new ObjectRef(hash);
        if (oref == null || oref.getType() == null || oref.getId() == null)
            return false;

        BentoDef bd = BentoDefFactory.get().getDef(oref.getType());
        if (bd == null)
            return false;

        Bento obj = CacheManager.get().getCachedObject(oref);
        if (obj == null)
            return false;

        Page page = bd.getViewPage(oref);
        if (page == null)
            return false;

        // what about authorizations ???
        // TBD:  could we display a dummy page called "direct access"?
        getPageNav().displayTopPage(page);
        return true;
    }

    

    private Hyperlink createMenuItemLink(final String label, final Page page) {
        final Hyperlink link = new Hyperlink();
        link.setText(label);
        link.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent arg0) {
                getPageNav().confirmLeavePage(new Dialog.DialogResponseHandler() {
                    public void onYes() {
                        for (Hyperlink h : m_topLinks)
                            h.removeStyleDependentName("selected");
                        link.addStyleDependentName("selected");
                        PageNavigation.get().displayTopPage(page);
                    }
                    public void onNo() {}
                });
            } } );
        link.setStylePrimaryName("rwt-menuItem");
        m_topLinks.add(link);
        return link;
    }
    

    public void onLogEvent(LogEvent event) {
        if (event.getSeverity() == LogEvent.Severity.INFO_CONN_OK)
            connectionStatus.setVisible(false);
        else if (event.getSeverity() == LogEvent.Severity.WARNING_CONN_LOST)
            connectionStatus.setVisible(true);
    }




}
