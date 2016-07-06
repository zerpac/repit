package ch.repit.rwt.client.security;

import ch.repit.rwt.client.Bento;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Checks authorizations.
 * Must be Serializable because it is in the Http Session that is saved as serialized.
 */
public class Authorizer implements Serializable {

    private Principal principal;
    private long creationTime;
    private transient Map<String,Set<Action>> assignedActionsCache;

    private Authorizer() {  }  // for serializable


    public Authorizer(Principal principal) {
        this.principal = principal;
        creationTime = System.currentTimeMillis();
    }
    
    public Principal getPrincipal() {
        return principal;
    }


    public boolean isAllowed(Action action, Bento object) {
        if (object != null && action != null && principal.getRoles() != null)
            return isAllowed(action, object, object.getType());
        else
            return false;
    }
    public boolean isAllowed(Action action, String objectType) {
        if (objectType != null && action != null && principal.getRoles() != null)
            return isAllowed(action, null, objectType);
        else
            return false;
    }
    public boolean isAllowed(Action action, String objectType, boolean isOwner) {
        if (objectType != null && action != null && principal.getRoles() != null)
            return isAllowed(action, null, objectType, isOwner);
        else
            return false;
    }

    public boolean stillValid() {
        return ( System.currentTimeMillis() < (creationTime + (long)(5 * 60 * 1000)) ); // 5 minutes
    }
    
    public enum ActionScope {
        GLOBAL,
        OWN,
        NONE;  // or return null...
    }

    public ActionScope getAllowedScope(Action action, String objectType) {
        if (getAssignedActions(objectType, false).contains(action))
            return ActionScope.GLOBAL;
        if (getAssignedActions(objectType, true).contains(action))
            return ActionScope.OWN;
        return ActionScope.NONE;
    }

    
    private boolean isAllowed(Action action, Bento object, String objectType) {

        // check if object is owned by principal, if yes chech <action>_OWN instead
        boolean isOwnerOfObj = false;
        if (object != null && principal.getUserRef() != null &&
                principal.getUserRef().equals(object.getOwnerRef())) {
            isOwnerOfObj = true;
        }

        return isAllowed(action, object, objectType, isOwnerOfObj);
    }

    private boolean isAllowed(Action action, Bento object, String objectType, boolean isOwner) {

        // fetch action list for this type
        Set<Action> assignedActions = getAssignedActions(objectType, isOwner);
        if (assignedActions == null || assignedActions.size() == 0)
            return false;  // no need to go any further

        return assignedActions.contains(action);
    }

    /**
     * @param objectType
     * @param ownObject if true, will return a concatenation of ownObjectPerms and globalScopePerms.
     * If false, only globalScopePerms
     * @return
     */
    private Set<Action> getAssignedActions(String objectType, boolean ownObject)
    {        
        String typeKey = objectType + (ownObject?"Own":"All");

        if (assignedActionsCache == null)
            assignedActionsCache = new HashMap<String,Set<Action>>();
        if (assignedActionsCache.get(typeKey) == null) {
            Set<Action> allAssignedActionsOfTypeGlobal = new HashSet<Action>();
            Set<Action> allAssignedActionsOfTypeOwn = new HashSet<Action>();
            for (Bento role : principal.getRoles()) {
                // fetch all global perms
                if (role.get(RoleDef.ATTR_PERM_ALL) != null && role.get(RoleDef.ATTR_PERM_ALL).getStringList() != null)
                for (String permStr : role.get(RoleDef.ATTR_PERM_ALL).getStringList()) {
                    if (permStr != null && permStr.trim().length() > 0) {
                        try {
                            Permission perm = new Permission(permStr);
                            if ( perm.getObjectType().equals(objectType) || perm.getObjectType().equals("*") ) {
                                allAssignedActionsOfTypeGlobal.addAll(perm.getAction().getAllInheritedActions());
                            }
                        } catch (IllegalArgumentException e) {
                            // there is noise in the existing perms, should be removed during next update
                        }
                    }
                }
                // fetch all own perms
                if (role.get(RoleDef.ATTR_PERM_OWN) != null && role.get(RoleDef.ATTR_PERM_OWN).getStringList() != null)
                for (String permStr : role.get(RoleDef.ATTR_PERM_OWN).getStringList()) {
                    if (permStr != null && permStr.trim().length() > 0) {
                        try {
                            Permission perm = new Permission(permStr);
                            if (perm.getObjectType().equals(objectType) || perm.getObjectType().equals("*") ) {
                                allAssignedActionsOfTypeOwn.addAll(perm.getAction().getAllInheritedActions());
                            }
                        } catch (IllegalArgumentException e) {
                            // there is noise in the existing perms, should be removed during next update
                        }
                    }
                }
            }

            assignedActionsCache.put(objectType + "All", allAssignedActionsOfTypeGlobal);
            
            // concat both list for own objs
            allAssignedActionsOfTypeOwn.addAll(allAssignedActionsOfTypeGlobal);
            assignedActionsCache.put(objectType + "Own", allAssignedActionsOfTypeOwn);
        }
        
        return assignedActionsCache.get(typeKey);
    }


}
