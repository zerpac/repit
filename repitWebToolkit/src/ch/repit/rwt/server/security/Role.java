
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.repit.rwt.server.security;

import ch.repit.rwt.server.persistence.BaseDataObject;
import java.util.List;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

/**
 *
 * @author tc149752
 */
@PersistenceCapable(identityType = IdentityType.APPLICATION)
@Inheritance(customStrategy = "complete-table")
public class Role extends BaseDataObject {

    @Persistent
    private String name;

    @Persistent
    private String category;

    @Persistent
    private String description;

    @Persistent
    private List<String> globalScopePermissions;
    @Persistent
    private List<String> ownObjectsPermissions;
    @Persistent
    private List<String> inheritedRoles;

    
    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getGlobalScopePermissions() {
        return globalScopePermissions;
    }

    public void setGlobalScopePermissions(List<String> globalScopePermissions) {
        this.globalScopePermissions = globalScopePermissions;
    }

    public List<String> getOwnObjectsPermissions() {
        return ownObjectsPermissions;
    }

    public void setOwnObjectsPermissions(List<String> ownObjectsPermissions) {
        this.ownObjectsPermissions = ownObjectsPermissions;
    }

    public List<String> getInheritedRoles() {
        return inheritedRoles;
    }

    public void setInheritedRoles(List<String> inheritedRoles) {
        this.inheritedRoles = inheritedRoles;
    }


    @Override
    public String getDisplayName() {
        return getName();
    }

    
    /*
     * Below are not supported for roles
     */

    @Override
    public String getOwner() {
        return null;
    }

    @Override
    public void setOwner(String ownerStr) {  }


}
