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

package org.netbeans.modules.rmi.wizard;

import java.io.IOException;
import org.openide.src.*;

/**
 *
 * @author  mryzl
 */

public interface Generator {

    /** Data for generator. */
    public void setData(Object settings);

    /** Perform generation. */
    public void generate() throws SourceException, IOException;
}

/*
* <<Log>>
*  2    Gandalf   1.1         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  1    Gandalf   1.0         7/27/99  Martin Ryzl     
* $ 
*/ 
