/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.repit.rwt.client.security;

import ch.repit.rwt.client.RwtException;
import java.io.Serializable;

/**
 *
 * @author tc149752
 */
public class SecurityException extends RwtException implements Serializable {

    public SecurityException() {}

    public SecurityException(String message) {
        super(message);
    }

}
