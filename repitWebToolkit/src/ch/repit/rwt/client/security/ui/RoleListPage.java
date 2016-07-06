/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.repit.rwt.client.security.ui;

import ch.repit.rwt.client.Bento;
import ch.repit.rwt.client.BentoAttribute;
import ch.repit.rwt.client.BentoStatus;
import ch.repit.rwt.client.ObjectRef;
import ch.repit.rwt.client.persistence.CacheManager;
import ch.repit.rwt.client.security.RoleDef;
import ch.repit.rwt.client.ui.BentoListPage;
import ch.repit.rwt.client.ui.Page;
import com.google.gwt.user.client.ui.HTML;
import java.util.Map;

/**
 *
 * @author tc149752
 */
public class RoleListPage extends BentoListPage
{
    public RoleListPage(Page topPage, BentoStatus... status)
    {
        super (topPage, RoleDef.TYPE, status);

        // set the columns
        super.addColumn("Nom du rôle",   RoleDef.ATTR_NAME, true);
        super.addColumn("Catégorie",     RoleDef.ATTR_CATEGORY, true);
        super.addColumn("Rôles hérités", "_inheritedRoles", false);
        super.addColumn("Description",   RoleDef.ATTR_DESCRIPTION, false);
    }

 
    @Override
    protected void onRowClicked(Bento bento, String columnsAttributeName) {
        getPageNav().displayPage(new RoleDetailsPage(bento.getRef()));
    }

    @Override
    protected boolean formatObject(Bento bento, Map formatedValue) {

        // fetch roles labels
        BentoAttribute rolesAttr = bento.get(RoleDef.ATTR_INHERITED_ROLES);
        HTML html = new HTML();
        if (rolesAttr.getStringList() != null) {
            String val = "";
            for (String item : rolesAttr.getStringList()) {
                if (item != null) {
                    ObjectRef roleRef = new ObjectRef(item);
                    Bento role = CacheManager.get().getCachedObject(roleRef);
                    if (role != null) {
                        String roleLabel = role.getDisplayValue("name");
                        val += roleLabel +"<br/>";
                    } else
                        val += "("+ item +")<br/>";
                }
            }
            html.setHTML(val);
        } else
            html.setHTML("");
        html.addStyleName("rwt-list-cellMultiLine");
        formatedValue.put("_inheritedRoles", html);

        return super.formatObject(bento, formatedValue);
    }


}
