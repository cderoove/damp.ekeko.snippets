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

package org.netbeans.core;

import java.io.*;
import java.text.DateFormat;
import java.text.MessageFormat;

/** A class that provides logging facility for the IDE - once instantiated,
* it redirects the System.err into a log_file.
*
* @author   Ian Formanek, Ales Novak
*/
public class TopLogging extends Object {
    /** The name of the log file */
    public static final String LOG_FILE_NAME = "forte4j.log"; // NOI18N

    static RandomAccessFile fileOutput;
    static boolean disabledConsole = true;

    private PrintStream logPrintStream;
    private PrintStream consoleErrStream;
    private OutputStream demultiplex;

    /** Creates a new TopLogging - redirects the System.err to a log file.
    * @param logDir A directory for the log file
    */
    public TopLogging (String logDir) throws IOException  {
        File logFile = new File(logDir+File.separator+LOG_FILE_NAME);
        if ((logFile.exists() && !logFile.canWrite()) || logFile.isDirectory()) {
            throw new IOException ("Cannot write to file"); // NOI18N
        }
        // output to file
        fileOutput = new RandomAccessFile(logFile, "rw"); //only write // NOI18N
        fileOutput.seek(fileOutput.length());

        // output to console
        consoleErrStream = System.err;

        DateFormat df = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL);
        java.util.Date date = new java.util.Date();

        logPrintStream = new PrintStream(demultiplex = new StreamDemultiplexor());
        disabledConsole = true;
        logPrintStream.println("-------------------------------------------------------------------------------"); // NOI18N
        logPrintStream.println(">Log Session: "+df.format (date)); // NOI18N
        logPrintStream.println(">System Info: "); // NOI18N
        printSystemInfo(logPrintStream);
        logPrintStream.println("-------------------------------------------------------------------------------"); // NOI18N
        disabledConsole = false;
        consoleErrStream = System.err;  // reacquire needed - cause of System.err has changed!!! see top.exec.ExecEng class
        System.setErr(logPrintStream);
    }

    /**
     * @return name of JIT (if installed). If Symantect JIT is available, also its version.
     **/
    private static String extractJITName() {
        // get as much information about JIT as possible
        String jit = System.getProperty ("java.compiler"); // is not supported e.g. on HP 1.1.5
        if ((jit == null) || ("".equals (jit))) // NOI18N
            jit = "unknown or not used"; // NOI18N

        // specific for Symantec JIT (on Windows only)!
        if (jit.equals("symcjit")) { // NOI18N
            // allows to extract version info (variable JAVA_COMPCMD must be set FORCE_SIGNON,
            // any java app run and first line of result contains version info
            // (like: Symantec Java! JustInTime Compiler Version 3.00.039(x) for JDK 1.1.x)
            String fileSeparator = System.getProperty ("file.separator");
            String javaRoot = System.getProperty ("java.home") + fileSeparator;
            String cmd = javaRoot + "bin" + fileSeparator + "java nonexistent"; // NOI18N
            // run fake Java to get version info
            String[] envp = new String[1];
            envp[0] = "JAVA_COMPCMD=FORCE_SIGNON";  // magic env. setting, every Java app wil print JIT version first // NOI18N

            try {
                Process p = Runtime.getRuntime().exec(cmd, envp);
                BufferedReader brd = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                p.waitFor();  // the output is short so this will not block (this is JDK bug)
                String firstLine = brd.readLine();
                brd.close();
                if (firstLine != null) {
                    // usual output is too long, try to shorten it if can
                    String stdStart = "Symantec Java! JustInTime Compiler Version "; // NOI18N
                    if (firstLine.startsWith(stdStart)) {
                        jit = "Symantec, version " + firstLine.substring(stdStart.length()); // NOI18N
                    } else {
                        jit = firstLine; // unknown message structure, let it be
                    }
                }
            } catch (IOException e) { // version can't be read
            } catch (InterruptedException e) { // dtto
            }
        }
        return jit;
    }

    public static void printSystemInfo(PrintStream ps) {
        final java.util.ResourceBundle topBundle =
            org.openide.util.NbBundle.getBundle(TopLogging.class);
        String buildNumber = System.getProperty ("netbeans.buildnumber"); // NOI18N
        String currentVersion = new MessageFormat (topBundle.getString ("currentVersion")).format (new Object[] { buildNumber });
        ps.println("  Product Version          = " + currentVersion); // NOI18N
        ps.println("  IDE Versioning           = " + System.getProperty ("org.openide.major.version") +
                   " spec=" + System.getProperty ("org.openide.specification.version") +
                   " impl=" + System.getProperty ("org.openide.version"));
        ps.println("  Operating System         = " + System.getProperty("os.name", "unknown")
                   + " Version " + System.getProperty("os.version", "unknown")
                   + " Running on " +  System.getProperty("os.arch", "unknown"));
        ps.println("  Java Version             = " + System.getProperty("java.version", "unknown"));
        ps.println("  Java VM Version          = " + System.getProperty("java.vm.name", "unknown") + " " + System.getProperty("java.vm.version", ""));
        ps.println("  Java Vendor              = " + System.getProperty("java.vendor", "unknown"));
        ps.println("  Java Vendor URL          = " + System.getProperty("java.vendor.url", "unknown"));
        ps.println("  Java Home                = " + System.getProperty("java.home", "unknown"));
        ps.println("  Java Class Version       = " + System.getProperty("java.class.version", "unknown"));
        ps.println("  System Locale            = " + java.util.Locale.getDefault()); // NOI18N
        ps.println("  JIT                      = " + extractJITName()); // NOI18N
        ps.println("  Home Dir                 = " + System.getProperty("user.home", "unknown"));
        ps.println("  Current Directory        = " + System.getProperty("user.dir", "unknown"));
        ps.println("  Forte for Java Home      = " + Main.homeDir); // NOI18N
        ps.println("  Forte for Java User Home = " + Main.userDir); // NOI18N
        ps.println("  System Directory         = " + Main.systemDir); // NOI18N
        ps.println("  CLASSPATH                = " + System.getProperty("java.class.path", "unknown")); // NOI18N
    }

    public void finalize() throws Throwable {
        demultiplex.flush();
        demultiplex.close();
    }

    static PrintStream getLogOutputStream() {
        return new PrintStream(new LogOutputStream());
    }

    class StreamDemultiplexor extends OutputStream {
        public void write(int b) throws IOException {
            fileOutput.write(b);
            if (! disabledConsole) consoleErrStream.write(b);
        }

        public void write(byte b[]) throws IOException {
            fileOutput.write(b);
            if (! disabledConsole) consoleErrStream.write(b);
        }

        public void write(byte b[],
                          int off,
                          int len)
        throws IOException {
            fileOutput.write(b, off, len);
            if (! disabledConsole) consoleErrStream.write(b, off, len);
        }

        public void flush() throws IOException {
            fileOutput.getFD().sync();
            consoleErrStream.flush();
        }

        public void close() throws IOException {
            fileOutput.close();
            consoleErrStream.close();
        }
    }

    private static class LogOutputStream extends OutputStream {
        public void write(int b) throws IOException {
            if (TopLogging.fileOutput != null)
                TopLogging.fileOutput.write(b);
        }

        public void write(byte b[]) throws IOException {
            if (TopLogging.fileOutput != null)
                TopLogging.fileOutput.write(b);
        }

        public void write(byte b[],
                          int off,
                          int len)
        throws IOException {
            if (TopLogging.fileOutput != null)
                TopLogging.fileOutput.write(b, off, len);
        }

        public void flush() throws IOException {
            if (TopLogging.fileOutput != null)
                TopLogging.fileOutput.getFD().sync();
        }

        public void close() throws IOException {
            if (TopLogging.fileOutput != null)
                TopLogging.fileOutput.close();
        }
    }
}

/*
 * Log
 *  11   Gandalf-post-FCS1.9.1.0     4/5/00   Jaroslav Tulach In system info locale is
 *       printed.
 *  10   Gandalf   1.9         1/14/00  Jesse Glick     Moving versioning info 
 *       out of localizable range.
 *  9    Gandalf   1.8         1/14/00  Radko Najman    'netbeans.log' changed 
 *       to 'forte4j.log'
 *  8    Gandalf   1.7         1/13/00  Jaroslav Tulach I18N
 *  7    Gandalf   1.6         11/9/99  Petr Hrebejk    Text in system info 
 *       changed to Forte for Java
 *  6    Gandalf   1.5         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  5    Gandalf   1.4         9/10/99  Ian Formanek    Removed deprecated code
 *  4    Gandalf   1.3         6/25/99  Ian Formanek    Extended system info 
 *       with VM version
 *  3    Gandalf   1.2         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  2    Gandalf   1.1         5/10/99  Jesse Glick     Module versioning--IDE 
 *       version numbers refined, made into system properties.
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 * Beta Change History:
 *  0    Tuborg    0.10        --/--/98 Ales Novak      StreamDemultiplexor
 *  0    Tuborg    0.11        --/--/98 Ales Novak      LogOutputStream
 *  0    Tuborg    0.12        --/--/98 Jan Formanek    system info now prints also TUBORG_HOME and system dir
 *  0    Tuborg    0.13        --/--/98 Jan Formanek    Tuborg -> Netbeans
 *  0    Tuborg    0.14        --/--/98 Jan Formanek    prints product version in system info
 *  0    Tuborg    0.15        --/--/98 Jan Formanek    system info texts improved
 */
