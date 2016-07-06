package ch.repit.rwt.client.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;


/**
 *
 */
public class Dialog  {

    // reuse the dialog boxes...
    private static DialogBox confirmBox;
    private static DialogResponseHandler confirmResponseHandler;
    private static HTML confirmMessage;

    private static DialogBox alertBox;
    private static HTML alertMessage;


    public static void alert(String message) {  // could add level (thus message and icon)

        if (alertBox == null) {
            // Create a dialog box and set the caption text
            alertBox = new DialogBox();
            alertBox.setHTML("<table width='100%'><tr><td align=left>Information</td>"+
                    "<td align=right><img src='"+GWT.getModuleBaseURL()+"icons/alerts/info_medium.gif'/></td></tr></table>");

            // Create a table to layout the content
            VerticalPanel dialogContents = new VerticalPanel();
            dialogContents.setSpacing(8);
            alertBox.setWidget(dialogContents);

            // Add some text to the top of the dialog
            alertMessage = new HTML();
            dialogContents.add(alertMessage);
            dialogContents.setCellHorizontalAlignment(alertMessage, VerticalPanel.ALIGN_CENTER);

            // Add a close button at the bottom of the dialog
            HorizontalPanel buttonBar = new HorizontalPanel();
            buttonBar.setSpacing(8);
            buttonBar.setHorizontalAlignment(HorizontalPanel.ALIGN_CENTER);
            Button yesButton = new Button("OK",
                new ClickHandler() {
                    public void onClick(ClickEvent event) {
                        alertBox.hide();
                        greyer(false);
                    }
                });
            buttonBar.add(yesButton);
            buttonBar.setCellHorizontalAlignment(yesButton,HorizontalPanel.ALIGN_CENTER);
            dialogContents.add(buttonBar);
            dialogContents.setCellHorizontalAlignment(buttonBar,HorizontalPanel.ALIGN_CENTER);

            alertBox.setAnimationEnabled(true);
        }
        alertMessage.setHTML(message);
        alertBox.center();
        alertBox.show();
    }

    
    public static void confirm(String message, DialogResponseHandler responseHandler) {
        greyer(true);

        if (confirmBox == null) {
            // Create a dialog box and set the caption text
            confirmBox = new DialogBox();
            confirmBox.setHTML("<table width='100%'><tr><td align=left>Demande de confirmation</td>"+
                    "<td align=right><img src='"+GWT.getModuleBaseURL()+"icons/alerts/info_medium.gif'/></td></tr></table>");

            // Create a table to layout the content
            VerticalPanel dialogContents = new VerticalPanel();
            dialogContents.setSpacing(8);
            confirmBox.setWidget(dialogContents);

            // Add some text to the top of the dialog
            confirmMessage = new HTML();
            dialogContents.add(confirmMessage);
            dialogContents.setCellHorizontalAlignment(confirmMessage, VerticalPanel.ALIGN_CENTER);

            // Add a close button at the bottom of the dialog
            HorizontalPanel buttonBar = new HorizontalPanel();
            buttonBar.setSpacing(8);
            buttonBar.setHorizontalAlignment(HorizontalPanel.ALIGN_CENTER);
            Button yesButton = new Button("Oui",
                new ClickHandler() {
                    public void onClick(ClickEvent event) {
                        confirmBox.hide();
                        greyer(false);
                        yesClicked();
                    }
                });
            buttonBar.add(yesButton);
            buttonBar.setCellHorizontalAlignment(yesButton,HorizontalPanel.ALIGN_CENTER);
            Button noButton = new Button("Non",
                new ClickHandler() {
                    public void onClick(ClickEvent event) {
                        confirmBox.hide();
                        greyer(false);
                        noClicked();
                    }
                });
            buttonBar.add(noButton);
            buttonBar.setCellHorizontalAlignment(noButton,HorizontalPanel.ALIGN_CENTER);
            dialogContents.add(buttonBar);
            dialogContents.setCellHorizontalAlignment(buttonBar,HorizontalPanel.ALIGN_CENTER);

            confirmBox.setAnimationEnabled(true);
        }
        confirmMessage.setHTML(message);
        confirmResponseHandler = responseHandler;
        confirmBox.center();
        confirmBox.setGlassEnabled(true);
        confirmBox.show();
    }


    public static Widget buildImageLink(String imageURL, String previewURL) {
      //  Anchor a;
        // old school
      //  a = new Anchor("<img src='" + previewURL + "'/>", true, imageURL, "otherTab");

        // new style
        Image full = new Image(imageURL);
        final PopupPanel imagePopup = new PopupPanel(true);
        imagePopup.setAnimationEnabled(true);
        imagePopup.setWidget(full);
        full.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                imagePopup.hide();
            }
        });
        Image thumb = new Image(previewURL);
        //thumb.addStyleName("cw-BasicPopup-thumb");
        thumb.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                imagePopup.center();
            }
        });

        return thumb;
    }


    private static void yesClicked() {
        confirmResponseHandler.onYes();
    }

    private static void noClicked() {
        confirmResponseHandler.onNo();
    }

    /**
     * JSNI method, invokes a script directly in Repit.jsp
     * cf http://www.hunlock.com/blogs/Snippets:_Howto_Grey-Out_The_Screen
     */
    native static void greyer(boolean show) /*-{
      $wnd.grayOut(show,null); // $wnd is a JSNI synonym for 'window'
    }-*/;


    public interface DialogResponseHandler {
        public void onYes();
        public void onNo();
    }


}
