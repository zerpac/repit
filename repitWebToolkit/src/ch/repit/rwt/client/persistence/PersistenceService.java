/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.repit.rwt.client.persistence;


import ch.repit.rwt.client.Bento;
import ch.repit.rwt.client.BentoComment;
import ch.repit.rwt.client.ObjectRef;
import ch.repit.rwt.client.persistence.BentoCache.CacheLoadLevel;
import ch.repit.rwt.client.security.SecurityException;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import java.util.Date;



/**
 * Service interface for user management
 */
@RemoteServiceRelativePath("persistenceService")
public interface PersistenceService extends RemoteService
{

    public BentoCache loadCache(CacheLoadLevel level)
            throws SecurityException;
    public BentoCache loadCache(CacheLoadLevel level, ObjectRef directAccess)
            throws SecurityException;

    public BentoCache refreshCache(Date cacheAge)
            throws SecurityException;


    public ObjectRef createBento(Bento newBento)
            throws SecurityException,
                   ValidationException,
                   UniqueKeyViolationException,
                   MissingMandatoryAttributeException;


    public ObjectRef updateBento(Bento updatedBento)
            throws SecurityException,
                   ValidationException,
                   UniqueKeyViolationException,
                   ObjectNotFoundException,
                   ObjectStaleException,
                   MissingMandatoryAttributeException;


    public void permanentDeleteBento(ObjectRef objectToDelete)
            throws SecurityException, ObjectNotFoundException;
    

    public void addComment(ObjectRef objectToComment, BentoComment newComment)
            throws SecurityException, ObjectNotFoundException;

    
    public void deleteComment(ObjectRef commentedObject, BentoComment commentToDelete)
            throws SecurityException, ObjectNotFoundException;


    public void deleteFile(ObjectRef bentoRef, Long fileId)
            throws SecurityException, ObjectNotFoundException;

}
