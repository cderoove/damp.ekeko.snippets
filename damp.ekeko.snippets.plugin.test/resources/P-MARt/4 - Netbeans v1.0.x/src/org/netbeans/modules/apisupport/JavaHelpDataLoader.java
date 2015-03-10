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

import java.io.IOException;
import java.text.Format;
import java.util.*;

import org.openide.actions.*;
import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.openide.util.MapFormat;
import org.openide.util.SharedClassObject;
import org.openide.util.actions.SystemAction;

import org.netbeans.modules.xml.XMLDataLoader;

public class JavaHelpDataLoader extends UniFileLoader {

    private static final long serialVersionUID =-7728960313890246535L;
    public JavaHelpDataLoader() {
        this (org.netbeans.modules.xml.XMLDataObject.class);
    }

    public JavaHelpDataLoader(Class recognizedObject) {
        super (recognizedObject);
    }

    // List of XML file types in use. Each is extension, then public ID, then resource path to DTD.
    private static final String[][] types = {
        { "hs",  "-//Sun Microsystems Inc.//DTD JavaHelp HelpSet Version 1.0//EN", "org/netbeans/modules/apisupport/resources/helpset_1_0.dtd" },
        { "jhm", "-//Sun Microsystems Inc.//DTD JavaHelp Map Version 1.0//EN",     "org/netbeans/modules/apisupport/resources/map_1_0.dtd" },
        { "toc", "-//Sun Microsystems Inc.//DTD JavaHelp TOC Version 1.0//EN",     "org/netbeans/modules/apisupport/resources/toc_1_0.dtd" },
        { "idx", "-//Sun Microsystems Inc.//DTD JavaHelp Index Version 1.0//EN",   "org/netbeans/modules/apisupport/resources/index_1_0.dtd" }
    };

    protected void initialize () {

        setDisplayName ("JavaHelp Files");

        ExtensionList extensions = new ExtensionList ();
        for (int i = 0; i < types.length; i++)
            extensions.addExtension (types[i][0]);
        setExtensions (extensions);

        setActions (((XMLDataLoader) SharedClassObject.findObject (XMLDataLoader.class, true)).getActions ());

        ClassLoader loader = getClass ().getClassLoader ();
        for (int i = 0; i < types.length; i++)
            org.openide.loaders.XMLDataObject.registerCatalogEntry (types[i][1], types[i][2], loader);

    }

    protected MultiDataObject createMultiObject (FileObject primaryFile)
    throws DataObjectExistsException, IOException {
        return new org.netbeans.modules.xml.XMLDataObject (primaryFile, this);
    }

    protected MultiDataObject.Entry createPrimaryEntry (MultiDataObject mdo, FileObject fo) {
        return new JavaHelpEntry (mdo, fo);
    }

    static class JavaHelpEntry extends FileEntry.Format {

        private static final long serialVersionUID =2248161434771748311L;
        public JavaHelpEntry (MultiDataObject mdo, FileObject fo) {
            super (mdo, fo);
        }

        protected java.text.Format createFormat (FileObject target, String name, String ext) {
            String homeID = target.isRoot () ? "HOMEID" : target.getPackageName ('.') + ".HOMEID";
            Map m = new HashMap ();
            if ("hs".equals (ext)) {
                m.put ("LIKELY_HOME_ID", homeID);
                String base;
                if (name.endsWith ("HelpSet"))
                    base = name.substring (0, name.length () - 7) + "HelpMap";
                else
                    base = "Map";
                m.put ("LIKELY_MAP_URL_QUOTED", "\"" + base + ".jhm\"");
                if (name.endsWith ("HelpSet"))
                    base = name.substring (0, name.length () - 7) + "HelpContents";
                else
                    base = "Contents";
                m.put ("LIKELY_TOC_URL", base + ".toc");
                if (name.endsWith ("HelpSet"))
                    base = name.substring (0, name.length () - 7) + "HelpIndex";
                else
                    base = "Index";
                m.put ("LIKELY_INDEX_URL", base + ".idx");
            } else if ("jhm".equals (ext)) {
                m.put ("LIKELY_HOME_ID_QUOTED", "\"" + homeID + "\"");
                String base;
                if (name.endsWith ("HelpMap"))
                    base = name.substring (0, name.length () - 7) + "HelpPage";
                else
                    base = "index";
                m.put ("LIKELY_HTML_URL_QUOTED", "\"" + base + ".html\"");
            }
            MapFormat format = new MapFormat (m);
            format.setLeftBrace ("__");
            format.setRightBrace ("__");
            return format;
        }

    }

}

/*
 * Log
 *  6    Gandalf-post-FCS1.4.1.0     3/28/00  Jesse Glick     SVUIDs.
 *  5    Gandalf   1.4         12/17/99 Jesse Glick     JavaHelp files are now 
 *       treated as real XML.
 *  4    Gandalf   1.3         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  3    Gandalf   1.2         10/5/99  Jesse Glick     Sundry API changes 
 *       affecting me.
 *  2    Gandalf   1.1         9/30/99  Jesse Glick     Package rename and misc.
 *  1    Gandalf   1.0         9/14/99  Jesse Glick     
 * $
 */
