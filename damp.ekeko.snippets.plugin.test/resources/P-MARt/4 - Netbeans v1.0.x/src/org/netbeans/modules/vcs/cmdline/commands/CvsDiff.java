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
import org.netbeans.modules.vcs.util.*;
import org.netbeans.modules.vcs.VcsFileSystem;
import org.netbeans.modules.vcs.cmdline.exec.*;
import org.openide.nodes.*;
import org.openide.loaders.DataObject;
import org.openide.filesystems.FileObject;
import org.openide.cookies.*;

import java.util.*;
import java.io.*;
import java.net.URL;
import javax.swing.*;
import javax.swing.text.*;

/**
 *
 * @author  Martin Entlicher
 * @version 
 */
public class CvsDiff extends VcsAdditionalCommand implements RegexListener {

    private Debug E=new Debug("CvsDiff",true); // NOI18N
    private Debug D=E;

    //private static transient String TMP_ROOT="vcs/tmp"; // NOI18N
    private transient String TMP_ROOT;
    private File tmpDir = null;
    private File tmpDir2 = null;
    private String tmpDirName = ""; // NOI18N
    private String tmpDir2Name = ""; // NOI18N


    /**
     * @associates String 
     */
    Hashtable vars = null;

    private String rootDir = null;
    private String rootDirWroot = null;
    private String dir = null;
    //private String mdir = null;
    private String file = null;

    private String diffDataRegex = null;
    private NoRegexListener stdoutNRListener = null;
    private NoRegexListener stderrNRListener = null;
    private RegexListener stdoutListener = null;
    private RegexListener stderrListener = null;
    private String dataRegex = null;
    private String errorRegex = null;
    private String checkoutCmd = null;
    private String diffCmd = null;

    private StringBuffer diffBuffer = new StringBuffer(4096);

    /**
     * @associates DiffAction 
     */
    private Vector diffActions = new Vector();
    private CvsDiffFrame diffFrame = null;
    private static int currentDiffLine = 0;
    private static final java.awt.Color colorMissing = new java.awt.Color(255, 160, 180);
    private static final java.awt.Color colorAdded = new java.awt.Color(180, 255, 180);
    private static final java.awt.Color colorChanged = new java.awt.Color(160, 200, 255);
    //private JEditorPane e1;
    //private JEditorPane e2;

    /** Creates new CvsDiff */
    public CvsDiff() {
    }

    private File createTMP() {
        TMP_ROOT=System.getProperty("netbeans.user")+File.separator+
                 "system"+File.separator+"vcs"+File.separator+"tmp";
        File tmpDir = new File(TMP_ROOT);
        if (!tmpDir.exists()) {
            tmpDir.mkdirs();
        }
        long tmpId;
        do {
            tmpId = 10000 * (1 + Math.round (Math.random () * 8)) + Math.round (Math.random () * 1000);
        } while (new File(TMP_ROOT+File.separator+"tmp"+tmpId).exists()); // NOI18N
        TMP_ROOT = TMP_ROOT+File.separator+"tmp"+tmpId; // NOI18N
        tmpDir = new File(TMP_ROOT);
        if (!tmpDir.exists()) {
            tmpDir.mkdirs();
        }
        return tmpDir;
    }

    private boolean checkOut(Hashtable vars, String file, String revision, String tmpDir) {
        String cmd = checkoutCmd;
        String varRevision = ""; // NOI18N
        //if (revision != null) varRevision = " -r "+revision+" "; // NOI18N
        if (revision != null) varRevision = ""+revision; // NOI18N
        vars.put("REVISION", varRevision); // NOI18N
        D.deb("varRevision = "+varRevision); // NOI18N
        vars.put("TEMPDIR", tmpDir); // NOI18N
        D.deb("checkOut Command: "+cmd); // NOI18N
        Variables v=new Variables();
        String prepared=v.expand(vars,cmd, true);
        D.deb("checkOut prepared: "+prepared); // NOI18N
        if (stderrListener != null) {
            String[] command = { "CHECKOUT: "+prepared }; // NOI18N
            stderrListener.match(command);
        }
        if (stderrNRListener != null) stderrNRListener.match("CHECKOUT: "+prepared); // NOI18N
        ExternalCommand ec=new ExternalCommand(prepared);
        ec.setTimeout(((Long) vars.get("TIMEOUT")).longValue()); // NOI18N
        if (this.stdoutNRListener != null) ec.addStdoutNoRegexListener(this.stdoutNRListener);
        if (this.stderrNRListener != null) ec.addStderrNoRegexListener(this.stderrNRListener);
        if (this.stdoutListener != null) {
            try {
                ec.addStdoutRegexListener(this.stdoutListener, this.dataRegex);
            } catch (BadRegexException e) {
                if (stderrListener != null) {
                    String[] elements = { "CHECKOUT: Bad data regex "+dataRegex+"\n" }; // NOI18N
                    stderrListener.match(elements);
                }
            }
        }
        if (this.stderrListener != null) {
            try {
                ec.addStderrRegexListener(this.stderrListener, this.errorRegex);
            } catch (BadRegexException e) {
                String[] elements = { "CHECKOUT: Bad error regex "+errorRegex+"\n" }; // NOI18N
                stderrListener.match(elements);
            }
        }
        if ( ec.exec() != ExternalCommand.SUCCESS ){
            E.err("exec failed "+ec.getExitStatus()); // NOI18N
            return false;
        } else return true;
    }

    private boolean performDiff(String revision1, String revision2) {
        String cmd = diffCmd;
        String varRevision = ""; // NOI18N
        if (revision1 != null) varRevision += " -r "+revision1+" "; // NOI18N
        if (revision2 != null) varRevision += " -r "+revision2+" "; // NOI18N
        vars.put("REVISION", varRevision); // NOI18N
        D.deb("diff command: "+cmd); // NOI18N
        Variables v=new Variables();
        String prepared=v.expand(vars,cmd, true);
        D.deb("diff prepared: "+prepared); // NOI18N
        if (stderrListener != null) {
            String[] command = { "DIFF: "+prepared }; // NOI18N
            stderrListener.match(command);
        }
        if (stderrNRListener != null) stderrNRListener.match("DIFF: "+prepared); // NOI18N
        ExternalCommand ec=new ExternalCommand(prepared);
        ec.setTimeout(((Long) vars.get("TIMEOUT")).longValue()); // NOI18N
        try{
            D.deb("stdout diff dataRegex = "+diffDataRegex); // NOI18N
            ec.addStdoutRegexListener(this,diffDataRegex);
        } catch (BadRegexException e) {
            if (stderrListener != null) {
                String[] elements = { "cvs diff: Bad data regex "+diffDataRegex }; // NOI18N
                stderrListener.match(elements);
            }
            return false;
        }
        if (this.stdoutNRListener != null) ec.addStdoutNoRegexListener(stdoutNRListener);
        if (this.stderrNRListener != null) ec.addStderrNoRegexListener(stderrNRListener);
        if (this.stdoutListener != null) {
            try {
                ec.addStdoutRegexListener(this.stdoutListener, this.dataRegex);
            } catch (BadRegexException e) {
                if (stderrListener != null) {
                    String[] elements = { "DIFF: Bad data regex "+dataRegex+"\n" }; // NOI18N
                    stderrListener.match(elements);
                }
            }
        }
        if (this.stderrListener != null) {
            try {
                ec.addStderrRegexListener(this.stderrListener, this.errorRegex);
            } catch (BadRegexException e) {
                String[] elements = { "DIFF: Bad error regex "+errorRegex+"\n" }; // NOI18N
                stderrListener.match(elements);
            }
        }
        if ( ec.exec() != ExternalCommand.SUCCESS ){
            D.deb("exec failed "+ec.getExitStatus()); // NOI18N
            return false;
        }
        return true;
    }

    private boolean openEditors(String file1, String file2, String title1, String title2) {
        String mime = (String) vars.get("MIMETYPE"); // NOI18N
        D.deb("I have MIME = "+mime); // NOI18N
        URL url1 = null;
        URL url2 = null;
        try {
            url1 = new File(file1).toURL();
            url2 = new File(file2).toURL();
        } catch (java.net.MalformedURLException e) {
            D.deb("MalformedURLException "+e.getMessage()); // NOI18N
            return false;
        }
        diffFrame = new CvsDiffFrame(this);
        MiscStuff.centerWindow(diffFrame);
        if (mime != null) {
            diffFrame.setMimeType1(mime);
            diffFrame.setMimeType2(mime);
        }
        try {
            diffFrame.setFile1(url1);
            diffFrame.setFile2(url2);
        } catch (IOException e) {
            D.err("IO Exception "+e.getMessage()); // NOI18N
            return false;
        }
        diffFrame.setFile1Title(title1);
        diffFrame.setFile2Title(title2);
        diffFrame.pack();
        diffFrame.show();
        return true;
    }

    private void insertEmptyLines() {
        int n = diffActions.size();
        int ins1 = 0;
        int ins2 = 0;
        D.deb("insertEmptyLines():"); // NOI18N
        for(int i = 0; i < n; i++) {
            DiffAction action = (DiffAction) diffActions.get(i);
            int n1 = action.getF1Line1() + ins1;
            int n2 = action.getF1Line2() + ins1;
            int n3 = action.getF2Line1() + ins2;
            int n4 = action.getF2Line2() + ins2;
            D.deb("Action: "+action.getAction()+": ("+n1+","+n2+","+n3+","+n4+")"); // NOI18N
            D.deb("ins1 = "+ins1+", ins2 = "+ins2); // NOI18N
            switch (action.getAction()) {
            case DiffAction.DELETE:
                diffFrame.addEmptyLines2(n3, n2 - n1 + 1);
                ins2 += n2 - n1 + 1;
                break;
            case DiffAction.ADD:
                diffFrame.addEmptyLines1(n1, n4 - n3 + 1);
                ins1 += n4 - n3 + 1;
                break;
            case DiffAction.CHANGE:
                int r1 = n2 - n1;
                int r2 = n4 - n3;
                if (r1 < r2) {
                    diffFrame.addEmptyLines1(n2, r2 - r1);
                    ins1 += r2 - r1;
                } else if (r1 > r2) {
                    diffFrame.addEmptyLines2(n4, r1 - r2);
                    ins2 += r1 - r2;
                }
                break;
            }
            action.setF1Line1(n1);
            action.setF1Line2(n2);
            action.setF2Line1(n3);
            action.setF2Line2(n4);
        }
    }

    private void setDiffHighlight(boolean set) {
        int n = diffActions.size();
        D.deb("Num Actions = "+n); // NOI18N
        for(int i = 0; i < n; i++) {
            DiffAction action = (DiffAction) diffActions.get(i);
            int n1 = action.getF1Line1();
            int n2 = action.getF1Line2();
            int n3 = action.getF2Line1();
            int n4 = action.getF2Line2();
            D.deb("Action: "+action.getAction()+": ("+n1+","+n2+","+n3+","+n4+")"); // NOI18N
            switch (action.getAction()) {
            case DiffAction.DELETE:
                if (set) diffFrame.highlightRegion1(n1, n2, colorMissing);
                else diffFrame.highlightRegion1(n1, n2, java.awt.Color.white);
                break;
            case DiffAction.ADD:
                if (set) diffFrame.highlightRegion2(n3, n4, colorAdded);
                else diffFrame.highlightRegion2(n3, n4, java.awt.Color.white);
                break;
            case DiffAction.CHANGE:
                if (set) {
                    diffFrame.highlightRegion1(n1, n2, colorChanged);
                    diffFrame.highlightRegion2(n3, n4, colorChanged);
                } else {
                    diffFrame.highlightRegion1(n1, n2, java.awt.Color.white);
                    diffFrame.highlightRegion2(n3, n4, java.awt.Color.white);
                }
                break;
            }
        }
    }

    /**
     * Executes the checkout and diff commands and display differences.
     * @param vars variables needed to run cvs commands
     * @param args the arguments, first two are supposed to be the revision tags to be compared.
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
        boolean status = true;
        this.stdoutNRListener = stdoutNRListener;
        this.stderrNRListener = stderrNRListener;
        this.stdoutListener = stdoutListener;
        this.dataRegex = dataRegex;
        this.stderrListener = stderrListener;
        this.errorRegex = errorRegex;
        this.vars = vars;
        int arglen = args.length;
        if (arglen < 2) {
            String message = "Too few arguments to Diff command !"; // NOI18N
            String[] elements = { message };
            if (stderrListener != null) stderrListener.match(elements);
            if (stderrNRListener != null) stderrNRListener.match(message);
            return false;
        }
        this.checkoutCmd = args[arglen - 2];
        this.diffCmd = args[arglen - 1];
        String mime = (String) vars.get("MIMETYPE"); // NOI18N
        if (mime == null || mime.indexOf("unknown") >= 0) { // NOI18N
            String message = org.openide.util.NbBundle.getBundle(CvsDiff.class).getString("CvsDiff.unknownMIMETYPE");
            String[] elements = { message };
            if (stderrListener != null) stderrListener.match(elements);
            if (stderrNRListener != null) stderrNRListener.match(message);
            return false;
        }
        this.rootDir = (String) vars.get("ROOTDIR"); // NOI18N
        String module = (String) vars.get("MODULE"); // NOI18N
        if (module == null) module = ""; // NOI18N
        if (module.length() > 0) module += File.separator;
        this.rootDirWroot = VcsFileSystem.substractRootDir(rootDir, module);
        D.deb("rootDir = "+rootDir+", module = "+module+" => rootDirWroot = "+rootDirWroot); // NOI18N
        //this.dir = (String) vars.get("DIR"); // NOI18N
        this.dir = module + (String) vars.get("DIR"); // NOI18N
        this.file = (String) vars.get("FILE"); // NOI18N
        tmpDir = createTMP();
        //tmpDirName = tmpDir.getName();
        tmpDirName = tmpDir.getAbsolutePath();
        String path = rootDir+File.separator+dir+File.separator+file;
        this.diffDataRegex = (String) vars.get("DATAREGEX"); // NOI18N
        if (this.diffDataRegex == null) this.diffDataRegex = "(^.*)$"; // NOI18N
        this.diffDataRegex = "(^[0-9]+(,[0-9]+|)[d][0-9]+$)|(^[0-9]+(,[0-9]+|)[c][0-9]+(,[0-9]+|)$)|(^[0-9]+[a][0-9]+(,[0-9]+|)$)"; // NOI18N
        String revision1 = null;
        String revision2 = null;
        if (args != null && args.length > 2) {
            revision1 = args[0];
            if (args.length > 3) {
                revision2 = args[1];
                tmpDir2 = createTMP();
                tmpDir2Name = tmpDir2.getAbsolutePath();
            }
        }
        status = checkOut(vars, dir+File.separator+file, revision1, tmpDirName);
        if (!status) {
            close();
            return status;
        }
        if (revision2 != null) {
            status = checkOut(vars, dir+File.separator+file, revision2, tmpDir2Name);
            if (!status) {
                close();
                return status;
            }
        }
        performDiff(revision1, revision2);
        final String file1Title = (revision1 == null) ? g("CvsDiff.titleCVSHeadRevision") : g("CvsDiff.titleCVSRevision", revision1); // NOI18N
        final String file2Title = (revision2 == null) ? g("CvsDiff.titleWorkingFile") : g("CvsDiff.titleCVSRevision", revision2); // NOI18N
        javax.swing.SwingUtilities.invokeLater(new Runnable () {
                                                   public void run () {
                                                       if (tmpDir2 == null)
                                                           openEditors(tmpDir+/*File.separator+dir+*/File.separator+file,
                                                                       rootDirWroot+File.separator+dir+File.separator+file,
                                                                       file1Title, file2Title);
                                                       else
                                                           openEditors(tmpDir+/*File.separator+dir+*/File.separator+file,
                                                                       tmpDir2+/*File.separator+dir+*/File.separator+file,
                                                                       file1Title, file2Title);
                                                       diffFrame.setTitle("cvs diff: "+file); // NOI18N
                                                       diffFrame.repaint();
                                                       insertEmptyLines();
                                                       setDiffHighlight(true);
                                                   }
                                               });
        D.deb("exec return = "+status); // NOI18N
        return status;
    }

    public int getNextDiffLine() {
        currentDiffLine++;
        if (currentDiffLine >= diffActions.size()) currentDiffLine = 0;
        return ((DiffAction) diffActions.get(currentDiffLine)).getF1Line1();
    }

    public int getPrevDiffLine() {
        currentDiffLine--;
        if (currentDiffLine < 0) currentDiffLine = diffActions.size() - 1;
        return ((DiffAction) diffActions.get(currentDiffLine)).getF1Line1();
    }
    /*
    public void diffAgain() {
      //setDiffHighlight(false);
      diffFrame.unhighlightAll();
      diffActions.removeAllElements();
      performDiff();
      setDiffHighlight(true);
}
    */
    public void close() {
        //new File(tmpDir, file).delete();
        D.deb("deleting "+tmpDir); // NOI18N
        MiscStuff.deleteRecursive(tmpDir);
        if (tmpDir2 != null) {
            D.deb("deleting "+tmpDir2); // NOI18N
            MiscStuff.deleteRecursive(tmpDir2);
        }
    }

    private boolean checkEmpty(String str, String element) {
        if (str == null || str.length() == 0) {
            if (this.stderrListener != null) {
                String[] elements = { "Bad format of diff result: "+element }; // NOI18N
                stderrListener.match(elements);
            }
            E.deb("Bad format of diff result: "+element); // NOI18N
            return true;
        }
        return false;
    }

    public void match(String[] elements) {
        diffBuffer.append(elements[0]+"\n"); // NOI18N
        D.deb("diff match: "+elements[0]); // NOI18N

        int index = 0, commaIndex = 0;
        int n1 = 0, n2 = 0, n3 = 0, n4 = 0;
        String nStr;
        if ((index = elements[0].indexOf('a')) >= 0) {
            DiffAction action = new DiffAction();
            try {
                n1 = Integer.parseInt(elements[0].substring(0, index));
                index++;
                commaIndex = elements[0].indexOf(',', index);
                if (commaIndex < 0) {
                    nStr = elements[0].substring(index, elements[0].length());
                    if (checkEmpty(nStr, elements[0])) return;
                    n3 = Integer.parseInt(nStr);
                    n4 = n3;
                } else {
                    nStr = elements[0].substring(index, commaIndex);
                    if (checkEmpty(nStr, elements[0])) return;
                    n3 = Integer.parseInt(nStr);
                    nStr = elements[0].substring(commaIndex+1, elements[0].length());
                    if (nStr == null || nStr.length() == 0) n4 = n3;
                    else n4 = Integer.parseInt(nStr);
                }
            } catch (NumberFormatException e) {
                if (this.stderrListener != null) {
                    String[] debugOut = { "NumberFormatException "+e.getMessage() }; // NOI18N
                    stderrListener.match(debugOut);
                }
                E.deb("NumberFormatException "+e.getMessage()); // NOI18N
            }
            action.setAddAction(n1, n3, n4);
            diffActions.add(action);
        } else if ((index = elements[0].indexOf('d')) >= 0) {
            DiffAction action = new DiffAction();
            commaIndex = elements[0].lastIndexOf(',', index);
            try {
                if (commaIndex < 0) {
                    n1 = Integer.parseInt(elements[0].substring(0, index));
                    n2 = n1;
                } else {
                    nStr = elements[0].substring(0, commaIndex);
                    if (checkEmpty(nStr, elements[0])) return;
                    n1 = Integer.parseInt(nStr);
                    nStr = elements[0].substring(commaIndex+1, index);
                    if (checkEmpty(nStr, elements[0])) return;
                    n2 = Integer.parseInt(nStr);
                }
                nStr = elements[0].substring(index+1, elements[0].length());
                if (checkEmpty(nStr, elements[0])) return;
                n3 = Integer.parseInt(nStr);
            } catch (NumberFormatException e) {
                if (this.stderrListener != null) {
                    String[] debugOut = { "NumberFormatException "+e.getMessage() }; // NOI18N
                    stderrListener.match(debugOut);
                }
                E.deb("NumberFormatException "+e.getMessage()); // NOI18N
            }
            action.setDeleteAction(n1, n2, n3);
            diffActions.add(action);
        } else if ((index = elements[0].indexOf('c')) >= 0) {
            DiffAction action = new DiffAction();
            commaIndex = elements[0].lastIndexOf(',', index);
            try {
                if (commaIndex < 0) {
                    n1 = Integer.parseInt(elements[0].substring(0, index));
                    n2 = n1;
                } else {
                    nStr = elements[0].substring(0, commaIndex);
                    if (checkEmpty(nStr, elements[0])) return;
                    n1 = Integer.parseInt(nStr);
                    nStr = elements[0].substring(commaIndex+1, index);
                    if (checkEmpty(nStr, elements[0])) return;
                    n2 = Integer.parseInt(nStr);
                }
                index++;
                commaIndex = elements[0].indexOf(',', index);
                if (commaIndex < 0) {
                    nStr = elements[0].substring(index, elements[0].length());
                    if (checkEmpty(nStr, elements[0])) return;
                    n3 = Integer.parseInt(nStr);
                    n4 = n3;
                } else {
                    nStr = elements[0].substring(index, commaIndex);
                    if (checkEmpty(nStr, elements[0])) return;
                    n3 = Integer.parseInt(nStr);
                    nStr = elements[0].substring(commaIndex+1, elements[0].length());
                    if (nStr == null || nStr.length() == 0) n4 = n3;
                    else n4 = Integer.parseInt(nStr);
                }
            } catch (NumberFormatException e) {
                if (this.stderrListener != null) {
                    String[] debugOut = { "NumberFormatException "+e.getMessage() }; // NOI18N
                    stderrListener.match(debugOut);
                }
                E.deb("NumberFormatException "+e.getMessage()); // NOI18N
            }
            action.setChangeAction(n1, n2, n3, n4);
            diffActions.add(action);
        }
    }

    private String g(String s) {
        D.deb("getting "+s);
        return org.openide.util.NbBundle.getBundle(CvsDiff.class).getString(s);
    }

    private String g(String s, Object obj) {
        return java.text.MessageFormat.format (g(s), new Object[] { obj });
    }

    private class DiffAction {
        public static final int DELETE = 0;
        public static final int CHANGE = 1;
        public static final int ADD = 2;

        private int action = 0;
        private int f1Line1 = 0;
        private int f1Line2 = 0;
        private int f2Line1 = 0;
        private int f2Line2 = 0;

        public DiffAction()  {
        }

        public void setDeleteAction(int f1Line1, int f1Line2, int f2Line1) {
            this.action = DELETE;
            this.f1Line1 = f1Line1;
            this.f1Line2 = f1Line2;
            this.f2Line1 = f2Line1;
        }

        public void setAddAction(int f1Line1, int f2Line1, int f2Line2) {
            this.action = ADD;
            this.f1Line1 = f1Line1;
            this.f2Line1 = f2Line1;
            this.f2Line2 = f2Line2;
        }

        public void setChangeAction(int f1Line1, int f1Line2, int f2Line1, int f2Line2) {
            this.action = CHANGE;
            this.f1Line1 = f1Line1;
            this.f1Line2 = f1Line2;
            this.f2Line1 = f2Line1;
            this.f2Line2 = f2Line2;
        }

        public int getAction() {
            return this.action;
        }

        public int getF1Line1() {
            return this.f1Line1;
        }

        public void setF1Line1(int value) {
            this.f1Line1 = value;
        }

        public int getF1Line2() {
            return this.f1Line2;
        }

        public void setF1Line2(int value) {
            this.f1Line2 = value;
        }

        public int getF2Line1() {
            return this.f2Line1;
        }

        public void setF2Line1(int value) {
            this.f2Line1 = value;
        }

        public int getF2Line2() {
            return this.f2Line2;
        }

        public void setF2Line2(int value) {
            this.f2Line2 = value;
        }
    }
}
/*
 * Log
 *  27   Gandalf-post-FCS1.25.1.0    3/29/00  Martin Entlicher Changed default tmp 
 *       directory to system/vcs/tmp
 *  26   Gandalf   1.25        1/18/00  Martin Entlicher 
 *  25   Gandalf   1.24        1/15/00  Ian Formanek    NOI18N
 *  24   Gandalf   1.23        1/6/00   Martin Entlicher 
 *  23   Gandalf   1.22        12/29/99 Martin Entlicher 
 *  22   Gandalf   1.21        12/28/99 Martin Entlicher 
 *  21   Gandalf   1.20        12/21/99 Martin Entlicher Changed to read the 
 *       command from it's argument
 *  20   Gandalf   1.19        12/14/99 Martin Entlicher Listeners added  
 *  19   Gandalf   1.18        12/8/99  Martin Entlicher 
 *  18   Gandalf   1.17        12/2/99  Martin Entlicher 
 *  17   Gandalf   1.16        11/11/99 Martin Entlicher checkout for diff 
 *       changed to non-absolut pathname
 *  16   Gandalf   1.15        11/10/99 Martin Entlicher 
 *  15   Gandalf   1.14        11/9/99  Martin Entlicher 
 *  14   Gandalf   1.13        11/9/99  Martin Entlicher 
 *  13   Gandalf   1.12        11/4/99  Martin Entlicher 
 *  12   Gandalf   1.11        11/2/99  Martin Entlicher 
 *  11   Gandalf   1.10        10/27/99 Martin Entlicher Checkout fixed for Win 
 *       NT
 *  10   Gandalf   1.9         10/26/99 Martin Entlicher 
 *  9    Gandalf   1.8         10/26/99 Martin Entlicher 
 *  8    Gandalf   1.7         10/26/99 Martin Entlicher 
 *  7    Gandalf   1.6         10/25/99 Pavel Buzek     
 *  6    Gandalf   1.5         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  5    Gandalf   1.4         10/13/99 Martin Entlicher 
 *  4    Gandalf   1.3         10/9/99  Pavel Buzek     
 *  3    Gandalf   1.2         10/9/99  Martin Entlicher 
 *  2    Gandalf   1.1         10/9/99  Martin Entlicher Added SERVERTYPE 
 *       variable
 *  1    Gandalf   1.0         10/7/99  Martin Entlicher initial revision
 * $
 */
