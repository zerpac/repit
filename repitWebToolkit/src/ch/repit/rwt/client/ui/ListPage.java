/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.repit.rwt.client.ui;

import ch.repit.rwt.client.logs.LogManager;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTMLTable.Cell;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author tc149752
 */
public abstract class ListPage<ObjType> extends Page
{

    private List<ObjType> m_objectList;
    private List<ObjType> m_displayedObjectList;
    
    private int m_columnCount = 0;
    protected List<String> m_columnAttributeNames = new ArrayList<String>();
    protected List<String> noWrapColumns = new ArrayList<String>();

    private List<Integer> m_sortableColumnIndexes = new ArrayList<Integer>();
    private int m_sortColumnIndex = 0;
    private boolean m_sortAscending = true;

    private FlexTable m_table;


    protected ListPage(Page topPage) {
        super(topPage);
        super.setOnlyTopToolbar(true);
        m_table = new FlexTable();
        m_table.setWidth("100%");
        m_table.setCellSpacing(1);
        m_table.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent arg0) {
                try {
                    Cell cell = m_table.getCellForEvent(arg0);
                    if (cell != null && cell.getRowIndex() > 0 && m_displayedObjectList != null) {
                        ObjType obj = m_displayedObjectList.get(cell.getRowIndex() - 1);
                        onRowClicked(obj, m_columnAttributeNames.get(cell.getCellIndex()));
                    } else if (cell != null && cell.getRowIndex() == 0) {
                        // sort column
                        int colIndex = cell.getCellIndex();
                        if (m_sortableColumnIndexes.contains(colIndex)) {
                            // remove sorted style to previous column
                            m_table.getCellFormatter().removeStyleName
                                    (0, m_sortColumnIndex, "rwt-list-headerCellSorted" + (m_sortAscending?"Asc":"Desc"));
                            // if same column, switch order, otherwise ascending
                            if (m_sortColumnIndex == colIndex)
                                m_sortAscending = !m_sortAscending;
                            else
                                m_sortAscending = true;
                            m_sortColumnIndex = colIndex;
                            // refresh the display
                            resetData(m_objectList);
                        }
                    }
                }
                catch (RuntimeException e) {
                    LogManager.get().error("Exception while handling table event", e);
                    throw e; // just for dev mode...
                }
            } } );
    }

    
    @Override
    protected Widget doContentlayout() {
        return m_table;
    }


    protected void addColumn(String title,
                             String attributeName,
                             boolean sortable) {
        addColumn(null, title, attributeName, sortable, false);
    }
    protected void addColumn(String title,
                             String attributeName,
                             boolean sortable,
                             boolean nowrap) {
        addColumn(null, title, attributeName, sortable, nowrap);
    }
    protected void addColumn(String image,
                             String title,
                             String attributeName,
                             boolean sortable) {
        addColumn(image, title, attributeName, sortable, false);
    }
    protected void addColumn(String image,
                             String title,
                             String attributeName,
                             boolean sortable,
                             boolean nowrap) {
        if (image!=null) {
            Image img = new Image(image);
            m_table.setWidget(0, m_columnCount, img);
        } else {
            m_table.setText(0, m_columnCount, title);
        }
        m_columnAttributeNames.add(attributeName);
        if (nowrap)
            noWrapColumns.add(attributeName);
        if (sortable) {
            m_table.getCellFormatter().setStylePrimaryName(0, m_columnCount, "rwt-list-headerCellSortable");
            m_sortableColumnIndexes.add(m_columnCount);
        }
        else
            m_table.getCellFormatter().setStylePrimaryName(0, m_columnCount, "rwt-list-headerCell");
        m_columnCount++;
    }

    protected void setSortColumn(String attributeName, boolean ascending) {
        m_sortColumnIndex = m_columnAttributeNames.indexOf(attributeName);
        m_sortAscending = ascending;
    }

    /**
     * Allows to filter the list content before processing the whole list.
     * Default implementation does no filtering
     */
    protected List<ObjType> filterData(List<ObjType> objectList) {
        return objectList;
    }




    protected void resetData(List<ObjType> objectList) {
        // resort the data according to previous order

        // 1. remove all lines except header
        for (int i = m_table.getRowCount()-1; i>=1; i--)
            m_table.removeRow(i);

        // filters the list
        m_objectList = filterData(objectList);


        // sets a message if table is empty
        if (m_objectList == null || m_objectList.size() == 0) {
            m_table.getFlexCellFormatter().setColSpan(1, 0, m_columnCount);
            m_table.setText(1, 0, "La table ne contient pas de données");
            m_table.getCellFormatter().setStylePrimaryName(1, 0, "rwt-list-cell");
            m_table.getCellFormatter().setAlignment(1, 0,
                  HasHorizontalAlignment.ALIGN_CENTER, HasVerticalAlignment.ALIGN_MIDDLE);
            m_table.getCellFormatter().setHeight(1, 0, "50px");
        }
            
        else {
            // sort the data according to previous order
            final String sortAttribute = m_columnAttributeNames.get(m_sortColumnIndex);
            Collections.sort(m_objectList, new Comparator<ObjType>() {
                public int compare(ObjType o1, ObjType o2) {
                    return sortCompare(o1, o2, sortAttribute, m_sortAscending);
                }
            });
            m_table.getCellFormatter().addStyleName
                (0, m_sortColumnIndex, "rwt-list-headerCellSorted" + (m_sortAscending?"Asc":"Desc"));
            
            m_displayedObjectList = new ArrayList<ObjType>();

            // 2. iterate on all objects
            for (ObjType obj : m_objectList)  {
                // 2.a. to get the display values
               // List rowAttrs = formatRowValues(obj);
                Map formatedValue = new HashMap();
                boolean displayLine = formatObject(obj, formatedValue);

                // 2.b. to set the line
                if (displayLine) {
                    int currentRow = m_table.getRowCount();
                    int col = 0;
                    for (String attr : m_columnAttributeNames) {
                        Object valObj = formatedValue.get(attr);
                        if (valObj == null)
                            valObj = "--";
                        if (valObj instanceof String)
                            m_table.setText(currentRow, col, (String)valObj);
                        else if (valObj instanceof Widget)
                            m_table.setWidget(currentRow, col, (Widget)valObj);
                        else
                            m_table.setText(currentRow, col, "BUG:" + valObj);
                        m_table.getCellFormatter().setStylePrimaryName(currentRow, col, "rwt-list-cell");
                        if (noWrapColumns.contains(attr))
                            m_table.getCellFormatter().setWordWrap(currentRow, col, false);
                        col++;
                    }
                    m_displayedObjectList.add(obj);
                }
            }
        }
    }


    /**
     * Performs a single row formating by setting into the map in parameter the column keys
     * defined via addColumn method, of the object in parameter. Can also perfom filtering
     * via the return value.
     * @param obj the object to format on the row
     * @param formatedValue an (originaly) empty map where entroes have to be added where the key
     * is the attribute and the value the string or widget to display.
     * @return true if the line has to be displayed, false otherwise
     */
    protected abstract boolean formatObject(ObjType obj, Map formatedValue);
    
    
    protected abstract void onRowClicked(ObjType data, String columnsAttributeName);


    protected abstract int  sortCompare(ObjType o1, ObjType o2, String sortAttribute, boolean ascending);

}
