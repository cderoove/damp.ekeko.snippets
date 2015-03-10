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
import org.openide.actions.*;
import org.openide.cookies.*;
import org.openide.filesystems.FileObject;
import org.openide.loaders.*;
import org.openide.nodes.*;
import org.openide.util.HelpCtx;
import org.openide.windows.CloneableTopComponent;
public class AudioDataObject extends MultiDataObject {
    private static final long serialVersionUID =-8566202829189670438L;
    public AudioDataObject (FileObject pf, AudioDataLoader loader) throws DataObjectExistsException {
        super (pf, loader);
        getCookieSet ().add (new ViewAudioSupport (this));
        /*
        cookies.add (new ExecSupport (getPrimaryEntry ()));
        */
    }
    public HelpCtx getHelpCtx () {
        return new HelpCtx ("org.netbeans.examples.modules.audioloader");
    }
    protected Node createNodeDelegate () {
        return new AudioDataNode (this);
    }
    static class ViewAudioSupport extends OpenSupport implements ViewCookie {
        private AudioDataObject obj;
        public ViewAudioSupport (AudioDataObject obj) {
            super (obj.getPrimaryEntry ());
            this.obj = obj;
        }
        protected CloneableTopComponent createCloneableTopComponent () {
            return new AudioViewer (obj);
        }
    }
}
