/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.repit.rwt.server.file;

import ch.repit.rwt.client.ContentTypeFamily;
import ch.repit.rwt.server.persistence.DataObject;
import java.util.List;
import java.util.Set;

/**
 *
 * @author tc149752
 */
public interface FileHolder extends DataObject {


    /**
     * @return a list of File the file holder contains. 
     */
    public List getFiles();


    /**
     * Creates a file instance (must be different for each entity), and already adds it
     * to the list (thus can cope with non-list files).
     * @return the created File implementation (or existing, that will be updated)
     */
    public File createFileInstance();


    /**
     * @return the list of ContentTypeFamily objects that are allowed file formats
     * for this file holder
     */
    public Set<ContentTypeFamily> listAllowedFileTypes();


    /**
     * @return the size in pixel of the sides of a square that must contain the image
     * preview (without distortion) for this file holer.d
     */
    public int getImagePreviewSize();




    /**
     * @return the maximum size in bytes a file is allowed to have for this file holder
     */
    public long getFileMaxSizeInBytes();


    // TBD: provide a way to limit the number of files per object ?
}
