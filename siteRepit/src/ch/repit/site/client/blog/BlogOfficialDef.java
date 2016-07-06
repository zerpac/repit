package ch.repit.site.client.blog;

import ch.repit.rwt.client.AttributeDef;
import ch.repit.rwt.client.AttributeDef.Feature;
import ch.repit.rwt.client.AttributeType;
import ch.repit.rwt.client.annotations.IncludeInAlerts;
import ch.repit.rwt.client.annotations.IncludeInReports;


@IncludeInAlerts
@IncludeInReports
public class BlogOfficialDef extends BlogDef {

    public static final String CATEGORY_NAME = "official"; // DO NOT UPDATE !!!
    public static final String TYPE = "BlogOfficial";

    static {
        blogAttrDefs.add(new AttributeDef(ATTR_CATEGORY,AttributeType.STRING, CATEGORY_NAME, Feature.MANDATORY));
    }

    public BlogOfficialDef() {
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
        return "Document Officiel";
    }

}
