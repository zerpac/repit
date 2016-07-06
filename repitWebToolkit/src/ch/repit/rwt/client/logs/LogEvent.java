/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.repit.rwt.client.logs;

import java.util.Date;


/**
 *
 * @author tc149752
 */
public class LogEvent  {

    public enum Severity {

        DEBUG,
        INFO,
        INFO_CONN_OK,
        HANDLING,
        HANDLED,
        WARNING,
        WARNING_CONN_LOST,
        ERROR;
    }

    private Date m_eventDate;
    private Severity m_severity;
    private String m_message;
    private Throwable m_exception;

    public LogEvent(Severity severity, String message) {
        this(severity, message, null);
    }

    public LogEvent(Severity severity, String message, Throwable exception) {
        m_eventDate = new Date();
        m_severity = severity;
        m_message = message;
        m_exception = exception;
    }

    public Date getEventDate() {
        return m_eventDate;
    }
    public Severity getSeverity() {
        return m_severity;
    }
    public String getMessage() {
        return m_message;
    }
    public Throwable getException() {
        return m_exception;
    }
}
