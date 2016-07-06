/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.repit.rwt.client.persistence;

import ch.repit.rwt.client.Bento;
import ch.repit.rwt.client.ObjectRef;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * 
 */
public class BentoCache implements Serializable {
    
    private Date cacheAge;
    private Map<String,List<Bento>> objects = new HashMap<String,List<Bento>>();
    private Set<ObjectRef> deletedObjects;

    private CacheLoadLevel cacheLevel;

    
    public BentoCache() { }



    public Date getCacheAge() {
        return cacheAge;
    }

    public void setCacheAge(Date cacheAge) {
        this.cacheAge = cacheAge;
    }

    public void putObjects(String type, List<Bento> listTO) {
        objects.put(type, listTO);
    }

    public Set<String> getTypes() {
        return objects.keySet();
    }

    public List<Bento> getObjects(String type) {
        return objects.get(type);
    }

    public CacheLoadLevel getCacheLevel() {
        return cacheLevel;
    }

    public void setCacheLevel(CacheLoadLevel cacheLevel) {
        this.cacheLevel = cacheLevel;
    }


    public void addDeletedObject(ObjectRef objectRef) {
        if (deletedObjects == null)
            deletedObjects =  new HashSet<ObjectRef>();
        deletedObjects.add(objectRef);
    }

    public Set<ObjectRef> getDeletedObjects() {
        return deletedObjects;
    }

    
    @Override
    public String toString() {
        return "BentoCache{Age=" + cacheAge + " ; cacheLevel=" + cacheLevel.name() +
                " ; objects=" + objects + " ; deletedObjects=" + deletedObjects;
    }


    public enum CacheLoadLevel {
        FULL         (true, "complet"),     // all
        INCREMENTAL3 (false, "secondaire"),     // archive and trash all objects
        INCREMENTAL2 (true, "primaire", INCREMENTAL3),     // draft and actives all objects
        INCREMENTAL1 (true, "minimal", INCREMENTAL2),     // active users and roles
        REFRESH      (false, "incrémental");

        private CacheLoadLevel nextLevel = null;
        private boolean throwPrevious;
        private String label;
        
        private CacheLoadLevel(boolean throwPrevious, String label, CacheLoadLevel... nextLevels) {
            if (nextLevels!=null && nextLevels.length > 0)
                nextLevel = nextLevels[0];
            this.throwPrevious = throwPrevious;
            this.label = label;
        }

        public CacheLoadLevel getNextLevel() {
            return nextLevel;
        }

        public boolean canThrowPrevious() {
            return throwPrevious;
        }

        public String getLabel() {
            return label;
        }

    }

}
