/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.repit.rwt.client.ui.form;

import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Panel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author tc149752
 */
public class FieldRow {

    private String title;
    private String help;
    private boolean visible = true;
    private List<Field> fields = new ArrayList<Field>();

    
    public FieldRow(String title) {
        this.title = title;
    }
    public FieldRow(String title, Field... fields) {
        this.title = title;
        this.fields = Arrays.asList(fields);
    }
    public FieldRow(String title, String help, Field... fields) {
        this.title = title;
        this.help = help;
        this.fields = Arrays.asList(fields);
    }

    public List<Field> getFields() {
        return fields;
    }

    public void addField(Field field) {
        fields.add(field);
    }

    public String getHelp() {
        return help;
    }

    public void setHelp(String help) {
        this.help = help;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Panel doLayout() {
        HorizontalPanel panel = new HorizontalPanel();
        for (Field field : fields) {
            panel.add(field.doLayout());
        }
        return panel;
    }

}
