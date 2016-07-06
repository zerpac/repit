/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.repit.rwt.client;

import ch.repit.rwt.client.util.Formaters;
import java.io.Serializable;
import java.util.Date;
import java.util.List;



public class BentoAttribute implements Serializable {

    private AttributeType m_type;
    private String   m_name;
    private String   m_string;
    private Integer  m_integer;
    private Date     m_date;
    private Boolean  m_boolean;
    private List<String>  m_stringList;

    // set first call to set sets the original and this to true
    private boolean m_initialValueSet = false;
    private boolean m_modified = false;

    public BentoAttribute() { }
    
    public BentoAttribute(String name, AttributeType type) {
        m_name = name;
        m_type = type;  
    }

    public AttributeType getAttributeType() {
        return m_type;
    }


    public String getDisplayValue() {
        String result = "";
        switch (getAttributeType())  {
            case STRING:
                result = getString();
                break;
            case INTEGER:
                result = "" + getInteger();
                break;
            case DATE:
                //result = "" + getDate();  // could do better...
                result = Formaters.get().formatDate(getDate(), Formaters.DatePattern.FULL);
                break;
            case BOOLEAN:
                result = "" + getBoolean();
                break;
            case STRING_LIST:
                result = "" + getStringList();   // could do better...
                break;
            case DAY:
                result = Formaters.get().formatDate(getDay(), Formaters.DatePattern.DATE);
                break;
            default:
                break;
        }
        return result;
    }


    public String getName() {
        return m_name;
    }

    public boolean isModified() {
        return m_modified;
    }
    
    public String getString() {
        return m_string;
    }
    public void set(String newValue) {
        if (m_type.equals(AttributeType.STRING)) {
            if (!m_initialValueSet) 
                m_initialValueSet = true;
            else if ( (newValue != null && !newValue.equals(m_string)) ||
                      (m_string != null && !m_string.equals(newValue)) )
                m_modified = true;
            m_string = newValue;
        }
    }

    public Integer getInteger() {
        return m_integer;
    }
    public void set(Integer newValue) {
        if (m_type.equals(AttributeType.INTEGER)) {
            if (!m_initialValueSet)
                m_initialValueSet = true;
            else if ( (newValue != null && !newValue.equals(m_integer)) ||
                      (m_integer != null && !m_integer.equals(newValue)) )
                m_modified = true;
            m_integer = newValue;
        }
    }

    public Day getDay() {
        return new Day(m_integer);
    }
    public void set(Day newDay) {
        Integer newValue = null;
        if (newDay != null) {
            newValue = newDay.getInteger();
        }
        if (m_type.equals(AttributeType.DAY)) {
            if (!m_initialValueSet)
                m_initialValueSet = true;
            else if ( (newValue != null && !newValue.equals(m_integer)) ||
                      (m_integer != null && !m_integer.equals(newValue)) )
                m_modified = true;
            m_integer = newValue;
        }
    }

    public Date getDate() {
        return m_date;
    }
    public void set(Date newValue) {
        if (m_type.equals(AttributeType.DATE)) {
            if (!m_initialValueSet)
                m_initialValueSet = true;
            else if ( (newValue != null && !newValue.equals(m_date)) ||
                      (m_date != null && !m_date.equals(newValue)) )
                m_modified = true;
            m_date = newValue;
        }
    }

    public Boolean getBoolean() {
        return m_boolean;
    }
    public void set(Boolean newValue) {
        if (m_type.equals(AttributeType.BOOLEAN)) {
            if (!m_initialValueSet)
                m_initialValueSet = true;
            else if ( (newValue != null && !newValue.equals(m_boolean)) ||
                      (m_boolean != null && !m_boolean.equals(newValue)) )
                m_modified = true;
            m_boolean = newValue;
        }
    }

    public List<String> getStringList() {
        // return Collections.unmodifiableList(m_stringList);
        return m_stringList;
    }
    public void set(List<String> newValue) {
        if (m_type.equals(AttributeType.STRING_LIST)) {
            if (!m_initialValueSet)
                m_initialValueSet = true;
            else if ( (newValue != null && !newValue.equals(m_stringList)) ||
                      (m_stringList != null && !m_stringList.equals(newValue)) )
                m_modified = true;
            m_stringList = newValue;
        }
    }


    @Override
    public String toString() {
        return "BentoAttr(" + m_type.name() + "=" + getDisplayValue() + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final BentoAttribute other = (BentoAttribute) obj;
        if ((this.m_name == null) ? (other.m_name != null) : !this.m_name.equals(other.m_name)) {
            return false;
        }
        if (this.m_type != other.m_type) {
            return false;
        }
        switch (m_type) {
            case STRING:
                if ((this.m_string == null) ? (other.m_string != null) : !this.m_string.equals(other.m_string)) {
                    return false;
                }
                break;
            case INTEGER:
                if (this.m_integer != other.m_integer && (this.m_integer == null || !this.m_integer.equals(other.m_integer))) {
                    return false;
                }
                break;
            case DATE:
                if (this.m_date != other.m_date && (this.m_date == null || !this.m_date.equals(other.m_date))) {
                    return false;
                }
                break;
            case BOOLEAN:
                if (this.m_boolean != other.m_boolean && (this.m_boolean == null || !this.m_boolean.equals(other.m_boolean))) {
                    return false;
                }
                break;
            case STRING_LIST:
                if (this.m_stringList != other.m_stringList && (this.m_stringList == null || !this.m_stringList.equals(other.m_stringList))) {
                    return false;
                }
                break;
            default:
                return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 29 * hash + this.m_type.hashCode();
        hash = 29 * hash + (this.m_name != null ? this.m_name.hashCode() : 0);
        hash = 29 * hash + (this.m_string != null ? this.m_string.hashCode() : 0);
        hash = 29 * hash + (this.m_integer != null ? this.m_integer.hashCode() : 0);
        hash = 29 * hash + (this.m_date != null ? this.m_date.hashCode() : 0);
        hash = 29 * hash + (this.m_boolean != null ? this.m_boolean.hashCode() : 0);
        hash = 29 * hash + (this.m_stringList != null ? this.m_stringList.hashCode() : 0);
        return hash;
    }

    

}


