/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.properties.syntax;

import org.openide.util.NbBundle;

/**
 * Options for the java editor kit
 *
 * @author Petr Jiricka, Libor Kramolis
 */
public class PropertiesPrintOptions extends org.netbeans.modules.editor.options.BasePrintOptions {

    public static final String PROPERTIES = "properties";

    static final long serialVersionUID =-1281317957713907843L;
    public PropertiesPrintOptions() {
        super (PropertiesKit.class, PROPERTIES);
    }

    public PropertiesPrintOptions (Class kitClass, String typeName) {
        super (kitClass, typeName);
    }
}

/*
 * <<Log>>
 *  3    Gandalf   1.2         11/27/99 Patrik Knakal   
 *  2    Gandalf   1.1         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  1    Gandalf   1.0         9/13/99  Petr Jiricka    
 * $
 */
