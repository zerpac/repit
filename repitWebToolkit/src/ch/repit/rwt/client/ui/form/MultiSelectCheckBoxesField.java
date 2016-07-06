/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.repit.rwt.client.ui.form;

import ch.repit.rwt.client.BentoAttribute;
import ch.repit.rwt.client.logs.LogManager;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Widget;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Same as MultiselectField, but displays the options as checkboxes
 */
public class MultiSelectCheckBoxesField extends MultiSelectField {

    private HorizontalPanel widget;

    private List<CheckBox> checkBoxes;
    private Map<CheckBox,String> availableItems;
    private boolean readonly = false;
    private ValueChangeHandler valueChangeHandler;
    

    public MultiSelectCheckBoxesField (String attributeName) {
        this(attributeName, null);
    }

    public MultiSelectCheckBoxesField (String attributeName, String label) {
        super(attributeName, label);

        checkBoxes = new ArrayList<CheckBox>();
        availableItems = new HashMap<CheckBox,String>();
        widget = new HorizontalPanel();

        final MultiSelectField aThis = this;
        valueChangeHandler = new ValueChangeHandler<Boolean>() {
            public void onValueChange(ValueChangeEvent<Boolean> arg0) {
                aThis.onChange();
            }
        };
    }
    

    @Override
    protected boolean isReadOnly() {
        return readonly;
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        this.readonly = readOnly;
        for (CheckBox cb : checkBoxes) {
            cb.setEnabled(!readOnly);
        }
    }

    @Override
    protected void setAttribute(BentoAttribute attribute) {
        if (attribute != null) {
            setValues(attribute.getStringList());
        }
    }

    @Override
    protected void readAttribute(BentoAttribute attribute) {
        attribute.set((List<String>)getValue());
    }

    @Override
    public Object getValue() {
        List<String> values = new ArrayList<String>();
        for (CheckBox cb : checkBoxes) {
            if (cb.getValue() == Boolean.TRUE) {
                values.add(availableItems.get(cb));
            }
        }
        return values;
    }

    @Override
    protected Widget getWidget() {
        return widget;
    }



    @Override
    public void setDefaultValue(Object defaultValue) {
        // TBD: should act only if empty
        if (defaultValue == null) {
            setValues( new ArrayList() );
        } else if (defaultValue instanceof List)
            setValues( (List) defaultValue );
        else if (defaultValue instanceof String) {
            List l = new ArrayList();
            l.add(defaultValue);
            setValues( l );
        } else {
            LogManager.get().error("Type inattendu lors de l'initialisation du MultiSelectCheckBoxField : " + defaultValue);
        }
    }
    
    
    //
    // specific methods
    //

    @Override
    public void setAvailableValues(List<String> valueList) {
        checkBoxes.clear();
        for (String itemText : valueList) {
            addAvailableValue(itemText,itemText);
        }
        createWidget();
    }

    @Override
    public void setAvailableValues(Map<String,String> valueMap) {
        checkBoxes.clear();
        for (String itemText : valueMap.keySet()) {
            addAvailableValue(valueMap.get(itemText),itemText);
        }
        createWidget();
    }

    //
    // private methods
    //

    private void addAvailableValue(String key, String label) {
        CheckBox cb;
        checkBoxes.add(cb = new CheckBox(label));
        availableItems.put(cb, key);
        cb.addValueChangeHandler(valueChangeHandler);
    }

    /**
     * will have no effect for values not present in available values list
     */
    private void setValues(List<String> values) {
        for (CheckBox cb : checkBoxes) {
            if ( (values == null) ||
                 (cb.getValue() == Boolean.TRUE && !values.contains(availableItems.get(cb)))) {
                cb.setValue(Boolean.FALSE);
            } else if (cb.getValue() == Boolean.FALSE && values.contains(availableItems.get(cb))) {
                cb.setValue(Boolean.TRUE);
            }
        }
    }

    private void createWidget() {
        widget.clear();
        for (CheckBox cb : checkBoxes) {
            widget.add(cb);
        }
    }

}
