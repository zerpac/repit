package ch.repit.rwt.client;

import ch.repit.rwt.client.audit.AuditQueryDef;
import ch.repit.rwt.client.pdf.StickerConfigDef;
import ch.repit.rwt.client.security.RoleDef;
import ch.repit.rwt.client.user.UserDef;
import ch.repit.rwt.client.user.UserPrefDef;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Singleton class that manages BentoDef instances, both sides
 */
public class BentoDefFactory {

    private static BentoDefFactory instance = new BentoDefFactory();
    public static BentoDefFactory get() {  return instance;  }

    private BentoDefFactory() {
        registerDef(new RoleDef());
        registerDef(new AuditQueryDef());

        // TBD: these are repit types, should be defined in a repit class...
//        registerDef(new UserDef());
//        registerDef(new UserPrefDef());
//        registerDef(new StickerConfigDef());
    }

    private Map<String,BentoDef> bentoDefs = new HashMap<String,BentoDef>();


    
    public void registerDefs(BentoDef... bentoDefs) {
        if (bentoDefs != null ) {
            for (BentoDef bd : bentoDefs)
                registerDef(bd);
        }
    }



    public void registerDef(BentoDef bentoDef) {
        if (bentoDef != null) {
            bentoDefs.put(bentoDef.getType(), bentoDef);
        }
    }

    public BentoDef getDef(String type) {
        return bentoDefs.get(type);
    }

    public Set<String> getTypes() {
        return bentoDefs.keySet();
    }

    public Collection<BentoDef> getDefs() {
        return bentoDefs.values();
    }
}
