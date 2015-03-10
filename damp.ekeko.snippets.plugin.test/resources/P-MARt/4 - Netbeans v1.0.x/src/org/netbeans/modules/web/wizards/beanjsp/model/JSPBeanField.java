/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is Forte for Java, Community Edition. The Initial
 * Developer of the Original Code is Sun Microsystems, Inc. Portions
 * Copyright 1997-2000 Sun Microsystems, Inc. All Rights Reserved.
 */

package  org.netbeans.modules.web.wizards.beanjsp.model;

import org.netbeans.modules.web.wizards.beanjsp.util.*;
import org.netbeans.modules.web.util.*;


import java.beans.*;
import java.util.*;
import java.lang.reflect.*;


public class JSPBeanField extends Object implements JSPItem {
    public static final int  READ = 0;
    public static final int  WRITE = 1;

    public static final int  READ_WRITE = 0;
    public static final int  READ_ONLY = 1;
    public static final int  WRITE_ONLY = 2;

    String fieldName;
    PropertyDescriptor propDesc;
    JSPBean jspBean;

    int access = READ;

    int htmlElementType;  // = HTMLElement.TEXTFIELD;

    String displayLabel;
    String initValue="";			 // NOI18N
    String queryParam="";			 // NOI18N

    String displayInitValue="";		 // NOI18N

    // JSPElement binding;

    /** Constructor */
    public JSPBeanField(JSPBean jspBean, PropertyDescriptor propDesc) {
        this.jspBean = jspBean;
        this.propDesc = propDesc;
        this.fieldName = propDesc.getName();
        access = READ;

        this.displayLabel = propDesc.getName();
        this.initValue="";						 // NOI18N
        this.queryParam = toHTMLElementName();

        this.displayInitValue="";					// NOI18N

        this.setHTMLElementType(this.getDefaultHTMLElementType());

        // this.binding = new JSPTextField();

    }

    public boolean hasKey(Object key) {
        if(!(key instanceof JSPBean))
            return false;
        return isFieldOf((JSPBean)key);
    }

public Object getKey() { return jspBean; }

    public boolean isFieldOf(JSPBean jspBean) {
        if(this.jspBean.getBeanName().equals(jspBean.getBeanName()))
            return true;
        else
            return false;
    }


public int getAccess() { return access; }
    public void setAccess(int access) { this.access = access; }

    public String getName() { return fieldName; }
    public void setName(String fieldName ) { this.fieldName = fieldName; }

    public String getDisplayLabel() { return displayLabel;}
    public void setDisplayLabel(String displayLabel){this.displayLabel = displayLabel;}

    public String getInitValue() { return initValue;}
    public void setInitValue(String initValue){this.initValue = initValue;}

    public String getDisplayInitValue() { return displayInitValue;}
    public void setDisplayInitValue(String displayInitValue){this.displayInitValue = displayInitValue;}


    public int getFieldAccessType() {
        Method getter = propDesc.getReadMethod();
        Method setter = propDesc.getWriteMethod();
        if(getter == null)
            return WRITE_ONLY;
        else if(setter == null)
            return READ_ONLY;
        else
            return READ_WRITE;
    }

    public boolean isReadOnly() {
        Method setter = propDesc.getWriteMethod();
        return (setter == null);
    }

    public boolean isWriteOnly() {
        Method getter = propDesc.getReadMethod();
        return (getter == null);
    }


    ////NB in this version we will not allow to change QeuranParam Name to user defined.
    // public String getQueryParam() { return queryParam; }
    public String getQueryParam() { return this.toHTMLElementName(); }
    public void setQueryParam(String queryParam) { this.queryParam = queryParam; }


    public int getHTMLElementType() { return htmlElementType;  }
    public void setHTMLElementType(int htmlElementType) { this.htmlElementType = htmlElementType; }

    public void selfPopulateToUseBeanInitProperties() {
        String initValue = this.getInitValue();
        if(initValue == null || initValue.trim().length() <= 0)
            return;
        this.jspBean.addInitBeanProperty(this);
    }

    public int getDefaultHTMLElementType() {

        // return HTMLElement.TEXTAREA;
        // /* ***
        Class propType = propDesc.getPropertyType();
        if(propType.getName().equals("boolean") || propType.getName().equals("java.lang.Boolean")){		 // NOI18N
            return HTMLElement.CHECKBOX;
        }else {
            return HTMLElement.TEXTFIELD;
        }
        // *** */
    }

    public Vector getHTMLElementChoices() {
        Vector htmlTypes = new Vector();
        Class propType = propDesc.getPropertyType();
        if(propType.isPrimitive() || propType.getName().equals("java.lang.Boolean")) {					 // NOI18N
            if(propType.getName().equals("boolean") || propType.getName().equals("java.lang.Boolean")){	 // NOI18N
                htmlTypes.add(HTMLElement.CHECKBOX_ELE);
                htmlTypes.add(HTMLElement.RADIOBUTTON_ELE);
            }
            htmlTypes.add(HTMLElement.TEXTFIELD_ELE);
            htmlTypes.add(HTMLElement.LISTBOX_ELE);
            htmlTypes.add(HTMLElement.CHOICE_ELE);
            htmlTypes.add(HTMLElement.HIDDEN_ELE);

        }else {
            htmlTypes.add(HTMLElement.TEXTFIELD_ELE);
            htmlTypes.add(HTMLElement.PASSWORD_ELE);
            htmlTypes.add(HTMLElement.LISTBOX_ELE);
            htmlTypes.add(HTMLElement.CHOICE_ELE);
            htmlTypes.add(HTMLElement.TEXTAREA_ELE);
            htmlTypes.add(HTMLElement.HIDDEN_ELE);
        }

        if(getAccess() == READ)
            htmlTypes.add(HTMLElement.TEXT_ELE);

        return htmlTypes;

    }

    public String toHTMLElementName() {
        return jspBean.getBeanVariableName()+"_"+propDesc.getName();	   // NOI18N
    }

    ////todo: check for args of the method.
    //// I am using this to getproperty for boolean . so no args
    public String toBooleanGetterJavaCode() {
        String beanVar = jspBean.getBeanVariableName();
        String getterName = propDesc.getReadMethod().getName();

        Class propType = propDesc.getPropertyType();

        StringBuffer expression = new StringBuffer();

        if(propType.getName().equals("java.lang.Boolean")) {						 // NOI18N
            expression.append(beanVar+"."+getterName+"().booleanValue()");		 // NOI18N
        }else {
            expression.append("new java.lang.Boolean(");							 // NOI18N
            expression.append(beanVar+"."+getterName+"()");						 // NOI18N
            expression.append(" ).booleanValue()");								 // NOI18N
        }
        return expression.toString();
    }

    public String toFormJSPCode(boolean tableRow) {
        String beanVar = jspBean.getBeanVariableName();

        String valueCode = "";									 // NOI18N

        String value = this.getDisplayInitValue();
        if(value != null && value.trim().length() > 0) {
            value = value.trim();
            if(this.htmlElementType == HTMLElement.CHECKBOX) {
                valueCode = HTMLElement.toHTMLCheckBoxValueCode(value);									 // NOI18N
            } else if( this.htmlElementType == HTMLElement.RADIOBUTTON ) {
                valueCode = HTMLElement.toHTMLRadioButtonValueCode(value);								 // NOI18N
            } else {
                valueCode = value;
            }
        } else {

            String jspCode = "<jsp:getProperty name=\""+beanVar+"\" "+									 // NOI18N
                             "property=\""+propDesc.getName()+"\" />";								 // NOI18N

            if(this.htmlElementType == HTMLElement.CHECKBOX) {
                jspCode = HTMLElement.toHTMLCheckBoxValueCode("",this.toBooleanGetterJavaCode());		 // NOI18N
            } else if( this.htmlElementType == HTMLElement.RADIOBUTTON ) {
                jspCode = HTMLElement.toHTMLRadioButtonValueCode("",this.toBooleanGetterJavaCode());	 // NOI18N
            } else {
                jspCode = "<jsp:getProperty name=\""+beanVar+"\" "+										 // NOI18N
                          "property=\""+propDesc.getName()+"\" />";								 // NOI18N
            }

            valueCode = jspCode;
        }

        //String elementCode = " <input type=text size=20 name="+toHTMLElementName()+" "+
        //		 "value = \""+jspCode +"\" >";

        String elementCode = HTMLElement.toHTMLElementCode(this.htmlElementType,this.toHTMLElementName(),valueCode);

        if(!tableRow) {
            return  "<br> <b>"+this.getDisplayLabel()+" </b> "+elementCode;					 // NOI18N
        }else {
            return "<td> <b> "+this.getDisplayLabel()+" </b> </td> \n"+						 // NOI18N
                   "<td> "+elementCode+" </td>";											 // NOI18N
        }

    }

    public String toSubmitProcessJSPCode() {

        String beanVar = jspBean.getBeanVariableName();

        String param = toHTMLElementName();

        String jspCode = "<jsp:setProperty name=\""+beanVar+"\" "+						 // NOI18N
                         "property=\""+propDesc.getName()+"\" "+							 // NOI18N
                         "param=\""+param+"\" /> \n";										 // NOI18N


        return jspCode;
    }

    public String toUseBeanInitJSPCode() {

        String beanVar = jspBean.getBeanVariableName();

        String value = this.getInitValue();
        if(value == null || value.trim().length() <= 0)
            return "";																	 // NOI18N
        else
            value = value.trim();

        String jspCode = "<jsp:setProperty name=\""+beanVar+"\" "+						 // NOI18N
                         "property=\""+propDesc.getName()+"\" "+							 // NOI18N
                         "value=\""+value+"\" /> \n";										 // NOI18N
        return jspCode;
    }

public String toString() { return jspBean.getBeanVariableName()+"."+propDesc.getName();}		 // NOI18N



}



