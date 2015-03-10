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

package org.openide.actions;

import java.util.ResourceBundle;
import java.awt.Dialog;
import java.awt.Window;
import java.awt.Rectangle;
import java.awt.event.*;
import java.io.*;
import java.util.HashSet;
import javax.swing.*;

import org.openide.*;
import org.openide.loaders.*;
import org.openide.cookies.*;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileLock;
import org.openide.explorer.propertysheet.*;
import org.openide.util.*;
import org.openide.util.actions.CookieAction;
import org.openide.nodes.*;

/** Customize a JavaBean.
* Opens a Property Sheet and allows making a
* serialized prototype from the modified object.
* <p>This class is final only for performance reasons.
*
* @author   Ian Formanek
*/
public final class CustomizeBeanAction extends CookieAction {
    /** generated Serialized Version UID */
    static final long serialVersionUID = -6378495195905487716L;

    /* Actually performs this action */
    protected void performAction (final Node[] activatedNodes) {
        // posts the request to request to have free AWT-Event-Queue thread
        RequestProcessor.postRequest (new Runnable () {
                                          public void run () {
                                              if (compileNodes (activatedNodes)) {
                                                  customize ((InstanceCookie)activatedNodes[0].getCookie(InstanceCookie.class));
                                              }
                                          }
                                      });
    }

    /** Execute some data objects.
    *
    * @param nodes the array of nodes to compile
    * @return true if compilation succeeded or was not performed, false if compilation failed
    */
    public static boolean compileNodes(Node[] nodes) {
        // search all nodes with unique ExecCookies/StartCookies -
        // - it is possible, that multiple activated nodes have the same exec cookie and
        // we have to prevent running it multiple times
        HashSet compile = new HashSet ();

        for (int i = 0; i < nodes.length; i++) {
            CompilerCookie comp = (CompilerCookie) nodes[i].getCookie(CompilerCookie.Compile.class);
            if (comp != null) {
                compile.add(comp);
            }
        }
        // compile
        if (!AbstractCompileAction.compile(java.util.Collections.enumeration(compile),
                                           AbstractCompileAction.findName(nodes))) {
            return false;
        }

        return true;
    }

    /* Exactly one selected node.
    */
    protected int mode () {
        return MODE_EXACTLY_ONE;
    }

    /* @return InstanceCookie node
    */
    protected Class[] cookieClasses () {
        return new Class[] { InstanceCookie.class };
    }


    /* Human presentable name of the action. This should be
    * presented as an item in a menu.
    * @return the name of the action
    */
    public String getName () {
        return ActionConstants.BUNDLE.getString ("CustomizeBean");
    }

    /* Help context where to find more about the acion.
    * @return the help context for this action
    */
    public HelpCtx getHelpCtx () {
        return new HelpCtx (CustomizeBeanAction.class);
    }

    /* Resource name for the icon.
    * @return resource name
    */
    protected String resourceIcon () {
        return "/org/openide/resources/actions/customize.gif"; // NOI18N
    }

    /** Customize a Bean.
    * @param cookie the object which can be instantiated
    */
    public static void customize (final InstanceCookie cookie) {
        if (cookie == null) return;

        final ResourceBundle bundle = ActionConstants.BUNDLE;

        Window w = null; // Visual rep. of bean
        Object b = null; // BEAN
        try {
            b = cookie.instanceCreate ();
        } catch (Exception ex) {
            TopManager.getDefault ().notifyException (ex);
            return;
        }
        final Object bean = b;

        // show visual repres. of bean
        if (bean instanceof java.awt.Window) {
            w = (java.awt.Window)bean;
            w.addWindowListener (new java.awt.event.WindowAdapter () {
                                     public void windowClosing(java.awt.event.WindowEvent e) {
                                         e.getWindow ().dispose ();
                                     }
                                 });
        } else
            if (bean instanceof java.awt.Component) {
                w = new PropertyDialogManager (//null,
                        bundle.getString ("CTL_Component_Title"),
                        (java.awt.Component)bean,
                        false
                    ).getDialog ();
            }

        final Window window = w;

        // create propertysheet
        PropertySheet propertySheet = new PropertySheet ();
        Node[] nodes = new Node [1];
        BeanNode bn = null;
        try {
            bn = new BeanNode(bean);
        } catch (java.beans.IntrospectionException e) {
            TopManager.getDefault().notify(
                new NotifyDescriptor.Exception(e,
                                               bundle.getString("EXC_Introspection"))
            );
            return;
        }
        nodes [0] = bn;
        propertySheet.setNodes (nodes);

        final JButton ser = new JButton (bundle.getString ("CTL_Serialize"));
        final JButton serAs = new JButton (bundle.getString ("CTL_SerializeAs"));
        final JButton cancel = new JButton (bundle.getString ("CTL_Cancel"));
        // dialog[0] = opened dialog
        final Dialog dialog[] = new Dialog[1];

        boolean brr = java.io.Serializable.class.isAssignableFrom (bean.getClass ());
        serAs.setEnabled (brr);
        ser.setEnabled (brr && cookie instanceof InstanceCookie.Origin);

        ActionListener listener = new ActionListener () {
                                      public void actionPerformed (ActionEvent ev) {
                                          if (ev.getSource () == cancel) {
                                              dialog[0].dispose ();
                                              if (window != null) window.dispose ();
                                              return;
                                          }
                                          if (serializeJavaBean (bean,
                                                                 ev.getSource () == serAs ? null : ((InstanceCookie.Origin)cookie).instanceOrigin ()
                                                                )) {
                                              dialog[0].dispose ();
                                              if (window != null) window.dispose ();
                                          }
                                      }
                                  };

        DialogDescriptor descr = new DialogDescriptor (
                                     propertySheet,
                                     java.text.MessageFormat.format (bundle.getString ("FMT_CTL_CustomizeTitle"), new Object[] { bean.getClass ().getName () }),
                                     false, // modal
                                     new Object[] { ser, serAs, cancel }, // options
                                     cancel, // initial value
                                     DialogDescriptor.DEFAULT_ALIGN,
                                     new HelpCtx (CustomizeBeanAction.class.getName () + ".dialog"), // NOI18N
                                     listener
                                 );


        dialog[0] = TopManager.getDefault ().createDialog (descr);
        dialog[0].show ();
        if (window != null) {
            Rectangle r = dialog [0].getBounds ();
            window.setLocation (r.x + r.width, r.y);
            window.show ();
        }
    }

    /** Serialize a bean to file.
    *
    * @param bean the bean to be serialized
    * @param file the file to serialize to, or <code>null</code> to prompt the user for a destination
    * @return <code>true</code> if successful
    */
    public static boolean serializeJavaBean (final Object bean,
            final FileObject file) {
        final ResourceBundle bundle = ActionConstants.BUNDLE;
        FileObject parent = null;
        String name = null;
        org.openide.filesystems.FileSystem targetFS;

        try {
            if (file == null) {
                TopManager tm = TopManager.getDefault ();
                JTextField tf = new JTextField (20);
                try {
                    // selects one folder from data systems
                    DataFolder df = (DataFolder)tm.getNodeOperation ().select (
                                        bundle.getString ("CTL_SerializeAs"),
                                        bundle.getString ("CTL_SaveIn"),
                                        tm.getPlaces().nodes().repository(new FolderFilter()),
                                        new FolderAcceptor(),
                                        tf
                                    )[0].getCookie(DataFolder.class);
                    parent = df.getPrimaryFile ();
                    targetFS = parent.getFileSystem ();
                    name = tf.getText ();
                } catch (org.openide.util.UserCancelException ex) {
                    return false;
                }
            } else {
                parent = file.getParent ();
                name = file.getName ();
                targetFS = file.getFileSystem ();
            }
        } catch (org.openide.filesystems.FileStateInvalidException e) {
            TopManager.getDefault ().notify (new NotifyDescriptor.Exception (e, bundle.getString ("EXC_Serialization") + " " + name));
            return false;
        }

        final String fileName = name;
        final FileObject parentFile = parent;
        try {
            targetFS.runAtomicAction(new org.openide.filesystems.FileSystem.AtomicAction() {
                                         public void run() throws IOException {
                                             ByteArrayOutputStream baos = null;
                                             ObjectOutputStream oos = null;
                                             OutputStream os = null;
                                             FileObject serFile = null;
                                             FileLock lock = null;
                                             try {
                                                 oos = new java.io.ObjectOutputStream (baos = new ByteArrayOutputStream ());
                                                 oos.writeObject (bean);
                                                 if ((serFile = parentFile.getFileObject (fileName, "ser")) == null) // NOI18N
                                                     serFile = parentFile.createData (fileName, "ser"); // NOI18N
                                                 lock = serFile.lock ();
                                                 oos.close ();
                                                 baos.writeTo (os = serFile.getOutputStream (lock));
                                             }
                                             finally {
                                                 if (lock != null) lock.releaseLock ();
                                                 if (os != null) os.close ();
                                             }
                                         }
                                     }
                                    );
        } catch (Exception e) {
            TopManager.getDefault ().notify (
                new NotifyDescriptor.Exception (e, bundle.getString ("EXC_Serialization") + " " +
                                                parent.getPackageName ('.') + '.' + name)
            );
            return false;
        }

        return true;
    }

    /** Filter for save as operation, accepts folders. */
    private static final class FolderFilter implements DataFilter {
        static final long serialVersionUID =6754682007992329276L;
        /** Accepts only data folders but ignore read only roots of file systems
        */
        public boolean acceptDataObject (DataObject obj) {
            return obj instanceof DataFolder &&
                   (!obj.getPrimaryFile ().isReadOnly () ||
                    obj.getPrimaryFile ().getParent () != null);
        }
    } // end of FolderFilter inner class

    /** Node acceptor that accepts read-write folders only */
    private static final class FolderAcceptor implements NodeAcceptor {
        public boolean acceptNodes (Node[] nodes) {
            if ((nodes == null) || (nodes.length == 0)) return false;
            DataFolder cookie =
                (DataFolder)nodes[0].getCookie (DataFolder.class);
            return nodes.length == 1 && cookie != null &&
                   !cookie.getPrimaryFile().isReadOnly();
        }
    } // end of FolderAcceptor inner class

}

/*
 * Log
 *  23   Gandalf   1.22        1/12/00  Ian Formanek    NOI18N
 *  22   Gandalf   1.21        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  21   Gandalf   1.20        8/9/99   Ian Formanek    Generated Serial Version
 *       UID
 *  20   Gandalf   1.19        8/4/99   Jan Jancura     Bug 2335
 *  19   Gandalf   1.18        7/26/99  Ian Formanek    Fixed bug 2151 - 
 *       Customization of a Bean doesn't work 
 *  18   Gandalf   1.17        7/25/99  Ian Formanek    real class name 
 *       displayed in the title
 *  17   Gandalf   1.16        7/25/99  Ian Formanek    Fixed bug #1908 - 
 *       BeanCustomizer doesn't show the name of bean it's editing.
 *  16   Gandalf   1.15        7/25/99  Ian Formanek    Fixed bug 2333 - Could 
 *       not customize Bean and serialize.
 *  15   Gandalf   1.14        6/24/99  Jesse Glick     Gosh-honest HelpID's.
 *  14   Gandalf   1.13        6/9/99   Ian Formanek    Fixed resources for 
 *       package change
 *  13   Gandalf   1.12        6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  12   Gandalf   1.11        5/28/99  Ian Formanek    Cleaned up source 
 *       (imports, ... - no semantic/english text change)
 *  11   Gandalf   1.10        5/27/99  Jaroslav Tulach Executors rearanged.
 *  10   Gandalf   1.9         5/2/99   Ian Formanek    Obsoleted 
 *       help->DEFAULT_HELP
 *  9    Gandalf   1.8         4/27/99  Jesse Glick     new HelpCtx () -> 
 *       HelpCtx.DEFAULT_HELP.
 *  8    Gandalf   1.7         3/26/99  Jesse Glick     [JavaDoc]
 *  7    Gandalf   1.6         3/20/99  Jaroslav Tulach DialogDescriptor has 
 *       only ActionListener
 *  6    Gandalf   1.5         2/17/99  Ian Formanek    Updated icons to point 
 *       to the right package (under ide/resources)
 *  5    Gandalf   1.4         1/25/99  David Peroutka  
 *  4    Gandalf   1.3         1/20/99  David Simonek   rework of class DO
 *  3    Gandalf   1.2         1/7/99   Ian Formanek    fixed resource names
 *  2    Gandalf   1.1         1/6/99   Jaroslav Tulach 
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
