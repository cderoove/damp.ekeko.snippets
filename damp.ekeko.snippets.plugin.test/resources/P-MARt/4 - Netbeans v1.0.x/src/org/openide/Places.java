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

package org.openide;

import org.openide.nodes.Node;
import org.openide.loaders.DataFilter;
import org.openide.loaders.DataFolder;

/** Provides access to all basic components ("places") in the IDE.
* One can obtain folders for templates, project files,
* a node for the repository, etc.
*
* @author Jaroslav Tulach
*/
public interface Places {
    /** Interesting places for nodes.
    * @return group of interesting nodes
    */
    public Nodes nodes ();

    /** Interesting places for data objects.
    * @return group of interesting data objects
    */
    public Folders folders ();

    /** Provides access to important node places.
    */
    public interface Nodes {
        /** Get the Repository node.
         * @return the node
        */
        public Node repository ();

        /** Get a Repository node with a given data filter.
         * @param f the requested filter
         * @return the node
         */
        public Node repository(DataFilter f);

        /** Get a root of packages with a given data filter.
        * @param f the requested filter
        * @return the node
        */
        public Node packages (DataFilter f);

        /** Get a node with all installed loaders.
         * @return the node
        */
        public Node loaderPool ();

        /** Get the control panel node.
         * @return the node
        */
        public Node controlPanel ();

        /** Get the project settings node.
         * @return the node
        */
        public Node project ();

        /** Get the environment node.
         * This node holds all transient information about
        * the IDE.
         * @return the node
        */
        public Node environment ();

        /** Get the session node.
        * This node holds all global information about the IDE.
        * @return the node
        */
        public Node session ();

        /** Get a node with all workspaces.
         * @return the node
         */
        public Node workspaces ();

        /** Get the Repository settings node.
         * @return the node
         */
        public Node repositorySettings ();

        /** Get the Desktop node for the current project.
         * This node can change when a new project is selected.
         * @return the node
        */
        public Node projectDesktop ();

        /** Get all root nodes.
         * These nodes are the ones which will be displayed as "top-level" roots.
         * Modules typically add to this set.
        * @return the nodes
        */
        public Node[] roots ();
    }

    /** Provides access to important folders.
    */
    public interface Folders {
        /** Get the default folder for templates.
         * @return the folder
        */
        public DataFolder templates ();

        /** Get the folder for toolbars.
         * @return the folder
        */
        public DataFolder toolbars ();

        /** Get the folder for menus.
         * @return the folder
        */
        public DataFolder menus ();

        /** Get the folder for actions pool.
         * @return the folder
        */
        public DataFolder actions ();

        /** Get the folder for bookmarks.
         * @return the folder
        */
        public DataFolder bookmarks ();

        /** Get the folder for projects.
         * @return the folder
        */
        public DataFolder projects ();

        /** Get the startup folder.
         * Files in this folder implementing {@link org.openide.cookies.ExecCookie}
         * will be run upon startup of the IDE.
         * @return the folder
        */
        public DataFolder startup ();
    }
}

/*
* Log
*  24   Gandalf   1.23        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  23   Gandalf   1.22        9/10/99  Jaroslav Tulach Introduction of services.
*  22   Gandalf   1.21        8/3/99   Jaroslav Tulach Project settings node.
*  21   Gandalf   1.20        6/28/99  Jaroslav Tulach Debugger types are like 
*       Executors
*  20   Gandalf   1.19        6/8/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  19   Gandalf   1.18        6/8/99   Ian Formanek    Added method actions() to
*       obtain folder for ActionsPool
*  18   Gandalf   1.17        5/27/99  Jaroslav Tulach Executors rearanged.
*  17   Gandalf   1.16        5/7/99   Jan Jancura     Places.Nodes.packages () 
*       method added
*  16   Gandalf   1.15        3/30/99  Jesse Glick     [JavaDoc]
*  15   Gandalf   1.14        3/29/99  Jaroslav Tulach places ().nodes 
*       ().session ()
*  14   Gandalf   1.13        3/15/99  Jesse Glick     [JavaDoc]
*  13   Gandalf   1.12        3/13/99  Jaroslav Tulach Places.roots ()
*  12   Gandalf   1.11        3/11/99  Ian Formanek    
*  11   Gandalf   1.10        3/11/99  Ian Formanek    Bookmarks & Startup added
*       to Session Settings
*  10   Gandalf   1.9         3/3/99   Jesse Glick     [JavaDoc]
*  9    Gandalf   1.8         2/19/99  Jaroslav Tulach added startup directory
*  8    Gandalf   1.7         2/12/99  Ian Formanek    Reflected renaming 
*       Desktop -> Workspace
*  7    Gandalf   1.6         1/25/99  Jaroslav Tulach Saves filesystempool & 
*       control panel in the default project
*  6    Gandalf   1.5         1/25/99  David Peroutka  support for menus and 
*       toolbars
*  5    Gandalf   1.4         1/20/99  Jaroslav Tulach 
*  4    Gandalf   1.3         1/20/99  David Peroutka  
*  3    Gandalf   1.2         1/6/99   Jaroslav Tulach 
*  2    Gandalf   1.1         1/6/99   Jaroslav Tulach 
*  1    Gandalf   1.0         1/5/99   Ian Formanek    
* $
*/
