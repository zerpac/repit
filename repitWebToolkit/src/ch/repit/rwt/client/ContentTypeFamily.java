package ch.repit.rwt.client;

import com.google.gwt.core.client.GWT;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * TBD: should go to client side, and contain icons as well...
 */
public enum ContentTypeFamily {

    PDF         ("PDF",      "pdf.png",      "application/pdf",
                                             "application/x-download"),   // PDF from firefox 3.5 on mac...

    MS_OFFICE   ("MsOffice", "msOffice.png", "application/msword",       // doc, rtf
                                             "application/vnd.ms-word",  // not confirmed! just to avoid additional redeploy
                                             "application/excel",          // xls (not confirmed)
                                             "application/vnd.ms-excel",   // xls (confirmed, see bug jcm)
                                             "application/vnd.ms-powerpoint"),  // pps, ppt

    OPEN_OFFICE ("OpenOffice", "openOffice.png", "application/vnd.oasis.opendocument.presentation",
                                                 "application/vnd.oasis.opendocument.spreadsheet",
                                                 "application/vnd.oasis.opendocument.text",
                                                 "application/octet-stream"),    // ODS, ODG form firefox 3.5...

    ARCHIVE  ("Zip Archive", "zip.png", "application/zip",      // zip
                                        "application/x-tar",    // tar
                                        "application/x-gzip"),  // gzip
                                      
    IMAGE  ("Image", null, "image/gif", 
                           "image/png", "image/x-png",
                           "image/jpg", "image/jpeg",
                           "image/pjpeg"),  // this is for progressive jpeg...
                       //    "image/tiff",
                       //    "image/ico"); // ??? for last 2....

    MOVIE  ("Movie", "mov.png", "video/avi",
                                "video/mpeg",      // mpeg, mpg
                                "video/quicktime", // mov
                                "video/x-ms-wmv");


    private ContentTypeFamily(String name, String icon, String... contentType) {
        this.name = name;
        this.icon = icon;
        contentTypes = new HashSet<String>();
        contentTypes.addAll(Arrays.asList(contentType));
    }

    private Set<String> contentTypes;

    private String icon;

    private String name;


    private static final String iconFolder = "icons/fileType/";

    
    public String getIconHref() {
        if (icon != null)
            return GWT.getModuleBaseURL() + iconFolder + icon;
        else
            return null;
    }

    public String getName() {
        return name;
    }




    /**
     * Retrieves the content type family for the specified content type.
     * If not found, will return null / unknown ??? TBD
     */
    public static ContentTypeFamily getContentTypeFamily(String contentType) {
        for (ContentTypeFamily ctf : ContentTypeFamily.values()) {
            if (ctf.contentTypes.contains(contentType))
                return ctf;
        }
        return null;
    }

}
