/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.repit.site.client.blog;

import ch.repit.rwt.client.AttributeDef;
import ch.repit.rwt.client.AttributeDef.Feature;
import ch.repit.rwt.client.AttributeType;
import ch.repit.rwt.client.BentoDef;
import ch.repit.rwt.client.BentoStatus;
import ch.repit.rwt.client.ObjectRef;
import ch.repit.rwt.client.security.Action;
import ch.repit.rwt.client.ui.Page;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author tc149752
 */
public abstract class BlogDef extends BentoDef {

    public static final String ATTR_SUBJECT   = "subject";
    public static final String ATTR_BODY      = "body";
    public static final String ATTR_PUBDATE   = "publicationDate";
    public static final String ATTR_CATEGORY  = "category";


    protected static Set<AttributeDef> blogAttrDefs = new HashSet<AttributeDef>();
    static {
        blogAttrDefs.add(new AttributeDef(ATTR_SUBJECT, AttributeType.STRING, Feature.MANDATORY));  // removed UNIQUE...
        blogAttrDefs.add(new AttributeDef(ATTR_BODY,    AttributeType.STRING, " ", Feature.NOT_AUDITABLE));
        blogAttrDefs.add(new AttributeDef(ATTR_PUBDATE, AttributeType.DATE));
    }

    public BlogDef() {
        super(blogAttrDefs);
    }

    protected BlogDef(Set<AttributeDef> attributeDefs) {
        super(attributeDefs);
    }


    @Override
    public String getDistinguishedAttribute() {
        return ATTR_SUBJECT;
    }

    @Override
    public String getJdoClassName() {
        return "ch.repit.site.server.blog.Blog";
    }

    @Override
    public List<Action> supportedActionsAll() {
        List<Action> list = super.supportedActionsAll();
        if (!list.contains(Action.DRAFT))
            list.add(Action.DRAFT);
        return list;
    }

    @Override
    public List<Action> supportedActionsOwn() {
        List<Action> list = super.supportedActionsOwn();
        if (!list.contains(Action.DRAFT))
            list.add(Action.DRAFT);
        return list;
    }

    @Override
    public Page getViewPage(ObjectRef oref) {
        return new BlogPage(this, oref, null);
    }


    @Override
    public BentoStatus getDefaultStatus() {
        return BentoStatus.DRAFT;
    }
}
