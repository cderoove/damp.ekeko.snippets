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

package org.netbeans.modules.antlr.editor;

import java.beans.*;
import java.text.MessageFormat;

import org.openide.TopManager;
import org.openide.cookies.OpenCookie;
import org.openide.cookies.ViewCookie;
import org.openide.cookies.CloseCookie;
import org.openide.filesystems.*;
import org.openide.windows.TopComponent;
import org.openide.windows.CloneableTopComponent;
import org.openide.util.WeakListener;

import org.openide.loaders.DataObject;

/** Simple support for an openable file.
* Can be used either as an {@link OpenCookie}, {@link ViewCookie}, or {@link CloseCookie},
* depending on which cookies the subclass implements.
*
* @author Jaroslav Tulach
*/
public abstract class GOpenSupport extends Object {
    protected DataObject obj;

    /** All opened editors on this file. */
    protected CloneableTopComponent.Ref allEditors = CloneableTopComponent.EMPTY;

    /** listener on changes in data object */
    private PropertyChangeListener listener;


    public GOpenSupport(final DataObject obj) {
        if (DataObject.PROP_VALID.equals (ev.getPropertyName ())) {
            // loosing validity
            if (!obj.isValid ()) {
                close ();
            }
        }
    }
};

// attach property change listener to be informed about loosing validity
obj.addPropertyChangeListener (new WeakListener.PropertyChange (
                                   listener
                               ));
}
}

/** Focuses existing component to open, or if none exists creates new.
* @see OpenCookie#open
*/
public void open () {
    CloneableTopComponent editor = openCloneableTopComponent();
    editor.requestFocus();
}

/** Focuses existing component to view, or if none exists creates new.
* The default implementation simply calls {@link #open}.
* @see ViewCookie#view
*/
public void view () {
    open ();
}

/** Focuses existing component to view, or if none exists creates new.
* The default implementation simply calls {@link #open}.
* @see ViewCookie#view
*/
public void edit () {
    open ();
}

/** Closes all components.
* @return <code>true</code> if every component is successfully closed or <code>false</code> if the user cancelled the request
* @see CloseCookie#close
*/
public boolean close () {
    synchronized (allEditors) {
        java.util.Enumeration en = allEditors.getComponents ();

        if (!en.hasMoreElements ()) {
            // nothing needs to be saved
            return true;
        }

        // user canceled the action
        if (!canClose ()) {
            return false;
        }

        while (en.hasMoreElements ()) {
            TopComponent c = (TopComponent)en.nextElement ();
            if (!c.close ()) {
                return false;
            }
        }
    }
    return true;
}

/** Should test whether all data is saved, and if not, prompt the user
* to save.
* The default implementation returns <code>true</code>.
*
* @return <code>true</code> if everything can be closed
*/
protected boolean canClose () {
    return true;
}

/** Simply open for an editor. */
protected final CloneableTopComponent openCloneableTopComponent() {
    try {
        /*MessageFormat mf = new MessageFormat(DataObject.getString("CTL_ObjectOpen"));

        TopManager.getDefault().setStatusText(mf.format (
          new Object[] {
            obj.getName(),
            obj.getPrimaryFile().toString()
          }
        ));*/
        synchronized (allEditors) {
            try {
                CloneableTopComponent ret = (CloneableTopComponent)allEditors.getAnyComponent ();
                ret.open();
                return ret;
            } catch (java.util.NoSuchElementException ex) {
                // no opened editor
                CloneableTopComponent editor = createCloneableTopComponent ();
                allEditors = editor.getReference ();
                editor.open();
                return editor;
            }
        }
    } finally {
        //TopManager.getDefault ().setStatusText (DataObject.getString ("CTL_ObjectOpened"));
    }
}


/** A method to create a new component. Must be overridden in subclasses.
* @return the cloneable top component for this support
*/
protected abstract CloneableTopComponent createCloneableTopComponent ();

}

/*
* Log
*  1    Gandalf-post-FCS1.0         4/7/00   Jesse Glick     
* $
*/
