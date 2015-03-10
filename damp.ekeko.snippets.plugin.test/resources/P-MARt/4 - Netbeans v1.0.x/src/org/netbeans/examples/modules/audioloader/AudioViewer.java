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

package org.netbeans.examples.modules.audioloader;
import java.awt.*;
import java.io.*;
import java.net.MalformedURLException;
import javax.media.*;
import javax.media.bean.playerbean.MediaPlayer;
import org.openide.*;
import org.openide.execution.NbClassPath;
import org.openide.filesystems.FileObject;
import org.openide.text.EditorSupport;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.windows.*;
public class AudioViewer extends CloneableTopComponent {
    private AudioDataObject obj;
    private MediaPlayer player;
    private static final long serialVersionUID =-4782743582015763601L;
    public AudioViewer () {
    }
    public AudioViewer (AudioDataObject obj) {
        super (obj);
        init (obj);
    }
    private void init (AudioDataObject obj) {
        this.obj = obj;
        setLayout (new BorderLayout ());
        player = new MediaPlayer ();
        try {
            FileObject fo = obj.getPrimaryFile ();
            // For some reason this does not work (get generic NoPlayerException):
            // FileUtil.setMIMEType ("au", "audio/basic"); // etc.
            // player.setMediaLocation (NbfsURLConnection.encodeFileObject (fo).toString ());
            // Note that the NbfsURLConnection provides a proper input stream, content length,
            // content type, etc.--so what fails? Maybe only file: URLs are accepted.
            File f = NbClassPath.toFile (fo);
            if (f == null) {
                TopManager.getDefault ().notify
                (new NotifyDescriptor.Message (NbBundle.getBundle (AudioViewer.class)
                                               .getString ("MSG_local_only")));
                return;
            }
            player.setMediaLocation (f.toURL ().toString ());
        } catch (MalformedURLException mue) {
            mue.printStackTrace ();
        }
        player.addControllerListener (new ControllerAdapter () {
                                          public void realizeComplete (RealizeCompleteEvent ev) {
                                              Component viz = player.getVisualComponent ();
                                              if (viz != null) add (viz, BorderLayout.CENTER);
                                              Component ctrl = player.getControlPanelComponent ();
                                              if (ctrl != null) add (ctrl, BorderLayout.SOUTH);
                                          }
                                      });
    }
    public boolean canClose (Workspace ws, boolean last) {
        player.stopAndDeallocate ();
        return super.canClose (ws, last);
    }
    public HelpCtx getHelpCtx () {
        return new HelpCtx ("org.netbeans.examples.modules.audioloader");
    }
    protected CloneableTopComponent createClonedObject () {
        return new AudioViewer (obj);
    }
    public void open (Workspace ws) {
        super.open (ws);
        if (ws == null) ws = TopManager.getDefault ().getWindowManager ().getCurrentWorkspace ();
        Mode m = ws.findMode (EditorSupport.EDITOR_MODE);
        if (m != null)
            m.dockInto (this);
        player.start ();
    }
    /**
     * @serialData Super, then store the AudioDataObject. */
    public void writeExternal(ObjectOutput oo) throws IOException {
        super.writeExternal (oo);
        oo.writeObject (obj);
    }
    /**
     * @serialData #see writeExternal */
    public void readExternal(ObjectInput oi) throws IOException, ClassNotFoundException {
        super.readExternal (oi);
        init ((AudioDataObject) oi.readObject ());
    }
}
