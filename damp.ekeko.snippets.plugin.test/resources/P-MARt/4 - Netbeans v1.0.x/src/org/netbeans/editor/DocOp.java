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

import java.util.ArrayList;
import javax.swing.text.BadLocationException;
import javax.swing.text.AbstractDocument;
import javax.swing.text.Position;
import javax.swing.text.Segment;
import javax.swing.event.DocumentEvent;
import javax.swing.undo.UndoableEdit;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.CannotRedoException;

/**
* Document operations. This class enhances basic
* marks operations by interaction with the data of the document.
* All mark operations should go through this class.
* This class implements <tt>AbstractDocument.Content</tt> interface.
* BOL means Begin of Line position.
* EOL means End of Line position.
*
* @author Miloslav Metelka
* @version 1.00
*/

class DocOp implements AbstractDocument.Content {

    /** Number of line cache entries */
    private static final int CACHE_LEN = 5;

    private static final String WRONG_POSITION = "Wrong position "; // NOI18N

    private static final String DOC_LEN = ". Document length is "; // NOI18N

    /** Default mark distance for inserting to the document.
    * If the insert is made then the distance between nearest
    * marks around insertion point is checked and if it's greater
    * than the max mark distance then another mark(s) are inserted.
    */
    private int MARK_DISTANCE;

    /** Maximum mark distance. When there is an insertion done in document
    * and the distance between marks get greater than this value, another
    * mark will be inserted.
    */
    private int MAX_MARK_DISTANCE;

    /** Minimum mark distance for removals. When there is a removal done
    * in document and it makes the marks to get closer than this value, then
    * the second mark will be removed.
    */
    private int MIN_MARK_DISTANCE;

    /** Size of one batch for updating syntax marks */
    private int SYNTAX_UPDATE_BATCH_SIZE;

    /** Document cache */
    private DocCache cache;

    /** Document cache support */
    private DocCacheSupport cacheSupport;

    /** Document marks handling */
    private DocMarks marks;

    /** Document */
    private BaseDocument doc;

    /** Document len */
    private int docLen;

    /** End of current line forward finder */
    private FinderFactory.EOLFwdFinder eolFwdFinder;

    /** Current line begining backward finder */
    private FinderFactory.BOLBwdFinder bolBwdFinder;

    /** Find both BOL and EOL one or more lines forward in text */
    private FinderFactory.BEOLLineFwdFinder beolLineFwdFinder;

    /** Find both BOL and EOL when position is known */
    private FinderFactory.BEOLPosFwdFinder beolPosFwdFinder;

    /** Finder for visual x-coord to position conversion */
    private FinderFactory.VisColPosFwdFinder visColPosFwdFinder;

    /** Finder for position to x-coord conversion */
    private FinderFactory.PosVisColFwdFinder posVisColFwdFinder;

    /** Line and column information cache */
    private CacheEntry lineCache[];

    /** End of document mark. This mark should speed up getting the line number
    * of the end of document so that the cache fragment doesn't have to move.
    */
    private Mark endMark;

    /** Mark that is inserted at the end of line (better said after the end
    * of line) where the insertion currently
    * occurs. The mark is possibly moved and/or updated before
    * the insertion/removal occurs. Then after the insertion/removal is done
    * mark syntax state is checked again. If it's the same as before it's not
    * necessary to paint the next line after insertion. If it's not the same
    * the painting must continue till the syntax mark with matching state.
    */
    MarkFactory.SyntaxMark eolMark;

    /** Renderer for doing syntax mark updates after insertion/removal. */
    private SyntaxUpdateRenderer suRenderer;

    /** First syntax mark that is laying in the left direction
    * from the update point.
    */
    private MarkFactory.SyntaxMark leftUpdateMark;

    /** Array holding returned line number */
    private int[] tmpLine = new int[1];

    /** Statistics */
    int statCacheHit;
    int statCacheMiss;

    /** Construct new document marks operations.
    * Since this class uses both cache and marks
    * both these classes must be already created in document.
    */
    DocOp() {
        cacheSupport = new MemCacheSupport();
        cache = new DocCache(cacheSupport, 2048, true); // !!!
        marks = new DocMarks();

        // initialize cache
        lineCache = new CacheEntry[CACHE_LEN];
        for (int i = 0; i < CACHE_LEN; i++) {
            lineCache[i] = new CacheEntry();
        }

        // create necessary finders
        bolBwdFinder = new FinderFactory.BOLBwdFinder();
        eolFwdFinder = new FinderFactory.EOLFwdFinder();
        beolLineFwdFinder = new FinderFactory.BEOLLineFwdFinder();
        beolPosFwdFinder = new FinderFactory.BEOLPosFwdFinder();
        visColPosFwdFinder = new FinderFactory.VisColPosFwdFinder();
        posVisColFwdFinder = new FinderFactory.PosVisColFwdFinder();

        // init endMark and eolMark
        try {
            endMark = insertMark(docLen, false);
            eolMark = new MarkFactory.SyntaxMark() {
                          protected void removeUpdateAction(int pos, int len) {
                              // prevent default removing of this mark
                          }
                      };
            insertMark(eolMark, docLen);
        } catch (BadLocationException e) {
            if (Boolean.getBoolean("netbeans.debug.exceptions")) { // NOI18N
                e.printStackTrace();
            }
        } catch (InvalidMarkException e) {
            if (Boolean.getBoolean("netbeans.debug.exceptions")) { // NOI18N
                e.printStackTrace();
            }
        }

        // create shared syntax update renderer
        suRenderer = new SyntaxUpdateRenderer();
    }

    void setDocument(BaseDocument doc) {
        this.doc = doc;
        MARK_DISTANCE = ((Integer)doc.getProperty(
                             Settings.MARK_DISTANCE)).intValue();
        MAX_MARK_DISTANCE = ((Integer)doc.getProperty(
                                 Settings.MAX_MARK_DISTANCE)).intValue();
        MIN_MARK_DISTANCE = ((Integer)doc.getProperty(
                                 Settings.MIN_MARK_DISTANCE)).intValue();
        SYNTAX_UPDATE_BATCH_SIZE = ((Integer)doc.getProperty(
                                        Settings.SYNTAX_UPDATE_BATCH_SIZE)).intValue();

    }

    public synchronized Position createPosition(int offset) throws BadLocationException {
        boolean insertAfter = (offset == 0); // support AbstractDocument keep marks at position 0 behavior
        if (offset == docLen + 1) { // support AbstractDocument's content initial "\n" behavior
            offset = docLen;
        }

        return new BasePosition(this, offset,
                                insertAfter ? Position.Bias.Backward : Position.Bias.Forward);
    }

    public synchronized Position createPosition(int offset, Position.Bias bias)
    throws BadLocationException {
        return new BasePosition(this, offset, bias);
    }

    public synchronized int length() {
        return docLen;
    }

    public String getString(int where, int len) throws BadLocationException {
        return new String(getChars(where, len));
    }

    public void getChars(int where, int len, Segment txt)
    throws BadLocationException {
        txt.array = getChars(where, len);
        txt.offset = 0;
        txt.count = len;
    }

    /** Retrieve the characters from the document cache. */
    synchronized char[] getChars(int pos, int len) throws BadLocationException {
        return cache.read(pos, len, null); // no cache fragment optimization yet
    }

    /** Get the characters into the given buffer. The buffer must contain
    * enough space for the requested data.
    */
    synchronized void getChars(int pos, char ret[], int offset, int len)
    throws BadLocationException {
        cache.read(pos, ret, offset, len, null);
    }

    public UndoableEdit insertString(int offset, String text) throws BadLocationException {
        ModifyUndoEdit undoEdit = new ModifyUndoEdit(false, offset, text);
        insertEdit(undoEdit);
        return undoEdit;
    }

    public UndoableEdit insert(int offset, char[] chars) throws BadLocationException {
        ModifyUndoEdit undoEdit = new ModifyUndoEdit(false, offset, chars);
        insertEdit(undoEdit);
        return undoEdit;
    }

    synchronized void insertEdit(ModifyUndoEdit undoEdit) throws BadLocationException {
        int offset = undoEdit.getOffset();
        checkEOLMark(offset);
        if (undoEdit.isTextValid()) {
            cache.insertString(offset, undoEdit.getText(), null);
        } else { // chars buffer valid
            cache.insert(offset, undoEdit.getChars(), null);
        }
        insertUpdate(undoEdit); // always done to update line cache
    }

    public UndoableEdit remove(int offset, int len) throws BadLocationException {
        ModifyUndoEdit undoEdit = new ModifyUndoEdit(true, offset, getChars(offset, len));
        removeEdit(undoEdit);
        return undoEdit;
    }

    synchronized void removeEdit(ModifyUndoEdit undoEdit) throws BadLocationException {
        checkEOLMark(undoEdit.getOffset());
        cache.remove(undoEdit.getOffset(), undoEdit.getLength(), null); // no cache fragment optimization yet
        removeUpdate(undoEdit);
    }

    synchronized int find(Finder finder, int startPos, int limitPos) throws BadLocationException {
        return cache.find(finder, startPos, limitPos, null);
    }

    /** Insert new mark at specified position. This function
    * finds the appropriate line number.
    */
    synchronized void insertMark(Mark mark, int pos)
    throws BadLocationException, InvalidMarkException {
        if (pos < 0 || pos > docLen) {
            throw new BadLocationException(WRONG_POSITION + pos + DOC_LEN + docLen, pos);
        }
        marks.insertMark(mark, pos, getLineImpl(pos));
    }

    /** Insert mark when knowing even the line offset.
    * This method is used solely by <tt>Analyzer</tt> when
    * document is read.
    */
    void insertMark(Mark mark, int pos, int line)
    throws BadLocationException, InvalidMarkException {
        marks.insertMark(mark, pos, line);
    }

    /** Write directly to the cache-support.
    * This method is used solely by <tt>Analyzer</tt> when
    * document is read.
    */
    void directCacheWrite(int pos, char cache[], int offset, int len)
    throws BadLocationException {
        cacheSupport.write(pos, cache, offset, len);
    }

    /** Initialize the default fragment of the cache by the given data.
    * This method is used solely by <tt>Analyzer</tt> when
    * document is read.
    */
    void initCacheContent(char initCache[], int offset, int cacheLen) {
        cache.initCacheContent(initCache, offset, cacheLen);
    }

    /** Insert new mark at specified position. The function
    * finds appropriate line number.
    */
    Mark insertMark(int pos, boolean insertAfter)
    throws BadLocationException {
        Mark mark = new Mark(insertAfter);
        try {
            insertMark(mark, pos);
        } catch(InvalidMarkException e) {
            if (Boolean.getBoolean("netbeans.debug.exceptions")) { // NOI18N
                e.printStackTrace();
            }
        }
        return mark;
    }

    /** Moves the mark to different position */
    synchronized void moveMark(Mark mark, int newPos)
    throws BadLocationException, InvalidMarkException {
        if (newPos < 0 || newPos > docLen) {
            throw new BadLocationException(WRONG_POSITION + newPos + DOC_LEN + docLen, newPos);
        }
        mark.remove();
        marks.insertMark(mark, newPos, getLineImpl(newPos));
    }

    synchronized Mark getOffsetMark(int pos, Class markClass) {
        return marks.getOffsetMark(pos, markClass);
    }

    synchronized void renderMarks(DocMarks.Renderer r) {
        marks.render(r);
    }

    /** Get begin of line from position on that line
    * @param position where the search begins
    * @return position of begin of the same line
    */
    synchronized int getBOL(int pos) throws BadLocationException {
        return getBOLImpl(pos);
    }

    /** Is the position at the begining of the line? */
    synchronized boolean isBOL(int pos) throws BadLocationException {
        return (pos == getBOLImpl(pos));
    }

    /** Get end of line position from position on that line. */
    synchronized int getEOL(int pos) throws BadLocationException {
        return getEOLImpl(pos);
    }

    /** Get end of line after the new-line position from position on that line.
    * This is in fact the begining of the next line except the last line.
    */
    synchronized int getEOLNL(int pos) throws BadLocationException {
        int eol = getEOLImpl(pos);
        if (eol < docLen) {
            return eol++;
        }
        return eol;
    }

    /** Is the position at the end of the line? */
    synchronized boolean isEOL(int pos) throws BadLocationException {
        return (pos == getEOLImpl(pos));
    }

    /** Get the begining position of specified line
    * @param line line offset for which the BOL should be determined
    * @return position of the begining of line or -1 if line is invalid
    */
    synchronized int getBOLFromLine(int line) {
        return getBOLFromLineImpl(line);
    }

    /** Get end of line position from specified line */
    synchronized int getEOLFromLine(int line) {
        int pos = getBOLFromLineImpl(line);
        if (pos < 0) {
            return 0;
        }
        try {
            return getEOLImpl(pos);
        } catch (BadLocationException e) {
            if (Boolean.getBoolean("netbeans.debug.exceptions")) { // NOI18N
                e.printStackTrace();
            }
            return -1;
        }
    }

    /** Advance given position n-lines forward/backward and return BOL. */
    synchronized int getBOLRelLine(int pos, int relLine)
    throws BadLocationException {
        int line = getLineImpl(pos);
        line += relLine;
        return getBOLFromLineImpl(line);
    }

    /** Advance given position n-lines forward/backward and return BOL. */
    synchronized int getEOLRelLine(int pos, int relLine)
    throws BadLocationException {
        int line = getLineImpl(pos);
        line += relLine;
        pos = getBOLFromLineImpl(line);
        if (pos < 0) {
            return pos;
        }
        try {
            return getEOLImpl(pos);
        } catch (BadLocationException e) {
            if (Boolean.getBoolean("netbeans.debug.exceptions")) { // NOI18N
                e.printStackTrace();
            }
            return -1;
        }
    }

    /** Get line from position. This is used for example when removal
    * from document is made to find right line number for marks update.
    */
    synchronized int getLine(int pos) throws BadLocationException {
        return getLineImpl(pos);
    }

    synchronized int getLineCount() {
        int lineCnt;
        try {
            lineCnt = endMark.getLine() + 1;
        } catch (InvalidMarkException e) {
            if (Boolean.getBoolean("netbeans.debug.exceptions")) { // NOI18N
                e.printStackTrace();
            }
            return 0;
        }

        return lineCnt;
    }

    /** Get position on line from visual column. This method can be used
    * only for superfixed font i.e. all characters of all font styles
    * have the same width.
    * @param visCol visual column
    * @param startLinePos position of line start
    * @return position on line for particular x-coord
    */
    synchronized int getOffsetFromVisCol(int visCol, int startLinePos)
    throws BadLocationException {
        if (visCol <= 0) {
            return startLinePos;
        }
        visColPosFwdFinder.setVisCol(visCol);
        visColPosFwdFinder.setTabSize(doc.getTabSize());
        int pos = cache.find(visColPosFwdFinder, startLinePos, -1, null);
        return (pos != -1) ? pos : getEOLImpl(startLinePos);
    }

    /** Get visual column from position. This method can be used
    * only for superfixed font i.e. all characters of all font styles
    * have the same width.
    * @param pos position for which the visual column should be returned
    *   the function itself computes the begining of the line first
    */ 
    synchronized int getVisColFromPos(int pos) throws BadLocationException {
        int startLinePos = getBOLImpl(pos);
        posVisColFwdFinder.setTabSize(doc.getTabSize());
        cache.find(posVisColFwdFinder, startLinePos, pos, null);
        return posVisColFwdFinder.getVisCol();
    }


    private int getBOLImpl(int pos) throws BadLocationException {
        if (pos <= 0) { // must be first line
            if (pos == 0) {
                return 0;
            }
            throw new BadLocationException(WRONG_POSITION + pos + DOC_LEN + length(), pos);
        }

        // search cache
        for (int i = 0; i < CACHE_LEN; i++) {
            if (pos >= lineCache[i].bol && pos <= lineCache[i].eol) {
                //        if (lineCache[i].bol != getCheckBOL(pos)) { // !!! remove
                //          checkCache();
                //        }
                cacheMoveFirst(i);
                statCacheHit++;
                return lineCache[0].bol;
            }
        }
        statCacheMiss++;

        // search document when not found
        return cache.find(bolBwdFinder, pos, 0, null) + 1;
    }

    private int getEOLImpl(int pos) throws BadLocationException {
        if (pos < 0) {
            throw new BadLocationException(WRONG_POSITION + pos + DOC_LEN + length(), pos);
        }

        // search cache
        for (int i = 0; i < CACHE_LEN; i++) {
            if (pos >= lineCache[i].bol && pos <= lineCache[i].eol) {
                //        if (lineCache[i].eol != getCheckEOL(pos)) {
                //          checkCache();
                //        }
                cacheMoveFirst(i);
                statCacheHit++;
                return lineCache[0].eol;
            }
        }
        statCacheMiss++;

        // search document
        pos = cache.find(eolFwdFinder, pos, -1, null);
        return (pos != -1) ? pos : docLen;
    }

    private int getBOLFromLineImpl(int line) {
        if (line < 0) {
            return -1;
        }

        // check cache first
        for (int i = 0; i < CACHE_LEN; i++) {
            if (line == lineCache[i].line) {
                //        if (lineCache[i].bol != getCheckBOLFromLine(line)) { // !!! remove
                //          checkCache();
                //        }
                cacheMoveFirst(i);
                statCacheHit++;
                return lineCache[0].bol;
            }
        }
        statCacheMiss++;

        // load line into cache
        cacheMoveFirst(CACHE_LEN - 1);
        return cacheLoadLine(line); // returns -1 for wrong pos

    }

    private int getLineImpl(int pos) throws BadLocationException {
        if (pos < 0 || pos > docLen) {
            throw new BadLocationException(WRONG_POSITION + pos + DOC_LEN + length(), pos);
        }

        // search cache
        for (int i = 0; i < CACHE_LEN; i++) {
            if (pos >= lineCache[i].bol && pos <= lineCache[i].eol) {
                //        if (lineCache[i].line != getCheckLine(pos)) {
                //          checkCache();
                //        }
                cacheMoveFirst(i);
                statCacheHit++;
                return lineCache[0].line;
            }
        }
        statCacheMiss++;

        // load line into cache
        cacheMoveFirst(CACHE_LEN - 1);
        return cacheLoadLineByPos(pos);
    }

    /** If this position is at BOL leave it as it is if returnIfBOL is set.
    * Otherwise get BOL of the next line or end of document.
    * @param returnIfBOL return immediately if the position is already on BOL
    */
    private int adjustNextBOL(int pos, boolean returnIfBOL) throws BadLocationException {
        if (returnIfBOL && pos == getBOLImpl(pos)) {
            return pos;
        }
        pos = getEOLImpl(pos);
        if (pos < docLen) {
            pos++;
        }
        return pos;
    }

    /** Get the first syntax mark that is in the left direction from the desired
    * position.
    */
    MarkFactory.SyntaxMark getLeftSyntaxMark(int pos) {
        return (MarkFactory.SyntaxMark)marks.getLeftMark(pos, MarkFactory.SyntaxMark.class);
    }

    private void update(boolean remove, ModifyUndoEdit undoEdit) {
        int pos = undoEdit.getOffset();
        SyntaxSeg.invalidate(doc, pos);
        SyntaxSeg.Slot slot = SyntaxSeg.getFreeSlot();
        Syntax syntax = doc.getFreeSyntax();

        try {
            cacheUpdate(remove, undoEdit); // Update line cache
            leftUpdateMark = getLeftSyntaxMark(pos - 1); // compute left syntax update mark
            int leftUpdatePos = (leftUpdateMark != null) ? leftUpdateMark.getOffset() : 0;
            // Add or remove syntax marks that are too close or too far away
            updateEvenly();

            // Prepare syntax for updating the mark states of the syntax marks
            prepareSyntax(slot, syntax, leftUpdateMark, leftUpdatePos, 0);

            // Update the syntax marks by mark renderer
            suRenderer.slot = slot;
            suRenderer.syntax = syntax;
            suRenderer.undoEdit = undoEdit;
            suRenderer.remove = remove;
            marks.render(suRenderer);
        } catch (InvalidMarkException e) {
            e.printStackTrace();
        } catch (BadLocationException e) {
            e.printStackTrace();
        } finally {
            doc.releaseSyntax(syntax);
            SyntaxSeg.releaseSlot(slot);
        }
    }

    void initialReadUpdate() { // doesn't need to be synced
        docLen = cache.getDocLength();
        invalidateCache(); // invalidate the line cache
    }

    /** This function is called after document insertion to update marks.
    * Event parameter contains inserted chars and also how many lines was inserted.
    */
    synchronized void insertUpdate(ModifyUndoEdit undoEdit) {
        docLen += undoEdit.getLength();
        marks.insertUpdate(undoEdit.getOffset(), undoEdit.getLength(), undoEdit.getLFCount());
        update(false, undoEdit);
    }

    /** This function is called after document removal to update marks.
    * Event parameter contains removed chars and also how many lines was removed.
    */
    synchronized void removeUpdate(ModifyUndoEdit undoEdit) {
        docLen -= undoEdit.getLength();
        marks.removeUpdate(undoEdit.getOffset(), undoEdit.getLine(), undoEdit.getLength(), undoEdit.getLFCount());
        update(true, undoEdit);
    }

    /** Update marks after insert/removal so that they are
    * well evenly distributed. The method is not synchronized on mark renderer
    * as all changes must be made under document being write-locked.
    */
    private void updateEvenly() {
        // Render the marks by mark renderer
        marks.render(
            new DocMarks.Renderer() {
                protected void render() {
                    int leftMarkIndex = -1;
                    int leftMarkPos = 0;
                    if (leftUpdateMark != null) {
                        leftMarkIndex = getMarkIndex(leftUpdateMark);
                        try {
                            leftMarkPos = leftUpdateMark.getOffset();
                        } catch (InvalidMarkException e) {
                            e.printStackTrace();
                        }
                    }

                    Mark markArray[] = getMarkArray();
                    int cnt = getMarkCnt(); // total mark count
                    int dist = 0; // distance of mark from the current leftMarkPos
                    Mark m;
                    int i = leftMarkIndex + 1;
                    boolean found = false;
                    // First remove all the marks that are on the same position
                    for (; i < cnt; i++) {
                        m = markArray[i];
                        dist += getRelPos(m);
                        if (m.getClass() == MarkFactory.SyntaxMark.class) { // syntax mark found
                            if (m != eolMark) { // eol mark is ignored here
                                found = true;
                                break;
                            }
                        }
                    }
                    if (!found) {
                        dist = length() - leftMarkPos;
                    }
                    // test for too small distance
                    if (dist < MIN_MARK_DISTANCE && found) {
                        try {
                            markArray[i].remove();
                        } catch (InvalidMarkException e) {
                            if (Boolean.getBoolean("netbeans.debug.exceptions")) { // NOI18N
                                e.printStackTrace();
                            }
                        }
                    }

                    // test for too large distance
                    if (dist > MAX_MARK_DISTANCE) {
                        int insCnt = dist / MARK_DISTANCE;
                        if (insCnt > 0) {
                            int startInsPos = leftMarkPos + (dist - (insCnt - 1) * MARK_DISTANCE) / 2;
                            try {
                                for (int j = 0; j < insCnt; j++) {
                                    MarkFactory.SyntaxMark newMark = new MarkFactory.SyntaxMark();
                                    insertMark(newMark, startInsPos + j * MARK_DISTANCE);
                                }
                            } catch (InvalidMarkException e) {
                                if (Boolean.getBoolean("netbeans.debug.exceptions")) { // NOI18N
                                    e.printStackTrace();
                                }
                            } catch (BadLocationException e) {
                                if (Boolean.getBoolean("netbeans.debug.exceptions")) { // NOI18N
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
            }
        );
    }

    /** Check if the EOL syntax mark is at the right place. If not, move it
    * to the end of current line and update it with the right syntax information.
    * It uses instance of syntax used in document.
    */
    void checkEOLMark(int pos) {
        if (docLen == 0) { // empty document is O.K.
            return;
        }
        try {
            int eolPos = adjustNextBOL(pos, false);
            if (eolMark.getOffset() != eolPos) { // mark at the wrong place
                SyntaxSeg.Slot slot = SyntaxSeg.getFreeSlot();
                Syntax syntax = doc.getFreeSyntax();
                MarkFactory.SyntaxMark mark = getLeftSyntaxMark(eolPos - 1);

                try {
                    moveMark(eolMark, eolPos);
                    prepareSyntax(slot, syntax, mark, eolPos, 0); // scan up to eolMark ignoring it in search
                    eolMark.updateStateInfo(syntax);
                } finally {
                    doc.releaseSyntax(syntax);
                    SyntaxSeg.releaseSlot(slot);
                }
            }
        } catch (BadLocationException e) {
            if (Boolean.getBoolean("netbeans.debug.exceptions")) { // NOI18N
                e.printStackTrace();
            }
        } catch (InvalidMarkException e) {
            if (Boolean.getBoolean("netbeans.debug.exceptions")) { // NOI18N
                e.printStackTrace();
            }
        }
    }

    /** Prepare syntax scanner so that it's ready to scan from requested
    * position.
    * @param slot syntax segment slot to be used
    * @param syntax syntax scanner to be used
    * @param leftSyntaxMark first syntax mark in the left direction
    *   from the reqPos, can be obtained by getLeftSyntaxMark()
    * @param reqPos position to which the syntax should be prepared
    * @param reqLen length that will be scanned by the caller after the syntax 
    *   is prepared. The prepareSyntax() automatically preloads this area
    *   into the syntax segment slot.
    * @return how many characters are in the preScan of the syntax before
    *   the reqPos
    */
    int prepareSyntax(SyntaxSeg.Slot slot, Syntax syntax,
                      MarkFactory.SyntaxMark leftSyntaxMark, int reqPos, int reqLen)
    throws BadLocationException {
        // get nearest previous syntax mark
        int markPos = 0;
        int preScan = 0;
        Syntax.StateInfo stateInfo = null;
        if (leftSyntaxMark != null) {
            stateInfo = leftSyntaxMark.getStateInfo();
            preScan = stateInfo.getPreScan();
            try {
                markPos = leftSyntaxMark.getOffset(); // get position to scan from
            } catch (InvalidMarkException e) {
                if (Boolean.getBoolean("netbeans.debug.exceptions")) { // NOI18N
                    e.printStackTrace();
                }
            }
        }

        // load syntax segment
        int prepareLen = (reqPos - markPos) + preScan; // length from left mark to reqPos plus preScan
        slot.load(doc, markPos - preScan, prepareLen + reqLen);

        // load state into syntax scanner - will scan from mark up to reqPos
        syntax.load(stateInfo, slot.array,
                    slot.offset + preScan, prepareLen - preScan, (reqPos >= docLen));
        //    System.out.println("DocOp.java:536 prepareSyntax(): Loaded state=" + ((leftSyntaxMark != null) ? ((leftSyntaxMark.getStateChain() != null) ? syntax.getStateName(leftSyntaxMark.getStateChain().state) : "nullState") : "nullMark") + ", over array='" + EditorDebug.debugChars(slot.array, slot.offset, slot.count) + "', slot.offset=" + slot.offset + ", slot.count=" + slot.count + ", prepareLen=" + prepareLen + ", preScan=" + preScan + ", reqPos=" + reqPos + ", syntax=" + syntax + ", docLen=" + docLen); // NOI18N

        // go through all the tokens till the required position
        while (syntax.nextToken() != Syntax.EOT) { }

        syntax.setStopOffset(slot.offset + slot.count);
        syntax.setLastBuffer(reqPos + reqLen >= docLen);
        return syntax.getPreScan();
    }

    /** Load the line info into lineCache[0]. The line offset must be >= 0.
    * @return BOL of loaded line or -1 if line offset is too high
    */
    private int cacheLoadLine(int line) {
        if (line == 0) { // handle line 0 specially
            try {
                int eol = cache.find(eolFwdFinder, 0, -1, null);
                if (eol == -1) {
                    eol = docLen;
                }
                lineCache[0].fill(0, eol, 0);
            } catch (BadLocationException e) {
                if (Boolean.getBoolean("netbeans.debug.exceptions")) { // NOI18N
                    e.printStackTrace();
                }
            }
            return 0;
        }

        int markLine;
        int markPos;
        try {
            Mark mark = marks.getMarkFromLine(line - 1);
            markPos = mark.getOffsetAndLine(tmpLine);
            markLine = tmpLine[0];
        } catch (InvalidMarkException e) {
            return cacheLoadLine(line); // try again
        }

        try {
            beolLineFwdFinder.fwdLines = line - markLine;
            markPos = cache.find(beolLineFwdFinder, markPos, -1, null);
            int bolPos = beolLineFwdFinder.bolPos;
            if (bolPos == -1) { // wrong line
                return -1;
            }
            if (markPos == -1) { // correct eolPos
                markPos = docLen;
            }
            lineCache[0].fill(bolPos, markPos, line);
            return bolPos;
        } catch (BadLocationException e) {
            throw new Error(); // shouldn't happen
        }
    }

    /* Load line when position is known.
    * @return line offset of the loaded line
    */
    private int cacheLoadLineByPos(int pos) throws BadLocationException {
        int markPos;
        int markLine;
        try {
            Mark mark = marks.getLeftMark(pos);
            markPos = mark.getOffsetAndLine(tmpLine);
            markLine = tmpLine[0];
        } catch (InvalidMarkException e) {
            return cacheLoadLineByPos(pos); // recall
        }

        beolPosFwdFinder.tgtPos = pos;
        markPos = cache.find(beolPosFwdFinder, markPos, -1, null);
        if (markPos == -1) { // correct eolPos
            markPos = docLen;
        }
        int bolPos = beolPosFwdFinder.bolPos;
        if (bolPos == -1) { // mark was on the same line with pos
            if (pos > 0) {
                bolPos = cache.find(bolBwdFinder, pos, 0, null) + 1;
            } else {
                bolPos = 0;
            }
        }
        int line = markLine + beolPosFwdFinder.line;
        lineCache[0].fill(bolPos, markPos, line);
        return line;
    }

    /** Move the entry with some index to be the first in the array */
    private void cacheMoveFirst(int ind) {
        if (ind == 0) {
            return;
        }
        CacheEntry ent = lineCache[ind];
        System.arraycopy(lineCache, 0, lineCache, 1, ind);
        lineCache[0] = ent;
    }

    private void invalidateCache() {
        for (int i = 0; i < CACHE_LEN; i++) {
            lineCache[i].invalidate();
        }
    }

    /** Update cache after document change. */
    private void cacheUpdate(boolean remove, ModifyUndoEdit undoEdit) {
        int pos = undoEdit.getOffset();
        int len = undoEdit.getLength();

        int bolRemovalPos = -1;
        int eolRemovalPos = -1;
        for (int i = 0; i < CACHE_LEN; i++) {
            CacheEntry ent = lineCache[i];
            if (ent.line != -1) {
                if (!remove) { // insert done
                    if (pos >= ent.bol) {
                        if (pos <= ent.eol) { // change inside line
                            int eolOffset = undoEdit.getFirstLFOffset();
                            if (eolOffset == -1) { // no LF inside text
                                ent.eol += len;
                            } else { // at least one LF inside text
                                ent.eol = eolOffset;
                            }
                        } else { // change after end -> do nothing
                        }
                    } else { // pos <= bol -> only move
                        ent.update(len, undoEdit.getLFCount());
                    }
                } else { // remove done
                    if (pos + len >= ent.bol) {
                        if (pos <= ent.eol) { // change before line end
                            ent.line = undoEdit.getLine();
                            if (pos + len > ent.eol) { // end of change after EOL
                                if (eolRemovalPos == -1) {
                                    try {
                                        eolRemovalPos = cache.find(eolFwdFinder, pos, -1, null);
                                    } catch (BadLocationException e) {
                                        if (Boolean.getBoolean("netbeans.debug.exceptions")) { // NOI18N
                                            e.printStackTrace();
                                        }
                                    }
                                    if (eolRemovalPos == -1) {
                                        eolRemovalPos = docLen;
                                    }
                                }
                                ent.eol = eolRemovalPos;
                                if (pos < ent.bol) { // change before line begin
                                    if (bolRemovalPos == -1) {
                                        try {
                                            bolRemovalPos = cache.find(bolBwdFinder, pos, 0, null) + 1;
                                        } catch (BadLocationException e) {
                                            if (Boolean.getBoolean("netbeans.debug.exceptions")) { // NOI18N
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                    ent.bol = bolRemovalPos;
                                }
                            } else { // end of change before EOL
                                ent.eol -= len;
                                if (pos < ent.bol) { // change before line begin
                                    if (bolRemovalPos == -1) {
                                        try {
                                            bolRemovalPos = cache.find(bolBwdFinder, pos, 0, null) + 1;
                                        } catch (BadLocationException e) {
                                            if (Boolean.getBoolean("netbeans.debug.exceptions")) { // NOI18N
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                    ent.bol = bolRemovalPos;
                                }
                            }
                        } else { // change after line end
                        }
                    } else { // pos + len <= bol -> only move
                        ent.update(-len, -undoEdit.getLFCount());
                    }
                }
            }
        }
    }

    /** This mark renderer is used to cycle through syntax marks to update them.
    * It also helps to compute the mark position in the fast way.
    */
    class SyntaxUpdateRenderer extends DocMarks.Renderer {

        /** Computed syntax update position */
        ModifyUndoEdit undoEdit;

        boolean remove;

        /** Slot for scanning the document */
        SyntaxSeg.Slot slot;

        /** Syntax for scanning the document */
        Syntax syntax;

        /** Get all syntax marks in a given range */
        public void render() {
            int markCnt = getMarkCnt();
            int index = 0;
            int pos = 0;
            boolean computed = false;
            Mark markArray[] = getMarkArray();
            int syntaxUpdatePos = -1;

            if (leftUpdateMark != null) {
                try {
                    index = getMarkIndex(leftUpdateMark) + 1;
                    pos = leftUpdateMark.getOffset();
                } catch (InvalidMarkException e) {
                    throw new Error(); // shouldn't happen
                }
            }

            int lastPos = pos; // position of the last syntax mark scanned
            int endPos = pos; // ending position of the rescanning
            if (!remove) { // insert done
                endPos = undoEdit.getOffset() + undoEdit.getLength(); // inserted area that can contain new marks
            }

            while (index < markCnt) { // possibly till end of mark array
                Mark mark = markArray[index++];
                pos += getRelPos(mark);

                if (mark instanceof MarkFactory.SyntaxMark) {
                    MarkFactory.SyntaxMark syntaxMark = (MarkFactory.SyntaxMark)mark;
                    int preScan = syntax.getPreScan();
                    int loadPos = lastPos - preScan;
                    int scanLen = pos - loadPos;

                    if (!slot.isAreaInside(doc, loadPos, scanLen)) {
                        // load the whole area into syntax segment
                        int loadSize = Math.min(docLen - loadPos,
                                                Math.max(scanLen, SYNTAX_UPDATE_BATCH_SIZE));
                        try {
                            slot.load(doc, loadPos, loadSize);
                        } catch (BadLocationException e) {
                            e.printStackTrace();
                            throw new Error(); // shouldn't happen
                        }

                    }

                    // this load should transfer no data but will adjust scanning offset
                    try {
                        slot.load(doc, loadPos, scanLen);
                    } catch (BadLocationException e) {
                        e.printStackTrace();
                        throw new Error(); // shouldn't happen
                    }

                    // Relocate scanning for the good offsets
                    syntax.relocate(slot.array, slot.offset + preScan, scanLen - preScan, (pos == docLen));
                    //          System.out.println("DocOp.java:600 syntax renderer: scan relocated to buffer='" + EditorDebug.debugChars(slot.array, slot.offset + preScan, scanLen - preScan) + "', slot.offset=" + slot.offset + ", scanLen=" + scanLen + ", preScan=" + preScan); // NOI18N

                    while (syntax.nextToken() != Syntax.EOT) { } // scan till the EOT

                    if (syntax.compareState(syntaxMark.getStateInfo()) == Syntax.EQUAL_STATE) {
                        if (syntaxUpdatePos < 0) {
                            syntaxUpdatePos = pos;
                        }
                        if (pos >= endPos && syntaxMark != eolMark) {
                            break;
                        }
                    } else { // state stored in mark is different
                        syntaxUpdatePos = -1;
                        syntaxMark.updateStateInfo(syntax);
                    }

                    lastPos = pos;
                }
            }

            // Update syntax update position
            if (syntaxUpdatePos < 0) {
                syntaxUpdatePos = docLen;
            }
            try {
                undoEdit.setSyntaxUpdateOffset(adjustNextBOL(syntaxUpdatePos, true));
            } catch (BadLocationException e) {
                throw new Error(); // shouldn't happen
            }
        }
    }

    /** Cache entry for line and column information caching.
    * Currently each entry caches begin and end of line and corresponding
    * line number.
    */
    private static class CacheEntry {

        /** Begin of line pos */
        int bol = -1;

        /** End of line pos */
        int eol = -1;

        /** Line offset */
        int line = -1;

        void fill(int bol, int eol, int line) {
            this.bol = bol;
            this.eol = eol;
            this.line = line;
        }

        void update(int deltaLen, int deltaLFCount) {
            this.bol += deltaLen;
            this.eol += deltaLen;
            this.line += deltaLFCount;
        }

        void invalidate() {
            bol = eol = line = -1;
        }

        public String toString() {
            return "line=" + line + ", bol=" + bol + ", eol=" + eol; // NOI18N
        }

    }

    /** Dump the line cache */
    public String cacheToString() {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < CACHE_LEN; i++) {
            sb.append("\ncache[" + i + "]: " + lineCache[i].toString()); // NOI18N
        }
        return sb.toString();
    }

    public String toString() {
        return "statCacheHit=" + statCacheHit // NOI18N
               + ", statCacheMiss=" + statCacheMiss // NOI18N
               + ", Line cache hit ratio=" + (Math.round(1000.0 * statCacheHit / (statCacheHit // NOI18N
                                              + statCacheMiss)) / 10d)
               + cacheToString();
    }

    public String markPlanesToString(Class markClasses[], char markChars[]) {
        return marks.planesToString(markClasses, markChars);
    }

    public String infoToString() {
        return "\n------------------------------ Statistics ------------------------------\n" // NOI18N
               + "cacheSupport: statCharsRead=" + cacheSupport.statCharsRead // NOI18N
               + ", statCharsWritten=" + cacheSupport.statCharsWritten // NOI18N
               + "\nCache: " + cache // NOI18N
               + "\nMarks: " + marks // NOI18N
               + "\nDocOp: " + this; // NOI18N
    }

    /**
     * UnoableEdit created for inserts and removals.
     * The <tt>remove</tt> flag determines whether it's insert or removal
     */
    class ModifyUndoEdit extends AbstractUndoableEdit {

        /** Whether removal was done instead of insertion */
        boolean remove;

        /** Offset where the characters was inserted */
        private int offset;

        /** The inserted characters. If the string was used
        * for the insertion this will be lazily initialized.
        */
        private char[] chars;

        /** The inserted string. If the character buffer was used
        * for the insertion this will be lazily initialized.
        */
        private String text;

        /** The number of the '\n' (line-feed) characters contained
        * in the inserted text. It's lazily initialized.
        */
        private int lfCount = -1;

        /** Line offset of the insert/removal */
        private int line;

        /** Offset of the end of the syntax updating */
        private int syntaxUpdateOffset;

        ModifyUndoEdit(boolean remove, int offset, char[] chars) {
            this.remove = remove;
            this.offset = offset;
            this.chars = chars;
            try {
                this.line = DocOp.this.getLine(offset);
            } catch (BadLocationException e) {
            }
        }

        ModifyUndoEdit(boolean remove, int offset, String text) {
            this.remove = remove;
            this.offset = offset;
            this.text = text;
            try {
                this.line = DocOp.this.getLine(offset);
            } catch (BadLocationException e) {
            }
        }

        boolean isInsert() {
            return !remove;
        }

        boolean isRemove() {
            return remove;
        }

        final int getOffset() {
            return offset;
        }

        /** Get the length of the inserted/removed text */
        int getLength() {
            return (chars != null) ? chars.length : text.length();
        }

        /** Get the inserted text. If the character buffer was used
        * for the insertion then the appropriate variable will be lazily initialized first.
        */
        String getText() {
            if (text == null) {
                text = new String(chars);
            }
            return text;
        }

        /** The inserted characters. If the string was used
        * for the insertion then the appropriate variable will be lazily initialized first.
        */
        char[] getChars() {
            if (chars == null) {
                chars = text.toCharArray();
            }
            return chars;
        }

        /** Whether the text is valid instead of chars. This method helps
        * to avoid the possible conversion from chars to string.
        */
        boolean isTextValid() {
            return (text != null);
        }

        /** Get the number of the '\n' (line-feed) characters contained
        * in the inserted text. It's lazily initialized if necessary.
        */
        int getLFCount() {
            if (lfCount == -1) {
                if (chars != null) { // chars valid valid
                    lfCount = Analyzer.getLFCount(chars);
                } else { // string valid
                    lfCount = Analyzer.getLFCount(text);
                }
            }
            return lfCount;
        }

        /** Get the document offset of the first LF contained
        * in the inserted/removed text or -1 for no LFs.
        * This value is not cached.
        */
        int getFirstLFOffset() {
            if (getLFCount() <= 0) {
                return -1;
            }

            int flfOffset;
            if (chars != null) { // chars valid valid
                flfOffset = Analyzer.getFirstLFOffset(chars);
            } else { // string valid
                flfOffset = Analyzer.getFirstLFOffset(text);
            }

            if (flfOffset >= 0) {
                flfOffset += offset; // shift by the insert/update offset
            }

            return flfOffset;
        }

        int getLine() {
            return line;
        }

        int getSyntaxUpdateOffset() {
            return syntaxUpdateOffset;
        }

        void setSyntaxUpdateOffset(int syntaxUpdateOffset) {
            this.syntaxUpdateOffset = syntaxUpdateOffset;
        }

        public void undo() throws CannotUndoException {
            super.undo();
            try {
                if (remove) {
                    insertEdit(this);
                } else { // insertion
                    removeEdit(this);
                }
            } catch (BadLocationException bl) {
                throw new CannotUndoException();
            }
        }

        public void redo() throws CannotRedoException {
            super.redo();
            try {
                if (remove) {
                    removeEdit(this);
                } else { // insertion
                    insertEdit(this);
                }
            } catch (BadLocationException bl) {
                throw new CannotRedoException();
            }
        }

    }

}

/*
 * $Log: 
 *  8    Gandalf-post-FCS1.7         4/3/00   Miloslav Metelka undo update
 *  7    Gandalf-post-FCS1.6         3/16/00  Miloslav Metelka reverted back
 *  6    Gandalf-post-FCS1.5         3/16/00  Miloslav Metelka reverted back to 
 *       structural for Jaga only
 *  5    Gandalf-post-FCS1.4         3/16/00  Miloslav Metelka reverted back for 
 *       postFCS
 *  4    Gandalf-post-FCS1.3         3/16/00  Miloslav Metelka reverted back to 
 *       structural change in Jaga view (propagated from postFCS)
 *  3    Gandalf-post-FCS1.2         3/15/00  Miloslav Metelka reverted previous 
 *       version - ST error?
 *  2    Gandalf-post-FCS1.1         3/15/00  Miloslav Metelka Structural change
 *  1    Gandalf-post-FCS1.0         3/8/00   Miloslav Metelka 
 * $
 */

