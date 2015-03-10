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

package org.netbeans.core;

import java.awt.datatransfer.*;
import java.awt.event.*;
import java.awt.Window;
import java.io.IOException;
import javax.swing.Timer;
import java.util.ArrayList;

import org.openide.util.datatransfer.ExClipboard;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.TopManager;

/** Clipboard workaround about the bug in the Windows system clipboard.
* This clipboard maintains connection to the system clipboard and
* synchronizes with its content. If a flavour is inserted to the clipboard
* that is not accepted by the system one, this clipboard held a reference
* to it.
*
* @author Jaroslav Tulach, Petr Hamernik
* @version 0.19, Jun 08, 1998
*/
public class CoronaClipboard extends ExClipboard {
    /** how often to test the change */
    private static final int TIME = 3000;

    /** empty array of flavors */
    private static final DataFlavor[] NONE = new DataFlavor[0];

    /** a set of all convertors */
    private static ArrayList convertors = new ArrayList ();

    /** array of convertors */
    private static Convertor[] array;

    /** second clipboard */
    private Clipboard nextClipboard;
    /** flavors of the old clipboard */
    private DataFlavor[] flavors;

    /** original transferable */
    private Transferable orignalTransferable;

    /**
    * Initializes this clipboard to work with system one
    * @param name name of clipboard
    */
    public CoronaClipboard (String name) {
        this (name, java.awt.Toolkit.getDefaultToolkit ().getSystemClipboard ());
    }

    /**
    * Initializes this clipboard to work with the parameter.
    * @param name name of clipboard
    * @param clipboard the second clipboard
    */
    public CoronaClipboard (String name, Clipboard clipboard) {
        super (name);
        nextClipboard = clipboard;

        // XXX(-trung) getTransferDataFlavors() causes deadlock on JDK 1.3 beta
        // Linux and Solaris.  See refreshClipboard()
        if (Utilities.isUnix())
            return;
        // XXX(-trung)
        
        Timer t = new Timer (TIME, new ActionListener () {
                                 public void actionPerformed (ActionEvent ev) {
                                     refreshClipboard ();
                                 }
                             });
        t.setRepeats (true);
        t.start ();
        refreshClipboard ();
    }

    /** Changes the content of transferable also notifies the previous one,
    * that is lost ownership of clipboard.
    */
    synchronized final void changeTransferable (Transferable t) {
        if (orignalTransferable != null) {
            transferableOwnershipLost (orignalTransferable);
        }
        orignalTransferable = t;
    }

    /** Refresh the contents of the external clipboard with
    * the same content.  This method sets
    * the contents of external clipboard and waits for a change
    * from outside application. As soon as the change occurs the
    * <CODE>lostOwnership()</CODE> call occurs which causes a new
    * call to this method. So all the changes to external clipboard
    * should be catched except the change after setting contents to null.
    * <CODE>refreshLost</CODE> flag handles this situation.
    *
    * @return change event has been fired or false if not
    */
    synchronized boolean refreshClipboard() {
        // XXX(-trung) getTransferDataFlavors() causes deadlock on JDK 1.3 beta
        // Linux and Solaris
        if (Utilities.isUnix())
            return false;
        // XXX(-trung)
        
        Transferable tran = nextClipboard.getContents(this);
        if (Utilities.isUnix ()) {
            tran = unixString (tran);
        }
        //    print (tran);
        try {
            DataFlavor[] arr = tran.getTransferDataFlavors ();

            if (arr == null) {
                arr = NONE;
            }

            if (!java.util.Arrays.equals (arr, flavors)) {
                // changed flavors
                flavors = arr;
                changeTransferable (null);
                fireClipboardChange ();
                return true;
            }
        } catch (NullPointerException e) {
        }

        return false;
    }

    /** Sets the content to this clipboard and also to the second one.
    * Registers to receive notification about lost of ownership and reacts
    * to such action by clearing content of this clipboard.
    */
    public synchronized void setContents (
        Transferable contents, ClipboardOwner owner
    ) {
        changeTransferable (contents);

        nextClipboard.setContents (convert (contents), owner);
        if (!refreshClipboard ()) {
            fireClipboardChange ();
        }
    }

    /** Getter that delegates to the provided clipboard.
    */
    public synchronized Transferable getContents (Object requestor) {
        Transferable t = nextClipboard.getContents (requestor);
        t = convert (t);
        if (Utilities.isUnix ()) {
            // create unix wraper to work on solaris
            return unixString (t);
        } else {
            return t;
        }
    }

    /** Current set of convertors */
    protected Convertor[] getConvertors () {
        return computeConvertors ();
    }
    
    /** test for JDK 1.3 */
   private static boolean jdk13 = System.getProperty ("java.vm.version").startsWith ("1.3"); // NOI18N

    /** Test for string on Unix machines (solaris especially)
    */
    private Transferable unixString (Transferable t) {
        if (jdk13) {
            // do not try to run this code on jdk 1.3, because the implementation of 
            // clipboard access is better
            return t;
        }
        
        DataFlavor[] df = t.getTransferDataFlavors ();

        if (df == null || df.length == 0) {
            final Thread[] th = new Thread[] { Thread.currentThread () };
            // test for string but prevent from being deadlocked
            try {
                try {
                    class Run extends Exception implements Runnable {
                        public void run () {
                            synchronized (th) {
                                // synchronized on th so we are sure that the
                                // the original thread is either before section
                                // marked as XXX or after it
                                Thread x = th[0];
                                if (x != null) {
                                    ClipboardDeadlockException ex = new ClipboardDeadlockException ();
//                                    TopManager.getDefault ().getErrorManager ().annotate (ex, this);
                                    x.stop (ex);
                                    x.interrupt ();
                                }
                            }
                        }
                    }
                    
                    RequestProcessor.postRequest (new Run (), 100);

                    String o = (String)t.getTransferData (DataFlavor.stringFlavor);
                    th[0] = null;

                    StringSelection ss = new StringSelection (o);
                    super.setContents (ss, ss);
                    changeTransferable (null);

                    return ss;
                } catch (Exception e) {
                    // consume any exception,
                } finally {
                    synchronized (th) {
                        // clear the thread not to be stopped again
                        th[0] = null;
                    }
                }
            } catch (ClipboardDeadlockException ex) {
                // be sure
            }
        }

        // otherwise use original
        return t;
    }

    /** Adds new convertor to the clipboard.
    */
    static synchronized void addConvertor (Convertor c) {
        array = null;
        convertors.add (c);
    }

    /** Removes a convertor from the clipboard.
    */
    static synchronized void removeConvertor (Convertor c) {
        array = null;
        convertors.remove (c);
    }

    /** Computes all convertors.
    */
    private static synchronized Convertor[] computeConvertors () {
        if (array == null) {
            array = new Convertor[convertors.size ()];
            convertors.toArray (array);
        }
        return array;
    }


    /*
    static void print (Transferable contents) {
    if (contents == null) {
     System.out.println ("null");
     return;
}

    System.out.println ("Content type: " + contents.getClass ().getName ());

    DataFlavor[] arr = contents.getTransferDataFlavors ();
    if (arr == null) {
     System.out.println ("null");
     return;
}

    for (int i = 0; i < arr.length; i++) {
     try {
       Object c = contents.getTransferData (arr[i]);
       if (c instanceof java.io.StringReader) {
         java.io.StringReader r = (java.io.StringReader)c;
         char[] cbuf = new char[1024];
         int l = r.read (cbuf);
         c = new String (cbuf, 0, l);
       }
       System.out.println (i + ". " + arr[i].getHumanPresentableName () + " content: " + c);
     } catch (Exception ex) {
       ex.printStackTrace();
     }
     }
}
    */


}

/*
 * Log
 *  13   Gandalf   1.12        1/18/00  Jaroslav Tulach Fires changes often.
 *  12   Gandalf   1.11        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  11   Gandalf   1.10        10/6/99  Jaroslav Tulach #3973
 *  10   Gandalf   1.9         6/30/99  Jaroslav Tulach Drag and drop support
 *  9    Gandalf   1.8         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  8    Gandalf   1.7         3/4/99   Jaroslav Tulach API cleaning
 *  7    Gandalf   1.6         3/2/99   Jaroslav Tulach 
 *  6    Gandalf   1.5         2/26/99  Jaroslav Tulach Deadlock enhancement.
 *  5    Gandalf   1.4         2/25/99  Jaroslav Tulach Change of clipboard 
 *       management  
 *  4    Gandalf   1.3         2/24/99  Jaroslav Tulach Convertors & clipboard 
 *       deadlock
 *  3    Gandalf   1.2         2/21/99  Jaroslav Tulach Synchronization of 
 *       source with X2
 *  2    Gandalf   1.1         2/11/99  Jaroslav Tulach Shares data with system 
 *       clipboard
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 * Beta Change History:
 *  0    Tuborg    0.17        --/--/98 Miloslav Metelk added getContents()
 *  0    Tuborg    0.18        --/--/98 Miloslav Metelk patched getContents()
 *  0    Tuborg    0.19        --/--/98 Jan Formanek    patch for Solaris
 */

