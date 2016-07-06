/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.repit.rwt.server.persistence;

import ch.repit.rwt.client.Bento;
import ch.repit.rwt.client.BentoDef;
import ch.repit.rwt.client.BentoDefFactory;
import ch.repit.rwt.client.BentoStatus;
import ch.repit.rwt.client.FileDescriptor;
import ch.repit.rwt.client.ObjectRef;
import ch.repit.rwt.client.persistence.ObjectNotFoundException;
import ch.repit.rwt.server.RwtRuntimeException;
import ch.repit.rwt.server.file.File;
import ch.repit.rwt.server.file.FileHolder;
import ch.repit.rwt.server.util.Logging;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

/**
 *
 * @author tc149752
 */
public class JdoHelper {

    private static Logging LOG = new Logging(PersistenceServiceImpl.class.getName());

    private static JdoHelper instance  = new JdoHelper();

    public static final JdoHelper get() {
        return instance;
    }

    private Map<String,Class> jdoClasses = new HashMap<String,Class>();
    
    private JdoHelper() {}

    
    // finds an object outside a transaction (will open one)
    public DataObject getDataObject(ObjectRef objectRef) throws ObjectNotFoundException {
        PersistenceManager pm = PMF.get().getPersistenceManager();
        try {
            return getDataObject(pm, objectRef);
        } finally {
            pm.close();
        }
    }

    // finds an object within an open transaction
    public DataObject getDataObject(PersistenceManager pm, ObjectRef objectRef) throws ObjectNotFoundException {
        Class typeClass = jdoClasses.get(objectRef.getType());
        try {
            DataObject dob = (DataObject)pm.getObjectById(typeClass, objectRef.getId());
            return dob;
        } catch (JDOObjectNotFoundException e) {
            throw new ObjectNotFoundException(e.getMessage(), e);
        }
    }


    /**
     * Meant to be used server-side
     * @param objectRef
     * @return
     */
    public Bento getBento(ObjectRef objectRef) throws ObjectNotFoundException {
        BentoDef bentoDef = BentoDefFactory.get().getDef(objectRef.getType());
        DataObject jdoObj = getDataObject(objectRef);
        Bento bento = bentoDef.createBento(jdoObj.getId(),
                (jdoObj.getOwner()!=null)?new ObjectRef(jdoObj.getOwner()):null,
                jdoObj.getLastUpdate());
        DataObjectMapper.do2bento(jdoObj, bento);
        return bento;
    }



    /**
     * Meant to be used server-side
     * params contains follwong keys
     * - ordering
     * - filter
     * WARNING: only returns ACTIVE objects!
     */
    public List<Bento> listActiveBentos(BentoDef bentoDef, Map<String,String> params) {
        List<Bento> bentoList = null;

        PersistenceManager pm = PMF.get().getPersistenceManager();
        try {
            Class typeClass = JdoHelper.get().getJdoClass(bentoDef.getType());
            String filter = " status == '"+BentoStatus.ACTIVE.name()+"' ";
            Query query = pm.newQuery(typeClass);
            List<DataObject> dataObjList;

            // adds additional filter (if any)
            if (bentoDef.getJdoFilter() != null) {
                    filter += " && " + bentoDef.getJdoFilter();
            }
            if (params.containsKey("filter"))
                filter += " && " + params.get("filter");
            query.setFilter(filter);

            // set ordering if desired
            if (params.containsKey("ordering"))
                query.setOrdering(params.get("ordering"));

            // exec
            dataObjList = (List<DataObject>)query.execute();

            // transform to bento
            bentoList = new ArrayList<Bento>();
            if (dataObjList != null && dataObjList.size() > 0) {
                for (DataObject jdoObj : dataObjList) {
                    Bento bento = bentoDef.createBento(jdoObj.getId(),
                            (jdoObj.getOwner()!=null)?new ObjectRef(jdoObj.getOwner()):null,
                            jdoObj.getLastUpdate());
                    DataObjectMapper.do2bento(jdoObj, bento);
                    bentoList.add(bento);
                }
            }
        } finally {
            pm.close();
        }
        return bentoList;
    }



    public byte[] readBlob(ObjectRef containingObjectRef, FileDescriptor fileDesc)
    {
        String method = "readPhoto";
        // 2. read the file content from the DB
        PersistenceManager pm = PMF.get().getPersistenceManager();
        try {
            // read DB file object
            FileHolder fileHolder = (FileHolder)JdoHelper.get().getDataObject(pm, containingObjectRef);

            File file = null;
            for (File f : (List<File>)fileHolder.getFiles())
                if (f.getFileKey().getId() == fileDesc.getId())
                    file = f;

            if (file == null) {
                LOG.debug(method, "File does not exist");
                return null;
            }

            com.google.appengine.api.datastore.Blob blob = file.getFileContent();

            if (blob != null)
                return  blob.getBytes();
            else  {
                LOG.debug(method, "File has no content");
                return null;
            }
        } catch (ObjectNotFoundException e) {
            return null;
        } finally {
            pm.close();
        }
    }



    
    public Class getJdoClass(ObjectRef objectRef) {
        return getJdoClass(objectRef.getType());
    }

    public Class getJdoClass(String type) {
        if (!jdoClasses.containsKey(type)) {
            BentoDefFactory defFactory = BentoDefFactory.get();
            try {
                if (defFactory.getDef(type).getJdoClassName() != null) // null allowed for non persistent objs
                    jdoClasses.put(type, Class.forName(defFactory.getDef(type).getJdoClassName()));
            } catch (ClassNotFoundException ex) {
                LOG.error("getJdoClass", "ClassNotFoundException for type " + type, ex);
                throw new RwtRuntimeException("ClassNotFoundException for type " + type, ex);
            }
        }
        return jdoClasses.get(type);
    }

 
}
