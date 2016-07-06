/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.repit.rwt.server.persistence;

import java.util.Date;
import java.util.List;

/**
 *
 * @author tc149752
 */
public interface DataObject {

    public Long getId();
    
    public String getDisplayName();

    public Date getLastUpdate();

    public String getOwner();
    public void setOwner(String ownerStr);

    public List<String> getComments();

    public void      setUpdated();

    public String getStatus();
    public void setStatus(String status);

}
