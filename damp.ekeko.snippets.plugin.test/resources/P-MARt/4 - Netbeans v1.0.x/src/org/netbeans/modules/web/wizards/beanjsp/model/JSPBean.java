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


import java.lang.*;
import java.io.*;
import java.util.*;
import java.lang.reflect.*;

import java.beans.*;



public class JSPBean extends Object implements JSPItem , Cloneable {

    public final static int SCOPE_PAGE = 0;
    public final static int SCOPE_REQUEST = 1;
    public final static int SCOPE_SESSION = 2;
    public final static int SCOPE_APPLICATION = 3;

    public final static String SCOPE_PAGE_STR = "page";								 // NOI18N
    public final static String SCOPE_REQUEST_STR = "request";							 // NOI18N
    public final static String SCOPE_SESSION_STR = "session";							 // NOI18N
    public final static String SCOPE_APPLICATION_STR = "application";					 // NOI18N


    private String beanName;

    private String beanVariableName;
    private int beanScope;

    private BeanInfo beanInfo;

    private JSPVector initBeanProperties;


    public JSPBean(BeanInfo beanInfo) {
        this.beanInfo = beanInfo;
        beanScope = SCOPE_PAGE;
        initBeanProperties = new JSPVector();  // clone should do deep copy here. why?
        try {
            beanName = beanInfo.getBeanDescriptor().getName();
            beanVariableName = toVariableName(beanName);
        }catch(Exception ex) {}  // ignored safely
    }

    public Object clone() {
        try {
            return super.clone();
        }catch(CloneNotSupportedException ex) {
            return null;
        }
    }

    public boolean hasKey(Object key) {
        if(!(key instanceof JSPBean))
            return false;
        return (key == this);
    }
public Object getKey() { return this; }


    public String getBeanName() { return beanName; }
    public void setBeanName(String beanName){this.beanName = beanName;}

    public String getBeanVariableName() { return beanVariableName; }
    public void setBeanVariableName(String beanVariableName){this.beanVariableName = beanVariableName;}

    public int getBeanScope(){ return beanScope; }
    public void setBeanScope(int beanScope) { this.beanScope = beanScope; }

    public BeanInfo getBeanInfo() { return this.beanInfo;}

    public static Vector getScopeList() {
        Vector scopeList = new Vector();
        scopeList.addElement(SCOPE_APPLICATION_STR);
        scopeList.addElement(SCOPE_SESSION_STR);
        scopeList.addElement(SCOPE_PAGE_STR);
        scopeList.addElement(SCOPE_REQUEST_STR);
        return scopeList;
    }

    public static String toScopeString(int beanScope) {
        switch(beanScope) {
        case SCOPE_PAGE:
            return SCOPE_PAGE_STR;
        case SCOPE_REQUEST:
            return SCOPE_REQUEST_STR;
        case SCOPE_SESSION:
            return SCOPE_SESSION_STR;
        case SCOPE_APPLICATION:
            return SCOPE_APPLICATION_STR;
        default:
            return "";					 // NOI18N
        }
    }

    public static int toScopeValue(String scopeString) {
        if(scopeString.equals(SCOPE_PAGE_STR))
            return SCOPE_PAGE;
        else if(scopeString.equals(SCOPE_REQUEST_STR))
            return SCOPE_REQUEST;
        else if(scopeString.equals(SCOPE_SESSION_STR))
            return SCOPE_SESSION;
        else if(scopeString.equals(SCOPE_APPLICATION_STR))
            return SCOPE_APPLICATION;
        else
            return SCOPE_PAGE;
    }

    public String toVariableName(String name) {
        //// modified decaptitalize of beans introspector
        if (name == null || name.length() == 0) {
            return name;
        }

        int len = name.length();
        int toLower = 0;
        for(int i = 0; i < len; ++i) {
            if(Character.isUpperCase(name.charAt(i)))
                ++toLower;
            else
                break;
        }

        char chars[] = name.toCharArray();
        if(toLower > 1 ) {
            for(int i=0; i < toLower-1; ++i) {
                chars[i] = Character.toLowerCase(chars[i]);
            }
        }else {
            chars[0] = Character.toLowerCase(chars[0]);
        }
        return new String(chars);

    }

    // call this method just before code generation . otherwise you will be in
    // trouble updating multiple copies of props

    public void addInitBeanProperty(JSPBeanField initProp) {
        this.initBeanProperties.add(initProp);
    }

    // call this method just before code generation . otherwise you will be in
    // trouble updating multiple copies of props

    public void addInitBeanProperties(JSPVector initProps) {
        this.initBeanProperties = initProps;
    }

    public String toUseBeanInitBodyJSPCode() {
        StringBuffer initJSPCode = new StringBuffer();
        Iterator iterator = initBeanProperties.iterator();
        for(;iterator.hasNext(); ) {
            JSPBeanField initField = (JSPBeanField) iterator.next();
            initJSPCode.append(initField.toUseBeanInitJSPCode());
        }
        return initJSPCode.toString();
    }

    public String toJSPCode() {
        String jspCode = "<jsp:useBean id=\""+this.getBeanVariableName()+"\" ";							 // NOI18N
        String scope = toScopeString(getBeanScope());
        if(scope.length() > 0 )
            jspCode += "scope=\""+scope+"\" ";															 // NOI18N
        jspCode += "class=\""+this.getBeanInfo().getBeanDescriptor().getBeanClass().getName()+"\" ";		 // NOI18N
        jspCode += " > ";																					 // NOI18N

        // jspCode += this.toUseBeanInitBodyJSPCode();

        jspCode += "</jsp:useBean>";					 // NOI18N

        return jspCode;
    }

public String toString() { return this.getBeanInfo().getBeanDescriptor().getBeanClass().getName();}
}


