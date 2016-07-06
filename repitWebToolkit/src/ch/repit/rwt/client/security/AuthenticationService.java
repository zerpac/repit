/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.repit.rwt.client.security;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;


/**
 * Service interface for security management, especially authentication
 */
@RemoteServiceRelativePath("authenticationService")
public interface AuthenticationService extends RemoteService {

    public Principal getConnectedPrincipal(String browserInfo) throws AuthenticationException;


}
