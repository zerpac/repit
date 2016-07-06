/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.repit.rwt.server.notification;

import ch.repit.rwt.server.util.Logging;
import com.google.gwt.user.server.Base64Utils;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import javax.servlet.ServletConfig;

/**
 * cf
 * - http://www.java-hair.com/downloads/TwitterClient.java
 * - http://apiwiki.twitter.com/Twitter-REST-API-Method%3A-statuses%C2%A0update
 */
public class TwitterNotif {

    private static Logging LOG = new Logging(TwitterNotif.class.getName());

    private String twiturl = "http://www.twitter.com/statuses/update.xml";
    private String basicAuth;
    private boolean twitEnabled = false;


    public void init(ServletConfig conf) {
        String method = "init";
        LOG.enter(method);

        String twitName = conf.getInitParameter("twitterName");
        if (twitName!=null && twitName.length()>1) {
            String twitPwd = conf.getInitParameter("twitterPwd");
            String twitUser = twitName+":"+twitPwd;
            basicAuth = "Basic "+Base64Utils.toBase64(twitUser.getBytes());
            LOG.debug(method, "Will tweet on " + twitName);
            twitEnabled = true;
        } else
            LOG.debug(method, "Twitter notifs disabled");
        
        LOG.leave(method);
    }


    /**
     * uses GAE URL Fetch Service
     */
    public void notif(String status) {
        if (twitEnabled) {
            String method = "notif";
            LOG.enter(method);

          //  status = status.replaceAll("é", "&eacute;");

            try {
                URL url = new URL(twiturl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoOutput(true);
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Authorization", basicAuth);

                OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
                writer.write("status=" + URLEncoder.encode(status) );
                writer.close();

                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    LOG.debug(method, "OK");
                } else {
                    LOG.warning(method, "Not OK; "+connection.getResponseCode());
                }
            } catch (MalformedURLException e) {
                LOG.warning(method, "MalformedURLException", e);
            } catch (IOException e) {
                LOG.warning(method, "IOException", e);
            }

            LOG.leave(method);
        }
    }
    

}
