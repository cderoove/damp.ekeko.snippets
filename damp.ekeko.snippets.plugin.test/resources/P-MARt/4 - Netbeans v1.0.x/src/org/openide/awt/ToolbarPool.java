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

import java.awt.Component;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.EventQueue;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

import org.openide.*;
import org.openide.loaders.*;
import org.openide.cookies.InstanceCookie;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;

/**
 * This class keeps track of the current toolbars and their names.
 * @author David Peroutka, Libor Kramolis
 */
public final class ToolbarPool extends JComponent {
    /** Default ToolbarPool */
    private static ToolbarPool defaultPool;

    /** objects responsible for creation of the window */
    private static Folder instance;

    /** DataFolder from which the pool was created */
    private static DataFolder folder;

    /** Maps name to <code>Toolbar</code>s */
    private static Map toolbars;
    /** Maps name to <code>ToolbarPool.Configuration</code>s */
    private static Map toolbarConfigs;

    /** Current name of selected configuration */
    private static String name = ""; // NOI18N

    /** Center component */
    private static Component center;

    /** Popup menu listener */
    private static PopupListener listener;

    /**
     * Returns default toolbar pool.
     * @return default system pool
     */
    public static ToolbarPool getDefault () {
        if (defaultPool == null) {
            synchronized (ToolbarPool.class) {
                if (defaultPool == null) {
                    defaultPool = new ToolbarPool (
                                      TopManager.getDefault ().getPlaces ().folders ().toolbars ()
                                  );
                    // we mustn't do this in constructor to prevent from
                    // nevereding recursive calls to this method.
                    defaultPool.instance.recreate();
                }
            }
        }

        return defaultPool;
    }

    static final long serialVersionUID =3420915387298484008L;
    /**
     * Creates a new <code>ToolbarPool</code>.
     */
    private ToolbarPool (DataFolder df) {
        folder = df;

        setLayout (new BorderLayout ());

        listener = new PopupListener();
        toolbars = new TreeMap();
        toolbarConfigs = new TreeMap();

        instance = new Folder (df);
    }

    /** Allows to wait till the content of the pool is initialized. */
    public void waitFinished () {
        instance.instanceFinished ();
    }

    /** Initialization of new values.
     * @param toolbars map (String, Toolbar) of toolbars
     * @param conf map (String, Configuration) of configs
     */
    void update (Map toolbars, Map conf) {
        this.toolbars = toolbars;
        this.toolbarConfigs = conf;

        setConfiguration (name);
        //      Configuration c = (Configuration)toolbarConfigs.get (name);
        //      if (c != null) {
        //        activate (c);
        //      } else if (toolbarConfigs.isEmpty()) {
        //        updateDefault ();
        //      } else {
        //        c = (Configuration)toolbarConfigs.values().iterator().next();
        //        activate (c);
        //      }
    }

    /** Updates the default configuration. */
    private synchronized void updateDefault () {
        JPanel tp = new JPanel (new FlowLayout (FlowLayout.LEFT));
        Toolbar[] list = getToolbars ();
        for (int i = 0; i < list.length; i++) {
            tp.add (list[i]);
        }
        name = ""; // NOI18N
        revalidate (tp);
    }

    /** Activates a configuration.
     * @param c configuration
     */
    private synchronized void activate (Configuration c) {
        Component comp = c.activate ();
        name = c.getName();
        revalidate (comp);
    }

    /** Sets DnDListener to all Toolbars. */
    public void setToolbarsListener (Toolbar.DnDListener l) {
        Iterator it = toolbars.values().iterator();
        while (it.hasNext())
            ((Toolbar)it.next()).setDnDListener (l);
    }

    /** Uses new component as a cental one. */
    private void revalidate (Component c) {
        if (c != center) {
            // exchange
            if (center != null) {
                remove (center);
                center.removeMouseListener (listener);
            }
            add (center = c, BorderLayout.CENTER);
            center.addMouseListener (listener);
        }

        //revalidate ();
        invalidate ();
        validate ();
        repaint ();

        // Dafe - this breaks main windoe positioning...
        /*java.awt.Window w = javax.swing.SwingUtilities.windowForComponent (this);
        if (w != null) {
          w.pack ();
    }*/

        // Libor - Dafe's fix breaks main window resizing...
        java.awt.Window w = javax.swing.SwingUtilities.windowForComponent (this);
        if (w != null) {
            w.validate ();
        }
    }

    //    public void updateUI () {
    //      super.updateUI();
    //      revalidate (center);
    //    }

    /**
     * Returns a <code>Toolbar</code> to which this pool maps the given name.
     * @param name a <code>String</code> that is to be a toolbar's name
     * @return a <code>Toolbar</code> to which this pool maps the name
     */
    public Toolbar findToolbar (String name) {
        return (Toolbar)toolbars.get (name);
    }

    /**
     * Getter for the name of current configuration.
     * @return the name of current configuration
     */
    public String getConfiguration () {
        return name;
    }

    /**
     * Switch to toolbar configuration by specific config name
     * @param name toolbar configuration name
     */
    public void setConfiguration (String n) {
        //    instance.waitFinished ();
        if (!instance.isFinished())
            return;

        Configuration config = null;
        if (n != null) {
            config = (Configuration)toolbarConfigs.get (n);
        }
        if (config != null) { // if configuration found
            activate (config);
        } else if (toolbarConfigs.isEmpty()) { // if no toolbar configuration
            updateDefault ();
        } else if (center == null) { // bad config name (n) and no configuration activated yet
            config = (Configuration)toolbarConfigs.values().iterator().next();
            activate (config);
        }
    }

    /**
     * @return the <code>DataFolder</code> from which the pool was created.
     */
    public DataFolder getFolder() {
        return folder;
    }


    /**
     * Returns the toolbars contained in this pool.
     * @return the toolbars contained in this pool
     */
    public synchronized Toolbar[] getToolbars() {
        Toolbar[] arr = new Toolbar[toolbars.size ()];
        return (Toolbar[])toolbars.values ().toArray (arr);
    }

    /**
     * @return the names of toolbar configurations contained in this pool
     */
    public synchronized String[] getConfigurations () {
        String[] arr = new String[toolbarConfigs.size ()];
        return (String[])toolbarConfigs.keySet ().toArray (arr);
    }


    /**
     * This class can be used to produce a <code>ToolbarPool</code> instance
     * from the given <code>DataFolder</code>.
     */
    private class Folder extends FolderInstance {
        private WeakHashMap foldersCache = new WeakHashMap (15);

        /** the <code>ToolbarPool</code> to work with */
        private ToolbarPool toolbarPool;

        public Folder (DataFolder f) {
            super (f);
        }

        /**
         * Full name of the data folder's primary file separated by dots.
         * @return the name
         */
        public String instanceName () {
            return instanceClass().getName();
        }

        /**
         * Returns the root class of all objects.
         * @return Object.class
         */
        public Class instanceClass () {
            return ToolbarPool.class;
        }

        /**
         * Accepts only cookies that can provide <code>Configuration</code>.
         * @param cookie the instance cookie to test
         * @return true if the cookie can provide <code>Configuration</code>
         */
        protected InstanceCookie acceptCookie (InstanceCookie cookie)
        throws java.io.IOException, ClassNotFoundException {
            Class cls = cookie.instanceClass();
            if (ToolbarPool.Configuration.class.isAssignableFrom (cls)) {
                return cookie;
            }
            if (Component.class.isAssignableFrom (cls)) {
                return cookie;
            }
            return null;
        }

        /**
         * Returns a <code>Toolbar.Folder</code> cookie for the specified
         * <code>DataFolder</code>.
         * @param df a <code>DataFolder</code> to create the cookie for
         * @return a <code>Toolbar.Folder</code> for the specified folder
         */
        protected InstanceCookie acceptFolder (DataFolder df) {
            InstanceCookie ic = (InstanceCookie)foldersCache.get (df);
            if (ic == null) {
                ic = new Toolbar.Folder (df);
                foldersCache.put (df, ic);
            }
            return ic;
        }

        /**
         * Updates the <code>ToolbarPool</code> represented by this folder.
         *
         * @param cookies array of instance cookies for the folder
         * @return the updated <code>ToolbarPool</code> representee
         */
        protected Object createInstance (InstanceCookie[] cookies)
        throws java.io.IOException, ClassNotFoundException {
            final int length = cookies.length;

            Map toolbars = new TreeMap ();
            Map conf = new TreeMap ();

            for (int i = 0; i < length; i++) {
                try {
                    Object obj = cookies[i].instanceCreate();

                    if (obj instanceof Toolbar) {
                        Toolbar toolbar = (Toolbar)obj;
                        // should be done by ToolbarPanel in add method
                        toolbar.removeMouseListener (listener);
                        toolbar.addMouseListener (listener);
                        //	    if (toolbar.getComponentCount() > 0)         // TEMPORARY
                        toolbars.put (toolbar.getName (), toolbar);
                        continue;
                    }

                    if (obj instanceof Configuration) {
                        Configuration config = (Configuration)obj;
                        String name = config.getName ();
                        if (name == null) {
                            name = cookies[i].instanceName ();
                        }
                        conf.put (name, config);
                        continue;
                    }
                    if (obj instanceof Component) {
                        Component comp = (Component)obj;
                        String name = comp.getName ();
                        if (name == null) {
                            name = cookies[i].instanceName ();
                        }
                        conf.put (name, new ComponentConfiguration (comp));
                        continue;
                    }
                } catch (java.io.IOException ex) {
                    TopManager.getDefault ().notifyException (ex);
                } catch (ClassNotFoundException ex) {
                    TopManager.getDefault ().notifyException (ex);
                }
            }
            update (toolbars, conf);

            return ToolbarPool.this;
        }
    } // end of Folder


    /**
     * Class to showing popup menu
     */
    private class PopupListener extends MouseUtils.PopupMouseAdapter {
        /**
         * Called when the sequence of mouse events should lead to actual showing popup menu
         */
        protected void showPopup (MouseEvent e) {
            Configuration conf = (Configuration)toolbarConfigs.get (name);
            if (conf != null) {
                JPopupMenu pop = conf.getContextMenu();
                pop.show (e.getComponent (), e.getX (), e.getY ());
            }
        }
    } // end of PopupListener


    /**
     * Abstract class for toolbar configuration
     */
    public static interface Configuration {
        /** Activates the configuration and returns right
        * component that can display the configuration.
        * @return representation component
        */
        public abstract Component activate ();

        /** Name of the configuration.
        * @return the name
        */
        public abstract String getName ();

        /** Popup menu that should be displayed when the users presses
        * right mouse button on the panel. This menu can contain
        * contains list of possible configurations, additional actions, etc.
        *
        * @return popup menu to be displayed
        */
        public abstract JPopupMenu getContextMenu ();
    }


    /** Implementation of configuration that reacts to one
    * component */
    private static final class ComponentConfiguration extends JPopupMenu
        implements Configuration, ActionListener {
        private Component comp;

        static final long serialVersionUID =-409474484612485719L;
        /** @param comp component that represents this configuration */
        public ComponentConfiguration (Component comp) {
            this.comp = comp;
        }

        /** Simply returns the representation component */
        public Component activate () {
            return comp;
        }

        /** @return name of the component
        */
        public String getName () {
            return comp.getName ();
        }

        /** Updates items in popup menu and returns itself.
        */
        public JPopupMenu getContextMenu () {
            removeAll ();

            // generate list of available toolbar panels
            Iterator it = Arrays.asList (ToolbarPool.getDefault ().getConfigurations ()).iterator ();
            ButtonGroup bg = new ButtonGroup ();
            String current = ToolbarPool.getDefault ().getConfiguration ();
            while (it.hasNext()) {
                final String name = (String)it.next ();
                JRadioButtonMenuItem mi = new JRadioButtonMenuItem (name, (name.compareTo (current) == 0));
                mi.addActionListener (this);
                bg.add (mi);
                this.add (mi);
            }

            return this;
        }

        /** Reacts to action in popup menu. Switches the configuration.
        */
        public void actionPerformed (ActionEvent evt) {
            ToolbarPool.getDefault().setConfiguration (evt.getActionCommand ());
        }

    }
} // end of ToolbarPool

/*
 * Log
 *  31   Gandalf   1.30        1/18/00  Jaroslav Tulach Configuration can be 
 *       component with getName () => null
 *  30   Gandalf   1.29        1/16/00  Libor Kramolis  
 *  29   Gandalf   1.28        1/13/00  Ian Formanek    NOI18N
 *  28   Gandalf   1.27        1/12/00  Ian Formanek    NOI18N
 *  27   Gandalf   1.26        12/17/99 Libor Kramolis  
 *  26   Gandalf   1.25        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  25   Gandalf   1.24        8/18/99  Ian Formanek    Generated serial version
 *       UID
 *  24   Gandalf   1.23        8/17/99  David Simonek   persistent main window 
 *       positioning issues
 *  23   Gandalf   1.22        8/9/99   Ian Formanek    Generated Serial Version
 *       UID
 *  22   Gandalf   1.21        7/29/99  Libor Kramolis  
 *  21   Gandalf   1.20        7/22/99  Libor Kramolis  
 *  20   Gandalf   1.19        7/22/99  Ian Formanek    Fixed Bug # 1957 - 
 *       Creating new package under Session Settings | Toolbars node causes 
 *       disappearance of Component Pallete and added items to this new package 
 *       aren't displayed.  Fixed Bug #2085 - After first startup toolbars are 
 *       inconsistent, component palette missing.
 *  19   Gandalf   1.18        6/22/99  Libor Kramolis  
 *  18   Gandalf   1.17        6/17/99  David Simonek   various serialization 
 *       bugfixes
 *  17   Gandalf   1.16        6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  16   Gandalf   1.15        6/7/99   Jaroslav Tulach #2080
 *  15   Gandalf   1.14        6/4/99   Libor Kramolis  
 *  14   Gandalf   1.13        5/15/99  Libor Kramolis  
 *  13   Gandalf   1.12        5/13/99  Libor Kramolis  
 *  12   Gandalf   1.11        5/11/99  Jaroslav Tulach ToolbarPool changed to 
 *       look better in Open API
 *  11   Gandalf   1.10        5/10/99  Jesse Glick     [JavaDoc] - removed 
 *       stuff that would not look good in public from JavaDoc.
 *  10   Gandalf   1.9         5/10/99  Ian Formanek    Patched to compile
 *  9    Gandalf   1.8         5/7/99   Libor Kramolis  
 *  8    Gandalf   1.7         4/8/99   Libor Kramolis  
 *  7    Gandalf   1.6         4/7/99   Libor Kramolis  
 *  6    Gandalf   1.5         4/4/99   Ian Formanek    Latest Libor's version
 *  5    Gandalf   1.4         3/30/99  Ian Formanek    FolderInstance creation 
 *       in single thread
 *  4    Gandalf   1.3         3/26/99  Libor Kramolis  
 *  3    Gandalf   1.2         3/24/99  Libor Kramolis  
 *  2    Gandalf   1.1         3/9/99   Ian Formanek    
 *  1    Gandalf   1.0         2/17/99  Ian Formanek    
 * $
 */
