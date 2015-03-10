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

package org.openide.execution;

import java.text.Format;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.openide.util.NbBundle;

/** Encapsulates start information for a process. It allows the user to
* specify the process name to execute and arguments to provide. The progammer
* then uses method exec to start the process and can pass additional format that
* will be applied to arguments. 
* <P>
* This allows to define arguments in format -user {USER_NAME} -do {ACTION} and then
* use MapFormat with defined values for USER_NAME and ACTION that will be substitued
* by into the arguments.
*
* @author  Ian Formanek, Jaroslav Tulach
*/
public final class NbProcessDescriptor extends Object implements java.io.Serializable {
    /** A JDK 1.1 serial version UID */
    static final long serialVersionUID = -4535211234565221486L;

    /** The name of the executable to run */
    private String processName;
    /** argument format */
    private String arguments;
    /** info about format of the arguments */
    private String info;

    /** Create a new descriptor for the specified process, classpath switch, and classpath.
    * @param processName     the name of the executable to run
    * @param argument string for formating of arguments
    */
    public NbProcessDescriptor(String processName, String arguments) {
        this (processName, arguments, null);
    }

    /** Create a new descriptor for the specified process, classpath switch, and classpath.
    * @param processName     the name of the executable to run
    * @param argument string for formating of arguments
    * @param info info how to format the arguments (human-readable string)
    */
    public NbProcessDescriptor(String processName, String arguments, String info) {
        this.processName = processName;
        this.arguments = arguments;
        this.info = info;
    }


    /** Get the name of the executable to run.
    * @return the name
    */
    public String getProcessName () {
        return processName;
    }

    /** Getter the execution arguments of the process.
    * @return the switch that the executable uses for passing the classpath as its command-line parameter 
    */
    public String getArguments () {
        return arguments;
    }

    /** Getter for the human readable info about the arguments.
    * @return the info string or null
    */
    public String getInfo () {
        return info;
    }

    /* JST: Commented out, should not be needed.
    *
    *  Get the command string and arguments from the supplied process name.
    * Normally the process name will be the actual name of the process executable,
    * in which case this method will just return that name by itself.
    * However, {@link org.openide.util.Utilities#parseParameters} is used
    * to break apart the string into tokens, so that users may:
    * <ul>
    * <li>Include command names with embedded spaces, such as <code>c:\Program Files\jdk\bin\javac</code>.
    * <li>Include extra command arguments, such as <code>-Dname=value</code>.
    * <li>Do anything else which might require unusual characters or processing. For example:
    * <p><code><pre>
    * "c:\program files\jdk\bin\java" -Dmessage="Hello /\\/\\ there!" -Xmx128m
    * </pre></code>
    * <p>This example would create the following executable name and arguments:
    * <ol>
    * <li> <code>c:\program files\jdk\bin\java</code>
    * <li> <code>-Dmessage=Hello /\/\ there!</code>
    * <li> <code>-Xmx128m</code>
    * </ol>
    * Note that the command string does not escape its backslashes--under the assumption
    * that Windows users will not think to do this, meaningless escapes are just left
    * as backslashes plus following character.
    * </ul>
    * <em>Caveat</em>: even after parsing, Windows programs (such as the Java launcher)
    * may not fully honor certain
    * characters, such as quotes, in command names or arguments. This is because programs
    * under Windows frequently perform their own parsing and unescaping (since the shell
    * cannot be relied on to do this). On Unix, this problem should not occur.
    * @return a list of the command name itself and any arguments, unescaped
    * @see Runtime#exec(String[])
    *
    public String[] getProcessArgs() {
      if (processArguments == null) {
        processArguments = parseArguments(processName);
      }
      return (String[]) processArguments.clone();
}
    */

    /** Executes the process with arguments formatted by the provided
    * format. Also the envp properties are passed to the executed process,
    * and a working directory may be supplied (this requires JDK 1.3 to
    * work correctly).
    *
    * @param format format to be aplied to arguments suplied by user
    * @param envp list of properties to be applied to the process, or <code>null</code> to leave unspecified
    * @param cwd the working directory to use, or <code>null</code> if this should not be specified
    * @return handle to executed process.
    * @exception IOException if the start of the process fails, or if setting the working directory is not supported
    */
    public Process exec (Format format, String[] envp, File cwd) throws IOException {
        String stringArgs = format == null ? arguments : format.format (arguments);
        String[] args = parseArguments (stringArgs);

        // copy the call string
        String[] call = new String[args.length + 1];
        call[0] = format.format(processName);
        System.arraycopy (args, 0, call, 1, args.length);
        /*
            System.out.println("Executing: ");
            for (int i = 0; i < call.length; i++) {
              System.out.println("  " + i + ". = " + call[i]);
            }    
        */    
        if (cwd == null) {
            if (envp == null) {
                return Runtime.getRuntime ().exec (call);
            } else {
                return Runtime.getRuntime ().exec (call, envp);
            }
        } else {
            // Must use introspection so this code is safe on 1.2.
            try {
                Method m = Runtime.class.getMethod ("exec", new Class[] { String[].class, String[].class, File.class }); // NOI18N
                return (Process) m.invoke (Runtime.getRuntime (), new Object[] { call, envp, cwd });
            } catch (NoSuchMethodException nsme) {
                throw new IOException (NbBundle.getBundle (NbProcessDescriptor.class).getString ("EXC_no_JDK13_exec"));
            } catch (InvocationTargetException ite) {
                Throwable t = ite.getTargetException ();
                if (t instanceof ThreadDeath) {
                    throw (ThreadDeath) t;
                } else if (t instanceof IOException) {
                    throw (IOException) t;
                } else {
                    if (Boolean.getBoolean ("netbeans.debug.exceptions")) // NOI18N
                        t.printStackTrace ();
                    throw new IOException (t.toString ());
                }
            } catch (Exception e) {
                if (Boolean.getBoolean ("netbeans.debug.exceptions")) // NOI18N
                    e.printStackTrace ();
                throw new IOException (e.toString ());
            }
        }
    }

    /** Executes the process with arguments formatted by the provided
    * format. Also the envp properties are passed to the executed process.
    *
    * @param format format to be aplied to arguments suplied by user
    * @param envp list of properties to be applied to the process, or <code>null</code> to leave unspecified
    * @return handle to executed process.
    * @exception IOException if the start of the process fails
    */
    public Process exec (Format format, String[] envp) throws IOException {
        return exec (format, envp, null);
    }

    /** Executes the process with arguments formatted by the provided
    * format. 
    *
    * @param format format to be aplied to arguments suplied by user
    * @return handle to executed process.
    * @exception IOException if the start of the process fails
    */
    public Process exec (Format format) throws IOException {
        return exec (format, null);
    }

    /** Executes the process with arguments provided in constructor.
    *
    * @return handle to executed process.
    * @exception IOException if the start of the process fails
    */
    public Process exec () throws IOException {
        return exec (null, null);
    }

    /* hashCode */
    public int hashCode() {
        return processName.hashCode() + arguments.hashCode ();
    }

    /* equals */
    public boolean equals(Object o) {
        if (! (o instanceof NbProcessDescriptor)) return false;
        NbProcessDescriptor him = (NbProcessDescriptor) o;
        return processName.equals(him.processName) && arguments.equals(him.arguments);
    }

    /** Parses given string to an array of arguments.
    * @param sargs is tokenized by spaces unless a space is part of "" token
    * @return tokenized string
    */
    private static String[] parseArguments(String sargs) {
        return org.openide.util.Utilities.parseParameters(sargs);
    }

}


/*
 * Log
 *  21   src-jtulach1.20        1/12/00  Ian Formanek    NOI18N
 *  20   src-jtulach1.19        12/21/99 Jesse Glick     External executors can 
 *       set envvars and (on 1.3) cwd.
 *  19   src-jtulach1.18        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  18   src-jtulach1.17        6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  17   src-jtulach1.16        6/8/99   Petr Hamernik   process name is formated
 *       too (using the same Format like arguments)
 *  16   src-jtulach1.15        5/31/99  Jaroslav Tulach External Execution & 
 *       Compilation
 *  15   src-jtulach1.14        5/18/99  Petr Hamernik   frixed bug #1638 once 
 *       more
 *  14   src-jtulach1.13        5/18/99  Ian Formanek    Fixed spaces in 
 *       classpath parameters
 *  13   src-jtulach1.12        5/17/99  Petr Hamernik   fixed bug #1638
 *  12   src-jtulach1.11        5/15/99  Jesse Glick     [JavaDoc]
 *  11   src-jtulach1.10        5/7/99   Ales Novak      getAllLibraries moved to
 *       CompilationEngine
 *  10   src-jtulach1.9         5/6/99   Jan Jancura     Bug when lib folder do 
 *       not exists.
 *  9    src-jtulach1.8         4/26/99  Jesse Glick     [JavaDoc]
 *  8    src-jtulach1.7         4/22/99  Ales Novak      utility methods added
 *  7    src-jtulach1.6         4/21/99  Jesse Glick     [JavaDoc]
 *  6    src-jtulach1.5         4/21/99  Ales Novak      method for parsing 
 *       commandline added
 *  5    src-jtulach1.4         4/16/99  Ales Novak      
 *  4    src-jtulach1.3         3/29/99  Jesse Glick     [JavaDoc]
 *  3    src-jtulach1.2         3/29/99  Ales Novak      
 *  2    src-jtulach1.1         3/23/99  Jesse Glick     [JavaDoc]
 *  1    src-jtulach1.0         1/6/99   Jaroslav Tulach 
 * $
 */



