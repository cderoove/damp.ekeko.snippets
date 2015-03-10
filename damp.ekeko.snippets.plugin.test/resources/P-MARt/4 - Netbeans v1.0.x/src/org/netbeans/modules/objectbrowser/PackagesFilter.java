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

package org.netbeans.modules.objectbrowser;

import java.util.*;
import java.text.MessageFormat;

import org.openide.loaders.DataFilter;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;

import org.apache.regexp.*;

/**
* This class represents sort of filters for object browser. 
* Each filter has name and set of subfilters (connected with ||).
* It stores currently selected filter too. Packages filter
* can be visually customized using PackagesFilterEditor.
*
* @author Jan Jancura
*/
class PackagesFilter implements java.io.Serializable {

    // static ....................................................................................

    static final long serialVersionUID = 3372096529946664837L;

    static String resolve (String s) {
        int i = s.indexOf ('*');
        int k = s.indexOf ('.');
        boolean aster = true;
        if ((i < 0) || ((k >= 0) && (k < i))) {
            aster = false;
            i = k;
        }
        int j = 0;
        StringBuffer sb = new StringBuffer ();
        while (i >= 0) {
            sb.append (s.substring (j, i));
            if (aster) sb.append (".*"); // NOI18N
            else sb.append ("[.]"); // NOI18N
            j = i + 1;
            i = s.indexOf ('*', j);
            k = s.indexOf ('.', j);
            if ((i < 0) || ((k >= 0) && (k < i))) {
                aster = false;
                i = k;
            } else
                aster = true;
        }
        sb.append (s.substring (j));
        return new String (sb);
    }


    // variables ....................................................................................

    /** Set of filter names. */
    Vector                       filterNames;
    /** Set of subilters for each filter. 
     * @associates Vector*/
    Vector                       filterValues;
    /** Index of currently selected filter.*/
    int                          index = 0;


    // init .........................................................................................

    /**
    * Creates a new set of filter containing "All packages" filter.
    */
    PackagesFilter () {
        filterNames = new Vector ();
        filterNames.add (NbBundle.getBundle (PackagesFilter.class).getString ("CTL_All_packages"));
        filterValues = new Vector ();
        Vector v = new Vector ();
        filterValues.add (v);
    }


    // main methods .....................................................................

    /**
    * @return DataFilter which represents currently selected filter.
    */
    DataFilter getDataFilter () {
        StringBuffer sb = new StringBuffer ();
        sb.append ('^');

        if ((index < 0) || (filterValues.size () == 0))
            sb.append (".*"); // NOI18N
        else {
            Vector v = (Vector) filterValues.elementAt (index);
            if (v.size () == 0)
                sb.append (".*"); // NOI18N
            else {
                sb.append (((Filter) v.elementAt (0)).getFilter ());
                int i, k = v.size ();
                for (i = 1; i < k; i++)
                    sb.append ('|').append (((Filter) v.elementAt (i)).getFilter ());
            }
        }
        sb.append ('$');
        return new InnerFilter (new String (sb));
    }

    /**
    * Sets current filter.
    */
    void setSelected (int i) {
        index = i;
    }


    // innerclasses .........................................................................

    /**
    * Subfilter interface. Each filter must be able to represents themselves as
    * a regular expression.
    */
    static interface Filter extends java.io.Serializable {
        static final long serialVersionUID = -6206484035199019046L;

        String getFilter ();
    }

    /**
    * Wildcard subfilter.
    */
    static class PackageFilter implements Filter {
        static final long serialVersionUID = -8168688048152187338L;

        String packageName = "com.*"; // NOI18N

        public String getFilter () {
            return PackagesFilter.resolve (packageName.trim ());
        }

        public String toString () {
            return new MessageFormat (
                       NbBundle.getBundle (PackagesFilter.class).
                       getString ("CTL_Wildcard_filter")
                   ).format (new Object[] {packageName});
        }
    }

    /**
    * Regular expression subfilter.
    */
    static class RegularFilter implements Filter {
        static final long serialVersionUID =1579868234167461686L;

        String expression = ".*"; // NOI18N

        public String getFilter () {
            return expression;
        }

        public String toString () {
            return new MessageFormat (
                       NbBundle.getBundle (PackagesFilter.class).
                       getString ("CTL_Regular_filter")
                   ).format (new Object[] {expression});
        }
    }

    /**
    * Packages subfilter.
    */
    static class PackageListFilter implements Filter {
        static final long serialVersionUID = 7083525471921225108L;

        String expression = ".*"; // NOI18N

        public String getFilter () {
            return expression;
        }

        public String toString () {
            return new MessageFormat (
                       NbBundle.getBundle (PackagesFilter.class).
                       getString ("CTL_Packages_filter")
                   ).format (new Object[] {expression});
        }
    }

    /**
    * DataFilter which encapsulates Regular expression parser.
    */
    private static class InnerFilter implements DataFilter {
        static final long serialVersionUID = 7205717425719798916L;

        RE re;

        InnerFilter (String exp) {
            try {
                re = new RE (exp);
            } catch (RESyntaxException e) {
                e.printStackTrace ();
            }
        }

        public boolean acceptDataObject (DataObject d) {
            return re.match (d.getPrimaryFile ().getPackageName ('.'));
        }
    }
}

/*
 * Log
 *  10   Gandalf   1.9         1/13/00  Radko Najman    I18N
 *  9    Gandalf   1.8         12/15/99 Jan Jancura     Bug 3039 + Bug 4917
 *  8    Gandalf   1.7         12/15/99 Jan Jancura     Bug 4906
 *  7    Gandalf   1.6         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  6    Gandalf   1.5         9/13/99  Jan Jancura     Regexp implementation 
 *       changed.
 *  5    Gandalf   1.4         8/18/99  Jan Jancura     Localization
 *  4    Gandalf   1.3         8/9/99   Ian Formanek    Generated Serial Version
 *       UID
 *  3    Gandalf   1.2         6/10/99  Jan Jancura     OB settings & save of 
 *       filters
 *  2    Gandalf   1.1         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  1    Gandalf   1.0         5/12/99  Jan Jancura     
 * $
 */
