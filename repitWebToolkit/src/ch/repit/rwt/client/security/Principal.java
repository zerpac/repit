/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.repit.rwt.client.security;

import ch.repit.rwt.client.Bento;
import ch.repit.rwt.client.ObjectRef;
import java.io.Serializable;
import java.util.Set;

/**
 *
 * @author tc149752
 */
public class Principal implements Serializable {
    //User gaeUser;  // does not compile (sic!)
    private ObjectRef userRef;
    private String nickName;
    private String authDomain;
    private String displayName;
    private Set<Bento> roles;
    private String email;

    public Principal() {}

    public ObjectRef getUserRef() {
        return userRef;
    }

    public void setUserRef(ObjectRef userRef) {
        this.userRef = userRef;
    }

    public String getAuthDomain() {
        return authDomain;
    }

    public void setAuthDomain(String authDomain) {
        this.authDomain = authDomain;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Set<Bento> getRoles() {
        return roles;
    }

    public void setRoles(Set<Bento> roles) {
        this.roles = roles;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }



}
