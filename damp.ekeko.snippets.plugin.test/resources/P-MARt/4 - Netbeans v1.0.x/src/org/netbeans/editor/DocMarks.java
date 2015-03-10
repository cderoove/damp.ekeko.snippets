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

import java.util.Enumeration;
import javax.swing.text.BadLocationException;

/**
* Document marks enable to store position and line information
* to simplify orientation in the document. They are stored
* in special structure so that all the main operations
* (insert mark, remove mark, insert into document, remove from document,
* getting mark's position, getting mark's line) are relatively cheap.
*
* @author Miloslav Metelka
* @version 1.00
*/

class DocMarks {
    /** How many leaves each node has in the tree */
    static final int LEAVES_PER_NODE = 16;
    /** Absolute count added in each of plane reallocation */
    static final int ABSOLUTE_REALLOC_CNT = 128;
    /** Relative realloc th - 1/4 of current array size will allocated */
    static final int RELATIVE_REALLOC_TH = 4;
    /** Maximum of unused marks. If there's more, they are garbage collected */
    static final int MAX_UNUSED_MARKS = 100;

    /** Some internal messages */
    static final String POS_LESS_ZERO = "Position must be >= 0"; // NOI18N
    static final String LINE_LESS_ZERO = "Line must be >= 0"; // NOI18N

    /** Mark at top of tree */
    TreeMark topMark;
    /** First mark of leaf plane. It's always the first and it's not removed */
    Mark startMark;
    /** Top plane */
    Plane topPlane;
    /** Leaf plane */
    Plane leafPlane;

    /** Unused plane. There can be only one plane unused. */
    Plane unusedPlane;
    /** Unused tree marks. They are concatenated in the list by <CODE>parent</CODE>.
    * They are reused by the next insert operation.
    */
    TreeMark unusedTreeMarks;
    /** Unused tree marks count. There is some max. count after which marks are
    * simply garbage collected.
    */
    int unusedTreeMarksCnt;

    /** Count of mark additions to the tree */
    int statMarksAdded;
    /** Count of full calling getOffset() resp. getOffsetRec() function */
    int statPosCalled;
    /** Count of full calling getLine() resp. getLineRec() function */
    int statLineCalled;


    /** Construct new marks */
    DocMarks() {

        startMark = new Mark() {

                        public int getOffset() {
                            return 0;
                        }

                        public int getLine() {
                            return 0;
                        }

                        protected void removeUpdateAction(int pos, int len) {
                            // can't be removed, so no action
                        }

                        public void remove() {
                            // can't be removed
                        }

                    };
        startMark.insertAfter = true;
        startMark.marks = this;
        startMark.valid = true;

        leafPlane = new Plane(this); // create leaf plane
        topPlane = new Plane(this, leafPlane);

        leafPlane.marks[0] = startMark; // always the first
        leafPlane.markCnt = 1;
        topPlane.marks[0] = new TreeMark();
        topPlane.marks[0].insertAfter = true;
        topPlane.markCnt = 1;
        leafPlane.marks[0].parent = topPlane.marks[0];
    }

    /** Get total mark count */
    public synchronized int getMarkCnt() {
        return leafPlane.markCnt;
    }

    /** Insert mark that which was previously created */
    public synchronized void insertMark(Mark mark, int pos, int line)
    throws BadLocationException, InvalidMarkException {
        insertMarkImpl(mark, pos, line);
    }

    /** Insert update of marks. This function must be called after insert to
    * update positions and line numbers of marks. The caller must determine
    * the line breaks in inserted text and pass it to this function.
    * @param pos position of insertion
    * @param len length of inserted data
    * @param linesInserted number of line breaks in the inserted part
    */
    public synchronized void insertUpdate(int pos, int len, int linesInserted) {
        if (len <= 0) {
            return;
        }
        int ind = getIndFromPos(pos, 0, true);
        if (ind < leafPlane.markCnt) {
            TreeMark m = leafPlane.marks[ind];
            while (m != null) {
                m.update(len, linesInserted);
                m = m.parent;
            }
        }
    }

    /** Remove update of marks. This function must be called after remove to
    * update positions and line numbers of marks. The caller must determine
    * the line breaks in removed text and pass it to this function. The function
    * operates in three steps:
    * 1. get the array of all marks in removal area and call 
    *   <CODE>removeUpdateAction()</CODE> in all of them.
    * 2. find all marks with <CODE>insertAfter</CODE> flag in removal area
    *   and remove them and immediatelly insert to the begining of removal
    *   area.
    * 3. track the removal area and shrink the distances between marks to zero.
    * @param pos position of removal
    * @param line line of removal
    * @param len length of removed data
    * @param linesRemoved number of line breaks in the removed part
    */
    public synchronized void removeUpdate(int pos, int line, int len, int linesRemoved) {
        if (len <= 0) {
            return;
        }

        TreeMark m = null;
        int ind1 = getIndFromPos(pos, line, false); // begining of cleared zone
        int posRest1 = leafPlane.tmpInt;
        int ind2 = getIndFromPos(pos + len, line, true); // end of zone

        if (ind1 == leafPlane.markCnt) { // remove after last mark
            return;
        }

        if (ind1 == ind2) { // No marks removed in this case
            m = leafPlane.marks[ind1];
            while (m != null) {
                m.update(-len, -linesRemoved);
                m = m.parent;
            }
        } else { // One or more marks removed
            // Copy marks into new array
            Mark marks[] = new Mark[ind2 - ind1];
            System.arraycopy(leafPlane.marks, ind1, marks, 0, marks.length);
            // Notify all marks in the area about remove
            for (int i = 0; i < marks.length; i++) {
                if (((Mark)marks[i]).isValid()) {
                    marks[i].removeUpdateAction(pos, len);
                }
            }

            // At first all marks with insertAfter must be put to the begining
            ind1 = getIndFromPos(pos, line, false); // refresh ind1
            posRest1 = leafPlane.tmpInt;
            int restLen = posRest1 + len;
            while (ind1 < leafPlane.markCnt) {
                m = leafPlane.marks[ind1];
                restLen -= m.relPos;
                if (restLen < 0) { // to cover insertAfter marks at pos + len
                    break;
                }
                if (m.insertAfter) { // Remove insertAfter marks
                    restLen += m.relPos; // will be removed so space for next one
                    try {
                        removeMarkImpl((Mark)m);
                        insertMarkImpl((Mark)m, pos, line);
                    } catch(BadLocationException e) {
                        if (Boolean.getBoolean("netbeans.debug.exceptions")) { // NOI18N
                            e.printStackTrace();
                        }
                    } catch(InvalidMarkException e) {
                        if (Boolean.getBoolean("netbeans.debug.exceptions")) { // NOI18N
                            e.printStackTrace();
                        }
                    }
                    // must correct restLen
                    restLen = pos + len - ((Mark)leafPlane.marks[ind1]).getOffsetRec(leafPlane);
                }
                ind1++;
            }

            // now modify the rest of marks (surely have insertAfter == false)
            ind1 = getIndFromPos(pos, line, false);
            posRest1 = leafPlane.tmpInt;
            //      int lineRest1 = line - ((Mark)leafPlane.marks[ind1 - 1]).getLineRec(leafPlane);
            int lineRest1 = topPlane.tmpInt; // get line rest
            int deltaPos; // delta by which mark pos should be decreased
            int deltaLine;

            while (ind1 < leafPlane.markCnt && len > 0) {
                m = leafPlane.marks[ind1];
                deltaPos = Math.min(m.relPos - posRest1, len);
                deltaLine = Math.min(m.relLine - lineRest1, linesRemoved);
                while (m != null) {
                    m.update(-deltaPos, -deltaLine);
                    m = m.parent;
                }
                len -= deltaPos;
                linesRemoved -= deltaLine;
                posRest1 = 0;
                lineRest1 = 0;
                ind1++;
            }
        }
    }

    /** Get the mark that is either start of document mark
    * or the first mark that is most on left of marks with same
    * position as parameter or, if no such mark exist, that is 
    * less than the parameter.
    * @param pos requested position
    */
    public synchronized Mark getLeftMark(int pos) {
        if (pos <= 0) {
            return startMark;
        }
        int ind = getIndFromPos(pos - 1, 0, false);
        if (ind >= leafPlane.markCnt
                || leafPlane.marks[ind].relPos - leafPlane.tmpInt > 1
           ) {
            // the distance of the found mark is more than 1 position
            // further from requested position (pos - 1) so we must
            // go one index back
            ind--;
        }
        return (Mark)leafPlane.marks[ind];
    }

    public synchronized Mark getLeftMark(int pos, Class markClass) {
        if (pos <= 0) {
            return null; // for this special case return null
        }
        int ind = getIndFromPos(pos - 1, 0, false);
        if (ind >= leafPlane.markCnt
                || leafPlane.marks[ind].relPos - leafPlane.tmpInt > 1
           ) {
            // the distance of the found mark is more than 1 position
            // further from requested position (pos - 1) so we must
            // go one index back
            ind--;
        }
        for (int i = ind; i >= 0; i--) {
            if (markClass.isInstance(leafPlane.marks[i])) {
                return (Mark)leafPlane.marks[i];
            }
        }
        return null;
    }

    /** Get mark that is right at given pos or null */
    public synchronized Mark getOffsetMark(int pos, Class markClass) {
        int ind = getIndFromPos(pos, 0, false);
        if (leafPlane.tmpInt > 0) { // no mark right at given pos
            return null;
        }
        ind--; // one position to the left to the correct mark
        while (ind >= 0) {
            if (markClass == null || markClass.isInstance(leafPlane.marks[ind])) {
                return (Mark)leafPlane.marks[ind];
            }
            if (leafPlane.marks[ind].relPos != 0) { // not on the same position as prev
                break;
            }
            ind--; // still on the same position
        }
        return null;
    }

    /** Render marks by some <CODE>Renderer</CODE>. It is the most
    * efficient way to handle especially multiple adjacent marks.
    * Rendering function is called in synchronized manner, so no one will
    * modify mark array while executing this function.
    */
    public synchronized void render(Renderer r) {
        r.marks = this;
        r.render();
        r.marks = null;
    }

    /** Gets the nearest lower position for specified line. This
    * method can be used when the only line information is available
    * and the position is needed (i.e. setting breakpoints, going to line
    * with error etc).
    * @param line line offset for which we want mark
    * @return mark with lower or equal line. Caution! When the caller gets
    *  the mark and it usually tries to get position of returned mark. However
    *  the mark can be removed meantime and call <CODE>getOffset()</CODE> will
    *  throw <CODE>InvalidMarkException</CODE>. In that case caller should
    *  call <CODE>getMarkFromLine()</CODE> again to get another mark and retry.
    */
    synchronized Mark getMarkFromLine(int line) {
        if (line < 0)
            return (Mark)leafPlane.marks[0]; // return start mark

        Plane plane = topPlane; // start from top plane
        if (line >= plane.marks[0].relLine) {
            return (Mark)(leafPlane.marks[leafPlane.markCnt - 1]);
        }

        int ind = 0; // index of mark in specific plane
        while ((plane = plane.child) != null) {
            ind = ind * LEAVES_PER_NODE;
            while (line >= plane.marks[ind].relLine) {
                line -= plane.marks[ind].relLine;
                ind++;
            }
        }
        return (Mark)(leafPlane.marks[ind - 1]); // return mark with lower line
    }

    /** Internal method for mark insertion */
    final void insertMarkImpl(Mark mark, int pos, int line)
    throws BadLocationException, InvalidMarkException {
        if(pos < 0)
            throw new BadLocationException(POS_LESS_ZERO, pos);
        if (line < 0)
            throw new BadLocationException(LINE_LESS_ZERO, line);
        if (mark.marks != null) // marks variable set
            throw new InvalidMarkException();
        if (mark.valid) // mark is already inserted in tree
            throw new InvalidMarkException();

        mark.marks = this; // stamp the mark is used by this class
        Plane plane = leafPlane; // start from leafPlane
        int ind = getIndFromPos(pos, line, mark.insertAfter);
        int posRest = plane.tmpInt; // index of previous
        //    int lineRest = line - ((Mark)plane.marks[ind - 1]).getLineRec(leafPlane);
        int lineRest = topPlane.tmpInt; // get line rest
        Plane parentPlane; // parent plane of active plane
        boolean lastLeafMark = (ind == plane.markCnt);
        boolean lastLeafInsertAfter; // insertAfter of last mark

        // now we have the insertion point in the leafPlane and we can insert
        plane.ensureCapacity(plane.markCnt + 1);
        if (!lastLeafMark) {
            System.arraycopy(plane.marks, ind, plane.marks, ind + 1, plane.markCnt - ind);
        }
        plane.marks[ind] = mark;
        plane.markCnt++;
        mark.init(posRest, lineRest);
        if (lastLeafMark) {
            lastLeafInsertAfter = mark.insertAfter;
        } else {
            plane.marks[ind + 1].update(-posRest, -lineRest);
            lastLeafInsertAfter = plane.marks[plane.markCnt - 1].insertAfter;
        }

        boolean markInserted = true; // mark inserted somewhere to current plane
        boolean parentMarkInserted; // mark inserted to parent plane
        int chunkSize = LEAVES_PER_NODE; // size of chunk

        // cycle through all planes except topPlane and update counts
        while (plane != topPlane) {
            parentPlane = plane.parent;
            // test need for allocation of new mark in parent plane
            parentMarkInserted = false; // suppose not adding mark to parent
            if (markInserted) {
                if (plane.markCnt % LEAVES_PER_NODE == 1) {
                    TreeMark addedMark = getFreshTreeMark();
                    if (parentPlane == topPlane) { // need to add new top plane
                        if (unusedPlane != null) {
                            unusedPlane.child = topPlane;
                            topPlane.parent = unusedPlane;
                            topPlane = unusedPlane;
                            topPlane.markCnt = 0;
                            unusedPlane = null;
                        } else {
                            topPlane = new Plane(this, topPlane);
                        }
                        TreeMark topM = getFreshTreeMark();
                        topPlane.marks[0] = topM;
                        topPlane.markCnt++;
                        topM.init(parentPlane.marks[0]);
                        parentPlane.marks[0].parent = topM;
                    }
                    parentPlane.ensureCapacity(parentPlane.markCnt + 1);
                    parentPlane.marks[parentPlane.markCnt] = addedMark;
                    parentPlane.markCnt++;
                    parentMarkInserted = true;
                }

                if (plane == leafPlane) { // need to correct inserted mark
                    mark.parent = parentPlane.marks[ind / LEAVES_PER_NODE];
                } else {
                    int lastInd = plane.markCnt - 1;
                    plane.marks[lastInd].parent = parentPlane.marks[lastInd / LEAVES_PER_NODE];
                }
            }

            if (lastLeafMark) {
                plane.marks[plane.markCnt - 1].parent.update(mark.relPos, mark.relLine);
            }

            int i = (ind / chunkSize) * chunkSize; // tracking index
            int chunkBound; // upper bound of current chunk
            boolean firstUpdate = true; // signals that the first chunk is being updated
            boolean fullChunk; // is the current chunk full or not

            while (i < leafPlane.markCnt) {
                chunkBound = i + chunkSize; // compute new chunk bound
                fullChunk = (leafPlane.markCnt > chunkBound); // full chunk (originally)
                parentPlane.marks[i / chunkSize].update( // update parent mark
                    (firstUpdate ? 0 : leafPlane.marks[i].relPos)
                    - (fullChunk ? leafPlane.marks[chunkBound].relPos : 0), // update relPos
                    + (firstUpdate ? 0 : leafPlane.marks[i].relLine)
                    - (fullChunk ? leafPlane.marks[chunkBound].relLine : 0), // update relLine
                    (fullChunk ? leafPlane.marks[chunkBound - 1].insertAfter
                     : lastLeafInsertAfter) // update insertAfter
                );
                // update parent of moved mark
                if (plane == leafPlane) {
                    plane.marks[i / (chunkSize / LEAVES_PER_NODE)].parent =
                        parentPlane.marks[i / chunkSize];
                }
                i = chunkBound; // go to the next chunk
                firstUpdate = false;
            }

            plane = parentPlane; // go to the parent plane
            markInserted = parentMarkInserted;
            chunkSize *= LEAVES_PER_NODE;
        }

        mark.valid = true; // mark is now valid
        statMarksAdded++;
    }

    /** Remove mark from tree. Mark is no longer valid and
    * its <CODE>valid</CODE> flag is set to false and its <CODE>marks</CODE>
    * variable is set to null.
    */
    void removeMarkImpl(Mark mark)
    throws InvalidMarkException {
        if (!mark.valid)
            throw new InvalidMarkException();
        if (mark.marks == null)
            throw new InvalidMarkException();

        Plane plane = leafPlane;
        Plane parentPlane;
        int ind = mark.getIndRec(leafPlane); // index of mark in leafPlane
        boolean lastLeafMark = (ind == --plane.markCnt); // update markCnt now
        boolean lastLeafInsertAfter; // insertAfter of last mark


        lastLeafInsertAfter = plane.marks[plane.markCnt
                                          - (lastLeafMark ? 1 : 0)].insertAfter;

        boolean markRemoved = true;
        boolean parentMarkRemoved;
        int chunkSize = LEAVES_PER_NODE;

        while (plane != topPlane) {
            parentPlane = plane.parent;

            int i = (ind / chunkSize) * chunkSize;
            int chunkBound;
            boolean firstUpdate = true;
            boolean fullChunk;

            while (i <= leafPlane.markCnt) {
                chunkBound = i + chunkSize;
                fullChunk = (leafPlane.markCnt >= chunkBound);
                parentPlane.marks[i / chunkSize].update(
                    (fullChunk ? leafPlane.marks[chunkBound].relPos : 0)
                    - (firstUpdate ? 0 : leafPlane.marks[i].relPos),
                    (fullChunk ? leafPlane.marks[chunkBound].relLine : 0)
                    - (firstUpdate ? 0 : leafPlane.marks[i].relLine),
                    (fullChunk ? leafPlane.marks[chunkBound].insertAfter
                     : lastLeafInsertAfter)
                );
                // update parent of moved mark
                if (plane == leafPlane && fullChunk) {
                    plane.marks[chunkBound / (chunkSize / LEAVES_PER_NODE)].parent =
                        parentPlane.marks[i / chunkSize];
                }
                i = chunkBound; // go to the next chunk
                firstUpdate = false;
            }

            if (lastLeafMark) {
                parentPlane.marks[ind / chunkSize].update(-mark.relPos, -mark.relLine);
            }

            parentMarkRemoved = false; // suppose not adding mark to parent
            if (markRemoved && plane.markCnt % LEAVES_PER_NODE == 0) {
                if (plane.markCnt == LEAVES_PER_NODE &&
                        parentPlane.parent == topPlane
                   ) { // need to remove current top plane
                    putFreshTreeMark(topPlane.marks[0]);
                    topPlane.markCnt = 0;
                    unusedPlane = topPlane;
                    topPlane = topPlane.child;
                    topPlane.parent = null;
                    topPlane.marks[0].parent = null; // took me 4 hours to discover
                }
                putFreshTreeMark(parentPlane.marks[--parentPlane.markCnt]);
                parentMarkRemoved = true;
            }

            plane = parentPlane; // go to the parent plane
            markRemoved = parentMarkRemoved;
            chunkSize *= LEAVES_PER_NODE;
        }

        if (!lastLeafMark) {
            leafPlane.marks[ind + 1].update(mark.relPos, mark.relLine);
            System.arraycopy(leafPlane.marks, ind + 1, leafPlane.marks, ind,
                             leafPlane.markCnt - ind); // physically  remove the mark from leafPlane
        }

        mark.valid = false; // mark is now invalid
        mark.marks = null; // marks reference removed

    }

    /** Get next greater mark's index from position and return
    * advance of given position and line against the position and line of
    * the [returned mark's index - 1] mark.
    * If there's a mark with the same position as pos, then
    * the index of the next mark after all the marks with position pos
    * will be returned and the leafPlane.tmpInt will be 0.
    * If there's a mark with the lower position only, then the next mark
    * with the greater position will be returned and the leafPlane.tmpInt
    * will be set to the difference from the requested position minus
    * the position of the lower mark.
    * @param pos position to find mark for
    * @param line line that should be also found
    * @param insertAfter if it's set to true then the index of the first
    *   mark with greater position or same position but having insertAfter set
    *   to true, is returned.
    * @return index of mark found and rest of position from the mark search
    * stored in <CODE>leafPlane.tmpInt</CODE> and rest of line search stored
    * in <CODE>topPlane.tmpInt</CODE>
    */
    final int getIndFromPos(int pos, int line, boolean insertAfter) {
        Plane plane = topPlane; // start from top plane
        // relPos in first mark in top plane means the whole range
        if (pos > plane.marks[0].relPos || (pos == plane.marks[0].relPos
                                            && (!insertAfter || (insertAfter && plane.marks[0].insertAfter)))
           ) {
            leafPlane.tmpInt = pos - plane.marks[0].relPos;
            topPlane.tmpInt = line - plane.marks[0].relLine;
            return leafPlane.markCnt;
        }

        int ind = 0; // index of mark in specific plane
        while ((plane = plane.child) != null) {
            ind = ind * LEAVES_PER_NODE;
            while (pos > plane.marks[ind].relPos ||
                    (pos == plane.marks[ind].relPos && (!insertAfter
                                                        || (insertAfter && plane.marks[ind].insertAfter)))
                  ) {
                pos -= plane.marks[ind].relPos;
                line -= plane.marks[ind].relLine;
                ind++;
            }
        }
        leafPlane.tmpInt = pos; // store rest of position
        topPlane.tmpInt = line; // store rest of line
        return ind; // return index
    }

    /** Get a free tree mark. It's either unused tree mark
    * stored in unusedTreeMarks or a new tree mark.
    */
    TreeMark getFreshTreeMark() {
        TreeMark m;
        if (unusedTreeMarks != null) {
            m = unusedTreeMarks;
            unusedTreeMarksCnt--;
            unusedTreeMarks = m.parent;
            m.parent = null;
        } else {
            m = new TreeMark();
        }
        return m;
    }

    /** Put a free tree mark. Either put it to list
    * of unused marks or let it be garbage collected.
    */
    void putFreshTreeMark(TreeMark m) {
        if (unusedTreeMarksCnt <= MAX_UNUSED_MARKS) {
            m.init(0, 0);
            m.insertAfter = false;
            m.parent = unusedTreeMarks;
            unusedTreeMarks = m;
            unusedTreeMarksCnt++;
        } // else let it be garbage collected
    }

    /** One plane in the hierarchy */
    static class Plane {
        /** Reference for faster getOffsetRec() accesses */
        DocMarks docMarks;
        /** Array of tree marks */
        TreeMark marks[];
        /** Size of the array */
        int markCnt;
        /** Parent plane (closer to tree top) */
        Plane parent;
        /** Child plane (closer to tree bottom) */
        Plane child;
        /** Temporary index for use in some functions */
        int tmpInt;

        /** Construct new empty plane */
        Plane(DocMarks docMarks, Plane child) {
            this.docMarks = docMarks;
            this.child = child;
            if (child != null) {
                child.parent = this;
            }
            marks = new TreeMark[ABSOLUTE_REALLOC_CNT];
        }

        /** Special constructor for creating leaf plane.
        * For leaf plane the mark array is real Mark array
        * so that <CODE>getMarks()</CODE> in renderer can make conversion.
        */
        Plane(DocMarks docMarks) {
            this.docMarks = docMarks;
            marks = new Mark[ABSOLUTE_REALLOC_CNT];
        }

        /** Test need for reallocation and if it's needed
        * reallocate the array.
        */
        void ensureCapacity(int reqCnt) {
            if (reqCnt > marks.length) { // need to realloc
                TreeMark newMarks[];
                int newCnt = marks.length + ABSOLUTE_REALLOC_CNT
                             + marks.length / RELATIVE_REALLOC_TH;
                newMarks = ((this == docMarks.leafPlane) ? new Mark[newCnt] : new TreeMark[newCnt]);
                System.arraycopy(marks, 0, newMarks, 0, markCnt);
                marks = newMarks;
            }
        }

        public String toString() {
            Plane plane = docMarks.leafPlane;
            int ind = 0;
            while (plane != null) {
                if (plane == this) {
                    if (plane == docMarks.leafPlane) {
                        return "LP"; // NOI18N
                    } else if (plane == docMarks.topPlane) {
                        return "TP"; // NOI18N
                    } else {
                        return "P[" + ind + "]"; // NOI18N
                    }
                }
                plane = plane.parent;
                ind++;
            }
            return "Corruption found - unknown plane"; // NOI18N
        }

    }


    /** Basic mark for making tree nodes. It's the class only for
    * internal purposes.
    */
    static class TreeMark {
        /** Relative position */
        int relPos;
        /** Relative Line number */
        int relLine;
        /** Parent mark in the tree. It's package private to ease
        * chaining of unused marks.
        */
        TreeMark parent;

        /** Flag describing if the mark should hold its
        * position when inserting right on it. If this flag
        * is true, the mark's position will stay the same.
        * If it's false the insert will move the mark on.
        * Bookmarks having this flag set to true must
        * occur before those having it set to false in the tree.
        */
        boolean insertAfter;

        /** Initialize various fields in tree mark */
        final void init(int relPos, int relLine) {
            this.relPos = relPos;
            this.relLine = relLine;
        }

        /** Initialize <CODE>relPos</CODE>, <CODE>relLine</CODE> and
        * <CODE>insertAfter</CODE> from the given mark.
        */
        final void init(TreeMark m) {
            relPos = m.relPos;
            relLine = m.relLine;
            insertAfter = m.insertAfter;
        }

        /** Update the position and line number of this mark.
        * This function is used mainly in mark insertion and deletion
        * to change both position and line number at once.
        * @param posDelta delta increase of <CODE>relPos</CODE>
        * @param lineDelta delta increase of <CODE>relLine</CODE>
        */
        final void update(int posDelta, int lineDelta) {
            relPos += posDelta;
            relLine += lineDelta;
        }

        /** Update the position and line number of this mark.
        * This function is used mainly in mark insertion and deletion
        * to change both position and line number at once.
        * @param posDelta delta increase of <CODE>relPos</CODE>
        * @param lineDelta delta increase of <CODE>relLine</CODE>
        * @param insertAfter setting for <CODE>insertAfter</CODE> in the mark
        */
        final void update(int posDelta, int lineDelta, boolean insertAfter) {
            relPos += posDelta;
            relLine += lineDelta;
            this.insertAfter = insertAfter;
        }

        /** Recursively get the position of this tree mark
        * to find the absolute position of the mark.
        */
        protected final int getOffsetRec(Plane plane) {
            if (plane == plane.docMarks.topPlane) {
                plane.tmpInt = 0;
                return 0;
            } else { // not top plane
                int pos = parent.getOffsetRec(plane.parent);
                int ind = plane.parent.tmpInt * LEAVES_PER_NODE;
                while (plane.marks[ind] != this) {
                    pos += plane.marks[ind].relPos;
                    ind++;
                }
                plane.tmpInt = ind;
                if (plane == plane.docMarks.leafPlane) {
                    pos += this.relPos;
                }
                return pos;
            }
        }

        /** Recursively get the line number of this tree mark
        * to find the absolute line number of the mark.
        */
        protected final int getLineRec(Plane plane) {
            if (plane == plane.docMarks.topPlane) {
                plane.tmpInt = 0;
                return 0;
            } else { // not top plane
                int line = parent.getLineRec(plane.parent);
                int ind = plane.parent.tmpInt * LEAVES_PER_NODE;

                while (plane.marks[ind] != this) {
                    line += plane.marks[ind].relLine;
                    ind++;
                }
                plane.tmpInt = ind;
                if (plane == plane.docMarks.leafPlane) {
                    line += this.relLine;
                }
                return line;
            }
        }

        /** Recursively get the pos and line number of this tree mark
        * to find the absolute pos and line number of the mark.
        * The line number will be stored in line[], but caller must
        * guarantee line[0] = 0.
        */
        protected final int getOffsetAndLineRec(Plane plane, int line[]) {
            if (plane == plane.docMarks.topPlane) {
                plane.tmpInt = 0;
                return 0;
            } else { // not top plane
                int pos = parent.getOffsetAndLineRec(plane.parent, line);
                int ind = plane.parent.tmpInt * LEAVES_PER_NODE;

                while (plane.marks[ind] != this) {
                    pos += plane.marks[ind].relPos;
                    line[0] += plane.marks[ind].relLine;
                    ind++;
                }
                plane.tmpInt = ind;
                if (plane == plane.docMarks.leafPlane) {
                    pos += this.relPos;
                    line[0] += this.relLine;
                }
                return pos;
            }
        }

        /** Recursively the index of the mark in leafPlane.
        * @param plane must be leafPlane initially
        */
        protected final int getIndRec(Plane plane) {
            if (plane == plane.docMarks.topPlane) {
                return 0;
            } else {
                int ind = parent.getIndRec(plane.parent) * LEAVES_PER_NODE;
                while (plane.marks[ind] != this) {
                    ind++;
                }
                return ind;
            }
        }

    }

    /** More efficient way of handling marks especially if there is a need
    * to work with more than one mark at the moment.
    */
    public static abstract class Renderer {

        DocMarks marks;

        /** Getter for marks */
        protected final DocMarks getMarks() {
            return marks;
        }

        /** Get array of all marks for document. This array
        * can be larger than actual mark count. Therefore it's needed
        * to retrieve actual mark count before using that array.
        */
        protected Mark[] getMarkArray() {
            return (Mark[])marks.leafPlane.marks;
        }

        /** Get total count of marks in mark array */
        protected int getMarkCnt() {
            return marks.leafPlane.markCnt;
        }

        /** Get index of given mark in array of marks returned by
        * <CODE>getMarkArray()</CODE>.
        */
        protected int getMarkIndex(Mark mark) {
            return mark.getIndRec(marks.leafPlane);
        }

        /** Get the relative distance of mark to previous mark
        * in mark array.
        */
        protected int getRelPos(Mark mark) {
            return mark.relPos;
        }

        /** Get relative line distance of mark to previous mark
        * in mark array.
        */
        protected int getRelLine(Mark mark) {
            return mark.relLine;
        }

        /** Rendering function of this mark renderer. It is used
        * to make the task this renderer is intended to.
        */
        protected abstract void render();

    }

    /** Get info about <CODE>DocMarks</CODE>. */
    public String toString() {
        String ret = "getMarkCnt()=" + getMarkCnt() + ", statMarksAdded=" + statMarksAdded // NOI18N
                     + ", statPosCalled=" + statPosCalled + ", statLineCalled=" + statLineCalled; // NOI18N
        return ret;
    }

    /** Dump contents of planes. All the marks and planes are listed to system output. */
    public String planesToString(Class markClasses[], char markChars[]) {
        StringBuffer sb = new StringBuffer();
        Plane plane = leafPlane;
        sb.append("PLANES DUMP:\n"); // NOI18N
        while (plane != null) {
            sb.append(plane + ": child=" + plane.child + ", parent=" + plane.parent); // NOI18N
            sb.append('\n');
            plane = plane.parent;
        }
        plane = leafPlane;
        sb.append("\ni\\P\tAbsPos\tAbsLine\t\n"); // NOI18N
        while(plane != null) {
            sb.append(plane + ": " + plane.markCnt + " marks\t"); // NOI18N
            plane = plane.parent;
        }
        sb.append('\n');
        int sumPos = 0, sumLine = 0;
        for (int i = 0; i < leafPlane.markCnt; i++) {
            sumPos += leafPlane.marks[i].relPos;
            sumLine += leafPlane.marks[i].relLine;
            char markChar = '?';
            if (markClasses != null) {
                for (int j = 0; j < markClasses.length; j++) {
                    if (markClasses[j].isInstance(leafPlane.marks[i])) {
                        markChar = markChars[j];
                        break;
                    }
                }
            }
            sb.append(markChar + "["+i+"]\t" + sumPos + "\t" + sumLine + "\t"); // NOI18N
            plane = leafPlane;
            int div = 1;
            while(i % div == 0) { // next plane should be disp'd
                int planeInd;
                planeInd = i / div; // index in plane
                if (planeInd < plane.markCnt) {
                    boolean insertAfter = plane.marks[planeInd].insertAfter;
                    sb.append(((insertAfter) ? "P=" : "p=") // NOI18N
                              + plane.marks[planeInd].relPos
                              + "\t" + ((insertAfter) ? "L=" : "l=") // NOI18N
                              + plane.marks[planeInd].relLine + "\t"); // NOI18N
                }
                div *= LEAVES_PER_NODE;
                plane = plane.parent; // go to parent plane
                if (plane == null)
                    break;
            }
            sb.append('\n');
        }

        return sb.toString();
    }


}

/*
 * Log
 *  15   Gandalf-post-FCS1.13.1.0    3/8/00   Miloslav Metelka 
 *  14   Gandalf   1.13        1/13/00  Miloslav Metelka 
 *  13   Gandalf   1.12        1/10/00  Miloslav Metelka 
 *  12   Gandalf   1.11        11/14/99 Miloslav Metelka 
 *  11   Gandalf   1.10        10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  10   Gandalf   1.9         10/10/99 Miloslav Metelka 
 *  9    Gandalf   1.8         9/16/99  Miloslav Metelka 
 *  8    Gandalf   1.7         9/15/99  Miloslav Metelka 
 *  7    Gandalf   1.6         5/13/99  Miloslav Metelka 
 *  6    Gandalf   1.5         5/10/99  Miloslav Metelka fix - line elem. mark
 *  5    Gandalf   1.4         4/23/99  Miloslav Metelka Undo added and internal 
 *       improvements
 *  4    Gandalf   1.3         4/8/99   Miloslav Metelka 
 *  3    Gandalf   1.2         3/23/99  Miloslav Metelka 
 *  2    Gandalf   1.1         3/18/99  Miloslav Metelka 
 *  1    Gandalf   1.0         2/3/99   Miloslav Metelka 
 * $
 */

