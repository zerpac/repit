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
public class MissingMandatoryAttributeException extends RwtException {

    private String missingAttribute;

    public MissingMandatoryAttributeException() {
        
    }

    public MissingMandatoryAttributeException(String attributeName) {
        super("Missing an attribute declared mandatory:" + attributeName);
        missingAttribute = attributeName;
    }

    public String getMissingAttribute() {
        return missingAttribute;
    }

}
