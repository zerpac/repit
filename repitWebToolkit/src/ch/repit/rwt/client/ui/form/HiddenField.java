/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.repit.rwt.client.ui.form;

import ch.repit.rwt.client.BentoAttribute;
import com.google.gwt.user.client.ui.Widget;

/**
 *
 * @author tc149752
 */
public class HiddenField extends Field {

    public HiddenField(String attributeName) {
        super(attributeName, null);
    }

    @Override
    protected Widget getWidget() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected boolean isReadOnly() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void setReadOnly(boolean readOnly) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void setDefaultValue(Object defaultValue) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void setAttribute(BentoAttribute attribute) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void readAttribute(BentoAttribute attribute) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Object getValue() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
