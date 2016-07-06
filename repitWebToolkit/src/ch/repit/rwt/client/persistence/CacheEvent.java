/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.repit.rwt.client.persistence;

import ch.repit.rwt.client.ObjectRef;
import ch.repit.rwt.client.persistence.BentoCache.CacheLoadLevel;
import java.util.HashSet;
import java.util.Set;


/**
 *
 * @author tc149752
 */
public class CacheEvent  {

    private CacheEventType eventType;
    private Set<String>    concernedTypes;   // only for updates and deletes
    private Set<ObjectRef> concernedObjects; // only for updates and deletes
    private CacheLoadLevel loadLevel;

    public CacheEvent(CacheEventType eventType, CacheLoadLevel loadLevel) {
        this.eventType = eventType;
        this.loadLevel = loadLevel;
    }
    public CacheEvent(CacheEventType eventType, Set<ObjectRef> concernedObjects) {
        this.eventType = eventType;
        this.concernedObjects = concernedObjects;
        if (concernedObjects != null)
            concernedTypes = new HashSet<String>();
            for (ObjectRef ref : concernedObjects)
                if (!concernedTypes.contains(ref.getType()))
                    concernedTypes.add(ref.getType());
    }

    public Set<ObjectRef> getConcernedObjects() {
        return concernedObjects;
    }

    public Set<String> getConcernedTypes() {
        return concernedTypes;
    }

    public CacheEventType getEventType() {
        return eventType;
    }

    public CacheLoadLevel getLoadLevel() {
        return loadLevel;
    }

    @Override
    public String toString() {
        return "Type=" + eventType + ";concernedObjects=" + concernedObjects;
    }


    public enum CacheEventType {
        FULL_RELOAD,
        UPDATES,
        DELETES
    }

}
