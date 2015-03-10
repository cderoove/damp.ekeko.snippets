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

package org.netbeans.core.windows.toolbars;

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

import org.openide.awt.*;
import org.openide.actions.CustomizeBeanAction;
import org.openide.filesystems.*;
import org.openide.loaders.*;

/**
 * ToolbarPanel is a container for the <code>Toolbar</code> components.  The
 * serialized state is used to represent a single configuration of the
 * toolbar.
 *
 * @author Libor Kramolis
 */
public class ToolbarPanel extends JPanel {
    static final long serialVersionUID =8538698225954965241L;
    /**
     * Create new ToolbarPanel
     */
    public ToolbarPanel () {
        super ();
        setLayout (new FlowLayout (FlowLayout.LEFT));
        //      setLayout (new ToolbarLayout());
    }
}

/*
* Log
*  4    Gandalf   1.3         1/16/00  Libor Kramolis  
*  3    Gandalf   1.2         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  2    Gandalf   1.1         8/18/99  Ian Formanek    Generated serial version 
*       UID
*  1    Gandalf   1.0         7/11/99  David Simonek   
* $
*/
