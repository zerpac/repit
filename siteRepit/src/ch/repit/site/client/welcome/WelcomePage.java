/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.repit.site.client.welcome;

import ch.repit.rwt.client.Bento;
import ch.repit.rwt.client.BentoDef;
import ch.repit.rwt.client.ObjectRef;
import ch.repit.rwt.client.logs.LogManager;
import ch.repit.rwt.client.persistence.CacheEvent;
import ch.repit.rwt.client.persistence.CacheEventHandler;
import ch.repit.rwt.client.persistence.CacheManager;
import ch.repit.rwt.client.ui.Page;
import ch.repit.rwt.client.util.ClientFormaters;
import ch.repit.rwt.client.util.Formaters;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 *
 * @author tc149752
 */
public class WelcomePage extends Page {


    
    public WelcomePage() {
        super();
        setTitle("Accueil");
        super.setShowPath(false);

        addTab("Dashboard", new DashboardTab(this));
      //  addTab("Météo", null);
      //  addTab("Enneigement", null);
        addTab("Webcam Dorchaux", new IFramePage(this, "http://www.lesmosses.net/webcam/frame_histocam.php?c=1d",
                700, 600,
                "Le contenu provenant d'un site externe, il se peut à tout moment que l'inclusion ne fonctionne plus. " +
                "Dans ce cas, merci de le signaler au webmaster. " +
                "La webcam originale et de nombreuses autres se trouvent sur " +
                "<a href='http://www.lesmosses.net' target='othertab'>www.lesmosses.net</a>"));
    }



    class DashboardTab extends Page implements CacheEventHandler {
        
        private FlexTable ftable = null;

        private FlexTable sessionEvents;
        private VerticalPanel nextOccupation;

        DashboardTab(Page topPage) {
            super(topPage);
            setTitle("Dashboard");
            setShowPath(false);
        }

        @Override
        protected Widget doContentlayout() {
            if (ftable == null) {
                ftable = new FlexTable();

                ftable.setWidth("100%");
                ftable.setCellSpacing(10);
                ftable.insertRow(0);

                // next occupation
                DecoratorPanel dp2 = new DecoratorPanel();
                dp2.setHeight("500px");
                nextOccupation = new VerticalPanel();
                nextOccupation.setWidth("100%");
                nextOccupation.setHeight("100%");
                nextOccupation.setSpacing(40);
                nextOccupation.setVerticalAlignment(VerticalPanel.ALIGN_MIDDLE);
                nextOccupation.setHorizontalAlignment(VerticalPanel.ALIGN_CENTER);
                nextOccupation.add(new HTML("Le chalet est actuellement:"));
                nextOccupation.add(new HTML("<big>LIBRE / OCCUPE (TBD)</big>"));
                nextOccupation.add(new Button("Inscription en un click"));
                ftable.insertCell(0,0);
                ftable.setWidget(0,0, nextOccupation);
                ftable.getCellFormatter().setWidth(0, 0, "50%");
                
                // this session events
                DecoratorPanel dp1 = new DecoratorPanel();
                sessionEvents = new FlexTable();
                sessionEvents.setCellSpacing(0);
                sessionEvents.setWidth("100%");
                sessionEvents.insertRow(0);
                sessionEvents.insertCell(0,0);
                sessionEvents.getFlexCellFormatter().setColSpan(0, 0, 4);
                sessionEvents.setWidget(0, 0, new HTML("Evénements depuis que vous êtes connecté"));
                ScrollPanel scroller = new ScrollPanel(sessionEvents);
                scroller.setHeight("300px");
                dp1.setWidget(scroller);
                ftable.insertCell(0,1);
                ftable.setWidget(0,1, dp1);
                ftable.getCellFormatter().setWidth(0, 1, "50%");
            }

            return ftable;
        }

        public void onCacheEvent(CacheEvent event) {
            if (event.getEventType() == CacheEvent.CacheEventType.UPDATES) {
                for (ObjectRef oref : event.getConcernedObjects()) {
                    Bento bento = CacheManager.get().getCachedObject(oref);
                    BentoDef bentoDef = bento.getDef();
                    sessionEvents.insertRow(1);
                    sessionEvents.insertCell(1,0);
                    sessionEvents.setText(1,0, Formaters.get().formatDate(bento.getLastUpdate()));
                    sessionEvents.insertCell(1,1);
                    sessionEvents.setWidget(1,1, new Hyperlink(bentoDef.getCommonName(bento), "aaa"));
                    sessionEvents.insertCell(1,2);
                    sessionEvents.setText(1,2, "mis à jour");
                }
            }
        }

        @Override
        protected void init() {
            super.init();
            CacheManager.get().registerEventHandler(this);
        }

        @Override
        protected void doUnLayout() {
            super.doUnLayout();
            CacheManager.get().unregisterEventHandler(this);
        }

    }


    public class IFramePage extends Page {

        private VerticalPanel vp;

        public IFramePage(Page parent, String iframeUrl, int iframeWidth, int iframeHeight, String html) {
            super(parent);
            vp = new VerticalPanel();
            vp.setWidth("100%");
            vp.add(new HTML("<iframe name='histocam' src='" + iframeUrl + "' border='0' marginwidth='0' marginheight='0' " +
                    " scrolling='no' width='" + iframeWidth + "' height='" + iframeHeight +
                    "' border='0' frameborder='no'></iframe>"));
            vp.add(new HTML(html));
        }
        
        @Override
        protected Widget doContentlayout() {
            return vp;
        }

    }


}
