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
import org.openide.TopManager;
import org.openide.execution.*;
import org.openide.filesystems.*;
import org.openide.util.*;
import javax.sound.sampled.*;
public class InternalPlayer extends Executor {
    private static final long serialVersionUID =-3129235161777547136L;
    public ExecutorTask execute (final ExecInfo info) {
        Runnable run = new Runnable () {
                           public void run () {
                               String resource = info.getClassName ().replace ('.', '/') + '.' + ScoreDataLoader.SECONDARY_EXT;
                               FileObject fo = TopManager.getDefault ().getRepository ().findResource (resource);
                               if (fo == null) {
                                   System.err.println (NbBundle.getBundle (ScoreExecSupport.class).getString ("EXC_No_AU_file_found"));
                                   return;
                               }
                               File f = NbClassPath.toFile (fo);
                               if (f == null) {
                                   System.err.println (NbBundle.getBundle (ScoreExecSupport.class).getString ("MSG_file_must_be_local"));
                                   return;
                               }
                               try {
                                   AudioInputStream ais = AudioSystem.getAudioInputStream (f);
                                   AudioFormat format = ais.getFormat ();
                                   DataLine.Info info = new DataLine.Info (Clip.class, ais.getFormat ());
                                   Clip clip = (Clip) AudioSystem.getLine (info);
                                   clip.open (ais);
                                   clip.start ();
                                   while (clip.isActive ()) {
                                       try {
                                           Thread.sleep (1000);
                                       } catch (InterruptedException ie) {
                                       }
                                   }
                                   clip.stop ();
                                   clip.close ();
                               } catch (Exception e) {
                                   e.printStackTrace ();
                               }
                           }
                       };
        return TopManager.getDefault ().getExecutionEngine ().execute
               (NbBundle.getBundle (ScoreExecSupport.class).getString ("LBL_audio_play_process"),
                run, null);
    }
    public HelpCtx getHelpCtx () {
        return new HelpCtx ("org.netbeans.examples.modules.minicomposer");
    }
}
