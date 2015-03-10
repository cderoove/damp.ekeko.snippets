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

import java.awt.Color;
import java.util.List;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;

/**
* Various draw layers are located here
*
* @author Miloslav Metelka
* @version 1.00
*/

public class DrawLayerFactory {

    /** Syntax draw layer name */
    public static final String SYNTAX_LAYER_NAME = "syntax-layer"; // NOI18N

    /** Syntax draw layer visibility */
    public static final int SYNTAX_LAYER_VISIBILITY = 1000;

    /** Bookmark draw layer name */
    public static final String BOOKMARK_LAYER_NAME = "bookmark-layer"; // NOI18N

    /** Bookmark draw layer visibility */
    public static final int BOOKMARK_LAYER_VISIBILITY = 2000;

    /** Highlight search layer name */
    public static final String HIGHLIGHT_SEARCH_LAYER_NAME = "highlight-search-layer"; // NOI18N

    /** Highlight search layer visibility */
    public static final int HIGHLIGHT_SEARCH_LAYER_VISIBILITY = 9000;

    /** Incremental search layer name */
    public static final String INC_SEARCH_LAYER_NAME = "inc-search-layer"; // NOI18N

    /** Incremental search layer visibility */
    public static final int INC_SEARCH_LAYER_VISIBILITY = 9500;

    /** Selection draw layer name */
    public static final String CARET_LAYER_NAME = "caret-layer"; // NOI18N

    /** Selection draw layer visibility */
    public static final int CARET_LAYER_VISIBILITY = 10000;


    /** Guarded layer name */
    public static final String GUARDED_LAYER_NAME = "guarded-layer"; // NOI18N

    /** Guarded layer visibility */
    public static final int GUARDED_LAYER_VISIBILITY = 1400;

    public static class SyntaxLayer extends DrawLayer {

        public SyntaxLayer() {
            super(SYNTAX_LAYER_NAME, SYNTAX_LAYER_VISIBILITY);
        }

        protected void init(DrawContext ctx) {
        }

        protected void updateStatus(DrawContext ctx, MarkFactory.DrawMark mark) {
            active = true;
        }

        protected void updateContext(DrawContext ctx) {
        }

    }


    /** This layer colors the line by a color specified in constructor
    * It requires only activation mark since it deactivates automatically
    * at the end of line.
    */
    public static abstract class ColorLineLayer extends DrawLayer {

        /** Coloring to use for highlighting */
        Coloring coloring;

        public ColorLineLayer(String name, int visibility) {
            super(name, visibility);
            extendEOL = true; // by default color till EOL
            updateStatusEOL = true;
        }

        protected void init(DrawContext ctx) {
            coloring = null;
        }

        protected void updateStatus(DrawContext ctx, MarkFactory.DrawMark mark) {
            if (mark != null) {
                active = mark.activateLayer;
            } else {
                active = false;
            }
        }

        protected void updateContext(DrawContext ctx) {
            if (coloring == null) {
                coloring = getColoring(ctx);
            }
            if (coloring != null) {
                coloring.apply(ctx);
            }
        }

        protected abstract Coloring getColoring(DrawContext ctx);

    }


    /** Layer that covers selection services provided by caret.
    * This layer assumes that both caretMark and selectionMark in
    * BaseCaret are properly served so that their active flags
    * are properly set.
    */
    public static class CaretLayer extends DrawLayer {

        Coloring coloring;

        public CaretLayer() {
            super(CARET_LAYER_NAME, CARET_LAYER_VISIBILITY);
            extendEmptyLine = true;
        }

        protected void init(DrawContext ctx) {
            coloring = null;
        }

        protected void updateStatus(DrawContext ctx, MarkFactory.DrawMark mark) {
            if (mark != null) {
                active = mark.activateLayer;
            } else {
                JTextComponent c = ctx.getExtUI().getComponent();
                active = c.getCaret().isSelectionVisible()
                         && ctx.getOffset() >= c.getSelectionStart()
                         && ctx.getOffset() < c.getSelectionEnd();
            }
        }

        protected void updateContext(DrawContext ctx) {
            if (coloring == null) {
                coloring = ctx.getExtUI().getColoring(Settings.SELECTION_COLORING);
            }
            if (coloring != null) {
                coloring.apply(ctx);
            }
        }

    }


    /** Highlight search layer highlights all occurences
    * of the searched string in text.
    */
    public static class HighlightSearchLayer extends DrawLayer {

        /** Pairs of start and end position of the found string */
        int blocks[] = new int[] { -1, -1 };

        /** Coloring to use for highlighting */
        Coloring coloring;

        /** Current index for painting */
        int curInd;

        /** Enabled flag */
        boolean enabled;

        public HighlightSearchLayer() {
            super(HIGHLIGHT_SEARCH_LAYER_NAME, HIGHLIGHT_SEARCH_LAYER_VISIBILITY);
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        protected void init(DrawContext ctx) {
            if (enabled) {
                try {
                    BaseDocument doc = (BaseDocument)ctx.getExtUI().getDocument();
                    blocks = FindSupport.getFindSupport().getBlocks(blocks,
                             doc, ctx.getDrawStartPos(), ctx.getDrawEndPos());
                } catch (BadLocationException e) {
                    blocks = new int[] { -1, -1 };
                }
                coloring = null; // reset so it will be re-read
                curInd = 0;
            } else {
                active = false;
            }
        }

        protected void updateStatus(DrawContext ctx, MarkFactory.DrawMark mark) {
            if (enabled) {
                int pos = ctx.getOffset();
                if (pos == blocks[curInd]) {
                    active = true;
                    nextUpdateStatusPos = blocks[curInd + 1];
                } else if (pos == blocks[curInd + 1]) {
                    active = false;
                    curInd += 2;
                    nextUpdateStatusPos = blocks[curInd];
                    if (pos == nextUpdateStatusPos) { // just follows
                        nextUpdateStatusPos = blocks[curInd + 1];
                        active = true;
                    }
                } else {
                    nextUpdateStatusPos = blocks[curInd];
                }
            }
        }

        protected void updateContext(DrawContext ctx) {
            int pos = ctx.getOffset();
            if (pos >= blocks[curInd] && pos < blocks[curInd + 1]) {
                if (coloring == null) {
                    coloring = ctx.getExtUI().getColoring(Settings.HIGHLIGHT_SEARCH_COLORING);
                }
                if (coloring != null) {
                    coloring.apply(ctx);
                }
            }
        }

    }

    /** Layer covering incremental search. There are just two positions
    * begining and end of the searched string
    */
    public static class IncSearchLayer extends DrawLayer {

        /** Coloring to use for highlighting */
        Coloring coloring;

        /** Position where the searched string begins */
        int pos;

        /** Length of area to highlight */
        int len;

        /** Whether this layer is enabled */
        boolean enabled;

        public IncSearchLayer() {
            super(INC_SEARCH_LAYER_NAME, INC_SEARCH_LAYER_VISIBILITY);
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        void setArea(int pos, int len) {
            this.pos = pos;
            this.len = len;
        }

        int getOffset() {
            return pos;
        }

        int getLength() {
            return len;
        }

        protected void init(DrawContext ctx) {
            if (enabled) {
                nextUpdateStatusPos = pos;
            } else {
                nextUpdateStatusPos = -1;
            }
            active = false;
        }

        protected void updateStatus(DrawContext ctx, MarkFactory.DrawMark mark) {
            if (enabled) {
                if (ctx.getOffset() == pos) {
                    active = true;
                    nextUpdateStatusPos = pos + len;
                } else if (ctx.getOffset() == pos + len) {
                    active = false;
                }
            }
        }

        protected void updateContext(DrawContext ctx) {
            if (coloring == null) {
                coloring = ctx.getExtUI().getColoring(Settings.INC_SEARCH_COLORING);
            }
            if (coloring != null) {
                coloring.apply(ctx);
            }
        }

    }

    /** Bookmark layer */
    public static class BookmarkLayer extends ColorLineLayer {

        public BookmarkLayer() {
            super(BOOKMARK_LAYER_NAME, BOOKMARK_LAYER_VISIBILITY);
        }

        protected Coloring getColoring(DrawContext ctx) {
            return ctx.getExtUI().getColoring(Settings.BOOKMARK_COLORING);
        }

    }

    /** Layer for guarded blocks */
    static class GuardedLayer extends ColorLineLayer {

        GuardedDocument doc;

        GuardedLayer() {
            super(GUARDED_LAYER_NAME, GUARDED_LAYER_VISIBILITY);
        }

        protected void init(DrawContext ctx) {
            super.init(ctx);
            doc = (GuardedDocument)ctx.getExtUI().getDocument();
        }

        protected void updateStatus(DrawContext ctx, MarkFactory.DrawMark mark) {
            if (mark != null) {
                active = mark.activateLayer;
            } else {
                active = doc.isPosGuarded(ctx.getOffset());
            }
        }

        protected Coloring getColoring(DrawContext ctx) {
            return ctx.getExtUI().getColoring(Settings.GUARDED_COLORING);
        }

    }

    /** Style layer getting color settings from particular style */
    public static class StyleLayer extends DrawLayer {

        protected Style style;

        protected MarkChain markChain;

        protected Color backColor;

        protected Color foreColor;

        public StyleLayer(String layerName, int layerVisibility, BaseDocument doc,
                          Style style) {
            super(layerName, layerVisibility);
            this.style = style;
            extendEOL = true;
            updateStatusEOL = true; // turn of at the end of line
            markChain = new MarkChain(doc, layerName);
        }

        public final MarkChain getMarkChain() {
            return markChain;
        }

        protected void init(DrawContext ctx) {
            active = false;
            foreColor = StyleConstants.getForeground(style);
            backColor = StyleConstants.getBackground(style);
        }

        protected void updateStatus(DrawContext ctx, MarkFactory.DrawMark mark) {
            if (mark != null) {
                active = mark.activateLayer;
            } else {
                active = false; // only activate by mark
            }
        }

        protected void updateContext(DrawContext ctx) {
            if (foreColor != null) {
                ctx.setForeColor(foreColor);
            }
            if (backColor != null) {
                ctx.setBackColor(backColor);
            }
        }

        public String toString() {
            return super.toString() + ((markChain != null) ? (", " + markChain) : ""); // NOI18N
        }

    }

    public static class WordColoringLayer extends DrawLayer {

        protected StringMap stringMap = new StringMap();

        public WordColoringLayer() {
            super("word-coloring-layer", 1300); // NOI18N
        }

        public void put(String s, Coloring c) {
            stringMap.put(s, c);
        }

        public void put(String[] strings, Coloring c) {
            for (int i = 0; i < strings.length; i++) {
                put(strings[i], c);
            }
        }

        public void put(List stringList, Coloring c) {
            String strings[] = new String[stringList.size()];
            stringList.toArray(strings);
            put(strings, c);
        }

        protected void init(DrawContext ctx) {
        }

        protected void updateStatus(DrawContext ctx, MarkFactory.DrawMark mark) {
            active = true;
        }

        protected void updateContext(DrawContext ctx) {
            Coloring c = (Coloring)stringMap.get(ctx.getBuffer(),
                                                 ctx.getTokenStart(), ctx.getTokenLength());
            if (c != null) {
                c.apply(ctx);
            }
        }

    }


}

/*
 * Log
 *  27   Gandalf-post-FCS1.25.1.0    3/8/00   Miloslav Metelka 
 *  26   Gandalf   1.25        1/26/00  Miloslav Metelka BadLocationExc behavior 
 *       changed
 *  25   Gandalf   1.24        1/13/00  Miloslav Metelka 
 *  24   Gandalf   1.23        1/11/00  Miloslav Metelka 
 *  23   Gandalf   1.22        1/10/00  Miloslav Metelka 
 *  22   Gandalf   1.21        12/28/99 Miloslav Metelka 
 *  21   Gandalf   1.20        11/14/99 Miloslav Metelka 
 *  20   Gandalf   1.19        11/8/99  Miloslav Metelka 
 *  19   Gandalf   1.18        10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  18   Gandalf   1.17        10/10/99 Miloslav Metelka 
 *  17   Gandalf   1.16        10/7/99  Miloslav Metelka StyleLayer reflects fore
 *       color
 *  16   Gandalf   1.15        9/10/99  Miloslav Metelka 
 *  15   Gandalf   1.14        8/17/99  Miloslav Metelka 
 *  14   Gandalf   1.13        7/20/99  Miloslav Metelka 
 *  13   Gandalf   1.12        7/2/99   Miloslav Metelka 
 *  12   Gandalf   1.11        6/1/99   Miloslav Metelka 
 *  11   Gandalf   1.10        5/17/99  Miloslav Metelka fix for Bwd finder
 *  10   Gandalf   1.9         5/16/99  Miloslav Metelka 
 *  9    Gandalf   1.8         5/13/99  Miloslav Metelka 
 *  8    Gandalf   1.7         5/5/99   Miloslav Metelka 
 *  7    Gandalf   1.6         4/23/99  Miloslav Metelka Undo added and internal 
 *       improvements
 *  6    Gandalf   1.5         4/8/99   Miloslav Metelka 
 *  5    Gandalf   1.4         4/1/99   Miloslav Metelka 
 *  4    Gandalf   1.3         3/30/99  Miloslav Metelka 
 *  3    Gandalf   1.2         3/23/99  Miloslav Metelka 
 *  2    Gandalf   1.1         3/18/99  Miloslav Metelka 
 *  1    Gandalf   1.0         2/3/99   Miloslav Metelka 
 * $
 */

