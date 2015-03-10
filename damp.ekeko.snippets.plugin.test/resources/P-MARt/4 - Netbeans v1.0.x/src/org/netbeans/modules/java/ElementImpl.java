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
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;

import org.openide.nodes.*;
import org.openide.src.*;
import org.openide.cookies.OpenCookie;
import org.openide.cookies.ElementCookie;
import org.openide.cookies.FilterCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.PositionRef;
import org.openide.text.PositionBounds;
import org.openide.text.EditorSupport;

/** Root of the plugable behaviour for the java sources.
*
* @author Petr Hamernik
*/
abstract class ElementImpl implements org.openide.src.Element.Impl, ElementProperties, OpenCookie,
    ElementFactory.Item {
    /** Element */
    transient Element element;

    /** Property change support */
    private transient PropertyChangeSupport support;

    /** array of cookies for this element */
    private transient CookieSet cookieSet;

    /** Flag that prevents any changes to be made to this element. Any attempt to
      make such a change will throw SourceException. 
      Currently, elements are locked iff they contain template specification
      (since Forte do not support them properly) - to prevent source code damage.
    */
    private transient boolean locked;

    /** True, if the element is not valid - e.g. it was deleted from the source. */
    private transient boolean valid = true;

    /** Reference to TextElement for this source element impl */
    Reference textElementRef;

    /** Position of the begin and the end of the element. */
    PositionBounds bounds;

    /** Bounds of the javadoc. Subclasses may not use it. */
    PositionBounds docBounds;

    /** Bounds of the header. Subclasses may not use it. */
    PositionBounds headerBounds;

    /** Bounds of the body. Subclasses may not use it. */
    PositionBounds bodyBounds;

    /** Javadoc. */
    JavaDocImpl javadoc;

    /** Hash of element's body. Can use any algorithm, must implement equals.
        Currently, the hash value is computed as CRC-32 of the element's body
    */
    private transient Object bodyHash;

    static final long serialVersionUID =-1411884372521664497L;

    /** Default constructor - only for parser. */
    ElementImpl() {
    }

    /** Constructor */
    public ElementImpl(PositionBounds bounds) {
        this.bounds = bounds;
    }

    /** Updates the element fields. This method is called after reparsing.
    * @param impl the carrier of new information.
    */
    void updateImpl(ElementImpl impl) {
        bounds = impl.bounds;
        docBounds = impl.docBounds;
        headerBounds = impl.headerBounds;
        bodyBounds = impl.bodyBounds;
        javadoc = impl.javadoc;
        javadoc.impl = this;

        Object newHash;
        newHash = getBodyHash();
        if (newHash != null && (bodyHash == null || !bodyHash.equals(newHash))) {
            // fire body property change...
            firePropertyChange(PROP_BODY, null, null);
        }
        bodyHash = newHash;
    }

    /** Computes and returns CRC32 of element's body
      @return CRC32 object describing the body
    */
    protected Object getBodyHash() {
        if (bodyBounds == null) {
            return null;
        }
        if (!bodyBounds.getBegin().getEditorSupport().isDocumentLoaded()) {
            // Do not even try to compute the hash if the document has not been already
            // loaded.
            return null;
        }
        java.util.zip.CRC32 crc = null;
        try {
            java.util.zip.CRC32 crc2 =  new java.util.zip.CRC32();
            crc2.update(bodyBounds.getText().getBytes());
            crc = crc2;
        } catch (BadLocationException e) {
        } catch (IOException e) {
        }
        return crc;
    }

    void setBodyBounds(PositionBounds b) {
        bodyBounds = b;
        bodyHash = getBodyHash();
    }

    /** Get the cookie set.
    * @return the cookie set.
    */
    protected synchronized final CookieSet getCookieSet() {
        if (cookieSet == null) {
            cookieSet = new CookieSet();
            cookieSet.add(this);
            cookieSet.addChangeListener(
                new ChangeListener() {
                    public void stateChanged(ChangeEvent ev) {
                        ElementImpl.this.firePropertyChange(Node.PROP_COOKIE, null, null);
                    }
                }
            );
        }
        return cookieSet;
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        // recompute element's hash according to the document contents.
        this.bodyHash = getBodyHash();
    }

    /** Get a cookie from the node.
    *
    * @param type the representation class
    * @return the cookie or <code>null</code>
    */
    public Node.Cookie getCookie (Class type) {
        Node.Cookie c = getCookieSet().getCookie(type);
        if (c != null) return c;
        if (ElementCookie.class.isAssignableFrom (type) ||
                FilterCookie.class.isAssignableFrom (type)) return null;
        if (bounds != null) {
            EditorSupport ed = bounds.getBegin().getEditorSupport();
            if (ed instanceof JavaEditor) {
                DataObject jdo = ((JavaEditor)ed).getJavaEntry().getDataObject();
                c = jdo.getCookie(type);
            }
        }
        return c;
    }

    /** Attaches to element */
    public void attachedToElement (Element element) {
        this.element = element;
    }

    /** Invokes the open action. */
    public void open() {
        PositionRef begin = (headerBounds != null) ? headerBounds.getBegin() :
                            ((bodyBounds != null) ? bodyBounds.getBegin() : bounds.getBegin());
        JavaEditor editor = (JavaEditor) begin.getEditorSupport();
        editor.openAt(begin).requestFocus();
    }

    /** Fires property change event.
    * @param name property name
    * @param o old value
    * @param n new value
    */
    protected final void firePropertyChange(String name, Object o, Object n) {
        if (support != null) {
            support.firePropertyChange (name, o, n);
        }
    }

    /** Adds property listener */
    public synchronized void addPropertyChangeListener (PropertyChangeListener l) {
        if (support == null) {
            synchronized (this) {
                // new test under synchronized block
                if (support == null) {
                    support = new PropertyChangeSupport (element);
                }
            }
        }
        support.addPropertyChangeListener (l);
    }

    /** Removes property listener */
    public void removePropertyChangeListener (PropertyChangeListener l) {
        if (support != null) {
            support.removePropertyChangeListener (l);
        }
    }

    abstract SourceElementImpl findSourceElementImpl();

    TextElement getTextElement() {
        TextElement textElement = null;
        if (textElementRef != null)
            textElement = (TextElement) textElementRef.get();
        if (textElement == null) {
            textElement = new TextElement(this);
            textElementRef = new WeakReference(textElement);
        }
        return textElement;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public void checkValid() throws SourceException {
        if (isValid()) {
            return;
        }
        throw new SourceException(Util.getString("EXC_ElementInvalid"));
    }

    // ================== Code generation ============================

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean enableLock) {
        locked = enableLock;
    }

    public void checkNotLocked() throws SourceException {
        if (!isLocked()) {
            checkValid();
            return;
        }
        throw new SourceException(Util.getString("EXC_ElementLocked"));
    }

    boolean isAtOffset(int offset) {
        if (bodyBounds == null) {
            return false;
        }
        return (bounds.getBegin().getOffset() <= offset) &&
               (bounds.getEnd().getOffset() > offset);
    }

    Element findElement(int offset) {
        return element;
    }

    /** Regenerates the javadoc in the source */

    /** Returns appropriate position for JavaDoc comment for the element.
      @return Position where the comment should start. If the element already
      has a comment, returns position of the comment's beginning.
    */
    PositionRef getJavaDocPosition() {
        if (docBounds != null) {
            return docBounds.getBegin();
        } else {
            return bounds.getBegin();
        }
    }

    void removeFromSource() throws SourceException {
        checkValid();
        SourceElementImpl.clearBounds(bounds);
    }
    void regenerateJavaDoc() throws SourceException {
        checkValid();
        CodeGenerator.regenerateJavaDoc(element, this);
    }

    /** Regenerates the header in the source */
    void regenerateHeader() throws SourceException {
        checkNotLocked();
        CodeGenerator.regenerateHeader(element, this);
    }

    /** Regenerates the whole element in the source.
    * Also updates the bounds of javadoc, header and body
    */
    void regenerate(Element element) throws SourceException {
        checkNotLocked();
        CodeGenerator.regenerateElement(element, this);
    }

    /** Creates the bounds for new element. The element type (class,
    *  method, ...) is given by the instance of the collection.
    * @param col The calling collection
    * @exception SourceException this implementation always throws
    *             (must be rewriten in ClassElementImpl and SourceElementImpl)
    */
    PositionBounds createBoundsFor(ElementsCollection col) throws SourceException {
        throw new SourceException();
    }
}


/*
 * Log
 *  28   Gandalf-post-FCS1.21.2.5    4/6/00   Svatopluk Dedic Extended messages for 
 *       SourceExceptions
 *  27   Gandalf-post-FCS1.21.2.4    3/30/00  Svatopluk Dedic Hash is computed only if
 *       document has been loaded
 *  26   Gandalf-post-FCS1.21.2.3    3/27/00  Svatopluk Dedic Fix for serialization 
 *       problems with CRC32
 *  25   Gandalf-post-FCS1.21.2.2    3/8/00   Svatopluk Dedic Invalid elements do not 
 *       allow modifications.
 *  24   Gandalf-post-FCS1.21.2.1    2/24/00  Svatopluk Dedic Minor changes
 *  23   Gandalf-post-FCS1.21.2.0    2/24/00  Ian Formanek    Post FCS changes
 *  22   src-jtulach1.21        10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems copyright in file comment
 *  21   src-jtulach1.20        9/13/99  Petr Hamernik   minor changes
 *  20   src-jtulach1.19        8/9/99   Ian Formanek    Generated Serial Version
 *       UID
 *  19   src-jtulach1.18        7/8/99   Petr Hamernik   changes reflecting 
 *       org.openide.src changes
 *  18   src-jtulach1.17        7/3/99   Petr Hamernik   SourceCookie.Editor - 
 *       1st version
 *  17   src-jtulach1.16        6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  16   src-jtulach1.15        6/2/99   Petr Hamernik   connections of java 
 *       sources
 *  15   src-jtulach1.14        5/24/99  Petr Hamernik   javadocimpl bugfix
 *  14   src-jtulach1.13        5/13/99  Jan Jancura     Do not delegate Element 
 *       & Filter Cookies
 *  13   src-jtulach1.12        5/10/99  Petr Hamernik   
 *  12   src-jtulach1.11        4/30/99  Petr Hamernik   
 *  11   src-jtulach1.10        4/21/99  Petr Hamernik   Java module updated
 *  10   src-jtulach1.9         4/7/99   Petr Hamernik   
 *  9    src-jtulach1.8         3/29/99  Petr Hamernik   
 *  8    src-jtulach1.7         3/29/99  Petr Hamernik   
 *  7    src-jtulach1.6         3/29/99  Ian Formanek    removed import of 
 *       modules.compiler
 *  6    src-jtulach1.5         3/18/99  Petr Hamernik   
 *  5    src-jtulach1.4         3/18/99  Petr Hamernik   
 *  4    src-jtulach1.3         3/10/99  Petr Hamernik   
 *  3    src-jtulach1.2         2/25/99  Petr Hamernik   
 *  2    src-jtulach1.1         2/17/99  Petr Hamernik   
 *  1    src-jtulach1.0         2/11/99  Petr Hamernik   
 * $
 */
