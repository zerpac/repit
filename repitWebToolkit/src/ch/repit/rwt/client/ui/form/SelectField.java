/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.repit.rwt.client.ui.form;

import ch.repit.rwt.client.BentoAttribute;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;

/**
 *
 * @author tc149752
 */
public class SelectField extends Field {

    private int MULTISELECT_MAX_SIZE = 10;

    ListBox widget;
    private boolean multiSelect;

    public SelectField (String attributeName, boolean multiSelect) {
        this(attributeName, null, multiSelect);
    }

    public SelectField (String attributeName, String label, boolean multiSelect) {
        super(attributeName, label);
        this.multiSelect = multiSelect;

        if (multiSelect) {
            widget = new ListBox(true);
            widget.setVisibleItemCount(MULTISELECT_MAX_SIZE);
        } else {
            widget = new ListBox(false);
            widget.setVisibleItemCount(1);
        }
           
        final SelectField aThis = this;
        widget.addChangeHandler(new ChangeHandler() {
            public void onChange(ChangeEvent arg0) {
                aThis.onChange();
            }
        });
    }
    

    @Override
    protected boolean isReadOnly() {
        return !widget.isEnabled();
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        widget.setEnabled(!readOnly);
    }

    @Override
    protected void setAttribute(BentoAttribute attribute) {
        if (attribute != null) {
            List<String> attrs;
            if (multiSelect)
                attrs = attribute.getStringList();
            else {
                attrs = new ArrayList<String>();
                attrs.add(attribute.getString());
            }
            if (attrs != null) {
                if (widget.getItemCount() == 0) {
                    setValueList(attrs);
                } // if readonly, we could also display only the ones
                for (int i = 0; i < widget.getItemCount(); i++) {
                    widget.setItemSelected(i, (attrs.contains(widget.getValue(i))));
                }
            }
        }
    }

    @Override
    protected void readAttribute(BentoAttribute attribute) {
        if (multiSelect)
            attribute.set((List<String>)getValue());
        else
            attribute.set((String)getValue());
    }

    @Override
    public Object getValue() {
        List<String> result = new ArrayList<String>();
        for (int i = 0; i < widget.getItemCount(); i++) {
            if (widget.isItemSelected(i)) {
                result.add(widget.getValue(i));
            }
        }
        if (multiSelect)
            return result;
        else
            return result.get(0);
    }

    @Override
    protected Widget getWidget() {
        return widget;
    }


    
    //
    // specific methods
    //

    public void setValueList(List<String> valueList) {
        if (multiSelect)
            widget.setVisibleItemCount(Math.min(valueList.size(), MULTISELECT_MAX_SIZE));
        for (String itemText : valueList) {
            widget.addItem(itemText);
        }
    }

    public void setValueMap(SortedMap<String,String> valueMap) {
        if (multiSelect)
            widget.setVisibleItemCount(Math.min(valueMap.size(), MULTISELECT_MAX_SIZE));
        for (String itemText : valueMap.keySet()) {
            widget.addItem(itemText, valueMap.get(itemText));
        }
    }

    @Override
    protected void setDefaultValue(Object defaultValue) {
        if (defaultValue == null)
            return;
        String defaultStr = defaultValue.toString();
        for (int i = 0; i<widget.getItemCount(); i++) {
            if (widget.getValue(i) != null && defaultStr.equals(widget.getValue(i)) ) {
                widget.setSelectedIndex(i);
                return;
            }
        }
    }
}
