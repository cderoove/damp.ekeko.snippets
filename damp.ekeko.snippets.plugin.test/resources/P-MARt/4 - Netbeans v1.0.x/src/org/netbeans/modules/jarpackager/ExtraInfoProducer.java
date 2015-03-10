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

package org.netbeans.modules.jarpackager;

import java.util.List;
import java.io.Serializable;
import java.io.InputStream;

/** Interface for inserting extra information into the jar archive.
*
* @author Dafe Simonek
*/
public interface ExtraInfoProducer extends Serializable {

    /** Returns a list containing extra information to be added
    * into the archive.
    * Returned list should consist of ExtraEntry items.
    * JarCreater will read the info from input stream into the
    * jar entry named according to the value returned from ExtraEntry.getName().
    * @return List containing extra info (with the structure desribed
    * above)
    */
    public List extraInfo ();

    /* Each extra entry holds entry name which is the name of the entry
    * in the resulting archive, the input stream is an instance of
    * InputStream class, which will be used to read extra info data.
    * size is the size of extra data in entry. */
    public interface ExtraEntry {

        /** Name of this entry, will be used as entry name in the archive */
        public String getName ();

        /** Newly created input stream to read data of this entry from */
        public InputStream createInputStream ();

        /** Size of extra data of this entry */
        public long getSize ();

    } // end of ExtraEntry inner interface

}

/*
* <<Log>>
*  6    Gandalf   1.5         1/25/00  David Simonek   Various bugfixes and i18n
*  5    Gandalf   1.4         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  4    Gandalf   1.3         10/4/99  David Simonek   
*  3    Gandalf   1.2         9/13/99  David Simonek   necessary changes for 
*       better support of STORED achives
*  2    Gandalf   1.1         6/4/99   David Simonek   manifest creation now 
*       supported correctly
*  1    Gandalf   1.0         6/3/99   David Simonek   
* $
*/