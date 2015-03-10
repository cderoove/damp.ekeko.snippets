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

/** File.
 * 
 * @author Michal Fadljevic
 */
//-------------------------------------------
public class VcsFile {
    private Debug E=new Debug("VcsFile", false); // NOI18N
    private Debug D=E;

    String name=""; // NOI18N
    int size=0;
    String date=""; // NOI18N
    String time=""; // NOI18N
    String status=""; // NOI18N
    String locker=""; // NOI18N
    String attr=""; // NOI18N
    // true if the file is not in VCS
    private boolean local = false;

    //private boolean important=true; -- not used

    //-------------------------------------------
    public VcsFile(){
    }

    //-------------------------------------------
    public VcsFile(String name){
        this.name=name;
    }

    //-------------------------------------------
    public VcsFile(String name, boolean local){
        this.name=name;
        this.local=local;
    }

    //-------------------------------------------
    public void setLocal (boolean local) {
        this.local=local;
    }
    //-------------------------------------------
    public boolean isLocal () {
        return local;
    }

    //-------------------------------------------
    public String getStatus(){
        return this.status; //+" "+locker; //+" "+date+" "+time+" "+size+" "+attr; // NOI18N
    }
    public void setStatus (String status) {this.status = status; }

    public String getName () { return this.name; }
    public void setName (String name) { this.name = name; }

    public String getDate () { return this.date; }
    public void setDate (String date) {this.date = date; }

    public String getTime () { return this.time; }
    public void setTime (String time) {this.time = time; }

    public String getLocker () { return this.locker; }
    public void setLocker (String locker) {this.locker = locker; }

    public String getAttr () { return this.attr; }
    public void setAttr (String attr) {this.attr = attr; }

    public int getSize () { return this.size; }
    public void setSize (int size) {this.size = size; }

    //-------------------------------------------
    //public void setImportant(boolean important){
    //  this.important=important;
    //}

    //-------------------------------------------
    //public boolean isImportant(){
    //  return important;
    //}

    //-------------------------------------------
    public String toString(){
        return "VcsFile["+ // NOI18N
               "name='"+name+"'"+ // NOI18N
               ",status="+status+ // NOI18N
               ",locker="+locker+ // NOI18N
               ",attr="+attr+ // NOI18N
               ",size="+size+ // NOI18N
               ",date="+date+ // NOI18N
               ",time="+time+ // NOI18N
               ",local="+local+ // NOI18N
               "]"; // NOI18N
    }


}

/*
 * Log
 *  7    Gandalf-post-FCS1.5.2.0     3/23/00  Martin Entlicher Removed property 
 *       "important".
 *  6    Gandalf   1.5         2/8/00   Martin Entlicher getStatus() returns only
 *       status instead of status, locker
 *  5    Gandalf   1.4         1/17/00  Martin Entlicher NOI18N  
 *  4    Gandalf   1.3         1/6/00   Martin Entlicher 
 *  3    Gandalf   1.2         10/25/99 Pavel Buzek     copyright and log
 *  2    Gandalf   1.1         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  1    Gandalf   1.0         9/30/99  Pavel Buzek     
 * $
 */
