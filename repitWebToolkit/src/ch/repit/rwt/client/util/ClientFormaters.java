/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.repit.rwt.client.util;

import java.util.*;

import com.google.gwt.i18n.client.DateTimeFormat;

/**
 *
 * @author tc149752
 */
public class ClientFormaters extends Formaters {


    protected ClientFormaters() {  }

    
    @Override
    public String formatDate(Date date, DatePattern pattern) {
        if (date == null)
            return "(date vide)";
        return DateTimeFormat.getFormat(pattern.getPattern()).format(date);
    }

}
