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

package org.netbeans.modules.projects.settings;

import org.openide.options.*;
import org.openide.util.*;

/**
 *
 * @author  mryzl
 */

public class ProjectOption extends SystemOption {

    /** Ask for adding to the project. */
    public static final int ADD_ASK = 0;

    /** Never add to the project. */
    public static final int ADD_NEVER = 1;

    /** Always add to the project and don't ask. */
    public static final int ADD_ALWAYS = 2;

    /** Property name. */
    public static final String ADD_TO_PROJECT = "AddToProject"; // NOI18N

    /** serialVersionUID */
    static final long serialVersionUID = 6586089473876193532L;


    /** Holds value of property addToProject. */
    private int addToProject;
    /** Creates new ProjectOption. */
    public ProjectOption() {
    }

    /** Get a human presentable name of the action.
    * This may be
    * presented as an item in a menu.
    * @return the name of the action
    */
    public String displayName () {
        return NbBundle.getBundle(ProjectOption.class).getString("CTL_ProjectOption"); // NOI18N
    }


    /** Get a help context for the action.
    * @return the help context for this action
    */
    public HelpCtx getHelpCtx () {
        return new HelpCtx(ProjectOption.class);
    }

    public boolean isGlobal() {
        return false;
    }

    /** Getter for property addToProject.
     *@return Value of property addToProject.
     */
    public int getAddToProject() {
        Integer val = (Integer) getProperty(ADD_TO_PROJECT);
        if (val != null) return val.intValue();
        else return ADD_ASK;
    }

    /** Setter for property addToProject.
     *@param addToProject New value of property addToProject.
     */
    public void setAddToProject(int addToProject) {
        Integer oldval = (Integer) getProperty(ADD_TO_PROJECT), newval = new Integer(addToProject);
        putProperty(ADD_TO_PROJECT, newval);
        firePropertyChange ("addToProject", oldval, newval); // NOI18N
    }

    // --
    /** property editor for period property
    */
    public static class AddPropertyEditor extends java.beans.PropertyEditorSupport {

        /** Array of tags
        */
        private static final String[] tags = {
            NbBundle.getBundle(ProjectOption.class).getString("CTL_AddAsk"),
            NbBundle.getBundle(ProjectOption.class).getString("CTL_AddNever"),
            NbBundle.getBundle(ProjectOption.class).getString("CTL_AddAlways"),
        };

        /** @return names of the supported member Acces types */
        public String[] getTags() {
            return tags;
        }

        /** @return text for the current value */
        public String getAsText () {
            return tags[((Integer)getValue()).intValue()];
        }

        /** @param text A text for the current value. */
        public void setAsText (String text) {
            for(int i = 0; i < tags.length; i++) {
                if (text.equals(tags[i])) {
                    setValue(new Integer(i));
                    return;
                }
            }
            setValue(new Integer(ADD_ASK));
        }
    }

}

/*
* Log
*  1    Gandalf   1.0         1/14/00  Martin Ryzl     
* $ 
*/ 
