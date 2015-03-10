/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.vcs.advanced;
import java.io.IOException;
import java.io.PrintStream;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;
import java.beans.*;
import java.text.*;

import org.openide.modules.*;
import org.openide.*;
import org.openide.filesystems.*;
import org.openide.util.*;

import org.netbeans.modules.vcs.util.*;

/** Module installer.
 * 
 * @author Michal Fadljevic
 */
//-------------------------------------------
public class CommandLineVcsModule extends ModuleInstall {
    private Debug E=new Debug("CommandLineVcsModule", false); // NOI18N
    private Debug D=E;


    //-------------------------------------------
    static final long serialVersionUID =-4711519737557272213L;
    public CommandLineVcsModule(){
    }


    //-------------------------------------------
    public void installed() {
        //D.deb("installed()"); // NOI18N
        // Called when the module is first installed into the IDE.
        copyConfigurationFiles();
    }

    //-------------------------------------------
    public void uninstalled() {
        D.deb("uninstalled()"); // NOI18N
        // Called when the module is uninstalled (from a running IDE).
    }

    //-------------------------------------------
    public void restored() {
        //D.deb("restored()"); // NOI18N
        // Called when an already-installed module is restored (at IDE startup time).
    }

    //-------------------------------------------
    public boolean closing() {
        //D.deb("closing()"); // NOI18N
        // Called when the IDE is about to exit.
        return true ;
    }


    //-------------------------------------------
    private void deleteFile(FileSystem fileSystem, String path){
        //D.deb("deleteFile("+path+")"); // NOI18N
        FileObject file=fileSystem.findResource(path);
        if( file==null ){
            //D.deb("No such file '"+path+"'"); // NOI18N
            return ;
        }

        try{
            FileLock lock=file.lock();
            file.delete(lock);
        }catch (IOException ex){
            E.err(ex,g("EXC_Failed_to_remove_file",path) ); // NOI18N
        }
    }


    //-------------------------------------------
    private void copyConfigurationFiles(){
        try {
            org.openide.filesystems.FileUtil.extractJar (
                TopManager.getDefault ().getRepository ().getDefaultFileSystem ().getRoot (),
                getClass ().getClassLoader ().getResourceAsStream ("org/netbeans/modules/vcs/advanced/config/vcs.jar") // NOI18N
            );
        } catch (java.io.IOException e) {
            org.openide.TopManager.getDefault ().notifyException (e);
        }
    }


    //-------------------------------------------
    String g(String s) {
        return NbBundle.getBundle
               ("org.netbeans.modules.vcs.advanced.Bundle").getString (s);
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
    //-------------------------------------------

}

/*
 * <<Log>>
 *  19   Gandalf   1.18        1/27/00  Martin Entlicher NOI18N
 *  18   Gandalf   1.17        11/27/99 Patrik Knakal   
 *  17   Gandalf   1.16        10/25/99 Pavel Buzek     copyright
 *  16   Gandalf   1.15        10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  15   Gandalf   1.14        10/1/99  Petr Hrebejk    org.openide.modules.ModuleInstall
 *        changed to class + some methods added
 *  14   Gandalf   1.13        9/30/99  Pavel Buzek     
 *  13   Gandalf   1.12        8/31/99  Pavel Buzek     
 *  12   Gandalf   1.11        8/2/99   Ian Formanek    Removed obsoleted method
 *  11   Gandalf   1.10        7/29/99  Ian Formanek    Improved installing 
 *       config files
 *  10   Gandalf   1.9         7/9/99   Michal Fadljevic 
 *  9    Gandalf   1.8         6/10/99  Michal Fadljevic cvsunix, cvswin 
 *       configurations added  
 *  8    Gandalf   1.7         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  7    Gandalf   1.6         6/4/99   Michal Fadljevic 
 *  6    Gandalf   1.5         6/1/99   Michal Fadljevic 
 *  5    Gandalf   1.4         5/27/99  Michal Fadljevic 
 *  4    Gandalf   1.3         5/25/99  Michal Fadljevic 
 *  3    Gandalf   1.2         5/21/99  Michal Fadljevic 
 *  2    Gandalf   1.1         5/20/99  Michal Fadljevic 
 *  1    Gandalf   1.0         5/20/99  Michal Fadljevic 
 * $
 */
