/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.repit.rwt.client.security;

import ch.repit.rwt.client.Bento;
import ch.repit.rwt.client.BentoDefFactory;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Principal returned by AuthServiceImpl when an exception occurs and the logged user is GAE admin
 * so that we are not blocked...
 */
public class RescueAdminPrincipal extends Principal {

    public RescueAdminPrincipal() {
        super.setAuthDomain("rescue.sos");
        super.setDisplayName("Rescue Administrator");
        super.setNickName("baywatch");
        Set<Bento> roles = new HashSet<Bento>();
        Bento supaUserRole = BentoDefFactory.get().getDef("Role").createBento();
        supaUserRole.get("name").set("supaUserRescue");
        supaUserRole.get("description").set("Static hard-coded role used when none can access the system");
        List<String> perms = new ArrayList<String>();
        perms.add("*:ADMIN");
        supaUserRole.get(RoleDef.ATTR_PERM_ALL).set(perms);
        roles.add(supaUserRole);
        super.setRoles(roles);
    }

}
