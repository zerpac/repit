/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.repit.rwt.server;

import ch.repit.rwt.client.Bento;
import ch.repit.rwt.client.BentoDefFactory;
import ch.repit.rwt.client.BentoInitializer;
import ch.repit.rwt.client.BentoStatus;
import ch.repit.rwt.client.ObjectRef;
import ch.repit.rwt.client.persistence.ObjectNotFoundException;
import ch.repit.rwt.client.security.Action;
import ch.repit.rwt.client.security.SecurityException;
import ch.repit.rwt.client.security.AuthenticationException;
import ch.repit.rwt.client.security.AuthorizationException;
import ch.repit.rwt.client.security.Authorizer;
import ch.repit.rwt.client.security.Principal;
import ch.repit.rwt.client.security.RescueAdminPrincipal;
import ch.repit.rwt.client.security.RoleDef;
import ch.repit.rwt.client.util.Formaters;
import ch.repit.rwt.server.persistence.DataObjectMapper;
import ch.repit.rwt.server.persistence.JdoHelper;
import ch.repit.rwt.server.persistence.PMF;
import ch.repit.rwt.server.security.PrincipalDO;
import ch.repit.rwt.server.security.Role;
import ch.repit.rwt.server.util.Logging;
import ch.repit.rwt.server.util.ServerFormaters;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.jdo.PersistenceManager;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

/**
 * common servlet layer keeping reference of the principal, fetching it if it
 * disappearred (e.g. server restart)
 */
public class RwtRemoteServiceServlet extends RemoteServiceServlet {

    private static Logging LOG = new Logging(RwtRemoteServiceServlet.class.getName());
    
    public static final String PRINCIPAL_SESSION_KEY = "ch.repit.rwt.Authorizer";
    private static final String NICKNAME_PARAM_KEY = "nickNameAttribute";
    private static final String PRINCIPAL_IMPL_PARAM_KEY = "principalImpl";


    private Class principalImplClass;
    private String nickNameAttribute;

    
    @Override
    public void init(ServletConfig conf) throws ServletException
    {
        super.init(conf);
        String method = "init";
        LOG.enter(method);

        // Have to init the server-side formater.
        // TBD: group those inits in a common part ?
        if (Formaters.get() == null)
            Formaters.register(new ServerFormaters());


        // init all bento defs
        String bentoFacInit = conf.getServletContext().getInitParameter("bentoInitializerClass");
        try {
            BentoInitializer initer = (BentoInitializer)Class.forName(bentoFacInit).newInstance();
            initer.registerCustomDefs();
        } catch (Exception ex) {
            LOG.error(method, "Unable to load class or invoke method " + bentoFacInit, ex);
            throw new ServletException("Unable to load class or invoke method " + bentoFacInit, ex);
        }
        BentoDefFactory defFactory = BentoDefFactory.get();

        nickNameAttribute = conf.getServletContext().getInitParameter(NICKNAME_PARAM_KEY);
        String principalImpl = conf.getServletContext().getInitParameter(PRINCIPAL_IMPL_PARAM_KEY);
        try {
            principalImplClass = Class.forName(principalImpl);
        } catch (ClassNotFoundException ex) {
            LOG.error(method, "Unable to load class " + principalImpl, ex);
            throw new ServletException("Unable to load class " + principalImpl, ex);
        }

        LOG.leave(method);
    }

    protected Authorizer getAuthorizer() throws AuthenticationException {
        return getAuthorizer(null, false);
    }

    protected Authorizer getAuthorizer(boolean forceReauth) throws AuthenticationException {
        return getAuthorizer(null, forceReauth);
    }

    protected Authorizer getAuthorizer(HttpServletRequest req) throws AuthenticationException {
        return getAuthorizer(req, false);
    }

    private Authorizer getAuthorizer(HttpServletRequest req, boolean forceReauth)
            throws AuthenticationException {

        // Check if authorizer in session
        HttpServletRequest request = getThreadLocalRequest();
        if (request == null)
            request = req;

        if (!forceReauth) {
            Authorizer authorizer = (Authorizer)request.getSession().getAttribute(PRINCIPAL_SESSION_KEY);

            UserService gaeUserService = UserServiceFactory.getUserService();
            if (gaeUserService.isUserLoggedIn() && authorizer != null &&
                    gaeUserService.getCurrentUser().getNickname().equals(authorizer.getPrincipal().getNickName())) {
                    // check that still correct

                // check that authorizer not too old
                if (authorizer.stillValid())
                    return authorizer;
            }
        }

        LOG.debug("getAuthorizer", "Creating new principal");

        // authorizer is null, we create it
        Principal principal = createPrincipalFromGAE(request);
        // throws AuthenticationException if user not found in our user DB, and not GAE admin

        Authorizer authorizer = new Authorizer(principal);
        request.getSession().setAttribute(PRINCIPAL_SESSION_KEY, authorizer);

        return authorizer;
    }



    protected void authorize(Action action, Bento object) throws SecurityException {
        if (!getAuthorizer().isAllowed(action, object)) {
            LOG.info("authorize", getAuthorizer().getPrincipal().getDisplayName() +
                    "Not authorized to " + action.name() + " object of type " + object.getType());
            throw new AuthorizationException(action.name(), object.getType());
        }
    }
    protected void authorize(Action action, String type) throws SecurityException {
        if (!getAuthorizer().isAllowed(action, type)) {
            LOG.info("authorize", getAuthorizer().getPrincipal().getDisplayName() +
                    "Not authorized to " + action.name() + " object of type " + type);
            throw new AuthorizationException(action.name(), type);
        }
    }



    /**
     * This one will fetch the object owner in the DB, thus may be less performant
     */
    protected void authorize(Action action, ObjectRef objectRef) throws SecurityException {
        authorize(action, objectRef, null);
    }
    protected void authorize(Action action, ObjectRef objectRef, HttpServletRequest req) throws SecurityException {
        boolean isObjectOwner = false;
        Authorizer auth = getAuthorizer(req);
        try {
            String ownerStr = JdoHelper.get().getDataObject(objectRef).getOwner();
            if (ownerStr != null && ownerStr.length() > 0) {
                ObjectRef owner = new ObjectRef(ownerStr);
                if (auth.getPrincipal().getUserRef().equals(owner)) {
                    isObjectOwner = true;
                }
            }

        } catch (ObjectNotFoundException ex) {
            LOG.error("authorize", "ObjectNotFoundException while authorizing object", ex);
        }

        if (!auth.isAllowed(action, objectRef.getType(), isObjectOwner)) {
            LOG.info("authorize", auth.getPrincipal().getDisplayName() +
                    "Not authorized to " + action.name() + " object of type " + objectRef.getType() + " owned:" + isObjectOwner);
            throw new AuthorizationException(action.name(), objectRef.getType());
        }
    }


    private Principal createPrincipalFromGAE(HttpServletRequest request) throws AuthenticationException {
        String method = "createPrincipalFromGAE";
        LOG.enter(method);

        Principal principal = new Principal();

        // retrieve GAE user
        UserService gaeUserService = UserServiceFactory.getUserService();

        if (! gaeUserService.isUserLoggedIn()) {
            getThreadLocalRequest().getSession().removeAttribute(PRINCIPAL_SESSION_KEY);
            throw new AuthenticationException("No user currently logged in");
        }
        User gaeUser = gaeUserService.getCurrentUser();

        // fetch user ref
        PrincipalDO principalDo = null;
        PersistenceManager pm = PMF.get().getPersistenceManager();
        try {
            String query = "select from " + principalImplClass.getName() + " where " +  
                    nickNameAttribute + " == '" + gaeUser.getNickname() + "'" +
                    " && status == '"+BentoStatus.ACTIVE.name()+"'" ;
            List<PrincipalDO> listUsers = (List<PrincipalDO>)pm.newQuery(query).execute();
            if (listUsers == null || listUsers.size() == 0) {
                LOG.info(method, "No strict case found, trying ignoring case...");
                // second chance, fetching all and comparing ignoring cases (until JDO allows toUpper queries...)
                String queryAll = "select from " + principalImplClass.getName() + " where " +
                        " status == '"+BentoStatus.ACTIVE.name()+"'" ;
                List<PrincipalDO> allUsers = (List<PrincipalDO>)pm.newQuery(queryAll).execute();
                listUsers = new ArrayList();
                for (PrincipalDO p : allUsers) {
                    LOG.debug(method, "testing user " + p.getNickName());
                    if (p.getNickName().equalsIgnoreCase(gaeUser.getNickname())) {
                        listUsers.add(p);
                        LOG.debug(method, "matched user " + p.getNickName());
                    }
                }
            }
            if (listUsers == null || listUsers.size() == 0) {
                LOG.warning(method, "user authenticated in GAE but not found in DB: " + gaeUser.getNickname());
                if (gaeUserService.isUserAdmin()) {
                    LOG.warning(method, "... but is GAE admin, thus RescuePrincipal activated");
                    principal = new RescueAdminPrincipal();
                    principal.setNickName(gaeUser.getNickname());
                    principal.setAuthDomain(gaeUser.getAuthDomain());
                } else {
                    getThreadLocalRequest().getSession().removeAttribute(PRINCIPAL_SESSION_KEY);
                    throw new AuthenticationException("User is logged but not in the database",
                        gaeUser.getNickname(), gaeUser.getAuthDomain());
                }
            } else {
                principalDo = listUsers.get(0);
                principal.setAuthDomain(gaeUser.getAuthDomain());
                principal.setNickName(principalDo.getNickName());
                principal.setUserRef(principalDo.getObjectRef());
                principal.setDisplayName(principalDo.getDisplayName());
                principal.setEmail(principalDo.getEmail());

                // fetch the roles
                Set<Bento> roles = new HashSet<Bento>();
                List<String> userRoleRefs = principalDo.getRolesRef();
                if (userRoleRefs != null) {
                    RoleDef roleDef = new RoleDef();
                    String queryRoles = "select from " + Role.class.getName();
                    List<Role> listRoles = (List<Role>)pm.newQuery(queryRoles).execute();
                    if (listRoles != null || listRoles.size() > 0) {
                        Set<String> subRolesRef = new HashSet();
                        // main roles
                        for (Role roleDo : listRoles) {
                            ObjectRef oref = new ObjectRef(roleDef.getType(), roleDo.getId());
                            if (userRoleRefs.contains(oref.toString())) {
                                Bento newRole = roleDef.createBento
                                        (roleDo.getId(), new ObjectRef(roleDo.getOwner()), roleDo.getLastUpdate());
                                DataObjectMapper.do2bento(roleDo, newRole);
                                roles.add(newRole);
                                List<String> subRoles = newRole.get(RoleDef.ATTR_INHERITED_ROLES).getStringList();
                                if (subRoles != null && subRoles.size() > 0)
                                    subRolesRef.addAll(subRoles);
                            }
                        }
                        // sub roles
                        for (Role roleDo : listRoles) {
                            ObjectRef oref = new ObjectRef(roleDef.getType(), roleDo.getId());
                            if (subRolesRef.contains(oref.toString()) &&
                                    !userRoleRefs.contains(oref.toString())) {
                                Bento newRole = roleDef.createBento
                                        (roleDo.getId(), new ObjectRef(roleDo.getOwner()), roleDo.getLastUpdate());
                                DataObjectMapper.do2bento(roleDo, newRole);
                                roles.add(newRole);
                            }
                        }
                        // WARNING: this will (likely) not cope with sub-sub-roles, etc!!!
                    }
                    principal.setRoles(roles);
                }
            }
        } finally {
            pm.close();
        }

        LOG.leave(method);
        return principal;
    }
}

