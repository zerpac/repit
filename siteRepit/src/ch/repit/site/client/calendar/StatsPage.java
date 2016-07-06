/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.repit.site.client.calendar;

import ch.repit.rwt.client.Bento;
import ch.repit.rwt.client.BentoDefFactory;
import ch.repit.rwt.client.BentoStatus;
import ch.repit.rwt.client.Day;
import ch.repit.rwt.client.ObjectRef;
import ch.repit.rwt.client.logs.LogManager;
import ch.repit.rwt.client.persistence.CacheEvent;
import ch.repit.rwt.client.persistence.CacheEventHandler;
import ch.repit.rwt.client.persistence.CacheManager;
import ch.repit.rwt.client.ui.Page;
import ch.repit.rwt.client.util.DateUtil;
import ch.repit.rwt.client.util.Formaters;
import ch.repit.rwt.client.user.UserDef;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.ButtonBase;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.Widget;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * See http://code.google.com/intl/fr/apis/chart/labels.html
 */
public class StatsPage extends Page implements CacheEventHandler {

    private HorizontalPanel hp;
    private Image perMonthChart;
    private Image perMemberChart;
    private HTML currentYearDisplay = new HTML();

    private Day statsFrom;
    private Day statsTo;

    public StatsPage(Page parentPage) {
        super(parentPage);
        super.setPrintable(true);
        super.setOnlyTopToolbar(true);

        hp = new HorizontalPanel();
        hp.addStyleName("rwt-pageContentWhite");

        statsTo = new Day( ((new Date()).getYear()+1900) * 10000 + 1031); // Oct 31st of current year
        statsFrom = DateUtil.addDay( DateUtil.addMonth(statsTo, -12), 1);

        CacheManager.get().registerEventHandler(this);
    }

    @Override
    protected Widget doContentlayout() {
        resetCharts();
        return hp;
    }

    
    public void onCacheEvent(CacheEvent event) {
        if ( event.getEventType() == CacheEvent.CacheEventType.FULL_RELOAD ||
                event.getConcernedTypes().contains(BookingDef.TYPE)) {
            resetCharts();
        }
    }
    

    @Override
    protected void fillToolbarWidgets(List<Widget> leftWidgets,
                                      List<Widget> middleWidgets,
                                      List<Widget> rightWidgets) {
        super.fillToolbarWidgets(leftWidgets, middleWidgets, rightWidgets);

        PushButton prev = new PushButton(new Image(GWT.getModuleBaseURL()+"icons/nav_prev.gif"));
        prev.setStylePrimaryName("nostyleaaa"); // i.e. no border
        prev.setTitle("année précédente");
        prev.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                statsFrom = DateUtil.addMonth(statsFrom, -12);
                statsTo = DateUtil.addMonth(statsTo, -12);
                resetCharts();
            }
        });
        leftWidgets.add(prev);

        PushButton next = new PushButton(new Image(GWT.getModuleBaseURL()+"icons/nav_next.gif"));
        next.setStylePrimaryName("nostyleaaa"); // i.e. no border
        next.setTitle("année suivante");
        next.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                statsFrom = DateUtil.addMonth(statsFrom, 12);
                statsTo = DateUtil.addMonth(statsTo, 12);
                resetCharts();
            }
        });
        leftWidgets.add(next);

        currentYearDisplay.setStylePrimaryName("repit-calendarMonthDisplay");
        middleWidgets.add(0,currentYearDisplay);
    }

    
    private void resetCharts() {

        if (perMonthChart != null)
            hp.remove(perMonthChart);
        if (perMemberChart != null)
            hp.remove(perMemberChart);
        perMonthChart = new Image();
        perMemberChart = new Image();

        // 1. find bookings in concerned period
        List<Bento> allBookings = CacheManager.get().getCachedObjects(BookingDef.TYPE);
        List<Bento> concernedBookings = new ArrayList();
        if (allBookings != null) {
            for (Bento b : allBookings) {
                if ( (b.getStatus()==BentoStatus.ACTIVE || b.getStatus()==BentoStatus.ARCHIVE)
                        && b.get(BookingDef.ATTR_FROM_DAY).getDay().before(statsTo)
                        && b.get(BookingDef.ATTR_TO_DAY).getDay().after(statsFrom)  )
                {
                    concernedBookings.add(b);
                }
            }
        }

        // 2. display year
        currentYearDisplay.setHTML(statsTo.getYear()
                + " <small>(" + Formaters.get().formatDateRange(statsFrom, statsTo) + ")</small>" );

        // 3. generate indiv charts
        resetMonthChart(concernedBookings);
        resetPieChart(concernedBookings);
        hp.add(perMonthChart);
        hp.add(perMemberChart);

        LogManager.get().debug("in resetCharts");
   }


    private void resetMonthChart(List<Bento> bookings) {
        // for nights per week / month, overall
        
        SortedMap<Integer,Integer> nightsPerMonth = new TreeMap();
        for (Bento b : bookings) {
            Day firstDay = b.get(BookingDef.ATTR_FROM_DAY).getDay();
            if (firstDay.before(statsFrom))
                firstDay = statsFrom;
            Day lastDay = b.get(BookingDef.ATTR_TO_DAY).getDay();
            if (lastDay.after(statsTo))
                lastDay = statsTo;
            
            int numPeople = 10;  // default for full booking
            if (!b.get(BookingDef.ATTR_RESERVATION).getBoolean())
                numPeople = b.get(BookingDef.ATTR_NUMBER_PEOPLE).getInteger();
            
            for ( Day d = firstDay; d.before(lastDay); d = DateUtil.addDay(d, 1) ) {
                int numThisMonth = 0;
                int currentMonth = d.getMonth();
                if (nightsPerMonth.containsKey(currentMonth))
                    numThisMonth = nightsPerMonth.get(currentMonth);
                nightsPerMonth.put(currentMonth, numThisMonth + numPeople);
            }
        }

        String chd = "";
        String comma = "";
        int totalNites = 0;
        int upper = 10;
        for (int month : new int [] {11,12,1,2,3,4,5,6,7,8,9,10} ) {
            Integer n =  nightsPerMonth.get(month);
            if (n == null)
                n = 0;
            chd += comma + n;
            comma = ",";
            if (n>upper)
                upper = n;
            totalNites += n;
        }
        upper = ( (upper / 100) + 1 ) * 100;
        int scale = upper / 5;
        String sc = "";
        for (int i = 0; i<=5; i++)
            sc += i*scale + "|";

        perMonthChart.setUrl("http://chart.apis.google.com/chart?cht=bvs" +
                "&chtt=Répartition+des+nuitées+par+mois|Total:+" +totalNites+
                "&chco=00AA44" +
                "&chs=360x200" + 
                "&chm=N*f0*,000000,0,-1,10" +
                "&chds=0," + upper +
                "&chxr=1,0," + upper +
                "&chd=t:" + chd +
                "&chxt=x,y,x" +
                "&chxl=0:|Nov|Déc|Jan|Fév|Mar|Avr|Mai|Juin|Juil|Aout|Sept|Oct|" +
                "2:|||" + statsTo.getYear());

    }

    
    private void resetPieChart(List<Bento> bookings) {
        // pie chart for % of nights per user per year
        // hyp: no booking spans several year (reasonable with separation Nov 1st)

        Map<ObjectRef,Integer> nightsPerMemberCount = new HashMap();
        int totalNites = 0;
        for (Bento b : bookings) {
            ObjectRef member = new ObjectRef(b.get(BookingDef.ATTR_USER).getString());
            Integer memberNightCount = 0;
            if (nightsPerMemberCount.containsKey(member))
                memberNightCount = nightsPerMemberCount.get(member);
            int numNights = (int) ( 
                    (Math.min(b.get(BookingDef.ATTR_TO_DAY).getDay().getDate().getTime(), statsTo.getDate().getTime()) -
                     Math.max(b.get(BookingDef.ATTR_FROM_DAY).getDay().getDate().getTime(), statsFrom.getDate().getTime()))
                      / (1000 * 60 * 60 * 24) );
            int numPeople = 10;  // default for full booking
            if (!b.get(BookingDef.ATTR_RESERVATION).getBoolean())
                numPeople = b.get(BookingDef.ATTR_NUMBER_PEOPLE).getInteger();
            memberNightCount += numNights * numPeople;
            totalNites += numNights * numPeople;
            nightsPerMemberCount.put(member, memberNightCount);
        }

        String chd = "";
        String chl = "";
        UserDef userDef = (UserDef)BentoDefFactory.get().getDef(UserDef.TYPE);
        String comma = "";
        String pipe = "";
        for (ObjectRef member : nightsPerMemberCount.keySet()) {
            String memName = userDef.getCommonName(CacheManager.get().getCachedObject(member));
            chd += comma + ( nightsPerMemberCount.get(member) / Math.max(1,(totalNites / 100)) );
            chl += pipe + memName; //+ " (" + nightsPerMemberCount.get(member) + ")";
            comma = ",";
            pipe = "|";
        }
        chl.replaceAll(" ", "+");

        perMemberChart.setUrl("http://chart.apis.google.com/chart?cht=p3" +
                "&chtt=Répartition+des+nuitées+par+membre" +
                "&chs=500x200" +
                "&chco=00AA44" +
                "&chd=t:"  + chd +
                "&chl=" + chl);
    }

}
    