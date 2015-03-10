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

package org.netbeans.modules.vcs;
import org.netbeans.modules.vcs.util.*;
import java.io.*;

/** DescribeMe
 * 
 * @author Michal Fadljevic
 */
//-------------------------------------------
public class VcsFileStatus implements Serializable {
    private Debug D=new Debug("VcsFileStatus", false); // NOI18N
    private String status=null;

    //-------------------------------------------
    static final long serialVersionUID =-7189976440743418249L;
    public VcsFileStatus(){
    }

    //-------------------------------------------
    public String getStatus(){
        return status;
    }

    //-------------------------------------------
    public String toString(){
        return status;
    }


}

/*
 * Log
 *  5    Gandalf   1.4         1/6/00   Martin Entlicher 
 *  4    Gandalf   1.3         11/27/99 Patrik Knakal   
 *  3    Gandalf   1.2         10/25/99 Pavel Buzek     copyright and log
 *  2    Gandalf   1.1         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  1    Gandalf   1.0         9/30/99  Pavel Buzek     
 * $
 */
