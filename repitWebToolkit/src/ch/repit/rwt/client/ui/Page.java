package ch.repit.rwt.client.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * Base class for RWT pages
 */
public class Page {


    private VerticalPanel m_panel = null;

    private Page parentPage = null;
    private Page currentTab = null;

    private boolean showPath = true;
    private boolean initCalled = false;

    private boolean printable = false;

    private List<Hyperlink> m_tabsLinks = new ArrayList<Hyperlink>();
    private Map<Hyperlink,Page> m_tabsPageMap = new HashMap<Hyperlink,Page>();
    
    private int defaultTabIndex = 0;

    /**
     * Constructor for top pages
     */
    protected Page() {
        parentPage = null;
    }

    /**
     * Constructor for sub pages
     */
    protected Page(Page parentPage) {
        this.parentPage = parentPage;
    }

    /**
     * Facility metohd (historical), same as PageNavigation.get()
     * @return
     */
    protected PageNavigation getPageNav() {
        return PageNavigation.get();
    }

    public Page getTopPage() {
        return parentPage;
    }

    protected void setShowPath(boolean showPath) {
        this.showPath = showPath;
    }

    private String m_title;
    public void setTitle(String title) {
        // avoid to set super title, no need for tooltip...
        m_title = title;
    }
    public String getTitle() {
        return m_title;
    }

    /**
     * returns the curent tab displayed, or this page if no tabs
     */
    public Page getCurrentTab() {
        if (currentTab != null)
            return currentTab;
        else
            return this;
    }

    public boolean isPrintable() {
        return printable;
    }

    public void setPrintable(boolean printable) {
        this.printable = printable;
    }


    /*
     * Should return a unique ID that allows history navigation to uniquely idneitfy this location
     * If object page, must also identify the object instance displayed.
    
    public String getPageId() {
        return m_title==null?"noPageId":m_title.replaceAll(" ", "");
    }
 */


    protected void addTab(Page page) {
        addTab(page.getTitle(), page, false);
    }
    protected void addTab(Page page, boolean isDefaultTab) {
        addTab(page.getTitle(), page, isDefaultTab);
    }


    /**
     * Either a page has tabs and subPages, or it has content. Content is ignored if there are tabs
     * @param label
     * @param page
     */
    protected void addTab(String label, Page page) {
        addTab(label, page, false);
    }

    /**
     * Either a page has tabs and subPages, or it has content. Content is ignored if there are tabs
     * @param label
     * @param page
     * @param isDefaultTab the last caller of this method with true set for this param will be displayed
     */
    protected void addTab(String label, Page page, boolean isDefaultTab) {
        if (label == null)
            label = "no label"; // if there is only one tab, will not display tabs
        Hyperlink tabLink = new Hyperlink();
        tabLink.setText(label);
        m_tabsLinks.add(tabLink);
        if (page == null) {
            page = new Page(this) {
                @Override
                protected Widget doContentlayout() {
                    return new HTML("<p>Page en travaux...");
                }
            };
        }
        m_tabsPageMap.put(tabLink, page);

        if (isDefaultTab)
            defaultTabIndex = m_tabsLinks.indexOf(tabLink);
    }


    /**
     * Invoked when the page is layout
     */
    protected void init() {  }

    /**
     * Pages that have content (non tab-holders) must implement this method. Usually
     * use caching of content, as this mathod may ba called several times.
     * @return the page content, between the 2 toolbars
     */
    protected Widget doContentlayout() {
        return null;
    }
    
    /**
     * Implement this to add widgets to the toolbar.
     * WARNING: it may be called twice, and the implementation MUST instanciate new widgets each
     * time (not required for handlers)!!!
     * Usually:
     * left   = buttons
     * middle = custom
     * right  = links
     * IMPORTANT: you should call super.fillToolbarButtons(leftWidgets,middleWidgets,rightWidgets);
     * at the beggining of the method, if you have page-level buttons
     */
    protected void fillToolbarWidgets(List<Widget> leftWidgets,
                                      List<Widget> middleWidgets,
                                      List<Widget> rightWidgets)
    {
        if (getTopPage() != null) {
            getTopPage().fillToolbarWidgets(leftWidgets, middleWidgets, rightWidgets);
        }
        // experimental
        if (isPrintable()) {
            PushButton print = new PushButton(new Image(GWT.getModuleBaseURL()+"icons/print.gif"));
            print.addClickHandler(printCh);
            print.setTitle("imprimer la page centrale");
            print.setStylePrimaryName("rwt-pushButton");
            rightWidgets.add(print);
        }
    }

    private ClickHandler printCh = new ClickHandler() {
        public void onClick(ClickEvent arg0) {
            // open an external window
            PageNavigation.get().displayForPrint(getCurrentTab().doContentlayout());
        }
    };

    
    /**
     * Called by PageNavigation
     */
    protected VerticalPanel doLayout()
    {
        if (m_panel == null) {
            if (!initCalled) {
                init();
                initCalled = true;
            }
            m_panel = new VerticalPanel();
            VerticalPanel innerPanel = m_panel;

            // 1. If top page, set page header
            if (parentPage == null) {
                // NO STYLE HERE
                m_panel.setSize("100%", "100%");
                // if no sub pages
                if (m_tabsLinks != null && m_tabsLinks.size() > 0) {
                    SimplePanel pageContentHolder = new SimplePanel();
                    m_panel.add(generatePageHeader(pageContentHolder));
                    m_panel.add(pageContentHolder);
                    // set first tab
                    m_tabsLinks.get(defaultTabIndex).addStyleDependentName("selected");
                    currentTab = m_tabsPageMap.get(m_tabsLinks.get(defaultTabIndex));
                    pageContentHolder.setWidget(currentTab.doLayout());  // init will be called
                }
                else {
                    m_panel.add(generatePageHeader(null));
                    innerPanel = new VerticalPanel();
                    innerPanel.setStylePrimaryName("rwt-page");
                    m_panel.add(innerPanel);
                }
            } else {
                innerPanel.setStylePrimaryName("rwt-page");
            }

            // 2. If no sub page (content is set), generate toolbox and add content
            Widget contentWidget = this.doContentlayout();
            if (contentWidget != null) {
                // top toolbar
                generateToolbar(innerPanel);

                // content
                SimplePanel pageContentHolder = new SimplePanel();
                pageContentHolder.setStylePrimaryName("rwt-pageContent");
                pageContentHolder.setWidget(contentWidget);
                innerPanel.add(pageContentHolder);
                innerPanel.setCellHorizontalAlignment(pageContentHolder, HorizontalPanel.ALIGN_CENTER);

                // bottom toolbar
                if (!this.isOnlyTopToolbar())
                    generateToolbar(innerPanel);
            }
        }
        return m_panel;
    }



    /**
     * Called (by page nav) when the user leaves a page, either through back link or other link.
     * For instance, could remove member references, references to listeners, etc.
     * By default default, nullify cached panel and delegates to sub tabs, if any.
     */
    protected void doUnLayout() {
        m_panel = null;
        for (Page p : m_tabsPageMap.values())
            p.doUnLayout();
    }



    /**
     * Only called on top pages
     */
    private Panel generatePageHeader(final SimplePanel pageContentHolder)
    {
        HorizontalPanel pageHeaderBG = new HorizontalPanel();
        pageHeaderBG.setStylePrimaryName("rwt-pageMenuBar");

        HorizontalPanel pageHeader = new HorizontalPanel();
        
        // link (and page title)
        if (showPath && getPageNav().hasNavigationPath()) {
            Widget history = getPageNav().getNavigationPath();
            pageHeader.add(history);
            pageHeader.setCellVerticalAlignment(history, VerticalPanel.ALIGN_MIDDLE);
            // maybe: try to use "flowPanel" instead of horizontal
        }

        //    b. tabs
        if (m_tabsLinks.size() > 1) {
            HorizontalPanel tabPanel = new HorizontalPanel();
            for (Iterator<Hyperlink> i = m_tabsLinks.iterator(); i.hasNext();) {
                final Hyperlink tabLink = i.next();

                tabLink.addClickHandler(new ClickHandler() {
                    public void onClick(ClickEvent arg0) {
                        getPageNav().confirmLeavePage(new Dialog.DialogResponseHandler() {
                            public void onYes() {
                                currentTab = m_tabsPageMap.get(tabLink);
                                for (Iterator<Hyperlink> i = m_tabsLinks.iterator(); i.hasNext();) {
                                    Hyperlink other = i.next();
                                    if (other != tabLink)
                                        other.removeStyleDependentName("selected");
                                }
                                tabLink.addStyleDependentName("selected");
                                pageContentHolder.setWidget(currentTab.doLayout());
                            }
                            public void onNo() {}
                        } );
                    } } );
                 
                tabLink.setStylePrimaryName("rwt-pageTab");
                tabPanel.add(tabLink);
                tabPanel.setCellVerticalAlignment(tabLink, VerticalPanel.ALIGN_BOTTOM);
            }
            // select the first one by default
            m_tabsLinks.get(defaultTabIndex).addStyleDependentName("selected");
            tabPanel.setHorizontalAlignment(VerticalPanel.ALIGN_LEFT);
            pageHeader.add(tabPanel);
            pageHeader.setCellHorizontalAlignment(tabPanel, VerticalPanel.ALIGN_LEFT);
            pageHeader.setCellVerticalAlignment(tabPanel, VerticalPanel.ALIGN_BOTTOM);
        }

        pageHeaderBG.add(pageHeader);

        // close link is mainly used if page is display in a dialog box
        if (closeLink != null) {
            pageHeaderBG.add(closeLink);
            pageHeaderBG.setCellHorizontalAlignment(closeLink, HorizontalPanel.ALIGN_RIGHT);
            pageHeaderBG.setCellVerticalAlignment(closeLink, HorizontalPanel.ALIGN_MIDDLE);
        }
        
        return pageHeaderBG;
    }


    private void generateToolbar(VerticalPanel panelToAdd)
    {
        // 1. delegate widget creation
        ArrayList<Widget> leftW = new ArrayList();
        ArrayList<Widget> middleW = new ArrayList();
        ArrayList<Widget> rightW = new ArrayList();
        this.fillToolbarWidgets(leftW, middleW, rightW);

        if (leftW.isEmpty() && rightW.isEmpty() && middleW.isEmpty())
            return;

        // There is at least one widget to dispaly, thus there will be a toolbar
        HorizontalPanel toolbar = new HorizontalPanel();
        toolbar.setStylePrimaryName("rwt-pageToolBar");

        // 2. buttons on the left (convention)
        if (!leftW.isEmpty()) {
            HorizontalPanel panel = new HorizontalPanel();
            panel.setHorizontalAlignment(VerticalPanel.ALIGN_LEFT);
            panel.setSpacing(2); // obliged...
            for (Widget w : leftW) {
                panel.add(w);
            }
            toolbar.add(panel);
            toolbar.setCellHorizontalAlignment(panel, VerticalPanel.ALIGN_LEFT);
            toolbar.setCellVerticalAlignment(panel, VerticalPanel.ALIGN_MIDDLE);
        }

        // 3. links on the left after buttons (convention)
        if (!middleW.isEmpty()) {
            HorizontalPanel panel = new HorizontalPanel();
            panel.setHorizontalAlignment(VerticalPanel.ALIGN_LEFT);
            for (Widget w : middleW) {
                w.addStyleName("rwt-pageToolbarItem");
                panel.add(w);
            }
            toolbar.add(panel);
            toolbar.setCellHorizontalAlignment(panel, VerticalPanel.ALIGN_LEFT);
            toolbar.setCellVerticalAlignment(panel, VerticalPanel.ALIGN_MIDDLE);
        }

        // 4. specials widgets on the right
        if (!rightW.isEmpty()) {
            HorizontalPanel panel = new HorizontalPanel();
            panel.setHorizontalAlignment(VerticalPanel.ALIGN_RIGHT);
            for (Widget w : rightW) {
                w.addStyleName("rwt-pageToolbarItem");
                panel.add(w);
            }
            toolbar.add(panel);
            toolbar.setCellHorizontalAlignment(panel, VerticalPanel.ALIGN_RIGHT);
            toolbar.setCellVerticalAlignment(panel, VerticalPanel.ALIGN_MIDDLE);
        }

        panelToAdd.add(toolbar);
        panelToAdd.setCellHorizontalAlignment(toolbar, HorizontalPanel.ALIGN_CENTER);
    }


    private PushButton closeLink = null;

    void showCloseButton(ClickHandler closeHandler) {
        closeLink = new PushButton(new Image(GWT.getModuleBaseURL()+"icons/cancelClose.png"));
        closeLink.setStylePrimaryName("rwt-pushButton"); // so that it does not have a border
        closeLink.addClickHandler(closeHandler);
    }

    
    private boolean onlyTopToolbar = false;
    protected boolean isOnlyTopToolbar() {
        return onlyTopToolbar;
    }
    protected void setOnlyTopToolbar(boolean onlyTopToolbar) {
        this.onlyTopToolbar = onlyTopToolbar;
    }

}
