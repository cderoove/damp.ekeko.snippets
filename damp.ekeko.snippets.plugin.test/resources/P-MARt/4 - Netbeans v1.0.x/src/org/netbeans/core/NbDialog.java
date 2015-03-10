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

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import javax.swing.*;

import org.openide.NotifyDescriptor;
import org.openide.DialogDescriptor;
import org.openide.TopManager;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/** Default implementation of Dialog created from DialogDescriptor.
*
* @author Ian Formanek
*/
final class NbDialog extends NbPresenter {
    static final long serialVersionUID =-4508637164126678997L;

    /** Creates a new Dialog from specified DialogDescriptor
    * @param d The DialogDescriptor to create the dialog from
    * @param owner Owner of this dialog.
    */
    public NbDialog (DialogDescriptor d, Frame owner) {
        super (d, owner, d.isModal ());
    }

    /** Creates a new Dialog from specified DialogDescriptor
    * @param d The DialogDescriptor to create the dialog from
    * @param owner Owner of this dialog.
    */
    public NbDialog (DialogDescriptor d, Dialog owner) {
        super (d, owner, d.isModal ());
    }

    /** Geter for help.
    */
    protected HelpCtx getHelpCtx () {
        return ((DialogDescriptor)descriptor).getHelpCtx ();
    }

    /** Options align.
    */
    protected int getOptionsAlign () {
        return ((DialogDescriptor)descriptor).getOptionsAlign ();
    }

    /** Getter for button listener or null
    */
    protected ActionListener getButtonListener () {
        return ((DialogDescriptor)descriptor).getButtonListener ();
    }

    /** Closing options.
    */
    protected Object[] getClosingOptions () {
        return ((DialogDescriptor)descriptor).getClosingOptions ();
    }

}

/*
* Log
*  31   Gandalf   1.30        1/18/00  Jaroslav Tulach Solves deadlock on 
*       Solaris 1.2.1
*  30   Gandalf   1.29        1/7/00   David Simonek   better settings of dialog
*       owners
*  29   Gandalf   1.28        12/30/99 Jaroslav Tulach New dialog for 
*       notification of exceptions.
*  28   Gandalf   1.27        12/15/99 Jesse Glick     There is a now a Help 
*       button automatically added to any dialog (though not notification 
*       dialogs) which provides help either explicitly or on its inner 
*       component.
*  27   Gandalf   1.26        12/6/99  Jaroslav Tulach Survives buttons == null
*  26   Gandalf   1.25        11/25/99 Libor Kramolis  
*  25   Gandalf   1.24        11/25/99 Jaroslav Tulach Default button has to be 
*       enabled, visible and capable.
*  24   Gandalf   1.23        11/24/99 Jaroslav Tulach New "New From Template" 
*       Dialog
*  23   Gandalf   1.22        11/4/99  Jaroslav Tulach DialogDescriptor.setClosingOptions
*        
*  22   Gandalf   1.21        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  21   Gandalf   1.20        9/23/99  Jaroslav Tulach #3719
*  20   Gandalf   1.19        9/15/99  Jaroslav Tulach NbDialog displays 
*       additonal options even standard options are specified.
*  19   Gandalf   1.18        8/9/99   Ian Formanek    Generated Serial Version 
*       UID
*  18   Gandalf   1.17        7/16/99  Jesse Glick     Uses HelpAction rather 
*       than trying to install its own help handler.
*  17   Gandalf   1.16        7/8/99   Jesse Glick     Added ability to display 
*       focus-based context help. This is necessary for e.g. multi-way custom 
*       propeds used in the new Form Editor, where context help must depend on 
*       activated piece.
*  16   Gandalf   1.15        7/7/99   Jesse Glick     
*  15   Gandalf   1.14        6/9/99   Ian Formanek    Temporarily removed 
*       handling Enter key as it works incorrectly (in input lines/editor panes 
*       it closes the dialog)
*  14   Gandalf   1.13        6/9/99   Ian Formanek    Fixed problem with 
*       ActionCommand when Enter key is pressed
*  13   Gandalf   1.12        6/8/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  12   Gandalf   1.11        6/8/99   Ian Formanek    Fixed problem when 
*       standard options were passed as part of options[] (displayed number as 
*       the button caption instead of the text)
*  11   Gandalf   1.10        6/7/99   Ian Formanek    Fixed usage of value with
*       ActionListener
*  10   Gandalf   1.9         6/4/99   Jaroslav Tulach Improved NbDialog.
*  9    Gandalf   1.8         6/3/99   Ian Formanek    
*  8    Gandalf   1.7         3/30/99  Ian Formanek    Fixed additional options
*  7    Gandalf   1.6         3/29/99  Ian Formanek    Fixed identifying source 
*       component if label is the same as some standard option
*  6    Gandalf   1.5         3/29/99  Ian Formanek    MUCH Improved - uses 
*       additional options, listens to property changes and renews the dialog 
*       accordingly.
*  5    Gandalf   1.4         3/26/99  Ian Formanek    Buttons aligned to the 
*       right if along bottom, default option on Enter key
*  4    Gandalf   1.3         3/20/99  Jaroslav Tulach DialogDescriptor has only
*       ActionListener
*  3    Gandalf   1.2         3/20/99  Ian Formanek    Added creation of buttons
*       from preset options
*  2    Gandalf   1.1         3/18/99  Ian Formanek    Much improved
*  1    Gandalf   1.0         3/11/99  Ian Formanek    
* $
*/
