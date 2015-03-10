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

package org.netbeans.modules.vcs.cmdline;
import java.util.Hashtable;
import org.netbeans.modules.vcs.*;

/**
 *
 * @author  Pavel Buzek
 * @version 
 */

public class CvsFactory implements VcsFactory {

    /** Creates new CommandLineVcsFactory */
    public CvsFactory() {
    }

    public VcsAdvancedCustomizer getVcsAdvancedCustomizer() {
        return null;
    }

    public VcsDirReader getVcsDirReader(DirReaderListener listener, String path, VcsFileSystem fileSystem) {
        CvsFileSystem fs = (CvsFileSystem) fileSystem;
        UserCommand list= fs.getCommand("LIST"); // NOI18N
        Hashtable vars= fs.getVariablesAsHashtable();

        if( !java.io.File.separator.equals ("/") ){ // NOI18N
            String winPath=path.replace('/',java.io.File.separatorChar);
            vars.put("DIR",winPath); // NOI18N
        } else {
            vars.put("DIR",path); // NOI18N
        }
        vars.put("DIR_S", path); // NOI18N

        String exec=list.getExec();
        if (!fs.promptForVariables(exec, vars, null)) return null;

        return new CommandLineVcsDirReader(listener,(CvsFileSystem) fileSystem,list,vars);
    }

    public VcsDirReader getVcsDirReaderRecursive(DirReaderListener listener, String path, VcsFileSystem fileSystem) {
        CvsFileSystem fs = (CvsFileSystem) fileSystem;
        UserCommand list= fs.getCommand("LIST_SUB"); // NOI18N
        if (list == null) return null;
        Hashtable vars= fs.getVariablesAsHashtable();

        if( !java.io.File.separator.equals ("/") ){ // NOI18N
            String winPath=path.replace('/',java.io.File.separatorChar);
            vars.put("DIR",winPath); // NOI18N
        } else {
            vars.put("DIR",path); // NOI18N
        }
        vars.put("DIR_S", path); // NOI18N

        String exec=list.getExec();
        if (!fs.promptForVariables(exec, vars, null)) return null;

        return new CommandLineVcsDirReaderRecursive(listener,(CvsFileSystem) fileSystem,list,vars);
    }

    public VcsAction getVcsAction (VcsFileSystem fs) {
        return new CvsAction ((CvsFileSystem) fs);
    }
}
/*
 * Log
 *  9    Gandalf-post-FCS1.7.2.0     3/23/00  Martin Entlicher Recursive reader added.
 *  8    Gandalf   1.7         2/8/00   Martin Entlicher 
 *  7    Gandalf   1.6         1/6/00   Martin Entlicher 
 *  6    Gandalf   1.5         12/21/99 Martin Entlicher 
 *  5    Gandalf   1.4         10/25/99 Pavel Buzek     
 *  4    Gandalf   1.3         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  3    Gandalf   1.2         10/13/99 Martin Entlicher 
 *  2    Gandalf   1.1         10/10/99 Pavel Buzek     
 *  1    Gandalf   1.0         9/30/99  Pavel Buzek     
 * $
 */
