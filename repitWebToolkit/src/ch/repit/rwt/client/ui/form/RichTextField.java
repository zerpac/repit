/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.repit.rwt.client.ui.form;

import ch.repit.rwt.client.BentoAttribute;
import ch.repit.rwt.client.ui.form.richtexttoolbar.RichTextToolbar;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.RichTextArea;
import com.google.gwt.user.client.ui.Widget;

/**
 *
 * @author tc149752
 */
public class RichTextField extends Field {

    private Grid grid;
    private RichTextArea area;

    public RichTextField(String attributeName, String label) {
        super(attributeName, label);
        
        // Create the text area and toolbar
        area = new RichTextArea();
        area.setSize("50em", "24em");
        area.setStylePrimaryName("rwt-RichTextArea");
        RichTextToolbar toolbar = new RichTextToolbar(area);

        // Add the components to a panel
        grid = new Grid(2, 1);
        grid.setStyleName("rwt-RichText");
        grid.setWidget(0, 0, toolbar);
        grid.setWidget(1, 0, area);
        grid.setWidth("100%");
        final Field aThis = this;
        area.addKeyPressHandler(new KeyPressHandler() {
            public void onKeyPress(KeyPressEvent arg0) {
                aThis.onChange();
            }
        });
        area.setHTML("");  // to avoid exception when saving a blog without editing the body
    }

    @Override
    protected Widget getWidget() {
        return grid;
    }

    @Override
    protected boolean isReadOnly() {
        return false;
    }

    @Override
    protected void setReadOnly(boolean readOnly) {
        
    }

    @Override
    protected void setAttribute(BentoAttribute attribute) {
        String disp = attribute.getDisplayValue();
        area.setHTML(disp!=null?disp:"");
    }

    @Override
    protected void readAttribute(BentoAttribute attribute) {
        attribute.set((String)getValue());
    }

    @Override
    public Object getValue() {
        String text = area.getHTML();
        if (text != null) {
            text = text.trim();
            if (text.length() == 0)
                text = null;
        }

        // adds the target in links, so that it opens in another tab
        text = text.replaceAll("<[a|A] href=", "<A target='otherTab' href=");

        return text;
    }

    @Override
    protected void setDefaultValue(Object defaultValue) {
        if (defaultValue != null)
            area.setHTML(defaultValue.toString());
    }


    // specific

    public void setText(String text) {
        area.setHTML(text);
    }

    public void setWidth(String width) {
        area.setWidth(width);
    }



}
