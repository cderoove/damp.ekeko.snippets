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

package org.netbeans.core;

import java.io.*;
import java.util.*;
import java.net.*;

import org.xml.sax.*;
import org.xml.sax.helpers.*;

import org.openide.TopManager;
import org.openide.NotifyDescriptor;
import org.openide.util.HelpCtx;

/** Reference implementation of the helpfiles of JavaHelp.
* Also holds all installed helpIDs and can display a 
* help page when such HelpCtx should be opened.
*
* @author Jaroslav Tulach
*/
public abstract class Help extends Object {
    private Help () {}
    /** instance of the Help */
    private static Impl help;

    /** Finds default instance of the help.
    */
    public static Impl getDefault () {
        if (help == null) {

            try {
                Class.forName ("javax.help.HelpSet"); // NOI18N
                try {
                    // Avoid making any static references to javax.help.*
                    // (just new JavaHelp() should work, but this is a little safer):
                    help = (Impl) Class.forName ("org.netbeans.core.JavaHelp").newInstance (); // NOI18N
                } catch (Exception e) {
                    // These would actually be unexpected.
                    TopManager.getDefault ().notifyException (e);
                    help = new XMLImpl ();
                }
            } catch (ClassNotFoundException cnfe) {
                // Fine, JavaHelp not installed.
                help = new XMLImpl ();
            }

        }
        return help;
    }

    /** Methods delegated to different implementations.
    */
    public static interface Impl {
        /** Adds help set URL into the system */
        public void addHelpSet (URL resource, String moduleCodeName, String moduleDisplayName);

        /** Removes help set URL from the system */
        public void removeHelpSet (URL resource);

        /** Shows help.
        */
        public void showHelp (HelpCtx ctx);

        /** Get a map from module code names to home IDs. */
        public Map getHomesByCode ();

        /** Get a map from module display names to home IDs. */
        public Map getHomesByDisplay ();

        /** Display name for the master index, or <code>null</code> if none. */
        public String getMasterDisplayName ();

        /** Help ID for the master index, or <code>null</code> if none. */
        public String getMasterID ();

        /** Test whether a given homeID is particularly distinguished, so should be displayed first.
        * @param id the home ID
        * @return true if it should be considered distinguished
        */
        public boolean isDistinguished (String homeID);
    }



    /** Simple implementation that takes only important tags from the HelpSet file.
    * Serves for two purposes:
    * <OL>
    *   <LI>Implementation of Help.Impl
    *   <LI>DocumentHandler callback for XML
    * </OL>
    */
    private static final class XMLImpl extends HandlerBase implements Impl {
        /** tags in map file */
        private static final String MAP_ID = "mapID"; // NOI18N
        private static final String TARGET = "target"; // NOI18N
        private static final String URL = "url"; // NOI18N

        /** important tags in help set file */
        private static final String MAPS = "maps"; // NOI18N
        private static final String HOME_ID = "homeID"; // NOI18N
        private static final String MAPREF = "mapref"; // NOI18N
        private static final String LOCATION = "location"; // NOI18N

        // [PENDING] these should not be static--this class should be
        // untangled so they do not have to be

        /** map of URL of help set to map of string ids to string URLs.
        * Type (URL, Map (String, String)).
        * @associates HashMap
        */
        private static Map help = new HashMap (7);

        /** map of helpset URLs to code names 
         * @associates String*/
        private static Map moduleCodeNames = new HashMap (); // Map<URL, String>

        /** map of helpset URLs to display names 
         * @associates String*/
        private static Map moduleDisplayNames = new HashMap (); // Map<URL, String>

        /** map of code names to home IDs 
         * @associates String*/
        private static Map homesByCode = new HashMap (); // Map<String, String>

        /** map of display names to home IDs 
         * @associates String*/
        private static Map homesByDisplay = new HashMap (); // Map<String, String>

        public synchronized Map getHomesByCode () {
            return new HashMap (homesByCode);
        }

        public synchronized Map getHomesByDisplay () {
            return new HashMap (homesByDisplay);
        }

        /** buffer to contain the homeID or null if we are not reading homeID */
        private StringBuffer homeID;

        /** base URL for parsing file */
        private URL base;

        private Parser parser;

        /** map to currently work with (String, String) 
         * @associates String*/
        private Map current;

        /** module info to currently work with */
        private String moduleCodeName, moduleDisplayName;

        /** Creates new XML parser/saver for HelpSet file.
        * This constructor is for the 
        */
        public XMLImpl () {
        }

        /** Creates new XML parser/saver for HelpSet file.
        */
        private XMLImpl (URL base, Map current, String moduleCodeName, String moduleDisplayName) {
            this.base = base;
            this.current = current;
            this.moduleCodeName = moduleCodeName;
            this.moduleDisplayName = moduleDisplayName;

            parser = org.openide.loaders.XMLDataObject.createParser ();
            parser.setDocumentHandler (this);
        }

        // [PENDING] synchronized??
        /** Adds help set URL into the system */
        public void addHelpSet (URL resource, String moduleCodeName, String moduleDisplayName) {
            //System.err.println ("Adding help set: " + resource + " " + moduleCodeName + " " + moduleDisplayName);
            try {
                HashMap map = new HashMap ();
                XMLImpl xml = new XMLImpl (resource, map, moduleCodeName, moduleDisplayName);
                xml.parse ();
                // store the map
                help.put (resource, map);
                moduleCodeNames.put (resource, moduleCodeName);
                moduleDisplayNames.put (resource, moduleDisplayName);
            } catch (SAXException e) {
                TopManager.getDefault ().notifyException (e);
            } catch (IOException e) {
                TopManager.getDefault ().notifyException (e);
            }
        }

        /** Removes help set URL from the system */
        public synchronized void removeHelpSet (URL resource) {
            help.remove (resource);
            homesByCode.remove (moduleCodeNames.remove (resource));
            homesByDisplay.remove (moduleDisplayNames.remove (resource));
        }

        /** Shows help.
        */
        public synchronized void showHelp (HelpCtx ctx) {
            URL url = ctx.getHelp ();
            String s = ctx.getHelpID ();

            if (url == null) {
                // iterator over maps
                String x = null;
                Iterator it = help.values ().iterator ();
                while (x == null && it.hasNext ()) {
                    Map m = (Map)it.next ();
                    x = (String)m.get (s);
                }
                if (x != null) {
                    try {
                        url = new URL (x);
                    } catch (MalformedURLException e) {
                        // go on, but it should not happen
                    }
                }
            }

            if (url != null) {
                TopManager.getDefault ().showUrl (url);
            } else {
                TopManager.getDefault ().notify (new NotifyDescriptor.Message (Main.getString ("EXC_HelpIDNotFound", s)));
            }
        }

        /** Registration of the id */
        private void registerID (String id, URL url) {
            // saving the URL as strings could save some memory
            current.put (id.intern (), url.toExternalForm ().intern ());
        }

        /** If we have homeID buffer non-null we add the text to it.
        * Because it composes the homeID.
        */
        public void characters(char[] ch, int start, int length) throws SAXException {
            if (homeID != null) {
                homeID.append (ch, start, length);
            }
        }

        /** Accepts module item */
        public void startElement (String name, AttributeList attr) throws SAXException {
            if (MAP_ID.equals (name)) {
                String target = attr.getValue (TARGET);
                String url = attr.getValue (URL);
                if (target != null && url != null) {
                    try {
                        registerID (target, new URL (base, url));
                    } catch (MalformedURLException e) {
                        throw new SAXException ("Malformed help URL: " + url + " for target: " + target); // NOI18N
                    }
                }
                return;
            }
            if (HOME_ID.equals (name)) {
                homeID = new StringBuffer ();
                return;
            }
            if (MAPREF.equals (name)) {
                processMap (attr.getValue (LOCATION));
                return;
            }
        }

        /** Accepts module item */
        public void endElement (String name) {
            if (HOME_ID.equals (name)) {
                String id = homeID.toString ();
                //System.err.println ("Found home ID: " + id + " " + moduleCodeName + " " + moduleDisplayName);
                homesByCode.put (moduleCodeName, id);
                homesByDisplay.put (moduleDisplayName, id);
                homeID = null;
            }
        }

        /** Processes map file.
        */
        private void processMap (String url) throws SAXException {
            try {
                URL map = new URL (base, url);
                XMLImpl impl = new XMLImpl (map, current, moduleCodeName, moduleDisplayName);
                impl.parse ();
            } catch (IOException e) {
                throw new SAXException (e.getMessage ());
            }
        }

        /** Parses the HelpSet file.
        */
        public void parse () throws IOException, SAXException {
            parser.parse (base.toExternalForm ());
        }

        /** Display name for the master index, or <code>null</code> if none. */
        public String getMasterDisplayName() {
            return null;
        }
        /** Help ID for the master index, or <code>null</code> if none. */
        public String getMasterID() {
            return null;
        }
        /** Test whether a given homeID is particularly distinguished, so should be displayed first.
         * @param id the home ID
         * @return true if it should be considered distinguished
         */
        public boolean isDistinguished(String homeID) {
            return false;
        }
    }


}

/*
* Log
*  12   Gandalf   1.11        1/13/00  Jaroslav Tulach I18N
*  11   Gandalf   1.10        12/21/99 Jesse Glick     Putting User's Guide off 
*       from the rest of the help menu items to visually distinguish it.
*  10   Gandalf   1.9         12/20/99 Jesse Glick     Reorganized Help | 
*       Features to be Help | Documentation, killing old UG browse action, 
*       better labelling of master help set, etc.
*  9    Gandalf   1.8         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  8    Gandalf   1.7         10/1/99  Libor Kramolis  
*  7    Gandalf   1.6         10/1/99  Libor Kramolis  
*  6    Gandalf   1.5         9/29/99  Jesse Glick     Safer test-loading of 
*       JavaHelp impl.
*  5    Gandalf   1.4         9/27/99  Jesse Glick     JavaHelp implementation 
*       (optional).
*  4    Gandalf   1.3         7/9/99   Jesse Glick     ModuleHelpAction 
*       implemented.
*  3    Gandalf   1.2         6/25/99  Jaroslav Tulach Works with IBM parser.
*  2    Gandalf   1.1         6/8/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  1    Gandalf   1.0         5/7/99   Jaroslav Tulach 
* $
*/
