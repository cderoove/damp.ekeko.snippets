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

package org.netbeans.modules.apisupport;

import java.awt.*;
import java.beans.*;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.*;
import javax.swing.JPanel;

import org.openide.*;
import org.openide.actions.*;
import org.openide.awt.SplittedPanel;
import org.openide.cookies.InstanceCookie;
import org.openide.execution.NbClassLoader;
import org.openide.execution.NbClassPath;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerPanel;
import org.openide.explorer.propertysheet.PropertySheetView;
import org.openide.explorer.view.BeanTreeView;
import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.openide.nodes.*;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.*;

/** Helps set various otherwise hidden template properties used by {@link TemplateWizard}.
* @author Jesse Glick
*/
public class TemplateWizardHelperAction extends CookieAction {

    /** Attribute used by SystemFileSystem to look for a localizing bundle. */
    private final static String ATTR_BUNDLE = "SystemFileSystem.localizingBundle";

    private static final long serialVersionUID =-536850488915983782L;
    protected Class[] cookieClasses () {
        return new Class[] { DataObject.class };
    }

    protected int mode () {
        return MODE_EXACTLY_ONE;
    }

    public static class MyExplorerPanel extends ExplorerPanel {
        private static final long serialVersionUID = -5685493348821098378L;
        public MyExplorerPanel () {
            setName ("Installable File Helper");
            setLayout (new BorderLayout ());
            SplittedPanel split = new SplittedPanel ();
            split.setSplitType (SplittedPanel.HORIZONTAL);
            split.add (new BeanTreeView (), SplittedPanel.ADD_LEFT);
            split.add (new PropertySheetView (), SplittedPanel.ADD_RIGHT);
            add (split, BorderLayout.CENTER);
        }
        protected void updateTitle () {
            // Do nothing.
        }
    }
    /** Will open a dialog showing the enhanced property sheet for the template file. */
    protected void performAction (Node[] nodes) {
        Node n = nodes[0];
        DataObject dob = (DataObject) n.getCookie (DataObject.class);
        if (dob == null) return;
        ExplorerPanel panel = new MyExplorerPanel ();
        Node philtre = new Filter (n, dob);
        panel.getExplorerManager ().setRootContext (philtre);
        try {
            panel.getExplorerManager ().setSelectedNodes (new Node[] { philtre });
        } catch (PropertyVetoException pve) {
            pve.printStackTrace ();
        }
        panel.open ();
        panel.requestFocus ();
    }

    public String getName () {
        return "Installable File Helper ...";
    }

    protected String iconResource () {
        return "resources/TemplateWizardHelperIcon.gif";
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx ("org.netbeans.modules.apisupport.utils.tmplwizhlpr");
    }

    /** Only enabled on templates. */
    protected boolean enable (Node[] nodes) {
        if (! super.enable (nodes)) return false;
        DataObject dob = (DataObject) nodes[0].getCookie (DataObject.class);
        return dob.isTemplate () || dob.getPrimaryFile ().isFolder ();
    }

    /** The dummy node used to display a property sheet of. */
    private static class Filter extends FilterNode {

        DataObject dob;

        public Filter (Node orig, DataObject dob) {
            super (orig, (dob instanceof DataFolder) ? new FilterChildren (orig) : Children.LEAF);
            this.dob = dob;
        }

        private static class MyHandle implements Node.Handle {
            private Node.Handle orig;
            private DataObject obj;
            private static final long serialVersionUID = 6315937780815111350L;
            public MyHandle (Node origNode, DataObject obj) {
                orig = origNode.getHandle ();
                this.obj = obj;
            }
            public Node getNode () throws IOException {
                return new Filter (orig.getNode (), obj);
            }
        }
        public Node.Handle getHandle () {
            return new MyHandle (getOriginal (), dob);
        }

        public SystemAction[] getActions () {
            return new SystemAction[] {
                       SystemAction.get (PopulateLocBundleAction.class),
                       null,
                       SystemAction.get (ToolsAction.class),
                       SystemAction.get (PropertiesAction.class),
                   };
        }

        private final Node.Property iteratorProp = new IteratorProp ();
        private final Node.Property urlProp = new UrlProp ();
        private final Node.Property rsrcProp = new RsrcProp ();
        private final Node.Property locBundleProp = new LocBundleProp ();
        private final Node.Property locNameProp = new LocNameProp ();

        public Node.PropertySet[] getPropertySets () {
            Node.PropertySet[] orig = super.getPropertySets ();
            Node.PropertySet[] nue = new Node.PropertySet[orig.length + 1];
            System.arraycopy (orig, 0, nue, 1, orig.length);
            nue[0] = new Node.PropertySet ("tmplwizhlpr", "Extra", "Extra properties applicable to templates and installable files.") {
                         public Node.Property[] getProperties () {
                             return new Node.Property[] { iteratorProp, urlProp, rsrcProp, locBundleProp, locNameProp };
                         }
                     };
            return nue;
        }

        /** A property permitting the {@link TemplateWizard.Iterator} to be set for a template. */
        private final class IteratorProp extends PropertySupport.ReadWrite {
            public IteratorProp () {
                super ("iterator",
                       TemplateWizard.Iterator.class,
                       "Sequence of Panels",
                       "An object implementing TemplateWizard.Iterator representing the template panels.");
            }
            public Object getValue () {
                return TemplateWizard.getIterator (dob);
            }
            public void setValue (Object val) throws InvocationTargetException {
                try {
                    TemplateWizard.setIterator (dob, (TemplateWizard.Iterator) val);
                } catch (IOException ioe) {
                    throw new InvocationTargetException (ioe);
                }
            }
            public boolean supportsDefaultValue () {
                return true;
            }
            public void restoreDefaultValue () throws InvocationTargetException {
                setValue (null);
            }
            public PropertyEditor getPropertyEditor () {
                return new IteratorPropEd ();
            }
        }

        /** A property for a template's description URL. */
        private final class UrlProp extends PropertySupport.ReadWrite {
            public UrlProp () {
                super ("url", URL.class, "Description URL", "Raw URL to a description page.");
            }
            public Object getValue () {
                return TemplateWizard.getDescription (dob);
            }
            public void setValue (Object val) throws InvocationTargetException {
                try {
                    TemplateWizard.setDescription (dob, (URL) val);
                } catch (IOException ioe) {
                    throw new InvocationTargetException (ioe);
                }
            }
            public boolean supportsDefaultValue () {
                return true;
            }
            public void restoreDefaultValue () throws InvocationTargetException {
                setValue (null);
            }
        }

        // accessor method
        void fPC (String prop, Object old, Object nue) {
            firePropertyChange (prop, old, nue);
        }

        /** A property for a template's description, considered as a classloader resource path. */
        private final class RsrcProp extends PropertySupport.ReadWrite {
            public RsrcProp () {
                super ("resourceDesc", String.class, "Description Resource", "Resource path to a description page.");
            }
            public Object getValue () {
                return TemplateWizard.getDescriptionAsResource (dob);
            }
            public void setValue (Object val) throws InvocationTargetException {
                try {
                    TemplateWizard.setDescriptionAsResource (dob, (String) val);
                    // The resourceDesc property will auto-fire changes, but changing it also may affect url:
                    fPC ("url", null, null);
                } catch (IOException ioe) {
                    ioe.printStackTrace ();
                    throw new InvocationTargetException (ioe);
                } catch (RuntimeException re) {
                    re.printStackTrace ();
                    throw re;
                }
            }
            public boolean supportsDefaultValue () {
                return true;
            }
            public void restoreDefaultValue () throws InvocationTargetException {
                setValue (null);
            }
            public PropertyEditor getPropertyEditor () {
                return new RsrcPropEd ();
            }
        }

        /** Property for the bundle file holding the file's localized name. */
        private final class LocBundleProp extends PropertySupport.ReadWrite {
            public LocBundleProp () {
                super ("locBundle", String.class, "Localizing Bundle", "Resource name of a bundle giving localized file names.");
            }
            public Object getValue () {
                return dob.getPrimaryFile ().getAttribute (ATTR_BUNDLE);
            }
            public void setValue (Object val) throws InvocationTargetException {
                try {
                    dob.getPrimaryFile ().setAttribute (ATTR_BUNDLE, val);
                    // If set, may affect the name:
                    fPC ("locName", null, null);
                } catch (IOException ioe) {
                    ioe.printStackTrace ();
                    throw new InvocationTargetException (ioe);
                }
            }
            public boolean supportsDefaultValue () {
                return true;
            }
            public void restoreDefaultValue () throws InvocationTargetException {
                setValue (null);
            }
            public PropertyEditor getPropertyEditor () {
                return new LocBundlePropEd ();
            }
        }

        /** Property for the localized name of the file, if any. */
        private final class LocNameProp extends PropertySupport.ReadOnly {
            public LocNameProp () {
                super ("locName", String.class, "Localized Name", "Localized name of this file in the current locale.");
            }
            public Object getValue () {
                FileObject fo = dob.getPrimaryFile ();
                String foName = fo.getPackageNameExt ('/', '.');
                String bundleName = (String) fo.getAttribute (ATTR_BUNDLE);
                if (bundleName == null) return "<unlocalized>";
                // Note: using (+/-) currentClassLoader for testing purposes, though at deploy time
                // only systemClassLoader is used.
                // NbClassLoader is needed because modifying the bundle does not seem to
                // properly refresh the currentClassLoader.
                // Also, first attempt tries to load from Repo even if this overrides
                // something in the system classpath (as is common in this case).
                // Second attempt falls back to IDE module classloader.
                ResourceBundle bundle;
                try {
                    bundle = NbBundle.getBundle (bundleName, Locale.getDefault (),
                                                 new NbClassLoader (TopManager.getDefault ().getRepository ().toArray (), null));
                } catch (MissingResourceException mre) {
                    try {
                        bundle = NbBundle.getBundle (bundleName, Locale.getDefault (),
                                                     TopManager.getDefault ().systemClassLoader ());
                    } catch (MissingResourceException mre2) {
                        return "<bundle not found>";
                    }
                }
                try {
                    return bundle.getString (foName);
                } catch (MissingResourceException mre) {
                    //System.err.println ("Keys:");
                    //Enumeration e = bundle.getKeys ();
                    //while (e.hasMoreElements ())
                    //  System.err.println ("\t" + e.nextElement ());
                    return "<key " + foName + " not found>";
                }
            }
        }

    }

    private static class FilterChildren extends FilterNode.Children {

        public FilterChildren (Node orig) {
            super (orig);
        }

        protected Node[] createNodes (Object key) {
            DataObject dob = (DataObject) ((Node) key).getCookie (DataObject.class);
            return new Node[] { new Filter ((Node) key, dob) };
        }

    }

    /** Custom property editor for the iterator.
    * Displays current value as an object, but can set a class name.
    * Custom editor also permits you to select the instance (usually Java source)
    * to use for the iterator.
    */
    private static final class IteratorPropEd extends PropertyEditorSupport {
        public String getAsText () {
            return String.valueOf (getValue ());
        }
        public void setAsText (String text) throws IllegalArgumentException {
            try {
                setValue ("null".equals (text) ?
                          null :
                          Beans.instantiate (TopManager.getDefault ().currentClassLoader (), text));
            } catch (Exception e) {
                throw new IllegalArgumentException (e.toString ());
            }
        }
        public boolean supportsCustomEditor () {
            return true;
        }

        public Component getCustomEditor () {
            ExplorerPanel panel = new ExplorerPanel ();
            panel.setLayout (new BorderLayout ());
            panel.add (new BeanTreeView (), BorderLayout.CENTER);
            ExplorerManager mgr = panel.getExplorerManager ();
            mgr.setRootContext (TopManager.getDefault ().getPlaces ().nodes ().repository ());
            mgr.addVetoableChangeListener (new VetoableChangeListener () {
                                               public void vetoableChange (PropertyChangeEvent ev) throws PropertyVetoException {
                                                   if (ExplorerManager.PROP_SELECTED_NODES.equals (ev.getPropertyName ())) {
                                                       Node[] nodes = (Node[]) ev.getNewValue ();
                                                       if (nodes == null || nodes.length > 1) throw new PropertyVetoException ("Must select one node", ev);
                                                       if (nodes.length == 0) return;
                                                       InstanceCookie inst = (InstanceCookie) nodes[0].getCookie (InstanceCookie.class);
                                                       try {
                                                           if (inst == null || ! TemplateWizard.Iterator.class.isAssignableFrom (inst.instanceClass ()))
                                                               throw new PropertyVetoException ("Must be a TemplateWizard.Iterator", ev);
                                                       } catch (Exception e) {
                                                           throw new PropertyVetoException (e.toString (), ev);
                                                       }
                                                   }
                                               }
                                           });
            mgr.addPropertyChangeListener (new PropertyChangeListener () {
                                               public void propertyChange (PropertyChangeEvent ev) {
                                                   if (ExplorerManager.PROP_SELECTED_NODES.equals (ev.getPropertyName ())) {
                                                       Node[] nodes = (Node[]) ev.getNewValue ();
                                                       if (nodes.length == 0) return;
                                                       InstanceCookie inst = (InstanceCookie) nodes[0].getCookie (InstanceCookie.class);
                                                       try {
                                                           setValue (inst.instanceCreate ());
                                                       } catch (Exception e) {
                                                           e.printStackTrace();
                                                       }
                                                   }
                                               }
                                           });
            JPanel outpanel = new JPanel ();
            outpanel.setLayout (new BorderLayout ());
            outpanel.add (panel, BorderLayout.CENTER);
            HelpCtx.setHelpIDString (outpanel, "org.netbeans.modules.apisupport.utils.tmplwizhlpr");
            return outpanel;
        }
    }

    /** Special property editor for the description as a resource path.
    * Can directly type in the value, or browse to an HTML file in the Repository.
    */
    private static final class RsrcPropEd extends PropertyEditorSupport {
        public String getAsText () {
            String val = (String) getValue ();
            return (val == null) ? "null" : val;
        }
        public void setAsText (String text) {
            setValue ("null".equals (text) ? null : text);
        }
        public boolean supportsCustomEditor () {
            return true;
        }

        private boolean isHtml (DataObject dob) {
            return "text/html".equals (dob.getPrimaryFile ().getMIMEType ());
        }
        public Component getCustomEditor () {
            ExplorerPanel panel = new ExplorerPanel ();
            panel.setLayout (new BorderLayout ());
            panel.add (new BeanTreeView (), BorderLayout.CENTER);
            ExplorerManager mgr = panel.getExplorerManager ();
            mgr.setRootContext (TopManager.getDefault ().getPlaces ().nodes ().repository (new DataFilter () {
                                    public boolean acceptDataObject (DataObject dob) {
                                        return (dob.getCookie (DataFolder.class) != null) || isHtml (dob);
                                    }
                                }));
            mgr.addVetoableChangeListener (new VetoableChangeListener () {
                                               public void vetoableChange (PropertyChangeEvent ev) throws PropertyVetoException {
                                                   if (ExplorerManager.PROP_SELECTED_NODES.equals (ev.getPropertyName ())) {
                                                       Node[] nodes = (Node[]) ev.getNewValue ();
                                                       if (nodes == null || nodes.length > 1) throw new PropertyVetoException ("Must select one node", ev);
                                                       if (nodes.length == 0) return;
                                                       DataObject dob = (DataObject) nodes[0].getCookie (DataObject.class);
                                                       if (dob == null) throw new PropertyVetoException ("Must select a file", ev);
                                                       if (! isHtml (dob)) throw new PropertyVetoException ("Must be an HTML file", ev);
                                                   }
                                               }
                                           });
            mgr.addPropertyChangeListener (new PropertyChangeListener () {
                                               public void propertyChange (PropertyChangeEvent ev) {
                                                   if (ExplorerManager.PROP_SELECTED_NODES.equals (ev.getPropertyName ())) {
                                                       Node[] nodes = (Node[]) ev.getNewValue ();
                                                       if (nodes.length == 0) return;
                                                       DataObject dob = (DataObject) nodes[0].getCookie (DataObject.class);
                                                       if (dob == null || ! isHtml (dob)) return;
                                                       setValue (dob.getPrimaryFile ().getPackageNameExt ('/', '.'));
                                                   }
                                               }
                                           });
            JPanel outpanel = new JPanel ();
            outpanel.setLayout (new BorderLayout ());
            outpanel.add (panel, BorderLayout.CENTER);
            HelpCtx.setHelpIDString (outpanel, "org.netbeans.modules.apisupport.utils.tmplwizhlpr");
            return outpanel;
        }
    }

    /** Special property editor for bundle files.
    * Can directly type in the value, or browse to a .properties file in the Repository.
    */
    private static final class LocBundlePropEd extends PropertyEditorSupport {
        public String getAsText () {
            String val = (String) getValue ();
            return (val == null) ? "null" : val;
        }
        public void setAsText (String text) {
            setValue ("null".equals (text) ? null : text);
        }
        public boolean supportsCustomEditor () {
            return true;
        }

        private boolean isProperties (DataObject dob) {
            return "properties".equals (dob.getPrimaryFile ().getExt ());
        }
        public Component getCustomEditor () {
            ExplorerPanel panel = new ExplorerPanel ();
            panel.setLayout (new BorderLayout ());
            panel.add (new BeanTreeView (), BorderLayout.CENTER);
            ExplorerManager mgr = panel.getExplorerManager ();
            mgr.setRootContext (TopManager.getDefault ().getPlaces ().nodes ().repository (new DataFilter () {
                                    public boolean acceptDataObject (DataObject dob) {
                                        return (dob.getCookie (DataFolder.class) != null) || isProperties (dob);
                                    }
                                }));
            mgr.addVetoableChangeListener (new VetoableChangeListener () {
                                               public void vetoableChange (PropertyChangeEvent ev) throws PropertyVetoException {
                                                   if (ExplorerManager.PROP_SELECTED_NODES.equals (ev.getPropertyName ())) {
                                                       Node[] nodes = (Node[]) ev.getNewValue ();
                                                       if (nodes == null || nodes.length > 1) throw new PropertyVetoException ("Must select one node", ev);
                                                       if (nodes.length == 0) return;
                                                       DataObject dob = (DataObject) nodes[0].getCookie (DataObject.class);
                                                       if (dob == null) throw new PropertyVetoException ("Must select a file", ev);
                                                       if (! isProperties (dob)) throw new PropertyVetoException ("Must be a properties file", ev);
                                                   }
                                               }
                                           });
            mgr.addPropertyChangeListener (new PropertyChangeListener () {
                                               public void propertyChange (PropertyChangeEvent ev) {
                                                   if (ExplorerManager.PROP_SELECTED_NODES.equals (ev.getPropertyName ())) {
                                                       Node[] nodes = (Node[]) ev.getNewValue ();
                                                       if (nodes.length == 0) return;
                                                       DataObject dob = (DataObject) nodes[0].getCookie (DataObject.class);
                                                       if (dob == null || ! isProperties (dob)) return;
                                                       // [PENDING] could in principle fail if there was a bundle
                                                       // that did not have a default-locale file as its primary file
                                                       setValue (dob.getPrimaryFile ().getPackageName ('.'));
                                                   }
                                               }
                                           });
            JPanel outpanel = new JPanel ();
            outpanel.setLayout (new BorderLayout ());
            outpanel.add (panel, BorderLayout.CENTER);
            HelpCtx.setHelpIDString (outpanel, "org.netbeans.modules.apisupport.utils.tmplwizhlpr");
            return outpanel;
        }
    }

    public static class PopulateLocBundleAction extends NodeAction {

        private static final long serialVersionUID =3217348412906163076L;
        public String getName () {
            return "Populate bundle ...";
        }

        public HelpCtx getHelpCtx () {
            return new HelpCtx ("org.netbeans.modules.apisupport.utils.tmplwizhlpr");
        }

        protected boolean enable (Node[] nodes) {
            if (nodes.length == 0) return false;
            for (int i = 0; i < nodes.length; i++)
                if (! (nodes[i] instanceof Filter))
                    return false;
            return true;
        }

        protected void performAction (Node[] nodes) {
            PrintWriter pw = TopManager.getDefault ().getIO ("Populating Bundles", false).getOut ();
            pw.println ("Processing localization bundles...");
            for (int i = 0; i < nodes.length; i++)
                process ((Filter) nodes[i], pw);
        }

        private void process (Filter f, PrintWriter pw) {
            FileObject fo = f.dob.getPrimaryFile ();
            String foName = fo.getPackageNameExt ('/', '.');
            String bundleName = (String) fo.getAttribute (ATTR_BUNDLE);
            if (bundleName == null) {
                pw.println ("The file " + foName + " had no bundle set.");
            } else {
                pw.println ("File " + foName + " with bundle " + bundleName + "...");
                // Note: this is the default locale bundle, intentionally.
                String bundleResource = bundleName.replace ('.', '/') + ".properties";
                FileObject bundle = TopManager.getDefault ().getRepository ().findResource (bundleResource);
                if (bundle == null) {
                    pw.println ("The bundle named " + bundleResource + " could not be found.");
                } else {
                    try {
                        InputStream is = bundle.getInputStream ();
                        String contains;
                        try {
                            Properties p = new Properties ();
                            p.load (is);
                            contains = p.getProperty (foName);
                            //pw.println ("Props dump:");
                            //p.list (pw);
                        } finally {
                            is.close ();
                        }
                        if (contains != null) {
                            pw.println ("Already had localized name: " + contains);
                        } else {
                            String locName = DataObject.find (fo).getNodeDelegate ().getDisplayName ();
                            pw.println ("Will add key for the localized name: " + locName);
                            File bundleF = NbClassPath.toFile (bundle);
                            if (bundleF == null) {
                                pw.println ("Sorry, only output to local-disk files is supported currently.");
                            } else {
                                FileLock lock = bundle.lock ();
                                try {
                                    PrintStream ps = new PrintStream (new FileOutputStream (bundleF.toString (), true));
                                    try {
                                        // [PENDING] should ideally check for trailing newline...
                                        ps.print (escape (foName));
                                        ps.print ('=');
                                        ps.println (locName);
                                    } finally {
                                        ps.close ();
                                    }
                                } finally {
                                    lock.releaseLock ();
                                }
                            }
                        }
                    } catch (IOException ioe) {
                        pw.println ("Caught exception:");
                        ioe.printStackTrace (pw);
                    }
                }
            }
            f.fPC ("locName", null, null);
            if (! f.isLeaf ()) {
                Enumeration e = f.getChildren ().nodes ();
                while (e.hasMoreElements ())
                    process ((Filter) e.nextElement (), pw);
            }
        }

        private String escape (String text) {
            // Escapes it for a props file.
            StringBuffer escaped = new StringBuffer ();
            int len = text.length ();
            for (int i = 0; i < len; i++) {
                char c = text.charAt (i);
                if (Character.isWhitespace (c) || c == ':' || c == '=' || c == '#' || c == '!')
                    escaped.append ('\\');
                escaped.append (c);
            }
            return escaped.toString ();
        }

    }

}

/*
 * Log
 *  11   Gandalf-post-FCS1.7.1.2     4/3/00   Jesse Glick     Serializable dialog, 
 *       hopefully.
 *  10   Gandalf-post-FCS1.7.1.1     3/28/00  Jesse Glick     SVUIDs.
 *  9    Gandalf-post-FCS1.7.1.0     3/9/00   Jesse Glick     Minor fixes.
 *  8    Gandalf   1.7         1/18/00  Jesse Glick     Fixed LocNameProp getter
 *       algorithm to load from Repo preferentially. Also fixed Generate action 
 *       to use real node display name as sample, and not escape the value.
 *  7    Gandalf   1.6         1/16/00  Jesse Glick     Appending property keys.
 *  6    Gandalf   1.5         1/14/00  Jesse Glick     Support for system file 
 *       system's name localizations.
 *  5    Gandalf   1.4         12/22/99 Jesse Glick     Oops, and forgot to set 
 *       the top node too.
 *  4    Gandalf   1.3         12/22/99 Jesse Glick     Now shows a 
 *       tree-structure useful for whole dirs full of templates.
 *  3    Gandalf   1.2         12/22/99 Jesse Glick     All right, now just 
 *       enabled on any folder.
 *  2    Gandalf   1.1         12/21/99 Jesse Glick     Now active on 
 *       non-templates if they are in the templates folder.
 *  1    Gandalf   1.0         12/17/99 Jesse Glick     
 * $
 */
