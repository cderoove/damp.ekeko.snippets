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

package org.openide.explorer.view;

import java.awt.event.*;
import java.awt.*;
import java.beans.*;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.openide.awt.ListPane;
import org.openide.awt.MouseUtils;
import org.openide.explorer.*;
import org.openide.util.actions.SystemAction;
import org.openide.util.actions.Presenter;
import org.openide.nodes.*;

/* TODO:
 - improve cell renderer (two lines of text or hints)
 - better behaviour during scrolling (ListPane)
 - external selection bug (BUG ID: 01110034)
*/

/** A view displaying icons.
*
* @author   Jaroslav Tulach
*/
public class IconView extends ListView implements Externalizable {

    /** generated Serialized Version UID */
    static final long serialVersionUID = -9129850245819731264L;


    public IconView () {
    }

    /** Creates the list that will display the data.
    */
    protected JList createList () {
        JList list = new ListPane ();
        list.setCellRenderer (new NodeRenderer (true));
        return list;
    }
}

/*
 * Log
 *  12   Gandalf   1.11        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  11   Gandalf   1.10        8/27/99  Jaroslav Tulach New threading model & 
 *       Children.
 *  10   Gandalf   1.9         6/28/99  Ian Formanek    Fixed bug 2043 - It is 
 *       virtually impossible to choose lower items of New From Template  from 
 *       popup menu on 1024x768
 *  9    Gandalf   1.8         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  8    Gandalf   1.7         4/24/99  Ian Formanek    Fixed bug which cause a 
 *       NullPointerException to be thrown when TemplatesExplorer was opened
 *  7    Gandalf   1.6         4/7/99   Jesse Glick     [JavaDoc]
 *  6    Gandalf   1.5         4/6/99   Ian Formanek    Added selection mode 
 *       property
 *  5    Gandalf   1.4         4/6/99   Ian Formanek    Added default handler
 *  4    Gandalf   1.3         3/20/99  Jesse Glick     [JavaDoc]
 *  3    Gandalf   1.2         2/11/99  Jaroslav Tulach SystemAction is 
 *       javax.swing.Action
 *  2    Gandalf   1.1         1/6/99   Ian Formanek    Reflecting changes in 
 *       location of package "awt"
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 * Beta Change History:
 *  0    Tuborg    0.11        --/--/98 Jan Formanek    added readObject
 *  0    Tuborg    0.12        --/--/98 Jan Formanek    changed the predecessor from JPanel to Object, added init() method
 *  0    Tuborg    0.20        --/--/98 Jan Formanek    reflecting changes in ExplorerView (became abstract class)
 *  0    Tuborg    0.20        --/--/98 Jan Formanek    validating scroll pane repaired
 *  0    Tuborg    0.30        --/--/98 Jan Formanek    SWITCHED TO NODES
 *  0    Tuborg    0.32        --/--/98 Jan Formanek    added listeners for subNodes add/remove and name changes
 *  0    Tuborg    0.40        --/--/98 Jan Formanek    reflecting changes in explorer model
 *  0    Tuborg    0.41        --/--/98 Jan Formanek    updateSelection added at initialize
 *  0    Tuborg    0.42        --/--/98 Jan Formanek    fixed clearing of selection after changing Node's name (BUG ID: 01000007)
 *  0    Tuborg    0.43        --/--/98 Jan Formanek    reflecting changes in ExplorerView
 *  0    Tuborg    0.46        --/--/98 Petr Hamernik   double click
 *  0    Tuborg    0.47        --/--/98 Petr Hamernik   default action
 *  0    Tuborg    0.49        --/--/98 Jan Formanek    improved context menu invocation
 */
