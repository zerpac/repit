/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.repit.rwt.client.security;

/**
 *
 * @author tc149752
 */
public class AuthorizationException extends SecurityException {

    private String actionName;
    private String objectType;

    private AuthorizationException() {
        super();
    }

    public AuthorizationException(String message) {
        super(message);
    }

    public AuthorizationException(String actionName, String objectType) {
        super("Not authorized to " + actionName + " objects of type " + objectType);
        this.actionName = actionName;
        this.objectType = objectType;
    }

    public String getActionName() {
        return actionName;
    }

    public String getObjectType() {
        return objectType;
    }



}
