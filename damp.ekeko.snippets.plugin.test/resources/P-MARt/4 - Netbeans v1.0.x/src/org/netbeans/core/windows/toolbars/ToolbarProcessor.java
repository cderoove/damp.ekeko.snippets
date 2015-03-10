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

import java.io.IOException;

import org.openide.loaders.XMLDataObject;
import org.openide.cookies.InstanceCookie;
import org.openide.nodes.Node;


/** Describe what the class is for.
 *
 * @author Libor Kramolis
 */
public class ToolbarProcessor implements XMLDataObject.Processor, InstanceCookie {

    protected XMLDataObject xmlDataObject;

    public void attachTo (XMLDataObject o) {
        xmlDataObject = o;
    }

    /**
     *
     */
    public String instanceName () {
        return instanceClass().getName();
    }

    /**
     *
     */
    public Class instanceClass () {
        return ToolbarConfiguration.class;
    }

    /**
     *
     */
    public Object instanceCreate () throws IOException {
        try {
            return new ToolbarConfiguration (xmlDataObject.getName(), xmlDataObject.getDocument());
        } catch (org.xml.sax.SAXException e) {
            throw new IOException (e.getClass().getName() + ": " + e.getMessage()); // NOI18N
        }
    }
}

/*
 * Log
 *  6    Gandalf   1.5         1/16/00  Libor Kramolis  
 *  5    Gandalf   1.4         1/13/00  David Simonek   localization
 *  4    Gandalf   1.3         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  3    Gandalf   1.2         9/30/99  Libor Kramolis  
 *  2    Gandalf   1.1         9/29/99  Libor Kramolis  
 *  1    Gandalf   1.0         7/11/99  David Simonek   
 * $
 */
