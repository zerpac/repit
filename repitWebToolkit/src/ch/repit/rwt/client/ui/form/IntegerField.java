/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.repit.rwt.client.ui.form;

import ch.repit.rwt.client.BentoAttribute;

/**
 *
 * @author tc149752
 */
public class IntegerField extends TextField {

    public IntegerField(String attributeName) {
        this(attributeName, null);
    }

    public IntegerField(String attributeName, String label) {   // could add a range...
        super(attributeName, label);
        super.setColumns(8);
        super.addValidator(new RegexpFieldValidator("\\d*", "Doit être un entier positif"));
    }

    @Override
    protected void readAttribute(BentoAttribute attribute) {
        if (getValue() != null && ((String)getValue()).length() > 0)
            attribute.set(Integer.parseInt((String)getValue()));  // NumberFormatEx avoided because of validator
        else
            attribute.set((Integer)null);
    }

    @Override
    protected void setAttribute(BentoAttribute attribute) {
        if (attribute.getInteger() != null)
            widget.setText("" + attribute.getInteger());
        else
            widget.setText("");
    }

}
