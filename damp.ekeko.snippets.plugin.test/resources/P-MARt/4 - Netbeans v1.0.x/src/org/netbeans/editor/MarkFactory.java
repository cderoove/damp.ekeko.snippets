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

package org.netbeans.editor;

import java.lang.ref.WeakReference;

/**
* Various marks are located here
*
* @author Miloslav Metelka
* @version 1.00
*/

public class MarkFactory {

    private MarkFactory() {
        // no instantiation
    }

    /** Syntax mark holds info about scan state of syntax scanner.
    * This helps in redraws because reparsing after insert/delete is done
    * only from nearest left syntax mark. Moreover rescaning is done only
    * until there are marks with different scan state. As soon as mark
    * is found with same parsing info as rescanning scanner has, parsing
    * ends.
    */
    public static class SyntaxMark extends Mark {

        /** Syntax mark state info */
        private Syntax.StateInfo stateInfo;

        /** Get state info of this mark */
        public Syntax.StateInfo getStateInfo() {
            return stateInfo;
        }

        public void updateStateInfo(Syntax syntax) {
            if (stateInfo == null) {
                stateInfo = syntax.createStateInfo();
            }
            syntax.storeState(stateInfo);
        }

        /** When removal occurs */
        protected void removeUpdateAction(int pos, int len) {
            try {
                remove();
            } catch (InvalidMarkException e) {
                // shouldn't happen
            }
        }

    }

    /** Mark that can have its position updated by where it's located */
    public static class ContextMark extends Mark {

        /** Stay at all times at the begining of the line */
        boolean stayBOL;

        public ContextMark(boolean stayBOL) {
            this(false, stayBOL);
        }

        public ContextMark(boolean insertAfter, boolean stayBOL) {
            super(insertAfter);
            this.stayBOL = stayBOL;
        }

    }

    /** Activation mark for particular layer. When layer is not active
    * its updateContext() method is not called.
    */
    public static class DrawMark extends ContextMark {

        /** Activation flag means either activate layer or deactivate it */
        protected boolean activateLayer;

        /** Reference to draw layer this mark belogns to */
        String layerName;

        /** Reference to extended UI if this draw mark is info-specific or
        * null if it's document-wide.
        */
        WeakReference extUIRef;

        public DrawMark(String layerName, ExtUI extUI) {
            super(false);
            this.layerName = layerName;
            setExtUI(extUI);
        }

        public boolean isDocumentMark() {
            return (extUIRef == null);
        }

        public ExtUI getExtUI() {
            if (extUIRef != null) {
                return (ExtUI)extUIRef.get();
            }
            return null;
        }

        public void setExtUI(ExtUI extUI) {
            this.extUIRef = (extUI != null) ? new WeakReference(extUI) : null;
        }

        public boolean isValid() {
            return !(extUIRef != null && extUIRef.get() == null);
        }

        public void setActivateLayer(boolean activateLayer) {
            this.activateLayer = activateLayer;
        }

        public boolean getActivateLayer() {
            return activateLayer;
        }

        public boolean removeInvalid() {
            if (!isValid()) {
                try {
                    this.remove();
                } catch (InvalidMarkException e) {
                }
                return true; // invalid and removed
            }
            return false; // valid
        }

        public String toString() {
            try {
                return "pos=" + getOffset() + ", line=" + getLine(); // NOI18N
            } catch (InvalidMarkException e) {
                return "mark not valid"; // NOI18N
            }
        }

    }

    /** Support for draw marks chained in double linked list */
    public static class ChainDrawMark extends DrawMark {

        /** Next mark in chain */
        protected ChainDrawMark next;

        /** Previous mark in chain */
        protected ChainDrawMark prev;

        public ChainDrawMark(String layerName, ExtUI extUI) {
            super(layerName, extUI);
        }

        public final ChainDrawMark getNext() {
            return next;
        }

        public final void setNext(ChainDrawMark mark) {
            next = mark;
        }

        /** Set next mark in chain */
        public void setNextChain(ChainDrawMark mark) {
            this.next = mark;
            if (mark != null) {
                mark.prev = this;
            }
        }

        public final ChainDrawMark getPrev() {
            return prev;
        }

        public final void setPrev(ChainDrawMark mark) {
            prev = mark;
        }

        /** Set previous mark in chain */
        public void setPrevChain(ChainDrawMark mark) {
            this.prev = mark;
            if (mark != null) {
                mark.next = this;
            }
        }

        /** Insert mark before this one in chain
        * @return inserted mark
        */
        public ChainDrawMark insertChain(ChainDrawMark mark) {
            ChainDrawMark thisPrev = this.prev;
            mark.prev = thisPrev;
            mark.next = this;
            if (thisPrev != null) {
                thisPrev.next = mark;
            }
            this.prev = mark;
            return mark;
        }

        /** Remove this mark from the chain
        * @return next chain member or null for end of chain
        */
        public ChainDrawMark removeChain() {
            ChainDrawMark thisNext = this.next;
            ChainDrawMark thisPrev = this.prev;
            if (thisPrev != null) { // not the first
                thisPrev.next = thisNext;
                this.prev = null;
            }
            if (thisNext != null) { // not the last
                thisNext.prev = thisPrev;
                this.next = null;
            }
            try {
                this.remove(); // remove the mark from DocMarks
            } catch (InvalidMarkException e) {
                // already removed
            }
            return thisNext;
        }

        public String toStringChain() {
            return toString() + (next != null ? "\n" + next.toStringChain() : ""); // NOI18N
        }

        public String toString() {
            return super.toString() + ", " // NOI18N
                   + ((prev != null) ? ((next != null) ? "chain member" // NOI18N
                            : "last member") : ((next != null) ? "first member" // NOI18N
                                                            : "standalone member")); // NOI18N
        }

    }

    /** Special mark for caret. Its signature is used in Analyzer not to move
    * this mark when initial read is performed.
    */
    static class CaretMark extends DrawMark {

        CaretMark() {
            super(DrawLayerFactory.CARET_LAYER_NAME, null);
        }

    }

    /** Mark for creating line elements */
    static class LineMark extends Mark {

        /** Weak reference to line element */
        WeakReference lineElemRef;

        public LineMark() {
            super(true);
        }

    }

}

/*
 * Log
 *  12   Gandalf-post-FCS1.10.1.0    3/8/00   Miloslav Metelka 
 *  11   Gandalf   1.10        1/13/00  Miloslav Metelka 
 *  10   Gandalf   1.9         12/28/99 Miloslav Metelka 
 *  9    Gandalf   1.8         11/8/99  Miloslav Metelka 
 *  8    Gandalf   1.7         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  7    Gandalf   1.6         10/10/99 Miloslav Metelka 
 *  6    Gandalf   1.5         7/29/99  Miloslav Metelka 
 *  5    Gandalf   1.4         7/20/99  Miloslav Metelka 
 *  4    Gandalf   1.3         5/5/99   Miloslav Metelka 
 *  3    Gandalf   1.2         4/8/99   Miloslav Metelka 
 *  2    Gandalf   1.1         3/27/99  Miloslav Metelka 
 *  1    Gandalf   1.0         3/23/99  Miloslav Metelka 
 * $
 */

