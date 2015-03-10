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

/** Directory.
 * 
 * @author Michal Fadljevic
 */
//-------------------------------------------
public class VcsDir extends VcsFile {
    private Debug E=new Debug("VcsDir", false); // NOI18N
    //private Debug D=E;

    /** Vector<VcsDir> 
     * @associates VcsDir*/
    private Vector subdirs=new Vector(10);

    /** Vector<VcsFile> 
     * @associates VcsFile*/
    private Vector files=new Vector(10);

    /** Complete path from root including filename */
    String path = ""; // NOI18N
    private boolean loaded = false;
    private boolean beingLoaded = false;

    //-------------------------------------------
    public VcsDir(){
        super();
    }


    //-------------------------------------------
    public VcsDir(String name){
        super(name);
    }

    public String getPath () { return this.path; }
    public void setPath (String path) { this.path = path; }

    public boolean isLoaded () { return this.loaded; }
    public void setLoaded (boolean loaded) { this.loaded = loaded; }
    public boolean isBeingLoaded () { return this.beingLoaded; }
    public void setBeingLoaded (boolean beingLoaded) { this.beingLoaded = beingLoaded; }

    public void setLoadedRecursive (boolean loaded) {
        this.loaded = loaded;
        Enumeration enum = subdirs.elements();
        while(enum.hasMoreElements()) {
            ((VcsDir) enum.nextElement()).setLoadedRecursive(loaded);
        }
    }

    //-------------------------------------------
    public void add(VcsFile fileOrDir){
        if (fileOrDir == null) return;
        if( fileOrDir instanceof VcsDir ){
            addSubdir( (VcsDir)fileOrDir );
        }
        else if( fileOrDir instanceof VcsFile ){
            addFile( fileOrDir );
        }
        else {
            E.err("invalid parameter fileOrDir="+fileOrDir+ " ignored "); // NOI18N
        }
    }

    //-------------------------------------------
    public void remove(VcsFile fileOrDir){
        if (fileOrDir == null) return;
        if( fileOrDir instanceof VcsDir ){
            removeSubdir( (VcsDir)fileOrDir );
        }
        else if( fileOrDir instanceof VcsFile ){
            removeFile( fileOrDir );
        }
        else {
            E.err("invalid parameter fileOrDir="+fileOrDir+ " ignored "); // NOI18N
        }
    }


    //-------------------------------------------
    public void removeFile(VcsFile file){
        if( files.remove(file)==false  ){
            E.err("Cannot removeFile("+file+"). It is not in this directory "+this); // NOI18N
        }
    }


    //-------------------------------------------
    public void removeSubdir(VcsDir dir){
        if( subdirs.remove(dir)==false  ){
            E.err("Cannot removeSubdir("+dir+"). It is not in this directory "+this); // NOI18N
        }
    }


    //-------------------------------------------
    public void addFile(VcsFile file){
        files.addElement(file);
    }


    //-------------------------------------------
    public void addSubdir(VcsDir dir){
        subdirs.addElement(dir);
    }


    //-------------------------------------------
    public boolean hasFile(String name){
        //D.deb("hasFile('"+name+"')"); // NOI18N
        for(int i=0;i<files.size();i++){
            VcsFile file=(VcsFile)files.elementAt(i);
            if( file.name.equals(name) ){
                return true ;
            }
        }
        return false ;
    }


    //-------------------------------------------
    public boolean hasSubdir(String name){
        //D.deb("hasSubdir('"+name+"')"); // NOI18N
        for(int i=0;i<subdirs.size();i++){
            VcsDir dir=(VcsDir)subdirs.elementAt(i);
            if( dir.name.equals(name) ){
                return true ;
            }
        }
        return false ;
    }


    //-------------------------------------------
    public String[] getSubdirs(){
        int subdirsLen=subdirs.size();
        String[] res=new String[subdirsLen];
        for(int i=0;i<subdirsLen;i++){
            VcsDir d=(VcsDir)subdirs.elementAt(i);
            res[i]=d.name;
        }
        return res;
    }


    //-------------------------------------------
    public String[] getFilesAndSubdirs(){
        int subdirsLen=subdirs.size();
        int filesLen=files.size();
        int len=subdirsLen+filesLen;
        String[] res=new String[len];
        int r=0;

        for(int i=0;i<subdirsLen;i++){
            VcsDir d=(VcsDir)subdirs.elementAt(i);
            //D.deb("d="+d.name); // NOI18N
            res[r++]=d.name;
        }

        for(int i=0;i<filesLen;i++){
            VcsFile f=(VcsFile)files.elementAt(i);
            //D.deb("f="+f.name); // NOI18N
            res[r++]=f.name;
        }

        //D.deb("res="+MiscStuff.arrayToString(res)); // NOI18N
        return res;
    }


    //-------------------------------------------
    public VcsFile getFile(String name){
        VcsFile f=null;
        for(int i=0;i<files.size();i++){
            f=(VcsFile)files.elementAt(i);
            if( f.name.equals(name) ){
                return f;
            }
        }
        return null;
    }

    //-------------------------------------------
    public VcsDir getDir(String name){
        VcsDir f=null;
        for(int i=0;i<subdirs.size();i++){
            f=(VcsDir)subdirs.elementAt(i);
            if( f.name.equals(name) ){
                return f;
            }
        }
        return null;
    }


    //-------------------------------------------
    public String toString(){
        return "VcsDir[name='"+name+"',status="+getStatus()+ // NOI18N
               " files="+files.size()+", subdir="+subdirs.size()+" isLocal = "+isLocal()+" isLoaded = "+isLoaded()+"]"; // NOI18N
    }

    public void removeLocalFilesAndSubdirs () {
        for(int i = 0; i < subdirs.size ();) {
            if(((VcsDir) subdirs.get (i)).isLocal ()) {
                subdirs.remove (i);
            } else {
                i++;
            }
        }
        for(int i = 0; i < files.size ();) {
            if(((VcsFile) files.get (i)).isLocal ()) {
                files.remove (i);
            } else {
                i++;
            }
        }
    }
    
    public void rename(String newPath) {
        String name = MiscStuff.getFileNamePart(newPath);
        this.setName(name);
        setPath(newPath);
        for(int i = 0; i < subdirs.size(); i++) {
            VcsDir subdir = (VcsDir) subdirs.get(i);
            subdir.rename(newPath+"/"+subdir.getName());
        }
    }
}

/*
 * Log
 *  9    Gandalf-post-FCS1.5.2.2     4/4/00   Martin Entlicher 
 *  8    Gandalf-post-FCS1.5.2.1     3/30/00  Martin Entlicher Code formatting changed.
 *  7    Gandalf-post-FCS1.5.2.0     3/23/00  Martin Entlicher Support for recursive 
 *       refresh.
 *  6    Gandalf   1.5         2/10/00  Martin Entlicher 
 *  5    Gandalf   1.4         1/15/00  Ian Formanek    NOI18N
 *  4    Gandalf   1.3         1/6/00   Martin Entlicher 
 *  3    Gandalf   1.2         10/25/99 Pavel Buzek     copyright and log
 *  2    Gandalf   1.1         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  1    Gandalf   1.0         9/30/99  Pavel Buzek     
 * $
 */
