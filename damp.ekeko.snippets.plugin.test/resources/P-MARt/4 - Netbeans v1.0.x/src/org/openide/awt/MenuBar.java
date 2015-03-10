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

package org.openide.awt;

import java.awt.event.*;
import java.io.*;
import java.util.Iterator;
import java.text.MessageFormat;
import javax.swing.*;

import org.openide.TopManager;
import org.openide.NotifyDescriptor;
import org.openide.loaders.*;
import org.openide.cookies.InstanceCookie;
import org.openide.nodes.*;
import org.openide.util.actions.Presenter;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.WeakListener;

/** An extended version of swing's JMenuBar. This menubar can
* load its content from the folder where its "disk image" is stored.<P>
* Moreover, menu is <code>Externalizable</code> to restore its persistent
* state with minimal storage expensiveness.
*
* @version 1.0
* @author  David Peroutka, Dafe Simonek
*/
public class MenuBar extends JMenuBar implements Externalizable {

    /** the folder which represents and loads content of the menubar */
    private MenuBarFolder menuBarFolder;

    static final long serialVersionUID =-4721949937356581268L;
    /** Don't call this constructor or this class will not get
    * initialized properly. This constructor is only for externalization.
    */
    public MenuBar() {
        super();
    }

    /** Creates a new <code>MenuBar</code> from given folder.
    * @param folder The folder from which to create the content of the menubar.
    * If the parameter is null, default menu folder is obtained.
    */
    public MenuBar(DataFolder folder) {
        super();
        // PENDING(david) -- setBorder(new MenuBorder());
        startLoading((folder == null)
                     ? TopManager.getDefault().getPlaces().folders().menus()
                     : folder);
    }

    /** Blocks until the menubar is completely created. */
    public void waitFinished () {
        menuBarFolder.instanceFinished();
    }

    /** Saves the contents of this object to the specified stream.
    *
    * @exception IOException Includes any I/O exceptions that may occur
    */
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(menuBarFolder.getFolder());
    }

    /**
     * Restores contents of this object from the specified stream.
     *
     * @exception ClassNotFoundException If the class for an object being
     *              restored cannot be found.
     */
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        startLoading((DataFolder)in.readObject());
    }

    /** Starts loading of this menu from menu folder */
    void startLoading (final DataFolder folder) {
        menuBarFolder = new MenuBarFolder(folder, this);
    }

    /** This class can be used to fill the content of given
    * <code>MenuBar</code> from the given <code>DataFolder</code>.
    */
    static final class MenuBarFolder extends FolderInstance {
        /** Asociated menubar which we should fill */
        MenuBar menuBar;

        /** Creates a new menubar folder on the specified <code>DataFolder</code>.
         * @param folder a <code>DataFolder</code> to work with
         */
        public MenuBarFolder (final DataFolder folder, final MenuBar menuBar) {
            super(folder);
            this.menuBar = menuBar;
            recreate ();
        }

        /** Full name of the data folder's primary file separated by dots.
        * @return the name
        */
        public String instanceName () {
            return menuBar.getClass().getName();
        }

        /** Returns the root class of all objects.
        * @return MenuBar.class
        */
        public Class instanceClass ()
        throws java.io.IOException, ClassNotFoundException {
            return MenuBar.class;
        }

        /** Accepts only cookies that can provide <code>MenuBar</code>.
        * @param cookie the instance cookie to test
        * @return true if the cookie can provide <code>MenuBar</code>
        */
        protected InstanceCookie acceptCookie (InstanceCookie cookie)
        throws java.io.IOException,
            ClassNotFoundException {
            return MenuBar.class.isAssignableFrom(cookie.instanceClass())
                   ? cookie : null;
        }

        /** Returns a <code>MenuFolder</code> cookie for the specified
        * <code>DataFolder</code>.
        * @param df a <code>DataFolder</code> to create the cookie for
        * @return a <code>MenuFolder</code> for the specified folder
        */
        protected InstanceCookie acceptFolder (DataFolder df) {
            return new MenuFolder(df);
        }

        /** Updates the <code>MenuBar</code> represented by this folder.
        *
        * @param cookies array of instance cookies for the folder
        * @return the updated <code>MenuBar</code> representee
        */
        protected Object createInstance (InstanceCookie[] cookies)
        throws java.io.IOException, ClassNotFoundException {
            //      System.out.println ("Creating menu bar:" + menuBar); // NOI18N
            // clear old content
            menuBar.removeAll();
            // fill with new content
            for (int i = 0; i < cookies.length; i++) {
                try {
                    Object obj = cookies[i].instanceCreate();
                    if (obj instanceof JMenu) {
                        menuBar.add((JMenu)obj);
                    }
                } catch (Exception exc) {
                    TopManager.getDefault().notifyException(exc);
                    /* new NotifyDescriptor.Exception(exc,
                       NbBundle.getBundle(this).getString("EXC_LoadMenu")));*/
                }
            }
            final MenuBar menuBarRef = menuBar;
            SwingUtilities.invokeLater(new Runnable() {
                                           public void run () {
                                               menuBarRef.validate();
                                               menuBarRef.repaint();
                                           }
                                       });
            return menuBar;
        }

        /** For outer class access to the data folder */
        DataFolder getFolder () {
            return folder;
        }

    }

    /** This class can be used to produce a <code>JMenu</code> instance
    * from the given <code>DataFolder</code>.
    */
    static final class MenuFolder extends FolderInstance
        implements NodeListener {

        /** the <code>JMenu</code> to work with */
        private JMenu menu;

        /**
         * Creates a menu on the specified <code>DataFolder</code>.
         * @param folder a <code>DataFolder</code> to work with
         */
        public MenuFolder (final DataFolder folder) {
            super(folder);
            recreate ();
        }

        /**
         * Returns a <code>Menu</code> representee of this folder.
         * @return a <code>Menu</code> representee of this folder
         */
        public final JMenu getMenu() {
            if (menu == null) {
                menu = new org.openide.awt.JMenuPlus(
                           folder.getNodeDelegate ().getDisplayName ()
                       );
                folder.getNodeDelegate ().addNodeListener (
                    WeakListener.node (this, folder.getNodeDelegate ())
                );
            }
            return menu;
        }

        /** If the display name changes, than change the name of the menu.
        */
        public void propertyChange (java.beans.PropertyChangeEvent ev) {
            if (
                Node.PROP_DISPLAY_NAME.equals (ev.getPropertyName ()) ||
                Node.PROP_NAME.equals (ev.getPropertyName ())
            ) {
                String name = folder.getNodeDelegate ().getDisplayName ();
                getMenu ().setText (name);
            }
        }

        /** Fired when a set of new children is added.
        * @param ev event describing the action
        */
        public void childrenAdded (NodeMemberEvent ev) {
        }

        /** Fired when a set of children is removed.
        * @param ev event describing the action
        */
        public void childrenRemoved (NodeMemberEvent ev) {
        }

        /** Fired when the order of children is changed.
        * @param ev event describing the change
        */
        public void childrenReordered(NodeReorderEvent ev) {
        }

        /** Fired when the node is deleted.
        * @param ev event describing the node
        */
        public void nodeDestroyed (NodeEvent ev) {
        }

        /** The name of the menu
        * @return the name
        */
        public String instanceName () {
            return getMenu().getClass().getName();
        }

        /** Returns the class of represented menu.
        * @return Object.class
        */
        public Class instanceClass ()
        throws java.io.IOException, ClassNotFoundException {
            return JMenu.class;
        }

        /** If no instance cookie, tries to create execution action on the
        * data object.
        */
        protected InstanceCookie acceptDataObject (DataObject dob) {
            InstanceCookie ic = super.acceptDataObject (dob);
            if (ic == null) {
                return new InstanceSupport.Instance (ExecBridge.createMenuItem (dob));
            } else {
                return ic;
            }
        }

        /**
         * Accepts only cookies that can provide <code>Menu</code>.
         * @param cookie an <code>InstanceCookie</code> to test
         * @return true if the cookie can provide accepted instances
         */
        protected InstanceCookie acceptCookie(InstanceCookie cookie)
        throws java.io.IOException,
            ClassNotFoundException {
            Class c = cookie.instanceClass();
            return ((Presenter.Menu.class.isAssignableFrom(c)) ||
                    (JMenuItem.class.isAssignableFrom(c)) ||
                    (JSeparator.class.isAssignableFrom(c))) ? cookie : null;
        }

        /**
         * Returns a <code>Menu.Folder</code> cookie for the specified
         * <code>DataFolder</code>.
         * @param df a <code>DataFolder</code> to create the cookie for
         * @return a <code>Menu.Folder</code> for the specified folder
         */
        protected InstanceCookie acceptFolder(DataFolder df) {
            return new MenuFolder(df);
        }

        /** Updates the <code>JMenu</code> represented by this folder.
        * @param cookies array of instance cookies for the folder
        * @return the updated <code>JMenu</code> representee
        */
        protected Object createInstance(InstanceCookie[] cookies)
        throws java.io.IOException, ClassNotFoundException {
            // clear first - refresh the menu's content
            getMenu().removeAll();
            for (int i = 0; i < cookies.length; i++) {
                try {
                    Object obj = cookies[i].instanceCreate();
                    if (obj instanceof Presenter.Menu) {
                        obj = ((Presenter.Menu)obj).getMenuPresenter();
                    }
                    if (obj instanceof JMenuItem) {
                        menu.add((JMenuItem)obj);
                    } else {
                        if (obj instanceof JSeparator) {
                            menu.addSeparator();
                        }
                    }
                } catch (Exception exc) {
                    TopManager.getDefault().notifyException(exc

                                                            /*            new NotifyDescriptor.Exception(exc,
                                                                          MessageFormat.format(
                                                                            NbBundle.getBundle(this).getString("EXC_LoadMenuItem"),
                                                                            new Object[] { cookies[i].instanceName() }
                                                                          )
                                                                        ) */
                                                           );
                }
            }
            menu.setEnabled((cookies.length == 0) ? false : true);

            // request menu repaint
            if (cookies.length > 0) {
                JPopupMenu popup = menu.getPopupMenu();
                popup.pack();
                popup.repaint();
            }

            // Special help ID dependent on name of menu, since otherwise
            // there would be no way to configure the ID from the DataFolder.
            HelpCtx.setHelpIDString (menu, MenuFolder.class.getName () + "." + menu.getText ()); // NOI18N
            return menu;
        }
    }

}

/*
 * Log
 *  17   dperoutka-src-gandalf1.16        1/20/00  Jaroslav Tulach Menu on multiuser 
 *       instalation can be renamed.
 *  16   dperoutka-src-gandalf1.15        1/13/00  Ian Formanek    NOI18N
 *  15   dperoutka-src-gandalf1.14        1/13/00  Jaroslav Tulach File names can be 
 *       localized.  
 *  14   dperoutka-src-gandalf1.13        1/12/00  Ian Formanek    NOI18N
 *  13   dperoutka-src-gandalf1.12        11/5/99  Jesse Glick     Context help jumbo 
 *       patch.
 *  12   dperoutka-src-gandalf1.11        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  11   dperoutka-src-gandalf1.10        8/9/99   Ian Formanek    Generated Serial Version
 *       UID
 *  10   dperoutka-src-gandalf1.9         6/28/99  Ian Formanek    NbJMenu renamed to 
 *       JMenuPlus
 *  9    dperoutka-src-gandalf1.8         6/28/99  Ian Formanek    Fixed bug 2043 - It is 
 *       virtually impossible to choose lower items of New From Template  from 
 *       popup menu on 1024x768
 *  8    dperoutka-src-gandalf1.7         6/9/99   Jaroslav Tulach Executables can be in 
 *       menu & toolbars.
 *  7    dperoutka-src-gandalf1.6         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  6    dperoutka-src-gandalf1.5         3/30/99  Ian Formanek    FolderInstance creation 
 *       in single thread
 *  5    dperoutka-src-gandalf1.4         3/2/99   David Simonek   icons repair
 *  4    dperoutka-src-gandalf1.3         2/26/99  David Simonek   
 *  3    dperoutka-src-gandalf1.2         2/19/99  David Simonek   menu related changes...
 *  2    dperoutka-src-gandalf1.1         1/25/99  David Peroutka  
 *  1    dperoutka-src-gandalf1.0         1/25/99  David Peroutka  
 * $
 */
