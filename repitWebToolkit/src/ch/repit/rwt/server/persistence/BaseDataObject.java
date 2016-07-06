package ch.repit.rwt.server.persistence;

import ch.repit.rwt.client.BentoStatus;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;


@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class BaseDataObject implements DataObject {


    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Long id;

    // should add  value strategy to set current date...
    @Persistent
    private Date lastUpdate;

    @Persistent
    private String owner;

    @Persistent
    private String status;


    @Persistent
    private List<String> comments = new ArrayList<String>();


    public Long      getId()    {
        return id;
    }

    public Date      getLastUpdate() {
        return lastUpdate;
    }
    public void      setUpdated()  {
        lastUpdate = new Date();
    }

    public String getStatus() {
        if (status == null) {
            status = BentoStatus.DRAFT.name();
        }
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }


    public String getOwner() {
        return owner;
    }
    public void setOwner(String ownerStr) {
        this.owner = ownerStr;
    }


    public String getDisplayName() {
        return "ID="+getId();
    }

    public List<String> getComments() {
        if (comments == null)
            comments =  new ArrayList<String>();
        return comments;
    }


}
