/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.repit.rwt.client.security;

/**
 * Interface that defines method to be called when authentication is performed.
 * Used mainly by the Site class.
 */
public interface AuthenticationHandler {

    public void onUserAuthenticated(Principal principal);

    public void onAuthenticationFailed(Throwable exception);

}
