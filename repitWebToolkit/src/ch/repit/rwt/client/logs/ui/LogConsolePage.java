/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.repit.rwt.client.logs.ui;

import ch.repit.rwt.client.logs.LogEvent;
import ch.repit.rwt.client.logs.LogManager;
import ch.repit.rwt.client.logs.LogEventHandler;
import ch.repit.rwt.client.ui.ListPage;
import ch.repit.rwt.client.ui.Page;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ButtonBase;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Widget;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


/**
 *
 * @author tc149752
 */
public class LogConsolePage extends ListPage<LogEvent> implements LogEventHandler
{
    private DateTimeFormat dateFormater = DateTimeFormat.getFormat("yyyy-MM-dd HH:mm:ss");
    private LinkedList<LogEvent> m_eventsList;
    private boolean withDebug = false;

    private ClickHandler m_resetHandler = new ClickHandler() {
        public void onClick(ClickEvent arg0) {
            m_eventsList.clear();
            resetData(m_eventsList);
        }
    };

    public LogConsolePage() {
        super (null);
        super.setTitle("Console de Logs");
        super.setShowPath(false);
        
        // set the columns
        super.addColumn("Date",      "date",     true);
        super.addColumn("Message",   "message",  true);
        super.addColumn("Catégorie", "severity", true);
        super.addColumn("Annexe",    "misc",     false);

        m_eventsList = new LinkedList<LogEvent>();
        LogManager.get().registerEventHandler(this);
        // DO NOT UNREGISTER THIS EVENT HANDLER, should run all the time!
    }

    @Override
    protected void fillToolbarWidgets(List<Widget> leftWidgets,
                                      List<Widget> middleWidgets,
                                      List<Widget> rightWidgets) {
        super.fillToolbarWidgets(leftWidgets, middleWidgets, rightWidgets);
        Button clearButton = new Button("Effacer les logs");
        clearButton.addClickHandler(m_resetHandler);
        leftWidgets.add(clearButton);

        CheckBox debugCb = new CheckBox("DEBUG");
        debugCb.setValue(withDebug);
        debugCb.addValueChangeHandler(new ValueChangeHandler() {
            public void onValueChange(ValueChangeEvent arg0) {
                withDebug = !withDebug;
                resetData(m_eventsList);
            }
        } );
        rightWidgets.add(debugCb);
    }

    public void onLogEvent(LogEvent event) {
        m_eventsList.addFirst(event);
        if (m_eventsList.size() > 200)
            m_eventsList.removeLast();
        this.resetData(m_eventsList);
    }

    @Override
    protected boolean formatObject(LogEvent obj, Map formatedValue) {
        if (obj.getSeverity() == LogEvent.Severity.DEBUG && !withDebug)
            return false;
        formatedValue.put("date",     dateFormater.format(obj.getEventDate()));
        formatedValue.put("message",  obj.getMessage());
        formatedValue.put("severity", obj.getSeverity().toString());
        formatedValue.put("misc",     formatException(obj.getException()));
        return true;
    }

    @Override
    protected void onRowClicked(LogEvent data, String columnsAttributeName) {
        // nothing yet, could show the full stack trace...
    }

    @Override
    protected int sortCompare(LogEvent o1, LogEvent o2, String sortAttribute, boolean ascending) {
        if (sortAttribute.equals("date")) {
            return (o1.getEventDate().compareTo(o2.getEventDate()) * (ascending?1:-1) );
        } else if (sortAttribute.equals("message")) {
            return (o1.getMessage().compareTo(o2.getMessage()) * (ascending?1:-1) );
        } else if (sortAttribute.equals("severity")) {
            return (o1.getSeverity().compareTo(o2.getSeverity()) * (ascending?1:-1) );
        } else
            return 0;
    }

    private String formatException(Throwable t) {
        if (t != null) {
            /* Not compiling with GWT 1.5.1...
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            t.printStackTrace(pw); */
            return "(" + t.getClass().getName() + ") " + t.getMessage(); //+ "<br/><pre>" + sw.toString() + "</pre>";
        }
        else
            return "";
    }

}