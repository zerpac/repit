/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.repit.rwt.client.ui.form;

import ch.repit.rwt.client.BentoAttribute;
import ch.repit.rwt.client.logs.LogManager;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 */
public class MultiSelectField extends Field {

    private int MULTISELECT_MAX_SIZE = 10;

    private ListBox availableList;
    private ListBox selectedList;
    private VerticalPanel buttonsPanel;
    private VerticalPanel availablePanel;
    private HorizontalPanel widget;

    private Map<String,String> availableItems;
    private boolean readonly = false;

    public MultiSelectField (String attributeName) {
        this(attributeName, null, null, null);
    }
    public MultiSelectField (String attributeName, String label) {
        this(attributeName, label, null, null);
    }

    /**
     * Normall either use label or the two others combined, not the 3 of them
     * @param attributeName
     * @param label
     * @param availableLabel
     * @param selectedLabel
     */
    public MultiSelectField (String attributeName, String label, String availableLabel, String selectedLabel) {
        super(attributeName, label);

        final MultiSelectField aThis = this;

        widget = new HorizontalPanel();
        availablePanel = new VerticalPanel();
        if (availableLabel != null) {
            HTML labelw = new HTML(availableLabel);
            labelw.addStyleName("rwt-formFieldLabel");
            availablePanel.add(labelw);
        }
        availableList = new ListBox(true);
        availableList.setVisibleItemCount(MULTISELECT_MAX_SIZE);
        availablePanel.add(availableList);

        buttonsPanel = new VerticalPanel();
        buttonsPanel.setVerticalAlignment(VerticalPanel.ALIGN_MIDDLE);

        PushButton addButton = new PushButton(new Image(GWT.getModuleBaseURL()+"icons/nav_next.gif"));
        addButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent arg0) {
                boolean anyChange = false;
                for (int i=availableList.getItemCount()-1; i>=0; i--) {
                    if (availableList.isItemSelected(i)) {
                        String itemText = availableList.getItemText(i);
                        String itemValue = availableList.getValue(i);
                        availableList.removeItem(i);
                        selectedList.addItem(itemText, itemValue);
                        anyChange = true;
                    }
                }
                if (anyChange) {
                    // set the list sizes
                    setListBoxSize();
                    aThis.onChange();
                }
            }
        });
        buttonsPanel.add(addButton);

        PushButton removeButton = new PushButton(new Image(GWT.getModuleBaseURL()+"icons/nav_prev.gif"));
        removeButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent arg0) {
                boolean anyChange = false;
                for (int i=selectedList.getItemCount()-1; i>=0; i--) {
                    if (selectedList.isItemSelected(i)) {
                        String itemText = selectedList.getItemText(i);
                        String itemValue = selectedList.getValue(i);
                        selectedList.removeItem(i);
                        if (availableItems.containsKey(itemValue))
                            availableList.addItem(itemText, itemValue);
                        anyChange = true;
                    }
                }
                if (anyChange) {
                    // set the list sizes
                    setListBoxSize();
                    aThis.onChange();
                }
            }
        });
        buttonsPanel.add(removeButton);
        buttonsPanel.setSpacing(5);

        VerticalPanel selectedPanel = new VerticalPanel();
        if (selectedLabel != null) {
            HTML labelw = new HTML(selectedLabel);
            labelw.addStyleName("rwt-formFieldLabel");
            selectedPanel.add(labelw);
        }
        selectedList = new ListBox(true);
        selectedList.setVisibleItemCount(MULTISELECT_MAX_SIZE);
        selectedPanel.add(selectedList);
        widget.add(availablePanel);
        widget.add(buttonsPanel);
        widget.setCellVerticalAlignment(buttonsPanel, VerticalPanel.ALIGN_MIDDLE);
        widget.add(selectedPanel);
    }
    

    @Override
    protected boolean isReadOnly() {
        return readonly;
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        this.readonly = readOnly;
        selectedList.setEnabled(!readOnly);
        availablePanel.setVisible(!readOnly);
        buttonsPanel.setVisible(!readOnly);
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
        for (int i = 0; i < selectedList.getItemCount(); i++) {
            values.add(selectedList.getValue(i));
        }
        return values;
    }

    @Override
    protected Widget getWidget() {
        return widget;
    }



    @Override
    public void setDefaultValue(Object defaultValue) {
        if (selectedList.getItemCount() == 0) {
            if (defaultValue == null) {
                setValues( new ArrayList() );
            } else if (defaultValue instanceof List)
                setValues( (List) defaultValue );
            else if (defaultValue instanceof String) {
                List l = new ArrayList();
                l.add(defaultValue);
                setValues( l );
            } else {
                LogManager.get().error("Type inattendu lors de l'initialisation du MultiSelectField : " + defaultValue);
            }
        }
    }
    
    
    //
    // specific methods
    //

    public void setAvailableValues(List<String> valueList) {
        availableItems = new HashMap<String,String>();
        for (String itemText : valueList) {
            availableList.addItem(itemText);
            availableItems.put(itemText,itemText);
        }
    }

    public void setAvailableValues(Map<String,String> valueMap) {
        availableItems = new HashMap<String,String>();
        for (String itemText : valueMap.keySet()) {
            availableList.addItem(itemText, valueMap.get(itemText));
            availableItems.put(valueMap.get(itemText),itemText);
        }
    }


    //
    // private methods
    //

    private void setListBoxSize() {
        int size = Math.min(MULTISELECT_MAX_SIZE,
                            Math.max(selectedList.getItemCount(), availableList.getItemCount()) );
        availableList.setVisibleItemCount(size);
        selectedList.setVisibleItemCount(size);
    }

    private void setValues(List<String> values) {
        List<String> vals;
        if (values != null && values.size() > 0)
            vals = new ArrayList<String>(values);
        else
            vals = new ArrayList<String>();
        selectedList.clear();
        availableList.clear();
        if (availableItems != null) {
            for (String itemValue : availableItems.keySet()) {
                String itemText = availableItems.get(itemValue);
                if (vals.contains(itemValue)) {
                    selectedList.addItem(itemText, itemValue);
                    vals.remove(itemValue);
                } else {
                    availableList.addItem(itemText, itemValue);
                }
            }
        }
        if (!vals.isEmpty()) {
            for (String itemValue : vals) {
                 selectedList.addItem("(" + itemValue + ")", itemValue);
            }
        }

        // set the list sizes
        if (!readonly)
            setListBoxSize();
        else
            selectedList.setVisibleItemCount( Math.min(MULTISELECT_MAX_SIZE, selectedList.getItemCount()));
    }



}
