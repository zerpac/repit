/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.repit.site.client.blog;

import ch.repit.rwt.client.Bento;
import ch.repit.rwt.client.BentoStatus;
import ch.repit.rwt.client.persistence.CacheManager;
import ch.repit.rwt.client.ui.BentoListPage;
import ch.repit.rwt.client.ui.Page;
import ch.repit.rwt.client.security.Action;
import ch.repit.rwt.client.security.Authorizer;
import ch.repit.rwt.client.security.SecurityManager;
import ch.repit.rwt.client.util.Formaters;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.HTML;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 *
 * @author tc149752
 */
public class BlogsListPage extends BentoListPage {


    public BlogsListPage(Page topPage, String type, BentoStatus... statusses)
    {
        this(topPage, type, NOLIMIT, statusses);
    }

    public BlogsListPage(Page topPage, String type, int onlyLastX, BentoStatus... statusses)
    {
        super(topPage, type, statusses);
   
        super.setRecentCountLimit(onlyLastX);
        setPrintable(false);

        super.addColumn("Auteur", "_poster", true);
        super.addColumn("Sujet", BlogDef.ATTR_SUBJECT, true);
        super.addColumn("Contenu (extrait)", "_contentExtract", false);

        super.addColumn(GWT.getModuleBaseURL()+"icons/paperclip.gif", "nombre de fichiers attachés", "_numFiles", true);
        super.addNumberCommentsColumn();

        // set the columns
        if ( !(listSupportedStatus().contains(BentoStatus.DRAFT) && listSupportedStatus().size()==1) )
            super.addColumn("Publié", BlogDef.ATTR_PUBDATE, true);

        super.addColumn("Modifié", "_objectLastUpdate", true);
        if (super.listTypes().size() > 1)
            super.addColumn("Catégorie", "_categoryLabel", true);  // this will never be reached...

        super.setSortColumn("_objectLastUpdate", false);
    }


    @Override
    protected List<Bento> filterData(List<Bento> objectList)
    {
        Authorizer auth = SecurityManager.get().getAuthorizer();
        if (objectList == null)
            return objectList;
        List<Bento> results = new ArrayList<Bento>();
        for (Bento b : objectList) {

            if ( listSupportedStatus().contains( b.getStatus() ) ) {

                if ( b.getStatus()==BentoStatus.TRASH ) {
                    if (auth.isAllowed(Action.VIEW_TRASH, b))  // ??? is this correct ???
                        results.add(b);
                }
                else {
                    if (b.getStatus()==BentoStatus.DRAFT) {
                        // for drafts, check if update rights on this specific object
                        if (auth.isAllowed(Action.UPDATE, b))
                            results.add(b);
                    }
                    else 
                        results.add(b);
                }
            }
        }
        return results;
    }



    @Override
    protected boolean formatObject(Bento bento, Map formatedValue) {
        formatedValue.put("_objectLastUpdate", Formaters.get().formatDate(bento.getLastUpdate()));
        // blog owner
        Bento owner = CacheManager.get().getCachedObject(bento.getOwnerRef());
        if (owner != null)
            formatedValue.put("_poster", owner.getDef().getCommonName(owner));

        // category label
        formatedValue.put("_categoryLabel", ((BlogDef)bento.getDef()).getTypeLabel());

        // content extract
        String content = bento.get(BlogDef.ATTR_BODY).getString();
        String contentExtract = content.replaceAll("<(.|\n)*?>", " ");
        if (contentExtract.length() > 110) {
            contentExtract = contentExtract.substring(0, 100) + " [...]";
        }
        HTML ce = new HTML(contentExtract);
        ce.addStyleName("repit-listDisplayCellMultiLine");
        formatedValue.put("_contentExtract", ce);

        // num files
        formatedValue.put("_numFiles", bento.getAttachedFiles()==null?"":""+bento.getAttachedFiles().length);
        
        return super.formatObject(bento, formatedValue);
    }
    

    @Override
    protected int sortCompare(Bento bento1, Bento bento2, String sortAttribute, boolean ascending) {

        int inverse = ascending?1:-1;

        if (sortAttribute.equals("_objectLastUpdate")) {
            return bento1.getLastUpdate().compareTo(bento2.getLastUpdate()) * inverse;    
        }
        else if (sortAttribute.equals("_poster")) {
            return (""+bento1.getOwnerRef()).compareTo((""+bento2.getOwnerRef())) * inverse;
        }
        else if (sortAttribute.equals("_numFiles")) {
            Integer c1 = bento1.getAttachedFiles()==null?0:bento1.getAttachedFiles().length;
            Integer c2 = bento2.getAttachedFiles()==null?0:bento2.getAttachedFiles().length;
            return c1.compareTo(c2) * inverse;
        }

        else
            return super.sortCompare(bento1, bento2, sortAttribute, ascending);
    }


    @Override
    protected void onRowClicked(Bento data, String columnClicked) {
        getPageNav().displayPage(new BlogPage((BlogDef)data.getDef(), data.getRef(), null));
    }
}