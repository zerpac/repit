/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.repit.rwt.server.persistence;

import ch.repit.rwt.client.AttributeDef;
import ch.repit.rwt.client.AttributeType;
import ch.repit.rwt.server.audit.AuditLog;
import ch.repit.rwt.server.util.Logging;
import ch.repit.rwt.client.Bento;
import ch.repit.rwt.client.Day;
import ch.repit.rwt.client.BentoAttribute;
import ch.repit.rwt.client.BentoComment;
import ch.repit.rwt.client.BentoDef;
import ch.repit.rwt.client.BentoDefFactory;
import ch.repit.rwt.client.BentoStatus;
import ch.repit.rwt.client.FileDescriptor;
import ch.repit.rwt.client.ObjectRef;
import ch.repit.rwt.client.persistence.MissingMandatoryAttributeException;
import ch.repit.rwt.client.util.Formaters;
import ch.repit.rwt.client.util.Formaters.DatePattern;
import ch.repit.rwt.server.audit.AuditLogAttribute;
import ch.repit.rwt.server.file.File;
import ch.repit.rwt.server.file.FileHolder;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 *
 * @author tc149752
 */
public class DataObjectMapper {

    private static Logging LOG = new Logging(DataObjectMapper.class.getName());

    
    public static void do2bento(DataObject dataObject, Bento bento)  {
        String method = "do2bento";
    //    LOG.enter(method, dataObject.getClass().getCanonicalName() + ":" + dataObject.getId());
        for (String attrName : bento.keySet()) {
            BentoAttribute attr = bento.get(attrName);
            Object value = readDoValue(attr, dataObject);
            switch (attr.getAttributeType())  {
                case STRING:
                    attr.set((String)value);
                    break;
                case INTEGER:
                    attr.set((Integer)value);
                    break;
                case DATE:
                    attr.set((Date)value);
                    break;
                case DAY:
                    if (value != null) {
                        Integer val = (Integer)value;
                      //  LOG.debug(method, "Value is a DAY; val=" + val);
                        Day d = new Day(val);
                        attr.set(d);
                    }
                    break;
                case BOOLEAN:
                    attr.set((Boolean)value);
                    break;
                case STRING_LIST:
                    if (value == null)
                        attr.set(new ArrayList<String>());
                    else
                        attr.set(new ArrayList<String>((List<String>)value));
                    break;
                default:
                    LOG.error(method, "Unknown attribute type : " + attr.getAttributeType());
                    throw new RuntimeException("Unknown attribute type : " + attr.getAttributeType());
            }
        }

        // copies the comments
        if (dataObject.getComments() != null && dataObject.getComments().size() > 0) {
            List<String> comments = dataObject.getComments();
            BentoComment[] commentDtoList = new BentoComment[comments.size()];
            
            for (int i = 0; i<commentDtoList.length; i++) {
                try {
                    String c = comments.get(i);
                    BentoComment dc = CommentMapper.string2Bento(c);
                    commentDtoList[i] = dc;
                } catch (Exception e) {
                    LOG.error(method, "(IGNORING) Unable to load comments for " + dataObject, e);
                    commentDtoList[i] = null;
                }
            }
            bento.setComments(commentDtoList);
        }

        // copies the file descriptors
        if (dataObject instanceof FileHolder) {
            FileHolder fileDataObject = (FileHolder)dataObject;
            if (fileDataObject.getFiles() != null && fileDataObject.getFiles().size() > 0) {
                List<File> files = fileDataObject.getFiles();
                FileDescriptor[] fileDescs = new FileDescriptor[files.size()];
                for (int i = 0; i<fileDescs.length; i++) {
                    File file = files.get(i);
                    LOG.debug(method, "Reading file:" + file);
                    FileDescriptor fileDesc = new FileDescriptor();
                    fileDesc.setFileName(file.getFileName());
                    fileDesc.setContentType(file.getFileContentType());
                    fileDesc.setSizeBytes(file.getFileSizeBytes());
                    if (file.getFileKey() != null)
                        fileDesc.setId(file.getFileKey().getId());  // TBD: how to cope with IDs???
                    fileDescs[i] = fileDesc;
                }
                bento.setAttachedFiles(fileDescs);
            }
        }
       
        // copies the status
    //    bento.setActive(dataObject.isActive());
        bento.setStatus( BentoStatus.valueOf(dataObject.getStatus()) );

        // the owner
        if (dataObject.getOwner() != null)
            bento.setOwnerRef(new ObjectRef(dataObject.getOwner()));

    //    LOG.leave(method, bento.getRef().toString());
    }





    public static void validateBentoWithDo(Bento bento,
                                DataObject dataObject,
                                Map<String,String> uniqueAttr2Check)
         throws MissingMandatoryAttributeException
    {
        String method = "validateBentoWithDo";
        LOG.enter(method);

        BentoDef bentoDef = BentoDefFactory.get().getDef(bento.getType());

        for (String attrName : bento.keySet()) {
            BentoAttribute attr = bento.get(attrName);
            AttributeDef attrDef = bentoDef.getAttributeDef(attrName);

         //   if (attr.isModified()) {      // TBD: seems not working...

            try {
                // gets the DO previous attribute value
                Object originalValue = readDoValue(attr, dataObject);

                Object newValue = null;
                switch (attr.getAttributeType())  {
                    case STRING: {
                        newValue = attr.getString();
                        break; }
                    case INTEGER: {
                        newValue = attr.getInteger();
                        break; }
                    case DATE: {
                        newValue = attr.getDate();
                        break; }
                    case DAY: {
                        newValue = attr.getDay();
                        break; }
                    case BOOLEAN: {
                        newValue = attr.getBoolean();
                        break; }
                    case STRING_LIST: {
                        newValue = attr.getStringList();
                        break; }
                    default:
                        LOG.error(method, "Unknown attribute type : " + attr.getAttributeType());
                        throw new RuntimeException("Unknown attribute type : " + attr.getAttributeType());
                }

                // check mandatory attributes
                if (attrDef.isMandatory() && newValue == null) {
                    throw new MissingMandatoryAttributeException(attrDef.getName());
                }

                // adds it to list of attrs to check
                if (attrDef.isUnique() && newValue != null && 
                        !newValue.equals(originalValue) &&
                        attr.getAttributeType() == AttributeType.STRING) {
                    uniqueAttr2Check.put(attrName, (String)newValue);  // so indeed we only check strings for uniqueness
                }

            } catch (IllegalArgumentException e) {
                LOG.error(method, "Reflection Issue, check object definition", e);
                throw new RuntimeException("Reflection Issue, check object definition", e);
            }
        }

        LOG.leave(method);
    }



    public static void bento2do(Bento bento,
                                DataObject dataObject,
                                AuditLog auditLog)
    {

        String method = "bento2do";
        LOG.enter(method);

        List<AuditLogAttribute> logAttrs = new ArrayList<AuditLogAttribute>();
        BentoDef bentoDef = BentoDefFactory.get().getDef(bento.getType());

        for (String attrName : bento.keySet()) {
            BentoAttribute attr = bento.get(attrName);
            AttributeDef attrDef = bentoDef.getAttributeDef(attrName);

         //   if (attr.isModified()) {      // TBD: seems not working...
            
            // does not read readonly attributes
            if (!attrDef.isReadonly()) {
                try {
                    String originalValueDisplay = null;
                    String newValueDisplay = null;

                    // gets the DO previous attribute value
                    Object originalValue = readDoValue(attr, dataObject);

                    // should check the type...
                    String methSupposedName = "set" + attrName.substring(0,1).toUpperCase() + attrName.substring(1);
                    Object newValue = null;
                    switch (attr.getAttributeType())  {
                        case STRING: {
                            Method meth = dataObject.getClass().getMethod(methSupposedName, String.class);
                            newValue = attr.getString();
                            if (newValue == null && originalValue == null)
                                newValue = attrDef.getDefaultValue();
                            meth.invoke(dataObject, newValue);
                            originalValueDisplay = originalValue==null?null:originalValue.toString();
                            newValueDisplay = newValue==null?null:newValue.toString();
                            break; }
                        case DAY:
                        case INTEGER: {
                            Method meth = dataObject.getClass().getMethod(methSupposedName, Integer.class);
                            newValue = attr.getInteger();
                            if (newValue == null && originalValue == null)
                                newValue = attrDef.getDefaultValue();
                            meth.invoke(dataObject, newValue);
                            originalValueDisplay = originalValue==null?null:originalValue.toString();
                            newValueDisplay = newValue==null?null:newValue.toString();
                            break; }
                        case DATE: {
                            Method meth = dataObject.getClass().getMethod(methSupposedName, Date.class);
                            newValue = attr.getDate();
                            if (newValue == null && originalValue == null)
                                newValue = attrDef.getDefaultValue();
                            meth.invoke(dataObject, newValue);
                            originalValueDisplay = originalValue==null?null:Formaters.get().formatDate((Date)originalValue,DatePattern.FULL);
                            newValueDisplay = newValue==null?null:Formaters.get().formatDate((Date)newValue,DatePattern.FULL);
                            break; }
                        case BOOLEAN: {
                            Method meth = dataObject.getClass().getMethod(methSupposedName, Boolean.class);
                            newValue = attr.getBoolean();
                            if (newValue == null && originalValue == null)
                                newValue = attrDef.getDefaultValue();
                            meth.invoke(dataObject, newValue);
                            originalValueDisplay = originalValue==null?null:(Boolean)originalValue?"Oui":"Non";
                            newValueDisplay = newValue==null?null:(Boolean)newValue?"Oui":"Non";
                            break; }
                        case STRING_LIST: {
                            Method meth = dataObject.getClass().getMethod(methSupposedName, List.class);
                            newValue = attr.getStringList();
                            if (newValue == null && originalValue == null)
                                newValue = attrDef.getDefaultValue();
                            meth.invoke(dataObject, newValue);
                            originalValueDisplay = originalValue==null?null:originalValue.toString();
                            newValueDisplay = newValue==null?null:newValue.toString();
                            break; }
                        default:
                            LOG.error(method, "Unknown attribute type : " + attr.getAttributeType());
                            throw new RuntimeException("Unknown attribute type : " + attr.getAttributeType());
                    }

                    // compares previous and new value, log modified attr if different
                    if (attrDef.isAuditable()) {
                        if (originalValue != null && newValue != null) {
                            if (!originalValue.equals(newValue)) {
                                AuditLogAttribute ala = new AuditLogAttribute();
                                ala.setAttributeName(attrName);
                                ala.setOldValue(originalValueDisplay);
                                ala.setNewValue(newValueDisplay);
                                logAttrs.add(ala);
                            }
                        } else if (newValue != null) {
                            AuditLogAttribute ala = new AuditLogAttribute();
                            ala.setAttributeName(attrName);
                            ala.setNewValue(newValueDisplay);
                            logAttrs.add(ala);
                        } else if (originalValue != null) {
                            AuditLogAttribute ala = new AuditLogAttribute();
                            ala.setAttributeName(attrName);
                            ala.setOldValue(originalValueDisplay);
                            logAttrs.add(ala);
                        }
                    }

                } catch (NoSuchMethodException e) {
                    LOG.error(method, "Reflection Issue, check object definition", e);
                    throw new RuntimeException("Reflection Issue, check object definition", e);
                } catch (IllegalAccessException e) {
                    LOG.error(method, "Reflection Issue, check object definition", e);
                    throw new RuntimeException("Reflection Issue, check object definition", e);
                } catch (IllegalArgumentException e) {
                    LOG.error(method, "Reflection Issue, check object definition", e);
                    throw new RuntimeException("Reflection Issue, check object definition", e);
                } catch (InvocationTargetException e) {
                    LOG.error(method, "Reflection Issue, check object definition", e);
                    throw new RuntimeException("Reflection Issue, check object definition", e);
                }

            
            }
        }

        // copies the status
        String oldStatus = dataObject.getStatus();
        String newStatus = bento.getStatus().name();
        if (oldStatus == null || !oldStatus.equals(newStatus)) {
            AuditLogAttribute ala = new AuditLogAttribute();
            ala.setAttributeName("status");
            ala.setOldValue(oldStatus);
            ala.setNewValue(newStatus);
            logAttrs.add(ala);
        }
        dataObject.setStatus(bento.getStatus().name());

        if (auditLog != null) {
            // set the log attributes
            auditLog.setAuditLogAttributes(logAttrs);
        }

        LOG.leave(method);
    }


    private static Object readDoValue(BentoAttribute attr, DataObject dataObject) {
        // gets the DO previous attribute value
        try {
            String getter = (attr.getAttributeType()==AttributeType.BOOLEAN)?"is":"get";
            String getterSupposedName = getter + attr.getName().substring(0,1).toUpperCase() + attr.getName().substring(1);
            Method methGetter = dataObject.getClass().getMethod(getterSupposedName);
            Object originalValue = methGetter.invoke(dataObject);
            return originalValue;
        } catch (Exception e) {
            LOG.error("readDoValue", "Reflection Issue, check object definition", e);
            throw new RuntimeException("Reflection Issue, check object definition", e);
        }
    }


}
