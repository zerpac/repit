package ch.repit.rwt.client.security;

import ch.repit.rwt.client.logs.LogManager;
import ch.repit.rwt.client.persistence.CacheEvent;
import ch.repit.rwt.client.persistence.CacheEventHandler;
import ch.repit.rwt.client.persistence.CacheManager;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Manages authentication ONLY on the client site
 *
 * Note: there is a cache event handler, that reloads the principal (and invalidates the actions cache)
 * when either the user or any Role the principal owns is modified
 */
public class SecurityManager implements CacheEventHandler  {

    private static SecurityManager s_instance = null;

    public static SecurityManager get() {
        if (s_instance == null)
            s_instance = new SecurityManager();
        return s_instance;
    }

    private Authorizer authorizer;

    private SecurityManager() {
        // event handler for when anything about the principal or its roles changes
        CacheManager.get().registerEventHandler(this);
    }

    public void authenticateUser(final AuthenticationHandler authHandler)
    {
        AuthenticationServiceAsync authService = GWT.create(AuthenticationService.class);
        authService.getConnectedPrincipal(authHandler==null?null:getBrowserInfo(), new AsyncCallback<Principal>() {
            public void onFailure(Throwable exception) {
                if (exception instanceof AuthenticationException) {
                    AuthenticationException ae = (AuthenticationException)exception;
                    LogManager.get().warning("L'utilisateur n'est pas dans la base, et n'est pas un admin GAE : " + ae.getNickname());
                    if (authHandler != null)
                        authHandler.onAuthenticationFailed(exception);
                } else {
                    LogManager.get().error("Echec à l'authentification de l'utilisateur", exception);
                    if (authHandler != null)
                        authHandler.onAuthenticationFailed(exception);
                }
            }
            public void onSuccess(Principal newPrincipal) {
                LogManager.get().debug("Utilisateur authentifié : " + newPrincipal.getNickName());
                setPrincipal(newPrincipal);
                if (authHandler != null)
                    authHandler.onUserAuthenticated(newPrincipal);
            }
        });
    }


    public Authorizer getAuthorizer() {
        return authorizer;
    }

    
    public void onCacheEvent(CacheEvent event) {
        if (event != null &&
               CacheEvent.CacheEventType.UPDATES.equals(event.getEventType()) ) {
            Principal p = authorizer.getPrincipal();
            if (p == null ||
                    event.getConcernedObjects().contains(p.getUserRef()) ||    // if user changed
                    event.getConcernedTypes().contains(RoleDef.TYPE) ) {       // if any role changed
                LogManager.get().debug("User modified, reauthenticating...");
                authenticateUser(null);
            }
        }
    }

    private void setPrincipal(Principal principal) {
        authorizer = new Authorizer(principal);
        CacheManager.get().activate();
        LogManager.get().debug("User authentifié : nickname=" + principal.getNickName() +
                            "; displayName=" + principal.getDisplayName() +
                            "; ObjectRef=" + principal.getUserRef() +
                            "; Roles=" + principal.getRoles());               
    }


    private native static String getBrowserInfo() /*-{
        return navigator.userAgent;
    }-*/;
}
