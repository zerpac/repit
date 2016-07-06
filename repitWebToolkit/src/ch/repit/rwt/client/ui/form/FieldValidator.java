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
public interface FieldValidator {

    /**
     *
     * @param messages a list of messages that the validator can fill to add info
     * @param value the value to validate
     * @param attributeTitle
     * @return false if the field is not validated, true otherwise
     */
    public boolean onValidate(List<String> messages, Object value, String attributeTitle);
}
