/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.rmi.wizard;

import java.io.*;
import java.lang.reflect.*;
import java.text.*;
import java.util.*;

import org.openide.src.*;

/**
 *
 * @author  mryzl
 */

public class SrcSupport extends Object {

    /** Identifier for java.rmi.RemoteException */
    public static final Identifier REMOTE_EXCEPTION = Identifier.create("java.rmi.RemoteException");

    /** Message format for comment of the method. */
    public static final MessageFormat METHOD_COMMENT = new MessageFormat("Method {0}.\n\n");

    /** Creates new SrcSupport. */
    public SrcSupport() {
    }

    /** Parse parameter line. If there is the name missing, new one will be created.
    * The name will be param + number
    *
    * @param line - line to be parsed
    * @return array of methods
    */
    public static MethodParameter[] parseParameters(String line) throws IllegalArgumentException {
        int iNum = 0;
        StringTokenizer st = new StringTokenizer(line, ",");
        MethodParameter[] mp = new MethodParameter[st.countTokens()];
        for(int i = 0; st.hasMoreTokens(); i++) {
            String token = st.nextToken();
            try {
                mp[i] = MethodParameter.parse(token);
            } catch (IllegalArgumentException ex) {
                // give'em another chance, perhaps there is only missing name
                mp[i] = MethodParameter.parse(token + " param" + (iNum++));
            }
        }
        return mp;
    }

    /** If some of the parameters have not names, create any.
    * @param mp - parameters
    * @return named parameters
    */
    public static MethodParameter[] nameParameters(MethodParameter[] mp) {
        int iNum = 0;

        for(int i = 0; i < mp.length; i++) {
            String name = mp[i].getName();
            if ((name == null) || (name.length()  == 0)) {
                mp[i].setName("param" + (iNum++));
            }
        }
        return mp;
    }

    /** Get parameters as one String.
    * @param mp - parameters
    * @return parameters as a String
    */
    public static String getParameters(MethodParameter[] mp) {
        StringBuffer sb = new StringBuffer();
        for(int i = 0; i < mp.length;  i++) {
            if (i != 0) sb.append(", ");
            sb.append(mp[i]);
        }
        return sb.toString();
    }

    /** Get parameters as one String.
    * @param mp - parameters
    * @return parameters as a String
    */
    public static String getParameterNames(MethodParameter[] mp) {
        StringBuffer sb = new StringBuffer();
        for(int i = 0; i < mp.length;  i++) {
            if (i != 0) sb.append(", ");
            sb.append(mp[i].getName());
        }
        return sb.toString();
    }

    /** Creates new MethodElement for RMI.
    *
    * @param params - parameters of the method
    * @return return type
    * @return properly set method element
    */
    public static MethodElement getRMIMethodElement(String name, String params, String rtype)
    throws IllegalArgumentException, SourceException {
        return getRMIMethodElement(null, name, params, rtype);
    }

    /** Creates MethodElement for RMI.
    *
    * @param me - method element. If null a new one will be created.
    * @param params - parameters of the method
    * @return return type
    * @return properly set method element
    */
    public static MethodElement getRMIMethodElement(MethodElement me, String name, String params, String rtype)
    throws IllegalArgumentException, SourceException {
        if (me == null) me = new MethodElement();
        me.setReturn(Type.createClass(Identifier.create(rtype)));
        me.setName(Identifier.create(name));
        me.setParameters(parseParameters(params));
        me.setExceptions(new Identifier[] {
                             REMOTE_EXCEPTION,
                         });
        me.setModifiers(Modifier.PUBLIC);
        return me;
    }

    /** Adds required exception to the method.
    * @param ce - method or constructor to be updated
    * @param exception - required exception 
    * @return updated method
    */
    public static ConstructorElement addException(ConstructorElement ce, String exception) throws SourceException {
        return addException(ce, Identifier.create(exception));
    }

    /** Adds required exception to the method.
    * @param ce - method or constructor to be updated
    * @param exception - required exception 
    * @return updated method
    */
    public static ConstructorElement addException(ConstructorElement ce, Identifier exception) throws SourceException {
        Identifier[] idents = ce.getExceptions(), newidents;
        int len = idents.length;
        for(int i = 0; i < len; i++) {
            if (idents[i].equals(exception)) return ce;
        }
        newidents = new Identifier[len + 1];
        System.arraycopy(idents, 0, newidents, 0, len);
        newidents[len] = exception;
        ce.setExceptions(newidents);
        return ce;
    }

    /** Creates comment for given method. It adds comment, then param tags for all
    * parameters, return and exception.
    *
    * @param me - methods to be commented
    * @param comment - general comment, if null, default comment will be added.
    */
    public static void commentMethod(ConstructorElement me, String comment) throws SourceException {
        StringBuffer sb = new StringBuffer();

        // set general comment
        if (comment == null) {
            METHOD_COMMENT.format(new Object[] { me.getName() } , sb, null);
        } else {
            sb.append(comment);
        }

        // set param tags
        MethodParameter[] mp = me.getParameters();
        for(int i = 0; i < mp.length; i++) {
            sb.append(" @param ");
            sb.append(mp[i].getName());
            sb.append("\n");
        }

        // set return type (only for methods)
        if (me instanceof MethodElement) {
            String ret = ((MethodElement)me).getReturn().toString();
            if (!ret.equals("void")) sb.append(" @return\n");
        }

        // set exceptions
        Identifier[] exs = me.getExceptions();
        if (exs.length > 0) {
            sb.append(" @throws ");
            for(int i = 0; i < exs.length; i++) {
                if (i != 0) sb.append(", ");
                sb.append(exs[i].toString());
            }
            sb.append("\n");
        }

        me.getJavaDoc().setRawText(sb.toString());
    }

    /** Creates constructor suitable for RMI Implementation.
    *
    * @param name - name of the constructor
    * @param params - parameters of the constructor
    * @param sup - parameter for super() statement
    * @param extends - if true, add throws RemoteException 
    */
    public static ConstructorElement getRMIConstructorElement(String name, String params, String sup, boolean ex)
    throws IllegalArgumentException, SourceException {
        ConstructorElement ce = new ConstructorElement();
        ce.setName(Identifier.create(name));
        ce.setModifiers(Modifier.PUBLIC);
        if (ex) {
            ce.setExceptions(new Identifier[] {
                                 REMOTE_EXCEPTION
                             });
        }
        MethodParameter[] mp = parseParameters(params);
        ce.setParameters(mp);
        StringBuffer sb = new StringBuffer();
        sb.append("\n ");
        if (sup != null) {
            sb.append("super(");
            sb.append(sup);
            sb.append(");\n");
        }
        ce.setBody(sb.toString());
        return ce;
    }
}

/*
* <<Log>>
*  7    Gandalf   1.6         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  6    Gandalf   1.5         7/28/99  Martin Ryzl     added selection of 
*       executor
*  5    Gandalf   1.4         7/28/99  Martin Ryzl     
*  4    Gandalf   1.3         7/27/99  Martin Ryzl     new version of generator 
*       is working
*  3    Gandalf   1.2         7/27/99  Martin Ryzl     compilation corrected
*  2    Gandalf   1.1         7/27/99  Martin Ryzl     
*  1    Gandalf   1.0         7/22/99  Martin Ryzl     
* $ 
*/ 
