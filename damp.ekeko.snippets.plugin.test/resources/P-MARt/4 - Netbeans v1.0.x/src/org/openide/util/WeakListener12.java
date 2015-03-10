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

package org.openide.util;

import java.beans.*;
import java.util.EventListener;
import java.awt.event.FocusListener;
import java.awt.event.FocusEvent;

import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

import org.openide.filesystems.*;
import org.openide.loaders.OperationListener;
import org.openide.loaders.OperationEvent;

import org.openide.nodes.*;

/** Factory for JDK 1.2.
*
* @author Jaroslav Tulach
*/
final class WeakListener12 extends Object implements WeakListener.Factory {
    public NodeListener node(NodeListener l,Object source) {
        WeakListener.Node wl = new WeakListener.Node (l);
        wl.setSource (source);
        return wl;
    }

    public PropertyChangeListener propertyChange(PropertyChangeListener l,Object source) {
        WeakListener.PropertyChange wl = new WeakListener.PropertyChange (l);
        wl.setSource (source);
        return wl;
    }

    public VetoableChangeListener vetoableChange(VetoableChangeListener l,Object source) {
        WeakListener.VetoableChange wl = new WeakListener.VetoableChange (l);
        wl.setSource (source);
        return wl;
    }

    public FileChangeListener fileChange(FileChangeListener l,Object source) {
        WeakListener.FileChange wl = new WeakListener.FileChange (l);
        wl.setSource (source);
        return wl;
    }

    public FileStatusListener fileStatus(FileStatusListener l,Object source) {
        WeakListener.FileStatus wl = new WeakListener.FileStatus (l);
        wl.setSource (source);
        return wl;
    }

    public RepositoryListener repository(RepositoryListener l,Object source) {
        WeakListener.Repository wl = new WeakListener.Repository (l);
        wl.setSource (source);
        return wl;
    }

    public DocumentListener document(DocumentListener l,Object source) {
        WeakListener.Document wl = new WeakListener.Document (l);
        wl.setSource (source);
        return wl;
    }

    public ChangeListener change(ChangeListener l,Object source) {
        WeakListener.Change wl = new WeakListener.Change (l);
        wl.setSource (source);
        return wl;
    }

    public FocusListener focus(FocusListener l,Object source) {
        WeakListener.Focus wl = new WeakListener.Focus (l);
        wl.setSource (source);
        return wl;
    }

    public OperationListener operation(OperationListener l,Object source) {
        WeakListener.Operation wl = new WeakListener.Operation (l);
        wl.setSource (source);
        return wl;
    }
}

/*
* Log
*  2    Gandalf   1.1         1/5/00   Jaroslav Tulach Added operation listener.
*  1    Gandalf   1.0         11/5/99  Jaroslav Tulach 
* $
*/