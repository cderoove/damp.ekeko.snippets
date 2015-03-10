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

package org.netbeans.core.windows.toolbars;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;

import org.openide.nodes.PropertySupport;
import org.openide.util.NbBundle;
import org.openide.awt.ToolbarPool;

import org.netbeans.core.windows.nodes.WorkspaceContext;

public class ToolbarConfigurationPropertyEditor extends PropertySupport.ReadWrite {

    private static final java.util.ResourceBundle bundle = NbBundle.getBundle (ToolbarConfigurationPropertyEditor.class);

    private static ToolbarPool toolbarPool = ToolbarPool.getDefault();

    private WorkspaceContext workspaceContext;


    public ToolbarConfigurationPropertyEditor (WorkspaceContext wc) {
        super (bundle.getString ("PROP_desk_config"),
               String.class,
               bundle.getString ("PROP_desk_config"),
               bundle.getString ("HINT_desk_config"));
        workspaceContext = wc;
    }

    public Object getValue () {
        return workspaceContext.getToolbarConfigName();
    }


    public void setValue (Object n) {
        workspaceContext.setToolbarConfigName (n.toString());
    }

    public PropertyEditor getPropertyEditor () {
        return new ToolbarConfigurationPropertyEditorSupport();
    }

    // class ToolbarConfigurationPropertyEditorSupport
    class ToolbarConfigurationPropertyEditorSupport extends PropertyEditorSupport {
        /** @return names of the toolbar configurations */
        public String[] getTags() {
            return toolbarPool.getConfigurations();
        }
    }
}

/*
 * Log
 *  3    Gandalf   1.2         1/16/00  Libor Kramolis  
 *  2    Gandalf   1.1         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  1    Gandalf   1.0         7/11/99  David Simonek   
 * $
 */
