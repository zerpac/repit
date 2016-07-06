
package ch.repit.rwt.client.ui;

import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * A special page that can contain mana pages
 */
public class CompositePage extends Page {

    private Page[] innerPages;

    public CompositePage(Page parentPage, String title, Page... innerPages) {
        super(parentPage);
        super.setTitle(title);
        this.innerPages = innerPages;
    }

    @Override
    protected VerticalPanel doLayout() {
        VerticalPanel composite = new VerticalPanel();
        composite.setStylePrimaryName("rwt-page");

        for (Page p : innerPages) {
            if (p!= null) {
                // adds the title
                HorizontalPanel bar = new HorizontalPanel();
                bar.setStylePrimaryName("rwt-pageToolBar");
                Label tit = new Label(p.getTitle());
                tit.setStylePrimaryName("rwt-pageToolBarItem");
                bar.add(tit);composite.add(bar);

                // adds the content
                VerticalPanel vp = p.doLayout();
                vp.removeStyleName("rwt-page");
                vp.setStylePrimaryName("rwt-pageMenuBar"); // just for the 100%...
                composite.add(vp);
            }
        }

        return composite;
    }



}
