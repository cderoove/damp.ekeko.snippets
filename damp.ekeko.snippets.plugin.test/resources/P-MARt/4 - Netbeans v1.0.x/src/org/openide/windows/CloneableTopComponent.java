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

package org.openide.windows;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.*;

import org.openide.loaders.DataObject;
import org.openide.util.io.NbMarshalledObject;
import org.openide.util.NbBundle;

/** A top component which may be cloned.
* Typically cloning is harmless, i.e. the data contents (if any)
* of the component are the same, and the new component is merely
* a different presentation.
* Also, a list of all cloned components is kept.
*
* @author Jaroslav Tulach
*/
public abstract class CloneableTopComponent extends TopComponent
    implements java.io.Externalizable, TopComponent.Cloneable {
    /** generated Serialized Version UID */
    static final long serialVersionUID = 4893753008783256289L;

    /** reference with list of components */
    private Ref ref;

    /** Create a cloneable top component.
    */
    public CloneableTopComponent () {
    }

    /** Create a cloneable top component associated with a data object.
    * @param obj the data object
    * @see TopComponent#TopComponent(DataObject)
    */
    public CloneableTopComponent (DataObject obj) {
        super (obj);
    }

    /** Clone the top component and register the clone.
    * @return the new component
    */
    public final Object clone () {
        return cloneComponent ();
    }

    /** Clone the top component and register the clone.
    * Simply calls createClonedObject () and registers the component to
    * Ref.
    *
    * @return the new cloneable top component
    */
    public final CloneableTopComponent cloneTopComponent() {
        CloneableTopComponent top = createClonedObject ();
        // register the component if it has not been registered before
        top.setReference (getReference ());
        return top;
    }

    /** Clone the top component and register the clone.
    * @return the new component
    */
    public final TopComponent cloneComponent() {
        return cloneTopComponent ();
    }

    /** Called from {@link #clone} to actually create a new component from this one.
    * The default implementation only clones the object by calling {@link Object#clone}.
    * Subclasses may leave this as is, assuming they have no special needs for the cloned
    * data besides copying it from one object to the other. If they do, the superclass
    * method should be called, and the returned object modified appropriately.
    * @return a copy of this object
    */
    protected CloneableTopComponent createClonedObject () {
        try {
            // clones the component using serialization
            NbMarshalledObject o = new NbMarshalledObject (this);
            CloneableTopComponent top = (CloneableTopComponent)o.get ();
            return top;
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new InternalError ();
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
            throw new InternalError ();
        }
    }

    /** Get a list of all components which are clone-sisters of this one.
    *
    * @return the clone registry for this component's group
    */
    public synchronized final Ref getReference () {
        if (ref == null) {
            ref = new Ref (this);
        }
        return ref;
    }

    /** Changes the reference to which this components belongs.
    * @param another the new reference this component should belong
    */
    public synchronized final void setReference (Ref another) {
        if (another == EMPTY) {
            throw new IllegalArgumentException(
                NbBundle.getBundle(CloneableTopComponent.class).getString("EXC_CannotAssign")
            );
        }

        if (ref != null) {
            // we belong to a reference
            synchronized (ref) {
                ref.getTable ().remove (this);
            }
        }
        // register with the new reference this changes the ref field
        another.register (this);
    }

    /** Called when this component is about to close.
    * The default implementation just unregisters the clone from its clone list.
    * <p>If this is the last component in its clone group, then
    * {@link #closeLast} is called to clean up.
    *
    * @return <CODE>true</CODE> if there are still clone sisters left, or this was the last in its group
    *    but {@link #closeLast} returned <code>true</code>
    */
    public boolean canClose (Workspace workspace, boolean last) {
        if (last) {
            return getReference ().unregister (this);
        }
        return true;
    }

    /** Called when the last component in a clone group is closing.
    * The default implementation just returns <code>true</code>.
    * Subclasses may specify some hooks to run.
    * @return <CODE>true</CODE> if the component is ready to be
    *    closed, <CODE>false</CODE> to cancel
    */
    protected boolean closeLast () {
        return true;
    }

    public void readExternal (java.io.ObjectInput oi)
    throws java.io.IOException, java.lang.ClassNotFoundException {
        super.readExternal (oi);
        if (serialVersion != 0) {
            // since serialVersion > 0
            // the reference object is also stored

            Ref ref = (Ref)oi.readObject ();
            if (ref != null) {
                setReference (ref);
            }
        }
    }

    public void writeExternal (java.io.ObjectOutput oo)
    throws java.io.IOException {
        super.writeExternal (oo);

        oo.writeObject (ref);
    }

    // say what? --jglick
    /* Empty set that should save work with testing like
    * <pre>
    * if (ref == null || ref.isEmpty ()) {
    *   CloneableTopComponent c = new CloneableTopComponent (obj);
    *   ref = c.getReference ();
    * }
    * </pre>
    * Instead one can always set <CODE>ref = Ref.EMPTY</CODE> and test only if
    * <CODE>ref.isEmpty</CODE> returns <CODE>true</CODE>.
    */
    /** Empty clone-sister list.
    */
    public static final Ref EMPTY = new Ref ();

    /** Keeps track of a group of sister clones.
    * <P>
    * <B>Warning:</B>
    * For proper use
    * subclasses should have method readResolve () and implement it
    * in right way to deal with separate serialization of TopComponent.
    */
    public static class Ref implements java.io.Serializable {
        /** generated Serialized Version UID */
        static final long serialVersionUID = 5543148876020730556L;

        /** list of registered components */
        private transient HashSet table;

        /** Default constructor for creating empty reference.
        */
        protected Ref () {
        }

        /** Constructor.
        * @param c the component to refer to
        */
        Ref (CloneableTopComponent c) {
            getTable ().add (c);
        }

        /** Lazy getter for table list. Should be called when synchronized.
        */
        private HashSet getTable () {
            if (table == null) {
                table = new HashSet (7);
            }
            return table;
        }


        /** Get all registered components.
        * @return set of {@link CloneableTopComponent}s
        */
        synchronized Set componentSet () {
            return (Set)getTable ().clone ();
        }

        /** Enumeration of all registered components.
        * @return enumeration of CloneableTopComponent
        */
        public Enumeration getComponents () {
            return java.util.Collections.enumeration (componentSet ());
        }

        /** Test whether there is any component in this set.
        * @return <CODE>true</CODE> if the reference set is empty
        */
        public synchronized boolean isEmpty () {
            return getTable ().isEmpty ();
        }

        /** Retrieve an arbitrary component from the set.
        * @return some component from the list of registered ones
        * @exception NoSuchElementException if the set is empty
        */
        public synchronized CloneableTopComponent getAnyComponent () {
            return (CloneableTopComponent)getTable ().iterator ().next ();
        }

        /** Register new component.
        * @param c the component to register
        */
        final synchronized void register (CloneableTopComponent c) {
            getTable ().add (c);
            c.ref = this;
        }

        /** Unregister the component. If this is the last asks if it is
        * allowed to unregister it.
        *
        * @param c the component to unregister
        * @return true if the component agreed to be unregister
        */
        final boolean unregister (CloneableTopComponent c) {
            HashSet table = getTable ();

            if ((table.size () > 1) || (c.closeLast())) {
                synchronized (this) {
                    // has to be synchronized because access to table structure
                    table.remove(c);
                }
                return true;
            } else {
                return false;
            }
        }
    } // end of Ref
}

/*
 * Log
 *  14   Gandalf   1.13        1/13/00  David Simonek   i18n
 *  13   Gandalf   1.12        12/10/99 Jaroslav Tulach canClose
 *  12   Gandalf   1.11        12/8/99  Jaroslav Tulach TopComponent enhanced.
 *  11   Gandalf   1.10        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  10   Gandalf   1.9         8/19/99  Ian Formanek    Fixed bug 3520 - 
 *       Breakpoint cannot be set in Editing wkspc when editor was closed in 
 *       Debugging wkspc.
 *  9    Gandalf   1.8         7/28/99  David Simonek   canClose() parameters 
 *       changed
 *  8    Gandalf   1.7         7/11/99  David Simonek   window system change...
 *  7    Gandalf   1.6         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  6    Gandalf   1.5         3/29/99  Jesse Glick     [JavaDoc]
 *  5    Gandalf   1.4         1/17/99  Jaroslav Tulach closeLast that returns 
 *       true if followed by a call to component.close ()
 *  4    Gandalf   1.3         1/7/99   Jaroslav Tulach 
 *  3    Gandalf   1.2         1/7/99   Ian Formanek    closeLast changed from 
 *       abstract to return true by default
 *  2    Gandalf   1.1         1/6/99   Jaroslav Tulach Change of package of 
 *       DataObject
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 * Beta Change History:
 *  0    Tuborg    0.14        --/--/98 Jaroslav Tulach synchronization methods refered to table
 *  0    Tuborg    0.14        --/--/98 Jaroslav Tulach Deadlock warning: Do not synchronize against the component!!!!
 *  0    Tuborg    0.15        --/--/98 Jaroslav Tulach Listeners added
 *  0    Tuborg    0.16        --/--/98 Jan Formanek    serialization fix
 *  0    Tuborg    0.17        --/--/98 Petr Hamernik   serialization fix 2
 */
