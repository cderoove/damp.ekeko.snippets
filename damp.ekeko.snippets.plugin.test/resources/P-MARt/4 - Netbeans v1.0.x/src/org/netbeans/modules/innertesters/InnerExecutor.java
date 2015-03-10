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

package org.netbeans.modules.innertesters;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Map;

import org.openide.execution.*;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/** Runs the inner test class with the correct classpath.
 *
 * @author Jesse Glick
 */
public class InnerExecutor extends ProcessExecutor {

    /** The regular Java launcher command, with the added classpath item.
     */
    private static final NbProcessDescriptor DEFAULT = new NbProcessDescriptor (
                // process
                "{" + Format.TAG_JAVAHOME + "}{" + Format.TAG_SEPARATOR + "}bin{" + Format.TAG_SEPARATOR + "}java",
                // arguments
                "-cp {" + TestFormat.TAG_TESTDIR + "}" +
                "{" + Format.TAG_PATHSEPARATOR + "}" + "{" + Format.TAG_REPOSITORY + "}" +
                "{" + Format.TAG_PATHSEPARATOR + "}" + "{" + Format.TAG_CLASSPATH + "}" +
                "{" + Format.TAG_PATHSEPARATOR + "}" + "{" + Format.TAG_LIBRARY + "} " +
                "{" + Format.TAG_CLASSNAME + "} " +
                "{" + Format.TAG_ARGUMENTS + "}",
                // description
                NbBundle.getBundle (InnerExecutor.class).getString ("HINT_inner_execution_format")
            );

    /** The default testing package root to use.
     */
    private File testDir = new File (System.getProperty ("java.io.tmpdir"), "innertst");
    /** The default inner class name.
     */
    private String innerName = "TEST";

    private static final long serialVersionUID =20936500639242675L;
    /** Make a new executor.
     * Use a special command.
     */
    public InnerExecutor () {
        setExternalExecutor (DEFAULT);
    }

    /** Get the default display name of the executor.
     * @return the display name
     */
    protected String displayName () {
        return NbBundle.getBundle (InnerExecutor.class).getString ("LBL_inner_tester_executor");
    }

    /** Bean getter.
     * @return the current test package root
     */
    public File getTestDir () {
        return testDir;
    }

    /** Bean setter.
     * @param nue the new test package root
     */
    public synchronized void setTestDir (File nue) {
        File old = testDir;
        testDir = nue;
        firePropertyChange ("testDir", old, nue);
    }

    /** Bean getter.
     * @return the current inner class name
     */
    public String getInnerName () {
        return innerName;
    }

    /** Bean setter.
     * @param nue the new inner class name
     */
    public synchronized void setInnerName (String nue) {
        String old = innerName;
        innerName = nue;
        firePropertyChange ("innerName", old, nue);
    }

    /** Actually create the external process.
     * @param info the name of the (outer) class to run
     * @throws IOException if there is a problem starting
     * @return the running process
     */
    protected Process createProcess (ExecInfo info) throws IOException {
        ExecInfo testInfo = new ExecInfo (info.getClassName () + '$' + getInnerName (),
                                          info.getArguments ());
        return getExternalExecutor ().exec (new TestFormat (testInfo,
                                            getClassPath (),
                                            getBootClassPath (),
                                            getRepositoryPath (),
                                            getLibraryPath (),
                                            getTestDir ()));
    }

    /** Get context help for the executor.
     * @return the help context
     */
    public HelpCtx getHelpCtx () {
        return new HelpCtx (InnerExecutor.class);
    }

    /** A format recognizing the <CODE>{testdir}</CODE> tag.
     */
    static class TestFormat extends Format {
        /** The tag name to recognize.
         */
        static final String TAG_TESTDIR = "testdir";

        private static final long serialVersionUID =8042236340342898125L;
        /** Create a new format.
         * @param info the (inner) class to run with arguments
         * @param classPath the IDE class path to use
         * @param bootClassPath the system boot class path
         * @param repositoryPath the classpath from Filesystems
         * @param libraryPath a list of libraries and modules
         * @param testDir the testing root directory
         */
        TestFormat (ExecInfo info, NbClassPath classPath, NbClassPath bootClassPath, NbClassPath repositoryPath, NbClassPath libraryPath, File testDir) {
            super (info, classPath, bootClassPath, repositoryPath, libraryPath);
            Map map = getMap ();
            map.put (TAG_TESTDIR, testDir.getAbsolutePath ());
        }


    }

}
