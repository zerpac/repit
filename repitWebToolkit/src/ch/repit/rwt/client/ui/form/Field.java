package ch.repit.rwt.client.ui.form;

import ch.repit.rwt.client.BentoAttribute;
import ch.repit.rwt.client.logs.LogManager;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author tc149752
 */
public abstract class Field {

    private String attributeName;
    private String label;
    private boolean mandatory;
    private List<FieldValidator> validators = new ArrayList<FieldValidator>();
    private List<FieldChangeHandler> changeHandlers = new ArrayList<FieldChangeHandler>();
    private boolean visible = true;
    private boolean ignored = false;

    VerticalPanel attributePanel; // keep it to change the style when validation errors

    public Field(String attributeName, String label) {
        this.attributeName = attributeName;
        this.label = label;
    }

    public String getAttributeName() {
        return attributeName;
    }

    // or is it tooltip?
    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    protected boolean isMandatory() {
        return mandatory;
    }

    protected void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }


    protected abstract Widget getWidget();

    protected abstract boolean isReadOnly();

    protected abstract void setReadOnly(boolean readOnly);

    protected abstract void setDefaultValue(Object defaultValue);

    protected abstract void setAttribute(BentoAttribute attribute);

    protected abstract void readAttribute(BentoAttribute attribute);

    public abstract Object getValue();

    
    // called on submit of the page, to validate the field's content
    public void addValidator(FieldValidator validator) {
        validators.add(validator);
    }

    // called when the value of the field has changed
    public void addChangeHandler(FieldChangeHandler changeHandler) {
        changeHandlers.add(changeHandler);
    }

    // checks the mandatory and applies validators
    public boolean validate(List<String> messages, String attributeTitle) {
        boolean noError = true;

        String attrTitle = attributeTitle;
            if (getLabel() != null && getLabel().trim().length() > 0)
                attrTitle += " (" + getLabel() + ")";
        
        // validate mandatory field
        if (isMandatory() && getValue() == null) {
            messages.add(attrTitle + " : champ obligatoire");
            noError = false;
        }

        // dispatch to custom validators
        for (FieldValidator v : validators) {
            if (!v.onValidate(messages, getValue(), attrTitle))
                noError = false;
        }

        // display the attribute in another color
        if (!noError)
            attributePanel.addStyleDependentName("validationError");
        else
            attributePanel.removeStyleDependentName("validationError");

        return noError;
    }

    public Panel doLayout() {
        attributePanel = new VerticalPanel();
        attributePanel.setVisible(visible);
        HorizontalPanel panel = new HorizontalPanel();
        if (this.getWidget() != null) {
            panel.add(this.getWidget());
            if (isMandatory()) {
                panel.add(new HTML("*"));
            }
            if (getLabel() != null && getLabel().trim().length() > 0) {
                HTML labelw = new HTML(getLabel());
                labelw.addStyleName("rwt-formFieldLabel");
                attributePanel.add(labelw);
            }
            attributePanel.addStyleName("rwt-formAttributePanel");
            attributePanel.add(panel);
        }
        return attributePanel;
    }

    
    /**
     * Invokes change handlers when the field value is changed. To be invoked
     * by custom change handler
     */
    protected final void onChange() {
        for (FieldChangeHandler h : changeHandlers) {
            try {
                h.onChange();
            } catch (Exception e) {
                LogManager.get().error("Exception in onChange for " + h, e);
            }
        }
    }

    public void setVisible(boolean b) {
        visible = b;
        if (attributePanel != null)
            attributePanel.setVisible(b);
    }

    public void setIgnore(boolean b) {
        ignored = b;
    }
    public boolean isIgnored() {
        return ignored;
    }

    // we are really interested only in event, not old or new value
    public interface FieldChangeHandler {
        public void onChange();
    }

}
