/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.repit.rwt.client.ui.form;

import ch.repit.rwt.client.AttributeType;
import ch.repit.rwt.client.BentoAttribute;
import ch.repit.rwt.client.Day;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.datepicker.client.DateBox;
import com.google.gwt.user.datepicker.client.DateBox.DefaultFormat;
import java.util.Date;

/**
 *
 * @author tc149752
 */
public class DateField extends Field {

    private DateBox widget;
    private boolean readOnly = false;


    public DateField(String attributeName) {
        this(attributeName, null);
    }

    public DateField(String attributeName, String label) {
        super(attributeName, label);
        widget = new DateBox();
        widget.setFormat(new DefaultFormat(DateTimeFormat.getFormat("dd.MM.yyyy")));
        final DateField aThis = this;
        widget.addValueChangeHandler(new ValueChangeHandler<Date>() {
            public void onValueChange(ValueChangeEvent<Date> arg0) {
                aThis.onChange();
            }
        });
    }

    @Override
    protected boolean isReadOnly() {
        return readOnly;
    }

    @Override
    protected void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
        widget.setEnabled(!readOnly); // TBD: does not work!!!
    }

    @Override
    protected void setAttribute(BentoAttribute attribute) {
        if (attribute.getAttributeType() == AttributeType.DATE)
            widget.setValue(attribute.getDate());
        else if (attribute.getAttributeType() == AttributeType.DAY)
            if (attribute.getDay() != null && attribute.getDay().getDate() != null)
                widget.setValue(attribute.getDay().getDate());
    }

    @Override
    public void readAttribute(BentoAttribute attribute) {
        if (attribute.getAttributeType() == AttributeType.DATE)
            attribute.set( getValue()==null?null:((Day)getValue()).getDate() );
        else if (attribute.getAttributeType() == AttributeType.DAY)
            attribute.set((Day)getValue());
    }

    @Override
    public Object getValue() {
        // returns 12 am, not midnight... 
        Date dt = widget.getValue();
        if (dt == null)
            return null;
        dt.setHours(12);
        return new Day(dt);
    }

    @Override
    protected Widget getWidget() {
        return widget;
    }

    @Override
    protected void setDefaultValue(Object defaultValue) {
        // does not make sense
    }


    
    //
    // specific methods
    //

}
