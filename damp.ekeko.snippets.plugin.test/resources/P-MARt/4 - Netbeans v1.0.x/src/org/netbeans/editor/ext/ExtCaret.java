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

package org.netbeans.editor.ext;

import java.awt.Rectangle;
import java.awt.Font;
import javax.swing.text.JTextComponent;
import javax.swing.text.BadLocationException;
import org.netbeans.editor.BaseCaret;
import org.netbeans.editor.Coloring;
import org.netbeans.editor.ExtUI;
import org.netbeans.editor.MarkFactory;
import org.netbeans.editor.DrawLayerFactory;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.InvalidMarkException;
import org.netbeans.editor.DrawContext;
import org.netbeans.editor.DrawLayer;
import org.netbeans.editor.Settings;
import org.netbeans.editor.SettingsUtil;
import org.netbeans.editor.SettingsChangeEvent;

/**
* Extended caret implementation
*
* @author Miloslav Metelka
* @version 1.00
*/

public class ExtCaret extends BaseCaret {

    /** Highlight row draw layer name */
    public static final String HIGHLIGHT_ROW_LAYER_NAME = "highlight-row-layer"; // NOI18N

    /** Highlight row draw layer visibility */
    public static final int HIGHLIGHT_ROW_LAYER_VISIBILITY = 1600;

    /** Highlight matching bracket draw layer name */
    public static final String HIGHLIGHT_BRACKET_LAYER_NAME = "highlight-bracket-layer"; // NOI18N

    /** Highlight matching bracket draw layer visibility */
    public static final int HIGHLIGHT_BRACKET_LAYER_VISIBILITY = 11000;

    /** Whether to highlight the background of the row
    * where the caret is.
    */
    boolean highlightRow;

    /** Whether to hightlight the matching bracket */
    boolean highlightBracket;

    /** Coloring used for highlighting the row where the caret is. */
    Coloring highlightRowColoring;

    /** Coloring used for highlighting the matching bracket */
    Coloring highlightBracketColoring;

    /** Mark holding the start of the line where the caret currently is. */
    MarkFactory.DrawMark highlightRowMark;

    /** Mark holding the position of the matching bracket. */
    MarkFactory.DrawMark highlightBracketMark;

    boolean bracketMarkValid;

    boolean simpleBracketMatching;

    static final long serialVersionUID =-4292670043122577690L;

    /** Called when settings were changed. The method is called
    * also in constructor, so the code must count with the evt being null.
    */
    public void settingsChange(SettingsChangeEvent evt) {
        super.settingsChange(evt);

        JTextComponent c = component;
        if (c != null) {
            ExtUI extUI = Utilities.getExtUI(c);
            Class kitClass = Utilities.getKitClass(c);
            highlightRowColoring = extUI.getColoring(Settings.HIGHLIGHT_ROW_COLORING);
            highlightBracketColoring = extUI.getColoring(Settings.HIGHLIGHT_BRACKET_COLORING);
            highlightRow = SettingsUtil.getBoolean(kitClass,
                                                   ExtSettings.HIGHLIGHT_CARET_ROW, false);
            highlightBracket = SettingsUtil.getBoolean(kitClass,
                               ExtSettings.HIGHLIGHT_MATCHING_BRACKET, false);
            simpleBracketMatching = SettingsUtil.getBoolean(kitClass,
                                    ExtSettings.SIMPLE_BRACKET_MATCHING, true);
        }
    }

    public void install(JTextComponent c) {
        ExtUI extUI = Utilities.getExtUI(c);
        extUI.addLayer(new HighlightRowLayer());
        extUI.addLayer(new HighlightBracketLayer());
        super.install(c);
    }

    public void deinstall(JTextComponent c) {
        ExtUI extUI = Utilities.getExtUI(c);
        extUI.removeLayer(HIGHLIGHT_ROW_LAYER_NAME);
        extUI.removeLayer(HIGHLIGHT_BRACKET_LAYER_NAME);
        super.deinstall(c);
    }

    protected void update(Rectangle scrollRect, int scrollPolicy) {
        if (highlightRow || highlightBracket) {
            JTextComponent c = component;
            if (c != null) {
                ExtUI extUI = Utilities.getExtUI(c);
                BaseDocument doc = (BaseDocument)c.getDocument();
                int dotPos = getDot();
                if (highlightRow) {
                    try {
                        int bolPos = Utilities.getRowStart(doc, dotPos);
                        if (highlightRowMark != null) {
                            int markPos = highlightRowMark.getOffset();
                            if (bolPos != markPos) {
                                extUI.repaintPos(markPos);
                                Utilities.moveMark(doc, highlightRowMark, bolPos);
                                extUI.repaintPos(bolPos);
                            }
                        } else { // highlight mark is null
                            highlightRowMark = new MarkFactory.DrawMark(HIGHLIGHT_ROW_LAYER_NAME, extUI);
                            highlightRowMark.setActivateLayer(true);
                            Utilities.insertMark(doc, highlightRowMark, bolPos);
                            extUI.repaintPos(bolPos);
                        }
                    } catch (BadLocationException e) {
                        highlightRow = false;
                    } catch (InvalidMarkException e) {
                        highlightRow = false;
                    }
                }

                try {
                    boolean madeValid = false;
                    if (highlightBracket && dotPos > 0) {
                        int matchPos = doc.getSyntaxSupport().findMatchingBracket(dotPos - 1,
                                       simpleBracketMatching);
                        if (matchPos >= 0) {
                            if (highlightBracketMark != null) {
                                int markPos = highlightBracketMark.getOffset();
                                if (markPos != matchPos) {
                                    extUI.repaintPos(markPos);
                                    Utilities.moveMark(doc, highlightBracketMark, matchPos);
                                    extUI.repaintPos(matchPos);
                                } else { // on the same position
                                    if (!bracketMarkValid) { // was not valid, must repaint
                                        extUI.repaintPos(matchPos);
                                    }
                                }
                            } else { // highlight mark is null
                                highlightBracketMark = new MarkFactory.DrawMark(
                                                           HIGHLIGHT_BRACKET_LAYER_NAME, extUI);
                                highlightBracketMark.setActivateLayer(true);
                                Utilities.insertMark(doc, highlightBracketMark, matchPos);
                                extUI.repaintPos(matchPos);
                            }
                            bracketMarkValid = true;
                            madeValid = true;
                        }
                    }

                    if (!madeValid) {
                        if (bracketMarkValid) {
                            bracketMarkValid = false;
                            extUI.repaintPos(highlightBracketMark.getOffset());
                        }
                    }
                } catch (BadLocationException e) {
                    if (System.getProperty("netbeans.debug.exceptions") != null) { // NOI18N
                        e.printStackTrace();
                    }
                    highlightBracket = false;
                } catch (InvalidMarkException e) {
                    if (System.getProperty("netbeans.debug.exceptions") != null) { // NOI18N
                        e.printStackTrace();
                    }
                    highlightBracket = false;
                }

            }
        }

        super.update(scrollRect, scrollPolicy);
    }

    /** Draw layer to highlight the row where the caret currently resides */
    class HighlightRowLayer extends DrawLayerFactory.ColorLineLayer {

        public HighlightRowLayer() {
            super(HIGHLIGHT_ROW_LAYER_NAME, HIGHLIGHT_ROW_LAYER_VISIBILITY);
        }

        protected Coloring getColoring(DrawContext ctx) {
            return highlightRowColoring;
        }

    }

    /** Draw layer to highlight the matching bracket */
    class HighlightBracketLayer extends DrawLayer {

        public HighlightBracketLayer() {
            super(HIGHLIGHT_BRACKET_LAYER_NAME, HIGHLIGHT_BRACKET_LAYER_VISIBILITY);
        }

        protected void init(DrawContext ctx) {
            active = bracketMarkValid;
        }

        protected void updateStatus(DrawContext ctx, MarkFactory.DrawMark mark) {
            if (mark != null) {
                if (bracketMarkValid) {
                    active = true;
                    setNextUpdateStatusPos(ctx.getOffset() + 1);
                }
            } else {
                active = false;
            }
        }

        protected void updateContext(DrawContext ctx) {
            if (highlightBracketColoring != null) {
                highlightBracketColoring.apply(ctx);
                Font f = ctx.getFont();
                if (!f.isBold()) {
                    ctx.setFont(f.deriveFont(f.getStyle() | Font.BOLD));
                }
            }
        }

    }

}

/*
 * Log
 *  10   Gandalf-post-FCS1.8.1.0     3/8/00   Miloslav Metelka 
 *  9    Gandalf   1.8         1/18/00  Miloslav Metelka 
 *  8    Gandalf   1.7         1/13/00  Miloslav Metelka Localization
 *  7    Gandalf   1.6         1/11/00  Miloslav Metelka 
 *  6    Gandalf   1.5         1/10/00  Miloslav Metelka 
 *  5    Gandalf   1.4         1/4/00   Miloslav Metelka 
 *  4    Gandalf   1.3         12/28/99 Miloslav Metelka 
 *  3    Gandalf   1.2         11/27/99 Patrik Knakal   
 *  2    Gandalf   1.1         11/11/99 Miloslav Metelka 
 *  1    Gandalf   1.0         11/8/99  Miloslav Metelka 
 * $
 */

