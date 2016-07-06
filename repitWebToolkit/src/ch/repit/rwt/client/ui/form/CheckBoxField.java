/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.repit.rwt.client.ui.form;

import ch.repit.rwt.client.BentoAttribute;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Widget;

/**
 *
 * @author tc149752
 */
public class CheckBoxField extends Field {

    private CheckBox widget;


    public CheckBoxField(String attributeName) {
        this(attributeName, null);
    }

    public CheckBoxField(String attributeName, String label) {
        super(attributeName, label);
        widget = new CheckBox();
        final CheckBoxField aThis = this;
        widget.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            public void onValueChange(ValueChangeEvent<Boolean> arg0) {
                aThis.onChange();
            }
        });
    }
    

    @Override
    protected boolean isReadOnly() {
        return !widget.isEnabled();
    }

    @Override
    protected void setReadOnly(boolean readOnly) {
        widget.setEnabled(!readOnly);
    }

    @Override
    protected void setAttribute(BentoAttribute attribute) {
        if (attribute.getBoolean() != null)
            widget.setValue(attribute.getBoolean());
    }

    @Override
    protected void readAttribute(BentoAttribute attribute) {
        attribute.set((Boolean)getValue());
    }

    @Override
    public Object getValue() {
        return widget.getValue();
    }

    @Override
    public Widget getWidget() {
        return widget;
    }

    @Override
    protected void setDefaultValue(Object defaultValue) {
        if (defaultValue != null)
            widget.setValue((Boolean)defaultValue);
    }


    
    //
    // specific methods
    //

}
