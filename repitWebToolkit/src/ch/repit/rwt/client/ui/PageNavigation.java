/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.repit.rwt.client.ui;

import ch.repit.rwt.client.logs.LogManager;
import ch.repit.rwt.client.ui.form.FormPage;
import com.google.gwt.core.client.GWT;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import java.util.Iterator;
import java.util.LinkedList;

/**
 *
 * @author tc149752
 */
public class PageNavigation {

    private static PageNavigation instance = new PageNavigation();

    public static PageNavigation get() {
        return instance;
    }

    private PageNavigation() {

        /*
        History.addValueChangeHandler(new ValueChangeHandler<String>() {
            public void onValueChange(ValueChangeEvent<String> event) {
                LogManager.get().debug("event="+event.toDebugString()+";type="+event.getType().toString());
                // how to differentiate between a new value added and the back or forward button clicked?
                String historyToken = event.getValue();
                ReplayableAction action = historyTable.get(historyToken);
                if (action != null) {
                    historyTable.remove(historyToken);
                    displayWithAction(historyToken, action);
                }
            }
        });
        */
    }

    /**
     * Called by Site at very beginning
     * @param centerPanel
     */
    void setCenterPanel(SimplePanel centerPanel) {
        this.centerPanel = centerPanel;
    }


    private SimplePanel centerPanel;
    private LinkedList<Page> pageHistory = new LinkedList<Page>();
    private DialogBox dialogBox;

 //   private Map<String,ReplayableAction> historyTable = new HashMap();

    /**
     * Displays a top-menu page
     * @param page
     */
    public void displayTopPage(Page page) {
        // release previous history pages
        while (pageHistory.size() > 1)
            pageHistory.removeLast().doUnLayout();
        pageHistory.clear(); // thus not unlayout on previous top pages

        // display requeted page
        displayPage(page);
    }

    
    /**
     * Navigates to the specified page. doLayout will be called on this page
     * @param page
     */
    public void displayPage(Page page) {
        // adds page to history
        pageHistory.addLast(page);

        // display the page layout
        centerPanel.setWidget(page.doLayout());

        // reset edition monitor
        setPageEdited(false);
    }


    /**
     * Display the page in a popup. Has no effect on last page and page edited.
     * Mainly used for log console.
     * @param page
     * @param modal
     */
    public void displayInPopup(final Page page, final boolean modal) {

        pageHistory.addLast(page);

        if (dialogBox == null) {
            // Create a dialog box and set the caption text
            dialogBox = new DialogBox(false, modal);
        }

        // display the page layout
        page.showCloseButton(new ClickHandler() {
            public void onClick(ClickEvent arg0) {
                closePopup();
            }
        } );
        dialogBox.setText(page.getTitle());
        dialogBox.setWidget(page.doLayout());

        dialogBox.center();
        if (modal)
            Dialog.greyer(true);
        dialogBox.show();
    }

    public void closePopup() {
        pageHistory.removeLast().doUnLayout();
        Dialog.greyer(false);
        dialogBox.hide();
    }

    
    public boolean hasNavigationPath() {
        return !(pageHistory.isEmpty());
        // or is greater than 1 ???
    }

    /**
     * Goes back one page
     */
    public void back() {
        if (pageHistory.size() >= 2) {
            // removes the current page ref
            pageHistory.removeLast().doUnLayout();
          //  History.back(); // will this work ??? 27 Oct 09: not any more with new app engine version (??? why?)

            // set the last but one
            Page backPage = pageHistory.getLast();
            centerPanel.setWidget(backPage.doLayout());
            setPageEdited(false);
        }
    }

    void backToPage(Page page) {
        Page tmpPage;
        do {
            tmpPage = pageHistory.removeLast();
            if (tmpPage==page) {
                displayPage(page);
            } else {
                tmpPage.doUnLayout();
            }
        } while (pageHistory.size() > 0 && tmpPage!=page);

    }


    public Widget getNavigationPath() {
        HorizontalPanel history = new HorizontalPanel();
        for (Iterator<Page> i = pageHistory.iterator(); i.hasNext();) {
            history.add(new HTML("&nbsp;&raquo;&nbsp;"));
            final Page page = i.next();
            if (i.hasNext()) {
                Hyperlink link = new Hyperlink();
                String subtitle = "";
                if (page.getCurrentTab() != null) {
                    String st = page.getCurrentTab().getTitle();
                    if (st != null && st.length() > 0)
                        subtitle = " (" + st + ")";
                }
                link.setText(page.getTitle() + subtitle);
                link.addClickHandler(new ClickHandler() {
                    public void onClick(ClickEvent arg0) {
                        confirmLeavePage(new Dialog.DialogResponseHandler() {
                            public void onYes() {
                                backToPage(page);
                            }
                            public void onNo() {}
                        } );
                    }
                });
                history.add(link);
            } else
                history.add(new HTML(page.getTitle()));
        }
        history.setStylePrimaryName("rwt-pageMenuBarHistory");
        return history;
    }


    private boolean m_isPageEdited = false;
    /**
     * Indicates that the current object is being edited. Thus, any menu or link will display a 
     * confirm window before being followed.
     * @param isPageEdited
     */
    public void setPageEdited(boolean isPageEdited) {
        // if page is edited, we call the page
        if (!m_isPageEdited && isPageEdited) {
            Page currentTab = pageHistory.getLast().getCurrentTab();
            if (currentTab instanceof FormPage)
                ((FormPage)currentTab).setPageEdited();
        }
        m_isPageEdited = isPageEdited;
    }
    public boolean isPageEdited() {
        return m_isPageEdited;
    }
    
    public void confirmLeavePage(final Dialog.DialogResponseHandler customResponseHandler) {
        if (isPageEdited()) {
            Dialog.confirm("Voulez-vous vraiment annuler l'édition en cours", new Dialog.DialogResponseHandler() {
                public void onYes() {
                    Page currentPage = pageHistory.getLast();
                    if (currentPage instanceof FormPage)
                        ((FormPage)currentPage).doCancelEdition();
                    setPageEdited(false);
                    customResponseHandler.onYes();
                }
                public void onNo() {
                    customResponseHandler.onNo();
                }
            });
        } else {
            customResponseHandler.onYes();
        }
    }

    private boolean debugEnabled = false;

    public void setDebugEnabled(boolean enabled) {
        if (debugEnabled != enabled) {
            // reinit layout
        }
        debugEnabled = enabled;
    }

    public boolean isDebugEnabled() {
        return debugEnabled;
    }

    public void displayForPrint(Widget pageContent) {
        LogManager.get().debug("displayForPrint : enter");
        String innerHtml = pageContent.getElement().getInnerHTML();
        innerHtml = "<table>" + innerHtml + "</table>";
        openWindowAndDisplayHtml(innerHtml, GWT.getModuleBaseURL());
        LogManager.get().debug("displayForPrint: leave");
    }

    /*
    public void displayWithAction(String pageId, ReplayableAction displayAction) {
        if (pageId != null)
            historyTable.put(pageId, displayAction);
        displayAction.play();
    }
     */

    /* removed the CSS (after content type)
     * - put it back with relative path (is it enough?)
     */
    native static void openWindowAndDisplayHtml(String innerHtml, String module) /*-{
        var generator=$wnd.open('','printWin','height=600,width=700,status=1');
        generator.document.write("<html>\n  <head>\n    <title>Impression</title>\n");
        generator.document.write('    <meta http-equiv=Content-Type content="text/html; charset=UTF-8">\n');
        generator.document.write('    <link type="text/css" rel="stylesheet" href="'+module+'rwt.css">\n');
        generator.document.write('    <link type="text/css" rel="stylesheet" href="'+module+'repit.css">\n');
        generator.document.write('  </head>\n  <body onload="javascript:print();">\n');
        generator.document.write(innerHtml);
        generator.document.write('\n  </body>\n</html>');
        generator.document.close();
    }-*/;

    /*
        generator.document.write('</head><body onload="javascript:print();" onClick="javascript:self.close();">');
     */

}

