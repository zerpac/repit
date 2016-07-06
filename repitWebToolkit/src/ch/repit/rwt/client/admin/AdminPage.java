/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.repit.rwt.client.admin;

import ch.repit.rwt.client.BentoStatus;
import ch.repit.rwt.client.audit.AuditQueryPage;
import ch.repit.rwt.client.security.ui.RoleListPage;
import ch.repit.rwt.client.ui.Page;



/**
 *
 * @author tc149752
 */
public class AdminPage extends Page {

    public AdminPage()
    {
        super();

        setTitle("Administration");

        super.setShowPath(false);
        super.addTab("Rôles actifs", new RoleListPage(this, BentoStatus.ACTIVE, BentoStatus.DRAFT, BentoStatus.ARCHIVE));
        // TBD: check authoriz TBD: split ...
        super.addTab("Rôles effacés", new RoleListPage(this, BentoStatus.TRASH));
        super.addTab("Rapports d'audit", new AuditQueryPage(this));
    }



}
