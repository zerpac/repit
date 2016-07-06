/*
 * LogWriter.java
 *
 * Created on 30. mars 2002, 00:18
 */

package ch.repit.rwt.server.util;

import java.util.logging.*;


/**
 * Simple tracing and logging class. 
 */
public class Logging 
{        
    private static final Logger LOGGER = Logger.getLogger("ch.repit");
    
    private static boolean init = true;  // until needed ...
    
    private String m_className;


    public Logging() {
        
    }
    
    public Logging(String className) {
        m_className = className;
    }
    
    public void debug(String method, String message)
    {
        if (LOGGER.isLoggable(Level.FINE))
        {
            trace(Level.FINE, method, message);
        }
    }    
    public void debug(String method, String message, Throwable exception)
    {
        if (LOGGER.isLoggable(Level.FINE))
        {
            trace(Level.FINE, method, message, exception);
        }
    }        
    public void info(String method, String message)
    {
        if (LOGGER.isLoggable(Level.INFO))
        {
            trace(Level.INFO, method, message);
        }
    }    
    public void info(String method, String message, Throwable exception)
    {
        if (LOGGER.isLoggable(Level.INFO))
        {
            trace(Level.INFO, method, message, exception);
        }
    }    

    public void enter(String method)
    {
        if (LOGGER.isLoggable(Level.FINE))
        {
            trace(Level.FINE, method, "enter");
        }
    }
    public void enter(String method, String message) {
        if (LOGGER.isLoggable(Level.FINE))
        {
            trace(Level.FINE, method, "enter: " + message);
        }
    }
    
    public void leave(String method)
    {
        if (LOGGER.isLoggable(Level.FINE))
        {
            trace(Level.FINE, method, "leave");
        }
    }
    public void leave(String method, String message) {
        if (LOGGER.isLoggable(Level.FINE))
        {
            trace(Level.FINE, method, "leave: " + message);
        }
    }

    
    public void warning(String method, String message)
    {
        if (LOGGER.isLoggable(Level.WARNING))
        {
            trace(Level.WARNING, method, message);
        }
    }
    public void warning(String method, String message, Throwable exception)
    {
        if (LOGGER.isLoggable(Level.WARNING))
        {
            trace(Level.WARNING, method, message, exception);
        }
    }    
    
    public void error(String method, String message)
    {
        trace(Level.SEVERE, method, message);
    }    
    public void error(String method, String message, Throwable exception)
    {
        trace(Level.SEVERE, method, message, exception);
    }    
    
    // ----------------------------------------------------------------------
    // private methods
    
    /*
     * write the message into a file
     */
    private void trace(Level level, String method, String message, Throwable exception)  
    {
        if (init) {
            LOGGER.logp(level, m_className, method, message, exception);
        } else {
            // could use a queue...
        }
    }
    
    private void trace(Level level, String method, String message)
    {
        if (init) {
            LOGGER.logp(level, m_className, method, message);
        } else {
            // could use a queue...
        }
    }

    
}

