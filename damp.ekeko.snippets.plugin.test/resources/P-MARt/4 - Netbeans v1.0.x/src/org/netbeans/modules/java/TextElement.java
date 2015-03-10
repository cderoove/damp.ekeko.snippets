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

import javax.swing.text.AttributeSet;
import javax.swing.text.Document;
import javax.swing.text.Position;

import org.openide.src.InitializerElement;
import org.openide.src.MemberElement;
import org.openide.src.ClassElement;
import org.openide.text.PositionRef;

/** Implementation of swing.text.Element for source Elements hierarchy.
 *
 * @author Petr Hamernik
 */
class TextElement extends Object implements javax.swing.text.Element {

    /** Element implementation which this text element belongs to */
    ElementImpl element;

    /** Creates new TextElement */
    public TextElement(ElementImpl element) {
        this.element = element;
    }

    /**  Fetches the document associated with this element.
     *
     * @return the document
     */
    public Document getDocument() {
        if (element instanceof SourceElementImpl) {
            return ((SourceElementImpl)element).getJavaDataObject().getJavaEditor().getDocument();
        }
        else {
            return element.bounds.getBegin().getEditorSupport().getDocument();
        }
    }

    /**  Fetches the parent element.  If the element is a root level
     * element returns null.
     *
     * @return the parent element
     */
    public javax.swing.text.Element getParentElement() {
        ClassElement clazz = null;
        if (element instanceof InitializerElementImpl)
            clazz = ((InitializerElement)element.element).getDeclaringClass();
        if (element instanceof MemberElementImpl)
            clazz = ((MemberElement)element.element).getDeclaringClass();

        return (clazz == null) ? null : ((ElementImpl)clazz.getCookie(ElementImpl.class)).getTextElement();
    }

    /**  Fetches the name of the element.  If the element is used to
     * represent some type of structure, this would be the type
     * name.
     * For member elements returns name, for initializer returns "<init>" and
     * for the source element returns "<root>".
     *
     * @return the element name
     */
    public String getName() {
        if (element instanceof MemberElementImpl)
            return ((MemberElementImpl)element).getName().getFullName();
        else if (element instanceof InitializerElementImpl)
            return "<init>"; // NOI18N
        else
            return "<root>"; // NOI18N
    }

    /**  Fetches the collection of attributes this element contains.
     *
     * @return the attributes for the element
     */
    public AttributeSet getAttributes() {
        return null;
    }

    /**  Fetches the offset from the beginning of the document
     * that this element begins at.  If this element has
     * children, this will be the offset of the first child.
     *
     * @return the starting offset >= 0
     */
    public int getStartOffset() {
        if (element instanceof SourceElementImpl) {
            return 0;
        }
        else {
            return element.bounds.getBegin().getOffset();
        }
    }

    /**  Fetches the offset from the beginning of the document
     * that this element ends at.  If this element has
     * children, this will be the end offset of the last child.
     *
     * @return the ending offset >= 0
     */
    public int getEndOffset() {
        if (element instanceof SourceElementImpl) {
            return getDocument().getLength();
        }
        else {
            return element.bounds.getEnd().getOffset();
        }
    }

    /**  Gets the child element index closest to the given offset.
     * The offset is specified relative to the begining of the
     * document.
     *
     * @param offset the specified offset >= 0
     * @return the element index >= 0
     */
    public int getElementIndex(int offset) {
        //PENDING
        return 0;
    }

    /**  Gets the number of child elements contained by this element.
     * If this element is a leaf, a count of zero is returned.
     *
     * @return the number of child elements >= 0
     */
    public int getElementCount() {
        int count = 0;

        if (element instanceof ClassElementImpl) {
            ClassElementImpl c = (ClassElementImpl) element;
            if (c.initializers != null) count = c.initializers.size();
            if (c.constructors != null) count += c.constructors.size();
            if (c.methods != null) count += c.methods.size();
            if (c.fields != null) count += c.fields.size();
            if (c.classes != null) count += c.classes.size();
        }
        else if (element instanceof SourceElementImpl) {
            count = ((SourceElementImpl)element).getClasses().length;
        }

        return count;
    }

    /**  Fetches the child element at the given index.
     *
     * @param index the specified index >= 0
     * @return the child element
     */
    public javax.swing.text.Element getElement(int index) {
        org.openide.src.Element retElement = null;

        if (element instanceof ClassElementImpl) {
            ClassElementImpl c = (ClassElementImpl) element;
            int count = 0;
            for (int i = 0; i <= 4; i++) {
                ElementsCollection col = null;
                switch (i) {
                case 0: col = c.fields; break;
                case 1: col = c.initializers; break;
                case 2: col = c.constructors; break;
                case 3: col = c.methods; break;
                case 4: col = c.classes; break;
                }
                if (col != null) {
                    int size = col.size();
                    if (count + size > index) {
                        retElement = (org.openide.src.Element)col.toArray()[index - count];
                        break;
                    }
                    count += size;
                }
            }
        }
        else if (element instanceof SourceElementImpl) {
            retElement = ((SourceElementImpl)element).getClasses()[index];
        }

        if (retElement == null)
            throw new IndexOutOfBoundsException();
        else
            return ((ElementImpl)retElement.getCookie(ElementImpl.class)).getTextElement();
    }

    /**  Is this element a leaf element?
     *
     * @return true if a leaf element else false
     */
    public boolean isLeaf() {
        return !((element instanceof ClassElementImpl) || (element instanceof SourceElementImpl));
    }
}

/*
 * Log
 *  7    Gandalf   1.6         1/12/00  Petr Hamernik   i18n: perl script used (
 *       //NOI18N comments added )
 *  6    Gandalf   1.5         11/30/99 Petr Hamernik   NullPointerException 
 *       fixed - when TextElement represents SourceElement
 *  5    Gandalf   1.4         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  4    Gandalf   1.3         9/24/99  Petr Hamernik   jlint change
 *  3    Gandalf   1.2         7/23/99  Petr Hamernik   not public
 *  2    Gandalf   1.1         7/19/99  Petr Hamernik   resting implementation 
 *       added
 *  1    Gandalf   1.0         7/3/99   Petr Hamernik   
 * $
 */
