/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.repit.rwt.client;

import java.io.Serializable;

/**
 *
 * @author tc149752
 */
public class RwtException extends Exception implements Serializable {

    public RwtException() {}

    public RwtException(String message) {
        super(message);
    }

    public RwtException(String message, Exception e) {
        super(message, e);
    }
}
