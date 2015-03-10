/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.vcs.advanced;

import java.beans.*;
import java.util.*;
import org.netbeans.modules.vcs.*;
import org.netbeans.modules.vcs.cmdline.*;

/**
 *
 * @author  Pavel Buzek
 * @version 
 */
public class CommandLineVcsAdvancedCustomizer extends Object implements VcsAdvancedCustomizer {

    /** Creates new CommandLineVcsAdvancedCustomizer */
    public CommandLineVcsAdvancedCustomizer() {
    }

    public void writeConfig(java.util.Properties props,Object config) {
        UserCommand.writeConfiguration (props, (java.util.Vector) config);
    }

    public Object readConfig (java.util.Properties props) {
        return UserCommand.readCommands (props);
    }

    public PropertyEditor getEditor(VcsFileSystem fileSystem){
        UserCommandsEditor commandEditor = new UserCommandsEditor();
        commandEditor.setValue( ((CommandLineVcsFileSystem) fileSystem).getCommands () );
        return commandEditor;
    }

    public javax.swing.JPanel getPanel(PropertyEditor pe) {
        /*
            commandEditor.addPropertyChangeListener
              (new PropertyChangeListener() {
        	public void propertyChange(PropertyChangeEvent e){
        	  //D.deb("commands - propertyChange()");
        	  fs.setCommands ((Vector) commandEditor.getValue() );
        	}
              });
        */   
        return new UserCommandsPanel ( (UserCommandsEditor) pe);
    }
}
/*
 * <<Log>>
 *  5    Gandalf   1.4         10/25/99 Pavel Buzek     copyright
 *  4    Gandalf   1.3         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  3    Gandalf   1.2         9/30/99  Pavel Buzek     
 *  2    Gandalf   1.1         9/8/99   Pavel Buzek     
 *  1    Gandalf   1.0         9/8/99   Pavel Buzek     
 * $
 */
