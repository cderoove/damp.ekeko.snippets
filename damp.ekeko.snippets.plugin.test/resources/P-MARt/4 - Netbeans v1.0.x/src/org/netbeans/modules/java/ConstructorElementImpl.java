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

package org.netbeans.modules.java;

import java.io.*;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.LinkedList;

import javax.swing.text.BadLocationException;

import org.openide.src.*;
import org.openide.text.PositionBounds;

/** Describes the constructor of the class.
*
* @author Petr Hamernik
*/
class ConstructorElementImpl extends MemberElementImpl implements ConstructorElement.Impl {
    /** arguments of the constructor or method */
    MethodParameter[] parameters;

    /** exceptions throwed by the constructor or method */
    Identifier[] exceptions;

    static final long serialVersionUID =-3124476910586352527L;
    /** Constructor for the parser. */
    public ConstructorElementImpl() {
    }

    /** Copy constructor.
    * @param el element to copy from
    */
    public ConstructorElementImpl(ConstructorElement el, PositionBounds bounds) throws SourceException {
        super(el, bounds);
        exceptions = el.getExceptions ();
        parameters = el.getParameters ();
        //    javadoc = new JavaDocImpl.Method(el.getJavaDoc().getRawText(), this);
        if ((bounds != null) && !(this instanceof MethodElementImpl))
            regenerate(el);
    }

    MemberElement updateImpl(ConstructorElementImpl impl, boolean retValue) {
        MemberElement prevElement = super.updateImpl(impl, retValue);
        // Parameters
        if (!compareParams(parameters, impl.parameters, false)) {
            if (retValue && (prevElement == null) && !compareParams(parameters, impl.parameters, true)) {
                prevElement = (ConstructorElement)(((ConstructorElement)element).clone());
            }
            MethodParameter[] old = parameters;
            parameters = impl.parameters;
            firePropertyChange(PROP_PARAMETERS, old, parameters);
        }

        // Exceptions
        if (!compareIdentifiers(exceptions, impl.exceptions, true)) {
            if (retValue && (prevElement == null))
                prevElement = (ConstructorElement)(((ConstructorElement)element).clone());
            Identifier[] old = exceptions;
            exceptions = impl.exceptions;
            firePropertyChange(PROP_EXCEPTIONS, old, exceptions);
        }
        return prevElement;
    }

    /** Updates the element fields. This method is called after reparsing.
    * @param impl the carrier of new information.
    */
    void updateImpl(ConstructorElementImpl impl, LinkedList changes, int changesMask) {
        boolean changesMatch = ((changesMask & JavaConnections.TYPE_CONSTRUCTORS_CHANGE) != 0);
        MemberElement prevElement = updateImpl(impl, changesMatch);
        if (changesMatch && (prevElement != null)) {
            changes.add(new JavaConnections.Change(JavaConnections.TYPE_CONSTRUCTORS_CHANGE, prevElement, element));
        }
    }

    /** @return the parameters
    */
    public MethodParameter[] getParameters() {
        MethodParameter[] ret = new MethodParameter[parameters.length];
        for (int i = 0; i < ret.length; i++)
            ret[i] = new MethodParameter(parameters[i].getName(),
                                         parameters[i].getType(),
                                         parameters[i].isFinal());
        return ret;
    }

    /** sets the method parameters
    */
    public void setParameters(MethodParameter[] parameters) throws SourceException {
        if (!compareSourceParams(this.parameters, parameters, false)) {
            checkNotLocked();
            MethodParameter[] old = this.parameters;
            this.parameters = parameters;
            try {
                regenerateHeader();
                modify();
                firePropertyChange (PROP_PARAMETERS, old, parameters);
            }
            catch (SourceException e) {
                this.parameters = old;
                throw e;
            }
        }
    }

    /** @return the array of the exceptions throwed by the method.
    */
    public Identifier[] getExceptions() {
        Identifier[] ret = new Identifier[exceptions.length];
        System.arraycopy(exceptions, 0, ret, 0, exceptions.length);
        return ret;
    }

    /** Sets the array of the exceptions throwed by the method.
    */
    public void setExceptions(Identifier[] exceptions) throws SourceException {
        if (!compareSourceIdentifiers(this.exceptions, exceptions)) {
            checkNotLocked();
            Identifier[] old = this.exceptions;
            this.exceptions = exceptions;
            try {
                modify();
                regenerateHeader();
                firePropertyChange (PROP_EXCEPTIONS, old, exceptions);
            }
            catch (SourceException e) {
                this.exceptions = old;
                throw e;
            }
        }
    }

    protected static boolean compareSourceParams(MethodParameter[] params1, MethodParameter[] params2, boolean onlyType) {
        if ((params1.length == 0) && (params2.length == 0))
            return true;

        if (params1.length != params2.length)
            return false;

        for (int i = 0; i < params1.length; i++) {
            if ((!onlyType && params1[i].getName().compareTo(params2[i].getName()) != 0) ||
                    !compareSourceTypes(params1[i].getType(), params2[i].getType())) {
                return false;
            }
	    if (params1[i].isFinal() != params2[i].isFinal()) {
		return false;
	    }
        }
        return true;
    }

    /** Compares two arrays of parameters.
    * @return true if the arrays contains the same params.
    */
    private static boolean compareParams(MethodParameter[] params1, MethodParameter[] params2, boolean onlyType) {
        if ((params1.length == 0) && (params2.length == 0))
            return true;

        if (params1.length != params2.length)
            return false;

        for (int i = 0; i < params1.length; i++) {
            if (!params1[i].compareTo(params2[i], onlyType, true))
                return false;
        }
        return true;
    }

    static boolean compareSourceIdentifiers(Identifier[] ids1, Identifier[] ids2) {
        if ((ids1.length == 0) && (ids2.length == 0))
            return true;

        if (ids1.length != ids2.length)
            return false;

        for (int i = 0; i < ids1.length; i++) {
            if (!compareSourceIdentifiers(ids1[i], ids2[i]))
                return false;
        }
        return true;
    }

    /** Compares two arrays of indentifiers.
    * @return true if the arrays contains the same identifiers.
    */
    static boolean compareIdentifiers(Identifier[] ids1, Identifier[] ids2, boolean source) {
        if ((ids1.length == 0) && (ids2.length == 0))
            return true;

        if (ids1.length != ids2.length)
            return false;

        for (int i = 0; i < ids1.length; i++) {
            if (!ids1[i].compareTo(ids2[i], source))
                return false;
        }
        return true;
    }

    /** Sets body of the element.
    * @param s the body
    */
    public void setBody(String s) throws SourceException {
        try {
            if (bodyBounds != null) { // body was set
                if (s != null) {
                    bodyBounds.setText(s);
                }
                else {
                    regenerate(element);
                }
            }
            else { // body was not set
                if (s == null) {
                    return;
                }
                else {
                    regenerate(element);
                }
            }
            firePropertyChange (PROP_BODY, null, null);
        }
        catch (Exception e) {
            throw new SourceException(e.getMessage());
        }
    }

    /** Getter for the body of element.
    * @return the string representing the body
    */
    public String getBody () {
        try {
            if (bodyBounds != null)
                return bodyBounds.getText();
        }
        catch (BadLocationException e) {
        }
        catch (IOException e) {
        }
        return null;
    }

    /** Provides access to constructor java doc.
    * @return constructor java doc
    */
    public JavaDoc.Method getJavaDoc () {
        return (JavaDoc.Method) javadoc;
    }

    public Object readResolve() {
        return new ConstructorElement(this, null);
    }
}

/*
 * Log
 *  24   Gandalf-post-FCS1.22.1.0    4/3/00   Svatopluk Dedic Parameters are compared 
 *       before modification; JavaConnection fixes
 *  23   src-jtulach1.22        1/14/00  Petr Hamernik   fixed #3726
 *  22   src-jtulach1.21        1/11/00  Petr Hamernik   fixed #3490
 *  21   src-jtulach1.20        1/10/00  Petr Hamernik   regeneration of 
 *       ClassElements improved (AKA #4536)
 *  20   src-jtulach1.19        10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  19   src-jtulach1.18        8/9/99   Ian Formanek    Generated Serial Version
 *       UID
 *  18   src-jtulach1.17        6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  17   src-jtulach1.16        6/2/99   Petr Hamernik   connections of java 
 *       sources
 *  16   src-jtulach1.15        5/17/99  Petr Hamernik   missing implementation 
 *       added
 *  15   src-jtulach1.14        5/14/99  Petr Hamernik   getters improved (clone 
 *       the array before return)
 *  14   src-jtulach1.13        5/13/99  Petr Hamernik   
 *  13   src-jtulach1.12        5/12/99  Petr Hamernik   ide.src.Identifier 
 *       changed
 *  12   src-jtulach1.11        5/11/99  Petr Hamernik   firing once more changed
 *  11   src-jtulach1.10        5/11/99  Petr Hamernik   fire EXCEPTION property 
 *       improved
 *  10   src-jtulach1.9         5/10/99  Petr Hamernik   
 *  9    src-jtulach1.8         4/30/99  Petr Hamernik   
 *  8    src-jtulach1.7         4/21/99  Petr Hamernik   Java module updated
 *  7    src-jtulach1.6         4/2/99   Petr Hamernik   
 *  6    src-jtulach1.5         3/29/99  Petr Hamernik   
 *  5    src-jtulach1.4         3/29/99  Petr Hamernik   
 *  4    src-jtulach1.3         3/15/99  Petr Hamernik   
 *  3    src-jtulach1.2         3/10/99  Petr Hamernik   
 *  2    src-jtulach1.1         2/25/99  Petr Hamernik   
 *  1    src-jtulach1.0         2/18/99  Petr Hamernik   
 * $
 */
