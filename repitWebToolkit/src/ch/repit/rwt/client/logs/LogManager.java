/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.repit.rwt.client.logs;

import java.util.ArrayList;
import java.util.List;


/**
 * Singleton class managing custom events.
 */
public class LogManager  {

    private static final LogManager s_instance = new LogManager();

    public static LogManager get() {
        return s_instance;
    }


    private List<LogEventHandler> m_eventHandlers;

    private LogManager() {
        m_eventHandlers = new ArrayList<LogEventHandler>();
    }

    public void addEvent(LogEvent e) {
        for (LogEventHandler handler : m_eventHandlers) {
            try {
                handler.onLogEvent(e);
            } catch (Exception ex) {} // avoid to crash client due to exceptions...
        }
    }

    public void registerEventHandler(LogEventHandler handler) {
        m_eventHandlers.add(handler);
    }

    public void debug(String message) {
        addEvent(new LogEvent(LogEvent.Severity.DEBUG, message));
    }
    public void info(String message) {
        addEvent(new LogEvent(LogEvent.Severity.INFO, message));
    }
    public void handling(String message) {
        addEvent(new LogEvent(LogEvent.Severity.HANDLING, message));
    }
    public void handled(String message) {
        addEvent(new LogEvent(LogEvent.Severity.HANDLED, message));
    }
    public void warning(String message) {
        addEvent(new LogEvent(LogEvent.Severity.WARNING, message));
    }
    public void warning(String message, Throwable t) {
        addEvent(new LogEvent(LogEvent.Severity.WARNING, message, t));
    }
    public void error(String message) {
        addEvent(new LogEvent(LogEvent.Severity.ERROR, message));
    }
    public void error(String message, Throwable t) {
        addEvent(new LogEvent(LogEvent.Severity.ERROR, message, t));
    }

    public void warningConnectionLost() {
        addEvent(new LogEvent(LogEvent.Severity.WARNING_CONN_LOST, "CONNECTION LOST"));
    }

    public void infoConnectionOk() {
        addEvent(new LogEvent(LogEvent.Severity.INFO_CONN_OK, "CONNECTION OK"));
    }


}
