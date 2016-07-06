/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.repit.rwt.client.ui.form;

import ch.repit.rwt.client.BentoAttribute;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/**
 *
 * @author tc149752
 */
public class BooleanDropDownField extends SelectField {

    public BooleanDropDownField(String attributeName, String label, String trueLabel, String falseLabel, String nullLabel) {
        super(attributeName, label, false);
        TreeMap<String,String> values = new TreeMap();
        values.put(trueLabel, Boolean.TRUE.toString());
        values.put(falseLabel, Boolean.FALSE.toString());
        if (nullLabel != null) {
            values.put(nullLabel, "null");
        }
        setValueMap(values);
    }



    @Override
    protected void setAttribute(BentoAttribute attribute) {
        if (attribute != null) {
            List<String> attrs = new ArrayList<String>();
            attrs.add(""+attribute.getBoolean());

            if (widget.getItemCount() == 0) {
                setValueList(attrs);
            }
            for (int i = 0; i < widget.getItemCount(); i++) {
                widget.setItemSelected(i, (attrs.contains(widget.getValue(i))));
            }
        }
    }

    @Override
    protected void readAttribute(BentoAttribute attribute) {
        attribute.set((Boolean)getValue());
    }

    @Override
    public Object getValue() {
        String val = (String)super.getValue();
        Boolean aval = null;
        if (!val.equals("null"))
            aval = Boolean.parseBoolean(val);
        return aval;
    }


}
