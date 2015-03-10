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

package org.netbeans.modules.web.core;

import java.util.Set;
import java.util.Iterator;
import java.util.Comparator;
import java.util.ArrayList;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.io.NotActiveException;

import org.openide.*;
import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.openide.windows.*;
import org.openide.actions.OpenAction;
import org.openide.actions.ViewAction;
import org.openide.text.*;
import org.openide.util.*;
import org.openide.util.actions.*;
import org.openide.nodes.Node;
import org.openide.nodes.Children;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.NodeListener;

/** Object that provides main functionality for internet data loader.
* This class is final only for performance reasons,
* can be unfinaled if desired.
*
* @author Petr Jiricka
*/
public final class WebDataObject extends MultiDataObject {

    public WebDataObject (FileObject pf, UniFileLoader l) throws DataObjectExistsException {
        super (pf, l);
    }

    protected org.openide.nodes.Node createNodeDelegate () {
        return new WebNode (this);
    }

}



/*
 * Log
 *  4    Gandalf   1.3         1/6/00   Petr Jiricka    Cleanup
 *  3    Gandalf   1.2         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  2    Gandalf   1.1         7/3/99   Petr Jiricka    
 *  1    Gandalf   1.0         6/30/99  Petr Jiricka    
 * $
 */
