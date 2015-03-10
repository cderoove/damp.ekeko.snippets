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
import java.io.*;
import java.util.*;
import java.beans.*;
import java.text.*;

import org.netbeans.modules.vcs.util.*;
import org.netbeans.modules.vcs.cmdline.exec.*;
import org.openide.*;
import org.openide.util.*;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.RepositoryListener;

/** This class is used by CommandLineVcsFileSystem to access files and directories
 *in both VCS and local disk. It holds tree of directories that were loaded from VCS
 *with list of their files and subdirectories in a memory cache. <br>
 *Memory cache is also saved to disk cache (in NetBeansHome/system/vcs/cache directory).
 *This enables fast access to structure of directories after restart of IDE. <br>
 *VCS directories are first searched for in memory cache than in disk cache.
 *If they are not found in disk cache they are automatically readed from VCS.
 *Local directories are not chached and are readed again every time the directory is refreshed.
 *Local directories can be refreshed automatically by setting refresh time greater than 0.
 *VCS directories are never refreshed automatically. To refresh VCS directories from VCS user has to
 *call <B>Version Control|Refresh</B> action on the directory.
 * @author Michal Fadljevic, Pavel Buzek
 */

//-------------------------------------------
public class VcsCache implements DirReaderListener, RepositoryListener {
    private Debug E=new Debug("VcsCache", true); // NOI18N
    private Debug D=E;

    /** All directories by full path. [key='path/to/some/dir' value=VcsDir]
     * @associates VcsDir
    */
    private Hashtable dirsByName=new Hashtable(200);

    private VcsFileSystem fileSystem=null;

    private String cacheDir=""; // NOI18N

    public final String localStatusStr = g("CTL_StatusLocal"); // NOI18N

    private boolean localFilesAdd = true;

    private volatile int numRefreshThreads = 0;
    private volatile boolean lastCanceled = false;

    /**
     * @associates String 
     */
    private volatile Vector pathsQueue = null;

    //-------------------------------------------
    public VcsCache(VcsFileSystem fileSystem, String cacheDir){
        this.fileSystem=fileSystem;
        this.cacheDir=cacheDir;
        WeakListener.Repository wl = new WeakListener.Repository (this);
        TopManager.getDefault ().getRepository ().addRepositoryListener (wl);
        numRefreshThreads = 0;
        lastCanceled = false;
        pathsQueue = new Vector();
    }

    public void fileSystemAdded(final org.openide.filesystems.RepositoryEvent p1) {
    }

    public void fileSystemPoolReordered(final org.openide.filesystems.RepositoryReorderedEvent p1) {
    }

    /** Remove cache directory when filesystem is removed.
    */
    public void fileSystemRemoved(final org.openide.filesystems.RepositoryEvent ev) {
        if(fileSystem.equals (ev.getFileSystem ())) {
            File dir = new File (cacheDir);
            if(dir.exists () && dir.isDirectory () && dir.canWrite ()) {
                if(!deleteDir (dir)) System.out.println("Cannot delete VCS cache directory:"+dir.getPath ()); // NOI18N
            }
            fileSystem.setCustomRefreshTime(0); //@Yury Kamen@//
        }
    }

    private boolean deleteDir (File dir) {
        // System.out.println("removing cache dir:"+dir.getPath ()); // NOI18N
        return MiscStuff.deleteRecursive(dir);
    }

    public void setLocalFilesAdd (boolean localFilesAdd) {
        this.localFilesAdd = localFilesAdd;
    }

    public boolean isLocalFilesAdd () {
        return localFilesAdd;
    }

    //-------------------------------------------
    private String arrayToLine(String[] sa){
        StringBuffer sb=new StringBuffer(100);
        for(int i=0;i<sa.length;i++){
            sb.append(sa[i].trim()+"|"); // NOI18N
        }
        return new String(sb);
    }


    //-------------------------------------------
    private void writeDirToDiskCache(String path, Vector rawData){
        String dirName=cacheDir+ (path.equals("")?"":(File.separator+path)); // NOI18N

        //D.deb("writeDirToDiskCache() dirName='"+dirName+"'"); // NOI18N
        File d=new File(dirName);
        if( d.isDirectory()==false ){
            if( d.mkdirs()==false ){
                E.err("Cannot create dir "+dirName); // NOI18N
                return ;
            }
        }
        BufferedWriter out = null;
        try{
            out = new BufferedWriter(
                      new OutputStreamWriter(
                          new BufferedOutputStream(
                              new FileOutputStream(dirName+File.separator+"list.txt")))); // NOI18N

            int len=rawData.size();
            //D.deb("writeDirToDiskCache("+path+", "+rawData+", len = "+len);
            for(int i=0;i<len;i++){
                String[] elements=(String[])rawData.elementAt(i);
                out.write( arrayToLine(elements) +"\n"); // NOI18N
            }
        }
        catch (IOException e){
            E.err(e,"writeDirToDiskCache() failed for "+dirName); // NOI18N
        } finally {
            try {
                if (out != null) out.close();
            } catch (IOException e) {
                E.err(e,"writeDirToDiskCache() failed for "+dirName); // NOI18N
            }
        }
    }

    private void writeDirToDiskCacheRecursive(VcsDir dir, VcsDirContainer rawData) {
        if (dir == null || rawData == null) return;
        writeDirToDiskCache(dir.getPath(), (Vector) rawData.getElement());
        String[] subdirs = dir.getSubdirs();
        for(int i = 0; i < subdirs.length; i++) {
            VcsDir subdir = dir.getDir(subdirs[i]);
            //D.deb("Putting '"+subdirs[i]+"' under '"+subdir.getPath()+"' to cache.");
            if (subdir != null) {
                writeDirToDiskCacheRecursive(subdir, rawData.getDirContainer(subdirs[i]));
            }
        }
    }

    private void renameDirInDiskCache(String oldName, String newName) {
        String oldPath = cacheDir + File.separator + oldName;
        String newPath = cacheDir + File.separator + newName;
        File oldF = new File(oldPath);
        File newF = new File(newPath);
        boolean success = oldF.renameTo(newF);
        if (!success) {
            E.err("renameDirInDiskCache failed for "+oldPath+" -> "+newPath);
        }
    }

    //-------------------------------------------
    /** Add the folder in memory cache and put it in the list of parent`s subdirectories.
     * @param path path to folder from root of VCS
     */
    public void addFolder(String path) {
        D.deb("addFolder('"+path+"')"); // NOI18N
        String dirName=MiscStuff.getDirNamePart(path);
        String fileName=MiscStuff.getFileNamePart(path);
        D.deb("dirName="+dirName+", fileName="+fileName); // NOI18N

        VcsDir parent=getDir(dirName);
        VcsDir kid=new VcsDir(fileName);
        kid.setLocal(true);
        kid.setStatus(localStatusStr);
        parent.add(kid);

        registerInMemoryCache(path,kid);
        heyDoRefreshDir(path);
    }


    public void rename(String oldName, String newName) {
        VcsFile file = getFile(oldName);
        D.deb("rename ("+oldName+", "+newName+"), file = "+file);
        if (file != null) {
            file.setName(MiscStuff.getFileNamePart(newName));
            return;
        }
        VcsDir dir = getDir(oldName);
        if (dir != null) {
            VcsDir newDir = (VcsDir) dir;
            renameDirInDiskCache(oldName, newName);
            unregisterFromMemoryCacheRecursive(newDir);
            newDir.rename(newName);
            registerInMemoryCacheRecursive(newName, newDir);
        }
    }

    //-------------------------------------------
    private void registerInMemoryCache(String path, VcsDir dir){
        D.deb("registerInMemoryCache('"+path+"',"+dir+")"); // NOI18N
        String[] subdirs = dir.getSubdirs();
        for(int i = 0; i < subdirs.length; i++) {
            VcsDir subdir = dir.getDir(subdirs[i]);
            D.deb("Putting '"+subdirs[i]+"' under '"+subdir.getPath()+"' to cache.");
            if (subdir != null) {
                VcsDir lastDir = (VcsDir) dirsByName.get(subdir.getPath());
                if (lastDir == null) dirsByName.put(subdir.getPath(), subdir);
                else {
                    lastDir.setStatus(subdir.getStatus());
                    lastDir.setLocker(subdir.getLocker());
                    lastDir.setLocal(subdir.isLocal());
                    lastDir.setLoaded(subdir.isLoaded());
                    D.deb("Dir "+subdir.getPath()+" already exists in the cache, status ["+subdir.getStatus()+"] updated");
                    D.deb("lastDir = "+lastDir);
                    dir.remove(subdir);
                    dir.add(lastDir);
                }
            }
            /*
            dirsByName.put(/*((path.length() > 0) ? path+"/" : "")+*//*subdirs[i],
                            dir.getDir(subdirs[i]));
            */
        }
        dirsByName.put(path,dir);
    }

    private void registerInMemoryCacheRecursive(String path, VcsDir dir){
        D.deb("registerInMemoryCacheRecursive('"+path+"',"+dir+")"); // NOI18N
        String[] subdirs = dir.getSubdirs();
        for(int i = 0; i < subdirs.length; i++) {
            VcsDir subdir = dir.getDir(subdirs[i]);
            D.deb("Putting '"+subdirs[i]+"' under '"+subdir.getPath()+"' to cache.");
            if (subdir != null) {
                registerInMemoryCacheRecursive(subdir.getPath(), subdir);
            }
        }
        dirsByName.put(path,dir);
    }

    //-------------------------------------------
    private void unregisterFromMemoryCache(String path){
        VcsDir dir=(VcsDir)dirsByName.get(path);
        if( dir==null ){
            return ;
        }
        D.deb("unregisterFromMemoryCache('"+path+"',"+dir+")"); // NOI18N
        dirsByName.remove(path);
    }

    private void unregisterFromMemoryCacheRecursive(VcsDir dir){
        String[] subdirs = dir.getSubdirs();
        for(int i = 0; i < subdirs.length; i++) {
            VcsDir subdir = dir.getDir(subdirs[i]);
            if (subdir != null) {
                unregisterFromMemoryCacheRecursive(subdir);
                dirsByName.remove(subdir.getPath());
            }
        }
    }


    //-------------------------------------------
    private String[] lineToStringArray(String line){
        Vector vec=new Vector(10);
        int pos=0;
        int i=-1;
        String item=null;
        while( (i=line.indexOf("|",pos)) >= 0 ){ // NOI18N
            item=line.substring(pos,i);
            vec.addElement(item);
            pos=i+1;
        }
        String []sa=new String[vec.size()];
        for(int j=0;j<vec.size();j++){
            sa[j]=(String) vec.elementAt(j);
        }
        return sa;
    }


    //-------------------------------------------
    private void deleteDirFromDiskCache(String path){
        //D.deb("deleteDirFromDiskCache('"+path+"')"); // NOI18N
        String dirName=cacheDir+ (path.equals("")?"":(File.separator+path)); // NOI18N
        File cacheFile=new File(dirName+File.separator+"list.txt"); // NOI18N
        if( cacheFile.exists() ){
            //D.deb("deleting cacheFile="+cacheFile); // NOI18N
            if( cacheFile.delete()==false ){
                E.err("Failed to delete "+cacheFile); // NOI18N
            }
        }
    }


    /**
    * Read contents of directory from disk cache and register directory into memory cache.
    * @return VcsDir object or null when the directory is not in cache
    */
    //-------------------------------------------
    private VcsDir readDirFromDiskCache(String path){
        D.deb("readDirFromDiskCache('"+path+"')"); // NOI18N

        String dirName=cacheDir+ (path.equals("")?"":(File.separator+path)); // NOI18N
        File cacheFile=new File(dirName+File.separator+"list.txt"); // NOI18N

        D.deb("cacheFile = "+cacheFile+", does"+(cacheFile.exists() ? "" : " not")+" exist");
        if( cacheFile.exists() && cacheFile.canRead() ){
            String name = MiscStuff.getFileNamePart(path);
            VcsDir dir=new VcsDir(name);
            dir.path = path;
            dir.setLoaded(true);
            try{
                BufferedReader in= new BufferedReader
                                   (new InputStreamReader
                                    (new BufferedInputStream
                                     (new FileInputStream(cacheFile))));
                String line=null;
                while( (line=in.readLine())!=null ){
                    String []sa=lineToStringArray(line);
                    //D.deb("sa="+MiscStuff.arrayToString(sa)); // NOI18N
                    VcsFile file=fileSystem.getVcsFactory().getVcsAction (fileSystem).parseFromCache(sa);
                    if(file instanceof VcsDir) {
                        ((VcsDir) file).setPath(path+((path.length() > 0) ? "/" : "")+file.getName());
                    }
                    D.deb("file="+file); // NOI18N
                    if (file.getStatus().equals(localStatusStr)) file.setLocal(true);
                    dir.add(file);
                }
                // D.deb("disk cache hit for '"+path+"'"); // NOI18N
                registerInMemoryCache(path,dir);
                //        readLocalDir (dir, path);
                return dir;
            }catch (IOException e){
                E.err(e,"readDirFromDiskCache() failed"); // NOI18N
                return null ;
            }
        }
        return null ;
    }

    /**
    * To be called when any change on directory occures that results in diferent appearence.
    */
    private void heyDoRefreshDir(String path){
        FileObject fo=fileSystem.findResource(path);
        if( fo!=null ){
            //D.deb("fo.refresh("+path+")"); // NOI18N
            fo.refresh();
        }
    }

    /**
    * To be called when any change on directory occures that results in diferent appearence.
    */
    private void heyDoRefreshDirRecursive(VcsDir dir){
        String[] subdirs = dir.getSubdirs();
        for(int i = 0; i < subdirs.length; i++) {
            VcsDir subdir = dir.getDir(subdirs[i]);
            D.deb("Refreshing '"+subdirs[i]+"'");
            if (subdir != null) {
                heyDoRefreshDirRecursive(subdir);
            }
        }
        FileObject fo=fileSystem.findResource(dir.getPath());
        if( fo!=null ){
            //D.deb("fo.refresh("+path+")"); // NOI18N
            fo.refresh();
        }
    }

    /**
    * Read list of files/directories in the specified directory on local disk
    * and add it to existing contents of the directory. Only files that were not
    * read from VCS are added. Registry each directory added into memory cache.
    */
    //-------------------------------------------
    private void refreshLocalFiles (VcsDir dir, String path){
        if(!localFilesAdd) return;
        // remove local files (they will be refreshed)
        dir.removeLocalFilesAndSubdirs ();
        String[] contents = dir.getFilesAndSubdirs();
        // debug
        File localDir = new File (fileSystem.getRootDirectory (), path);
        if(localDir.exists() && localDir.canRead ()) {
            String[] files = localDir.list (fileSystem.getLocalFileFilter());
            if (files != null) {
                for(int i=0; i<files.length; i++) {
                    if(!dir.hasFile (files [i]) && !dir.hasSubdir (files [i])) {
                        D.deb("Adding local file "+files[i]); // NOI18N
                        VcsFile f;
                        if ((new File(localDir, files[i])).isDirectory()) {
                            f = new VcsDir();
                            String newPath = path.length ()==0 ? files[i] : path + "/" + files[i]; // NOI18N
                            //            System.out.println("dir path:" + newPath); // NOI18N
                            ((VcsDir) f).path = newPath;
                            if (!isDir(newPath)) {
                                D.deb("Dir not in memory cache => REGISTERED");
                                registerInMemoryCache (newPath, (VcsDir) f);
                            }
                        }
                        else f = new VcsFile();
                        f.name = files[i];
                        f.setLocal (true);
                        f.status = localStatusStr;
                        //          System.out.println("localFile/Dir:"+f.name); // NOI18N
                        dir.add (f);
                    }
                }
            }
        }
    }

    private void refreshLocalFilesRecursively(VcsDir dir, String path) {
        refreshLocalFiles(dir, path);
        String[] subdirs = dir.getSubdirs();
        for(int i = 0; i < subdirs.length; i++) {
            VcsDir subdir = dir.getDir(subdirs[i]);
            D.deb("Refreshing '"+subdirs[i]+"'");
            if (subdir != null) {
                refreshLocalFilesRecursively(subdir, subdir.getPath());
            }
        }
    }

    private synchronized void runDirReaderFromQueue() {
        if (pathsQueue.isEmpty()) return;
        String path=(String)pathsQueue.remove(0);
        D.deb(" ++++  runDirReaderFromQueue(): path = "+path); // NOI18N
        VcsDirReader reader= fileSystem.getVcsFactory ().getVcsDirReader (this, path, fileSystem);
        if (reader != null) {
            new Thread (reader, "VCS-DirReader").start(); // NOI18N
            numRefreshThreads++;
            lastCanceled = false;
            D.deb(" ++++  reader started, numRefreshThreads = "+numRefreshThreads); // NOI18N
        } else {
            fileSystem.debug(fileSystem.getBundleProperty("MSG_CommandCanceled")); // NOI18N
            lastCanceled = true;
        }
    }

    //-------------------------------------------
    private void readDir(String path, final boolean recursive){
        D.deb("readDir('"+path+"')"); // NOI18N

        VcsDir dir=getDir(path);

        D.deb("dir = "+dir+", isLoaded() = "+((dir != null) ? new Boolean(dir.isLoaded()).toString() : "XXX"));
        D.deb("\t\t isLocal() = "+((dir != null) ? ""+dir.isLocal() : "XXX"));
        //boolean wasLocal = (dir != null && dir.isLocal());
        // unknown or local files read from vcs disc cache
        if(dir==null || (!dir.isLocal() && !dir.isLoaded())) {
            dir=readDirFromDiskCache(path);
            //if (wasLocal) dir.setLocal(true);
            //dir.setLoaded(true);
        }
        // add local files to dir
        if(dir != null && (dir.isLoaded() || dir.isLocal())){
            D.deb("dir = "+dir+", isLocal = "+dir.isLocal());
            heyDoRefreshDir(path);
            return ;
        }

        dir=new VcsDir();
        dir.path = path;
        dir.name = MiscStuff.getFileNamePart (dir.path);
        registerInMemoryCache(dir.path, dir);
        dir.setBeingLoaded(true);
        heyDoRefreshDir(dir.path);
        dir.setBeingLoaded(false);

        final String fPath = path;
        final VcsCache cache = this;
        if (!(lastCanceled && numRefreshThreads > 0)) {
            D.deb(" ++++  Running Read Dir Thread, path = "+fPath); // NOI18N
            if (!path.equals ("")) pathsQueue.add(path); // NOI18N
            //new Thread(new Runnable () {
            javax.swing.SwingUtilities.invokeLater(new Runnable () {

                                                       public synchronized void run () {
                                                           D.deb("Entering invokeLater"); // NOI18N
                                                           if(recursive || (fPath.equals ("") && fileSystem.getAskIfDownloadRecursively() && // NOI18N
                                                                            NotifyDescriptor.Confirmation.YES_OPTION.equals (
                                                                                TopManager.getDefault ().notify (new NotifyDescriptor.Confirmation (
                                                                                                                     fileSystem.getBundleProperty("DLG_DownloadRecursively"), NotifyDescriptor.Confirmation.YES_NO_OPTION))))) { // NOI18N
                                                               VcsDirReader reader= fileSystem.getVcsFactory ().getVcsDirReaderRecursive (cache, fPath, fileSystem);
                                                               if (reader != null) {
                                                                   new Thread (reader).start();
                                                               } else {
                                                                   fileSystem.getVcsFactory ().getVcsAction (fileSystem).doListSub (fPath);
                                                               }
                                                           } else {
                                                               new Thread(new Runnable () {
                                                                              public void run () {
                                                                                  if (fPath.equals ("")) pathsQueue.add(fPath); // I don't want to access pathQueue in the AWT thread // NOI18N
                                                                              }
                                                                          }).start();
                                                               //D.deb(" ++++  There is "+pathsQueue.size()+" items in the queue."); // NOI18N
                                                               if (numRefreshThreads > 0) {
                                                                   D.deb(" ++++ Leaving for later, numRefreshThreads = "+numRefreshThreads); // NOI18N
                                                               } else {
                                                                   new Thread(new Runnable () {
                                                                                  public void run () {
                                                                                      runDirReaderFromQueue();  // do not run this in AWT thread
                                                                                  }
                                                                              }, "VCS-Run DirReader").start(); // NOI18N
                                                               }
                                                               /*
                                                               D.deb(" ++++  getting the reader");
                                                               VcsDirReader reader= fileSystem.getVcsFactory ().getVcsDirReader (cache, fPath, fileSystem);
                                                               if (reader != null) {
                                                                 new Thread (reader).start();
                                                                 numRefreshThreads++;
                                                                 lastCanceled = false;
                                                                 D.deb(" ++++  reader started, numRefreshThreads = "+numRefreshThreads);
                                                           } else {
                                                                 fileSystem.debug(fileSystem.getBundleProperty("MSG_CommandCanceled"));
                                                                 lastCanceled = true;
                                                           }
                                                               */
                                                           }
                                                           fileSystem.setAskIfDownloadRecursively(true);
                                                           D.deb("Finished invokeLater"); // NOI18N
                                                       }
                                                   });/*, "Read Dir Thread").start();*/ // NOI18N
        } else {
            D.deb(" ++++  Removing all paths queue"); // NOI18N
            pathsQueue.removeAllElements();
        }
    }

    /** Called by VcsDirReader when asynchronous reading of directory is completed.
     * @param dir directory that was read by VcsDirReader
     * @param rawData vector of <CODE>String[]</CODE> that describes files and subdirectories,
     *it is stored in disk cache
     */
    public synchronized void readDirFinished(VcsDir dir,Vector rawData, boolean success) {
        D.deb("readDirFinished(name="+dir.name+",path="+dir.path+",data="+rawData+")"); // NOI18N
        // Unregister subdirectories of the dir. This is necessary if subdirs have been
        // previously loaded from local fs and now they are added from vcs
        // since local sudirsdirs are registered in memory chache
        Enumeration en = dirsByName.keys ();
        String[] subs = dir.getSubdirs ();
        String pathPrefix = dir.path.length() == 0 ? "" : dir.path + "/"; // NOI18N
        /*
        for(int i=0; i<subs.length; i++) {
          //      System.out.println (pathPrefix + subs[i]);
          VcsDir sub = getDir(pathPrefix + subs[i]);
          if (sub!=null && sub.isLocal ()) {
            unregisterFromMemoryCache (pathPrefix + subs[i]);
          }
          //D.deb("readDirFinished: "+sub.getName()+" status = "+sub.getStatus());
    }
        */
        writeDirToDiskCache(dir.path, rawData);
        registerInMemoryCache(dir.path, dir);
        for(int i=0; i<subs.length; i++) {
            VcsDir sub = getDir(pathPrefix + subs[i]);
            //D.deb("readDirFinished: "+sub.getName()+" status = "+sub.getStatus());
        }
        heyDoRefreshDir(dir.path);
        fileSystem.statusChanged (dir.path, false); // perform non-recursive change of status
        if (numRefreshThreads > 0) numRefreshThreads--;
        D.deb(" ++++  numRefreshThreads = "+numRefreshThreads+" after readDirFinished."); // NOI18N
        D.deb(" ++++  Finished with "+success); // NOI18N
        if (!success) pathsQueue.removeAllElements();
        D.deb(" ++++  number of items in the queue = "+pathsQueue.size()); // NOI18N
        if (!pathsQueue.isEmpty()) runDirReaderFromQueue();
    }


    /** Called by VcsDirReader when asynchronous recursive reading of directory is completed.
     * @param dir directory that was read by VcsDirReader
     * @param rawData vector of <CODE>String[]</CODE> that describes files and subdirectories,
     *it is stored in disk cache
     */
    public synchronized void readDirFinishedRecursive(VcsDir dir, VcsDirContainer rawData, boolean success) {
        D.deb("readDirFinished(name="+dir.name+",path="+dir.path+",data="+rawData+")"); // NOI18N
        // Unregister subdirectories of the dir. This is necessary if subdirs have been
        // previously loaded from local fs and now they are added from vcs
        // since local sudirsdirs are registered in memory chache
        Enumeration en = dirsByName.keys ();
        String[] subs = dir.getSubdirs ();
        String pathPrefix = (dir.path.length() == 0) ? "" : dir.path + "/"; // NOI18N
        unregisterFromMemoryCacheRecursive(dir);
        writeDirToDiskCacheRecursive(dir, rawData);
        registerInMemoryCacheRecursive(dir.path, dir);
        for(int i=0; i<subs.length; i++) {
            VcsDir sub = getDir(pathPrefix + subs[i]);
            //D.deb("readDirFinished: "+sub.getName()+" status = "+sub.getStatus());
        }
        heyDoRefreshDirRecursive(dir);
        fileSystem.statusChanged (dir.path, true); // perform recursive change of status
        /*
        if (numRefreshThreads > 0) numRefreshThreads--;
        D.deb(" ++++  numRefreshThreads = "+numRefreshThreads+" after readDirFinished."); // NOI18N
        D.deb(" ++++  Finished with "+success); // NOI18N
        if (!success) pathsQueue.removeAllElements();
        D.deb(" ++++  number of items in the queue = "+pathsQueue.size()); // NOI18N
        if (!pathsQueue.isEmpty()) runDirReaderFromQueue();
        */
    }


    /** Force refresh of directory. Delete the directory from cache
     * and read it.
     * @param path complete name of directory to refresh
     */
    public void refreshDir(String path){
        D.deb("refreshDir("+path+")"); // NOI18N
        // delete only files from vcs (not local)
        VcsDir dir = getDir(path);

        // true for all vcs dir (not local)
        // dir==null means it is vcs dir that is not in memory cache
        // !dir.local means it is vcs dir that is in memory cache
        if(dir==null || !dir.isLocal ()) {
            deleteDirFromDiskCache(path);
            if (dir != null) dir.setLoaded(false);
        }
        readDir(path, false);
    }

    /** Force recursive refresh of directory. Delete the directory from cache
     * and read it.
     * @param path complete name of directory to refresh
     */
    public void refreshDirRecursive(String path){
        D.deb("refreshDirRecursive("+path+")"); // NOI18N
        // delete only files from vcs (not local)
        VcsDir dir = getDir(path);

        // true for all vcs dir (not local)
        // dir==null means it is vcs dir that is not in memory cache
        // !dir.local means it is vcs dir that is in memory cache
        if(dir==null || !dir.isLocal ()) {
            deleteDirFromDiskCache(path);
            if (dir != null) dir.setLoaded(false);
        }
        readDir(path, true);
    }


    //-------------------------------------------
    /** This method should be called by file system to obtain complete
     *list of files and subdirectories of a directory. List of local files
     *and dirs is refreshed.
     * @param path path of directory
     * @return merged list of VCS and local files
     */
    public String[] getFilesAndSubdirs(String path) {
        D.deb("getFilesAndSubdirs('"+path+"')"); // NOI18N

        VcsDir dir=(VcsDir)dirsByName.get(path);
        if(dir != null && (dir.isLoaded() || dir.isBeingLoaded() || dir.isLocal())) {
            D.deb("memory cache hit for '"+path+"'"); // NOI18N
            // refresh local subdirs and files
            refreshLocalFiles (dir, path);
            return dir.getFilesAndSubdirs();
        }

        dir=readDirFromDiskCache(path);
        if (dir != null && (dir.isLoaded() || dir.isBeingLoaded() || dir.isLocal())) {
            D.deb("disk cache hit for '"+path+"'"); // NOI18N
            refreshLocalFiles (dir, path);
            return dir.getFilesAndSubdirs();
        }
        D.deb("Did not found in memory nor in disk cache, calling readDir("+path+")");
        readDir (path, false);
        return new String[0];
    }

    /** This method should be called by file system to obtain complete
     * list of files and subdirectories of a directory. List only local files
     * and dirs. No Version Control command is run.
     * @param path path of directory
     * @return merged list of VCS and local files
     */
    public String[] getLocalFilesAndSubdirs(String path) {
        D.deb("getLocalFilesAndSubdirs('"+path+"')"); // NOI18N

        VcsDir dir=(VcsDir)dirsByName.get(path);
        if (dir != null) {
            D.deb("memory cache hit for '"+path+"'"); // NOI18N
            // refresh local subdirs and files
            refreshLocalFiles (dir, path);
            return dir.getFilesAndSubdirs();
        }
        dir=new VcsDir();
        dir.path = path;
        dir.name = MiscStuff.getFileNamePart (dir.path);
        registerInMemoryCache(dir.path, dir);
        heyDoRefreshDir(dir.path);
        //refreshLocalFiles (dir, path);
        return dir.getFilesAndSubdirs();
    }

    //-------------------------------------------
    /** Sorts directories and files so that dirs are first.
    * @param path path of directoru\ty
    * @param files original list of files and subdirs
    * @return sorted list
    */
    public String[] dirsFirst(String path,String[] files) {
        //D.deb("dirsFirst("+path+", "+MiscStuff.arrayToString(files)); // NOI18N
        Vector dirsVec=new Vector(10);
        Vector filesVec=new Vector(20);
        String[] res=new String[files.length];

        for(int i=0;i<files.length;i++){

            String dir=path+files[i];
            if( isDir(dir) || new File(dir).isDirectory() ){
                dirsVec.addElement(files[i]);
            }
            else{
                filesVec.addElement(files[i]);
            }
        }

        int dirsVecLen=dirsVec.size();
        int filesVecLen=filesVec.size();
        int j=0;
        for(int i=0;i<dirsVecLen;i++){
            res[j++]= (String)dirsVec.elementAt(i);
        }
        for(int i=0;i<filesVecLen;i++){
            res[j++]= (String)filesVec.elementAt(i);
        }
        return res;
    }


    //-------------------------------------------
    public String getStatus(Vector/*<VcsFile>*/ importantFiles){
        D.deb("getStatus("+importantFiles+")"); // NOI18N

        String result=g("CTL_StatusNotInSync"); // NOI18N
        String status=""; // NOI18N
        int len=importantFiles.size();
        VcsFile file=null;

        D.deb("len="+len); // NOI18N
        if( len<1 ){
            return g("CTL_StatusUnknown"); // NOI18N
        }
        else if( len==1 ){
            file=(VcsFile)importantFiles.elementAt(0);
            D.deb("one file status="+file.getStatus()); // NOI18N
            return file.getStatus();
        }

        file=(VcsFile)importantFiles.elementAt(0);
        status=file.getStatus();
        D.deb("status="+status); // NOI18N
        for(int i=1;i<len;i++){
            file=(VcsFile)importantFiles.elementAt(i);
            //D.deb("file="+file); // NOI18N
            if( !file.getStatus().equals(status) ){
                return result;
            }
        }
        return status;
    }


    //-------------------------------------------
    public void setFileStatus(String path, String status){
        //D.deb("setFileStatus('"+path+"','"+status+"')"); // NOI18N
        VcsFile file=getFile(path);
        if( file==null ){
            //if (path.length() > 0) E.err("file not found '"+path+"'"); // NOI18N
            // file was not found, status unchanged
            return ;
        }
        file.status=status;
    }


    //-------------------------------------------
    public String getFileStatus(String path){
        //D.deb("getFileStatus('"+path+"')"); // NOI18N

        if( isFile(path) ){
            VcsFile file=getFile(path);
            if( file==null ){
                //E.err("cannot find file '"+path+"'"); // NOI18N
                return g("CTL_StatusNotInView"); // NOI18N
            }
            //D.deb("file.status='"+file.getStatus()+"'"); // NOI18N
            return file.getStatus();
        }

        if( isDir(path) ){
            VcsDir dir=getDir(path);
            if( dir==null ){
                //E.err("cannot find dir '"+path+"'"); // NOI18N
                return g("CTL_StatusNotInView"); // NOI18N
            }
            //D.deb("dir.status='"+dir.getStatus()+"'"); // NOI18N
            return dir.getStatus();
        }

        return g("CTL_StatusNotInView"); // NOI18N
    }

    public String getFileLocker(String path){
        String locker = null;
        if (isFile(path)) {
            VcsFile file = getFile(path);
            if (file != null) locker = file.getLocker();
        }
        return locker;
    }

    public String getLocker(Vector importantFiles) {
        String locker = null;
        int len = importantFiles.size();
        for(int i = 0; i < len; i++) {
            VcsFile file = (VcsFile) importantFiles.elementAt(i);
            String fileLocker = file.getLocker();
            if (locker == null) locker = fileLocker;
            else {
                if (fileLocker != null && fileLocker.length() > 0) {
                    locker += " "+fileLocker;
                }
            }
        }
        return locker;
    }

    //-------------------------------------------
    public VcsDir getDir(String path){
        String dirName=MiscStuff.getDirNamePart(path);
        String fileName=MiscStuff.getFileNamePart(path);

        VcsDir dir=(VcsDir)dirsByName.get(path);
        //D.deb("getDir("+path+") = "+dir);
        if( dir!=null ){
            return dir;
        }

        dir=(VcsDir)dirsByName.get(dirName);
        //D.deb("getDir("+dirName+") = "+dir);
        if( dir!=null ){
            return dir;
        }

        return null ;
    }


    //-------------------------------------------
    public VcsFile getFile(String path){
        String dirName=MiscStuff.getDirNamePart(path);
        String fileName=MiscStuff.getFileNamePart(path);

        VcsDir dir=(VcsDir)dirsByName.get(dirName);
        if( dir==null ){
            return null;
        }
        VcsFile file=dir.getFile(fileName);
        return file;
    }



    //-------------------------------------------
    public boolean isFile(String path){
        //D.deb("isFile('"+path+"')"); // NOI18N
        if( isDir(path) ){
            return false;
        }
        String dirName=MiscStuff.getDirNamePart(path);
        String fileName=MiscStuff.getFileNamePart(path);

        VcsDir dir=(VcsDir)dirsByName.get(dirName);
        if( dir==null ){
            //D.deb("no"); // NOI18N
            return false;
        }
        if( dir.hasFile(fileName) ){
            return true;
        }
        //D.deb("no"); // NOI18N
        return false;
    }


    //-------------------------------------------
    public boolean isDir(String path){
        //D.deb("isDir('"+path+"')"); // NOI18N
        if( path.equals("") ){ // NOI18N
            return true ;
        }
        String dirName=MiscStuff.getDirNamePart(path);
        String fileName=MiscStuff.getFileNamePart(path);

        VcsDir dir=(VcsDir)dirsByName.get(path);
        if( dir!=null ){
            return true  ;
        }
        dir=(VcsDir)dirsByName.get(dirName);
        if( dir!=null ){
            if( dir.hasSubdir(fileName) ){
                return true ;
            }
        }

        //D.deb("no"); // NOI18N
        return false ;
    }



    //-------------------------------------------
    public void addFile(String path){
        //D.deb("addFile('"+path+"')"); // NOI18N
        String dirName=MiscStuff.getDirNamePart(path);
        String fileName=MiscStuff.getFileNamePart(path);

        VcsDir dir=getDir(dirName);
        if (dir == null) {
            String dirNameName = MiscStuff.getFileNamePart(dirName);
            dir = new VcsDir(dirNameName);
            dir.setPath(dirName);
            dirsByName.put(dirName, dir);
        }
        VcsFile file=new VcsFile(fileName);
        dir.add(file);
    }


    //-------------------------------------------
    public void removeFile(String path){
        //D.deb("removeFile('"+path+"')"); // NOI18N
        String dirName=MiscStuff.getDirNamePart(path);
        String fileName=MiscStuff.getFileNamePart(path);

        VcsDir dir = getDir(dirName);
        VcsFile file = getFile(path);
        if (dir != null) dir.remove(file);
    }

    //-------------------------------------------
    String g(String s) {
        //D.deb("getting "+s);
        return NbBundle.getBundle
               ("org.netbeans.modules.vcs.cmdline.Bundle").getString (s);
    }
    String  g(String s, Object obj) {
        return MessageFormat.format (g(s), new Object[] { obj });
    }
    String g(String s, Object obj1, Object obj2) {
        return MessageFormat.format (g(s), new Object[] { obj1, obj2 });
    }
    String g(String s, Object obj1, Object obj2, Object obj3) {
        return MessageFormat.format (g(s), new Object[] { obj1, obj2, obj3 });
    }

}


/*
 * $Log: 
 *  24   Gandalf-post-FCS1.18.1.4    04/04/00 Martin Entlicher 
 *  23   Gandalf-post-FCS1.18.1.3    04/04/00 Martin Entlicher Proper localized
 *       output to the disk cache.
 *  22   Gandalf-post-FCS1.18.1.2    04/04/00 Martin Entlicher Small bug fix.
 * 
 *  21   Gandalf-post-FCS1.18.1.1    03/30/00 Martin Entlicher Memory cache
 *       changes.
 *  20   Gandalf-post-FCS1.18.1.0    03/23/00 Martin Entlicher Support for
 *       recursive refresh by one command added, new folder denoting as local, add
 *       new folders to cache only in case they are not there
 *       (registerInMemoryCache()), added get of only local files
 *       (getLocalFilesAndSubdirs())
 *  19   Gandalf   1.18        03/09/00 Martin Entlicher Properly handle local
 *       directories.
 *  18   Gandalf   1.17        02/11/00 Martin Entlicher 
 *  17   Gandalf   1.16        02/10/00 Martin Entlicher Added statuses for
 *       directories. Locking enabled.
 *  16   Gandalf   1.15        02/09/00 Martin Entlicher Fix when reading local
 *       files from non-existent directory.
 *  15   Gandalf   1.14        02/09/00 Martin Entlicher 
 *  14   Gandalf   1.13        01/19/00 Martin Entlicher 
 *  13   Gandalf   1.12        01/17/00 Martin Entlicher Internationalization added
 *  12   Gandalf   1.11        01/15/00 Ian Formanek    NOI18N
 *  11   Gandalf   1.10        01/07/00 Martin Entlicher Localization check
 *  10   Gandalf   1.9         12/28/99 Martin Entlicher Collect refresh requests
 *       in the queue and deleting the queue when one command fails.
 *  9    Gandalf   1.8         12/21/99 Martin Entlicher 
 *  8    Gandalf   1.7         12/16/99 Martin Entlicher 
 *  7    Gandalf   1.6         12/01/99 Martin Entlicher Yuri Kamen's improvement
 *       added
 *  6    Gandalf   1.5         11/09/99 Martin Entlicher 
 *  5    Gandalf   1.4         11/09/99 Martin Entlicher 
 *  4    Gandalf   1.3         10/26/99 Martin Entlicher 
 *  3    Gandalf   1.2         10/25/99 Pavel Buzek     copyright and log
 *  2    Gandalf   1.1         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  1    Gandalf   1.0         09/30/99 Pavel Buzek     
 * $
 */
