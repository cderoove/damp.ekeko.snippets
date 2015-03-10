/* Copyright (c) 2003 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.util;

import java.io.*;
import java.util.*;

/*****************************************************
 * NutchRemoteFileSystem implements the NutchFileSystem over
 * machines that can be linked via some set of command-line args.
 * (presumably 'scp').
 *
 * @author Mike Cafarella
 *****************************************************/
public class NutchRemoteFileSystem extends NutchGenericFileSystem {
    static String SRCPATH_SYMBOL = "%srcpath%";
    static String DSTPATH_SYMBOL = "%dstpath%";
    static String DSTMACH_SYMBOL = "%dstmach%";

    String cpTemplate = null, rmTemplate = null, mkdirTemplate = null;

    /**
     * Create the ShareSet automatically, then do regular constructor.
     */
    public NutchRemoteFileSystem(File dbRoot, String cpTemplate, String rmTemplate, String mkdirTemplate) throws IOException {
        this(dbRoot, new ShareSet(dbRoot), cpTemplate, rmTemplate, mkdirTemplate);
    }

    /**
     * The NutchRemoteFileSystem takes template-strings for 
     * its various needed commands, which may differ among installations.  
     * The class will fill in these templates with the necessary args,
     * and then invoke them via System.exec().
     *
     * We're given the ShareSet here.
     */
    public NutchRemoteFileSystem(File dbRoot, ShareSet shareSet, String cpTemplate, String rmTemplate, String mkdirTemplate) throws IOException {
        super(dbRoot, shareSet, true);
        this.cpTemplate = cpTemplate;
        this.rmTemplate = rmTemplate;
        this.mkdirTemplate = mkdirTemplate;
        
        // Make sure templates are found
        if (cpTemplate == null) {
            throw new IOException("No value found for cptemplate");
        }
        if (rmTemplate == null) {
            throw new IOException("No value found for rmtemplate");
        }
        if (mkdirTemplate == null) {
            throw new IOException("No value found for mkdirtemplate");
        }

        // Make sure the templates have everything they should
        if (cpTemplate.indexOf(SRCPATH_SYMBOL) < 0) {
            throw new IOException("The cptemplate string does not contain " + SRCPATH_SYMBOL);
        }
        if (cpTemplate.indexOf(DSTPATH_SYMBOL) < 0) {
            throw new IOException("The cptemplate string does not contain " + DSTPATH_SYMBOL);
        }
        if (rmTemplate.indexOf(DSTPATH_SYMBOL) < 0) {
            throw new IOException("The rmtemplate string does not contain " + DSTPATH_SYMBOL);
        }
        if (mkdirTemplate.indexOf(DSTPATH_SYMBOL) < 0) {
            throw new IOException("The mkdirtemplate string does not contain " + DSTPATH_SYMBOL);
        }
    }
    
    /**
     * Copy a file from one place to another.  Requires that
     * template-strings be set correctly.  
     */
    protected void copyFile(File srcFile, String locationMach, String locationStr, String nutchFileName, boolean overwrite) throws IOException {
        //
        // Use values to fill in the template strs.
        //
        String cpCommand = cpTemplate.replaceAll(SRCPATH_SYMBOL, srcFile.getPath());
        cpCommand = cpCommand.replaceAll(DSTMACH_SYMBOL, locationMach);
        cpCommand = cpCommand.replaceAll(DSTPATH_SYMBOL, new File(new File(locationStr), nutchFileName).getPath());

        String mkdirCommand = mkdirTemplate.replaceAll(DSTPATH_SYMBOL, new File(new File(locationStr), nutchFileName).getParentFile().getPath());

        //
        // Make sure the target directory exists
        //
        invoke(mkdirCommand);

        //
        // Finally, invoke the newly-built copy command.
        //
        invoke(cpCommand);
    }

    /**
     * Remove a file the given location.  Requires that template-
     * strings be set correctly.  
     */
    protected void deleteFile(String locationMach, String locationStr, String nutchFileName) throws IOException {
        //
        // Use values to fill in template strs
        //
        String rmCommand = rmTemplate.replaceAll(DSTMACH_SYMBOL, locationMach);
        rmCommand = rmCommand.replaceAll(DSTPATH_SYMBOL, new File(new File(locationStr), nutchFileName).getPath());

        //
        // Finally, invoke newly-built command
        //
        invoke(rmCommand);
    }

    /**
     * Currently unimplemented
     */
    protected void lockFile(String locMach, String locStr, String filename, boolean exclusive) throws IOException {
    }

    /**
     */
    protected void release(String locMach, String locStr, String filename) throws IOException {
    }

    /**
     */
    protected void renameFile(File srcFile, String locMach, String locStr, String filename, boolean overwrite) throws IOException {
    }

    /**
     * Take care of the details of invoking an external process.
     * We always assume traditional error-code interpretation 
     * (0 for success, non-zero for failure).
     */
    void invoke(String command) throws IOException {
        Process p = Runtime.getRuntime().exec(command);
        int returnCode = 0;
        try {
            returnCode = p.waitFor();
        } catch (InterruptedException ie) {
            returnCode = -1;
        }

        if (returnCode != 0) {
            throw new IOException("Runtime.exec() failed with code " + returnCode + " while running " + command);
        }
    }
}
