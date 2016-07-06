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
public class ObjectNotFoundException extends RwtException {

    public ObjectNotFoundException() {
        
    }

    public ObjectNotFoundException(String message) {
        super(message);
    }

    public ObjectNotFoundException(String message, Exception e) {
        super(message, e);
    }
}
