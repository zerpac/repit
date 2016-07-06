/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.repit.rwt.server.persistence;

import ch.repit.rwt.client.persistence.ValidationException;
import ch.repit.rwt.client.Bento;

/**
 *
 * @author tc149752
 */
public interface Validator {

    public void validate(Bento object) throws ValidationException;
 
}
