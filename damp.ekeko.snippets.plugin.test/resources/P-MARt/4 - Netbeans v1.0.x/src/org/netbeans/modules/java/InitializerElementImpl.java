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
import javax.swing.text.StyledDocument;
import javax.swing.text.BadLocationException;

import org.openide.src.*;
import org.openide.text.*;
import org.openide.text.PositionRef;
import org.openide.text.NbDocument;

/** Element which represents the initializer block.
*
* @author Petr Hamernik
*/
final class InitializerElementImpl extends ElementImpl implements InitializerElement.Impl {
    /** Is this block static ? */
    boolean stat;

    static final long serialVersionUID =4077327616265897219L;
    /** Constructor for the parser.
    */
    InitializerElementImpl() {
    }

    /** Copy constructor. It copy all fields from the parameter and
    * It regenerates the text in the given bounds.
    * @param el source for this element.
    * @param bounds where to create the new element.
    */
    InitializerElementImpl(InitializerElement el, PositionBounds bounds) throws SourceException {
        super(bounds);
        stat = el.isStatic();
        //   javadoc = new JavaDocImpl(el.getJavaDoc().getRawText(), this);
        if (bounds != null)
            regenerate(el);
    }

    /** Updates the element fields. This method is called after reparsing.
    * @param impl the carrier of new information.
    */
    void updateImpl(InitializerElementImpl impl) {
	super.updateImpl(impl);
        if (stat != impl.stat) {
            stat = impl.stat;
            firePropertyChange(PROP_STATIC, new Boolean(!stat), new Boolean(stat));
        }
    }

    /** Sets the 'static' flag for this initializer. */
    public void setStatic(final boolean stat) throws SourceException {
        if (stat == this.stat)
            return;

        checkNotLocked();
        this.stat = stat;
        try {
            regenerate((InitializerElement) element);
            firePropertyChange(PROP_STATIC, new Boolean(!stat), new Boolean(stat));
        }
        catch (SourceException e) {
            this.stat = !stat;
            throw e;
        }
    }

    /** is this initializer static.
    * @return true if it is.
    */
    public boolean isStatic() {
        return stat;
    }

    /** Sets body of the element.
    * @param s the body
    */
    public void setBody(String s) throws SourceException {
        checkNotLocked();
        try {
            bodyBounds.setText(s);
            firePropertyChange(PROP_BODY, null, null);
        }
        catch (Exception e) {
            throw new SourceException(e.getMessage());
        }
    }

    /** Getter for the body of element.
    * @return the string representing the body
    */
    public String getBody() {
        try {
            return bodyBounds.getText();
        }
        catch (BadLocationException e) {
        }
        catch (IOException e) {
        }
        return ""; // NOI18N
    }

    /** Gets the javadoc for this initializer */
    public JavaDoc getJavaDoc() {
        return javadoc;
    }

    public Object readResolve() {
        return new InitializerElement(this, null);
    }

    SourceElementImpl findSourceElementImpl() {
        ClassElement c = ((InitializerElement)element).getDeclaringClass();
        ClassElementImpl impl = (ClassElementImpl) c.getCookie(ClassElementImpl.class);
        return impl.findSourceElementImpl();
    }

    public void markCurrent(boolean beforeAfter) {

    }

}
/*
 * Log
 *  20   Gandalf-post-FCS1.18.1.0    4/3/00   Svatopluk Dedic Checks against modifying
 *       deleted elements
 *  19   src-jtulach1.18        1/12/00  Petr Hamernik   i18n: perl script used (
 *       //NOI18N comments added )
 *  18   src-jtulach1.17        1/10/00  Petr Hamernik   regeneration of 
 *       ClassElements improved (AKA #4536)
 *  17   src-jtulach1.16        10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  16   src-jtulach1.15        8/9/99   Ian Formanek    Generated Serial Version
 *       UID
 *  15   src-jtulach1.14        7/8/99   Petr Hamernik   changes reflecting 
 *       org.openide.src changes
 *  14   src-jtulach1.13        6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  13   src-jtulach1.12        6/2/99   Petr Hamernik   connections of java 
 *       sources
 *  12   src-jtulach1.11        5/17/99  Petr Hamernik   missing implementation 
 *       added
 *  11   src-jtulach1.10        5/10/99  Petr Hamernik   
 *  10   src-jtulach1.9         4/21/99  Petr Hamernik   Java module updated
 *  9    src-jtulach1.8         4/15/99  Petr Hamernik   parser improvements
 *  8    src-jtulach1.7         4/1/99   Petr Hamernik   
 *  7    src-jtulach1.6         3/29/99  Petr Hamernik   
 *  6    src-jtulach1.5         3/22/99  Petr Hamernik   
 *  5    src-jtulach1.4         3/18/99  Petr Hamernik   
 *  4    src-jtulach1.3         3/12/99  Petr Hamernik   
 *  3    src-jtulach1.2         3/10/99  Petr Hamernik   
 *  2    src-jtulach1.1         2/25/99  Petr Hamernik   
 *  1    src-jtulach1.0         2/18/99  Petr Hamernik   
 * $
 */
