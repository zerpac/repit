/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.repit.rwt.client.ui;

import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

/**
 *
 * @author tc149752
 */
public class HelpPage extends Page {

    private String helpText;


    public HelpPage(Page topPage, String helpText) {
        super(topPage);
        this.helpText = helpText;
    }

    @Override
    protected Widget doContentlayout() {
        HTML htmlHelp = new HTML(helpText);
        return htmlHelp;
    }

}
