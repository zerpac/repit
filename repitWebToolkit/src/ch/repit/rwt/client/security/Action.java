/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.repit.rwt.client.security;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author tc149752
 */

public enum Action {
        
    VIEW_TRASH     ("Can view the trashed objects"),

    COMMENT        ("Can comment the object, assuming the object can be commented"),

    AUDIT          ("Can read audit logs about the object type"),

    UPDATE         ("Can update most attributes objects of this type, including deleting comments"),

    CREATE         ("Can create object of this type and become their owner (except for users)"),

    DRAFT          ("Can manage objects before they are published, and publish them. Can also permanently delete them"),

    TRASH          ("Can pseudo delete objects, and recover them", UPDATE, VIEW_TRASH),

    MANAGE         ("Can create, update objects, trash and untrash, edit level-2 attributes",
                    UPDATE, CREATE, TRASH, AUDIT, DRAFT),

    ADMIN          ("Supa-power-user, can permanently delete objects", MANAGE);  // maybe not needed, as this will be siteadmin...

    
    private String description;
    private Set<Action> includedActionSet;

    private Action(String description, Action... includedActions) {
        this.description = description;
        includedActionSet = new HashSet<Action>();
        for (Action a : includedActions)
            includedActionSet.add(a);
    }

    public String getDescription() {
        return description;
    }

    // watch out for recursive...
    public Set<Action> getAllInheritedActions() {
        Set<Action> result = new HashSet<Action>();
        result.add(this);
        result.addAll(includedActionSet);
        for (Action a : includedActionSet)
            result.addAll(a.getAllInheritedActions());
        return result;
    }

}

