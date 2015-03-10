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

package org.netbeans.modules.search.types;

import java.io.*;

import org.openide.util.*;
import org.openide.loaders.*;
import org.openide.filesystems.*;

import org.openidex.search.*;

import org.netbeans.modules.search.res.*;
import org.netbeans.modules.search.*;

/**
 * Test DataObject primaryFile for line full-text match.
 *
 * @author  Petr Kuzel
 * @version 1.0
 */
public class FullTextType extends TextType {

    public static final long serialVersionUID = 1L;

    /** Creates new FullTextType */
    public FullTextType() {
    }


    /**
    * @return true if object passes the test.
    */
    public boolean test (DataObject dobj) {
        try {

            String line = ""; //NOI18N
            boolean hit = false;

            FileObject fo = dobj.getPrimaryFile();

            // it is strange
            if (fo == null) return false;

            // Primary File Content

            InputStream is = fo.getInputStream();
            LineNumberReader reader = new LineNumberReader(new InputStreamReader(is));
            while (true) {
                line = reader.readLine();
                if (line == null) break;
                if ( match(line)  ) {
                    hit = true;

                    // add line detail
                    StructuredDetail det = new StructuredDetail();
                    det.fo = fo;
                    det.line = reader.getLineNumber();
                    det.text = line;
                    addDetail(det);
                }
            }

            // gloal line datail
            if (hit)
                addDetail(Res.text("DETAIL_LINE") + reader.getLineNumber()); // NOI18N

            return hit;

        } catch (FileNotFoundException ex) {
            return false;

        } catch (IOException ex) {
            if (Boolean.getBoolean ("netbeans.debug.exceptions")) // NOI18N
                ex.printStackTrace();
            return false;

        }
    }

    /** @return string desribing current state.
    */
    public String toString() {
        return "FullTextType: " + isValid() + " substring:" + matchString + " REstring:" + reString + " re:" + re; // NOI18N
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(this.getClass());
    }

    public String getTabText() {
        return Res.text("FULLTEXT_CRITERION"); // NOI18N
    }

    public Class[] getDetailClasses() {
        return new Class[] { String.class, StructuredDetail.class };
    }
}


/*
* Log
*  14   Gandalf-post-FCS1.10.2.2    4/4/00   Petr Kuzel      Comments + output window 
*       fix
*  13   Gandalf-post-FCS1.10.2.1    3/9/00   Petr Kuzel      I18N
*  12   Gandalf-post-FCS1.10.2.0    2/24/00  Ian Formanek    Post FCS changes
*  11   Gandalf   1.10        1/18/00  Jesse Glick     Context help.
*  10   Gandalf   1.9         1/13/00  Radko Najman    I18N
*  9    Gandalf   1.8         1/11/00  Petr Kuzel      Result details added.
*  8    Gandalf   1.7         1/10/00  Petr Kuzel      "valid" fired.
*  7    Gandalf   1.6         1/5/00   Petr Kuzel      Margins used. Help 
*       contexts.
*  6    Gandalf   1.5         1/4/00   Petr Kuzel      Bug hunting.
*  5    Gandalf   1.4         12/23/99 Petr Kuzel      Architecture improved.
*  4    Gandalf   1.3         12/20/99 Petr Kuzel      L&F fixes.
*  3    Gandalf   1.2         12/17/99 Petr Kuzel      Bundling.
*  2    Gandalf   1.1         12/15/99 Martin Balin    Fixed package name
*  1    Gandalf   1.0         12/14/99 Petr Kuzel      
* $ 
*/ 

