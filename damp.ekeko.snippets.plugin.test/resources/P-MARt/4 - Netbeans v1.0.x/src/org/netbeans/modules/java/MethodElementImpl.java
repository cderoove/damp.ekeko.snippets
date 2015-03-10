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
import java.util.LinkedList;
import java.lang.reflect.Modifier;

import org.openide.src.*;
import org.openide.text.PositionBounds;

/** Describes method in the class.
*
* @author Petr Hamernik
*/
final class MethodElementImpl extends ConstructorElementImpl implements MethodElement.Impl {

    /** Type of exception */
    Type type;

    static final long serialVersionUID =3720482145527687805L;
    /** Constructor for the parser. */
    MethodElementImpl() {
    }

    /** Copy constructor.
    * @param el element to copy from
    */
    public MethodElementImpl(MethodElement el, PositionBounds bounds) throws SourceException {
        super(el, bounds);
        type = el.getReturn ();
        if (bounds != null)
            regenerate(el);
    }

    /** Updates the element fields. This method is called after reparsing.
    * @param impl the carrier of new information.
    */
    void updateImpl(MethodElementImpl impl, LinkedList changes, int changesMask) {
        boolean changesMatch = ((changesMask & JavaConnections.TYPE_METHODS_CHANGE) != 0);
        MemberElement prevElement = super.updateImpl(impl, changesMatch);
        if (!type.compareTo(impl.type, true)) {
            if (changesMatch && (prevElement == null))
                prevElement = (MethodElement)(((MethodElement)element).clone());
            Type old = type;
            type = impl.type;
            firePropertyChange(PROP_RETURN, old, type);
        }
        if (changesMatch && (prevElement != null)) {
            changes.add(new JavaConnections.Change(JavaConnections.TYPE_METHODS_CHANGE, prevElement, element));
        }
    }

    /** Setter for the return type */
    public Type getReturn() {
        return type;
    }

    /** @return the return type */
    public void setReturn (Type type) throws SourceException {
        checkNotLocked();
        if (compareSourceTypes(this.type, type)) {
            return;
        }
        Type old = this.type;
        this.type = type;
        try {
            regenerateHeader();
            modify();
            firePropertyChange(PROP_RETURN, old, type);
        }
        catch (SourceException e) {
            this.type = old;
            throw e;
        }
    }

    public Object readResolve() {
        return new MethodElement(this, null);
    }
}

/*
 * Log
 *  16   Gandalf-post-FCS1.14.1.0    4/3/00   Svatopluk Dedic Fixed JavaConnections 
 *       firing; Return types are compared before modification
 *  15   src-jtulach1.14        1/10/00  Petr Hamernik   regeneration of 
 *       ClassElements improved (AKA #4536)
 *  14   src-jtulach1.13        10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  13   src-jtulach1.12        8/9/99   Ian Formanek    Generated Serial Version
 *       UID
 *  12   src-jtulach1.11        6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  11   src-jtulach1.10        6/2/99   Petr Hamernik   connections of java 
 *       sources
 *  10   src-jtulach1.9         5/13/99  Petr Hamernik   changes in comparing 
 *       Identifier, Type classes
 *  9    src-jtulach1.8         5/10/99  Petr Hamernik   
 *  8    src-jtulach1.7         4/21/99  Petr Hamernik   Java module updated
 *  7    src-jtulach1.6         4/2/99   Petr Hamernik   
 *  6    src-jtulach1.5         3/29/99  Petr Hamernik   
 *  5    src-jtulach1.4         3/29/99  Petr Hamernik   
 *  4    src-jtulach1.3         3/12/99  Petr Hamernik   
 *  3    src-jtulach1.2         3/10/99  Petr Hamernik   
 *  2    src-jtulach1.1         2/25/99  Petr Hamernik   
 *  1    src-jtulach1.0         2/18/99  Petr Hamernik   
 * $
 */
