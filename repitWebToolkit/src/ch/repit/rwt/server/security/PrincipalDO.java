/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.repit.rwt.server.security;

import ch.repit.rwt.client.ObjectRef;
import java.util.List;

/**
 *
 * @author tc149752
 */
public interface PrincipalDO {

    public Long getId();

    public String getNickName();

    public String getDisplayName();

    public String getEmail();

    public List<String> getRolesRef();

    public ObjectRef getObjectRef();

}
