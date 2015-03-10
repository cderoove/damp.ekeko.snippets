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

package org.netbeans.modules.apisupport;

import java.beans.*;
import java.io.*;
import java.util.*;
import java.util.jar.Manifest;
import javax.swing.SwingUtilities;
import javax.swing.event.*;
import javax.swing.text.*;

import org.openide.actions.*;
import org.openide.cookies.*;
import org.openide.execution.*;
import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.openide.nodes.*;
import org.openide.text.EditorSupport;
import org.openide.text.NbDocument;
import org.openide.util.HelpCtx;
import org.openide.util.WeakListener;

// [PENDING] for some reason, reload dialog appears frequently from these files
// though there is no code which is actually writing to the file except via EditorSupport's
// own Document!

public class ManifestDataObject extends MultiDataObject {

    private ChangeListener chglist;
    private DocumentListener doclist;

    private static final long serialVersionUID =5300582282070962055L;
    public ManifestDataObject(FileObject pf, ManifestDataLoader loader) throws DataObjectExistsException {
        super (pf, loader);
        init ();
    }

    /*
    public Node.Cookie getCookie (Class clazz) {
      Node.Cookie cookie = super.getCookie (clazz);
      System.err.println("ManifestDataObject.getCookie: " + clazz + " -> " + cookie);
      return cookie;
}
    */

    private void init () {
        CookieSet cookies = getCookieSet ();
        cookies.add (new ManifestProvider.ModuleExecSupport (getPrimaryEntry ()));
        final ManifestProviderSupport supp = new ManifestProviderSupport ();
        cookies.add (supp);
        final EditorSupport es = new EditorSupport (getPrimaryEntry ());
        //es.setMIMEType ("text/plain");
        cookies.add (es);
        es.addChangeListener (WeakListener.change (chglist = new ChangeListener () {
                                  public void stateChanged (ChangeEvent ev) {
                                      //System.err.println("ManifestDataObject.es.stateChanged");
                                      new Thread (new Runnable () {
                                                      public void run () {
                                                          Document doc = es.getDocument ();
                                                          if (doc == null) {
                                                              //System.err.println("\t(no doc)");
                                                              supp.fireStateChange ();
                                                              return;
                                                          }
                                                          //System.err.println("\t(got doc)");
                                                          doc.addDocumentListener (WeakListener.document (doclist = new DocumentListener () {
                                                                                       public void insertUpdate (DocumentEvent ev) {
                                                                                           //System.err.println("ManifestDataObject.doc.insertUpdate");
                                                                                           update ();
                                                                                       }
                                                                                       public void removeUpdate (DocumentEvent ev) {
                                                                                           //System.err.println("ManifestDataObject.doc.removeUpdate");
                                                                                           update ();
                                                                                       }
                                                                                       public void changedUpdate (DocumentEvent ev) {
                                                                                       }
                                                                                       private void update () {
                                                                                           supp.fireStateChange ();
                                                                                       }
                                                                                   }, doc));
                                                      }
                                                  }, "update doc listener for ManifestDataObject").start ();
                                  }
                              }, es));
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx ("org.netbeans.modules.apisupport.modules");
    }

    protected Node createNodeDelegate () {
        return new ManifestDataNode (this);
    }

    private class ManifestProviderSupport implements ManifestProvider {
        /**
         * @associates ChangeListener 
         */
        private Set listeners = new HashSet (); // Set<ChangeListener>

        private Exception parseException = null;
        private boolean inited = false;

        // [PENDING] UTF8 encoding is untested, JRE does not yet support it anyway

        public synchronized Manifest getManifest () throws IOException {
            final StyledDocument doc = ((EditorCookie) getCookie (EditorCookie.class)).getDocument ();
            InputStream is;
            if (doc != null) {
                //System.err.println("ManifestDataObject.supp.getManifest; doc found");
                final String[] text = new String[] { null };
                try {
                    final BadLocationException[] ble = new BadLocationException[] { null };
                    // [PENDING] could just use render() here
                    NbDocument.runAtomicAsUser (doc, new Runnable () {
                                                    public void run () {
                                                        try {
                                                            text[0] = doc.getText (0, doc.getLength ());
                                                        } catch (BadLocationException ble2) {
                                                            ble[0] = ble2;
                                                        }
                                                    }
                                                });
                    if (ble[0] != null) throw ble[0];
                } catch (BadLocationException ble3) {
                    ble3.printStackTrace ();
                    throw new IOException (ble3.toString ());
                }
                is = new ByteArrayInputStream (text[0].getBytes ("UTF8"));
            } else {
                //System.err.println("ManifestDataObject.supp.getManifest; doc not found");
                is = getPrimaryFile ().getInputStream ();
            }
            try {
                Manifest mani = new Manifest (is);
                parseException = null;
                return mani;
            } catch (IOException ioe) {
                parseException = ioe;
                return new Manifest ();
            } finally {
                is.close ();
                inited = true;
            }
        }

        public synchronized void setManifest (final Manifest m) throws IOException {
            // [PENDING] JRE bug workaround
            if (m.getMainAttributes ().getValue ("Manifest-Version") == null) {
                m.getMainAttributes ().putValue ("Manifest-Version", "1.0");
            }
            final StyledDocument doc = ((EditorCookie) getCookie (EditorCookie.class)).openDocument ();
            try {
                final BadLocationException[] ble = new BadLocationException[] { null };
                final IOException[] ioe = new IOException[] { null };
                NbDocument.runAtomicAsUser (doc, new Runnable () {
                                                public void run () {
                                                    try {
                                                        doc.remove (0, doc.getLength ());
                                                        ByteArrayOutputStream baos = new ByteArrayOutputStream ();
                                                        try {
                                                            m.write (new StripCarriagesStream (baos));
                                                        } finally {
                                                            baos.close ();
                                                        }
                                                        doc.insertString (0, baos.toString ("UTF8"), null);
                                                    } catch (BadLocationException ble2) {
                                                        ble[0] = ble2;
                                                    } catch (IOException ioe2) {
                                                        ioe[0] = ioe2;
                                                    }
                                                }
                                            });
                if (ble[0] != null) throw ble[0];
                if (ioe[0] != null) throw ioe[0];
            } catch (BadLocationException ble3) {
                ble3.printStackTrace ();
                throw new IOException (ble3.toString ());
            }
            // Note: file is not saved at this point, that is up to the user.
            parseException = null;
            inited = true;
        }

        public void addFiles (Set files) throws IOException {
        }

        public void removeFiles (Set files) throws IOException {
        }

        public Set getFiles () throws IOException {
            return Collections.EMPTY_SET;
        }

        public void addChangeListener (ChangeListener list) {
            synchronized (listeners) {
                listeners.add (list);
            }
        }

        public void removeChangeListener (ChangeListener list) {
            synchronized (listeners) {
                listeners.remove (list);
            }
        }

        void fireStateChange () {
            SwingUtilities.invokeLater (new Runnable () {
                                            public void run () {
                                                //System.err.println("ManifestDataObject.supp.fireStateChange");
                                                ChangeEvent ev = new ChangeEvent (this);
                                                Set _listeners;
                                                synchronized (listeners) {
                                                    _listeners = new HashSet (listeners);
                                                }
                                                Iterator it = _listeners.iterator ();
                                                while (it.hasNext ())
                                                    ((ChangeListener) it.next ()).stateChanged (ev);
                                            }
                                        });
        }

        private synchronized void init () {
            try {
                getManifest ();
            } catch (IOException ioe) {
                // OK to ignore here
            }
        }

        public boolean isValid () {
            init ();
            return parseException == null;
        }

        public Exception getParseException () {
            return parseException;
        }

        public File getManifestAsFile() {
            return NbClassPath.toFile (getPrimaryFile ());
        }

    }

    private static class StripCarriagesStream extends FilterOutputStream {

        // [PENDING] will break with UTF8

        public StripCarriagesStream (OutputStream os) {
            super (os);
        }

        public void write (int b) throws IOException {
            if (b != '\r') super.write (b);
        }

        public void write (byte[] b) throws IOException {
            write (b, 0, b.length);
        }

        public void write (byte[] b, int off, int len) throws IOException {
            for (int i = off; i < off + len; i++)
                if (b[i] != '\r')
                    super.write (b[i]);
        }

    }

}

/*
 * Log
 *  6    Gandalf-post-FCS1.3.1.1     4/16/00  Jesse Glick     Hopefully avoiding a 
 *       deadlock after reloading a manifest file, and some other 
 *       threading-related stuff.
 *  5    Gandalf-post-FCS1.3.1.0     3/28/00  Jesse Glick     More robust module 
 *       install executor.
 *  4    Gandalf   1.3         2/4/00   Jesse Glick     
 *  3    Gandalf   1.2         1/26/00  Jesse Glick     Live manifest parsing.
 *  2    Gandalf   1.1         1/26/00  Jesse Glick     Manifest handling 
 *       changed--now more dynamic, synched properly with open document as for 
 *       real file types.
 *  1    Gandalf   1.0         1/22/00  Jesse Glick     
 * $
 */
