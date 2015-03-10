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
import org.netbeans.modules.web.wizards.beanjsp.ui.*;
import org.netbeans.modules.web.util.*;

import java.lang.*;
import java.io.*;
import java.util.*;
import java.lang.reflect.*;

import java.beans.*;


public class JSPBeanMethod extends Object implements JSPItem {

    MethodDescriptor methodDesc;
    JSPBean jspBean;
    /** Constructor */
    public JSPBeanMethod(JSPBean jspBean, MethodDescriptor methodDesc) {
        this.jspBean = jspBean;
        this.methodDesc = methodDesc;
    }

    public String getName() { return this.methodDesc.getName();}

    public boolean hasKey(Object key) {
        if(!(key instanceof JSPBean))
            return false;
        return isMethodOf((JSPBean)key);
    }

public Object getKey() { return jspBean; }

    public boolean isMethodOf(JSPBean jspBean) {
        if(this.jspBean.getBeanName().equals(jspBean.getBeanName()))
            return true;
        else
            return false;
    }

    public String toJSPCode() {

        String beanVar = jspBean.getBeanVariableName();

        Method method = methodDesc.getMethod();
        Class[] params = method.getParameterTypes();
        Class ret = method.getReturnType();

        String methodCall = beanVar+"."+method.getName();								 // NOI18N

        if( ret.isAssignableFrom(Void.TYPE) &&
                (params == null || params.length == 0) )  {
            return " <% "+															// NOI18N
                   methodCall+"( );"+														// NOI18N
                   " %> \n";																// NOI18N

        } else {
            String methodComments = "<!-- This method has non void return type or"+									// NOI18N
                                    " one or more parameters required. Please modify it in your JSP Page -->\n";		// NOI18N

            StringBuffer unsupportedMethodCall = new StringBuffer();
            // upsupportedMethodCall.append(methodComments);
            if(!ret.isAssignableFrom(Void.TYPE))
                unsupportedMethodCall.append(ret.getName()+" retVar = ");				 // NOI18N

            unsupportedMethodCall.append(methodCall+"(");								 // NOI18N
            if(params != null && params.length > 0) {
                unsupportedMethodCall.append(" "+params[0]);							 // NOI18N
                for(int i=1; i < params.length; ++i) {
                    unsupportedMethodCall.append(", "+params[i]);						 // NOI18N
                }
            }
            unsupportedMethodCall.append(" );");										 // NOI18N

            JSPPageWizard.doNonVoidMethodWarning = true;

            return methodComments+"<!-- <\\% "+									 // NOI18N
                   unsupportedMethodCall.toString()+
                   " %>  --> \n";																 // NOI18N
        }

    }

    public String getDisplayName() {
        Method method = methodDesc.getMethod();
        Class[] params = method.getParameterTypes();
        StringBuffer displayBuff = new StringBuffer();
        displayBuff.append(method.getName()+"(");			// NOI18N
        if(params != null && params.length > 0) {
            displayBuff.append(" "+params[0]);			 // NOI18N
            for(int i=1; i < params.length; ++i) {
                displayBuff.append(", "+params[i]);	 // NOI18N
            }
        }
        displayBuff.append(")");				 // NOI18N
        return displayBuff.toString();
    }

    public String toString() { return jspBean.getBeanVariableName()+"."+this.getDisplayName();}		 // NOI18N

}

