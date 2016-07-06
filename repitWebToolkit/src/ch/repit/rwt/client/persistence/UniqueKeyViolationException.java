/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.repit.rwt.client.persistence;

import ch.repit.rwt.client.RwtException;



/**
 *
 * @author tc149752
 */
public class UniqueKeyViolationException extends RwtException {

    String attributeName, attributeValue;

    public UniqueKeyViolationException() {
        super();
    }


    public UniqueKeyViolationException(String attributeName, String attributeValue) {
        super("The attribute " + attributeName + " has not a unique value of " + attributeValue);
        this.attributeName = attributeName;
        this.attributeValue = attributeValue;
    }

    public String getAttributeName() {
        return attributeName;
    }

    public String getAttributeValue() {
        return attributeValue;
    }

    

}
