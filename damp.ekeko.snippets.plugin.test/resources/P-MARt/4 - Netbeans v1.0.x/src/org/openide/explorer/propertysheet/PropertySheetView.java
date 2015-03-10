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

package org.openide.explorer.propertysheet;

import java.beans.*;
import java.awt.Dimension;
import javax.swing.*;

import org.openide.nodes.Node;
import org.openide.explorer.*;

/** An Explorer view displaying a property sheet.
* @see PropertySheet
* @author   Jan Jancura, Jaroslav Tulach, Ian Formanek
*/
public class PropertySheetView extends PropertySheet {
    /** generated Serialized Version UID */
    static final long serialVersionUID = -7568245745904766160L;
    /** helper flag for avoiding multiple initialization of the GUI */
    transient private boolean guiInitialized = false;

    /** The Listener that tracks changes in explorerManager */
    transient private PropertyIL managerListener;

    /** manager to use */
    transient private ExplorerManager explorerManager;

    /** Initializes the GUI of the view */
    private void initializeGUI() {
        guiInitialized = true;
        setBorder (new javax.swing.border.EtchedBorder());
        managerListener = new PropertyIL();
    }

    /* Initializes the sheet.
    */
    public void addNotify () {
        super.addNotify ();

        explorerManager = ExplorerManager.find (this);
        if (!guiInitialized)
            initializeGUI();

        // add propertyChange listeners to the explorerManager
        explorerManager.addPropertyChangeListener(managerListener);
        setNodes (explorerManager.getSelectedNodes ());
    }

    /* Deinitializes the sheet.
    */
    public void removeNotify () {
        super.removeNotify ();

        if (explorerManager != null) { //[PENDING] patch for bug in JDK1.3 Window
            // (doublecall destroy()&removeNotify() for
            // destroyed, but no garbagecollected windows
            explorerManager.removePropertyChangeListener(managerListener);
            explorerManager = null;
            setNodes (new Node[0]);
        }
    }

    /* Changes preferred size
    */
    public Dimension getPreferredSize () {
        return new Dimension (200, 300);
    }

    // INNER CLASSES ***************************************************************************

    /**
    * The inner adaptor class for listening to the ExplorerManager's property and
    * vetoable changes.
    */
    class PropertyIL implements PropertyChangeListener {
        public void propertyChange (PropertyChangeEvent evt) {
            if (ExplorerManager.PROP_SELECTED_NODES.equals (evt.getPropertyName ())) {
                setNodes ((Node []) evt.getNewValue ());
            }
        }
    }
}

/*
 * Log
 *  5    Gandalf   1.4         2/17/00  Jan Jancura     Last minute change: 
 *       patch for bug in JDK1.3 (removeNotify() called twice)  
 *  4    Gandalf   1.3         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  3    Gandalf   1.2         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  2    Gandalf   1.1         3/20/99  Jesse Glick     [JavaDoc]
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 * Beta Change History:
 *  0    Tuborg    0.27        --/--/98 Jan Formanek    removed initializingEM variable
 *  0    Tuborg    0.30        --/--/98 Jan Formanek    reflecting changes in explorer model
 *  0    Tuborg    0.31        --/--/98 Jan Formanek    border around PropertySheet
 *  0    Tuborg    0.32        --/--/98 Jan Formanek    reflecting changes in ExplorerView
 */
