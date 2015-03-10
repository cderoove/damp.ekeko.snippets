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

package org.netbeans.modules.debugger.support;

import java.util.ResourceBundle;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

import org.openide.TopManager;
import org.openide.util.NbBundle;
import org.openide.windows.Workspace;
import org.openide.windows.WindowManager;

/** A property editor for available workspaces.
* Actually edits the string name of the workspace, not
* the {@link Workspace} itself. Also permits user to specify
* "none" (i.e. no valid workspace).
* @see WorkspacePool#getWorkspaces
* @author   Jan Jancura
*/
public class WorkspaceEditor extends java.beans.PropertyEditorSupport {

    /** Mapping between programmatic and display names of workspaces 
     * @associates String*/
    private Map namesMap;
    /** Validity flag - true if namesMap has been initialized already */
    private boolean namesInitialized = false;

    /*
    * @return The property value as a human editable string.
    * <p>   Returns null if the value can't be expressed as an editable string.
    * <p>   If a non-null value is returned, then the PropertyEditor should
    *       be prepared to parse that string back in setAsText().
    */
    public String getAsText() {
        if (!namesInitialized) {
            namesInitialized = true;
            initializeNamesMap(
                TopManager.getDefault().getWindowManager().getWorkspaces()
            );
        }
        String value = (String)getValue();
        String displayName = (String)namesMap.get(value);
        return (displayName == null) ? value : displayName;
    }

    /* Set the property value by parsing a given String.  May raise
    * java.lang.IllegalArgumentException if either the String is
    * badly formatted or if this kind of property can't be expressed
    * as text.
    * @param text  The string to be parsed.
    */
    public void setAsText (String text) throws java.lang.IllegalArgumentException {
        String programmaticName = findProgrammaticName(text);
        setValue((programmaticName == null) ? text : programmaticName);
    }

    /*
    * If the property value must be one of a set of known tagged values,
    * then this method should return an array of the tag values.  This can
    * be used to represent (for example) enum values.  If a PropertyEditor
    * supports tags, then it should support the use of setAsText with
    * a tag value as a way of setting the value.
    *
    * @return The tag values for this property.  May be null if this
    *   property cannot be represented as a tagged value.
    *
    */
    public String[] getTags() {
        WindowManager wm = TopManager.getDefault().getWindowManager();
        Workspace[] wss = wm.getWorkspaces();
        if (!namesInitialized) {
            namesInitialized = true;
            initializeNamesMap(wss);
        }
        String[] names = new String[wss.length + 1];
        for (int i = wss.length; --i >= 0; ) {
            names[i] = (String)namesMap.get(wss[i].getName());
        }
        names[wss.length] = NbBundle.getBundle(WorkspaceEditor.class).getString("Workspace_None");
        return names;
    }

    /** Initializes name mapping with given workspace set.
    * Result is stored in nameMap private variable. */
    private void initializeNamesMap (Workspace[] wss) {
        // fill name mapping with proper values
        namesMap = new HashMap(wss.length * 2);
        for (int i = 0; i < wss.length; i++) {
            // create new string for each display name to be able to search
            // using '==' operator in findProgrammaticName(String displayName) method
            namesMap.put(wss[i].getName(), new String(wss[i].getDisplayName()));;
        }
    }

    /** @return Returns programmatic name of the workspace for given
    * display name of the workspace. Uses special features of namesMap mapping
    * to perform succesfull search. */
    private String findProgrammaticName(String displayName) {
        for (Iterator iter = namesMap.entrySet().iterator(); iter.hasNext(); ) {
            Map.Entry curEntry = (Map.Entry)iter.next();
            if (displayName == curEntry.getValue()) {
                return (String)curEntry.getKey();
            }
        }
        return null;
    }

}

/*
 * Log
 *  4    Gandalf-post-FCS1.2.2.0     3/15/00  David Simonek   japanese localization 
 *       now works correctly (workspace setting)
 *  3    Gandalf   1.2         12/10/99 Daniel Prusa    Bug 4746 - class was not
 *       public
 *  2    Gandalf   1.1         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  1    Gandalf   1.0         10/6/99  Jan Jancura     
 * $
 * Beta Change History:
 *  0    Tuborg    0.11        --/--/98 anonymous       []
 */
