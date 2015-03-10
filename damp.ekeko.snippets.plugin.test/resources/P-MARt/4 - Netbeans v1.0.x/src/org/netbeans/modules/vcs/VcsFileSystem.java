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

package org.netbeans.modules.vcs;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.beans.*;
import java.text.*;
import javax.swing.*;

import org.openide.util.actions.*;
import org.openide.util.NbBundle;
import org.openide.*;
import org.netbeans.modules.vcs.cmdline.*;
import org.netbeans.modules.vcs.util.*;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileSystem.Status;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.AbstractFileSystem;
import org.openide.filesystems.DefaultAttributes;
import org.openide.filesystems.FileStatusEvent;

/** Generic VCS filesystem.
 * 
 * @author Michal Fadljevic
 */
//-------------------------------------------
public abstract class VcsFileSystem extends AbstractFileSystem implements AbstractFileSystem.List, AbstractFileSystem.Info, AbstractFileSystem.Change, FileSystem.Status, Serializable {
    private Debug E=new Debug("VcsFileSystem", true); // NOI18N
    private Debug D=E;

    /**
     * @associates UserCommand 
     */
    private transient Hashtable commandsByName=null;

    protected static final int REFRESH_TIME = 0;
    protected int refreshTimeToSet = REFRESH_TIME;

    private static final String LOCAL_FILES_ADD_VAR = "SHOWLOCALFILES"; // NOI18N
    private static final String VAR_TRUE = "true"; // NOI18N
    private static final String VAR_FALSE = "false"; // NOI18N
    private static final String LOCK_FILES_ON = "LOCKFILES"; // NOI18N
    private static final String PROMPT_FOR_LOCK_ON = "PROMPTFORLOCK"; // NOI18N

    /**
     * The name of the variable for which we get user input.
     */
    private static final String PROMPT_FOR = "PROMPT_FOR"; // NOI18N
    /**
     * The name of the variable for which we get user to set true or false.
     */
    private static final String ASK_FOR = "ASK_FOR"; // NOI18N

    private static int last_refreshTime = REFRESH_TIME;
    private static File last_rootFile = new File (System.getProperty("user.home")); // NOI18N

    private static boolean last_useUnixShell = false;

    /** root file */
    private File rootFile = last_rootFile; // NOI18N

    private boolean useUnixShell = last_useUnixShell;

    /** is read only */
    private boolean readOnly;

    /**
     * @associates VcsConfigVariable 
     */
    protected Hashtable variablesByName = new Hashtable ();

    private boolean lockFilesOn = false;
    private boolean promptForLockOn = true;
    private volatile boolean promptForLockResult = false;

    private boolean debug=true;

    private String config="Empty"; // NOI18N

    /** user variables Vector<String> 'name=value' */
    private Vector variables=new Vector(10);

    private transient String password=null;

    /** advanced confgiguration */
    private Object advanced=null;

    protected transient VcsCache cache=null;

    private long cacheId=0;

    private static transient String CACHE_ROOT="vcs/cache"; // NOI18N
    //  private static transient long CACHE_LAST_ID=0;

    private transient VcsAction action=null;

    private transient ErrorCommandDialog errorDialog = null;
    private transient volatile boolean lastCommandState = true;
    private transient volatile boolean lastCommandFinished = true;

    /**
     * @associates String 
     */
    private transient Vector unimportantNames;

    protected boolean ready=false;
    private boolean askIfDownloadRecursively = true;

    /**
     * @associates Integer 
     */
    private volatile Hashtable numDoAutoRefreshes = new Hashtable();

    /**
     * Whether to prompt the user for variables for each selected file. Value of this variable
     * willl be the default value in the VariableInputDialog and changing the value there will
     * change the value of this variable.
     */
    private boolean promptForVarsForEachFile = false;

    public boolean isLockFilesOn () { return lockFilesOn; }
    public void setLockFilesOn (boolean lock) { lockFilesOn = lock; }
    public boolean isPromptForLockOn () { return promptForLockOn; }
    public void setPromptForLockOn (boolean prompt) { promptForLockOn = prompt; }
    public boolean getAskIfDownloadRecursively () { return askIfDownloadRecursively; }
    public void setAskIfDownloadRecursively (boolean ask) { askIfDownloadRecursively = ask; }
    public boolean isUseUnixShell () { return useUnixShell; }

    protected void setUseUnixShell (boolean unixShell) {
        useUnixShell = unixShell;
        last_useUnixShell = unixShell;
    }

    
    /**
     * Get whether to perform the auto-refresh in the given directory path.
     * @param path The given directory path
     */
    public synchronized boolean getDoAutoRefresh(String path) {
        synchronized (numDoAutoRefreshes) {
            D.deb("getDoAutoRefresh("+path+") ..."); // NOI18N
            int numDoAutoRefresh = getNumDoAutoRefresh(path);
            if (numDoAutoRefresh > 0) {
                numDoAutoRefresh--;
                if (numDoAutoRefresh > 0) setNumDoAutoRefresh(numDoAutoRefresh, path);
                else removeNumDoAutoRefresh(path);
                D.deb("  return "+(numDoAutoRefresh == 0)); // NOI18N
                return (numDoAutoRefresh == 0);
            } else {D.deb("  return true"); return true;} // nothing known about that path, but refresh requested. // NOI18N
        }
    }

    /**
     * Set how many times I call a command after which the auto-refresh is executed in the given path.
     * @param numDoAutoRefresh The number of auto-refreshes
     * @param path The given directory path
     */
    public synchronized void setNumDoAutoRefresh(int numDoAutoRefresh, String path) {
        synchronized (numDoAutoRefreshes) {
            D.deb("setNumDoAutoRefresh("+numDoAutoRefresh+", "+path+")"); // NOI18N
            numDoAutoRefreshes.put(path, new Integer(numDoAutoRefresh));
        }
    }

    /**
     * Get the number of command calls after which perform the auto-refresh command in the given path.
     * @param path The given path
     */
    public synchronized int getNumDoAutoRefresh(String path) {
        synchronized (numDoAutoRefreshes) {
            Integer numDoAutoRefreshObj = (Integer) numDoAutoRefreshes.get(path);
            int numDoAutoRefresh = 0;
            if (numDoAutoRefreshObj != null) {
                numDoAutoRefresh = numDoAutoRefreshObj.intValue();
            }
            D.deb("getNumDoAutoRefresh("+path+") = "+numDoAutoRefresh); // NOI18N
            return numDoAutoRefresh;
        }
    }

    /**
     * Remove the number of command calls after which perform the auto-refresh command in the given path.
     * @param path The given path
     */
    public synchronized void removeNumDoAutoRefresh(String path) {
        synchronized (numDoAutoRefreshes) {
            D.deb("removeNumDoAutoRefresh("+path+")"); // NOI18N
            numDoAutoRefreshes.remove(path);
        }
    }

    public boolean getLastCommandState () { return lastCommandState; }
    public void setLastCommandState (boolean lastCommandState) { this.lastCommandState = lastCommandState; }
    public boolean getLastCommandFinished () { return lastCommandFinished; }
    public void setLastCommandFinished (boolean lastCommandFinished) { this.lastCommandFinished = lastCommandFinished; }

    //-------------------------------------------
    public String getConfigRoot(){
        return ""; // NOI18N
    }

    public FileObject getConfigRootFO(){
        return null; // NOI18N
    }

    /*
     * Mark the file as being unimportant.
     * @param name the file name
     */
    public void markUnimportant(String name) {
        D.deb("==== unimportant("+name+") ====");
        if (!unimportantNames.contains(name)) unimportantNames.addElement(name);
    }

    public boolean isImportant(String name) {
        D.deb("isImportant("+name+")");
        D.deb("unimportantNames = "+unimportantNames);
        D.deb("contains() = "+unimportantNames.contains(name));;
        return !unimportantNames.contains(name);
    }

    /**
     * Perform refresh of status information on all children of a directory
     * @param path the directory path
     * @param recursivey whether to refresh recursively
     */
    public void statusChanged (String path, boolean recursively) {
        //D.deb("statusChanged("+path+")"); // NOI18N
        FileObject fo = findResource(path);
        if (fo == null) return;
        //D.deb("I have root = "+fo.getName()); // NOI18N
        Enumeration enum = fo.getChildren(recursively);
        HashSet hs = new HashSet();
        while(enum.hasMoreElements()) {
            fo = (FileObject) enum.nextElement();
            hs.add(fo);
            //D.deb("Added "+fo.getName()+" fileObject to update status"+fo.getName()); // NOI18N
        }
        Set s = Collections.synchronizedSet(hs);
        fireFileStatusChanged (new FileStatusEvent(this, s, false, true));
    }

    public void setCustomRefreshTime (int time) {
        if (isValid ()) {
            D.deb("Filesystem valid, setting the refresh time to "+time); // NOI18N
            setRefreshTime (time);
        } else {
            D.deb("Filesystem not valid yet for refresh time "+time); // NOI18N
            refreshTimeToSet = time;
        }
        last_refreshTime = time;
    }

    public int getCustomRefreshTime () {
        if (isValid ()) {
            D.deb("Filesystem valid, getting the refresh time "+getRefreshTime ()); // NOI18N
            return getRefreshTime ();
        } else return refreshTimeToSet;
    }

    //-------------------------------------------
    public void setConfig(String label){
        this.config=label;
    }


    //-------------------------------------------
    public String getConfig(){
        return config;
    }


    //-------------------------------------------
    public void debugClear(){
        if( getDebug() ){
            try{
                TopManager.getDefault().getStdOut().reset();
            }catch (IOException e){}
        }
    }


    //-------------------------------------------
    public void debug(String msg){
        if( getDebug() ){
            TopManager.getDefault().getStdOut().println(msg);
        }
    }


    //-------------------------------------------
    public void setImportant(boolean important){
        //D.deb("setImportant("+important+")"); // NOI18N
    }


    //-------------------------------------------
    public VcsCache getCache(){
        return cache;
    }

    //-------------------------------------------
    public void setCache(VcsCache cache) {
        this.cache = cache;
    }

    public abstract VcsFactory getVcsFactory ();


    //-------------------------------------------
    private void createDir(String path){
        File dir=new File(path);
        if( dir.isDirectory() ){
            return ;
        }
        if( dir.mkdirs()==false ){
            E.err(g("MSG_UnableToCreateDirectory", path)); // NOI18N
            debug(g("MSG_UnableToCreateDirectory", path)); // NOI18N
        }
    }

    //-------------------------------------------
    protected String createNewCacheDir(){
        String dir;
        if(cacheId==0) {
            do {
                cacheId = 10000 * (1 + Math.round (Math.random () * 8)) + Math.round (Math.random () * 1000);
            } while (new File(CACHE_ROOT+File.separator+cacheId).isDirectory ());
        }
        dir = CACHE_ROOT+File.separator+cacheId;
        createDir (dir);
        return dir;
    }

    //-------------------------------------------
    protected void init(){
        D.deb ("init()"); // NOI18N
        unimportantNames = new Vector();
        CACHE_ROOT=System.getProperty("netbeans.user")+File.separator+
                   "system"+File.separator+"vcs"+File.separator+"cache"; // NOI18N

        cache=new VcsCache(this, createNewCacheDir ());
        errorDialog = new ErrorCommandDialog(null, new JFrame(), false);
        try {
            setInitRootDirectory(rootFile);
        } catch (PropertyVetoException e) {
            // Could not set root directory
        } catch (IOException e) {
            // Could not set root directory
        }
    }


    static final long serialVersionUID =8108342718973310275L;

    //-------------------------------------------
    public VcsFileSystem() {
        D.deb("VcsFileSystem()"); // NOI18N
        info = this;
        change = this;
        DefaultAttributes a = new DefaultAttributes (info, change, this);
        attr = a;
        list = a;
        setRefreshTime (last_refreshTime);
        refreshTimeToSet = last_refreshTime;
        init();
        D.deb("constructor done.");
    }

    public ErrorCommandDialog getErrorDialog() {
        return errorDialog;
    }

    public void setErrorDialog(ErrorCommandDialog errDlg) {
        errorDialog = errDlg;
    }

    //-------------------------------------------
    public long getCacheId(){
        return cacheId;
    }


    //-------------------------------------------
    private void readObject(ObjectInputStream in) throws
                //D.deb("readObject() - restoring bean"); // NOI18N
        ClassNotFoundException, IOException, NotActiveException{
        // cache is transient
        boolean localFilesOn = in.readBoolean ();
        in.defaultReadObject();
        init();
        cache.setLocalFilesAdd (localFilesOn);
    }


    //-------------------------------------------
    private void writeObject(ObjectOutputStream out) throws IOException {
        //D.deb("writeObject() - saving bean"); // NOI18N
        // cache is transient
        out.writeBoolean (cache.isLocalFilesAdd ());
        out.defaultWriteObject();
    }


    //-------------------------------------------
    public void setDebug(boolean debug){
        this.debug=debug;
    }


    //-------------------------------------------
    public boolean getDebug(){
        return debug;
    }


    //-------------------------------------------
    public Vector getVariables(){
        return variables;
    }


    //-------------------------------------------
    public void setVariables(Vector variables){
        //D.deb ("setVariables()"); // NOI18N
        boolean containsCd = false;
        String cdValue = System.getProperty ("os.name").equals ("Windows NT") ? "cd /D" : "cd";
        int len = variables.size ();
        VcsConfigVariable var;
        for(int i=0; i<len; i++){
            var = (VcsConfigVariable) variables.get (i);
            if(var.getName ().equalsIgnoreCase (LOCAL_FILES_ADD_VAR)) {
                if(var.getValue ().equalsIgnoreCase (VAR_TRUE)) {
                    cache.setLocalFilesAdd (true);
                }
                if(var.getValue ().equalsIgnoreCase (VAR_FALSE)) {
                    cache.setLocalFilesAdd (false);
                }
            }
            if(var.getName ().equalsIgnoreCase (LOCK_FILES_ON)) {
                if(var.getValue ().equalsIgnoreCase (VAR_TRUE)) {
                    setLockFilesOn (true);
                }
                if(var.getValue ().equalsIgnoreCase (VAR_FALSE)) {
                    setLockFilesOn (false);
                }
            }
            if(var.getName ().equalsIgnoreCase (PROMPT_FOR_LOCK_ON)) {
                if(var.getValue ().equalsIgnoreCase (VAR_TRUE)) {
                    setPromptForLockOn (true);
                }
                if(var.getValue ().equalsIgnoreCase (VAR_FALSE)) {
                    setPromptForLockOn (false);
                }
            }
            if(var.getName ().equals ("CD")) { // NOI18N
                //var.setValue (cdValue); <- I don't want to change the value if it is set !!
                containsCd = true;
            }
        }
        if( variables.equals(this.variables) ){
            return ;
        }
        if (!containsCd) {
            variables.add (new VcsConfigVariable ("CD", "cd", cdValue, false, false, false, "", 0)); // NOI18N
        }
        Vector old=this.variables;
        this.variables=variables;
        variablesByName.clear ();
        for (int i=0, n=variables.size (); i<n; i++) {
            var = (VcsConfigVariable) variables.get (i);
            variablesByName.put (var.getName (), var);
        }

        firePropertyChange("variables", old, variables); // NOI18N
    }

    public static String substractRootDir(String rDir, String module) {
        if (module == null || module.length() == 0) return rDir;
        String m;
        if (module.charAt(module.length() - 1) == File.separatorChar)
            m = module.substring(0, module.length() - 1);
        else
            m = module.substring(0);
        String rDirSlashes;
        boolean chRDir = false;
        if (File.separatorChar != '/' && rDir.indexOf(File.separatorChar) > 0) {
            rDirSlashes = rDir.replace(File.separatorChar, '/');
            chRDir = true;
        } else rDirSlashes = rDir;
        String moduleSlashes;
        if (File.separatorChar != '/' && m.indexOf(File.separatorChar) > 0) {
            moduleSlashes = m.replace(File.separatorChar, '/');
        } else moduleSlashes = m;
        int i = rDirSlashes.lastIndexOf(moduleSlashes);
        if (i <= 0) return rDir;
        if (chRDir) return rDir.substring(0, i-1).replace('/', File.separatorChar);
        else return rDir.substring(0, i-1); // I have to remove the slash also.
    }


    //-------------------------------------------
    public Hashtable getVariablesAsHashtable(){
        int len=getVariables().size();
        Hashtable result=new Hashtable(len+5);
        for(int i=0; i<len; i++) {
            VcsConfigVariable var = (VcsConfigVariable) getVariables().elementAt (i);
            result.put(var.getName (), var.getValue ());
        }

        result.put("netbeans.home",System.getProperty("netbeans.home"));
        result.put("netbeans.user",System.getProperty("netbeans.user"));
        result.put("java.home",System.getProperty("java.home"));
        String osName=System.getProperty("os.name");
        result.put("classpath.separator", (osName.indexOf("Win")<0 ? ":":";" )); // NOI18N
        result.put("path.separator", ""+File.separator); // NOI18N
        if(result.get("PS")==null) { // NOI18N
            result.put("PS", ""+File.separator); // NOI18N
        }

        String rootDir = getRootDirectory().toString();
        String module = (String) result.get("MODULE"); // NOI18N
        //if (osName.indexOf("Win") >= 0) // NOI18N
        //module=module.replace('\\','/');
        result.put("ROOTDIR", substractRootDir(rootDir, module)); // NOI18N

        return result;
    }


    //-------------------------------------------
    public void setPassword(String password){
        this.password=password;
    }

    //-------------------------------------------
    public String getPassword(){
        return password;
    }

    /**
     * Find out what to prompt for the user for before running the command.
     * @param exec The command to exec
     * @param vars The variables to use
     * @return The array of variable labels for the user to input, one for each variable
     */
    private String[] needPromptFor(String exec, Hashtable vars) {
        Vector results = new Vector();
        String search = "${"+PROMPT_FOR+"(";
        int pos = 0;
        int index;
        while((index = exec.indexOf(search, pos)) >= 0) {
            index += search.length();
            int index2 = exec.indexOf(")", index);
            if (index2 < 0) break;
            String str = exec.substring(index, index2);
            results.addElement(str);
            pos = index2;
        }
        return (String[]) results.toArray(new String[0]);
    }

    /**
     * Find out what to ask the user for before running the command.
     * @param exec The command to exec
     * @param vars The variables to use
     * @return The array of questions for the user, one for each variable
     */
    private String[] needAskFor(String exec, Hashtable vars) {
        Vector results = new Vector();
        String search = /*"${"+*/ASK_FOR+"("; // to be able to put this to conditional expression
        int pos = 0;
        int index;
        while((index = exec.indexOf(search, pos)) >= 0) {
            index += search.length();
            int index2 = exec.indexOf(")", index);
            if (index2 < 0) break;
            String str = exec.substring(index, index2);
            results.addElement(str);
            pos = index2;
        }
        return (String[]) results.toArray(new String[0]);
    }

    //-------------------------------------------
    private boolean needPromptForPR(String name, String exec, Hashtable vars){
        //D.deb("needPromptFor('"+name+"','"+exec+"')"); // NOI18N
        boolean result=false;
        String oldPassword=(String)vars.get("PASSWORD"); vars.put("PASSWORD",""); // NOI18N
        String oldReason=(String)vars.get("REASON"); vars.put("REASON",""); // NOI18N

        String test="variable_must_be_prompt_for"; // NOI18N
        vars.put(name,test);
        Variables v=new Variables();
        String s=v.expand(vars,exec, false);
        result= ( s.indexOf(test)>=0 ) ? true : false ;

        if( oldPassword!=null ){ vars.put("PASSWORD",oldPassword); } // NOI18N
        if( oldReason!=null ){ vars.put("REASON",oldReason); } // NOI18N

        return result ;
    }


    /**
     * Ask the user for the value of some variables.
     * @param exec the command to execute
     * @param vars the variables
     * @param forEachFile whether to ask for these variables for each file being processed
     * @return true if all variables were entered, false otherways
     */
    public boolean promptForVariables(String exec, Hashtable vars, boolean[] forEachFile){
        if( needPromptForPR("PASSWORD",exec,vars) ){ // NOI18N
            String password=getPassword();
            if(password==null){
                password = ""; // NOI18N
                NotifyDescriptorInputPassword nd = new NotifyDescriptorInputPassword (g("MSG_Password"), g("MSG_Password")); // NOI18N
                if(NotifyDescriptor.OK_OPTION.equals (TopManager.getDefault ().notify (nd))) {
                    password = nd.getInputText ();
                } else return false;
                setPassword(password);
            }
            vars.put("PASSWORD",password); // NOI18N
            /* Do not change forEachFile, if the command is successful it will not ask any more */
        }
        if (forEachFile == null || forEachFile[0] == true) {
            if( needPromptForPR("REASON",exec,vars) ){ // NOI18N
                String reason=""; // NOI18N
                String file = (String) vars.get("FILE"); // NOI18N
                /*
                NotifyDescriptor.InputLine nd;
                if (file != null)
                  nd = new NotifyDescriptor.InputLine (g("MSG_Reason"), g("MSG_ReasonFor", file)); // NOI18N
                else
                  nd = new NotifyDescriptor.InputLine (g("MSG_Reason"), g("MSG_Reason")); // NOI18N
                if(NotifyDescriptor.OK_OPTION.equals (TopManager.getDefault ().notify (nd))) {
                  reason = nd.getInputText ();
            } else return false;
                */
                String[] prompt = new String[1];
                if (file != null) {
                    prompt[0] = new String(g("MSG_ReasonFor", file));
                } else {
                    prompt[0] = new String(g("MSG_Reason"));
                }
                VariableInputDialog dlg = new VariableInputDialog(new java.awt.Frame(), true);
                dlg.setVarPromptLabels(prompt);
                if (forEachFile == null) dlg.showPromptEach(false);
                else dlg.setPromptEach(promptForVarsForEachFile);
                if (dlg.showDialog()) {
                    String[] values = dlg.getVarPromptValues();
                    reason = values[0];
                    if (forEachFile != null) {
                        forEachFile[0] = dlg.getPromptForEachFile();
                        promptForVarsForEachFile = forEachFile[0];
                    }
                } else return false;
                vars.put("REASON", MiscStuff.msg2CmdlineStr(reason, isUseUnixShell())); // NOI18N
            }
            String[] prompt = needPromptFor(exec, vars);
            String[] ask = needAskFor(exec, vars);
            if (prompt != null && prompt.length > 0 || ask != null && ask.length > 0) {
                VariableInputDialog dlg = new VariableInputDialog(new java.awt.Frame(), true);
                dlg.setVarPromptLabels(prompt);
                dlg.setVarAskLabels(ask);
                if (forEachFile == null) dlg.showPromptEach(false);
                else dlg.setPromptEach(promptForVarsForEachFile);
                if (dlg.showDialog()) {
                    String[] values = dlg.getVarPromptValues();
                    for(int i = 0; i < prompt.length; i++) {
                        vars.put(PROMPT_FOR+"("+prompt[i]+")", MiscStuff.msg2CmdlineStr(values[i], isUseUnixShell()));
                    }
                    values = dlg.getVarAskValues();
                    for(int i = 0; i < ask.length; i++) {
                        vars.put(ASK_FOR+"("+ask[i]+")", values[i]);
                    }
                    if (forEachFile != null) {
                        forEachFile[0] = dlg.getPromptForEachFile();
                        promptForVarsForEachFile = forEachFile[0];
                    }
                } else return false;
            }
        }
        return true;
    }

    protected void warnDirectoriesDoNotExists() {
        D.deb("warnDirectoriesDoNotExists()");
        Hashtable vars = getVariablesAsHashtable();
        String module = (String) vars.get("MODULE");
        if (module == null) module = "";
        String rootDir = substractRootDir(getRootDirectory().toString(), module);
        File root = new File(rootDir);
        D.deb("RootDirectory = "+rootDir);
        if( root == null || !root.isDirectory() ){
            //E.err("not directory "+root); // NOI18N
            D.deb("NOT DIRECTORY: "+root);
            final String badDir = root.toString();
            javax.swing.SwingUtilities.invokeLater(new Runnable () {
                                                       public void run () {
                                                           TopManager.getDefault ().notify (new NotifyDescriptor.Message(MessageFormat.format (org.openide.util.NbBundle.getBundle(VcsFileSystem.class).getString("Filesystem.notRootDirectory"), new Object[] { badDir } )));
                                                       }
                                                   });
            return ;
        }
        File moduleDir = new File(root, module);
        D.deb("moduleDir = "+moduleDir);
        if( moduleDir == null || !moduleDir.isDirectory() ){
            D.deb("NOT DIRECTORY: "+moduleDir);
            final String badDir = module;
            javax.swing.SwingUtilities.invokeLater(new Runnable () {
                                                       public void run () {
                                                           TopManager.getDefault ().notify (new NotifyDescriptor.Message(MessageFormat.format (org.openide.util.NbBundle.getBundle(VcsFileSystem.class).getString("Filesystem.notModuleDirectory"), new Object[] { badDir } )));
                                                       }
                                                   });
        }
    }

    //-------------------------------------------
    public FileSystem.Status getStatus(){
        return this;
    }


    //-------------------------------------------
    public Image annotateIcon(Image icon, int iconType, Set files) {
        //D.deb("annotateIcon()"); // NOI18N
        return icon;
    }


    //-------------------------------------------
    public String annotateName(String name, Set files) {
        String result=name;
        String fullName=""; // NOI18N
        String fileName=""; // NOI18N

        Object[] oo=files.toArray();
        int len=oo.length;
        if( len==0 || name.indexOf(getRootDirectory().toString())>=0){
            return result;
        }

        if( len==1 ){
            FileObject ff=(FileObject)oo[0];
            fullName=ff.getPackageNameExt('/','.');
            fileName=MiscStuff.getFileNamePart(fullName);

            String status=cache.getFileStatus(fullName).trim();
            //D.deb("name = "+fullName+": status = "+status);
            if( status.length()>0 ){
                result=name+" ["+status+"]"; // NOI18N
            }
            String locker = cache.getFileLocker(fullName);
            //D.deb("locker = '"+locker+"'");
            if (locker != null && locker.length() > 0) {
                result += " ("+locker+")";  // NOI18N
            }
        }
        else{
            Vector/*<VcsFile>*/ importantFiles=getImportantFiles(oo);
            String status=cache.getStatus(importantFiles).trim();
            //D.deb("status = "+status);
            if( status.length()>0 ){
                result=name+" ["+status+"]"; // NOI18N
            }
            String locker = cache.getLocker(importantFiles);
            //D.deb("locker = '"+locker+"'");
            if (locker != null && locker.length() > 0) {
                result += " ("+locker+")";  // NOI18N
            }
        }

        D.deb("annotateName("+name+") -> result='"+result+"'"); // NOI18N
        return result;
    }


    //-------------------------------------------
    private Vector/*VcsFile*/ getImportantFiles(Object[] oo){
        //D.deb("getImportantFiles()"); // NOI18N
        Vector result=new Vector(3);
        int len=oo.length;

        for(int i=0;i<len;i++){
            FileObject ff=(FileObject)oo[i];
            String fullName=ff.getPackageNameExt('/','.');
            String fileName=MiscStuff.getFileNamePart(fullName);

            VcsFile file=cache.getFile(fullName);
            if( file==null ){
                D.deb("no such file '"+fullName+"'"); // NOI18N
                continue ;
            }
            //D.deb("fileName="+fileName); // NOI18N
            //if( file.isImportant() ){ // TODO Change this line !!!
            if( fileName.indexOf(".class")<0 ){ // NOI18N
                result.addElement(file);
            }
        }

        return result;
    }


    //-------------------------------------------
    public SystemAction[] getActions(){
        //D.deb("getActions()"); // NOI18N
        if( action==null ){
            action=getVcsFactory ().getVcsAction(this);
        }
        SystemAction [] actions=new SystemAction[1];
        actions[0]=action;
        return actions;
    }

    public void setValidFS(boolean v) {
        boolean valid = isValid();
        D.deb("Filesystem is "+((valid) ? "":"not ")+"valid.");
        if (v != valid) {
            D.deb("setting valid = "+v);
            firePropertyChange (org.openide.filesystems.FileSystem.PROP_VALID,
                                new Boolean (!v), new Boolean (v));
        }
        D.deb("Filesystem is "+((isValid()) ? "":"not ")+"valid.");
    }

    //-------------------------------------------
    /* Human presentable name */
    public String getDisplayName() {
        //D.deb("getDisplayName() isValid="+isValid()); // NOI18N
        /*
        if(!isValid())
          return g("LAB_FileSystemInvalid", rootFile.toString ()); // NOI18N
        else
        */
        return g("LAB_FileSystemValid", rootFile.toString ()); // NOI18N
    }

    /**
     * Set the root directory of the filesystem to the parameter passed.
     * @param r file to set root to
     * @exception PropertyVetoException if the value if vetoed by someone else (usually
     *    by the {@link org.openide.filesystems.Repository Repository})
     * @exception IOException if the root does not exists or some other error occured
     */
    private void setInitRootDirectory(File r) throws PropertyVetoException, IOException {
        Hashtable vars = getVariablesAsHashtable();
        String module = (String) vars.get("MODULE");
        if (module == null) module = "";
        String root = r.getCanonicalPath();
        if (module.length() > 0) {
            int i = root.indexOf(module);
            if (i > 0) root = root.substring(0, i - 1);
        }
        r = new File(root);
        setRootDirectory(r);
    }

    //-------------------------------------------
    /** Set the root directory of the file system. It adds the module name to the parameter.
     * @param r file to set root to plus module name
     * @exception PropertyVetoException if the value if vetoed by someone else (usually
     *    by the {@link org.openide.filesystems.Repository Repository})
     * @exception IOException if the root does not exists or some other error occured
     */
    public synchronized void setRootDirectory (File r) throws PropertyVetoException, IOException {
        //D.deb("setRootDirectory("+r+")"); // NOI18N
        if (/*!r.exists() ||*/ r.isFile ()) {
            throw new IOException(g("EXC_RootNotExist", r.toString ())); // NOI18N
        }

        Hashtable vars = getVariablesAsHashtable();
        String module = (String) vars.get("MODULE");
        if (module == null) module = "";
        File root = new File(r, module);
        String name = computeSystemName (root);
        /* Ignoring other filesystems' names => it is possible to mount VCS filesystem with the same name.
        Enumeration en = TopManager.getDefault ().getRepository ().fileSystems ();
        while (en.hasMoreElements ()) {
          FileSystem fs = (FileSystem) en.nextElement ();
          if (fs.getSystemName ()==name) {
            // NotifyDescriptor.Exception nd = new NotifyDescriptor.Exception (
            throw new PropertyVetoException ("Directory already mounted", // NOI18N
              new PropertyChangeEvent (this, "RootDirectory", getSystemName (), name)); // NOI18N
            // TopManager.getDefault ().notify (nd);
          }
    }
        */
        D.deb("Setting system name '"+name+"'"); // NOI18N
        setSystemName(name);

        rootFile = root;
        last_rootFile = new File(""+root);
        ready=true ;

        firePropertyChange("root", null, refreshRoot ()); // NOI18N
    }

    //-------------------------------------------
    public void setRootFile(File rootFile) {
        this.rootFile = rootFile;
    }

    //-------------------------------------------
    /** Get the root directory of the file system.
     * @return root directory
     */
    public File getRootDirectory () {
        return rootFile;
    }

    //-------------------------------------------
    /** Set whether the file system should be read only.
     * @param flag <code>true</code> if it should
     */
    public void setReadOnly(boolean flag) {
        D.deb("setReadOnly("+flag+")"); // NOI18N
        if (flag != readOnly) {
            readOnly = flag;
            firePropertyChange (PROP_READ_ONLY, new Boolean (!flag), new Boolean (flag));
        }
    }

    //-------------------------------------------
    /* Test whether file system is read only.
     * @return <true> if file system is read only
     */
    public boolean isReadOnly() {
        //D.deb("isReadOnly() ->"+readOnly); // NOI18N
        return readOnly;
    }

    //-------------------------------------------
    /** Prepare environment by adding the root directory of the file system to the class path.
     * @param environment the environment to add to
     */
    public void prepareEnvironment(FileSystem.Environment environment) {
        D.deb("prepareEnvironment() ->"+rootFile.toString()); // NOI18N
        environment.addClassPath(rootFile.toString ());
    }

    //-------------------------------------------
    /** Compute the system name of this file system for a given root directory.
     * <P>
     * The default implementation simply returns the filename separated by slashes.
     * @see FileSystem#setSystemName
     * @param rootFile root directory for the filesystem
     * @return system name for the filesystem
     */
    protected String computeSystemName (File rootFile) {
        D.deb("computeSystemName() ->"+rootFile.toString ().replace(File.separatorChar, '/') ); // NOI18N
        return rootFile.toString ().replace(File.separatorChar, '/');
    }

    //-------------------------------------------
    /** Creates file for given string name.
     * @param name the name
     * @return the file
     */
    public File getFile (String name) {
        return new File (rootFile, name);
    }

    //-------------------------------------------
    //
    // List
    //

    //-------------------------------------------
    /* Scans children for given name
     */
    public String[] children (String name) {
        D.deb("children('"+name+"')"); // NOI18N
        String[] vcsFiles=null;
        String[] files=null;

        if( !ready ){
            D.deb("not ready"); // NOI18N
            return new String[0];
        }

        if( cache.isDir(name) ){
            vcsFiles=cache.getFilesAndSubdirs(name);
            D.deb("vcsFiles="+MiscStuff.arrayToString(vcsFiles)); // NOI18N

            String p=""; // NOI18N
            try{
                p=rootFile.getCanonicalPath();
            }
            catch (IOException e){
                E.err(e,"getCanonicalPath() failed"); // NOI18N
            }
            files=cache.dirsFirst(p+File.separator+name,vcsFiles);
            D.deb("files="+MiscStuff.arrayToString(files)); // NOI18N
            return files;
        }
        return new String[0];
    }


    // create local folder for existing VCS folder that is missing
    private void checkLocalFolder (String name) throws java.io.IOException {
        StringTokenizer st = new java.util.StringTokenizer (name, "/"); // NOI18N
        String dir = null;
        while(st.hasMoreElements()) {
            dir = dir==null ? (String) st.nextElement () : dir + "/" + (String) st.nextElement (); // NOI18N
            File f = getFile (dir);
            if(f.exists ()) continue;

            Object[] errorParams = new Object[] {
                                       f.getName (),
                                       getDisplayName (),
                                       f.toString ()
                                   };

            boolean b = f.mkdir();
            if (!b) {
                throw new IOException(MessageFormat.format (g("EXC_CannotCreateF"), errorParams)); // NOI18N
            }
            D.deb ("local dir created='"+dir+"'"); // NOI18N
        }
    }

    //-------------------------------------------
    //
    // Change
    //

    /* Creates new folder named name.
     * @param name name of folder
     * @throws IOException if operation fails
     */
    public void createFolder (String name) throws java.io.IOException {
        D.deb("createFolder('"+name+"')"); // NOI18N
        if( name.startsWith("/") ){ // NOI18N
            // Jarda TODO
            name=name.substring(1);
            D.deb("corrected name='"+name+"'"); // NOI18N
        }

        File f = getFile (name);
        Object[] errorParams = new Object[] {
                                   f.getName (),
                                   getDisplayName (),
                                   f.toString ()
                               };

        if (name.equals ("")) { // NOI18N
            throw new IOException(MessageFormat.format (g("EXC_CannotCreateF"), errorParams)); // NOI18N
        }

        if (f.exists()) {
            throw new IOException(MessageFormat.format (g("EXC_FolderAlreadyExist"), errorParams)); // NOI18N
        }

        int lastSeparator = name.lastIndexOf ("/"); // NOI18N

        if (lastSeparator > 0) checkLocalFolder (name.substring (0, lastSeparator));


        boolean b = f.mkdir();
        if (!b) {
            throw new IOException(MessageFormat.format (g("EXC_CannotCreateF"), errorParams)); // NOI18N
        }
        cache.addFolder(name);
    }

    //-------------------------------------------
    /* Create new data file.
     *
     * @param name name of the file
     *
     * @return the new data file object
     * @exception IOException if the file cannot be created (e.g. already exists)
     */
    public void createData (String name) throws IOException {
        D.deb("createData("+name+")"); // NOI18N
        if( name.startsWith("/") ){ // NOI18N
            // Jarda TODO
            name=name.substring(1);
            D.deb("corrected name='"+name+"'"); // NOI18N
        }

        File f = getFile (name);
        Object[] errorParams = new Object[] {
                                   f.getName (),
                                   getDisplayName (),
                                   f.toString (),
                               };

        int lastSeparator = name.lastIndexOf ("/"); // NOI18N

        //if (lastSeparator < 0) lastSeparator = 0;

        if (lastSeparator > 0) checkLocalFolder (name.substring (0, lastSeparator));


        if (!f.createNewFile ()) {
            throw new IOException(MessageFormat.format (g("EXC_DataAlreadyExist"), errorParams)); // NOI18N
        }
        cache.addFile(name);
        cache.setFileStatus(name, cache.localStatusStr);
    }

    //-------------------------------------------
    /* Renames a file.
     *
     * @param oldName old name of the file
     * @param newName new name of the file
     */
    public void rename(String oldName, String newName) throws IOException {
        D.deb("rename(oldName="+oldName+",newName="+newName+")"); // NOI18N
        File of = getFile (oldName);
        File nf = getFile (newName);

        if (!of.renameTo (nf)) {
            throw new IOException(g("EXC_CannotRename", oldName, getDisplayName (), newName)); // NOI18N
        }
        cache.rename(oldName, newName);
    }

    //-------------------------------------------
    /* Delete the file.
     *
     * @param name name of file
     * @exception IOException if the file could not be deleted
     */
    public void delete (String name) throws IOException {
        D.deb("delete('"+name+"')"); // NOI18N
        File file = getFile (name);
        /*
        if (!file.delete()) {
          throw new IOException (g("EXC_CannotDelete", name, getDisplayName (), file.toString ()));
    }
        */
        if (!file.exists()) return; // silently ignore non existing files
        if (!MiscStuff.deleteRecursive(file)) {
            throw new IOException (g("EXC_CannotDelete", name, getDisplayName (), file.toString ())); // NOI18N
        }
        cache.removeFile(name);
    }

    //-------------------------------------------
    //
    // Info
    //

    //-------------------------------------------
    /*
     * Get last modification time.
     * @param name the file to test
     * @return the date
     */
    public java.util.Date lastModified(String name) {
        D.deb("lastModified("+name+")"); // NOI18N
        return new java.util.Date (getFile (name).lastModified ());
    }

    //-------------------------------------------
    /* Test if the file is folder or contains data.
     * @param name name of the file
     * @return true if the file is folder, false otherwise
     */
    public boolean folder (String name) {
        return cache.isDir(name);
        // return getFile (name).isDirectory ();
    }

    //-------------------------------------------
    /* Test whether this file can be written to or not.
     * All folders are not read only, they are created before writting into them.
     * @param name the file to test
     * @return <CODE>true</CODE> if file is read-only
     */
    public boolean readOnly (String name) {
        //D.deb("readOnly('"+name+"')"); // NOI18N
        if(folder(name)) return false;
        return !getFile (name).canWrite ();
    }

    /** Get the MIME type of the file.
     * Uses {@link FileUtil#getMIMEType}.
     *
     * @param name the file to test
     * @return the MIME type textual representation, e.g. <code>"text/plain"</code>
     */
    public String mimeType (String name) {
        D.deb("mimeType('"+name+"')"); // NOI18N
        int i = name.lastIndexOf ('.');
        String s;
        try {
            s = FileUtil.getMIMEType (name.substring (i + 1));
        } catch (IndexOutOfBoundsException e) {
            s = null;
        }
        D.deb("mimeType() -> '"+s+"'"); // NOI18N
        return s == null ? "content/unknown" : s; // NOI18N
    }

    //-------------------------------------------
    /* Get the size of the file.
     *
     * @param name the file to test
     * @return the size of the file in bytes or zero if the file does not contain data (does not
     *  exist or is a folder).
     */
    public long size (String name) {
        D.deb("size("+name+")"); // NOI18N
        return getFile (name).length ();
    }

    /* Get input stream.
     *
     * @param name the file to test
     * @return an input stream to read the contents of this file
     * @exception FileNotFoundException if the file does not exists or is invalid
     */
    public InputStream inputStream (String name) throws java.io.FileNotFoundException {
        //D.deb("inputStream("+name+")"); // NOI18N
        return new FileInputStream (getFile (name));
    }

    //-------------------------------------------
    /* Get output stream.
     *
     * @param name the file to test
     * @return output stream to overwrite the contents of this file
     * @exception IOException if an error occures (the file is invalid, etc.)
     */
    public OutputStream outputStream (String name) throws java.io.IOException {
        D.deb("outputStream("+name+")"); // NOI18N
        return new FileOutputStream (getFile (name));
    }

    public synchronized boolean getPromptForLockResult() {
        return promptForLockResult;
    }

    public synchronized void setPromptForLockResult(boolean promptForLockResult) {
        this.promptForLockResult = promptForLockResult;
    }

    /** Run the LOCK command to lock the file.
     *
     * @param name name of the file
     */
    public void lock (String name_) throws IOException {
        if (!isImportant(name_)) return; // ignore locking of unimportant files
        final String name = name_;
        final VcsFileSystem current = this;
        new Thread(new Runnable() {
                       public void run() {
                           D.deb("lock('"+name+"')"); // NOI18N
                           D.deb("this = "+this); // NOI18N
                           File f = getFile (name);
                           if(f.canWrite ()) return;
                           Vector files = new Vector();
                           files.add (name);
                           getVcsFactory ().getVcsAction (current).doEdit (files);
                           if(isLockFilesOn ()) {
                               VcsFile vcsFile = cache.getFile (name);
                               // *.orig is a temporary file created by AbstractFileObject
                               // on saving every file to enable undo if saving fails
                               if(vcsFile==null || vcsFile.isLocal () || name.endsWith (".orig")) return; // NOI18N
                               else {
                                   D.deb ("lock on file:"+vcsFile.toString()); // NOI18N
                                   setPromptForLockResult(false);
                                   if(isPromptForLockOn ()) {
                                       try {
                                           javax.swing.SwingUtilities.invokeAndWait(new Runnable() {
                                                       public void run() {
                                                           boolean result;
                                                           NotifyDescriptor.Confirmation confirm = new NotifyDescriptor.Confirmation (g("MSG_LockFileCh"), NotifyDescriptor.Confirmation.OK_CANCEL_OPTION); // NOI18N
                                                           result = (TopManager.getDefault ().notify (confirm).equals (NotifyDescriptor.Confirmation.OK_OPTION));
                                                           setPromptForLockResult(result);
                                                       }
                                                   });
                                       } catch (InterruptedException e) {
                                           setPromptForLockResult(true);
                                       } catch (java.lang.reflect.InvocationTargetException e) {
                                           setPromptForLockResult(true);
                                       }
                                   }
                                   if(!isPromptForLockOn () || getPromptForLockResult()) {
                                       files = new Vector();
                                       files.add (name);
                                       getVcsFactory ().getVcsAction (current).doLock (files);
                                   }
                               }
                           }
                       }
                   }, "VCS-Locking Files").start(); // NOI18N
    }

    /** Does nothing to unlock the file.
     *
     * @param name name of the file
     */
    public void unlock (String name) {
        if (!isImportant(name)) return; // ignore unlocking of unimportant files
        D.deb("unlock('"+name+"')"); // NOI18N
        if(isLockFilesOn ()) {
            Vector files = new Vector();
            files.add (name);
            getVcsFactory ().getVcsAction (this).doUnlock (files);
        }
    }

    //-------------------------------------------
    /** Does nothing to mark the file as unimportant.
     *
     * @param name the file to mark
     *
    public void markUnimportant (String name) {
      // TODO...
        D.deb(" ==== markUnimportant("+name+") ==== "); // NOI18N
            VcsFile file=cache.getFile(name);
            if( file==null ){
              //E.err("no such file '"+name+"'"); // NOI18N
              return ;
            }
            file.setImportant(false);
}
    */

    public Object getAdvancedConfig () {
        return this.advanced;
    }

    //------------------------------------------
    public void setAdvancedConfig (Object advanced) {
        //super.setAdvancedConfig (advanced);
        this.advanced = advanced;
        Vector commands = (Vector) advanced;
        int len=commands.size();
        commandsByName=new Hashtable(len+5);
        for(int i=0;i<len;i++){
            UserCommand uc=(UserCommand)commands.elementAt(i);
            commandsByName.put(uc.getName(), uc);
        }
    }

    //-------------------------------------------
    public Vector getCommands(){
        return (Vector) getAdvancedConfig ();
    }

    //-------------------------------------------
    public void setCommands(Vector commands){
        setAdvancedConfig (commands);
    }

    //-------------------------------------------
    public UserCommand getCommand(String name){
        if( commandsByName==null ){
            setCommands ((Vector) getAdvancedConfig ());
        }
        return (UserCommand)commandsByName.get(name);
    }


    //-------------------------------------------
    public Vector getAdditionalCommands(){
        Vector commands=getCommands();
        if (commands == null) return null;
        int len=commands.size();
        Vector additionalCommands=new Vector(5);
        for(int i=0;i<len;i++){
            UserCommand uc=(UserCommand)commands.elementAt(i);
            if( isAdditionalCommand(uc.getName()) ){
                additionalCommands.add(uc);
            }
        }
        return additionalCommands;
    }



    //-------------------------------------------
    public boolean isAdditionalCommand(String name){
        if( name.equals("LIST") || // NOI18N
                name.equals("DETAILS") || // NOI18N
                name.equals("CHECKIN") || // NOI18N
                name.equals("CHECKOUT") || // NOI18N
                name.equals("LOCK") || // NOI18N
                name.equals("UNLOCK") || // NOI18N
                name.equals("ADD") || // NOI18N
                name.equals("REMOVE") || // NOI18N
                name.equals("LIST_SUB") ){ // NOI18N
            return false ;
        }
        return true;
    }

    public FilenameFilter getLocalFileFilter() {
        return null;
    }

    public String getBundleProperty(String s) {
        return g(s);
    }

    //-------------------------------------------
    protected String g(String s) {
        D.deb("getting "+s);
        return NbBundle.getBundle
               ("org.netbeans.modules.vcs.cmdline.Bundle").getString (s);
    }
    protected String  g(String s, Object obj) {
        return MessageFormat.format (g(s), new Object[] { obj });
    }
    protected String g(String s, Object obj1, Object obj2) {
        return MessageFormat.format (g(s), new Object[] { obj1, obj2 });
    }
    protected String g(String s, Object obj1, Object obj2, Object obj3) {
        return MessageFormat.format (g(s), new Object[] { obj1, obj2, obj3 });
    }
    //-------------------------------------------
}
/*
 * Log
 *  36   Gandalf-post-FCS1.32.1.2    4/4/00   Martin Entlicher serial version added, 
 *       filesystem always mounts O.K.
 *  35   Gandalf-post-FCS1.32.1.1    3/29/00  Martin Entlicher Improved synchronization
 *       of automatic refresh, support for asking variables,  deserialization of
 *       root fixed.
 *  34   Gandalf-post-FCS1.32.1.0    3/23/00  Martin Entlicher Remember the last 
 *       refresh time, remember unimportant names, the change of the status is 
 *       possible to perform recursively, support for asking the user for 
 *       additional variables before the command is run.
 *  33   Gandalf   1.32        3/8/00   Martin Entlicher VCS properties read from
 *       filesystem
 *  32   Gandalf   1.31        2/15/00  Martin Entlicher netbeans.user added to 
 *       variables.
 *  31   Gandalf   1.30        2/11/00  Martin Entlicher changed setRootDirectory
 *       to consider its argument as a working directory  without module name.
 *  30   Gandalf   1.29        2/10/00  Martin Entlicher Locking action changed, 
 *       warning of nonexistent root directory or module name, automatic refresh
 *       after last command only.
 *  29   Gandalf   1.28        2/9/00   Martin Entlicher Set user.home as the 
 *       starting directory.
 *  28   Gandalf   1.27        1/19/00  Martin Entlicher Deleted catching of 
 *       annotated name,  new files has initial local status.
 *  27   Gandalf   1.26        1/18/00  Martin Entlicher 
 *  26   Gandalf   1.25        1/17/00  Martin Entlicher 
 *  25   Gandalf   1.24        1/15/00  Ian Formanek    NOI18N
 *  24   Gandalf   1.23        1/6/00   Martin Entlicher 
 *  23   Gandalf   1.22        1/5/00   Martin Entlicher 
 *  22   Gandalf   1.21        12/28/99 Martin Entlicher One ErrorCommandDialog 
 *       for the whole session + Yuri changes
 *  21   Gandalf   1.20        12/21/99 Martin Entlicher Refresh time set after 
 *       the filesystem is mounted.
 *  20   Gandalf   1.19        12/16/99 Martin Entlicher 
 *  19   Gandalf   1.18        12/8/99  Martin Entlicher 
 *  18   Gandalf   1.17        11/30/99 Martin Entlicher 
 *  17   Gandalf   1.16        11/24/99 Martin Entlicher 
 *  16   Gandalf   1.15        11/23/99 Martin Entlicher 
 *  15   Gandalf   1.14        11/16/99 Martin Entlicher Fixed update of file 
 *       status
 *  14   Gandalf   1.13        11/9/99  Martin Entlicher 
 *  13   Gandalf   1.12        11/9/99  Martin Entlicher 
 *  12   Gandalf   1.11        11/4/99  Martin Entlicher 
 *  11   Gandalf   1.10        11/2/99  Pavel Buzek     statusChanged is using 
 *       fireFileStatusChanged
 *  10   Gandalf   1.9         10/26/99 Martin Entlicher 
 *  9    Gandalf   1.8         10/25/99 Pavel Buzek     copyright and log
 *  8    Gandalf   1.7         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  7    Gandalf   1.6         10/12/99 Pavel Buzek     
 *  6    Gandalf   1.5         10/9/99  Pavel Buzek     
 *  5    Gandalf   1.4         10/9/99  Pavel Buzek     
 *  4    Gandalf   1.3         10/9/99  Pavel Buzek     
 *  3    Gandalf   1.2         10/7/99  Pavel Buzek     
 *  2    Gandalf   1.1         10/5/99  Pavel Buzek     VCS at least can be 
 *       mounted
 *  1    Gandalf   1.0         9/30/99  Pavel Buzek     
 * $
*/
