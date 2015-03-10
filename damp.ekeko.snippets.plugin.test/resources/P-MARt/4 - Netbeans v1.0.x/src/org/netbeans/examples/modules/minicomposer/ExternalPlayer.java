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

package org.netbeans.examples.modules.minicomposer;
import java.io.File;
import java.io.IOException;
import java.util.*;
import org.openide.TopManager;
import org.openide.execution.*;
import org.openide.filesystems.FileObject;
import org.openide.util.*;
public class ExternalPlayer extends ProcessExecutor {
    private static final NbProcessDescriptor DEFAULT = new NbProcessDescriptor (
                Utilities.isWindows () ?
                "\"C:\\Program Files\\Windows Media Player\\mplayer2.exe\"" :
                "play",
                (Utilities.isWindows () ? "/Play " : "") +
                "{" + MyFormat.TAG_AUFILE + "}",
                NbBundle.getBundle (ExternalPlayer.class).getString ("MSG_format_hint")
            );
    private static final long serialVersionUID =-4397529002559509129L;
    public ExternalPlayer () {
        setExternalExecutor (DEFAULT);
    }
    protected Process createProcess (ExecInfo info) throws IOException {
        String resource = info.getClassName ().replace ('.', '/') + '.' + ScoreDataLoader.SECONDARY_EXT;
        FileObject fo = TopManager.getDefault ().getRepository ().findResource (resource);
        if (fo == null) {
            throw new IOException ("no-au-file") {
                public String getLocalizedMessage () {
                    return NbBundle.getBundle (ScoreExecSupport.class).getString ("EXC_No_AU_file_found");
                }
            };
        }
        File aufile = NbClassPath.toFile (fo);
        if (aufile == null) {
            throw new IOException ("must-be-local") {
                public String getLocalizedMessage () {
                    return NbBundle.getBundle (ScoreExecSupport.class).getString ("MSG_file_must_be_local");
                }
            };
        }
        return getExternalExecutor ().exec (new MyFormat (aufile));
    }
    protected String displayName () {
        try {
            return java.beans.Introspector.getBeanInfo (getClass ()).getBeanDescriptor ().getDisplayName ();
        } catch (Exception e) {
            if (Boolean.getBoolean ("netbeans.debug.exceptions"))
                e.printStackTrace ();
            return getClass ().getName ();
        }
    }
    public HelpCtx getHelpCtx () {
        return new HelpCtx ("org.netbeans.examples.modules.minicomposer");
    }
    static class MyFormat extends MapFormat {
        static final String TAG_AUFILE = "aufile";
        private static final long serialVersionUID =6980703950237286310L;
        MyFormat (File aufile) {
            super (new HashMap (1));
            getMap ().put (TAG_AUFILE, aufile.getAbsolutePath ());
        }
    }
}
