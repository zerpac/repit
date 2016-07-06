/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.repit.rwt.client.persistence;

import ch.repit.rwt.client.Bento;
import ch.repit.rwt.client.BentoComment;
import ch.repit.rwt.client.ObjectRef;
import ch.repit.rwt.client.persistence.BentoCache.CacheLoadLevel;
import com.google.gwt.user.client.rpc.AsyncCallback;
import java.util.Date;

/**
 *
 * @author tc149752
 */
public interface PersistenceServiceAsync {

    // should go in another service...
    public void loadCache(CacheLoadLevel level, AsyncCallback<BentoCache> callback);
    public void loadCache(CacheLoadLevel level, ObjectRef directAccess, AsyncCallback<BentoCache> callback);
    
    public void refreshCache(Date cacheAge, AsyncCallback<BentoCache> callback);
    

    public void createBento(Bento newBento, AsyncCallback<ObjectRef> callback);

    public void updateBento(Bento updatedBento, AsyncCallback<ObjectRef> callback);

    public void permanentDeleteBento(ObjectRef objectToDelete, AsyncCallback<Void> callback);

    public void addComment(ObjectRef objectToComment, BentoComment newComment, AsyncCallback<Void> callback);

    public void deleteComment(ObjectRef commentedObject, BentoComment commentToDelete, AsyncCallback<Void> callback);

    public void deleteFile(ObjectRef bentoRef, Long fileId, AsyncCallback<Void> callback);

}
