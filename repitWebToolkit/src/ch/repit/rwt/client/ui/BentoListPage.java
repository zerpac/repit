/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.repit.rwt.client.ui;

import ch.repit.rwt.client.Bento;
import ch.repit.rwt.client.BentoAttribute;
import ch.repit.rwt.client.BentoDef;
import ch.repit.rwt.client.BentoDef.LabelGender;
import ch.repit.rwt.client.BentoDefFactory;
import ch.repit.rwt.client.BentoStatus;
import ch.repit.rwt.client.persistence.CacheEvent;
import ch.repit.rwt.client.persistence.CacheEventHandler;
import ch.repit.rwt.client.persistence.CacheManager;
import ch.repit.rwt.client.security.Action;
import ch.repit.rwt.client.util.Formaters;
import ch.repit.rwt.client.util.Formaters.DatePattern;
import ch.repit.rwt.client.security.SecurityManager;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.Widget;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Map;

/**
 *
 * @author tc149752
 */
public abstract class BentoListPage extends ListPage<Bento> implements CacheEventHandler {

    protected final static int NOLIMIT = -1;
    private final static String EMPTY = "-";
    protected final static String NUMBER_COMMENTS = "__numComments";
    
    private Set<BentoStatus> statusSet;
    private Set<String> bentoTypes;
    private int recentCount;
    private Map<String,Formaters.DatePattern> dateFormaters = new HashMap();



    protected BentoListPage(Page topPage, String objectType, BentoStatus... statusToDisplay) {
        super(topPage);

        bentoTypes = new HashSet();
        bentoTypes.add(objectType);

        // status restriction
        statusSet = new HashSet();
        if (statusToDisplay == null || statusToDisplay.length == 0) {
            statusSet.addAll(Arrays.asList(BentoStatus.values()));
        } else
            statusSet.addAll(Arrays.asList(statusToDisplay));

        // all lists are printable by default if not (only) trash
        setPrintable(! statusSet.contains(BentoStatus.TRASH) || statusSet.size()>1);
    }

    
    protected void addObjectTypes(String... types) {
        bentoTypes.addAll(Arrays.asList(types));
    }

    protected Set<BentoStatus> listSupportedStatus() {
        return statusSet;
    }


    @Override
    protected void init() {
        if (getPageNav().isDebugEnabled()) {
            addColumn("ObjectRef",    "__objectRef",     true);
            addColumn("OwnerRef",     "__ownerRef",     false);
            addColumn("Last Update",  "__objectLastUpdate", true);
        }
    }


    @Override
    protected void fillToolbarWidgets(List<Widget> leftWidgets,
                                      List<Widget> middleWidgets,
                                      List<Widget> rightWidgets) {
        super.fillToolbarWidgets(leftWidgets, middleWidgets, rightWidgets);

        // plus button(s)
        for (String type : listTypes()) {

            if ( !(statusSet.contains(BentoStatus.TRASH) && statusSet.size()==1)
                    && SecurityManager.get().getAuthorizer().isAllowed(Action.CREATE, type)) {

                final BentoDef bentoDef = BentoDefFactory.get().getDef(type);
                PushButton newButton = new PushButton(new Image(GWT.getModuleBaseURL()+"icons/plus.png"));
                newButton.addClickHandler(this.getNewObjectHandler(bentoDef));
                newButton.setTitle("Créer un" + ((bentoDef.getLabelGender()==LabelGender.FEMININ)?"e":"")
                        + " " + bentoDef.getTypeLabel());
                newButton.setStylePrimaryName("toto");
                rightWidgets.add(0,newButton);
            }
        }
    }

    /**
     * Can be overridden if default is not good
     * @return
     */
    protected ClickHandler getNewObjectHandler(final BentoDef bentoDef) {
        return new ClickHandler() {
            public void onClick(ClickEvent arg0) {
                getPageNav().displayPage(bentoDef.getViewPage(null));
            }
        };
    }

    public Set<String> listTypes() {
        return bentoTypes;
    }

    public Set<BentoStatus> listStatus() {
        return statusSet;
    }


    public void setRecentCountLimit(int recentCount) {
        this.recentCount = recentCount;
    }

    protected void addDateColumn(String title,
                             String attributeName,
                             boolean sortable,
                             DatePattern datePattern) {
        super.addColumn(title, attributeName, sortable);
        if (datePattern != null)
            dateFormaters.put(attributeName, datePattern);
    }
    
    protected void addNumberCommentsColumn() {
        super.addColumn(GWT.getModuleBaseURL()+"icons/comment-icon.png", "nombre de commentaires", NUMBER_COMMENTS, true);
    }

    @Override
    protected boolean formatObject(Bento bento, Map formatedValue) {

        if (bento == null)
            return false;


        if ( statusSet.contains(bento.getStatus()) ) {

            // adds debug fields
            if (getPageNav().isDebugEnabled()) {
                formatedValue.put("__objectRef", ""+bento.getRef());
                formatedValue.put("__ownerRef", ""+bento.getOwnerRef());
                formatedValue.put("__objectLastUpdate", Formaters.get().formatDate(bento.getLastUpdate(), DatePattern.FULL));
            }

            // common code for number of comments
            if (m_columnAttributeNames.contains(NUMBER_COMMENTS)) {
                formatedValue.put(NUMBER_COMMENTS, bento.getComments()==null?"":""+bento.getComments().length);
            }

            for (String attrName : bento.keySet()) {
                BentoAttribute attr = bento.get(attrName);
                switch (attr.getAttributeType())  {
                    case STRING:
                        if (attr.getString() != null)
                            formatedValue.put(attrName, attr.getString());
                        else
                            formatedValue.put(attrName, EMPTY);
                        break;
                    case INTEGER:
                        if (attr.getInteger() != null)
                            formatedValue.put(attrName, ""+attr.getInteger());
                        else
                            formatedValue.put(attrName, EMPTY);
                        break;
                    case DATE:
                        if (attr.getDate() != null)
                            if (dateFormaters.containsKey(attrName))
                                formatedValue.put(attrName, Formaters.get().formatDate(attr.getDate(), dateFormaters.get(attrName)));
                            else
                                formatedValue.put(attrName, Formaters.get().formatDate(attr.getDate()));
                        else
                            formatedValue.put(attrName, EMPTY);
                        break;
                    case BOOLEAN:
                        CheckBox cb = new CheckBox();
                        cb.setValue( (attr.getBoolean()!=null)?attr.getBoolean():false );
                        cb.setEnabled(false);
                        formatedValue.put(attrName, cb);
                        break;
                    case STRING_LIST:
                        HTML html = new HTML();
                        if (attr.getStringList() != null) {
                            String val = "<ul>";
                            for (String item : attr.getStringList()) {
                                val += "<li>"+item+"</li>";
                            }
                            html.setHTML(val + "</ul>");
                        } else
                            html.setHTML(EMPTY);
                        formatedValue.put(attrName, html);
                        break;
                    default:
                        formatedValue.put(attrName, attr.getDisplayValue());
                        break;
                }
            }
            return true;
         } else {
            return false;
         }
    }


    @Override
    protected int sortCompare(Bento bento1, Bento bento2, String sortAttribute, boolean ascending)
    {
        int inverse = ascending?1:-1;

        // sorts on debug columns
        if (getPageNav().isDebugEnabled()) {
            if (sortAttribute.equals("__objectRef")) {
                return bento1.getId().compareTo(bento2.getId()) * inverse;
            } else if (sortAttribute.equals("__objectLastUpdate")) {
                return bento1.getLastUpdate().compareTo(bento2.getLastUpdate()) * inverse;
            }
        }

        if (sortAttribute.equals(NUMBER_COMMENTS)) {
            Integer c1 = bento1.getComments()==null?0:bento1.getComments().length;
            Integer c2 = bento2.getComments()==null?0:bento2.getComments().length;
            return c1.compareTo(c2) * inverse;
        }

        // assume non is null...
        BentoAttribute attr1 = bento1.get(sortAttribute);
        BentoAttribute attr2 = bento2.get(sortAttribute);

        if (attr1 == null) return inverse;
        if (attr2 == null) return -1*inverse;
        switch (attr1.getAttributeType())  {
            case STRING:
                if (attr1.getString() == null) return inverse;
                if (attr2.getString() == null) return -1*inverse;
                return (attr1.getString().compareToIgnoreCase(attr2.getString())) * inverse;
            case INTEGER:
                if (attr1.getInteger() == null) return inverse;
                if (attr2.getInteger() == null) return -1*inverse;
                return (attr1.getInteger().compareTo(attr2.getInteger())) * inverse;
            case DATE:
                if (attr1.getDate() == null) return inverse;
                if (attr2.getDate() == null) return -1*inverse;
                return (attr1.getDate().compareTo(attr2.getDate())) * inverse;
            case DAY:
                if (attr1.getDay() == null) return inverse;
                if (attr2.getDay() == null) return -1*inverse;
                return (attr1.getDay().getInteger().compareTo(attr2.getDay().getInteger())) * inverse;
            case BOOLEAN:
                if (attr1.getBoolean() == null) return inverse;
                if (attr2.getBoolean() == null) return -1*inverse;
                return (attr1.getBoolean().compareTo(attr2.getBoolean())) * inverse;
            case STRING_LIST:
                return 0; // list are not sortable
            default:
                return 0;
        }
    }



    @Override
    protected void doUnLayout() {
        // unregister the event handler 
        CacheManager.get().unregisterEventHandler(this);

        super.doUnLayout();
    }

    @Override
    protected Widget doContentlayout() {
        // register the event handler
        CacheManager.get().registerEventHandler(this);

        // sets the initial data, if present
        resetBentoList();

        return super.doContentlayout();
    }
    

    public void onCacheEvent(CacheEvent event) {
        // check if the event is really a user list refresh...
        if (event.getEventType() == CacheEvent.CacheEventType.FULL_RELOAD) {
            resetBentoList();
        } else if (event.getConcernedTypes() != null) {
            boolean concernd = false;
            for (String t : bentoTypes)
                concernd = concernd || event.getConcernedTypes().contains(t);
            if (concernd)
                resetBentoList();
        }
    }


    protected void resetBentoList() {
        List<Bento> myList = new ArrayList();
        for (String t : bentoTypes) {
            List<Bento> tmpl = CacheManager.get().getCachedObjects(t);
            if (tmpl != null)
                myList.addAll(tmpl);
        }
        myList = filterData(myList);
        Collections.sort(myList, new Comparator<Bento>() {
            public int compare(Bento o1, Bento o2) {
                return -1 * o1.getLastUpdate().compareTo(o2.getLastUpdate());
            }
        });
        if (this.recentCount > 0 && recentCount != NOLIMIT && myList.size() > recentCount) {
            List<Bento> headList = new ArrayList();
            for (int i = 0 ; i<recentCount; i++)
                headList.add(myList.get(i));
            myList = headList;
        }
        resetData(myList);
    }


    /**
     * filter per status
     * @param objectList
     * @return
     */
    @Override
    protected List<Bento> filterData(List<Bento> bentos) {
        List<Bento> result = new ArrayList();
        if (bentos!=null)
            for (Bento b : bentos)
                if (this.listSupportedStatus().contains(b.getStatus()))
                    result.add(b);
        return result;
    }

}
