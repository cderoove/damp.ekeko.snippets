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

package org.openide.text;

import java.lang.ref.WeakReference;
import java.awt.Color;
import java.awt.Component;
import java.beans.*;
import javax.swing.text.*;
import javax.swing.JEditorPane;

import org.openide.options.SystemOption;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/** Dummy class holding utility methods for working with NetBeans document conventions.
*
* @author Jaroslav Tulach
*/
public final class NbDocument extends Object {
    private NbDocument() {}

    /** Attribute that signals that a given character is guarded (cannot
    * be modified). Implements {@link AttributeSet.CharacterAttribute} to signal that
    * this attribute applies to characters, not paragraphs.
    */
    public static final Object GUARDED = new AttributeSet.CharacterAttribute () {};
    /** Attribute set that adds to a part of document guarded flag
    */
    private static final SimpleAttributeSet ATTR_ADD = new SimpleAttributeSet ();
    /** Attribute set to remove the guarded flag.
    */
    private static final SimpleAttributeSet ATTR_REMOVE = new SimpleAttributeSet ();

    static {
        ATTR_ADD.addAttribute(GUARDED, Boolean.TRUE);
        ATTR_REMOVE.addAttribute(GUARDED, Boolean.FALSE);
    }

    /** Common colors of breakpoint lines, erroneous lines, etc.
    */
    public static final Colors COLORS = new Colors ();

    /** Name of style attached to documents to mark a paragraph (line)
    * as a (debugger) breakpoint.
    */
    public static final String BREAKPOINT_STYLE_NAME = "NbBreakpointStyle"; // NOI18N

    /** Name of style attached to documents to mark a paragraph (line)
    * as erroneous.
    */
    public static final String ERROR_STYLE_NAME = "NbErrorStyle"; // NOI18N

    /** Name of style attached to documents to mark a paragraph (line)
    * as current (in a debugger).
    */
    public static final String CURRENT_STYLE_NAME = "NbCurrentStyle"; // NOI18N

    /** Name of style attached to documents to unmark a paragraph (line)
    * as anything special.
    */
    public static final String NORMAL_STYLE_NAME = "NbNormalStyle"; // NOI18N

    /** Find the root element of all lines.
    * All conforming NetBeans documents
    * should return a valid element.
    *
    * @param doc styled document (expecting NetBeans document)
    * @return the root element
    */
    public static Element findLineRootElement (StyledDocument doc) {
        Element e = doc.getParagraphElement (0).getParentElement ();
        if (e == null) {
            // try default root (should work for text/plain)
            e = doc.getDefaultRootElement ();
        }
        return e;
    }

    /** For given document and an offset, find the line number.
    * @param doc the document
    * @param offset offset in the document
    * @return the line number for that offset
    */
    public static int findLineNumber (StyledDocument doc, int offset) {
        Element paragraphsParent = findLineRootElement (doc);
        return paragraphsParent.getElementIndex (offset);
    }

    /** Finds column number given an offset.
    * @param doc the document
    * @param offset offset in the document
    * @return column within the line of that offset (counting starts from zero)
    */
    public static int findLineColumn (StyledDocument doc, int offset) {
        Element paragraphsParent = findLineRootElement (doc);
        int indx = paragraphsParent.getElementIndex (offset);
        return offset - paragraphsParent.getElement (indx).getStartOffset ();
    }

    /** Finds offset of the beginning of a line.
    * @param doc the document
    * @param line number of the line to find the start of
    * @return offset
    */
    public static int findLineOffset (StyledDocument doc, int lineNumber) {
        Element paragraphsParent = findLineRootElement (doc);
        return paragraphsParent.getElement (lineNumber).getStartOffset ();
    }

    /** Creates position with a bias. If the bias is {@link Position.Bias#Backward}
    * then if an insert occures at the position, the text is inserted
    * after the position. If the bias is {@link Position.Bias#Forward <code>Forward</code>}, then the text is
    * inserted before the position.
    * <P>
    * The method checks if the document implements {@link PositionBiasable},
    * and if so, {@link PositionBiasable#createPosition <code>createPosition</code>} is called.
    * Otherwise an attempt is made to provide a <code>Position</code> with the correct behavior.
    *
    * @param doc document to create position in
    * @param offset the current offset for the position
    * @param bias the bias to use for the position
    * @exception BadLocationException if the offset is invalid
    */
    public static Position createPosition (
        Document doc, int offset, Position.Bias bias
    ) throws BadLocationException {
        if (doc instanceof PositionBiasable) {
            return ((PositionBiasable)doc).createPosition (offset, bias);
        } else {
            if (bias == Position.Bias.Forward) {
                // default behaviour
                return doc.createPosition (offset);
            } else {
                // use our special position
                return BackwardPosition.create (doc, offset);
            }
        }
    }

    /** Mark part of a document as guarded (immutable to the user).
    * @param doc styled document
    * @param offset offset to start at
    * @param len length of text to mark as guarded
    */
    public static void markGuarded (StyledDocument doc, int offset, int len) {
        doc.setCharacterAttributes (offset, len, ATTR_ADD, false);
    }

    /** Remove guarded mark on a block of a document.
    * @param doc styled document
    * @param offset offset to start at
    * @param len length of text to mark as unguarded
    */
    public static void unmarkGuarded (StyledDocument doc, int offset, int len) {
        doc.setCharacterAttributes (offset, len, ATTR_REMOVE, false);
    }

    /** Inserts a text into given offset and marks it guarded.
    * @param doc document to insert to
    * @param offset offset of insertion
    * @param txt string text to insert
    */
    public static void insertGuarded (StyledDocument doc, int offset, String txt) throws BadLocationException {
        doc.insertString (offset, txt, ATTR_ADD);
    }

    /** Attach a breakpoint to a line in the document.
    * If the document has a defined style named {@link #BREAKPOINT_STYLE_NAME}, it is used.
    * Otherwise, a new style is defined.
    *
    * @param doc the document
    * @param offset identifies the line to set breakpoint to
    */
    public static void markBreakpoint (StyledDocument doc, int offset) {
        Style bp = doc.getStyle (BREAKPOINT_STYLE_NAME);
        if (bp == null) {
            // create the style
            bp = doc.addStyle (BREAKPOINT_STYLE_NAME, null);
            if (bp == null) return;

            ColorListener.createListener (bp, COLORS.getBreakpoint (), Color.white, BREAKPOINT_STYLE_NAME);
        }

        doc.setLogicalStyle (offset, bp);
    }


    /** Mark a line as erroneous (e.g.&nbsp;by the compiler).
    * If the document has a defined style named {@link #ERROR_STYLE_NAME}, it is used.
    * Otherwise, a new style is defined.
    *
    * @param doc the document
    * @param offset identifies the line to mark
    */
    public static void markError (StyledDocument doc, int offset) {
        Style bp = doc.getStyle (ERROR_STYLE_NAME);
        if (bp == null) {
            // create the style
            bp = doc.addStyle (ERROR_STYLE_NAME, null);
            if (bp == null) return;

            ColorListener.createListener (bp, COLORS.getError (), Color.white, ERROR_STYLE_NAME);
        }

        doc.setLogicalStyle (offset, bp);
    }

    /** Marks a line as current (e.g.&nbsp;for the debugger).
    * If the document has a defined style named {@link #CURRENT_STYLE_NAME}, it is used.
    * Otherwise, a new style is defined.
    *
    * @param doc the document
    * @param offset identifies the line to mark
    */
    public static void markCurrent (StyledDocument doc, int offset) {
        Style bp = doc.getStyle (CURRENT_STYLE_NAME);
        if (bp == null) {
            // create the style
            bp = doc.addStyle (CURRENT_STYLE_NAME, null);
            if (bp == null) return;

            ColorListener.createListener (bp, COLORS.getCurrent (), Color.white, CURRENT_STYLE_NAME);
        }

        doc.setLogicalStyle (offset, bp);
    }

    /**
     * Mark a line as normal (no special attributes).
     * This uses the dummy style named {@link #NORMAL_STYLE_NAME}.
     * This method should be used to undo the effect of {@link #markBreakpoint}, {@link #markError} and {@link #markCurrent}.
     * @param doc the document
     * @param offset identified the line to unmark
     */
    public static void markNormal (StyledDocument doc, int offset) {
        Style st = doc.getStyle (NORMAL_STYLE_NAME);
        if (st == null)
            st = doc.addStyle (NORMAL_STYLE_NAME, null);

        if (st != null) {
            doc.setLogicalStyle (offset, st);
        }
    }

    /** Locks the document to have exclusive access to it.
     * Documents implementing {@link Lockable} can specify exactly how to do this.
    *
    * @param doc document to lock
    * @param run the action to run
    */
    public static void runAtomic (StyledDocument doc, Runnable run) {
        if (doc instanceof WriteLockable) {
            // use the method
            ((WriteLockable)doc).runAtomic (run);
        } else {
            // transfer the runnable to event dispatch thread
            synchronized (doc) {
                run.run ();
            }
        }
    }

    /** Executes given runnable in "user mode" does not allowing any modifications
    * to parts of text marked as guarded. The actions should be run as "atomic" so
    * either happen all at once or none at all (if a guarded block should be modified).
    *
    * @param doc document to modify
    * @param run runnable to run in user mode that will have exclusive access to modify the document
    * @exception BadLocationException if a modification of guarded text occured
    *   and that is why no changes to the document has been done.
    */
    public static void runAtomicAsUser (StyledDocument doc, Runnable run) throws BadLocationException {
        if (doc instanceof WriteLockable) {
            // use the method
            ((WriteLockable)doc).runAtomicAsUser (run);
        } else {
            // transfer the runnable to event dispatch thread
            synchronized (doc) {
                run.run ();
            }
        }
    }

    /** Find a way to print a given document.
     * If the document implements the correct interface(s) then the document is returned,
     * else {@link DefaultPrintable} is used as a wrapper. In this last case it is useful
     * to implement {@link NbDocument.Printable} to describe how to print in terms of
     * attributed characters, rather than specifying the complete page layout from scratch.
     *
     * @param doc the document to find printing support for
     * @return an object that is instance of eith {@link java.awt.print.Printable} or {@link java.awt.print.Pageable}
     */
    public static Object findPageable(StyledDocument doc) {
        if (doc instanceof java.awt.print.Pageable) {
            return doc;
        } else if (doc instanceof java.awt.print.Printable) {
            return doc;
        } else {
            return new DefaultPrintable(doc);
        }
    }

    /** Class that holds all colors used in the editor for OpenIDE-specific purposes.
    * It includes colors for breakpoints, current lines, etc.
    */
    public static final class Colors extends SystemOption {
        /** Property name for breakpoint color. */
        public static final String PROP_BREAKPOINT = BREAKPOINT_STYLE_NAME;
        /** Property name for erroneous line color. */
        public static final String PROP_ERROR = ERROR_STYLE_NAME;
        /** Property name for current line color. */
        public static final String PROP_CURRENT = CURRENT_STYLE_NAME;

        /** Color for breakpoints. */
        private static Color breakpoint = new Color (127, 127, 255);

        /** error line */
        private static Color error = Color.red;

        /** current line color */
        private static Color current = Color.magenta;


        /* @return display name
        */
        static final long serialVersionUID =-9152250591365746193L;
        public String displayName () {
            return NbBundle.getBundle (NbDocument.class).getString ("MSG_COLORS");
        }

        public HelpCtx getHelpCtx () {
            return new HelpCtx (Colors.class);
        }

        /** Set breakpoint color. This changes color in all editors that
        * react to {@link #BREAKPOINT_STYLE_NAME} style.
        * @param c new color
        */
        public void setBreakpoint (Color c) {
            Color old = breakpoint;
            breakpoint = c;
            firePropertyChange (PROP_BREAKPOINT, old, c);
        }

        /** Get breakpoint color.
         * @return the color
        */
        public Color getBreakpoint () {
            return breakpoint;
        }

        /** Set erroneous line color. This changes color in all editors that
        * reacts to {@link #ERROR_STYLE_NAME} style.
        * @param c new color
        */
        public void setError (Color c) {
            Color old = error;
            error = c;
            firePropertyChange (PROP_ERROR, old, c);
        }

        /** Get color of erroneous lines.
         * @return the color
        */
        public Color getError () {
            return error;
        }

        /** Set current line color. This changes color in all editors that
        * reacts to {@link #CURRENT_STYLE_NAME} style.
        * @param c new color
        */
        public void setCurrent (Color c) {
            Color old = current;
            current = c;
            firePropertyChange (PROP_CURRENT, old, c);
        }

        /** Get current line color.
         * @return the color
        */
        public Color getCurrent () {
            return current;
        }
    }

    /** Listener on change of color. When it happens the
    * associated style's background attribute is modified.
    */
    private static final class ColorListener implements PropertyChangeListener {
        /** Style to watch over */
        private WeakReference style;

        /** property to watch for */
        private String propName;

        /**
        * @param s the style
        * @param c the color to assign
        * @param n property to watch changes of
        */
        public static void createListener (Style s, Color backgroundColor, Color foregroundColor, String n) {
            s.addAttribute (
                StyleConstants.ColorConstants.Background, backgroundColor
            );
            s.addAttribute (
                StyleConstants.ColorConstants.Foreground, foregroundColor
            );
            COLORS.addPropertyChangeListener (new ColorListener (s, n));
        }

        /**
        * @param style the style to set the color to
        * @param color current color
        */
        private ColorListener (Style style, String n) {
            this.style = new WeakReference (style);
            propName = n;
        }

        /** Changes the color in the style.
        */
        public void propertyChange (PropertyChangeEvent ev) {
            if (propName.equals (ev.getPropertyName ())) {
                Style s = (Style)style.get ();
                if (s == null) {
                    // deregister
                    COLORS.removePropertyChangeListener (this);
                    return;
                }
                s.addAttribute (
                    StyleConstants.ColorConstants.Background, (Color)ev.getNewValue ()
                );
            }
        }
    }

    /** Specialized version of document that knows how to lock the document
    * for complex modifications.
    */
    public interface WriteLockable extends Document {
        /** Executes given runnable in lock mode of the document.
        * In this mode, all redrawing is stopped and the caller has exclusive
        * access to all modifications to the document.
        * <P>
        * By definition there should be only one locker at a time. Sample implementation, if you are extending {@link AbstractDocument}:
        *
        * <p><code>
        * writeLock();<br>
        * try {<br>
        * &nbsp;&nbsp;r.run();<br>
        * } finally {<br>
        * &nbsp;&nbsp;writeUnlock();<br>
        * }
        * </code>
        *
        * @param run runnable to run while locked
        *
        * @see NbDocument#runAtomic
        */
        public void runAtomic (Runnable r);

        /** Executes given runnable in "user mode" does not allowing any modifications
        * to parts of text marked as guarded. The actions should be run as "atomic" so
        * either happen all at once or none at all (if a guarded block should be modified).
        *
        * @param run runnable to run in user mode that will have exclusive access to modify the document
        * @exception BadLocationException if a modification of guarded text occured
        *   and that is why no changes to the document has been done.
        */
        public void runAtomicAsUser (Runnable r) throws BadLocationException;
    }

    /** Document which may support styled text printing.
    * Any document that wishes to support special formatting while printing
    * can implement this interface and provide a <code>AttributedCharacterIterator</code>
    * specifying colors, fonts, etc.
    */
    public interface Printable extends Document {
        /** Get an attributed character iterator for the document, so that it may be printed.
         * <p>For a convenient way to do this, you may use {@link AttributedCharacters#iterator a simple implementation}
         * of an
         * attributed character list.
        * @return list of <code>AttributedCharacterIterator</code>s to be used for printing
        *
        * @see NbDocument#findPageable */
        public java.text.AttributedCharacterIterator[] createPrintIterators();
    }

    /** Enhanced version of document that provides better support for
    * holding and working with biased positions. It adds one new method
    * {@link #createPosition} that creates
    * a position that moves either to the left or to the right when an insertion
    * is performed at it.
    * <P>
    * If a document implements this interface, the new method is
    * used in {@link NbDocument#createPosition}.
    * If not, special support for the position is created.
    */
    public interface PositionBiasable extends Document {
        /** Creates position with a bias. If the bias is {@link Position.Bias#Backward}
        * then if an insert occures at the position, the text is inserted
        * after the position. If the bias is {@link Position.Bias#Forward <code>Forward</code>}, then the text is
        * inserted before the position.
        *
        * @param offset the offset for the position
        * @param bias the bias to use for the position
        * @exception BadLocationException if the offset is invalid
        *
        * @see NbDocument#createPosition
        */
        public Position createPosition (int offset, Position.Bias bias)
        throws BadLocationException;
    }

    /** Enabled documents to add special UI components to their Editor pane.
    * If this interface is implemented by the Editor document, it can be used
    * to add other components (such as toolbars) to the pane.
    */
    public interface CustomEditor extends Document {
        /** Create a whole editor component over the given <code>JEditorPane</code>.
         * The implementation should generally add some kind of scrolling
         * support to the given <code>JEditorPane</code> (typically with scrollbars),
         * possibly some other components
         * according to the desired layout,
         * and return the resulting container.
         * @param j editor pane over which the resulting component
         *   will be built
         * @return component encapsulating the pane and all other
         *   custom UI components
         */
        public Component createEditor(JEditorPane j);
    }

}


/*
* Log
*  35   src-jtulach1.34        1/13/00  Ian Formanek    NOI18N
*  34   src-jtulach1.33        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  33   src-jtulach1.32        10/7/99  Miloslav Metelka foreground color in 
*       styles
*  32   src-jtulach1.31        9/25/99  Jaroslav Tulach runAtomic on document not
*       supporting it directly does not use invokeAndWait but simply synchronize
*       on the document.
*  31   src-jtulach1.30        8/17/99  Ian Formanek    Generated serial version 
*       UID
*  30   src-jtulach1.29        7/27/99  Miloslav Metelka Colors updated
*  29   src-jtulach1.28        7/2/99   Jesse Glick     Help IDs for system 
*       options.
*  28   src-jtulach1.27        6/9/99   Ian Formanek    manifest tags changed to 
*       NetBeans-
*  27   src-jtulach1.26        6/8/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  26   src-jtulach1.25        6/4/99   Ales Novak      # 1970
*  25   src-jtulach1.24        5/11/99  Ales Novak      new constant adde + only 
*       constructor for DefaultPrintable used
*  24   src-jtulach1.23        4/21/99  Jesse Glick     [JavaDoc]
*  23   src-jtulach1.22        4/21/99  Miloslav Metelka Added CustomEditor 
*       handling
*  22   src-jtulach1.21        4/21/99  Ales Novak      changes to printing
*  21   src-jtulach1.20        4/9/99   David Simonek   bugfix #1429
*  20   src-jtulach1.19        3/26/99  Ian Formanek    Fixed use of obsoleted 
*       NbBundle.getBundle (this)
*  19   src-jtulach1.18        3/19/99  Petr Hamernik   bugfix
*  18   src-jtulach1.17        3/17/99  Jaroslav Tulach Output Window fixing.
*  17   src-jtulach1.16        3/15/99  Jesse Glick     Utility classes ought not
*       have public constructors.
*  16   src-jtulach1.15        2/26/99  Jesse Glick     [JavaDoc]
*  15   src-jtulach1.14        2/26/99  Jesse Glick     Added markNormal() to 
*       undo effects of others.  invokeSafe() is private.
*  14   src-jtulach1.13        2/24/99  Jesse Glick     [JavaDoc]
*  13   src-jtulach1.12        2/19/99  Petr Hamernik   
*  12   src-jtulach1.11        2/19/99  Jaroslav Tulach 
*  11   src-jtulach1.10        2/19/99  Jaroslav Tulach runAtomicAsUser
*  10   src-jtulach1.9         2/19/99  Ales Novak      
*  9    src-jtulach1.8         2/15/99  Jesse Glick     [JavaDoc]
*  8    src-jtulach1.7         2/12/99  Jaroslav Tulach New interfaces for 
*       printing and locking
*  7    src-jtulach1.6         2/11/99  Jesse Glick     Just notes: I think there
*       is code missing to init attribute sets.
*  6    src-jtulach1.5         2/10/99  Jesse Glick     [JavaDoc]
*  5    src-jtulach1.4         2/10/99  Jesse Glick     [JavaDoc]
*  4    src-jtulach1.3         2/8/99   Jesse Glick     [JavaDoc]
*  3    src-jtulach1.2         1/29/99  Jaroslav Tulach 
*  2    src-jtulach1.1         1/28/99  Jaroslav Tulach 
*  1    src-jtulach1.0         1/28/99  Jaroslav Tulach 
* $
*/
