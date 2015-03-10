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
import java.util.Hashtable;
import org.netbeans.modules.vcs.*;
import org.netbeans.modules.vcs.cmdline.*;
/**
 *
 * @author  Pavel Buzek
 * @version 
 */
public class CommandLineVcsFactory implements VcsFactory {

    /** Creates new CommandLineVcsFactory */
    public CommandLineVcsFactory() {
    }

    public VcsAdvancedCustomizer getVcsAdvancedCustomizer() {
        return new CommandLineVcsAdvancedCustomizer ();
    }

    public VcsDirReader getVcsDirReader(DirReaderListener listener, String path, VcsFileSystem fileSystem) {
        CommandLineVcsFileSystem fs = (CommandLineVcsFileSystem) fileSystem;
        UserCommand list= fs.getCommand("LIST"); // NOI18N
        Hashtable vars= fs.getVariablesAsHashtable();


        if( !java.io.File.separator.equals ("/") ){ // NOI18N
            String winPath=path.replace('/',java.io.File.separator.charAt(0));
            vars.put("DIR",winPath); // NOI18N
        } else {
            vars.put("DIR",path); // NOI18N
        }

        //System.out.println("Factory.getVcsDirReader (), DIR="+(String) vars.get("DIR")); // NOI18N
        String exec=list.getExec();
        if (!fs.promptForVariables(exec, vars, null)) return null;

        return new CommandLineVcsDirReader(listener,(CommandLineVcsFileSystem) fileSystem,list,vars);
    }

    public VcsDirReader getVcsDirReaderRecursive(DirReaderListener listener, String path, VcsFileSystem fileSystem) {
        CommandLineVcsFileSystem fs = (CommandLineVcsFileSystem) fileSystem;
        UserCommand list= fs.getCommand("LIST_SUB"); // NOI18N
        if (list == null) return null;
        if (list.getExec().trim().length() <= 0) return null;
        Hashtable vars= fs.getVariablesAsHashtable();


        if( !java.io.File.separator.equals ("/") ){ // NOI18N
            String winPath=path.replace('/',java.io.File.separator.charAt(0));
            vars.put("DIR",winPath); // NOI18N
        } else {
            vars.put("DIR",path); // NOI18N
        }

        //System.out.println("Factory.getVcsDirReader (), DIR="+(String) vars.get("DIR")); // NOI18N
        String exec=list.getExec();
        if (!fs.promptForVariables(exec, vars, null)) return null;

        return new CommandLineVcsDirReaderRecursive(listener,(CommandLineVcsFileSystem) fileSystem,list,vars);
    }

    public VcsAction getVcsAction (VcsFileSystem fs) {
        return new CommandLineAction ((CommandLineVcsFileSystem) fs);
    }
}
/*
 * <<Log>>
 *  11   Gandalf-post-FCS1.9.2.0     3/23/00  Martin Entlicher Dir Reader Recursive 
 *       added.
 *  10   Gandalf   1.9         1/27/00  Martin Entlicher NOI18N
 *  9    Gandalf   1.8         12/21/99 Martin Entlicher 
 *  8    Gandalf   1.7         10/25/99 Pavel Buzek     copyright
 *  7    Gandalf   1.6         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  6    Gandalf   1.5         10/13/99 Pavel Buzek     
 *  5    Gandalf   1.4         10/12/99 Pavel Buzek     
 *  4    Gandalf   1.3         10/10/99 Pavel Buzek     
 *  3    Gandalf   1.2         10/5/99  Pavel Buzek     
 *  2    Gandalf   1.1         9/30/99  Pavel Buzek     
 *  1    Gandalf   1.0         9/8/99   Pavel Buzek     
 * $
 */
