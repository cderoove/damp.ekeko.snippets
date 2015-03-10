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

import org.openide.text.PositionBounds;
import org.openide.src.*;

/** Describes a main elements of java source
* (variables, methods and classes). Provides support
* for associating this element with declaring class.
*
* @author Petr Hamernik, Jaroslav Tulach
*/
abstract class MemberElementImpl extends ElementImpl implements MemberElement.Impl {
    /** Modifiers for this element */
    int mod;

    /** Name of this element */
    Identifier name;

    /**
        True, if the element was invalidated for some reason e.g. programmatic
        change. The element will be revalidated on the next parse. This is to support
        JavaConnections. If a member element is updated and it has `needUpdate' set to true,
        it fires change event regardless of its previous state.
    */
    private transient boolean updateNeeded = false;

    static final long serialVersionUID =6388377681336329844L;
    /** Constructor for the parser. */
    public MemberElementImpl() {
    }

    /** Copy */
    public MemberElementImpl(MemberElement el, PositionBounds bounds) throws SourceException {
        super(bounds);
        mod = el.getModifiers();
        name = el.getName();
    }

    protected boolean isUpdateNeeded() {
        return updateNeeded;
    }

    protected void modify() {
        updateNeeded = true;
    }

    /** Compares source names of the identifiers; it does not pay attention to full names
        specified in the identifier object.
    */
    protected static boolean compareSourceIdentifiers(Identifier oldId, Identifier newId) {
        return oldId == newId || oldId.getSourceName().equals(newId.getSourceName());
    }

    /** Compares types not paying attention to fully qualified names of class types.
    */
    protected static boolean compareSourceTypes(Type oldType, Type newType) {
        if (oldType == newType) {
            return true;
        }
        // if one of the types is a primitive one, they must be the same instance to match.
        if (oldType.isPrimitive() || newType.isPrimitive()) {
            return false;
        }
        if (oldType.isArray()) {
            if (!newType.isArray()) {
                return false;
            }
            return compareSourceTypes(oldType.getElementType(), newType.getElementType());
        } else if (newType.isArray()) {
            return false;
        }
        if (!oldType.isClass() || !newType.isClass()) {
            throw new InternalError("Unexpected type combination.");
        }
        return oldType.getSourceString().equals(newType.getSourceString());
    }

    /** Updates the element fields. This method is called after reparsing.
    * @param impl the carrier of new information.
    */
    MemberElement updateImpl(MemberElementImpl impl, boolean retValue) {
        super.updateImpl(impl);
        MemberElement retElement = null;

        if (isUpdateNeeded()) {
            try {
                retElement = (MemberElement)(((MemberElement)element).clone());
                updateNeeded = false;
            }
            catch (CloneNotSupportedException e) {
            }
        }
        if (mod != impl.mod) {
            ClassElement c = ((MemberElement)element).getDeclaringClass();

            // Do NOT create return value for interface members; all interface members
            // have implicit modifiers that might, but should not be specified in the code.
            if ((c == null || c.isClassOrInterface()) && retValue) {
                try {
                    retElement = (MemberElement)(((MemberElement)element).clone());
                }
                catch (CloneNotSupportedException e) {
                }
            }
            int old = mod;
            mod = impl.mod;
            firePropertyChange(PROP_MODIFIERS, new Integer(old), new Integer(mod));
        }
        if (!name.compareTo(impl.name, true)) {
            if (retValue && (retElement == null)) {
                try {
                    retElement = (MemberElement)(((MemberElement)element).clone());
                }
                catch (CloneNotSupportedException e) {
                }
            }
            Identifier old = name;
            name = impl.name;
            firePropertyChange(PROP_NAME, old, name);
        }
        return retElement;
    }

    /** Getter for modifiers for this element.
    * @see java.lang.reflect.Modifier
    * @return constants from java.lang.reflect.Modifier
    */
    public int getModifiers() {
        return mod;
    }

    /** Setter for modifiers for this element.
    * @see java.lang.reflect.Modifier
    * @param mod constants from java.lang.reflect.Modifier
    */
    public void setModifiers(int mod) throws SourceException {
        if (mod == this.mod) {
            return;
        }

        checkNotLocked();
        int old = this.mod;
        this.mod = mod;
        try {
            regenerateHeader();
            firePropertyChange(PROP_MODIFIERS, new Integer(old), new Integer(mod));
        }
        catch (SourceException e) {
            this.mod = old;
            throw e;
        }
    }

    /** Getter for name of the field.
    * @return the name
    */
    public Identifier getName() {
        return name;
    }

    /** Setter for name of the field.
    * @param name the name of the field
    */
    public synchronized void setName(Identifier name) throws SourceException {
        if (name.getSourceName().equals(this.name.getSourceName())) {
            return;
        }
        Identifier old = this.name;
        this.name = name;
        try {
            regenerateHeader();
            modify();
            firePropertyChange(PROP_NAME, old, name);
        }
        catch (SourceException e) {
            this.name = old;
            throw e;
        }
    }

    public void markCurrent(boolean beforeAfter) {

    }

    SourceElementImpl findSourceElementImpl() {
        ClassElement c = (element instanceof ClassElement) ?
                         (ClassElement)element :
                         ((MemberElement)element).getDeclaringClass();

        return (SourceElementImpl) c.getSource().getCookie(SourceElementImpl.class);
    }


}

/*
 * Log
 *  18   Gandalf-post-FCS1.14.2.2    4/17/00  Svatopluk Dedic Programmatic name change
 *       will fire JavaConnection
 *  17   Gandalf-post-FCS1.14.2.1    4/3/00   Svatopluk Dedic Checks against current 
 *       state before a modification is done; support for comparing source-text 
 *       representations of Identifier and Type
 *  16   Gandalf-post-FCS1.14.2.0    2/24/00  Ian Formanek    Post FCS changes
 *  15   src-jtulach1.14        10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  14   src-jtulach1.13        8/9/99   Ian Formanek    Generated Serial Version
 *       UID
 *  13   src-jtulach1.12        7/8/99   Petr Hamernik   changes reflecting 
 *       org.openide.src changes
 *  12   src-jtulach1.11        6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  11   src-jtulach1.10        6/2/99   Petr Hamernik   connections of java 
 *       sources
 *  10   src-jtulach1.9         5/13/99  Petr Hamernik   changes in comparing 
 *       Identifier, Type classes
 *  9    src-jtulach1.8         5/12/99  Petr Hamernik   ide.src.Identifier 
 *       changed
 *  8    src-jtulach1.7         5/10/99  Petr Hamernik   
 *  7    src-jtulach1.6         4/28/99  Petr Hamernik   simple synchronization 
 *       using ConnectionCookie
 *  6    src-jtulach1.5         4/21/99  Petr Hamernik   Java module updated
 *  5    src-jtulach1.4         3/29/99  Petr Hamernik   
 *  4    src-jtulach1.3         3/29/99  Petr Hamernik   
 *  3    src-jtulach1.2         3/10/99  Petr Hamernik   
 *  2    src-jtulach1.1         2/25/99  Petr Hamernik   
 *  1    src-jtulach1.0         2/18/99  Petr Hamernik   
 * $
 */
