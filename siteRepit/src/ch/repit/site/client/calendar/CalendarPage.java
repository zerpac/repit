/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.repit.site.client.calendar;

import ch.repit.site.client.calendar.BookingManager.BookingLine;
import ch.repit.rwt.client.Bento;
import ch.repit.rwt.client.Day;
import ch.repit.rwt.client.ObjectRef;
import ch.repit.rwt.client.logs.LogManager;
import ch.repit.rwt.client.persistence.CacheEvent;
import ch.repit.rwt.client.persistence.CacheEventHandler;
import ch.repit.rwt.client.persistence.CacheManager;
import ch.repit.rwt.client.security.Action;
import ch.repit.rwt.client.security.SecurityManager;
import ch.repit.rwt.client.ui.Page;
import ch.repit.rwt.client.util.DateUtil;
import ch.repit.rwt.client.util.Formaters;
import ch.repit.rwt.client.util.Formaters.DatePattern;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ButtonBase;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLTable.Cell;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.Widget;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 *
 * @author tc149752
 */
public class CalendarPage extends Page implements CacheEventHandler {

    // TBD: put that crap in util.DateFormater
    public static final String[] DAYS_FULL =
        { "Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi", "Dimanche" };
  
    private final FlexTable calTable = new FlexTable();

    private Day currentMonth;
    private HTML currentMonthDisplay = new HTML();
    private Map tableRefs;
    private Map<String,Day> cellDateMap;
    private Map<Day,List<String>> dateCellsMap;
    private Day createBookingStartOrEndDate = null;
    private CalendarPage calPage = this;

    private Button cancelDateSelect1, cancelDateSelect2;
    

    public CalendarPage(Page parentPage) {
        super(parentPage);
        super.setShowPath(false);
        setPrintable(true);
        super.setOnlyTopToolbar(true);
        initCalTable();
        CacheManager.get().registerEventHandler(this);
    }

    
    @Override
    protected Widget doContentlayout() {
        return calTable;
    }


    public void onCacheEvent(CacheEvent event) {
        if ( event.getEventType() == CacheEvent.CacheEventType.FULL_RELOAD ||
                event.getConcernedTypes().contains(BookingDef.TYPE) ||
                event.getConcernedTypes().contains(CalendarEventDef.TYPE) ) {
            displayCalMonth(currentMonth);
        }
    }


    @Override
    protected void fillToolbarWidgets(List<Widget> leftWidgets,
                                      List<Widget> middleWidgets,
                                      List<Widget> rightWidgets) {
        super.fillToolbarWidgets(leftWidgets, middleWidgets, rightWidgets);

        /*
        PushButton prev4 = new PushButton(new Image(GWT.getModuleBaseURL()+"icons/nav_prev_big.gif"));
        prev4.setStylePrimaryName("nostyleaaa"); // i.e. no border
        prev4.setTitle("4 mois avant");
        prev4.addClickHandler(prev4MonthCH);
        leftWidgets.add(prev4);
        */

        PushButton prev = new PushButton(new Image(GWT.getModuleBaseURL()+"icons/nav_prev.gif"));
        prev.setStylePrimaryName("nostyleaaa"); // i.e. no border
        prev.setTitle("mois précédent");
        prev.addClickHandler(prevMonthCH);
        leftWidgets.add(prev);

        PushButton back2now = new PushButton(new Image(GWT.getModuleBaseURL()+"icons/nav_home.gif"));
        back2now.setStylePrimaryName("nostyleaaa"); // i.e. no border
        back2now.setTitle("mois courant");
        back2now.addClickHandler(currentMonthCH);
        leftWidgets.add(back2now);

        PushButton next = new PushButton(new Image(GWT.getModuleBaseURL()+"icons/nav_next.gif"));
        next.setStylePrimaryName("nostyleaaa"); // i.e. no border
        next.setTitle("mois suivant");
        next.addClickHandler(nextMonthCH);
        leftWidgets.add(next);

        /*
        PushButton next4 = new PushButton(new Image(GWT.getModuleBaseURL()+"icons/nav_next_big.gif"));
        next4.setStylePrimaryName("nostyleaaa"); // i.e. no border
        next4.setTitle("4 mois après");
        next4.addClickHandler(next4MonthCH);
        leftWidgets.add(next4);
         */

        cancelDateSelect2 = cancelDateSelect1;

        cancelDateSelect1 = new Button("", cancelSelectCH);
        cancelDateSelect1.setVisible(false);
        cancelDateSelect1.setStylePrimaryName("nhostile");
        middleWidgets.add(cancelDateSelect1);

        
        currentMonthDisplay.setStylePrimaryName("repit-calendarMonthDisplay");
        middleWidgets.add(currentMonthDisplay);
    }

    /*
    private ClickHandler prev4MonthCH = new ClickHandler() {
        public void onClick(ClickEvent arg0) {
            displayCalMonth(DateUtil.addMonth(currentMonth, -4));
        } }; */
    private ClickHandler prevMonthCH = new ClickHandler() {
        public void onClick(ClickEvent arg0) {
            displayCalMonth(DateUtil.addMonth(currentMonth, -1));
        } };
    private ClickHandler currentMonthCH = new ClickHandler() {
        public void onClick(ClickEvent arg0) {
            displayCalMonth(DateUtil.today());   // TBD: could fetch "NTP-like" from server...
        } };
    private ClickHandler nextMonthCH = new ClickHandler() {
        public void onClick(ClickEvent arg0) {
            displayCalMonth(DateUtil.addMonth(currentMonth, 1));
        } } ;
        /*
    private ClickHandler next4MonthCH = new ClickHandler() {
        public void onClick(ClickEvent arg0) {
            displayCalMonth(DateUtil.addMonth(currentMonth, 4));
        } } ; */
    private ClickHandler cancelSelectCH = new ClickHandler() {
        public void onClick(ClickEvent arg0) {
            removeCalSelection();
        } };


    /**
     * Removes the cal selection, for instance after a successful event creation
     */
    protected void removeCalSelection() {
        if (createBookingStartOrEndDate != null) {
            applyStyleToDate(createBookingStartOrEndDate, "repit-calCellSelect", true);
            createBookingStartOrEndDate = null;
        }
        if (cancelDateSelect1 != null)
            cancelDateSelect1.setVisible(false);
        if (cancelDateSelect2 != null)
            cancelDateSelect2.setVisible(false);
    }


    private void initCalTable() {

        // calTable.setBorderWidth(1);
        calTable.setCellSpacing(0);
        calTable.setWidth("100%");
        FlexTable.FlexCellFormatter fcf = calTable.getFlexCellFormatter();
        int rowCount = 0; 
        
        // initial row to define the widthes
        calTable.insertRow(rowCount);
        for (int i=0; i< 7*5; i++) {
            calTable.insertCell(rowCount, i);
            fcf.setWidth(rowCount, i, "2.8%");
            calTable.setText(rowCount, i, "");
        }

        // row for the month and year
        calTable.insertRow(++rowCount);
        calTable.insertCell(rowCount, 0);
        fcf.setColSpan(rowCount, 0, 35);
        fcf.setAlignment(rowCount, 0, HasHorizontalAlignment.ALIGN_CENTER, HasVerticalAlignment.ALIGN_TOP);
        HorizontalPanel headerLine = new HorizontalPanel();
        headerLine.setWidth("100%");

        // sets the name of the day
        calTable.insertRow(++rowCount);
        for (int i=0; i<7; i++) { 
            // <td class="bl bt br bb" colspan=5 bgcolor=lightgreen align=center><b><%= CalendarBean.DAYS_FULL[i] %></b></td>
            calTable.insertCell(rowCount, i);
            fcf.setColSpan(rowCount, i, 5);
            fcf.addStyleName(rowCount, i, "rwt-list-headerCell bl"); //"tableheader bl bt bb small");
            fcf.setAlignment(rowCount, i, HasHorizontalAlignment.ALIGN_CENTER, HasVerticalAlignment.ALIGN_MIDDLE);
            calTable.setText(rowCount, i, DAYS_FULL[i]);
        }
        fcf.addStyleName(rowCount, 6, "br");

        // define click handler for days
        calTable.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent arg0) {
                Cell cell = calTable.getCellForEvent(arg0);
                if (cell != null) {
                    String key = "" + cell.getRowIndex() + ":" + cell.getCellIndex();

                    // this is if a booking was clicked
                    if (tableRefs.containsKey(key)) {
                        Object oro = tableRefs.get(key);
                        if (oro != null && oro instanceof ObjectRef) {
                            ObjectRef or = (ObjectRef)oro;
                            getPageNav().displayPage(new CalendarEntryPage(or));
                        }
                    }

                    // this is if a date was clicked
                    else {
                        Day dt = cellDateMap.get(key);
                        if (dt != null) {
                            // if click on same date, remove selection
                            if (dt.equals(createBookingStartOrEndDate)) {
                                removeCalSelection();
                            }

                            // if date in the past, check that user has right to edit
                            else if (!dt.before(DateUtil.today()) ||
                                    SecurityManager.get().getAuthorizer().isAllowed(Action.MANAGE, BookingDef.TYPE)) {

                                // if first date click, set selection
                                if (createBookingStartOrEndDate == null) {
                                    createBookingStartOrEndDate = dt;
                                    applyStyleToDate(createBookingStartOrEndDate, "repit-calCellSelect", false);

                                    if (cancelDateSelect1 != null) {
                                        cancelDateSelect1.setHTML("Annuler sélection " + Formaters.get().formatDate(dt, DatePattern.DATE));
                                        cancelDateSelect1.setVisible(true);
                                    }
                                    if (cancelDateSelect2 != null) {
                                        cancelDateSelect2.setHTML("Annuler sélection " + Formaters.get().formatDate(dt, DatePattern.DATE));
                                        cancelDateSelect2.setVisible(true);
                                    }
                                }
                                // if click on other date, create new booking
                                else {
                                    // transmit createBookingStartOrEndDate and dt
                                    getPageNav().displayPage(new BookingDetailsPage
                                            (null, null, calPage, createBookingStartOrEndDate, dt));
                                }
                            } else {
                                LogManager.get().info("Il n'est pas possible de saisir des événements dans le passé");
                            }
                        }
                    }
                }
            }
        });

        displayCalMonth(DateUtil.today());
    }


    private void displayCalMonth(Day date) {

        tableRefs = new HashMap();

        // delete the table content
        for (int r = calTable.getRowCount(); r > 3; r--) {
            calTable.removeRow(3);
        }
        dateCellsMap = new HashMap<Day, List<String>>();
        cellDateMap = new HashMap<String, Day>();

        // compute monday of previous month
        currentMonth = date;
        Day monday = date;
        int thisMonth = monday.getMonth();
        while (thisMonth == monday.getMonth() || monday.getDayOfWeek() != 1) {
            monday = DateUtil.addDay(monday, -1);
        }
        int lastMonth = monday.getMonth();

        // display the corresponding weeks
        monday = DateUtil.addDay(monday, -1);
        do {
            monday = DateUtil.addDay(monday, 1);
            insertWeek(monday, thisMonth);
            monday = DateUtil.addDay(monday, 6);
        } while (monday.getMonth() == lastMonth || monday.getMonth() == thisMonth);

        // set the date in display and title
        String month = Formaters.get().formatDate(date, DatePattern.MONTH_YEAR);

        setTitle(month);
        currentMonthDisplay.setHTML(month);

        // Sets the current dat in another format
        if (dateCellsMap.containsKey(DateUtil.today())) {
            applyStyleToDate(DateUtil.today(), "repit-calCellToday", false);
        }

        // sets the previous month in another style
        for (Day d : dateCellsMap.keySet()) {
            if (thisMonth != d.getMonth()) {
                applyStyleToDate(d, "repit-calCellOtherMonth", false);
            }
        }

        // sets the first selection date if present
        if (createBookingStartOrEndDate != null) 
            applyStyleToDate(createBookingStartOrEndDate, "repit-calCellSelect", false);
    }

    
    // TBD: remove selectionable cells for days in the past (with no rights)
    private void insertWeek(Day monday, int currentMonth)
    {
        int rowCount = calTable.getRowCount();
        FlexTable.FlexCellFormatter fcf = calTable.getFlexCellFormatter();

        // fetch bookings for this week
        List<BookingLine> lines = BookingManager.get().getBookingsByWeek(monday);

        // the day and month labels
        Day tmpDate = monday;
        calTable.insertRow(rowCount);
        int col = 0;
        for (int i=0; i<7; i++) {
            // display month name only if not current
            String dateLabel = Formaters.get().formatDate(tmpDate,
                    (tmpDate.getMonth()==currentMonth)?DatePattern.DAY:DatePattern.DAY_MONTH);

            List<Bento> calEvents = BookingManager.get().getCalendarEventsForDay(tmpDate);
            
            // if there are events
            if (calEvents != null && calEvents.size() > 0) {
                int daySpan = 1;
                int evSpan = 4;
                if (calEvents.size() == 2) {
                    daySpan = 1;
                    evSpan = 2;
                } else if (calEvents.size() == 3) {
                    daySpan = 2;
                    evSpan = 1;
                } else if (calEvents.size() == 4) {
                    daySpan = 1;
                    evSpan = 1;
                }
                String firstEvType = calEvents.get(0).get(CalendarEventDef.ATTR_EVENT_TYPE).getDisplayValue();
                insertDaySlice(rowCount, col++, daySpan, tmpDate,
                        "repit-calCellEmpty repit-calCellEvent-" + firstEvType + " bl small", dateLabel);
                int dispCnt = 0;
                for (Bento b : calEvents) {
                    String evType = b.get(CalendarEventDef.ATTR_EVENT_TYPE).getDisplayValue();
                    // at most 4 events displayed per day !!!
                    if (dispCnt++ < 4) {
                        calTable.insertCell(rowCount, col);
                        fcf.setColSpan(rowCount, col, evSpan);
                        tableRefs.put(""+rowCount+":"+col, b.getRef());
                        fcf.addStyleName(rowCount, col, "repit-calCellEvent-" + evType + " blw small");
                        fcf.setHorizontalAlignment(rowCount, col, HasHorizontalAlignment.ALIGN_RIGHT);
                        calTable.setText(rowCount, col, b.getDisplayValue(CalendarEventDef.ATTR_TITLE));
                        col++;
                    }
                }
                
            // if there are no events
            } else {
                insertDaySlice(rowCount, col++, 5, tmpDate, "repit-calCellEmpty bl small", dateLabel);
            }
            tmpDate = DateUtil.addDay(tmpDate, 1);
        }      
        fcf.addStyleName(rowCount, col-1, "br");

        // separator line
        insertWeekSlice(++rowCount, monday, "repit-calCellEmpty bl minus", "&nbsp;");

        // adds the booking lines
        for (BookingLine line : lines) {

            int colCount = 0;
            calTable.insertRow(++rowCount);
            
            Iterator it = line.iterator();
            BookingManager.Booking inscription = null;
            int[] startEnd = null;
            tmpDate = monday;
            
            for (int wi = 1; wi < 8; wi++) {
                if ( (inscription == null || wi > startEnd[1]) && (it != null && it.hasNext())) {
                    inscription = (BookingManager.Booking)it.next();
                    startEnd = inscription.getStartAndEnd(monday);
                }

                if ( (startEnd == null) || (wi < startEnd[0]) | (wi > startEnd[1]) ) {
                    // regular empty cell
                    tmpDate = DateUtil.addDay(monday,  wi-1);
                    insertDaySlice(rowCount, colCount++, 5, tmpDate, "repit-calCellEmpty bl", "&nbsp;");
                    
                } else if ( (startEnd != null) && ((wi == startEnd[0]) | startEnd[0] == 0)) {
                    // there is an insc on this cell
                    int startHourOffset = 0;
                    int endHourOffset = 0;
                    // if it starts here, we put the width part of the cells before
                    if (startEnd[0] > 0) {
                        String fromHourStr = inscription.getBento().get(BookingDef.ATTR_FROM_HOUR).getString();
                        if (fromHourStr != null && fromHourStr.length() > 0) {
                            try {
                                int fhi = Integer.parseInt(fromHourStr);
                                startHourOffset = fhi;
                            } catch (Exception e) {}
                        }
                        tmpDate = DateUtil.addDay(monday,  wi-1);
                        insertDaySlice(rowCount, colCount++, 1 + startHourOffset, tmpDate, "repit-calCellEmpty bl", "&nbsp;");
                    }
                    if (startEnd[1] < 8) {
                        String toHourStr = inscription.getBento().get(BookingDef.ATTR_TO_HOUR).getString();
                        if (toHourStr != null && toHourStr.length() > 0) {
                            try {
                                int thi = Integer.parseInt(toHourStr);
                                endHourOffset = 3 - thi;
                            } catch (Exception e) {}
                        }
                    }

                    // the actual inscription
                    int colspan = (startEnd[1] - startEnd[0]) * 5 + 3;
                    colspan -= startHourOffset;
                    colspan -= endHourOffset;
                    if (startEnd[0] == 0)
                        colspan -= 4;
                    if (startEnd[1] == 8)
                        colspan -= 4;
                    calTable.insertCell(rowCount, colCount);
                    fcf.setColSpan(rowCount, colCount, colspan);
                    tableRefs.put(""+rowCount+":"+colCount, inscription.getBento().getRef());
                    if (inscription.getBento().get(BookingDef.ATTR_RESERVATION).getBoolean())
                        fcf.addStyleName(rowCount, colCount, "repit-calCellResa");
                    else
                        fcf.addStyleName(rowCount, colCount, "repit-calCellBooking");
                    if (startEnd[0] > 0)
                        fcf.addStyleName(rowCount, colCount, "repit-calCellBooking-start");
                    else
                        fcf.addStyleName(rowCount, colCount, "bl");
                    if (startEnd[1] < 8)
                        fcf.addStyleName(rowCount, colCount, "repit-calCellBooking-end");
                    fcf.setHorizontalAlignment(rowCount, colCount, HasHorizontalAlignment.ALIGN_CENTER);

                    // the inscription content
                    HorizontalPanel hp = new HorizontalPanel();
                    Bento author = CacheManager.get().getCachedObject
                            (new ObjectRef(inscription.getBento().get(BookingDef.ATTR_USER).getString()));
                    if (author.getAttachedFiles() != null && author.getAttachedFiles().length > 0) {
                        Image photo = new Image();
                        photo.setUrl(author.getAttachedFiles()[0].getPreviewUrl(author.getRef()));
                        photo.setPixelSize(20,20);
                        hp.add(photo);
                        hp.setCellHorizontalAlignment(photo, HorizontalPanel.ALIGN_LEFT);
                    }
                    hp.add(new HTML(inscription.getLabel()));

                    calTable.setWidget(rowCount, colCount, hp);
                    colCount++;

                    wi = startEnd[1];
                    if (startEnd[1] < 8) {
                        // it stops here, thus we put the part of the cell after
                        tmpDate = DateUtil.addDay(monday,  wi-1); 
                        insertDaySlice(rowCount, colCount++, 1 + endHourOffset, tmpDate, "repit-calCellEmpty", "&nbsp;");
                    }
                }
            }
            fcf.addStyleName(rowCount, colCount-1, "br"); 

            // separator
            insertWeekSlice(++rowCount, monday, "repit-calCellEmpty bl minus", "&nbsp;");
        }
        
        // close week
        insertWeekSlice(++rowCount, monday, "repit-calCellEmpty bl bb minus", "&nbsp;");
    }


    private void insertWeekSlice(int rowCount, Day monday, String styles, String content) {
        calTable.insertRow(rowCount);
        FlexTable.FlexCellFormatter fcf = calTable.getFlexCellFormatter();
        Day tmpDate = monday;
        for (int i=0; i<7; i++) {
            insertDaySlice(rowCount, i, 5, tmpDate, styles, content);
            tmpDate = DateUtil.addDay(tmpDate, 1);
        }
        fcf.addStyleName(rowCount, 6, "br");
    }

    private void insertDaySlice(int row, int col, int colSpan, Day day, String styles, String content) {
        calTable.insertCell(row, col);
        FlexTable.FlexCellFormatter fcf = calTable.getFlexCellFormatter();
        fcf.setColSpan(row, col, colSpan);
        fcf.addStyleName(row, col, styles);
        if (content != null)
            calTable.setWidget(row, col, new HTML(content));

        cellDateMap.put(""+row+":"+col, day);
        List<String> dateCellsList = dateCellsMap.get(day);
        if (dateCellsList == null) {
            dateCellsList = new ArrayList<String>();
            dateCellsMap.put(day,dateCellsList);
        }
        dateCellsList.add(""+row+":"+col);
    }


    private void applyStyleToDate(Day date, String style, boolean removeStyle) {
        List<String> cells = dateCellsMap.get(date);
        if (cells != null)
            for (String cell : cells) {
                String[] c = cell.split(":");
                if (c.length == 2) {
                    int row = Integer.parseInt(c[0]);
                    int col = Integer.parseInt(c[1]);
                    if (!removeStyle)
                        calTable.getFlexCellFormatter().addStyleName(row, col, style);
                    else
                        calTable.getFlexCellFormatter().removeStyleName(row, col, style);
                }
            }
    }

}
