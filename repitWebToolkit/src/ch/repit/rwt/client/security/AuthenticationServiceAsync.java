/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.repit.rwt.client.security;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 *
 * @author tc149752
 */
public interface AuthenticationServiceAsync {

    public void getConnectedPrincipal(String browserInfo, AsyncCallback<Principal> callback);

}
