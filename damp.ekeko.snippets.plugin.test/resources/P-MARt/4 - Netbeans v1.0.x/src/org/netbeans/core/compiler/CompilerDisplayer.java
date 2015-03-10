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

package org.netbeans.core.compiler;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ResourceBundle;

import org.openide.loaders.DataObject;
import org.openide.text.Line;
import org.openide.TopManager;
import org.openide.compiler.*;
import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileObject;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputEvent;
import org.openide.windows.OutputListener;
import org.openide.windows.OutputWriter;
import org.openide.util.NbBundle;

/** This class is responsible for displaying the messages
* as a reactions to the state changes and errors produced by
* the compiler.
*
* @author Ales Novak, Ian Formanek, Petr Hamernik, Jaroslav Tulach
* @version 0.23, May 26, 1998
*/
final class CompilerDisplayer extends Object implements CompilerListener {
    /** output tab */
    private InputOutput compilerIO;
    /** writer to that tab */
    private OutputWriter ow = null;
    /** format for errors */
    private MessageFormat errorMsg;
    /** format for error description */
    private MessageFormat errorDescr;
    /** message for compiling */
    private MessageFormat compilingMsg;
    /** compilaton successful */
    private MessageFormat compSuccess;
    /** compilaton unsuccessful */
    private MessageFormat compUnsuccess;
    /** compilation started */
    private MessageFormat compStarted;
    /** compiling progress status messages */
    private static String parsing;
    private static String generating;
    private static String writing;
    private static String cleaning;

    /** flag to test whether the tab has been selected or not */
    private boolean notSelected = true;

    public CompilerDisplayer () {
        // bundle
        final ResourceBundle bundle = NbBundle.getBundle (CompilerDisplayer.class);
        if (parsing == null) {
            parsing = bundle.getString ("MSG_StatusParsing");
            writing = bundle.getString ("MSG_StatusWriting");
            generating = bundle.getString ("MSG_StatusGenerating");
            cleaning = bundle.getString ("MSG_StatusCleaning");
        }
        errorMsg = new MessageFormat (bundle.getString ("MSG_CompileError"));
        errorDescr = new MessageFormat (bundle.getString ("MSG_CompileErrorDescr"));
        compilingMsg = new MessageFormat (bundle.getString ("MSG_Compiling"));
        compSuccess = new MessageFormat (bundle.getString ("MSG_CompilationSuccessful"));
        compUnsuccess = new MessageFormat (bundle.getString ("MSG_CompilationUnsuccessful"));
        compStarted = new MessageFormat (bundle.getString ("MSG_CompilationStarted"));
    }

    /****** Implementation of the CompilerListener interface *******/

    /** Displayes status text as a reaction to the
    * notification of compilation progress.
    *
    * @param ev event that holds information about currently compiled object
    */
    public void compilerProgress (ProgressEvent ev) {
        // choose right message
        String status = null;
        switch (ev.getTask()) {
        case ProgressEvent.TASK_PARSING: status = parsing; break;
        case ProgressEvent.TASK_WRITING: status = writing; break;
        case ProgressEvent.TASK_GENERATING: status = generating; break;
        case ProgressEvent.TASK_CLEANING: status = cleaning; break;
        default: status = parsing; break;
        }
        if (status == null) return;
        Object[] args = new Object[] {
                            status,
                            ev.getFile().getPackageNameExt ('/', '.')
                        };
        String msg = compilingMsg.format (args);
        setStatusText (msg);
    }

    /** Displayes error line in output window as a reaction to the
    * notification that an error occured in the compiler.
    * @param ev event describing that error
    */
    public void compilerError (final ErrorEvent ev) {
        javax.swing.SwingUtilities.invokeLater (new Runnable () {
                                                    public void run () {
                                                        initialize ();

                                                        if (notSelected) {
                                                            notSelected = false;
                                                            compilerIO.select ();
                                                        }

                                                        if (ev.getFile() == null) {
                                                            String msg = ev.getMessage();
                                                            if (!msg.equals("")) { // NOI18N
                                                                if (msg.startsWith("\n")) // NOI18N
                                                                    msg = msg.substring(1);
                                                                println(msg);
                                                                ow.flush();
                                                            }
                                                            return;
                                                        }

                                                        Object[] args = new Object[] {
                                                                            ev.getFile ().getPackageNameExt ('/', '.'),
                                                                            new Integer (ev.getLine ()),
                                                                            new Integer (ev.getColumn ()),
                                                                            ev.getMessage ()
                                                                        };
                                                        String text = errorMsg.format (args);
                                                        try {
                                                            ErrorCtl ec = new ErrorCtl (
                                                                              ev.getFile(),
                                                                              Math.max(ev.getLine() - 1, 0),
                                                                              Math.max(ev.getColumn() - 1, 0),
                                                                              text
                                                                          );
                                                            println(text, ec);
                                                        } catch (IOException ex) {
                                                            println (text);
                                                        }
                                                        if (!ev.getReferenceText().equals("")) { // NOI18N
                                                            String refText = ev.getReferenceText();
                                                            if (refText.startsWith("\n")) // NOI18N
                                                                refText = refText.substring(1);
                                                            println (errorDescr.format (new Object[] { refText }));
                                                        }
                                                        // ow.println (""); // NOI18N
                                                    }
                                                });
    }

    /** Displayes information that compilation has just started. */
    void compilationStarted (final CompilerTaskImpl task) {
        initialize ();
        setStatusText(compStarted.format (
                          new Object[] { task.getDisplayName() }
                      ));
        try {
            ow.reset ();
            notSelected = true;
        } catch (java.io.IOException ex) {
            TopManager.getDefault().notifyException (ex);
        }
    }

    /** Displayes information that compilation has finished,
    * and whether succesfully or not. */
    public void compilationFinished (final CompilerTaskImpl task) {
        initialize ();
        MessageFormat msg = task.isSuccessful() ? compSuccess : compUnsuccess;
        setStatusText(msg.format (new Object[] { task.getDisplayName() }));
    }

    private void initialize () {
        if (ow == null) {
            synchronized (this) {
                // prepare output tab
                setOw (NbBundle.getBundle (CompilerDisplayer.class).getString ("CTL_CompileTab"));
            }
        }
    }

    private void setOw (String name) {
        if (ow != null) return;
        compilerIO = TopManager.getDefault().getIO(name);
        compilerIO.setFocusTaken (false);
        ow = compilerIO.getOut();
    }

    /** Sets text to status line
    */
    public void setStatusText (String text) {
        final String txt = oneLine(text);
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
                                                   public void run() {
                                                       TopManager.getDefault().setStatusText (txt);
                                                   }
                                               });
    }

    /** Removes newlines from the string */
    private static String oneLine(final String txt) {
        StringBuffer sb = new StringBuffer(txt.length());
        boolean lastIsNewline = false;
        for (int i = 0; i < txt.length(); i++) {
            char ch = txt.charAt(i);
            if (ch == '\n' || ch == '\r') {
                if (! lastIsNewline) {
                    lastIsNewline = true;
                    sb.append(' ');
                }
            } else {
                sb.append(ch);
                lastIsNewline = false;
            }
        }
        return sb.toString();
    }

    /** Prints the text */
    final void println(final String msg) {
        ow.println(msg);
    }

    /** Prints the text */
    final void println(final String msg, final ErrorCtl err) throws IOException {
        ow.println(msg, err);
    }


    final class ErrorCtl implements OutputListener {
        /** file we check */
        FileObject file;

        /** line we check */
        Line xline;

        /** column with the err */
        int column;

        /** text to display */
        private String text;

        /**
        * @param fo is a FileObject with an error
        * @param line is a line with the error
        * @param column is a column with the error
        * @param text text to display to status line
        * @exception FileNotFoundException
        */
        public ErrorCtl (FileObject fo, int line, int column, String text)
        throws java.io.IOException {
            file = fo;
            this.column = column;
            DataObject data = DataObject.find (file);
            LineCookie cookie = (LineCookie)data.getCookie(LineCookie.class);
            if (cookie == null) {
                throw new java.io.FileNotFoundException ();
            }
            xline = cookie.getLineSet ().getOriginal (line);
            this.text = text;
        }

        public void outputLineSelected (OutputEvent ev) {
            try {
                xline.markError();
                xline.show(Line.SHOW_TRY_SHOW, column);
            } catch (IndexOutOfBoundsException ex) {
            }
        }

        public void outputLineAction (OutputEvent ev) {
            try {
                xline.markError();
                xline.show(Line.SHOW_GOTO, column);
                setStatusText (text);
            } catch (IndexOutOfBoundsException ex) {
            }
        }

        public void outputLineCleared (OutputEvent ev) {
            try {
                xline.unmarkError();
            } catch (IndexOutOfBoundsException ex) {
            }
        }
    } // end of ErrorCtl inner class

}

/*
 * Log
 *  26   Gandalf   1.25        1/13/00  Jaroslav Tulach I18N
 *  25   Gandalf   1.24        1/12/00  Ales Novak      i18n
 *  24   Gandalf   1.23        1/5/00   Ales Novak      newlines not passed to 
 *       setStatusText
 *  23   Gandalf   1.22        12/9/99  Ales Novak      column fixed
 *  22   Gandalf   1.21        11/4/99  Ian Formanek    Removed obsoleted text
 *  21   Gandalf   1.20        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  20   Gandalf   1.19        10/6/99  Jaroslav Tulach ProgressEvent.TASK_CLEANING
 *       
 *  19   Gandalf   1.18        9/21/99  Petr Hamernik   some small improvements 
 *       (new lines printing...)
 *  18   Gandalf   1.17        7/30/99  Jaroslav Tulach getOriginal & getCurrent
 *       in LineSet
 *  17   Gandalf   1.16        7/29/99  Ales Novak      bugfix
 *  16   Gandalf   1.15        7/28/99  Ales Novak      part of bugfix #2826
 *  15   Gandalf   1.14        7/27/99  Jaroslav Tulach Faster lines.
 *  14   Gandalf   1.13        7/15/99  Petr Hamernik   optimization
 *  13   Gandalf   1.12        7/11/99  David Simonek   window system change...
 *  12   Gandalf   1.11        6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  11   Gandalf   1.10        5/15/99  Ales Novak      deadlock avoidance
 *  10   Gandalf   1.9         4/22/99  Ales Novak      compilation finished no 
 *       more written
 *  9    Gandalf   1.8         4/9/99   Ian Formanek    Compiler progress 
 *       notifications improved
 *  8    Gandalf   1.7         3/29/99  Jaroslav Tulach Successful compilation 
 *       does not open the output tab
 *  7    Gandalf   1.6         3/26/99  Ian Formanek    Fixed use of obsoleted 
 *       NbBundle.getBundle (this)
 *  6    Gandalf   1.5         3/18/99  Jaroslav Tulach 
 *  5    Gandalf   1.4         2/1/99   Jaroslav Tulach 
 *  4    Gandalf   1.3         1/27/99  Jaroslav Tulach 
 *  3    Gandalf   1.2         1/6/99   Jaroslav Tulach 
 *  2    Gandalf   1.1         1/6/99   Ian Formanek    Reflecting change in 
 *       datasystem package
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 * Beta Change History:
 *  0    Tuborg    0.12        --/--/98 Jan Formanek    reflecting changes in TopOutput
 *  0    Tuborg    0.13        --/--/98 Jan Formanek    reflecting changes in TopOutput #2
 *  0    Tuborg    0.14        --/--/98 Petr Hamernik   small change
 *  0    Tuborg    0.17        --/--/98 Jan Formanek    repaired to LineCookie
 *  0    Tuborg    0.18        --/--/98 Jan Formanek    (Petr  bugfix of repair 0.17
 *  0    Tuborg    0.19        --/--/98 Jan Jancura     Tab switching changed
 *  0    Tuborg    0.20        --/--/98 Petr Hamernik   unmark error
 *  0    Tuborg    0.21        --/--/98 Jaroslav Tulach localization
 *  0    Tuborg    0.22        --/--/98 Jaroslav Tulach -||-
 *  0    Tuborg    0.23        --/--/98 Jan Formanek    reflecting changes in cookies
 */
