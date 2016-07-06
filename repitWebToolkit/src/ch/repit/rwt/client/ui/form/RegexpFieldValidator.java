/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.repit.rwt.client.ui.form;

import java.util.List;

/**
 *
 * @author tc149752
 */

public class RegexpFieldValidator implements FieldValidator {

    String pattern, failureMessage;
    
    public RegexpFieldValidator(String pattern, String failureMessage) {
        this.pattern = pattern;
        this.failureMessage = failureMessage;
    }

    public boolean onValidate(List<String> messages, Object value, String attributeTitle) {
        String val = (String)value;
        if (val == null || val.trim().length() == 0)
            return true;
        val = val.trim();
        if (!val.matches(pattern)) {
            messages.add(attributeTitle + " : " + failureMessage);
            return false;
        }
        return true;
    }

}
