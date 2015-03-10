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
import java.awt.event.*;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.ResourceBundle;
import javax.swing.JDialog;

import org.openide.*;
import org.openide.compiler.*;
import org.openide.compiler.Compiler;
import org.openide.debugger.*;
import org.openide.execution.*;
import org.openide.explorer.propertysheet.*;
import org.openide.filesystems.*;
import org.openide.cookies.CompilerCookie;
import org.openide.cookies.ExecCookie;
import org.openide.cookies.ArgumentsCookie;
import org.openide.cookies.DebuggerCookie;
import org.openide.nodes.Sheet;
import org.openide.nodes.PropertySupport;
import org.openide.util.NbBundle;
import org.openide.util.Mutex;
import org.openide.util.Utilities;

/** Support for execution of a class file. Looks for the class with
* the same base name as the primary file, locates a main method
* in it, and starts it.
*
* @author Jaroslav Tulach
*/
public class ExecSupport extends Object
    implements ExecCookie, ArgumentsCookie, DebuggerCookie {
    /** extended attribute for the type of executor */
    private static final String EA_EXECUTOR = "NetBeansAttrExecutor"; // NOI18N
    /** extended attribute for attributes */
    private static final String EA_ARGUMENTS = "NetBeansAttrArguments"; // NOI18N
    /** extended attribute for debugger type */
    private static final String EA_DEBUGGER_TYPE = "NetBeansAttrDebuggerType"; // NOI18N

    // copy from JavaNode
    /** Name of property providing argument parameter list. */
    public static final String PROP_FILE_PARAMS   = "params"; // NOI18N
    /** Name of property providing a custom {@link Executor} for a file. */
    public static final String PROP_EXECUTION     = "execution"; // NOI18N
    /** Name of property providing a custom {@link DebuggerType} for a file. */
    public static final String PROP_DEBUGGER_TYPE     = "debuggerType"; // NOI18N

    /** bundle */
    static ResourceBundle bundle;

    /** entry to be associated with */
    protected MultiDataObject.Entry entry;

    /** Create new support for given entry. The file is taken from the
    * entry and is updated if the entry moves or renames itself.
    * @param entry entry to create instance from
    */
    public ExecSupport (MultiDataObject.Entry entry) {
        this.entry = entry;
    }

    /* Starts the class.
    */
    public void start () {
        Executor exec = getExecutor (entry);
        if (exec == null) {
            exec = defaultExecutor ();
        }

        String[] params = getArguments ();

        try {
            exec.execute(new ExecInfo(entry.getFile().getPackageName ('.'), params));
        } catch (final IOException ex) {
            Mutex.EVENT.readAccess (new Runnable () {
                                        public void run () {
                                            if (startFailed (ex)) {
                                                // restart
                                                ExecSupport.this.start ();
                                            }
                                        }
                                    });
        }
    }

    /* Start debugging of associated object.
    * @param stopOnMain if <code>true</code>, debugger stops on the first line of debugged code
    * @exception DebuggerException if the session cannot be started
    */
    public void debug (final boolean stopOnMain) throws DebuggerException {
        String[] params = getArguments ();
        DebuggerType t = getDebuggerType (entry);
        if (t == null) {
            t = defaultDebuggerType ();
        }

        try {
            ExecInfo ei = new ExecInfo (entry.getFile ().getPackageName ('.'), params);
            t.startDebugger (ei, stopOnMain);
            // ok, debugger started
            return;
        } catch (final DebuggerException ex) {
            try {
                Mutex.EVENT.readAccess (new Mutex.ExceptionAction () {
                                            public Object run () throws DebuggerException {
                                                if (debugFailed (ex)) {
                                                    // restart
                                                    debug (stopOnMain);
                                                }
                                                return null;
                                            }
                                        });
            } catch (org.openide.util.MutexException mx) {
                throw (DebuggerException)mx.getException ();
            }
        }
    }

    /** Called when invocation of the executor fails. Allows to do some
    * modifications to the type of execution and try it again.
    *
    * @param ex exeception that occured during execution
    * @return true if the execution should be restarted
    */
    protected boolean startFailed (IOException ex) {
        Executor e = (Executor)choose (getExecutor (entry), Executor.class, ex);
        if (e == null) {
            return false;
        } else {
            try {
                setExecutor (entry, e);
                return true;
            } catch (IOException exc) {
                return false;
            }
        }
    }

    /** Called when invocation of the debugger fails. Allows to do some
    * modifications to the type of debugging and try it again.
    *
    * @param ex exeception that occured during execution
    * @return true if the debugging should be started again
    */
    protected boolean debugFailed (DebuggerException ex) {
        DebuggerType e = (DebuggerType)choose (getDebuggerType (entry), DebuggerType.class, ex);
        if (e == null) {
            return false;
        } else {
            try {
                setDebuggerType (entry, e);
                return true;
            } catch (IOException exc) {
                return false;
            }
        }
    }

    /** Check if this object is up to date or in need of compilation.
    * Should compile it if necessary.
    * <p>The default implementation checks whether {@link CompilerCookie} is provided and
    * if so, creates a job and compiles the object. This behavior may be 
    * overridden by subclasses.
    *
    * @return <code>true</code> if the object was successfully brought up to date, <code>false</code> if the attempt failed (and it may be still be out of date)
    * @deprecated The check should be done in an action - ExecAction, ...
    */
    protected boolean checkCompiled () {
        DataObject obj = entry.getDataObject ();

        CompilerCookie c = (CompilerCookie)obj.getCookie (CompilerCookie.class);

        if (c != null) {
            CompilerJob job = new CompilerJob (Compiler.DEPTH_ZERO);
            job.setDisplayName (obj.getName ());
            c.addToJob (job, Compiler.DEPTH_ZERO);
            if (!job.isUpToDate ()) {
                // add name
                // compile it
                CompilerTask t = job.start ();
                return t.isSuccessful ();
            }
        }
        return true;
    }

    /** This method allows subclasses to override the default
    * debugger type they want to use for debugging.
    *
    * @return current implementation returns DebuggerType.getDefault ()
    */
    protected DebuggerType defaultDebuggerType () {
        return DebuggerType.getDefault ();
    }

    /** This method allows subclasses to override the default
    * executor they want to use for debugging.
    *
    * @return current implementation returns Executor.getDefault ()
    */
    protected Executor defaultExecutor () {
        return Executor.getDefault ();
    }


    /** Set the executor for a given file object.
     * Uses file attributes to store this information.
    * @param entry entry to set the executor for
    * @param exec executor to use
    * @exception IOException if executor cannot be set
    */
    public static void setExecutor (MultiDataObject.Entry entry, Executor exec) throws IOException {
        entry.getFile ().setAttribute (EA_EXECUTOR,
                                       exec == null ? null : new Executor.Handle (exec)
                                      );
    }

    /** Get the executor for a given file object.
    * @param entry entry to obtain the executor for
    * @return executor associated with the file, or null if the default should be used
    */
    public static Executor getExecutor (MultiDataObject.Entry entry) {
        try {
            Executor.Handle handle = (Executor.Handle)entry.getFile ().getAttribute (EA_EXECUTOR);
            if (handle != null) {
                ServiceType exec = handle.getServiceType ();
                if (exec instanceof Executor) {
                    return (Executor)exec;
                }
            }
        } catch (Exception ex) {
            // IOException
        }
        return null;
    }

    /* Sets execution arguments for the associated entry.
    * @param args array of arguments
    * @exception IOException if arguments cannot be set
    */
    public void setArguments (String[] args) throws IOException {
        entry.getFile ().setAttribute (EA_ARGUMENTS, args);
    }

    /** Set execution arguments for a given entry.
    * @param entry the entry
    * @param args array of arguments
    * @exception IOException if arguments cannot be set
    */
    public static void setArguments (MultiDataObject.Entry entry, String[] args) throws IOException {
        entry.getFile ().setAttribute (EA_ARGUMENTS, args);
    }

    /* Getter for arguments associated with given file.
    * @return the arguments or empty array if no arguments associated
    */
    public String[] getArguments () {
        try {
            String[] args = (String[])entry.getFile ().getAttribute (EA_ARGUMENTS);
            if (args != null) {
                return args;
            }
        } catch (Exception ex) {
            // null pointer or IOException
        }
        return new String[0];
    }

    /** Get the arguments associated with a given entry.
    * @param entry the entry
    * @return the arguments, or an empty array if no arguments are specified
    */
    public static String[] getArguments(MultiDataObject.Entry entry) {
        try {
            String[] args = (String[])entry.getFile ().getAttribute (EA_ARGUMENTS);
            if (args != null) {
                return args;
            }
        } catch (Exception ex) {
            // null pointer or IOException
        }
        return new String[0];
    }

    //
    // debugger support
    //

    /** Assignes a debugger type to an entry.
    * @param entry the object's entry
    * @param type the debugger type for this entry
    * @exception IOException if arguments cannot be set
    */
    public static void setDebuggerType (MultiDataObject.Entry entry, DebuggerType type) throws IOException {
        entry.getFile ().setAttribute (EA_DEBUGGER_TYPE,
                                       type == null ? null : new DebuggerType.Handle (type)
                                      );
    }

    /** Retrieves the debugger type for this entry.
    * @param entry the entry
    * @return the debugger type or null if no type assigned
    */
    public static DebuggerType getDebuggerType (MultiDataObject.Entry entry) {
        try {
            DebuggerType.Handle h = (DebuggerType.Handle)entry.getFile ().getAttribute (EA_DEBUGGER_TYPE);
            ServiceType t = h.getServiceType ();
            if (t instanceof DebuggerType) {
                return (DebuggerType)t;
            }
        } catch (Exception ex) {
        }
        return null;
    }

    /** Helper method that creates default properties for execution of
    * a given support.
    * Includes properties to set the executor; debugger; and arguments.
    *
    * @param set sheet set to add properties to
    */
    public void addProperties (Sheet.Set set) {
        set.put(new PropertySupport.ReadWrite (
                    PROP_FILE_PARAMS,
                    String.class,
                    getString("PROP_fileParams"),
                    getString("HINT_fileParams")
                ) {
                    public Object getValue() {
                        String[] args = getArguments ();
                        StringBuffer b = new StringBuffer(50);
                        for (int i = 0; i < args.length; i++) {
                            b.append(args[i]).append(' ');
                        }
                        return b.toString();
                    }
                    public void setValue (Object val) throws InvocationTargetException {
                        if (val instanceof String) {
                            try {
                                setArguments(Utilities.parseParameters((String)val));
                            } catch(IOException e) {
                                throw new InvocationTargetException (e);
                            }
                        }
                        else {
                            throw new IllegalArgumentException();
                        }
                    }

                    public boolean supportsDefaultValue () {
                        return true;
                    }

                    public void restoreDefaultValue () throws InvocationTargetException {
                        try {
                            setArguments(null);
                        } catch(IOException e) {
                            throw new InvocationTargetException (e);
                        }
                    }

                }
               );
        set.put(createExecutorProperty ());
        set.put(createDebuggerProperty ());
    }

    /** Creates the executor property for entry.
    * @return the property
    */
    private PropertySupport createExecutorProperty () {
        return new PropertySupport.ReadWrite (
                   PROP_EXECUTION,
                   Executor.class,
                   getString("PROP_execution"),
                   getString("HINT_execution")
               ) {
                   public Object getValue() {
                       Executor e = getExecutor (entry);
                       if (e == null)
                           return defaultExecutor ();
                       else
                           return e;
                   }
                   public void setValue (Object val) throws InvocationTargetException {
                       try {
                           setExecutor(entry, (Executor) val);
                       } catch (IOException ex) {
                           throw new InvocationTargetException (ex);
                       }
                   }
                   public boolean supportsDefaultValue () {
                       return true;
                   }

                   public void restoreDefaultValue () throws InvocationTargetException {
                       setValue (null);
                   }
               };
    }

    /** Creates the debugger property for entry.
    * @return the property
    */
    private PropertySupport createDebuggerProperty () {
        return new PropertySupport.ReadWrite (
                   PROP_DEBUGGER_TYPE,
                   DebuggerType.class,
                   getString("PROP_debuggerType"),
                   getString("HINT_debuggerType")
               ) {
                   public Object getValue() {
                       DebuggerType dt = getDebuggerType (entry);
                       if (dt == null)
                           return defaultDebuggerType ();
                       else
                           return dt;
                   }
                   public void setValue (Object val) throws InvocationTargetException {
                       try {
                           setDebuggerType (entry, (DebuggerType) val);
                       } catch (IOException ex) {
                           throw new InvocationTargetException (ex);
                       }
                   }
                   public boolean supportsDefaultValue () {
                       return true;
                   }

                   public void restoreDefaultValue () throws InvocationTargetException {
                       setValue (null);
                   }
               };
    }

    /** @return a localized String */
    static String getString(String s) {
        if (bundle == null) {
            bundle = NbBundle.getBundle(ExecSupport.class);
        }
        return bundle.getString(s);
    }

    /** Opens dialog and asks for different executor or debugger.
    * @param current current value
    * @param clazz the class to use to locate property editor
    * @param ex the exception that caused the problem
    * @return the new value or null if choosing failed
    */
    private static ServiceType choose (ServiceType current, Class clazz, final Exception ex) {
        PropertyEditor ed = PropertyEditorManager.findEditor (clazz);
        if (ed == null) return null;

        ed.setValue (current);

        java.awt.Component c = ed.getCustomEditor ();
        if (c == null) return null;

        String configure = org.openide.util.NbBundle.getBundle(ExecSupport.class).getString("CTL_ServiceConfigure");
        NotifyDescriptor exc = new NotifyDescriptor.Exception (ex);
        exc.setTitle (org.openide.util.NbBundle.getBundle(ExecSupport.class).getString("CTL_Service_Configuration_Title"));
        exc.setMessage (ex.getLocalizedMessage ());
        exc.setOptions (new Object[] { configure, NotifyDescriptor.CANCEL_OPTION });
        Object res = TopManager.getDefault ().notify (exc);
        if (!configure.equals (res)) {
            return null;
        }

        DialogDescriptor d = new DialogDescriptor (
                                 c,
                                 getString ("MSG_ConfigureService")
                             );
        d.setOptions (new Object[] {
                          DialogDescriptor.OK_OPTION,
                          DialogDescriptor.CANCEL_OPTION
                      });

        java.awt.Dialog dialog = TopManager.getDefault ().createDialog (d);

        dialog.show ();

        if (d.getValue () == NotifyDescriptor.OK_OPTION) {
            // get the current value
            return (ServiceType)ed.getValue ();
        } else {
            // canceled
            return null;
        }
    }
}

/*
* Log
*  33   Gandalf   1.32        1/12/00  Ian Formanek    NOI18N
*  32   Gandalf   1.31        1/7/00   Jaroslav Tulach #5111
*  31   Gandalf   1.30        1/5/00   Jaroslav Tulach Service configuration has
*       two dialogs
*  30   Gandalf   1.29        11/24/99 Ales Novak      thread that waited for 
*       the end of a running process moved to ProcessExecutor
*  29   Gandalf   1.28        10/29/99 Jesse Glick     Removed deprecated static
*       variants of {Exec,Compiler}Support.addProperties.
*  28   Gandalf   1.27        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  27   Gandalf   1.26        10/7/99  Jesse Glick     Encouraged to use 
*       nonstatic methods to add properties to sheet sets for supports.
*  26   Gandalf   1.25        10/1/99  Jesse Glick     Cleanup of service type 
*       name presentation.
*  25   Gandalf   1.24        10/1/99  Ales Novak      major change of execution
*  24   Gandalf   1.23        9/15/99  Jaroslav Tulach Query when wrong executor
*       or debugger is used.
*  23   Gandalf   1.22        9/10/99  Jaroslav Tulach Changes in services APIs.
*  22   Gandalf   1.21        7/12/99  Martin Ryzl     access modifier for entry
*       changed to protected
*  21   Gandalf   1.20        6/28/99  Jaroslav Tulach Debugger types are like 
*       Executors
*  20   Gandalf   1.19        6/11/99  Jan Jancura     
*  19   Gandalf   1.18        6/8/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  18   Gandalf   1.17        6/4/99   Petr Jiricka    
*  17   Gandalf   1.16        5/14/99  Ales Novak      bugfix for #1667 #1598 
*       #1625
*  16   Gandalf   1.15        4/21/99  Jaroslav Tulach Debugger types.
*  15   Gandalf   1.14        4/21/99  Ales Novak      NullPointerEx removed
*  14   Gandalf   1.13        4/2/99   Jesse Glick     [JavaDoc]
*  13   Gandalf   1.12        4/2/99   Jaroslav Tulach Compiles before 
*       execution.
*  12   Gandalf   1.11        3/22/99  Jesse Glick     [JavaDoc]
*  11   Gandalf   1.10        3/19/99  Ales Novak      
*  10   Gandalf   1.9         3/19/99  Ales Novak      
*  9    Gandalf   1.8         3/14/99  Jaroslav Tulach Change of 
*       MultiDataObject.Entry.
*  8    Gandalf   1.7         3/11/99  Jesse Glick     [JavaDoc]
*  7    Gandalf   1.6         3/11/99  Jan Jancura     
*  6    Gandalf   1.5         3/9/99   Jesse Glick     [JavaDoc]
*  5    Gandalf   1.4         3/3/99   Jaroslav Tulach Also implements debugger 
*       cookie.
*  4    Gandalf   1.3         2/4/99   Petr Hamernik   setting of extended file 
*       attributes doesn't require FileLock
*  3    Gandalf   1.2         1/20/99  David Simonek   rework of class DO
*  2    Gandalf   1.1         1/6/99   Ian Formanek    
*  1    Gandalf   1.0         1/5/99   Ian Formanek    
* $
*/
