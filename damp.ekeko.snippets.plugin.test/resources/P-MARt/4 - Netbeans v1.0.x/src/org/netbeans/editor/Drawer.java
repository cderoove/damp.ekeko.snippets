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

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Font;
import java.awt.Color;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Insets;
import java.text.AttributedCharacterIterator;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;

/**
* Class responsible for doing the all redrawing on the screen.
* It's also responsible for doing syntax reanalyzing after operations
* that change syntax highlighting.
* There is only one instance for all documents and views.
*
* @author Miloslav Metelka
* @version 1.00
*/
class Drawer {

    /** Initial size of mark array in <CODE>DrawMarkRenderer</CODE>. */
    private static final int DEFAULT_DRAW_MARK_RENDERER_SIZE = 20;

    /** Only one instance of drawer */
    private static Drawer drawer;

    /** Prevent creation */
    private Drawer() {
    }

    /** Get the static instance of drawer */
    public static Drawer getDrawer() {
        if (drawer == null) {
            drawer = new Drawer();
        }
        return drawer;
    }

    /** Draw on the specified area.
    * @param dg draw graphics through which the drawing is done
    * @param extUI extended UI to use
    * @param startPos position from which the drawing starts. It must be BOL
    * @param endPos position where the drawing stops. It must be either BOL of
    * @param targetPos position where the targetPosReached() method
    *   of drawGraphics is called. This is useful for caret update or modelToView.
    *   The Integer.MAX_VALUE can be passed to ignore that behavior. The -1 value
    *   has special meaning there so that it calls targetPosReached() after each
    *   character processed. This is used by viewToModel to find the position
    *   for some point.
    */
    void draw(DrawGraphics dg, ExtUI extUI, int startPos, int endPos,
              int baseX, int baseY, int targetPos) throws BadLocationException {
        // Some correctness tests at the begining
        if (dg == null || extUI == null
                || startPos < 0 || endPos < 0 || startPos > endPos
                || baseX < 0 || baseY < 0
           ) {
            return;
        }

        /*
        try {
          int cnt = Utilities.getLineOffset(extUI.getDocument(), endPos)
              - Utilities.getLineOffset(extUI.getDocument(), startPos);
          if (cnt > 0) {
            System.out.println("startPos=" + startPos + ", endPos=" + endPos + ", line difference=" + cnt); // NOI18N
          }
    } catch (BadLocationException e) {
          e.printStackTrace();
    }
        */
        //    System.out.println("Drawer.java:92 startPos=" + startPos + ", endPos=" + endPos + ", baseX=" + baseX + ", baseY=" + baseY); // NOI18N

        synchronized (extUI) { // lock operations manipulating draw layer chain
            BaseDocument doc = extUI.getDocument();
            if (doc == null) { // no base-document available
                return;
            }

            SyntaxSeg.Slot slot = SyntaxSeg.getFreeSlot();
            Syntax syntax = doc.getFreeSyntax();
            DrawMarkRenderer drawMarkRenderer = new DrawMarkRenderer();

            doc.readLock();
            try {
                JTextComponent component = extUI.getComponent();
                int docLen = doc.getLength();
                int visCol = 0; // visual column (tabs expanded) on the line
                int x = baseX;
                int y = baseY;
                int ascent = extUI.ascents[0];
                int spaceWidth = 0; // display width of actual space character
                Coloring defaultColoring = extUI.getDefaultColoring();
                Font compFont = defaultColoring.getFont();
                Color compBackColor = defaultColoring.getBackColor();
                Color compForeColor = defaultColoring.getForeColor();
                Font previousFont = compFont; // used when calling targetPosReached()
                int tabSize = doc.getTabSize();
                int pos = startPos; // actual painting position
                boolean lastBuffer = false; // last syntax segment buffer in document
                boolean endDocDraw = false; // paint normal line at very end of doc
                int widestWidth = 0; // widest x coordinate encountered during paint
                DrawContextImpl ctx = new DrawContextImpl();
                boolean targetAll = (targetPos == -1); // all chars are targets
                boolean contDraw = true; // flag indicating whether draw should continue
                int lineNum = 0; // current line number
                int lineCnt = 0; // number of lines drawn
                char[] lineNumChars = null;
                Coloring lineNumColoring = null;
                Font lnFont = null;
                Color lnBackColor = null;
                Color lnForeColor = null;
                Graphics graphics = dg.getGraphics();
                boolean lazyLineNum = false;

                int debugFragCnt = 0; // !!!
                int debugUpdateCnt = 0; // !!!

                if (graphics != null) {
                    if (extUI.renderingHints != null) {
                        ((Graphics2D)graphics).setRenderingHints(extUI.renderingHints);
                    }
                    if (extUI.textLimitLineVisible) { // draw limit line
                        int chw = FontMetricsCache.getFontMetrics(extUI.getDefaultColoring().getFont(), graphics).stringWidth("x");
                        int lineX = baseX + extUI.textLimitWidth * chw;
                        graphics.setColor(extUI.textLimitLineColor);
                        Rectangle clip = graphics.getClipBounds();
                        //            graphics.drawRect(lineX, clip.y, 1, clip.height);
                        graphics.drawLine(lineX, clip.y, lineX, clip.y + clip.height);
                    }
                }

                dg.setJoinTokens(extUI.fixedFont);

                // create buffer for showing line numbers
                if (extUI.lineNumberVisible && dg.supportsLineNumbers()) {
                    try {
                        lineNum = Utilities.getLineOffset(doc, pos) + 1;
                    } catch (BadLocationException e) {
                        if (Boolean.getBoolean("netbeans.debug.exceptions")) { // NOI18N
                            e.printStackTrace();
                        }
                    }
                    lineNumColoring = extUI.getColoring(Settings.LINE_NUMBER_COLORING);
                    if (lineNumColoring == null) {
                        lineNumColoring = defaultColoring; // no number coloring found
                    }
                    lnFont = lineNumColoring.getFont();
                    if (lnFont == null) {
                        lnFont = compFont;
                    }
                    lnBackColor = lineNumColoring.getBackColor();
                    if (lnBackColor == null) {
                        lnBackColor = compBackColor;
                    }
                    lnForeColor = lineNumColoring.getForeColor();
                    if (lnForeColor == null) {
                        lnForeColor = compForeColor;
                    }
                    lineNumChars = new char[Math.max(extUI.lineNumberMaxDigitCnt, 1)];
                    if (graphics == null) {
                        lazyLineNum = true;
                    }
                }

                // Initialize draw context
                ctx.pos = startPos;
                ctx.drawStartPos = startPos;
                ctx.drawEndPos = endPos;
                ctx.extUI = extUI;
                ctx.foreColor = compForeColor;
                ctx.backColor = compBackColor;
                ctx.font = compFont;
                ctx.bol = true; // draw must always start at line begin

                // Init draw graphics
                dg.init(ctx);

                // INIT ALL LAYERS BEFORE THE PAINT BEGINS
                DrawLayer[] layerArray = extUI.getDrawLayerList().currentLayers();
                int activeLayerEndIndex = 0;
                for (int i = 0; i < layerArray.length; i++) {
                    layerArray[i].init(ctx); // init all layers
                }

                int updatePos = Integer.MAX_VALUE; // next status update position
                int layerUpdatePos = updatePos; // position of next layer update
                for (int i = 0; i < layerArray.length; i++) { // update status of all layers
                    DrawLayer l = layerArray[i];
                    l.updateStatus(ctx, null);
                    if (l.nextUpdateStatusPos > pos
                            && l.nextUpdateStatusPos < layerUpdatePos
                       ) {
                        layerUpdatePos = l.nextUpdateStatusPos;
                    }
                    if (l.active) {
                        activeLayerEndIndex = i + 1; // assign end of active layers
                    }
                }
                updatePos = layerUpdatePos;

                // GET ALL THE DRAW MARKS IN DRAW AREA THROUGH RENDERER
                drawMarkRenderer.setRange(startPos, endPos);
                doc.op.renderMarks(drawMarkRenderer); // no synch needed
                int markInd = 0; // index of current (next) draw mark in array
                MarkFactory.DrawMark mark = null; // current draw mark
                boolean markUpdate = false; // update status is because of mark
                int markPos = Integer.MAX_VALUE; // position of actual draw mark

                // Get current draw mark
                if (drawMarkRenderer.rangeMarkCnt > 0) {
                    mark = drawMarkRenderer.rangeMarkArray[markInd];
                    markPos = drawMarkRenderer.rangePosArray[markInd++];
                    if (markPos < layerUpdatePos) {
                        updatePos = markPos;
                        markUpdate = true;
                    } else {
                        updatePos = layerUpdatePos;
                    }
                }

                // Prepare syntax scanner and then cycle through all the syntax segments
                doc.op.prepareSyntax(slot, syntax, doc.op.getLeftSyntaxMark(pos), pos, endPos - pos);
                syntax.setLastBuffer(true); // always set to handle even non-complete lines
                ctx.buffer = slot.array;
                dg.setBuffer(slot.array);

                // CYCLE THROUGH ALL THE TOKENS FOUND IN THE BUFFER -------------------
                do {
                    int tokenID = syntax.nextToken();
                    if (tokenID == Syntax.EOT) { // end of text area
                        if (!ctx.eol) { // force EOL
                            tokenID = Syntax.EOL;
                            endDocDraw = true;
                        } else { // there was already EOL
                            if (pos == docLen && !endDocDraw) {
                                endDocDraw = true;
                            } else {
                                break; // break this cycle (and also outer one)
                            }
                        }
                    }

                    // Get the token type and docColorings
                    String tokenName = syntax.getTokenName(tokenID);
                    Coloring c = extUI.getColoring(tokenName);
                    if (c == null) {
                        c = defaultColoring;
                    }
                    // Get the token
                    ctx.tokenStart = syntax.getTokenOffset();
                    ctx.tokenLen = syntax.getTokenLength();
                    int drawnLen = 0; // the length of current token that was already drawn
                    int fragLen = 0; // length of current token fragment
                    int blankWidth = 0; // display width of blank space for WS tokens

                    if (ctx.bol) { // if we are on the line begining
                        spaceWidth = extUI.defaultSpaceWidth;
                        // possibly print line numbers at begining of each line
                        if (lazyLineNum) {
                            int i = Math.max(lineNumChars.length - 1, 0);
                            int n = lineNum;
                            do {
                                lineNumChars[i--] = (char)('0' + (n % 10));
                                n /= 10;
                            } while (n != 0 && i >= 0);
                            while (i >= 0) {
                                lineNumChars[i--] = ' ';
                            }
                            dg.setBuffer(lineNumChars);
                            dg.setForeColor(lnForeColor);
                            dg.setBackColor(lnBackColor);
                            dg.setFont(lnFont);
                            dg.drawChars(0, lineNumChars.length, 0,
                                         y, extUI.lineNumberWidth,
                                         extUI.charHeight, extUI.lineNumberAscent, false);
                            dg.setBuffer(slot.array);
                            lineNum++;
                        }
                    }

                    // PROCESS ALL THE FRAGMENTS OF ONE TOKEN ----------------------------
                    do {
                        // Fill in the draw context
                        ctx.pos = pos;
                        ctx.foreColor = compForeColor;
                        ctx.backColor = compBackColor;
                        ctx.font = compFont;
                        ctx.eol = (tokenID == Syntax.EOL);

                        // Check for status updates in planes at the begining of this fragment
                        int nextPos = Integer.MAX_VALUE;
                        while (updatePos == pos) {
                            debugUpdateCnt++;
                            if (markUpdate) { // update because of draw mark
                                // means no-mark update yet performed
                                activeLayerEndIndex = 0;
                                for (int i = 0; i < layerArray.length; i++) {
                                    DrawLayer l = layerArray[i];
                                    if (l.getName().equals(mark.layerName)
                                            && (mark.isDocumentMark() || extUI == mark.getExtUI())
                                       ) {
                                        l.updateStatus(ctx, mark);
                                        if (l.nextUpdateStatusPos > pos
                                                && l.nextUpdateStatusPos < layerUpdatePos
                                           ) {
                                            layerUpdatePos = l.nextUpdateStatusPos;
                                        }
                                    }
                                    if (l.active) {
                                        activeLayerEndIndex = i + 1;
                                    }
                                }

                                // Get next mark
                                if (markInd < drawMarkRenderer.rangeMarkCnt) {
                                    mark = drawMarkRenderer.rangeMarkArray[markInd];
                                    markPos = drawMarkRenderer.rangePosArray[markInd++];
                                } else { // no more draw marks
                                    mark = null;
                                    markPos = Integer.MAX_VALUE;
                                }
                                // Check next update position
                                if (markPos < layerUpdatePos) {
                                    updatePos = markPos;
                                } else {
                                    markUpdate = false;
                                    updatePos = layerUpdatePos;
                                }
                            } else { // update because nextUpdateStatusPos set in some layer
                                layerUpdatePos = Integer.MAX_VALUE;
                                activeLayerEndIndex = 0;
                                for (int i = 0; i < layerArray.length; i++) {
                                    DrawLayer l = layerArray[i];
                                    if (l.nextUpdateStatusPos == pos) {
                                        l.updateStatus(ctx, null);
                                    }
                                    if (l.nextUpdateStatusPos > pos
                                            && l.nextUpdateStatusPos < layerUpdatePos
                                       ) {
                                        layerUpdatePos = l.nextUpdateStatusPos;
                                    }
                                    if (l.active) {
                                        activeLayerEndIndex = i + 1;
                                    }
                                }
                                if (pos == markPos) { // mark on this position
                                    markUpdate = true;
                                } else { // no marks on this position, set new update pos
                                    updatePos = layerUpdatePos;
                                    if (markPos < updatePos) {
                                        updatePos = markPos;
                                        markUpdate = true;
                                    }
                                }
                            }
                        }

                        // COMPUTE CURRENT FRAGMENT (of token) LENGTH ----------------------
                        int fragStart = ctx.tokenStart + drawnLen;
                        fragLen = Math.min(updatePos - pos, ctx.tokenLen - drawnLen);
                        // check whether there are no tabs in the fragment and possibly shrink
                        boolean wsFrag = false;
                        boolean tabsInFrag = false; // whether there are tabs inside fragment
                        if (fragLen > 0 && slot.array[fragStart] == ' ') { // space is first in the token
                            int nwInd = Analyzer.getFirstNonSpace(slot.array, fragStart, fragLen);
                            if (nwInd != -1) { // not whole fragment is whitespace
                                fragLen = nwInd - fragStart;
                            }
                            wsFrag = true;
                        } else { // space is not first char in the token
                            int tabOffset = Analyzer.getFirstTab(slot.array, fragStart, fragLen);
                            if (tabOffset >= 0) { // tab inside fragment
                                if (tabOffset == fragStart) { // tab is first char in fragment
                                    tabsInFrag = true;
                                    int nwInd = Analyzer.getFirstNonWhite(slot.array, fragStart, fragLen);
                                    if (nwInd != -1) { // not whole fragment is whitespace
                                        fragLen = nwInd - fragStart;
                                    }
                                    wsFrag = true;
                                } else { // tab somewhere inside fragment
                                    fragLen = tabOffset - fragStart; // shrink fragment size
                                }
                            }
                        }
                        int spaceLen = fragLen;

                        debugFragCnt++;
                        // Go through all layers to update draw context
                        int layerEndInd = Math.min(activeLayerEndIndex, layerArray.length);
                        for (int i = 0; i < layerEndInd; i++) {
                            DrawLayer l = layerArray[i];
                            if (l.active) {
                                // syntax layer special handling follows
                                if (DrawLayerFactory.SYNTAX_LAYER_NAME == l.getName()) {
                                    c.apply(ctx);
                                } else { // regular layer
                                    l.updateContext(ctx);
                                }
                            }
                        }

                        // HANDLE POSSIBLE WHITE SPACE EXPANSION AND COMPUTE DISPLAY WIDTH
                        int fragWidth = 0; // display width of the fragment characters
                        if (wsFrag) { // white space fragment
                            /* spaceWidth is not updated for white space tokens, so that
                            * spaceWidth stays computed from the last non-WS token drawn.
                            * Although this is slightly logically incorrect, it allows
                            * concatenating of the tokens with the whitespace between them.
                            * For example 'private static final int' can be drawn
                            * by calling drawChars() only once instead of four times.
                            */
                            spaceLen = Analyzer.getColumn(slot.array,
                                                          fragStart, fragLen, tabSize, visCol) - visCol;
                            fragWidth = spaceLen * spaceWidth;
                        } else { // non-WS token
                            // update spaceWidth
                            spaceWidth = extUI.spaceWidths[ctx.font.getStyle()];
                            // Compute fragWidth
                            if (fragLen > 0) {
                                if (extUI.superFixedFont) {
                                    fragWidth = fragLen * extUI.spaceWidths[0];
                                } else if (extUI.fixedFont) {
                                    fragWidth = fragLen * extUI.spaceWidths[ctx.font.getStyle()];
                                } else {
                                    fragWidth = FontMetricsCache.getFontMetrics(ctx.font, component).charsWidth(
                                                    slot.array, fragStart, fragLen);
                                }
                            }
                        }

                        // POSSIBLY FILL THE BACKGROUND WITH SPECIAL COLOR ----------------
                        boolean emptyLine = false;
                        blankWidth = fragWidth;
                        if (ctx.eol) { // special handling for EOL
                            dg.flush();
                            do {
                                blankWidth = 0;
                                if (ctx.bol) { // empty line found
                                    if (!emptyLine) { // not yet processed
                                        layerEndInd = Math.min(activeLayerEndIndex, layerArray.length);
                                        for (int i = 0; i < layerEndInd; i++) {
                                            DrawLayer l = layerArray[i];
                                            if (l.active && l.extendEmptyLine) {
                                                emptyLine = true; // for at least one layer
                                                l.updateContext(ctx);
                                            }
                                        }
                                        if (emptyLine) { // count only if necessary
                                            blankWidth = spaceWidth / 2; // display half of char
                                        }
                                    } else { // already went through the cycle once for empty line
                                        emptyLine = false;
                                    }
                                }

                                if (!emptyLine) { // EOL and currently not servicing empty line
                                    boolean extendEOL = false;
                                    layerEndInd = Math.min(activeLayerEndIndex, layerArray.length);
                                    for (int i = 0; i < layerEndInd; i++) {
                                        DrawLayer l = layerArray[i];
                                        if (l.active && l.extendEOL) {
                                            extendEOL = true; // for at least one layer
                                            l.updateContext(ctx);
                                        }
                                    }
                                    if (extendEOL) {
                                        blankWidth = component.getWidth();
                                    }
                                }

                                if (blankWidth > 0) {
                                    dg.setBackColor(ctx.backColor);
                                    dg.fillRect(x, y, blankWidth, extUI.charHeight);
                                    if (emptyLine) {
                                        x += blankWidth;
                                    }
                                }
                            } while (emptyLine);

                        } else { // DRAW REGULAR FRAGMENT

                            dg.setBackColor(ctx.backColor);
                            if (!wsFrag) {
                                dg.setForeColor(ctx.foreColor);
                                dg.setFont(ctx.font);
                                if (!extUI.superFixedFont && graphics != null) {
                                    if (extUI.fixedFont) {
                                        ascent = extUI.ascents[ctx.font.getStyle()];
                                    } else {
                                        ascent = (int)(FontMetricsCache.getFontMetrics(ctx.font, graphics).getAscent() * extUI.lineHeightCorrection);
                                    }
                                }
                            }
                            dg.drawChars(tabsInFrag ? -1 : fragStart, spaceLen, x, y,
                                         fragWidth, extUI.charHeight, ascent, wsFrag);
                        }

                        // CHECK WHETHER TARGET POS WAS REACHED ---------------------------
                        if (fragLen == 0 && (targetPos == pos || targetAll)) {
                            char ch = (tokenID == Syntax.EOL) ? '\n' : ' ';
                            contDraw = dg.targetPosReached(pos, ch, x, y, extUI.spaceWidths[0],
                                                           ctx, previousFont);
                        } else if (targetAll) {
                            int prevWidth = 0;
                            int curWidth;
                            int baseIndex = fragStart;
                            for (int i = 0; contDraw && i < fragLen; i++) {
                                if (tabsInFrag) { // handle fragment with tabs in a different way
                                    int spcCount = Analyzer.getColumn(slot.array,
                                                                      fragStart, i + 1, tabSize, visCol) - visCol;
                                    curWidth = spcCount * spaceWidth;

                                } else { // no tabs in a fragment
                                    if (extUI.fixedFont) {
                                        curWidth = (i + 1) * spaceWidth;
                                    } else {
                                        curWidth = FontMetricsCache.getFontMetrics(ctx.font, component).charsWidth(
                                                       slot.array, baseIndex,  i + 1);
                                    }
                                }
                                contDraw = dg.targetPosReached(pos + i, slot.array[baseIndex + i],
                                                               x + prevWidth, y, curWidth - prevWidth, ctx,
                                                               (i == 0) ? previousFont : ctx.font);
                                prevWidth = curWidth;
                            }
                        } else if (targetPos < pos + fragLen && pos <= targetPos) {
                            int curWidth;
                            int prevWidth = 0;
                            int baseIndex = fragStart;
                            int i = (targetPos - pos);
                            if (extUI.fixedFont) { // fixed of superFixed font
                                prevWidth = i * spaceWidth;
                                curWidth = (i + 1) * spaceWidth;
                            } else { // variable width font
                                if (i > 0) {
                                    if (tabsInFrag) { // handle fragment with tabs in a different way
                                        int spcCount = Analyzer.getColumn(slot.array,
                                                                          fragStart, i, tabSize, visCol) - visCol;
                                        prevWidth = spcCount * spaceWidth;

                                    } else { // no tabs in a fragment
                                        prevWidth = FontMetricsCache.getFontMetrics(ctx.font, component).charsWidth(
                                                        slot.array, baseIndex, i);
                                    }
                                }

                                if (tabsInFrag) { // handle fragment with tabs in a different way
                                    int spcCount = Analyzer.getColumn(slot.array,
                                                                      fragStart, i + 1, tabSize, visCol) - visCol;
                                    curWidth = spcCount * spaceWidth;

                                } else { // no tabs in a fragment
                                    curWidth = FontMetricsCache.getFontMetrics(ctx.font, component).charsWidth(
                                                   slot.array, baseIndex, i + 1);
                                }
                            }
                            contDraw = dg.targetPosReached(pos + i, slot.array[baseIndex + i],
                                                           x + prevWidth, y, curWidth - prevWidth, ctx,
                                                           (i == 0) ? previousFont : ctx.font);
                        }
                        previousFont = ctx.font;

                        // Update status of layers that need it at the end of line
                        if (ctx.eol) {
                            activeLayerEndIndex = 0;
                            for (int i = 0; i < layerArray.length; i++) {
                                DrawLayer l = layerArray[i];
                                if (l.updateStatusEOL) {
                                    l.updateStatus(ctx, null);
                                    if (l.nextUpdateStatusPos >= pos + fragLen
                                            && l.nextUpdateStatusPos < updatePos
                                       ) {
                                        updatePos = l.nextUpdateStatusPos;
                                        markUpdate = false;
                                    }
                                }
                                if (l.active) {
                                    activeLayerEndIndex = i + 1;
                                }
                            }
                        }

                        // Move the variables to the next fragment in token
                        pos += fragLen;
                        drawnLen += fragLen;
                        visCol += spaceLen;
                        x += fragWidth;
                        ctx.bol = false;
                    } while(contDraw && drawnLen < ctx.tokenLen);
                    // all fragments of token were drawn here

                    // Update coordinates at the end of each line
                    if (ctx.eol) {
                        dg.eol(x, y); // sign EOL to DG
                        widestWidth = Math.max(widestWidth, x); // update widest width
                        visCol = 0;
                        x = baseX;
                        y += extUI.charHeight;
                        ctx.bol = true;
                        lineCnt++;
                    }

                } while (contDraw); // cycle through all tokens inside buffer
                dg.setBuffer(null);

                dg.finish();
                extUI.updateVirtualWidth(widestWidth
                                         + extUI.lineNumberWidth + extUI.spaceWidths[0]); // one char width for cursor

                if (graphics != null) {
                    Rectangle bounds = extUI.getExtentBounds();
                    Rectangle clip = graphics.getClipBounds();
                    Insets textMargin = extUI.textMargin;
                    if (extUI.lineNumberVisible && !lazyLineNum) { // draw line numbers now
                        if (extUI.lineNumberVisible && dg.supportsLineNumbers()) {
                            int numY = baseY;
                            if (clip.x <= bounds.x + textMargin.left) {
                                graphics.setColor(lnBackColor);
                                if (lnBackColor != null && !compBackColor.equals(lnBackColor)) {
                                    graphics.fillRect(bounds.x, numY, extUI.lineNumberWidth,
                                                      lineCnt * extUI.charHeight);
                                }
                                graphics.setColor(lnForeColor);
                                graphics.setFont(lnFont);
                                numY += extUI.lineNumberAscent;
                                int lastDigit = Math.max(lineNumChars.length - 1, 0);
                                int numX = bounds.x;
                                if (extUI.lineNumberMargin != null) {
                                    numX += extUI.lineNumberMargin.left;
                                }

                                for (int j = 0; j < lineCnt; j++) { // draw all line numbers
                                    int n = lineNum + j;
                                    int i = lastDigit;
                                    do {
                                        lineNumChars[i--] = (char)('0' + (n % 10));
                                        n /= 10;
                                    } while (n != 0 && i >= 0);
                                    while (i >= 0) {
                                        lineNumChars[i--] = ' ';
                                    }
                                    graphics.drawChars(lineNumChars, 0, lineNumChars.length, numX, numY);
                                    numY += extUI.charHeight;
                                }
                            }
                        }
                    }
                    // Possibly clear margins
                    graphics.setColor(compBackColor);
                    int leftM = textMargin.left - extUI.lineNumberWidth;
                    if (leftM > 0 && bounds.x > 0) {
                        graphics.fillRect(bounds.x + extUI.lineNumberWidth,
                                          baseY, leftM, lineCnt * extUI.charHeight);
                    }
                    if (textMargin.right > 0) {
                        graphics.fillRect(bounds.x + bounds.width - textMargin.right,
                                          baseY, textMargin.right, lineCnt * extUI.charHeight);
                    }
                    if (textMargin.top > 0 && clip.y < bounds.y + textMargin.top) {
                        graphics.fillRect(bounds.x, bounds.y,
                                          bounds.width, textMargin.top);
                    }
                    int bY = bounds.y + bounds.height - textMargin.bottom;
                    if (textMargin.bottom > 0 && clip.y + clip.height > bY) {
                        graphics.fillRect(bounds.x, bY,
                                          bounds.width, textMargin.bottom);
                    }
                }

            } finally {
                doc.releaseSyntax(syntax);
                SyntaxSeg.releaseSlot(slot);
                doc.readUnlock();
            }
        } // synchronized on extUI
    }


    private final class DrawContextImpl implements DrawContext {

        Color foreColor;

        Color backColor;

        Font font;

        int pos;

        int drawStartPos;

        int drawEndPos;

        boolean bol;

        boolean eol;

        ExtUI extUI;

        char[] buffer;

        int token;

        int tokenStart;

        int tokenLen;

        public Color getForeColor() {
            return foreColor;
        }

        public void setForeColor(Color foreColor) {
            this.foreColor = foreColor;
        }

        public Color getBackColor() {
            return backColor;
        }

        public void setBackColor(Color backColor) {
            this.backColor = backColor;
        }

        public Font getFont() {
            return font;
        }

        public void setFont(Font font) {
            this.font = font;
        }

        public int getOffset() {
            return pos;
        }

        public int getDrawStartPos() {
            return drawStartPos;
        }

        public int getDrawEndPos() {
            return drawEndPos;
        }

        public boolean isBOL() {
            return bol;
        }

        public boolean isEOL() {
            return eol;
        }

        public ExtUI getExtUI() {
            return extUI;
        }

        public char[] getBuffer() {
            return buffer;
        }

        public int getToken() {
            return token;
        }

        public int getTokenStart() {
            return tokenStart;
        }

        public int getTokenLength() {
            return tokenLen;
        }

    }

    /** Draw graphics interface is used to enable various kinds of drawing. It's used
    * for drawing into classic graphics, painting the caret and printing. Its functions
    * are similair to subset of Graphics but there are few differences.
    */
    interface DrawGraphics {

        /** Get foreground color */
        public Color getForeColor();

        /** Set foreground color */
        public void setForeColor(Color foreColor);

        /** Get background color */
        public Color getBackColor();

        /** Set background color */
        public void setBackColor(Color backColor);

        /** Get current font */
        public Font getFont();

        /** Set current font */
        public void setFont(Font font);

        /** Get the graphics to determine whether this draws to a graphics.
        * This is useful for fast line numbering and others.
        */
        public Graphics getGraphics();

        /** Whether draw graphics supports displaying of line numbers.
        * If not line number displaying is not done.
        */
        public boolean supportsLineNumbers();

        /** Whether the draw graphics can use the optimization technique
        * that joins multiple tokens with the same font and color into one.
        * This can be turned off generally when using variable width fonts.
        */
        public void setJoinTokens(boolean join);

        /** Initialize this draw graphics before drawing */
        public void init(DrawContext ctx);

        /** Flush the cached information, so that there's no dependence
        * on buffer content.
        */
        public void flush();

        /** Called when whole drawing ends. Can be used to deallocate
        * some resources etc.
        */
        public void finish();

        /** Fill rectangle with specified color
        */
        public void fillRect(int x, int y, int width, int height);

        /** Draw characters from the specified offset in the buffer
        * @param offset offset in the buffer for drawn text; if the text contains
        *   tabs, then offset is set to -1 and length contains the full length
        *   of the expanded tabs
        * @param length length of the text being drawn
        * @param x x coordinate
        * @param y y coordinate
        * @param textWidth width of the text being drawn in points
        * @param isWhite whether the text contains only ' ' or '\t'
        */
        public void drawChars(int offset, int length, int x, int y,
                              int width, int height, int ascent, boolean isWhite);

        /** Set character buffer from which the characters are drawn. */
        public void setBuffer(char[] buffer);

        /** This method is called to notify this draw graphics in response
        * from targetPos parameter passed to draw().
        * @param pos position reached. This has sense when targetPos is -1
        * @param ch character at pos
        * @param x x painting position
        * @param y y painting position
        * @param charWidth visual width of the character ch
        * @param ctx current draw context containing 
        * @param previousFont this is useful for caret painting to do correct
        *   italicizing
        * @return whether the drawing should continue or not. If it returns
        *   false it's guaranteed that this method will not be called again
        *   and the whole draw() method will be stopped.
        */
        public boolean targetPosReached(int pos, char ch, int x, int y,
                                        int charWidth, DrawContext ctx, Font previousFont);

        /** EOL encountered */
        public void eol(int x, int y);


    }

    /** Parent of caret and view DGs. It only remembers
    * current color, font and buffer.
    */
    static abstract class AbstractDG implements Drawer.DrawGraphics {

        /** Current foreground color */
        Color foreColor;

        /** Current background color */
        Color backColor;

        /** Current font */
        Font font;

        /** Character buffer from which the data are drawn */
        char[] buffer;

        public Color getForeColor() {
            return foreColor;
        }

        public void setForeColor(Color foreColor) {
            this.foreColor = foreColor;
        }

        public Color getBackColor() {
            return backColor;
        }

        public void setBackColor(Color backColor) {
            this.backColor = backColor;
        }

        public Font getFont() {
            return font;
        }

        public void setFont(Font font) {
            this.font = font;
        }

        public Graphics getGraphics() {
            return null;
        }

        public boolean supportsLineNumbers() {
            return false;
        }

        /** Tokens joining is ignored by default */
        public void setJoinTokens(boolean join) {
        }

        public void init(DrawContext ctx) {
        }

        public void flush() {
        }

        public void finish() {
        }

        public void fillRect(int x, int y, int width, int height) {
        }

        public void drawChars(int offset, int length, int x, int y,
                              int width, int height, int ascent, boolean isWhite) {
        }

        public void setBuffer(char[] buffer) {
            this.buffer = buffer;
        }

        public boolean targetPosReached(int pos, char ch, int x, int y,
                                        int charWidth, DrawContext ctx, Font previousFont) {
            return true; // shouldn't reach this place
        }

        public void eol(int x, int y) {
        }

    }

    /** Implementation of DrawGraphics to delegate to some Graphics. */
    static final class GraphicsDG extends AbstractDG {

        Graphics graphics;

        /** Current graphics color */
        Color gColor;

        /** Current graphics font */
        Font gFont;

        /** Background color of component */
        Color compBackColor;

        /** Start of the chars that were not drawn yet */
        int startOffset = -1;

        /** End of the chars that were not drawn yet */
        int endOffset;

        /** X coordinate where the drawing of chars should occur */
        int x;

        /** Y coordinate where the drawing of chars should occur */
        int y;

        int width;
        int height;
        int ascent;

        boolean joinTokens;

        int debugDCCnt; // drawChars() real call count !!!
        int debugFRCnt; // fillRect() real call count !!!


        GraphicsDG(Graphics graphics) {
            this.graphics = graphics;
        }

        public void init(DrawContext ctx) {
            JTextComponent c = ctx.getExtUI().getComponent();
            compBackColor = c.getBackground();
            gColor = graphics.getColor();
            gFont = graphics.getFont();
            debugDCCnt = 0;
            debugFRCnt = 0;
        }

        public void flush() {
            if (startOffset < 0) {
                return;
            }
            if (startOffset == endOffset) {
                startOffset = -1;
                return;
            }
            fillRect(x, y, width, height);
            if (foreColor != gColor) {
                graphics.setColor(foreColor);
                gColor = foreColor;
            }
            if (font != gFont) {
                graphics.setFont(font);
                gFont = font;
            }
            graphics.drawChars(buffer, startOffset, endOffset - startOffset, x, y + ascent);
            debugDCCnt++;
            startOffset = -1;
        }

        public void finish() {
            flush();
        }

        public void setForeColor(Color foreColor) {
            if (!foreColor.equals(this.foreColor)) {
                flush();
                this.foreColor = foreColor;
            }
        }

        public void setBackColor(Color backColor) {
            if (!backColor.equals(this.backColor)) {
                flush();
                this.backColor = backColor;
            }
        }

        public void setFont(Font font) {
            if (!font.equals(this.font)) {
                flush();
                this.font = font;
            }
        }

        public Graphics getGraphics() {
            return graphics;
        }

        public boolean supportsLineNumbers() {
            return true;
        }

        public void setJoinTokens(boolean join) {
            joinTokens = join;
        }

        public void fillRect(int x, int y, int width, int height) {
            if (width > 0) {
                if (!backColor.equals(compBackColor)) {
                    if (backColor != gColor) {
                        graphics.setColor(backColor);
                        gColor = backColor;
                    }
                    graphics.fillRect(x, y, width, height);
                    debugFRCnt++;
                }
            }
        }

        public void drawChars(int offset, int length, int x, int y,
                              int width, int height, int ascent, boolean isWhite) {
            if (!joinTokens && startOffset >= 0) { // flush each painting when not joining
                flush();
            }
            if (length >= 0) {
                if (offset < 0) { // has tabs inside
                    flush();
                    fillRect(x, y, width, height);
                } else { // no tabs inside
                    if (startOffset < 0) {
                        if (isWhite) { // use fill for non-acumulated space
                            fillRect(x, y, width, height);
                        } else { // non-space text
                            startOffset = offset;
                            endOffset = offset + length;
                            this.x = x;
                            this.y = y;
                            this.width = width;
                            this.height = height;
                            this.ascent = ascent;
                        }
                    } else { // already token before
                        endOffset += length;
                        this.width += width;
                    }
                }
            }
        }

        public void setBuffer(char[] buffer) {
            flush();
            this.buffer = buffer;
            startOffset = -1;
        }

        public void eol(int x, int y) {
            flush();
        }

    }

    static class PrintDG extends AbstractDG {

        PrintContainer container;

        /** Whether there were some paints already on the line */
        boolean lineInited;

        /** Construct the new print graphics
        * @param container print container to which the tokens
        *   are added.
        */
        public PrintDG(PrintContainer container) {
            this.container = container;
        }

        public boolean supportsLineNumbers() {
            return true;
        }

        public void drawChars(int offset, int length, int x, int y,
                              int width, int height, int ascent, boolean isWhite) {
            if (length > 0) {
                char[] chars = new char[length];
                if (offset < 0) { // tabs inside
                    System.arraycopy(Analyzer.getSpacesBuffer(length), 0, chars, 0, length);
                } else { // no tabs inside
                    System.arraycopy(buffer, offset, chars, 0, length);
                }
                container.add(chars, font, foreColor, backColor);
            }
        }

        public void eol(int x, int y) {
            if (!lineInited && container.initEmptyLines()) {
                drawChars(-1, 1, x, y, 1, 1, 1, true);
            }
            container.eol();
            lineInited = false;
        }

    }


    /** This mark renderer is used to get two arrays - one for syntax marks
    * that were found in draw area and the other array holds positions
    * of these marks.
    */
    static class DrawMarkRenderer extends DocMarks.Renderer {

        /** Array of marks */
        MarkFactory.DrawMark rangeMarkArray[]
        = new MarkFactory.DrawMark[DEFAULT_DRAW_MARK_RENDERER_SIZE];

        /** Array of positions */
        int rangePosArray[] = new int[DEFAULT_DRAW_MARK_RENDERER_SIZE];

        /** Total count of found marks (and also positions) */
        int rangeMarkCnt;

        /** Starting position of mark search */
        int startPos;

        /** End position of mark search */
        int endPos;

        /** Get all syntax marks in a given range */
        public void render() {
            int markCnt = getMarkCnt();
            Mark mark = (Mark)getMarks().getLeftMark(startPos);
            int srcIndex = 0;
            int pos = 0;
            rangeMarkCnt = 0;
            Mark markArray[] = getMarkArray();
            try {
                srcIndex = getMarkIndex(mark);
                pos = mark.getOffset();
            } catch (InvalidMarkException e) {
                if (Boolean.getBoolean("netbeans.debug.exceptions")) { // NOI18N
                    e.printStackTrace();
                }
            }
            if (pos < startPos) { // go to next mark
                mark = markArray[++srcIndex];
                pos += getRelPos(mark);
            }
            while (pos <= endPos) { // will include even end-of-doc marks
                if (mark instanceof MarkFactory.DrawMark) {
                    MarkFactory.DrawMark dm = (MarkFactory.DrawMark)mark;
                    if (!dm.removeInvalid()) { // remove if not valid
                        // Check array ranges
                        if (rangePosArray.length < rangeMarkCnt + 1) {
                            MarkFactory.DrawMark rma[] = new MarkFactory.DrawMark[
                                                             2 * rangeMarkArray.length];
                            System.arraycopy(rangeMarkArray, 0, rma, 0, rangeMarkCnt);
                            rangeMarkArray = rma;

                            int rpa[] = new int[rma.length];
                            System.arraycopy(rangePosArray, 0, rpa, 0, rangeMarkCnt);
                            rangePosArray = rpa;
                        }

                        rangeMarkArray[rangeMarkCnt] = (MarkFactory.DrawMark)mark;
                        rangePosArray[rangeMarkCnt++] = pos;
                    }
                }
                if (++srcIndex < markCnt) {
                    mark = markArray[srcIndex];
                    pos += getRelPos(mark);
                } else {
                    break;
                }
            }
        }

        /** Set the start and end positions between which
        * the renderer will operate.
        */
        public void setRange(int startPos, int endPos) {
            this.startPos = startPos;
            this.endPos = endPos;
        }

    }

}

/*
 * Log
 *  52   Gandalf-post-FCS1.46.1.4    4/18/00  Miloslav Metelka cursor move fix
 *  51   Gandalf-post-FCS1.46.1.3    4/17/00  Miloslav Metelka fixed ghost tabs
 *  50   Gandalf-post-FCS1.46.1.2    4/13/00  Miloslav Metelka text-line now visible
 *  49   Gandalf-post-FCS1.46.1.1    4/5/00   Miloslav Metelka using FM caching
 *  48   Gandalf-post-FCS1.46.1.0    3/8/00   Miloslav Metelka 
 *  47   Gandalf   1.46        1/13/00  Miloslav Metelka 
 *  46   Gandalf   1.45        1/10/00  Miloslav Metelka 
 *  45   Gandalf   1.44        12/28/99 Miloslav Metelka 
 *  44   Gandalf   1.43        11/14/99 Miloslav Metelka 
 *  43   Gandalf   1.42        11/10/99 Miloslav Metelka 
 *  42   Gandalf   1.41        11/8/99  Miloslav Metelka 
 *  41   Gandalf   1.40        10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  40   Gandalf   1.39        10/10/99 Miloslav Metelka 
 *  39   Gandalf   1.38        10/8/99  Miloslav Metelka Stability improvement
 *  38   Gandalf   1.37        10/8/99  Miloslav Metelka stability improvements
 *  37   Gandalf   1.36        10/6/99  Miloslav Metelka 
 *  36   Gandalf   1.35        9/30/99  Miloslav Metelka 
 *  35   Gandalf   1.34        9/16/99  Miloslav Metelka 
 *  34   Gandalf   1.33        9/15/99  Miloslav Metelka 
 *  33   Gandalf   1.32        9/10/99  Miloslav Metelka 
 *  32   Gandalf   1.31        8/17/99  Miloslav Metelka 
 *  31   Gandalf   1.30        7/29/99  Miloslav Metelka 
 *  30   Gandalf   1.29        7/21/99  Miloslav Metelka 
 *  29   Gandalf   1.28        7/21/99  Miloslav Metelka 
 *  28   Gandalf   1.27        7/20/99  Miloslav Metelka 
 *  27   Gandalf   1.26        7/2/99   Miloslav Metelka 
 *  26   Gandalf   1.25        6/29/99  Miloslav Metelka Scrolling and patches
 *  25   Gandalf   1.24        6/25/99  Miloslav Metelka from floats back to ints
 *  24   Gandalf   1.23        6/24/99  Miloslav Metelka Drawing improved
 *  23   Gandalf   1.22        6/22/99  Miloslav Metelka 
 *  22   Gandalf   1.21        6/8/99   Miloslav Metelka 
 *  21   Gandalf   1.20        6/4/99   Miloslav Metelka removed debug
 *  20   Gandalf   1.19        6/1/99   Miloslav Metelka 
 *  19   Gandalf   1.18        6/1/99   Miloslav Metelka 
 *  18   Gandalf   1.17        5/21/99  Miloslav Metelka 
 *  17   Gandalf   1.16        5/18/99  Miloslav Metelka patched printing
 *  16   Gandalf   1.15        5/15/99  Miloslav Metelka fixes
 *  15   Gandalf   1.14        5/13/99  Miloslav Metelka 
 *  14   Gandalf   1.13        5/7/99   Miloslav Metelka line numbering and fixes
 *  13   Gandalf   1.12        5/5/99   Miloslav Metelka 
 *  12   Gandalf   1.11        4/23/99  Miloslav Metelka Undo added and internal 
 *       improvements
 *  11   Gandalf   1.10        4/8/99   Miloslav Metelka 
 *  10   Gandalf   1.9         4/8/99   Miloslav Metelka 
 *  9    Gandalf   1.8         4/5/99   Ian Formanek    Removed debug print
 *  8    Gandalf   1.7         4/1/99   Miloslav Metelka 
 *  7    Gandalf   1.6         3/30/99  Miloslav Metelka 
 *  6    Gandalf   1.5         3/27/99  Miloslav Metelka 
 *  5    Gandalf   1.4         3/23/99  Miloslav Metelka 
 *  4    Gandalf   1.3         3/18/99  Miloslav Metelka 
 *  3    Gandalf   1.2         2/13/99  Miloslav Metelka 
 *  2    Gandalf   1.1         2/9/99   Miloslav Metelka 
 *  1    Gandalf   1.0         2/3/99   Miloslav Metelka 
 * $
 */

