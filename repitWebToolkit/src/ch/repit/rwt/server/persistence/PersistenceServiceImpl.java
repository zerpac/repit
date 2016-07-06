package ch.repit.rwt.server.persistence;

import ch.repit.rwt.server.util.Logging;
import ch.repit.rwt.client.BentoComment;
import ch.repit.rwt.client.Bento;
import ch.repit.rwt.client.BentoDef;
import ch.repit.rwt.client.BentoDefFactory;
import ch.repit.rwt.client.BentoStatus;
import ch.repit.rwt.client.ObjectRef;
import ch.repit.rwt.client.audit.AuditLogDTO;
import ch.repit.rwt.server.RwtRemoteServiceServlet;
import ch.repit.rwt.client.audit.AuditLogDTO.AuditableAction;
import ch.repit.rwt.client.audit.AuditQueryDef;
import ch.repit.rwt.client.persistence.MissingMandatoryAttributeException;
import ch.repit.rwt.client.persistence.ObjectStaleException;
import ch.repit.rwt.client.persistence.PersistenceService;
import ch.repit.rwt.client.persistence.BentoCache;
import ch.repit.rwt.client.persistence.BentoCache.CacheLoadLevel;
import ch.repit.rwt.client.persistence.ObjectNotFoundException;
import ch.repit.rwt.client.persistence.UniqueKeyViolationException;
import ch.repit.rwt.client.persistence.ValidationException;
import ch.repit.rwt.client.security.Action;
import ch.repit.rwt.client.security.SecurityException;
import ch.repit.rwt.client.security.Authorizer;
import ch.repit.rwt.client.security.Authorizer.ActionScope;
import ch.repit.rwt.client.security.Principal;
import ch.repit.rwt.client.security.RoleDef;
import ch.repit.rwt.client.user.UserDef;
import ch.repit.rwt.client.util.Formaters;
import ch.repit.rwt.client.util.Formaters.DatePattern;
import ch.repit.rwt.server.RwtRuntimeException;
import ch.repit.rwt.server.audit.AuditLog;
import ch.repit.rwt.server.audit.AuditLogAttribute;
import ch.repit.rwt.server.audit.AuditManager;
import ch.repit.rwt.server.file.File;
import ch.repit.rwt.server.file.FileHolder;
import ch.repit.rwt.server.notification.Notifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.servlet.ServletException;
import javax.servlet.ServletConfig;


/**
 * Implementation of Bento Transfert Objects persistence layer
 */
public class PersistenceServiceImpl extends RwtRemoteServiceServlet implements PersistenceService
{    
    private static Logging LOG = new Logging(PersistenceServiceImpl.class.getName());

    private Map<String,Validator> validators = new HashMap<String,Validator>();

    private Notifier notifier;
    

    @Override
    public void init(ServletConfig conf) throws ServletException     
    {
        super.init(conf);
        String method = "init";
        LOG.enter(method);
        
        // read jdo classes
        BentoDefFactory defFactory = BentoDefFactory.get();
        JdoHelper jdoHelper = JdoHelper.get();
        for (String type : defFactory.getTypes()) {
            try {
                jdoHelper.getJdoClass(type);
            } catch (RwtRuntimeException ex) {
                LOG.error(method, "ClassNotFoundException for type " + type, ex);
                throw new ServletException("ClassNotFoundException for type " + type, ex);
            }
        }

        // read validators
        for (String type : defFactory.getTypes()) {
            BentoDef bd = BentoDefFactory.get().getDef(type);
            String valiClassName = bd.getValidator();
            if (valiClassName != null) {
                try {
                    Validator vali = (Validator) Class.forName(valiClassName).newInstance();
                    validators.put(type, vali);
                } catch (InstantiationException ex) {
                    LOG.error(method, "ClassNotFoundException for class " + valiClassName, ex);
                    throw new ServletException("ClassNotFoundException for class " + valiClassName, ex);
                } catch (IllegalAccessException ex) {
                    LOG.error(method, "ClassNotFoundException for type " + valiClassName, ex);
                    throw new ServletException("ClassNotFoundException for class " + valiClassName, ex);
                } catch (ClassNotFoundException ex) {
                    LOG.error(method, "ClassNotFoundException for type " + valiClassName, ex);
                    throw new ServletException("ClassNotFoundException for class " + valiClassName, ex);
                }
            }
        }

        // init notifier
        notifier = new Notifier();

        LOG.leave(method);
    }


    /**
       - full, with priority (objectType, status) and scope of user on these objects
         will have a param with values
           - HIGH (active roles, own user)
           - Medium (all active and drafts with scope) // dont worry if roles are fetched twice...
           - low (all archives and trash with scope)
       - incremental, with cacheAge param
         fetch all objects with age younger than param, then
           - for each object of status DRAFT or TRASH check its scope wrt user
           - perform an audit query for deleted objects since cacheAge
     */
    public BentoCache loadCache(CacheLoadLevel level) throws SecurityException {
        return loadCache(level, null);
    }

    public BentoCache loadCache(CacheLoadLevel level, ObjectRef directObjRef) throws SecurityException {
        String method = "loadCache";
        LOG.enter(method, "level="+level.name()+"; directObjRef="+directObjRef);

        /* for dev
        try {
            Thread.sleep(3000);
        } catch (InterruptedException ex) { }
        */

        // check the principal is set...
        Authorizer auth = getAuthorizer();

        BentoCache cache = new BentoCache();
        cache.setCacheLevel(level);   // every load should replace the previous one

        // do this before query, just in case a modif occurs in the mean time
        cache.setCacheAge(new Date());

        // define level-specific restrictions on type
        Set<String> objectTypes;
        if (level == CacheLoadLevel.INCREMENTAL1) {
            objectTypes = new HashSet();
            objectTypes.add(UserDef.TYPE);
            objectTypes.add(RoleDef.TYPE);
        } else {
            objectTypes = BentoDefFactory.get().getTypes();
        }

        // 1. fetch the various objects
        for (String type : objectTypes) {
            if (JdoHelper.get().getJdoClass(type) != null) {

                BentoDef bentoDef = BentoDefFactory.get().getDef(type);
                Class typeClass = JdoHelper.get().getJdoClass(type);

                Authorizer.ActionScope trashScope = auth.getAllowedScope(Action.VIEW_TRASH, type);
                Authorizer.ActionScope draftScope = auth.getAllowedScope(Action.DRAFT, type);

                List<String> filters = new ArrayList<String>();
                switch (level) {
                    case INCREMENTAL1:
                        filters.add(" status == '"+BentoStatus.ACTIVE.name()+"' ");
                        break;
                    case INCREMENTAL2:
                        filters.add(" status == '"+BentoStatus.ACTIVE.name()+"' ");
                        if (ActionScope.NONE != draftScope)
                            filters.add(" status == '"+BentoStatus.DRAFT.name()+"' "
                                    +  ( (ActionScope.OWN==draftScope)?
                                             (" && owner=='" + auth.getPrincipal().getUserRef() + "'")
                                             :"") );
                        break;
                    case INCREMENTAL3:
                        filters.add(" status=='"+BentoStatus.ARCHIVE.name()+"' ");
                        filters.add(" status=='"+BentoStatus.TRASH.name()+"' ");
                        if (ActionScope.NONE != trashScope)
                            filters.add(" status=='"+BentoStatus.TRASH.name()+"' "
                                    +  ( (ActionScope.OWN==trashScope)?
                                             (" && owner=='" + auth.getPrincipal().getUserRef() + "'")
                                             :"") );
                        break;
                    case FULL:
                        filters.add(" status == '"+BentoStatus.ACTIVE.name()+"' ");
                        if (ActionScope.NONE != draftScope)
                            filters.add(" status == '"+BentoStatus.DRAFT.name()+"' "
                                    +  ( (ActionScope.OWN==draftScope)?
                                             (" && owner=='" + auth.getPrincipal().getUserRef() + "'")
                                             :"") );
                        filters.add(" status=='"+BentoStatus.ARCHIVE.name()+"' ");
                        filters.add(" status=='"+BentoStatus.TRASH.name()+"' ");
                        if (ActionScope.NONE != trashScope)
                            filters.add(" status=='"+BentoStatus.TRASH.name()+"' "
                                    +  ( (ActionScope.OWN==trashScope)?
                                             (" && owner=='" + auth.getPrincipal().getUserRef() + "'")
                                             :"") );
                        break;
                }

                // Iterate in filter (normaly only one)
                for (String filter : filters) {
                    PersistenceManager pm = PMF.get().getPersistenceManager();
                    try {
                        Query query = pm.newQuery(typeClass);
                        List<DataObject> dataObjList;

                        // adds additional filter (if any)
                        if (bentoDef.getJdoFilter() != null) {
                            if (filter == null)
                                filter = bentoDef.getJdoFilter();
                            else
                                filter += " && " + bentoDef.getJdoFilter();
                        }

                        if (filter != null)
                            query.setFilter(filter);
                        dataObjList = (List<DataObject>)query.execute();
                     
                        // 2. transform to bento
                        List<Bento> bentoList = cache.getObjects(type);
                        if (bentoList == null)
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
                        cache.putObjects(type, bentoList);
                    } finally {
                        pm.close();
                    }
                }
            }
        }

        if (directObjRef != null) {
            try {
                Bento directBento = JdoHelper.get().getBento(directObjRef);
                List<Bento> list = cache.getObjects(directObjRef.getType());
                if (list == null) {
                    list = new ArrayList();
                    cache.putObjects(directObjRef.getType(), list);
                }
                list.add(directBento);
            } catch (ObjectNotFoundException e) {
                // nada...
            }
        }

        LOG.leave(method);
        return cache;
    }


    /**
     * Here we fetch
     *  - everything updated since cacheAge, then we check if the user can view it.
     *    If Trash and user cannot view it, we send it in deleted refs
     *  - in audit, everything deleted since cacheAge, and we send it in deleted refs
     */
    public BentoCache refreshCache(Date cacheAge) throws SecurityException {
        String method = "refreshCache";
        LOG.enter(method, "cacheAge="+cacheAge);

        // check the principal is set...
        Authorizer auth = getAuthorizer();
        String userRefStr = "";
        if (auth.getPrincipal()!=null && auth.getPrincipal().getUserRef()!=null)
            userRefStr = auth.getPrincipal().getUserRef().toString();

        BentoCache cache = new BentoCache();        
        cache.setCacheLevel(CacheLoadLevel.REFRESH); 

        // do this before query, just in case a modif occurs in the mean time
        cache.setCacheAge(new Date());  // TBD: dont take this, but age of latest fetched object... (P3)

        // 1. fetch the various objects
        for (String type : BentoDefFactory.get().getTypes()) {
            if (JdoHelper.get().getJdoClass(type) != null) {

                BentoDef bentoDef = BentoDefFactory.get().getDef(type);
                Class typeClass = JdoHelper.get().getJdoClass(type);

                // if supports drafts, adds a specific query (TOO COMPLICATED, FILTERS AFTERWARD)
                ActionScope draftScope = auth.getAllowedScope(Action.DRAFT, type);
                ActionScope trashScope = auth.getAllowedScope(Action.VIEW_TRASH, type);
                
                PersistenceManager pm = PMF.get().getPersistenceManager();
                try {
                    Query query = pm.newQuery(typeClass);
                    List<DataObject> dataObjList;

                    String filter = "lastUpdate > cacheAge";

                    // adds additional filter (if any)
                    if (bentoDef.getJdoFilter() != null) {
                        filter += " && " + bentoDef.getJdoFilter();
                    }

                    query.setFilter(filter);
                    query.declareParameters("java.util.Date cacheAge");
                    dataObjList = (List<DataObject>)query.execute(cacheAge);

                    // transform to bento
                    List<Bento> bentoList = cache.getObjects(type);
                    if (bentoList == null)
                        bentoList = new ArrayList<Bento>();
                    if (dataObjList != null && dataObjList.size() > 0) {
                        for (DataObject jdoObj : dataObjList) {

                            BentoStatus objStatus = BentoStatus.valueOf(jdoObj.getStatus());
                            boolean add = false;
                            boolean delete = false;

                            switch (objStatus) {
                                case ACTIVE:
                                case ARCHIVE:
                                    add = true;
                                    break;
                                case DRAFT:
                                    add = ( draftScope == ActionScope.GLOBAL
                                          || (draftScope == ActionScope.OWN
                                              && userRefStr.equals(jdoObj.getOwner()) ) );
                                    break;
                                case TRASH:
                                    add = ( trashScope == ActionScope.GLOBAL
                                          || (trashScope == ActionScope.OWN
                                              && userRefStr.equals(jdoObj.getOwner()) ) );
                                    delete = !add;
                                    break;
                            }

                            if (add) {
                                Bento bento = bentoDef.createBento(jdoObj.getId(),
                                        (jdoObj.getOwner()!=null)?new ObjectRef(jdoObj.getOwner()):null,
                                        jdoObj.getLastUpdate());
                                DataObjectMapper.do2bento(jdoObj, bento);
                                bentoList.add(bento);
                            }

                            // this is the case for trahs objects not visible, they MIGHT just have been trashed
                            if (delete) {
                                cache.addDeletedObject( new ObjectRef(type,jdoObj.getId()) );
                            }
                        }
                    }
                    cache.putObjects(type, bentoList);
                } finally {
                    pm.close();
                }
            }
        }

        
        // 2. fetch audit permanent deletion
        //    this is specialy useful for shared draft that may be directly deleted
        Bento auditQuery = BentoDefFactory.get().getDef(AuditQueryDef.TYPE).createBento();
        auditQuery.get(AuditQueryDef.ATTR_FROMDATE).set(cacheAge);
        auditQuery.get(AuditQueryDef.ATTR_ACTION).set(AuditableAction.DELETE.name());
        List<AuditLogDTO> auditLogs = AuditManager.get().findAuditLogs(auditQuery);
        for (AuditLogDTO deletion : auditLogs) {
            cache.addDeletedObject(deletion.getObject());
        }

        LOG.leave(method);
        return cache;
    }

    
    public ObjectRef createBento(Bento newBento)
            throws SecurityException, ValidationException, UniqueKeyViolationException, MissingMandatoryAttributeException {
        String method = "createBento";
        LOG.enter(method);
        assert newBento != null : "input param cannot be null";

        ObjectRef newObjectId = null;

        // check permissions
        authorize(Action.CREATE, newBento);
 
        // custom validation if any
        customValidation(newBento);

        // do the job
        PersistenceManager pm = PMF.get().getPersistenceManager();
        try {
            Class typeClass = JdoHelper.get().getJdoClass(newBento.getType());

            // 2. create a DO based on the TO
            DataObject dataObject = (DataObject)typeClass.newInstance();

            AuditLog auditLog = new AuditLog(AuditableAction.CREATE,
                super.getAuthorizer().getPrincipal().getUserRef());

            // check unique attrs uniqueness and mandatory
            Map<String,String> uniqueAttr2Check = new HashMap<String,String>();
            DataObjectMapper.validateBentoWithDo(newBento, dataObject, uniqueAttr2Check);
            if (!uniqueAttr2Check.isEmpty())
                checkUniqueAttributes(pm, typeClass, uniqueAttr2Check);
            // throws UniqueKeyViolationException, MissingMandatoryAttributeException


            // now that it is valid, we can set the DO; otherwise, the DO was set
            // even without calling makePersistent! (GAE 1.2.1, local server)
            DataObjectMapper.bento2do(newBento, dataObject, auditLog);
            dataObject.setUpdated();

            // must set the owner at this stage (server side)
            BentoDef bdef = BentoDefFactory.get().getDef(newBento.getType());

            if (bdef.getOwnerPolicy() == BentoDef.OwnerPolicy.CREATOR_IF_NOT_SPECIFIED && 
                    newBento.getOwnerRef() != null) {
                dataObject.setOwner(newBento.getOwnerRef().toString());
            } else if ( (bdef.getOwnerPolicy() == BentoDef.OwnerPolicy.CREATOR) ||
                     (bdef.getOwnerPolicy() == BentoDef.OwnerPolicy.CREATOR_IF_NOT_SPECIFIED &&
                    newBento.getOwnerRef() == null) )  {
                // fetch principal ID
                Principal principal = getAuthorizer().getPrincipal();
                if (principal != null && principal.getUserRef() != null) {
                    dataObject.setOwner(principal.getUserRef().toString());
                } else
                    // can occur with rescue admin, but not that bad... (user and roles dont have the creator policy)
                    LOG.error(method, "Could not set principal id to created object as principal was null or had no ObjectRef : " + principal);
            } else {
                // for OBJECT and NONE (OBJECT will have its ID set afterwards)
                dataObject.setOwner(null);
            }

            // set as active (TBD: check if status has to be used)
            dataObject.setStatus(newBento.getStatus().name());

            // 3. save the DO
            DataObject savedDo = pm.makePersistent(dataObject);
            newObjectId = new ObjectRef(newBento.getType(), savedDo.getId());

            // set the owner is the object ID if specified
            if (bdef.getOwnerPolicy() == BentoDef.OwnerPolicy.OBJECT) {
                savedDo.setOwner(newObjectId.toString());
                pm.makePersistent(savedDo);
            }

            // audit the create if not a draft
            if (!BentoStatus.DRAFT.name().equals(savedDo.getStatus())) {
                auditLog.setObject(newObjectId);
                AuditManager.get().writeAuditLog(pm, auditLog);

                // notify if needed
                notifier.notify(super.getAuthorizer().getPrincipal(),
                        dataObject.getDisplayName(),
                        newObjectId,
                        AuditableAction.CREATE);
            }

        } catch (InstantiationException ex) {
            LOG.error(method, "InstantiationException when creating DataObject", ex);
            throw new RuntimeException("InstantiationException when creating DataObject", ex);
        } catch (IllegalAccessException ex) {
            LOG.error(method, "IllegalAccessException when creating DataObject", ex);
            throw new RuntimeException("IllegalAccessException when creating DataObject", ex);
        } finally {
            pm.close();
        }
        LOG.leave(method);

        return newObjectId;
    }



    public ObjectRef updateBento(Bento updatedBento)
            throws SecurityException, ValidationException, ObjectNotFoundException, ObjectStaleException, UniqueKeyViolationException, MissingMandatoryAttributeException {
        String method = "updateBento";
        LOG.enter(method);
        assert updatedBento != null : "input param cannot be null";

        // check permissions
        authorize(Action.UPDATE, updatedBento);

        // custom validation if active (e.g. if disabled, no vali. If reenabled, vali!)
        if (updatedBento.getStatus() != BentoStatus.TRASH)
            customValidation(updatedBento);

        PersistenceManager pm = PMF.get().getPersistenceManager();
        try {
            // 1. loads the corresponding DO
            DataObject dataObject = JdoHelper.get().getDataObject(pm, updatedBento.getRef());
            
            // 2. check timestamp, exception if different
            //    NOTE: could also compare attributes modified, to check previous and new value,
            //          and throw exception ONLY if the same attribute was updated...
            Date previousUpdateDate = dataObject.getLastUpdate();
            if (dataObject.getLastUpdate().after(updatedBento.getLastUpdate())) {
                LOG.warning(method, "Object is stale: " + updatedBento.getRef());
                throw new ObjectStaleException("Object is stale: " + updatedBento.getRef());
            }
 
            // check unique attrs uniqueness
            Map<String,String> uniqueAttr2Check = new HashMap<String,String>();
            DataObjectMapper.validateBentoWithDo(updatedBento, dataObject, uniqueAttr2Check);
            if (!uniqueAttr2Check.isEmpty())
                checkUniqueAttributes(pm, JdoHelper.get().getJdoClass(updatedBento.getType()), uniqueAttr2Check);
            // throws UniqueKeyViolationException, MissingMandatoryAttributeException

            // 3. fill attributes
            AuditableAction action = AuditableAction.UPDATE;
            if (BentoStatus.TRASH.name().equals(dataObject.getStatus()) 
                    && !(updatedBento.getStatus() == BentoStatus.TRASH) )
                action = AuditableAction.UNTRASH;
            if (!BentoStatus.TRASH.name().equals(dataObject.getStatus())
                    && (updatedBento.getStatus() == BentoStatus.TRASH))
                action = AuditableAction.TRASH;
            boolean wasDraft = BentoStatus.DRAFT.name().equals(dataObject.getStatus());
            AuditLog auditLog = new AuditLog(action,
                    getAuthorizer().getPrincipal().getUserRef(),
                    updatedBento.getRef());
            DataObjectMapper.bento2do(updatedBento, dataObject, auditLog);
            dataObject.setUpdated();
            LOG.debug(method, "data object is ok, will be persisted");

            // TBD: if unable to rollback, we will need to have a bento2do in 2 steps
            //      1. perform all controls, without touching the do
            //      2. actually update the do
            
            // save the object
            pm.makePersistent(dataObject);

            // audit the update and notify if not a draft
            if (!BentoStatus.DRAFT.name().equals(dataObject.getStatus())) {
                if (wasDraft) {
                    auditLog.setAction(AuditableAction.CREATE);  // audit life of object starts here
                    action = AuditableAction.CREATE;
                }

                AuditManager.get().writeAuditLog(pm, auditLog);
              
                // notify if needed
                if (action != AuditableAction.UPDATE ||
                    (System.currentTimeMillis() - previousUpdateDate.getTime()) > (600 * 1000) )
                         // if it is an update and object was not updated during the last 10 minutes...
                    notifier.notify(super.getAuthorizer().getPrincipal(),
                        dataObject.getDisplayName(), 
                        updatedBento.getRef(),
                        action);
            }

        } finally {
            pm.close();
        }
        LOG.leave(method);

        // returned objectRef is here only to have the same signateur as the create...
        return updatedBento.getRef();
    }


    public void permanentDeleteBento(ObjectRef objectToDelete) throws SecurityException, ObjectNotFoundException {
        String method = "permanentDeleteBento";
        LOG.enter(method);
        assert objectToDelete != null : "objectToDelete cannot be null";

        // perform delete in JDO
        PersistenceManager pm = PMF.get().getPersistenceManager();
        try {
            DataObject dataObject = JdoHelper.get().getDataObject(pm, objectToDelete);

            // check permissions
            if ( BentoStatus.DRAFT.name().equals(dataObject.getStatus()) ) {
                authorize(Action.DRAFT, objectToDelete);
            } else
                authorize(Action.ADMIN, objectToDelete.getType());

            AuditLog auditLog = new AuditLog(AuditableAction.DELETE,
                    super.getAuthorizer().getPrincipal().getUserRef(),objectToDelete);
            pm.deletePersistent(dataObject);

            AuditManager.get().writeAuditLog(pm, auditLog);
        } finally {
            pm.close();
        }

        LOG.leave(method);
    }


    public void addComment(ObjectRef objectToComment, BentoComment newComment) throws SecurityException, ObjectNotFoundException {
        String method = "addComment";
        LOG.enter(method);
        assert objectToComment != null : "input object cannot be null";
        assert newComment != null : "input comment cannot be null";

        if (newComment == null || newComment.getCommentText() == null) {
            LOG.error(method, "Comment is null");
            return;
        }
            
        // check permissions
        authorize(Action.COMMENT, objectToComment); 

        PersistenceManager pm = PMF.get().getPersistenceManager();
        try {
            // 1. loads the corresponding DO
            DataObject dataObject = JdoHelper.get().getDataObject(pm, objectToComment);

            LOG.debug(method, "before: " + dataObject.getComments());

            // do this server-side to ensure correctness
            newComment.setCommenterName(getAuthorizer().getPrincipal().getUserRef().toString());
            newComment.setCommentDate(new Date());

            String c =  CommentMapper.bento2String(newComment);
            dataObject.getComments().add(c);
            dataObject.setUpdated();

            // Log only the comment text
            AuditLog auditLog = new AuditLog(AuditableAction.COMMENT,
                    super.getAuthorizer().getPrincipal().getUserRef(),objectToComment);
            auditLog.getAuditLogAttributes().add(new AuditLogAttribute
                    ("commentText", null, newComment.getCommentText()));

            // save the object with new comment
            pm.makePersistent(dataObject);

            LOG.debug(method, "after: " + dataObject.getComments());

            // set an entry in audit log
            if (!BentoStatus.DRAFT.name().equals(dataObject.getStatus())) {
                AuditManager.get().writeAuditLog(pm, auditLog);

                // notify if needed
                notifier.notify(super.getAuthorizer().getPrincipal(),
                    dataObject.getDisplayName(),
                    objectToComment,
                    AuditableAction.COMMENT,
                    "<i>" + newComment.getCommentText().replaceAll("\n", "<br/>") + "</i>");
            }
           

        } finally {
            pm.close();
        }
        LOG.leave(method);
    }

    
    public void deleteComment(ObjectRef commentedObject, BentoComment commentToDelete) throws SecurityException, ObjectNotFoundException {
        String method = "deleteComment";
        LOG.enter(method);
        assert commentedObject != null;
        assert commentToDelete != null;

        // check permissions
        authorize(Action.UPDATE, commentedObject);

        PersistenceManager pm = PMF.get().getPersistenceManager();
        try {
            // 1. loads the corresponding DO
            DataObject dataObject = JdoHelper.get().getDataObject(pm, commentedObject);

            List<String> comments = dataObject.getComments();
            if (comments == null)
                throw new ObjectNotFoundException("Object " + commentedObject + " has no comments");
            String toRemove = null;
            for (String c : comments) {
                if (c.startsWith(""+commentToDelete.getCommentDate().getTime())) {
                    // kind of dangerous to check only the date, but no real unique id for comments...
                    toRemove = c;
                }
            }
            if (toRemove != null) {
                // Log the comment to be removed
                AuditLog auditLog = new AuditLog(AuditableAction.UPDATE,
                        super.getAuthorizer().getPrincipal().getUserRef(),commentedObject);
                auditLog.getAuditLogAttributes().add(new AuditLogAttribute
                        ("commentText", commentToDelete.getCommentText(), null));
                auditLog.getAuditLogAttributes().add(new AuditLogAttribute
                        ("commenterName", commentToDelete.getCommenterName(), null));
                auditLog.getAuditLogAttributes().add(new AuditLogAttribute
                        ("commentDate", Formaters.get().formatDate(commentToDelete.getCommentDate(),DatePattern.FULL), null));

                dataObject.getComments().remove(toRemove);
                dataObject.setUpdated();
                pm.makePersistent(dataObject);

                // set an entry in audit log
                if (!BentoStatus.DRAFT.name().equals(dataObject.getStatus())) {
                    AuditManager.get().writeAuditLog(pm, auditLog);
                }
            } else {
                throw new ObjectNotFoundException("Object " + commentedObject + " does not contain the comment to be removed");
            }
        } finally {
            pm.close();
        }
        LOG.leave(method);
    }



    public void deleteFile(ObjectRef bentoRef, Long fileId) throws SecurityException, ObjectNotFoundException {
        String method = "deleteFile";
        LOG.enter(method);
        assert bentoRef != null;
        assert fileId != null;

        // check permissions
        authorize(Action.UPDATE, bentoRef);

        PersistenceManager pm = PMF.get().getPersistenceManager();
        try {
            // 1. loads the corresponding DO
            DataObject dataObject = JdoHelper.get().getDataObject(pm, bentoRef);

            if (dataObject instanceof FileHolder) {
                FileHolder fdo = (FileHolder)dataObject;
                List<File> files = fdo.getFiles();
                if (files == null || files.size() == 0)
                    throw new ObjectNotFoundException("Object " + bentoRef + " has no files");
                File toRemove = null;
                for (File f : files)
                    if (f.getFileKey().getId() == fileId.longValue())
                        toRemove = f;
                if (toRemove != null) {
                    
                    // Log the file to be removed
                    AuditLog auditLog = new AuditLog(AuditableAction.UPDATE,
                            super.getAuthorizer().getPrincipal().getUserRef(), bentoRef);
                    auditLog.getAuditLogAttributes().add(new AuditLogAttribute
                        ("fileName", toRemove.getFileName(), null));
                    auditLog.getAuditLogAttributes().add(new AuditLogAttribute
                        ("contentType", toRemove.getFileContentType(), null));

                    pm.deletePersistent(toRemove);
                    fdo.setUpdated();
                    pm.makePersistent(fdo);

                    // set an entry in audit log
                    if (!BentoStatus.DRAFT.name().equals(dataObject.getStatus())) {
                        AuditManager.get().writeAuditLog(pm, auditLog);

                        // note that there is no notification sent... (yet)
                    }
                } else
                    throw new ObjectNotFoundException("File " + fileId.toString() + " not found in " + bentoRef);
            }

            else {
                LOG.error(method, "Object is not allowed to contain files:" + bentoRef);
                throw new ObjectNotFoundException("Object is not allowed to contain files...");
            }
        } finally {
            pm.close();
        }
        LOG.leave(method);
    }



    private void checkUniqueAttributes(PersistenceManager pm, Class classType, Map<String,String> uniqueAttrs)
             throws UniqueKeyViolationException
    {
        for (String attrName : uniqueAttrs.keySet()) {

            Query query = pm.newQuery("select from " +classType.getName() +
                          " where " + attrName + " == attrParam " +
                          " parameters String attrParam");
            List list = (List)query.execute(uniqueAttrs.get(attrName));

            if (list != null && list.size() > 0) {
                LOG.warning("checkUniqueAttributes", "A unique attribute is not unique : " +
                        attrName + "=" + uniqueAttrs.get(attrName));
                throw new UniqueKeyViolationException(attrName, uniqueAttrs.get(attrName)); 
            }
        }
    }


    private void customValidation(Bento bento) throws ValidationException  {
        if (validators.containsKey(bento.getType())) {
            Validator vali = validators.get(bento.getType());
            if (vali != null)
                vali.validate(bento);
        }
    }


}
