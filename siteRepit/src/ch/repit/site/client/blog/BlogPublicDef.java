package ch.repit.site.client.blog;

import ch.repit.rwt.client.AttributeDef;
import ch.repit.rwt.client.AttributeDef.Feature;
import ch.repit.rwt.client.AttributeType;
import ch.repit.rwt.client.annotations.IncludeInAlerts;
import ch.repit.rwt.client.annotations.IncludeInReports;


@IncludeInAlerts
@IncludeInReports
public class BlogPublicDef extends BlogDef {

    public static final String CATEGORY_NAME = "public";  // do not update

    public static final String TYPE = "BlogPublic";

    static {
        blogAttrDefs.add(new AttributeDef(ATTR_CATEGORY,AttributeType.STRING, CATEGORY_NAME, Feature.MANDATORY));
    }

    public BlogPublicDef() {
        super(blogAttrDefs);
    }



    @Override
    public String getType() {
        return TYPE;
    }
    
    @Override
    public String getJdoFilter() {
        return " category == '"+CATEGORY_NAME+"' ";
    }

    @Override
    public String getTypeLabel() {
        return "Blog";
    }


}
