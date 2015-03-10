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

package org.netbeans.modules.vcs.cmdline.commands;

import org.netbeans.modules.vcs.cmdline.*;
import org.netbeans.modules.vcs.VcsFileSystem;
import org.netbeans.modules.vcs.util.*;
import org.netbeans.modules.vcs.cmdline.exec.*;
import java.util.*;

/**
 *
 * @author  Martin Entlicher
 * @version 
 */
public class CvsAddTag extends VcsAdditionalCommand {

    private Debug E=new Debug("CvsAddTag",true); // NOI18N
    private Debug D=E;

    /**
     * @associates String 
     */
    Hashtable vars = null;
    CvsLogInfo logInfo = new CvsLogInfo();
    private NoRegexListener stdoutNRListener = null;
    private NoRegexListener stderrNRListener = null;
    private RegexListener stdoutListener = null;
    private RegexListener stderrListener = null;
    private String dataRegex = null;
    private String errorRegex = null;

    /** Creates new CvsAddTag */
    public CvsAddTag() {
    }

    /**
     * Executes the tag command to add the tag specified in AddTagDialog
     * @param vars variables needed to run cvs commands
     * @param args the arguments,
     * @param stdoutNRListener listener of the standard output of the command
     * @param stderrNRListener listener of the error output of the command
     * @param stdoutListener listener of the standard output of the command which
     *                       satisfies regex <CODE>dataRegex</CODE>
     * @param dataRegex the regular expression for parsing the standard output
     * @param stderrListener listener of the error output of the command which
     *                       satisfies regex <CODE>errorRegex</CODE>
     * @param errorRegex the regular expression for parsing the error output
     * @return true if the command was succesfull,
     *         false if some error has occured.
     */
    public boolean exec(Hashtable vars, String[] args,
                        NoRegexListener stdoutNRListener, NoRegexListener stderrNRListener,
                        RegexListener stdoutListener, String dataRegex,
                        RegexListener stderrListener, String errorRegex) {
        this.vars = vars;
        this.stdoutNRListener = stdoutNRListener;
        this.stderrNRListener = stderrNRListener;
        this.stdoutListener = stdoutListener;
        this.dataRegex = dataRegex;
        this.stderrListener = stderrListener;
        this.errorRegex = errorRegex;
        if (args.length < 2) {
            String message = org.openide.util.NbBundle.getBundle(AddTagDialog.class).getString("AddTag.tooFewArgs");
            String[] elements = { message };
            if (stderrListener != null) stderrListener.match(elements);
            if (stderrNRListener != null) stderrNRListener.match(message);
            return false;
        }
        String[] logInfoArgs = new String[1];
        logInfoArgs[0] = args[0];
        String[] addTagArgs = new String[1];
        addTagArgs[0] = args[1];
        boolean success;
        success = this.logInfo.updateLogInfo(vars, logInfoArgs, stdoutNRListener, stderrNRListener);
        if (success) {
            AddTagDialog at;
            javax.swing.JFrame tagFrame = new javax.swing.JFrame ();
            MiscStuff.centerWindow(tagFrame);
            at = new AddTagDialog (tagFrame, true);
            MiscStuff.centerWindow(at);
            Vector revisions = logInfo.getRevisions();
            at.setRevisions(revisions);
            //at.show ();
            //wait();
            success = at.showDialog();
            if (success) {
                D.deb("exec: revision at "+at.getRevision()+" = "+(String) revisions.get(at.getRevision())); // NOI18N
                success = addTag(revisions, addTagArgs, at.getRevision(), at.getTagName(),
                                 at.isReleaseTag(), at.isRepositoryTag());
            } else success = true;
        }
        D.deb("exec: success = "+success); // NOI18N
        return success;
    }

    private boolean addTag(Vector revisions, String[] args, int index, String tagName,
                           boolean isReleaseTag, boolean isRepositoryTag) {
        String cmd;
        if (isRepositoryTag)
            cmd = "rtag "; // NOI18N
        else
            cmd = "tag "; // NOI18N
        if (!isReleaseTag) // It is a branch tag
            cmd += "-b "; // NOI18N
        if (index > 0) cmd += "-r " + (String) revisions.get(index) + " "; // NOI18N
        cmd += "\\\""+tagName+"\\\""; // NOI18N
        /*
        String osName = System.getProperty("os.name");
        String rootDir = (String) vars.get("ROOTDIR");
        String module = (String) vars.get("MODULE");
        if (module == null) module = "";
        String rootDirWroot = VcsFileSystem.substractRootDir(rootDir, module);
        String modfile = (String) vars.get("MODPATH");
        if(osName.indexOf("Win") >= 0)
          cmd = "cmd /X /C \"${CD} \\\""+rootDirWroot+"\\\"&& set CVSROOT=:${SERVERTYPE}:${CVSROOT}&& "+
                cmd+" \\\""+modfile+"\\\"\"";
        else
          cmd = "sh -c \"${CD} \\\""+rootDirWroot+"\\\"; CVSROOT=\\\":${SERVERTYPE}:${CVSROOT}\\\"; export CVSROOT; "+
                cmd+" \\\""+modfile+"\\\"\"";
        */
        vars.put("CVS_TAG", cmd); // NOI18N
        D.deb("addTag: cmd = "+cmd); // NOI18N
        String fullCmd = MiscStuff.array2string(args);
        Variables v=new Variables();
        String prepared=v.expand(vars,fullCmd, true);
        D.deb("Add Tag prepared: "+prepared); // NOI18N
        if (stderrListener != null) {
            String[] command = { "ADDTAG: "+prepared }; // NOI18N
            stderrListener.match(command);
        }
        if (stderrNRListener != null) stderrNRListener.match("ADDTAG: "+prepared); // NOI18N
        ExternalCommand ec=new ExternalCommand(prepared);
        ec.setTimeout(((Long) vars.get("TIMEOUT")).longValue()); // NOI18N
        /*String logDataRegex = (String) vars.get("DATAREGEX");
        try{
          D.deb("stdout log dataRegex = "+logDataRegex);
          ec.addStdoutRegexListener(this, logDataRegex);
    } catch (BadRegexException e) {
          if (stderrListener != null) stderrListener.match("cvs log: Bad data regex "+logDataRegex+"\n");
          return false;
    }*/
        if (stdoutNRListener != null) ec.addStdoutNoRegexListener(stdoutNRListener);
        if (stderrNRListener != null) ec.addStderrNoRegexListener(stderrNRListener);
        if (stdoutListener != null) {
            try {
                ec.addStdoutRegexListener(stdoutListener, dataRegex);
            } catch (BadRegexException e) {
                if (stderrListener != null) {
                    String[] elements = { "AddTag: Bad data regex "+dataRegex+"\n" }; // NOI18N
                    stderrListener.match(elements);
                }
            }
        }
        if (stderrListener != null) {
            try {
                ec.addStderrRegexListener(stderrListener, errorRegex);
            } catch (BadRegexException e) {
                String[] elements = { "AddTag: Bad data regex "+errorRegex+"\n" }; // NOI18N
                stderrListener.match(elements);
            }
        }
        if ( ec.exec() != ExternalCommand.SUCCESS ){
            E.err("exec failed "+ec.getExitStatus()); // NOI18N
            return false;
        } else return true;
    }
}