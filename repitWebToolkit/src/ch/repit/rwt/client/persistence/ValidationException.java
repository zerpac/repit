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
public class ValidationException  extends RwtException {

    public ValidationException() {

    }

    public ValidationException(String message) {
        super(message);
    }

}
