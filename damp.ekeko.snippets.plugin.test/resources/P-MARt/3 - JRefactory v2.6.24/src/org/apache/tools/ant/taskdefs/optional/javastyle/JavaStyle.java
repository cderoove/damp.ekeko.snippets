/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 * any, must include the following acknowlegement:
 * "This product includes software developed by the
 * Apache Software Foundation (http://www.apache.org/)."
 * Alternately, this acknowlegement may appear in the software itself,
 * if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Tomcat", and "Apache Software
 * Foundation" must not be used to endorse or promote products derived
 * from this software without prior written permission. For written
 * permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 * nor may "Apache" appear in their names without prior written
 * permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package org.apache.tools.ant.taskdefs.optional.javastyle;

import java.io.File;
import java.util.Vector;

import org.acm.seguin.pretty.PrettyPrintFile;
import org.acm.seguin.refactor.undo.FileSet;
import org.acm.seguin.tools.install.RefactoryInstaller;

/**
 * <p>
 *
 * Task for formatting Java source code. This task utilises the PrettyPrinter
 * code in the JRefactory code produced by <a href="mailto:seguin@acm.org">Chris
 * Sequin</a> . This library can be found at <a
 * href="http://users.snip.net/~aseguin/chrissoft.html">
 * http://users.snip.net/~aseguin/chrissoft.html</a> .</p> <p>
 *
 * The current implementation is <i>very</i> simple. I am implemented it as
 * quickly as possible to make it available for others to be able to improve
 * upon!</p> <p>
 *
 * The task takes only a "file" attribute or an embedded fileset. It contains no
 * other attributes at present, as all formatting controls are read from the
 * default pretty printer preferences file which is stored in a ".Refactory"
 * directory in your home directory. If no such file exists the first time this
 * task is run, a default file is generated automatically.</p> <p>
 *
 * Features that would be well worth implementing include: adding attributes to
 * control all of the formatting features of the PrettyPrinter, and removing any
 * reliance on an external preference file; allowing the formatted source files
 * to be saved into a different location than the originals.</p>
 *
 * @author    Stuart Roebuck <a href="mailto:stuart.roebuck@adolos.com">
 *      stuart.roebuck@adolos.com</a>
 * @created   January 22, 2001
 */
public class JavaStyle extends MatchingTask {

    /**
     * The individual file specified for styling.
     */
    protected File file = null;
    /**
     * A collection of filesets to be styled.
     */
    protected Vector filesets = new Vector();
    private int verbosity = Project.MSG_VERBOSE;
    private boolean quiet = false;


    /**
     * Constructor for the JavaStyle object
     */
    public JavaStyle() {
        super();
    }


    /**
     * Set the name of a single file to be styled.
     *
     * @param file  the file to be styled
     */
    public void setFile(File file) {
        this.file = file;
    }


    /**
     * Used to force listing of all names of styled files.
     *
     * @param verbose  "true" or "on"
     */
    public void setVerbose(boolean verbose) {
        if (verbose) {
            this.verbosity = Project.MSG_INFO;
        } else {
            this.verbosity = Project.MSG_VERBOSE;
        }
    }


    /**
     * If the file does not exist, do not display a diagnostic message or modify
     * the exit status to reflect an error.
     *
     * @param quiet  "true" or "on"
     */
    public void setQuiet(boolean quiet) {
        this.quiet = quiet;
    }


    /**
     * Adds a set of files (nested fileset attribute).
     *
     * @param set  the fileset to add.
     */
    public void addFileset(FileSet set) {
        filesets.addElement(set);
    }


    /**
     * Styles the file(s).
     *
     * @exception BuildException  can be thrown if no files are specified in the
     *      task.
     */
    public void execute() throws BuildException {
        if (file == null && filesets.size() == 0) {
            throw new BuildException("At least one of the file or dir attributes, or a fileset element, must be set.");
        }

        // style the single file
        if (file != null) {
            if (file.exists()) {
                if (file.isDirectory()) {
                    log("Directory " + file.getAbsolutePath() + " is not a file, use the dir attribute instead.");
                } else {
                    log("JavaStyling: " + file.getAbsolutePath());
                    styleFile(file);
                }
            } else {
                log("Could not find file " + file.getAbsolutePath() + " to style.");
            }
        }

        // style the files in the filesets
        for (int i = 0; i < filesets.size(); i++) {
            FileSet fs = (FileSet) filesets.elementAt(i);
            DirectoryScanner ds = fs.getDirectoryScanner(project);
            String[] files = ds.getIncludedFiles();
            styleFiles(fs.getDir(project), files);
        }

    }


    /**
     * Initialise all the preference files for JRefactory. If there are no
     * existing preference files then create some default ones.
     *
     * @throws BuildException  if someting goes wrong with the build
     */
    public void init() throws BuildException {
        //  Make sure everything is installed properly
        (new RefactoryInstaller(false)).run();
    }


    /**
     * Style a single file
     *
     * @param file  The file to be styled.
     */
    protected void styleFile(File file) {
        PrettyPrintFile ppf = new PrettyPrintFile();
        ppf.setAsk(false);
        if (ppf.isApplicable(file)) {
            ppf.apply(file);
        }
    }


    /**
     * Style a list of files in a given directory.
     *
     * @param dir    the directory containing the files.
     * @param files  an array of filenames within the directory.
     */
    protected void styleFiles(File dir, String[] files) {
        if (files.length > 0) {
            log("JavaStyling " + files.length + " files from " + dir.getAbsolutePath());
            for (int j = 0; j < files.length; j++) {
                File f = new File(dir, files[j]);
                log("JavaStyling " + f.getAbsolutePath(), verbosity);
                styleFile(f);
            }
        }
    }

}

