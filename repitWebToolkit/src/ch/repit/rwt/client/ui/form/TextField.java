/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.repit.rwt.client.ui.form;

import ch.repit.rwt.client.BentoAttribute;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.TextBoxBase;
import com.google.gwt.user.client.ui.Widget;

/**
 *
 * @author tc149752
 */
public class TextField extends Field {

    TextBoxBase widget;
    private int columns = 30;
    private int rows = 1;


    public TextField(String attributeName) {
        this(attributeName, null, 1);
    }


    public TextField(String attributeName, String label) {
        this(attributeName, label, 1);
    }

    public TextField(String attributeName, String label, int rows) {
        super(attributeName, label);
        this.rows = rows;
        if (rows == 1) {
            widget = new TextBox();
            ((TextBox)widget).setMaxLength(columns);
        } else {
            widget = new TextArea();
            ((TextArea)widget).setVisibleLines(rows);
        }
        final TextField aThis = this;
        widget.addChangeHandler(new ChangeHandler() {
            public void onChange(ChangeEvent arg0) {
                aThis.onChange();
            }
        });
        widget.addKeyPressHandler(new KeyPressHandler() {
            public void onKeyPress(KeyPressEvent arg0) {
                aThis.onChange();
                // NTH: could compare the value and only trigger event if the value actually changed...
            }
        });
        setColumns(columns);
    }
    
    @Override
    protected boolean isReadOnly() {
        return widget.isReadOnly();
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        widget.setReadOnly(readOnly);
    }

    public void setDefaultValue(Object defaultValue) {
        if (defaultValue != null)
            widget.setText(defaultValue.toString());
    }

    @Override
    protected void setAttribute(BentoAttribute attribute) {
        if (attribute.getDisplayValue() != null)
            widget.setText(attribute.getDisplayValue());
    }

    @Override
    protected void readAttribute(BentoAttribute attribute) {
        attribute.set((String)getValue());
    }

    @Override
    public Object getValue() {
        String text = widget.getText();
        if (text != null) {
            text = text.trim();
            if (text.length() == 0)
                text = null;
        }
        return text;
    }

    @Override
    protected Widget getWidget() {
        return widget;
    }


    
    //
    // specific methods
    //

    public int getColumns() {
        return columns;
    }

    public void setColumns(int columns) {
        this.columns = columns;
        if (rows == 1)
            ((TextBox)widget).setMaxLength(columns);
        else
            ((TextArea)widget).setCharacterWidth(columns);
        widget.setWidth("" + (6+ columns * 6) + "px");
    }

    public int getRows() {
        return rows;
    }


    public void setText(String text) {
        widget.setText(text);
    }


    // setRows would mean switching widget class....

}
