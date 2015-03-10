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

package org.netbeans.examples.modules.microed;

import org.openide.filesystems.FileUtil;
import org.openide.options.SystemOption;
import org.openide.util.*;
import java.awt.Font;
import java.util.*;
import javax.swing.JEditorPane;
import javax.swing.text.EditorKit;

/** System option in the Control Panel for micro-editor.
* @author Jesse Glick
* @version Date
*/
public class Settings extends SystemOption {
    private static final ResourceBundle bundle = NbBundle.getBundle (Settings.class);

    static String kitClass = "javax.swing.text.DefaultEditorKit";
    static String[] mimeTypes = new String[] { FileUtil.getMIMEType ("txt"),
                                FileUtil.getMIMEType ("java"),
                                FileUtil.getMIMEType ("html") };
    // Note that both HashMap and EditorKit are serializable, so this is safe:


    /**
     * @associates EditorKit 
     */
    static HashMap saved = new HashMap (); // Map<String,EditorKit>
    static Font font = new Font ("Monospaced", Font.PLAIN, 12);
    static boolean debug = true;

    public static final String PROP_kitClass = "kitClass";
    public static final String PROP_mimeTypes = "mimeTypes";
    public static final String PROP_font = "font";
    // saved is a hidden property

    private static final String myKit = Kit.class.getName ();

    // Turned on when module installed/restored; then setMimeTypes
    // becomes active. Do not try to register/unregister kits just
    // because the system option is being restored--wait until that is
    // done and the module is officially restored.
    private static boolean running = false;

    // Singleton.
    public static final Settings DEFAULT = new Settings ();

    public Settings () {}

    public String displayName () { return bundle.getString ("LABEL_settings"); }

    // Simple bound property.
    public String getKitClass () { return kitClass; }
    public void setKitClass (String kc) {
        String old = kitClass;
        kitClass = kc;
        if (! kc.equals (old))
            firePropertyChange (PROP_kitClass, old, kc);
    }

    // Bound property. When the set of MIME types changes, we actually
    // register or unregister the proper editor kits as needed.
public String[] getMimeTypes () { return (String[]) mimeTypes.clone (); }
    public void setMimeTypes (String[] mt) {
        String[] old = mimeTypes;
        mimeTypes = (String[]) mt.clone ();
        if (running) {
            // Calculate kits to install & uninstall.
            int outer, inner;
            boolean same;
            String compType;
            // Compute delta:
            for (outer = 0; outer < old.length; outer++) {
                compType = old[outer];
                same = false;
                for (inner = 0; inner < mt.length; inner++) {
                    if (compType.equals (mt[inner]))
                        same = true;
                }
                if (! same)
                    uninstallKits (new String[] { compType });
            }
            for (outer = 0; outer < mt.length; outer++) {
                compType = mt[outer];
                same = false;
                for (inner = 0; inner < old.length; inner++) {
                    if (compType.equals (old[inner]))
                        same = true;
                }
                if (! same)
                    installKits (new String[] { compType });
            }
        }
        // This does a recursive compare as is needed for the array:
        if (! Utilities.compareObjects (old, mt))
            firePropertyChange (PROP_mimeTypes, old, mt);
    }

    // Simple bound property.
public Font getFont () { return font; }
    public void setFont (Font f) {
        Font old = font;
        font = f;
        if (! f.equals (old))
            firePropertyChange (PROP_font, old, f);
    }

    // hidden property!
    // don't bother firing events, no one should listen
public HashMap getSaved () { return saved; }
    public void setSaved (HashMap s) { saved = s; }

    // does not need to be bound either:
    public boolean isDebug () { return debug; }
    public void setDebug (boolean d) { debug = d; }

    // Called from Install.
    static void startRunning () { running = true; }
    static void installAll () {
        installKits (mimeTypes);
    }
    static void uninstallAll () {
        uninstallKits (mimeTypes);
    }

    // Actually registers our kit for the named MIME types.
    private static void installKits (String[] mt) {
        for (int i = 0; i < mt.length; i++) {
            String type = mt[i];
            if (debug) System.err.println ("Installing my kit for " + type + " (may require JDK 1.3!)...");
            // There might not be one, in which case we should not save any.
            //
            // NOTE: JEditorPane prints an exception to stderr even if the
            // problem was only that there was none registered.
            // http://developer.javasoft.com/developer/bugParade/bugs/4219676.html
            // Fixed in 1.3.
            //
            // Also there is a JEditorPane bug that causes JEditorPane to ignore the setting
            // if the getter was ever called for that type! So this is impossible in 1.2.*.
            // http://developer.javasoft.com/developer/bugParade/bugs/4237712.html
            // Fixed in 1.3.
            EditorKit old = JEditorPane.createEditorKitForContentType (type);
            if (debug) System.err.println ("Old kit: " + old);
            if (old != null) saved.put (type, old);
            // Remember the class loader! The system one may not do the
            // trick as this is in a module.
            JEditorPane.registerEditorKitForContentType (type, myKit, Settings.class.getClassLoader ());
            if (debug) System.err.println ("Now the kit is: " + JEditorPane.createEditorKitForContentType (type));
        }
    }
    // Correspondingly uninstalls, trying to restore the previous kit
    // per MIME type if there was one.
    private static void uninstallKits (String[] mt) {
        for (int i = 0; i < mt.length; i++) {
            String type = mt[i];
            if (debug) System.err.println ("Uninstalling my kit for " + type);
            EditorKit nue = (EditorKit) saved.get (type);
            if (debug) System.err.println ("Restored kit: " + nue);
            if (nue != null) {
                saved.remove (type);
                // Unfortunately there does not seem to be a more convenient
                // way of doing this as a static method on JEditorPane:
                if (debug) System.err.println ("Old kit: " + JEditorPane.createEditorKitForContentType (type));
                JEditorPane.registerEditorKitForContentType (type, nue.getClass ().getName (),
                        nue.getClass ().getClassLoader ());
                if (debug) System.err.println ("Now the kit is: " + JEditorPane.createEditorKitForContentType (type));
            }
        }
    }
}
