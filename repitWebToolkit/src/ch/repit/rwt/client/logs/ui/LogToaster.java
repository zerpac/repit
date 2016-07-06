/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.repit.rwt.client.logs.ui;

import ch.repit.rwt.client.logs.LogEvent;
import ch.repit.rwt.client.logs.LogManager;
import ch.repit.rwt.client.logs.LogEventHandler;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;


/**
 *
 * @author tc149752
 */
public class LogToaster extends HorizontalPanel implements LogEventHandler {

    private HTML m_messageHolder;
    private HTML m_iconHolder;
    private Timer m_dimTimer;
    private Timer m_hideTimer;

    public LogToaster()
    {
        LogManager.get().registerEventHandler(this);

        this.setStylePrimaryName("rwt-logToaster");
        this.setSpacing(5);
        
        // icon container (TBD)
        m_iconHolder = new HTML("");
        this.add(m_iconHolder);

        // message container
        m_messageHolder = new HTML("No message");
        m_messageHolder.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent arg0) {
                displayEvent(null, false);      
            }
        });
        this.add(m_messageHolder);
        
        m_dimTimer = new Timer() {
            @Override
            public void run() {
                 removeStyleDependentName("displayed");
                 addStyleDependentName("dimmed");
            }
        };
        m_hideTimer = new Timer() {
            @Override
            public void run() {
                 removeStyleDependentName("dimmed");
                 removeStyleDependentName("displayed");
                 setVisible(false);
            }
        };
    }

    
    public void onLogEvent(LogEvent event) {
        // only handles some event types
        switch (event.getSeverity())  {
            case INFO:
                displayEvent(event, true);
                m_iconHolder.setHTML("<img src='"+GWT.getModuleBaseURL()+"icons/alerts/info_medium.gif'/>");
                break;
            case HANDLED:
                displayEvent(event, true);
                m_iconHolder.setHTML("<img src='"+GWT.getModuleBaseURL()+"icons/alerts/success_medium.gif'/>");
                break;
            case HANDLING:
                displayEvent(event, false);
                m_iconHolder.setHTML("<img src='"+GWT.getModuleBaseURL()+"icons/loading.gif' width='20px'/>");
                break;
            case WARNING:
                displayEvent(event, false);
                m_iconHolder.setHTML("<img src='"+GWT.getModuleBaseURL()+"icons/alerts/warning_medium.gif'/>");
                break;
            case ERROR:
                displayEvent(event, false);
                m_iconHolder.setHTML("<img src='"+GWT.getModuleBaseURL()+"icons/alerts/error_medium.gif'/>");
                break;
            default:
                break;
        }
    }


    private void displayEvent(LogEvent event, boolean withDimmer) {
        // reset previous state
        m_dimTimer.cancel();
        m_hideTimer.cancel();
        removeStyleDependentName("dimmed");
        removeStyleDependentName("displayed");
        setVisible(false);
        m_iconHolder.setHTML("&nbsp;");
        m_messageHolder.setHTML("&nbsp;");
        if (event != null) {
            // set current message
            m_messageHolder.setHTML(event.getMessage());
            // set corresponding icon (info)
            addStyleDependentName("displayed");
            setVisible(true);
            if (withDimmer) {
                m_dimTimer.schedule(3000);
                m_hideTimer.schedule(5000);
            }
        }
    }




}
