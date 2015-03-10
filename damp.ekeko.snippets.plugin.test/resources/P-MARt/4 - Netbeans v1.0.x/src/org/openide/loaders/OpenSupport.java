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

package org.openide.loaders;

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

/** Simple support for an openable file.
* Can be used either as an {@link OpenCookie}, {@link ViewCookie}, or {@link CloseCookie},
* depending on which cookies the subclass implements.
*
* @author Jaroslav Tulach
*/
public abstract class OpenSupport extends Object {
    /** Entry to work with. */
    protected MultiDataObject.Entry entry;

    /** All opened editors on this file. */
    protected CloneableTopComponent.Ref allEditors;

    /** New support for a given entry. The file is taken from the
    * entry and is updated if the entry moves or renames itself.
    * @param entry entry to create instance from
    */
    public OpenSupport (MultiDataObject.Entry entry) {
        this.entry = entry;

        Listener l = new Listener (entry);
        this.allEditors = l;

        final DataObject obj = entry.getDataObject ();
        if (obj != null) {
            // attach property change listener to be informed about loosing validity
            obj.addPropertyChangeListener (WeakListener.propertyChange (
                                               l, obj
                                           ));
            // attach vetoable change listener to be cancel loosing validity when modified
            obj.addVetoableChangeListener (WeakListener.vetoableChange (
                                               l, obj
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
        return close (true);
    }

    /** Closes all opened windows.
    * @param ask true if we should ask user
    * @return true if sucesfully closed
    */
    boolean close (boolean ask) {
        synchronized (allEditors) {
            java.util.Enumeration en = allEditors.getComponents ();

            if (!en.hasMoreElements ()) {
                // nothing needs to be saved
                return true;
            }

            // user canceled the action
            if (ask && !canClose ()) {
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
        MessageFormat mf = new MessageFormat(DataObject.getString("CTL_ObjectOpen"));
        DataObject obj = entry.getDataObject();

        synchronized (allEditors) {
            try {
                CloneableTopComponent ret = (CloneableTopComponent)allEditors.getAnyComponent ();
                ret.open();
                return ret;
            } catch (java.util.NoSuchElementException ex) {
                // no opened editor
                TopManager.getDefault().setStatusText(mf.format (
                                                          new Object[] {
                                                              obj.getName(),
                                                              obj.getPrimaryFile().toString()
                                                          }
                                                      ));

                CloneableTopComponent editor = createCloneableTopComponent ();
                editor.setReference (allEditors);
                editor.open();

                TopManager.getDefault ().setStatusText (DataObject.getString ("CTL_ObjectOpened"));
                return editor;
            }
        }
    }


    /** A method to create a new component. Must be overridden in subclasses.
    * @return the cloneable top component for this support
    */
    protected abstract CloneableTopComponent createCloneableTopComponent ();


    /** Property change & veto listener. To react to dispose/delete of
    * the data object.
    */
    private static final class Listener extends CloneableTopComponent.Ref
        implements PropertyChangeListener, VetoableChangeListener {
        /** generated Serialized Version UID */
        static final long serialVersionUID = -1934890789745432531L;
        /** entry to serialize */
        private MultiDataObject.Entry entry;

        /** Constructor.
        */
        public Listener (MultiDataObject.Entry entry) {
            this.entry = entry;
        }

        public void propertyChange (PropertyChangeEvent ev) {
            if (DataObject.PROP_VALID.equals (ev.getPropertyName ())) {
                // loosing validity
                DataObject obj = entry.getDataObject ();
                OpenSupport os = (OpenSupport)obj.getCookie (OpenSupport.class);
                if (!obj.isValid () && os != null) {
                    // mark the object as not being modified, so nobody
                    // will ask for save
                    obj.setModified (false);

                    os.close (false);
                }
            }
        }

        /** Forbids setValid (false) on data object when there is an
        * opened editor.
        *
        * @param ev PropertyChangeEvent
        */
        public void vetoableChange (PropertyChangeEvent ev)
        throws PropertyVetoException {
            if (DataObject.PROP_VALID.equals (ev.getPropertyName ())) {
                // loosing validity
                DataObject obj = entry.getDataObject ();
                if (obj.isValid () && obj.isModified ()) {
                    OpenSupport os = (OpenSupport)obj.getCookie (OpenSupport.class);
                    if (os != null && !os.close (true)) {
                        // is modified and has not been sucessfully closed
                        throw new PropertyVetoException (
                            obj.getPrimaryFile ().toString (), ev
                        );
                    }
                }
            }
        }

        /** Resolvable to connect to the right data object. This
        * method is used for connectiong CloneableTopComponents via
        * their CloneableTopComponent.Ref
        */
        public Object readResolve () {
            DataObject obj = entry.getDataObject ();
            OpenSupport os = (OpenSupport)obj.getCookie (OpenSupport.class);
            if (os == null) {
                // problem! no replace!?
                return this;
            }
            // use the editor support's CloneableTopComponent.Ref
            return os.allEditors;
        }
    }
}

/*
* Log
*  20   src-jtulach1.19        12/8/99  Jaroslav Tulach TopComponent enhanced.
*  19   src-jtulach1.18        11/5/99  Jaroslav Tulach WeakListener has now 
*       registration methods.
*  18   src-jtulach1.17        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  17   src-jtulach1.16        9/30/99  Jaroslav Tulach 
*  16   src-jtulach1.15        9/30/99  Jaroslav Tulach OpenSupport is attached 
*       to setValid veto change of its data object.
*  15   src-jtulach1.14        9/13/99  Jaroslav Tulach #2628
*  14   src-jtulach1.13        9/13/99  Jan Jancura     Do not write "Opening 
*       blablabla" when no new document is opened - only focused.
*  13   src-jtulach1.12        7/13/99  Ales Novak      new win sys change
*  12   src-jtulach1.11        7/11/99  David Simonek   window system change...
*  11   src-jtulach1.10        6/22/99  Ales Novak      creating of editors is 
*       centralized
*  10   src-jtulach1.9         6/8/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  9    src-jtulach1.8         4/2/99   Jaroslav Tulach New from template opens 
*       the editor.
*  8    src-jtulach1.7         3/31/99  David Simonek   ugly ugly ugly 
*       requestFocus bungs fixed
*  7    src-jtulach1.6         3/17/99  Jaroslav Tulach Output Window fixing.
*  6    src-jtulach1.5         3/15/99  Jesse Glick     [JavaDoc]
*  5    src-jtulach1.4         3/14/99  Jaroslav Tulach Change of 
*       MultiDataObject.Entry.
*  4    src-jtulach1.3         3/10/99  Jesse Glick     [JavaDoc]
*  3    src-jtulach1.2         2/3/99   Jaroslav Tulach 
*  2    src-jtulach1.1         1/17/99  Jaroslav Tulach open/close/canClose 
*       methods
*  1    src-jtulach1.0         1/7/99   Jaroslav Tulach 
* $
*/
