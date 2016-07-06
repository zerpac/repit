/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.repit.rwt.server;

import java.io.Serializable;

/**
 *
 * @author tc149752
 */
public class RwtRuntimeException extends RuntimeException implements Serializable {

    public RwtRuntimeException() {}

    public RwtRuntimeException(String message) {
        super(message);
    }

    public RwtRuntimeException(String message, ClassNotFoundException ex) {
        super(message, ex);
    }

}
