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

import java.awt.event.ActionEvent;
import java.util.Iterator;
import java.util.List;
import javax.swing.text.JTextComponent;
import javax.swing.text.BadLocationException;
import javax.swing.text.TextAction;
import javax.swing.text.Segment;

/**
* Various internal tests
*
* @author Miloslav Metelka
* @version 0.10
*/

public class EditorDebug {

    private EditorDebug() {
        // instance creation has no sense
    }

    public static void dumpPlanes(JTextComponent component) {
        BaseDocument doc = (BaseDocument)component.getDocument();
        Class markClasses[] = new Class[] {
                                  MarkFactory.LineMark.class,
                                  MarkFactory.CaretMark.class,
                                  MarkFactory.DrawMark.class,
                                  MarkFactory.SyntaxMark.class,
                                  Mark.class
                              };
        char markChars[] = new char[] {
                               'L', 'C', 'D', 'S', 'B'
                           };
        System.out.println("--------------------------- DUMP OF MARK PLANES --------------------------------"); // NOI18N
        System.out.println("Mark legend:\nD - DrawMark\n" // NOI18N
                           + "S - SyntaxMark\nB - BaseMark\n" // NOI18N
                           + "L - LineMark\nC - CaretMark"); // NOI18N
        System.out.println(doc.op.markPlanesToString(markClasses, markChars));
        System.out.println("--------------------------------------------------------------------------------\n"); // NOI18N
    }

    public static void dumpSyntaxMarks(JTextComponent component) {
        System.out.println("--------------------------- DUMP OF SYNTAX MARKS --------------------------------"); // NOI18N
        final BaseDocument doc = (BaseDocument)component.getDocument();
        final int docLen = doc.getLength();
        doc.op.renderMarks(
            new DocMarks.Renderer() {
                public void render() {
                    int markCnt = getMarkCnt();
                    int index = 0;
                    int pos = 0;
                    int lastPos = pos;
                    int lineCnt = 0;
                    int lastMarkPos = 0;
                    int maxMarkDistance = 0;
                    int minMarkDistance = docLen;
                    Mark markArray[] = getMarkArray();
                    SyntaxSeg.Slot slot = SyntaxSeg.getFreeSlot();
                    Syntax syntax = doc.getFreeSyntax();

                    try {
                        syntax.load(null, slot.array, 0, 0, false);
                        while (index < markCnt) {
                            Mark mark = markArray[index++];
                            pos += getRelPos(mark);
                            if (mark instanceof MarkFactory.SyntaxMark) {
                                MarkFactory.SyntaxMark syntaxMark = (MarkFactory.SyntaxMark)mark;
                                int delta = pos - lastMarkPos;
                                if (delta > maxMarkDistance) {
                                    maxMarkDistance = delta;
                                }
                                if (delta < minMarkDistance) {
                                    minMarkDistance = delta;
                                }
                                lastMarkPos = pos;

                                int preScan = syntax.getPreScan();
                                int loadPos = lastPos - preScan;
                                int scanLen = pos - loadPos;
                                try {
                                    slot.load(doc, loadPos, scanLen);
                                } catch (BadLocationException e) {
                                    e.printStackTrace();
                                }
                                syntax.relocate(slot.array, slot.offset + preScan, scanLen - preScan, (pos == docLen));
                                while (true) {
                                    int tokenID = syntax.nextToken();
                                    if (tokenID == Syntax.EOT) {
                                        break;
                                    }
                                    if (tokenID == Syntax.EOL) {
                                        lineCnt++;
                                    }
                                }
                                lastPos = pos;
                                try {
                                    System.out.println(((syntaxMark == doc.op.eolMark)
                                                        ? "!!EOLMark!!" : "syntaxMark:") // NOI18N
                                                       + " getOffset()=" + Utilities.debugPosition(doc, syntaxMark.getOffset()) // NOI18N
                                                       + ", getLine()=" + syntaxMark.getLine() // NOI18N
                                                       + ", " + syntaxMark // NOI18N
                                                       + ",\n    StateInfo=" + syntaxMark.getStateInfo() // NOI18N
                                                       + ",\n    Syntax:" + syntax); // NOI18N
                                } catch (InvalidMarkException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    } finally {
                        doc.releaseSyntax(syntax);
                        SyntaxSeg.releaseSlot(slot);
                    }

                    System.out.println("Maximum mark distance is " + maxMarkDistance // NOI18N
                                       + "\nMinimum mark distance is " + minMarkDistance); // NOI18N
                }
            }
        );
        System.out.println("--------------------------------------------------------------------------------\n"); // NOI18N
    }

    public static void test(JTextComponent component) {
        BaseTextUI ui = ((BaseTextUI)component.getUI());
        BaseView view = (BaseView)ui.getRootView(component);
        view = (BaseView)view.getView(0);
        final BaseDocument doc = (BaseDocument)component.getDocument();
        ExtUI extUI = ui.getExtUI();
        final int docLen = doc.getLength();

        System.out.println("\n------------------------- Registry --------------------------------"); // NOI18N
        System.out.println(Registry.getRegistry().toString());

        System.out.println("\n------------------------- DEBUGGING INFORMATION --------------------------------"); // NOI18N
        String buf = "Document: " + doc // NOI18N
                     + "\nview.mainHeight=" + ((LeafView)view).mainHeight // NOI18N
                     + "\nDoubleBuffering=" + component.isDoubleBuffered(); // NOI18N
        buf += "\ncomponent.getLocation()=" + component.getLocation() // NOI18N
               + "\ncomponent.getSize()=" + component.getSize() // NOI18N
               + "\nvirtualSize=" + extUI.virtualSize; // NOI18N
        buf += "\nExtUI LAYERS:\n" + extUI.getDrawLayerList(); // NOI18N
        System.out.println(buf);

        System.out.println(doc.op.infoToString());

        buf = "\n-------------------------- EOL Test -------------------------------\n"; // NOI18N
        try {
            int lineCnt1 = doc.op.getLine(doc.getLength());
            buf += "Number of lines by getLine()=" + lineCnt1; // NOI18N
            final int lineCnt2[] = new int[1];
            System.out.println(buf);
            doc.op.renderMarks(
                new DocMarks.Renderer() {
                    public void render() {
                        int markCnt = getMarkCnt();
                        int index = 0;
                        int pos = 0;
                        int lastPos = pos;
                        int lineCnt = 0;
                        Mark markArray[] = getMarkArray();
                        SyntaxSeg.Slot slot = SyntaxSeg.getFreeSlot();
                        Syntax syntax = doc.getFreeSyntax();

                        try {
                            syntax.load(null, slot.array, 0, 0, false);
                            while (index < markCnt) {
                                Mark mark = markArray[index++];
                                pos += getRelPos(mark);
                                if (mark instanceof MarkFactory.SyntaxMark) {
                                    MarkFactory.SyntaxMark syntaxMark = (MarkFactory.SyntaxMark)mark;
                                    int preScan = syntax.getPreScan();
                                    int loadPos = lastPos - preScan;
                                    int scanLen = pos - loadPos;
                                    try {
                                        slot.load(doc, loadPos, scanLen);
                                    } catch (BadLocationException e) {
                                        e.printStackTrace();
                                    }
                                    syntax.relocate(slot.array, slot.offset + preScan, scanLen - preScan, (pos == docLen));
                                    while (true) {
                                        int tokenID = syntax.nextToken();
                                        if (tokenID == Syntax.EOT) {
                                            break;
                                        }
                                        if (tokenID == Syntax.EOL) {
                                            lineCnt++;
                                        }
                                    }
                                    lastPos = pos;
                                    try {
                                        if (mark.getLine() != lineCnt) {
                                            System.out.println("Error found comparing line at mark with index=" // NOI18N
                                                               + (index - 1) + " having pos=" + pos // NOI18N
                                                               + ". Line offset stored in mark is " + mark.getLine() // NOI18N
                                                               + " but syntax scanning gives " + lineCnt + ". Scanned area" // NOI18N
                                                               + " with preScan=" + preScan + " is " + scanLen + " long: '" // NOI18N
                                                               + debugChars(slot.array, slot.offset, scanLen)
                                                               + "'. The line test was interrupted."); // NOI18N
                                            break;
                                        }
                                    } catch (InvalidMarkException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                            lineCnt2[0] = lineCnt;
                        } finally {
                            doc.releaseSyntax(syntax);
                            SyntaxSeg.releaseSlot(slot);
                        }
                    }
                }
            );
            buf = "Number of lines by syntax scanning=" + lineCnt2[0] + "\n"; // NOI18N
            buf += (lineCnt1 == lineCnt2[0]) ? "Line counting test succeeded." // NOI18N
                   : "Line counting test failed !"; // NOI18N
            System.out.println(buf);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }

        buf = "\n------------------------ CR occurence test ------------------------\n"; // NOI18N
        try {
            char chars[] = doc.getChars(0, docLen);
            int i;
            for (i = 0; i < docLen; i++) {
                if (chars[i] == '\r') {
                    buf += "CR found at pos=" + i + ", line=" + doc.op.getLine(i) + "\n"; // NOI18N
                    break;
                }
            }
            if (i == docLen) {
                buf += "No CR found. CR occurence test suceeded."; // NOI18N
            }
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
        System.out.println(buf);
    }

    static class DumpPlanesAction extends TextAction {

        static final long serialVersionUID =-6845202263936980798L;

        DumpPlanesAction() {
            super(BaseKit.dumpPlanesAction);
        }

        public void actionPerformed(ActionEvent evt) {
            JTextComponent component = getTextComponent(evt);
            dumpPlanes(component);
        }
    }

    static class DumpSyntaxMarksAction extends TextAction {

        static final long serialVersionUID =-5416690898412332655L;

        DumpSyntaxMarksAction() {
            super(BaseKit.dumpSyntaxMarksAction);
        }

        public void actionPerformed(ActionEvent evt) {
            JTextComponent component = getTextComponent(evt);
            dumpSyntaxMarks(component);
        }
    }

    static class SelfTestAction extends TextAction {

        static final long serialVersionUID =4317494489421553541L;

        SelfTestAction() {
            super(BaseKit.selfTestAction);
        }

        public void actionPerformed(ActionEvent evt) {
            JTextComponent component = getTextComponent(evt);
            test(component);
        }
    }

    public static void checkSettings(Class kitClass) throws Error {
        int readBufferSize = SettingsUtil.getInteger(kitClass, Settings.READ_BUFFER_SIZE,
                             DefaultSettings.defaultReadBufferSize);

        int writeBufferSize = SettingsUtil.getInteger(kitClass, Settings.WRITE_BUFFER_SIZE,
                              DefaultSettings.defaultWriteBufferSize);

        int readMarkDistance = SettingsUtil.getInteger(kitClass, Settings.READ_MARK_DISTANCE,
                               DefaultSettings.defaultReadMarkDistance);

        int markDistance = SettingsUtil.getInteger(kitClass, Settings.MARK_DISTANCE,
                           DefaultSettings.defaultMarkDistance);

        int maxMarkDistance = SettingsUtil.getInteger(kitClass, Settings.MAX_MARK_DISTANCE,
                              DefaultSettings.defaultMaxMarkDistance);

        int minMarkDistance = SettingsUtil.getInteger(kitClass, Settings.MIN_MARK_DISTANCE,
                              DefaultSettings.defaultMinMarkDistance);

        int syntaxUpdateBatchSize = SettingsUtil.getInteger(kitClass, Settings.SYNTAX_UPDATE_BATCH_SIZE,
                                    DefaultSettings.defaultSyntaxUpdateBatchSize);


        // Now perform checks
        if (maxMarkDistance < markDistance) {
            throw new Error("maxMarkDistance=" + maxMarkDistance // NOI18N
                            + " < markDistance=" + markDistance); // NOI18N
        }

        if (markDistance < minMarkDistance) {
            throw new Error("markDistance=" + markDistance // NOI18N
                            + " < minMarkDistance=" + minMarkDistance); // NOI18N
        }

        if (readMarkDistance < minMarkDistance) {
            throw new Error("readMarkDistance=" + readMarkDistance // NOI18N
                            + " < minMarkDistance=" + minMarkDistance); // NOI18N
        }

        if (syntaxUpdateBatchSize < maxMarkDistance) {
            throw new Error("syntaxUpdateBatchSize=" + syntaxUpdateBatchSize // NOI18N
                            + " < maxMarkDistance=" + maxMarkDistance); // NOI18N
        }

    }

    /** Replace '\n', '\r' and '\t' in the string so they are identifiable. */
    public static String debugString(String s) {
        return debugChars(s.toCharArray(), 0, s.length());
    }

    public static String debugChars(Segment seg) {
        return debugChars(seg.array, seg.offset, seg.count);
    }

    public static String debugChars(char chars[]) {
        return debugChars(chars, 0, chars.length);
    }

    /** Replace '\n', '\r' and '\t' in the char array so they are identifiable. */
    public static String debugChars(char chars[], int offset, int len) {
        if (len < 0) {
            return "EditorDebug.debugChars() !ERROR! len=" + len + " < 0"; // NOI18N
        }
        if (offset < 0) {
            return "EditorDebug.debugChars() !ERROR! offset=" + offset + " < 0"; // NOI18N
        }
        if (offset + len > chars.length) {
            return "EditorDebug.debugChars() !ERROR! offset=" + offset + " + len=" + len // NOI18N
                   + " > chars.length=" + chars.length; // NOI18N
        }
        StringBuffer sb = new StringBuffer(len);
        int endOffset = offset + len;
        for (; offset < endOffset; offset++) {
            switch (chars[offset]) {
            case '\n':
                sb.append("\\n"); // NOI18N
                break;
            case '\t':
                sb.append("\\t"); // NOI18N
                break;
            case '\r':
                sb.append("\\r"); // NOI18N
                break;
            default:
                sb.append(chars[offset]);
            }
        }
        return sb.toString();
    }

    public static String debugPairs(int[] pairs) {
        String ret;
        if (pairs == null) {
            ret = "Null pairs"; // NOI18N
        } else if (pairs.length == 0) {
            ret = "No pairs"; // NOI18N
        } else {
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < pairs.length; i += 2) {
                sb.append('[');
                sb.append(pairs[i]);
                sb.append(", "); // NOI18N
                sb.append(pairs[i + 1]);
                if (i < pairs.length - 1) {
                    sb.append("]\n"); // NOI18N
                }
            }
            ret = sb.toString();
        }

        return ret;
    }

    public static String debugArray(Object[] array) {
        String ret;
        if (array == null) {
            ret = "Null array"; // NOI18N
        } else if (array.length == 0) {
            ret = "Empty array"; // NOI18N
        } else {
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < array.length; i++) {
                sb.append('[');
                sb.append(i);
                sb.append("]="); // NOI18N
                sb.append(array[i]);
                if (i != array.length - 1) {
                    sb.append('\n');
                }
            }
            ret = sb.toString();
        }
        return ret;
    }

    public static String  debugArray(int[] array) {
        String ret;
        if (array == null) {
            ret = "Null array"; // NOI18N
        } else if (array.length == 0) {
            ret = "Empty array"; // NOI18N
        } else {
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < array.length; i++) {
                sb.append('[');
                sb.append(i);
                sb.append("]="); // NOI18N
                sb.append(array[i]);
                if (i != array.length - 1) {
                    sb.append('\n');
                }
            }
            ret = sb.toString();
        }
        return ret;
    }

    public static String debugBlocks(BaseDocument doc, int[] blocks) {
        String ret;
        if (blocks == null) {
            ret = "Null blocks"; // NOI18N
        } else if (blocks.length == 0) {
            ret = "Empty blocks"; // NOI18N
        } else if (blocks.length % 2 != 0) {
            ret = "Blocks.length=" + blocks.length + " is not even!"; // NOI18N
        } else {
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < blocks.length; i += 2) {
                sb.append('[');
                sb.append(i);
                sb.append("]=("); // NOI18N
                sb.append(blocks[i]);
                sb.append(", "); // NOI18N
                sb.append(blocks[i + 1]);
                sb.append(") or ("); // NOI18N
                sb.append(Utilities.debugPosition(doc, blocks[i]));
                sb.append(", "); // NOI18N
                sb.append(Utilities.debugPosition(doc, blocks[i + 1]));
                sb.append(')');

                if (i != blocks.length - 1) {
                    sb.append('\n');
                }
            }
            ret = sb.toString();
        }
        return ret;
    }

    public static String debugList(List l) {
        String ret;
        if (l == null) {
            ret = "Null list"; // NOI18N
        } else if (l.size() == 0) {
            ret = "Empty list"; // NOI18N
        } else {
            int cnt = l.size();
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < cnt; i++) {
                sb.append('[');
                sb.append(i);
                sb.append("]="); // NOI18N
                sb.append(l.get(i));
                if (i != cnt - 1) {
                    sb.append('\n');
                }
            }
            ret = sb.toString();
        }
        return ret;
    }

    public static String debugIterator(Iterator i) {
        String ret;
        if (i == null) {
            ret = "Null iterator"; // NOI18N
        } else if (!i.hasNext()) {
            ret = "Empty iterator"; // NOI18N
        } else {
            StringBuffer sb = new StringBuffer();
            int ind = 0;
            while (i.hasNext()) {
                sb.append('[');
                sb.append(ind++);
                sb.append("]="); // NOI18N
                sb.append(i.next().toString());
                if (i.hasNext()) {
                    sb.append('\n');
                }
            }
            ret = sb.toString();
        }
        return ret;
    }

}

/*
 * Log
 *  8    Gandalf-post-FCS1.6.1.0     3/8/00   Miloslav Metelka 
 *  7    Gandalf   1.6         1/13/00  Miloslav Metelka 
 *  6    Gandalf   1.5         12/28/99 Miloslav Metelka 
 *  5    Gandalf   1.4         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  4    Gandalf   1.3         10/10/99 Miloslav Metelka 
 *  3    Gandalf   1.2         9/16/99  Miloslav Metelka 
 *  2    Gandalf   1.1         9/15/99  Miloslav Metelka 
 *  1    Gandalf   1.0         9/10/99  Miloslav Metelka 
 * $
 */

