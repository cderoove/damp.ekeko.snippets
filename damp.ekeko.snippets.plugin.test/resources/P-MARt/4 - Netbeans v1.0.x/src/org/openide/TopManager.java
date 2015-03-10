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

package org.openide;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.event.*;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import javax.swing.SwingUtilities;
import javax.swing.text.Keymap;

import org.openide.compiler.CompilationEngine;
import org.openide.cookies.ProjectCookie;
import org.openide.debugger.Debugger;
import org.openide.debugger.DebuggerNotFoundException;
import org.openide.execution.ExecutionEngine;
import org.openide.filesystems.Repository;
import org.openide.loaders.*;
import org.openide.nodes.Node;
import org.openide.nodes.NodeAcceptor;
import org.openide.options.ControlPanel;
import org.openide.util.HelpCtx;
import org.openide.util.UserCancelException;
import org.openide.util.datatransfer.ExClipboard;
import org.openide.windows.OutputWriter;
import org.openide.windows.InputOutput;
import org.openide.windows.WindowManager;

/** Heart of the whole IDE.
* Provides initial access to all features in the system.
* Use {@link #getDefault} to obtain the default instance in the system.
*
* @author Jaroslav Tulach, Ales Novak, Ian Formanek, Petr Hamernik,
*   Jan Jancura, Dafe Simonek
*/
public abstract class TopManager extends Object {
    /** Name of property for the debugger. */
    public static final String PROP_DEBUGGER = "debugger"; // NOI18N

    /** Name of property for the global keymap. */
    public static final String PROP_GLOBAL_KEYMAP = "globalKeymap"; // NOI18N

    /** Name of property for the Places object.
    * This is most likely to change when a new project is opened.
    */
    public static final String PROP_PLACES = "places"; // NOI18N

    /** default top manager */
    private static TopManager defaultTopManager;

    /**
     * Get the default top manager for the system.
     * @return the default
     */
    public static TopManager getDefault () {
        return defaultTopManager;
    }

    /** Set default top manager.
    * This may be called only once.
    * @param tm the new top manager
    * @exception SecurityException if the manager is already set
    */
    public static synchronized void setDefault (TopManager tm)
    throws SecurityException {
        if (defaultTopManager == null) {
            defaultTopManager = tm;
        } else {
            throw new SecurityException ();
        }
    }


    /** Get the Repository of user and system files.
    * @return the repository
    */
    public abstract Repository getRepository ();

    /** Display help.
    * @param helpCtx help context to be displayed in help window
    */
    public abstract void showHelp (HelpCtx helpCtx);

    /** Browse a document over HTTP.
    * @param url URL of WWW document to be shown
    */
    public abstract void showUrl (URL url);

    /** Support writing to the Output Window on the main tab.
    * @return a writer for the standard IDE output
    */
    public abstract OutputWriter getStdOut();

    /** Support reading from and writing to a specific tab on the Output Window.
    * If a tab with the supplied name already exists,
    * a new tab with the same name will be created regardless.
    * @param name desired name of the tab
    * @return an <code>InputOutput</code> class for accessing the new tab
    */
    public InputOutput getIO(String name) {
        return getIO(name, true);
    }

    /** Support reading from and writing to a specific tab on the Output Window.
    * If a tab with the supplied name already exists,
    * a new tab with the same name will be created regardless.
    * @param name desired name of the tab
    * @param newIO if <tt>true</tt> new <code>InputOutput</code> is returned else already used <code>InputOutput</code> is returned.
    * @return an <code>InputOutput</code> class for accessing the new tab
    */
    public abstract InputOutput getIO(String name, boolean newIO);

    // Must be redefined in subclasses.
    // no shit; it's abstract --jglick
    /** Get global system clipboard.
    *
    * @return the clipboard
    */
    public abstract ExClipboard getClipboard ();

    /** Get system control panel.
    * This permits all current user options to be examined, among other things.
    * @return the control panel
    */
    public abstract ControlPanel getControlPanel ();

    /** Getter for registry of services registered to the system.
    * @return the singleton with all services
    */
    public abstract ServiceType.Registry getServices ();

    /** Notify the user of an otherwise unhandled Java exception.
    * This method provides a convenient way of handling exceptions
    * occurring in the IDE. If you catch an exception and wish to inform
    * the user about it (though no response is required), you may send the message
    * to this method. The manager handles the rest.
    * <P>
    * The default implementation uses {@link #notify} to alert the user at a critical level.
    *
    * @param ex the exception
    */
    public void notifyException (final Throwable ex) {
        SwingUtilities.invokeLater(new Runnable() {
                                       public void run () {
                                           TopManager.this.notify (new NotifyDescriptor.Exception(ex));
                                       }
                                   });
    }

    /** Notify the user of something in a message box, possibly with feedback.
    * @param nd description of the notification
    * @return the option that caused the message box to be closed
    */
    public abstract Object notify (NotifyDescriptor descriptor);

    /** Get a new standard dialog.
    * The dialog is designed and created as specified in the parameter.
    * Anyone who wants a dialog with standard buttons and
    * standard behavior should use this method.
    *
    * @param descriptor general description of the dialog
    */
    public abstract Dialog createDialog (DialogDescriptor descriptor);

    /** Show text in the IDE's status line.
    * @param text the text to be shown
    */
    public abstract void setStatusText(String text);

    /** Get global keyboard-shortcut map.
     * @return default root of shortcuts
     */
    public abstract Keymap getGlobalKeymap();

    /** Get default compilation engine.
     * @return default compilation engine in the system */
    public abstract CompilationEngine getCompilationEngine();

    /** Get default execution engine.
    * @return default execution engine in the system
    */
    public abstract ExecutionEngine getExecutionEngine();

    /** Get the default debugger.
    *
    * @return default debugger in the system (never <code>null</code>)
    * @throws DebuggerNotFoundException in case of a problem (for example, there is no debugger installed)
    */
    public abstract Debugger getDebugger () throws DebuggerNotFoundException;

    /** Save all opened objects. */
    public abstract void saveAll ();

    /** Exit the IDE.
    * This method will return only if {@link System#exit <code>System.exit()</code>} fails, or if at least one component of the
    * system refuses to exit (because it cannot be properly shut down).
    */
    public abstract void exit ();

    /** Get default data loader pool.
    * @return default loader pool in the system
    */
    public abstract DataLoaderPool getLoaderPool ();

    /** Get default handler for node customization and exploration.
    * @return default node operation manager in the system
    */
    public abstract NodeOperation getNodeOperation ();

    /** Get object providing locations of important places in the system.
    * @return the descriptor
    */
    public abstract Places getPlaces ();

    /** Opens specified project. Asks to save the previously opened project.
    * @exception IOException if error occurs accessing the project
    * @exception UserCancelException if the selection is interrupted by the user
    */
    public abstract void openProject (ProjectCookie project) throws IOException, UserCancelException;

    /** Get the window manager for the IDE.
    * The window manager is
    * responsible for the placement of windows and the handling of {@link TopComponent}s.
    * <P>
    * It is usually used only from the {@link org.openide.windows} package.
    *
    * @return default window manager
    */
    public abstract WindowManager getWindowManager ();

    /** Provide access to the system class loader.
    * This class loader allows loading of
    * IDE implementation classes in the startup class path, and all installed modules as well.
    * It does not load any classes from the
    * user Repository.
    * <P>
    * The class loader may change from call to call, for example due to a module installation.
    *
    * @return the system classloader
    */
    public abstract ClassLoader systemClassLoader ();

    /** Provide access to the user class loader.
     * This class loader can load everything provided by the {@link #systemClassLoader system one}, as
     * well as user classes in the {@link org.openide.filesystems.Repository Repository}.
    * <P>
    * The class loader may change from call to call, as it is affected by module and repository operations.
    * The returned classloader will reflect the current state of the
    * filesystem, taking into account modifications of <code>.class</code> files.
    *
    * @return the user classloader
    */
    public abstract ClassLoader currentClassLoader ();


    /** Add a listener to property changes in the TopManager.
    * @param l the listener to add
    */
    public abstract void addPropertyChangeListener (PropertyChangeListener l);

    /** Remove a listener to property changes in the TopManager.
    * @param l the listener to remove
    */
    public abstract void removePropertyChangeListener (PropertyChangeListener l);


    /** Provides common operations on nodes.
     * Any component may
    * ask to open a customizer for, or explore, any node.
    */
    public static abstract class NodeOperation {
        /** Tries to open a customization dialog for the specified node.
         * The dialog is
        * modal and the function returns only after
        * customization is finished, if it was possible.
        *
        * @param n the node to customize
        * @return <CODE>true</CODE> if the node had a customizer,
        * <CODE>false</CODE> if not
        * @see Node#hasCustomizer
        * @see Node#getCustomizer
        */
        public abstract boolean customize (Node n);

        /** Explore a node (and its subhierarchy).
         * It will be opened in a new Explorer view, as the root node of that window.
        * @param n the node to explore
        */
        public abstract void explore (Node n);

        /** Open a modal Property Sheet on a node.
        * @param n the node to show properties of
        */
        public abstract void showProperties (Node n);

        /** Open a modal Property Sheet on a set of nodes.
        * @param n the array of nodes to show properties of
        * @see #showProperties(Node)
        */
        public abstract void showProperties (Node[] n);

        /** Open a modal Explorer on a root node, permitting a node selection to be returned.
        * <p>The acceptor
        * should be asked each time the set of selected nodes changes, whether to accept or
        * reject the current result. This will affect for example the
        * display of the "OK" button.
        *
        * @param title title of the dialog
        * @param rootTitle label at root of dialog. May use <code>&amp;</code> for a {@link javax.swing.JLabel#setDisplayedMnemonic(int) mnemonic}.
        * @param root root node to explore
        * @param acceptor class asked to accept or reject current selection
        * @param top an extra component to be placed on the dialog (may be <code>null</code>)
        * @return an array of selected (and accepted) nodes
        *
        * @exception UserCancelException if the selection is interrupted by the user
        */
        public abstract Node[] select (String title, String rootTitle, Node root, NodeAcceptor acceptor, Component top)
        throws UserCancelException;

        /** Open a modal Explorer without any extra dialog component.
        * @param title title of the dialog
        * @param rootTitle label at root of dialog. May use <code>&amp;</code> for a {@link javax.swing.JLabel#setDisplayedMnemonic(int) mnemonic}.
        * @param root root node to explore
        * @param acceptor class asked to accept or reject current selection
        * @return an array of selected (and accepted) nodes
        *
        * @exception UserCancelException if the selection is interrupted by the user
        * @see #select(String, String, Node, NodeAcceptor, Component)
        */
        public Node[] select (String title, String rootTitle, Node root, NodeAcceptor acceptor)
        throws UserCancelException  {
            return select (title, rootTitle, root, acceptor, null);
        }

        /** Open a modal Explorer accepting only a single node.
        * @param title title of the dialog
        * @param rootTitle label at root of dialog. May use <code>&amp;</code> for a {@link javax.swing.JLabel#setDisplayedMnemonic(int) mnemonic}.
        * @param root root node to explore
        * @return the selected node
        *
        * @exception UserCancelException if the selection is interrupted by the user
        * @see #select(String, String, Node, NodeAcceptor)
        */
        public final Node select (String title, String rootTitle, Node root) throws UserCancelException {
            return select (title, rootTitle, root, new NodeAcceptor () {
                               public boolean acceptNodes (Node[] nodes) {
                                   return nodes.length == 1;
                               }
                           })[0];
        }
    }
}

/*
 * Log
 *  7    Tuborg    1.6         10/06/98 David Peroutka  New notification system
 *  6    Tuborg    1.5         09/18/98 Jaroslav Tulach currentClassLoader
 *  5    Tuborg    1.4         08/18/98 Petr Hamernik   confirmDeleteObject remove
 *                                                      from
 *                                                      TopManager.Confirmation
 *  4    Tuborg    1.3         07/27/98 Jaroslav Tulach TopManager.getDefault
 *                                                      ().getDefaultMultiFrame ()
 *                                                      added
 *
 *  3    Tuborg    1.2         07/07/98 Petr Hamernik   creating new packages -
 *                                                      input dialog
 *  2    Tuborg    1.1         06/15/98 Ian Formanek
 *  1    Tuborg    1.0         06/11/98 David Peroutka
 * $
 * Beta Change History:
 *  0    Tuborg    0.32        --/--/98 Jan Formanek    added method setStatusText(String)
 *  0    Tuborg    0.33        --/--/98 Jan Formanek    setStatusText impl moved to CoronaTopManager
 *  0    Tuborg    0.34        --/--/98 Ales Novak      implements TopOutput
 *  0    Tuborg    0.35        --/--/98 Jaroslav Tulach getHelp replaced by showHelp. Constructor changed.
 *  0    Tuborg    0.36        --/--/98 Jaroslav Tulach added getDataLoaderPool method
 *  0    Tuborg    0.37        --/--/98 Jaroslav Tulach changed to work with TopComponent
 *  0    Tuborg    0.38        --/--/98 Jan Formanek    removed implements TopOutput, added methods getStdOut, getIO
 *  0    Tuborg    0.39        --/--/98 Jaroslav Tulach changed top focus listener
 *  0    Tuborg    0.41        --/--/98 Jaroslav Tulach deleted support for focus listener, instead use TopFrameReg
 *  0    Tuborg    0.41        --/--/98 Jaroslav Tulach listeners to listen to frame activations
 *  0    Tuborg    0.42        --/--/98 Jan Formanek    lazy initialization of execEngine and compEngine
 *  0    Tuborg    0.43        --/--/98 Jaroslav Tulach notifyException now takes ExceptionDescriptor
 *  0    Tuborg    0.44        --/--/98 Jaroslav Tulach new interface for projects
 *  0    Tuborg    0.45        --/--/98 Jaroslav Tulach interface to explorer
 *  0    Tuborg    0.46        --/--/98 Petr Hamernik   default root of key shortcuts added
 *  0    Tuborg    0.47        --/--/98 Jan Jancura     getDebugger added
 *  0    Tuborg    0.48        --/--/98 Ales Novak      exit
 *  0    Tuborg    0.49        --/--/98 Jan Formanek    explore method added
 *  0    Tuborg    0.50        --/--/98 Jan Formanek    notifyException(Exception) changed to notifyException(Throwsable)
 *  0    Tuborg    0.51        --/--/98 Petr Hamernik   placeFrame added
 *  0    Tuborg    0.53        --/--/98 Jan Formanek    showProperties methods added to NodeOperation
 *  0    Tuborg    0.54        --/--/98 Ales Novak      ProjectOperation.save takes ObjectOutStr instead of os
 *  0    Tuborg    0.55        --/--/98 Jan Formanek    updateUI method added
 *  0    Tuborg    0.56        --/--/98 Jaroslav Tulach ExClipboard
 *  0    Tuborg    0.57        --/--/98 Jaroslav Tulach workspace element
 *  0    Tuborg    0.58        --/--/98 Jaroslav Tulach notify (NotifyDescriptor)
 *  0    Tuborg    0.61        --/--/98 Jan Jancura     showURL method
 *  0    Tuborg    0.62        --/--/98 Jan Formanek    explore moved to NodeOperation
 */
