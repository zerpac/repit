/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.repit.site.server.blog;

import ch.repit.site.client.blog.BlogDef;
import ch.repit.site.client.blog.BlogOfficialDef;
import ch.repit.site.client.blog.BlogPublicDef;
import ch.repit.rwt.client.BentoDefFactory;
import ch.repit.rwt.client.ContentTypeFamily;
import ch.repit.rwt.server.file.FileHolder;
import ch.repit.rwt.server.persistence.BaseDataObject;

import com.google.appengine.api.datastore.Text;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;


@PersistenceCapable(identityType = IdentityType.APPLICATION)
@Inheritance(customStrategy = "complete-table")
public class Blog extends BaseDataObject implements FileHolder {

    @Persistent
    private String category;

    @Persistent
    private String subject;

    @Persistent
    private Text body;  

    @Persistent
    private Date publicationDate;

    @Persistent
    private List<BlogFile> blogFiles = new ArrayList<BlogFile>();


    // constructor for creates
    public Blog() { }

    @Override
    public String getDisplayName() {
        return getDef().getTypeLabel() + " \"" + getSubject() + "\"";
    }

    public String getSubject() {
        return subject;
    }
    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        if (body == null)
            return null;
        else
            return body.getValue();
    }
    public void setBody(String body) {
        this.body = new Text(body);
    }

    public Date getPublicationDate() {
        return publicationDate;
    }

    public void setPublicationDate(Date publicationDate) {
        this.publicationDate = publicationDate;
    }

    public String getCategory() {
        return category;
    }

    public BlogDef getDef() {
        // hacky...
        if (BlogOfficialDef.CATEGORY_NAME.equals(category))
            return (BlogDef)BentoDefFactory.get().getDef(BlogOfficialDef.TYPE);
        return (BlogDef)BentoDefFactory.get().getDef(BlogPublicDef.TYPE);
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public List<BlogFile> getFiles() {
        return blogFiles;
    }

    public BlogFile createFileInstance() {
        BlogFile blogFile = new BlogFile();
        blogFiles.add(blogFile);
        return blogFile;
    }

    private transient Set<ContentTypeFamily> allowedFileTypes = null;

    public Set<ContentTypeFamily> listAllowedFileTypes() {
        if (allowedFileTypes == null || allowedFileTypes.size() == 0) {
            Set<ContentTypeFamily> copy = new HashSet<ContentTypeFamily>();
            copy.add(ContentTypeFamily.PDF);
            copy.add(ContentTypeFamily.IMAGE);
            copy.add(ContentTypeFamily.OPEN_OFFICE);
            copy.add(ContentTypeFamily.MS_OFFICE);
            copy.add(ContentTypeFamily.ARCHIVE);
         //   copy.add(ContentTypeFamily.MOVIE);  forget movies, it is not the goal!
            allowedFileTypes = copy;
        }
        return allowedFileTypes;
    }

    public int getImagePreviewSize() {
        return 240;
    }

    public long getFileMaxSizeInBytes() {
        return 1000000;
    }

}
