/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.repit.rwt.client.persistence;

import ch.repit.rwt.client.Bento;
import ch.repit.rwt.client.ObjectRef;
import ch.repit.rwt.client.logs.LogManager;
import ch.repit.rwt.client.persistence.BentoCache.CacheLoadLevel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.StatusCodeException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Manages caches of DTO lists of various types, as a singleton
 * ONLY CLIENT-SIDE
 */
public class CacheManager
{
    private static final CacheManager s_instance = new CacheManager();

    private BentoCache localCache;
    private List<CacheEventHandler> m_eventHandlers = new ArrayList<CacheEventHandler>();
    private PersistenceServiceAsync persistenceService;
    private boolean activated = false;


    public static CacheManager get() {
        return s_instance;
    }


    private CacheManager() {
        
    }

    public void activate() {
        if (!activated) {
            refreshCache();
            Timer t = new Timer() {
                public void run() {
                    refreshCache();
                }
            };
            t.scheduleRepeating(60000);  // 1 minute delay between refreshes
        }
        activated = true;
    }


    public PersistenceServiceAsync getPersistenceService() {
        if (persistenceService == null) {
            persistenceService = GWT.create(PersistenceService.class);
        }
        return persistenceService;
    }


    public void registerEventHandler(CacheEventHandler handler) {
        if (!m_eventHandlers.contains(handler))
            m_eventHandlers.add(handler);
    }

    public void unregisterEventHandler(CacheEventHandler handler) {
        m_eventHandlers.remove(handler);
    }

    
    public List<Bento> getCachedObjects(String type) {
        if (localCache == null)
            return null;
        else {
            if (localCache.getTypes().contains(type))
                return localCache.getObjects(type);
            else {
                List<Bento> result = new ArrayList();
                for (String t : localCache.getTypes())
                    if (t.startsWith(type))
                        result.addAll(localCache.getObjects(t));
                return result;
            }
        }
    }

    public Bento getCachedObject(String ref) {
        return getCachedObject(new ObjectRef(ref));
    }
    
    public Bento getCachedObject(ObjectRef ref) {
        if (localCache == null || ref == null ||
                localCache.getObjects(ref.getType()) == null)
            return null;
        else {
            for (Bento obj : localCache.getObjects(ref.getType())) {
                if (obj != null && obj.getId() != null &&
                        obj.getId().equals(ref.getId()))
                    return obj;
            }
            return null;
        }
    }

    public Set<String> getCachedObjectsTypes() {
        return localCache.getTypes();
    }
    

    public void refreshCache() {
        refreshCache(false);
    }
    public void refreshCache(boolean force) {
        if (force || localCache == null) {
            LogManager.get().handling("Chargement du cache "+CacheLoadLevel.INCREMENTAL1.getLabel()+" en cours...");

            // adds requested object for direct links or twitter in the first query
            String hashRaw = Window.Location.getHash();
            ObjectRef requestedBentoRef = null;
            if (hashRaw != null && hashRaw.length() > 0) {
                String hash = hashRaw.substring(1);  // removes leading #
                ObjectRef oref = new ObjectRef(hash);
                if (oref != null && oref.getType()!=null && oref.getId() != null) {
                    requestedBentoRef = oref;
                }
            }

            if (requestedBentoRef==null)
                getPersistenceService().loadCache(CacheLoadLevel.INCREMENTAL1, refreshCallback);
            else
                getPersistenceService().loadCache(CacheLoadLevel.INCREMENTAL1, requestedBentoRef, refreshCallback);
        }
        else {
            LogManager.get().debug("Rafraîchissement du cache en cours");
            getPersistenceService().refreshCache(localCache.getCacheAge(), refreshCallback);
        }
    }

    
    AsyncCallback<BentoCache> refreshCallback = new AsyncCallback<BentoCache>() {
        public void onFailure(Throwable caught) {
            if (caught instanceof StatusCodeException)
                LogManager.get().warningConnectionLost();
            else
                LogManager.get().warning("Erreur lors de la lecture du cache auprès du serveur", caught);
        }
        public void onSuccess(BentoCache newCache) {

            LogManager.get().infoConnectionOk();
            LogManager.get().debug("Server-side cache refresh ok; now doing the local update; newCache=" + newCache);

            // store new cache if full cache
            if (newCache.getCacheLevel().canThrowPrevious()) {
                localCache = newCache;
            }

            // otherwise, stores the differences
            else {
                // handles creates or updates
                Set<ObjectRef> objRefs = new HashSet<ObjectRef>();
                for (String t : newCache.getTypes()) {
                    for (Bento newDto : newCache.getObjects(t)) {
                        Bento oldDto = getCachedObject(newDto.getRef());
                        if (localCache.getObjects(t) == null)
                            localCache.putObjects(t, new ArrayList<Bento>());
                        if (oldDto != null)
                            localCache.getObjects(t).remove(oldDto);
                        localCache.getObjects(t).add(newDto);
                        // adds an event for that
                        objRefs.add(newDto.getRef());
                    }
                }
                if (objRefs.size() > 0)
                    CacheManager.get().notifyCacheEvent(new CacheEvent(CacheEvent.CacheEventType.UPDATES, objRefs));

                // handles deletes 
                if (newCache.getDeletedObjects() != null) {
                    for (ObjectRef ref : newCache.getDeletedObjects()) {
                        Bento dtoToRemove = getCachedObject(ref);
                        if (dtoToRemove != null && localCache.getObjects(ref.getType()) != null)
                            localCache.getObjects(ref.getType()).remove(dtoToRemove);
                    }
                    CacheManager.get().notifyCacheEvent(new CacheEvent(CacheEvent.CacheEventType.DELETES,
                            (Set<ObjectRef>)newCache.getDeletedObjects()));
                }

                // sets the cache new date
                localCache.setCacheAge(newCache.getCacheAge());
                localCache.setCacheLevel(newCache.getCacheLevel());
            }

            // invokes next step if needed
            if (localCache.getCacheLevel() != CacheLoadLevel.REFRESH) {
                CacheLoadLevel nextLevel = localCache.getCacheLevel().getNextLevel(); 
                // notify update
                CacheManager.get().notifyCacheEvent(new CacheEvent(CacheEvent.CacheEventType.FULL_RELOAD, localCache.getCacheLevel()));
                LogManager.get().handled("Chargement du cache "+localCache.getCacheLevel().getLabel()+" terminé");

                if (nextLevel != null) {
                    LogManager.get().handling("Chargement du cache "+nextLevel.getLabel()+" en cours");
                    getPersistenceService().loadCache(nextLevel, refreshCallback);
                }
            }

            else {
                LogManager.get().debug("Rafraîchissement du cache terminé");
            }
        }
    };


    void notifyCacheEvent(CacheEvent event) {
        if (event != null) {
            for (CacheEventHandler handler : new ArrayList<CacheEventHandler>(m_eventHandlers)) {
                // performs a copy of the list to avoid ConcurrentModificationException... 
                try {
                    handler.onCacheEvent(event);
                } catch (RuntimeException e) {
                    // We dont want any exception in handlers messing up with cache activity, so we sink it
                    LogManager.get().error("Exception while handling " + handler.toString(), e);
                    throw e; // for devMode
                }
            }
        }
    }

}
