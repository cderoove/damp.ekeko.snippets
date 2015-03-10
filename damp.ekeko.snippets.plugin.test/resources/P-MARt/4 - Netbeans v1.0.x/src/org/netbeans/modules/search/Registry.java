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

package org.netbeans.modules.search;

import java.util.*;

import org.openide.*;

import org.openidex.search.*;

import org.netbeans.modules.search.types.*;

/**
 * Service types registry routines.
 *
 * @author  pkuzel
 * @version 
 */
public class Registry extends Object {

    /** Creates new Registry */
    public Registry() {
    }

    /**
    * @param list of classes that 
    */
    public static void reorderBy(Class[] types) {

        ServiceType.Registry registry = TopManager.getDefault().getServices();
        Vector registered = new Vector(registry.getServiceTypes());

        List newList = new ArrayList();

        for ( int i=0; i< types.length; i++ ) {
            Iterator it = new Vector(registered).iterator();

            while (it.hasNext()) {
                Object next = it.next();
                if (types[i].isInstance(next)) {
                    newList.add(next);
                    registered.remove(next);
                }
            }

        }

        // add rest of them in original order
        newList.addAll(registered);

        registry.setServiceTypes(newList);
    }

    public static boolean exist(SearchType obj) {
        ServiceType.Registry registry = TopManager.getDefault().getServices();
        Enumeration en = registry.services(obj.getClass());

        while (en.hasMoreElements()) {
            SearchType next = (SearchType) en.nextElement();

            if (next.getName().equals(obj.getName()))
                return true;
        }

        return false;
    }

    /***/
    public static void append(SearchType obj) {
        ServiceType.Registry registry = TopManager.getDefault().getServices();
        List result = registry.getServiceTypes();
        result.add(obj);
        registry.setServiceTypes(result);
    }

    /**
    * Remove a service matching template.
    * @param obj service template - used name and class
    */
    public static void remove(SearchType obj) {
        ServiceType.Registry registry = TopManager.getDefault().getServices();
        List result = registry.getServiceTypes();

        ArrayList ret = new ArrayList();

        Iterator it = result.iterator();
        while (it.hasNext()) {
            ServiceType next = (ServiceType) it.next();

            if ( ! next.getName().equals(obj.getName()) ||
                    ! next.getClass().equals(obj.getClass()) )
                ret.add(next);
        }

        registry.setServiceTypes(ret);
    }

    /**
    * @param args the command line arguments
    */
    public static void main (String args[]) {
        reorderBy(new Class[] {ObjectNameType.class, FullTextType.class} );
        append(new FullTextType());
    }

    public static void list(Iterator it) {
        System.err.println("Listing: ");
        while(it.hasNext()) {
            System.err.println(it.next().toString());
        }
    }
}


/*
* Log
*  3    Gandalf   1.2         1/5/00   Petr Kuzel      Margins used. Help 
*       contexts.
*  2    Gandalf   1.1         1/4/00   Petr Kuzel      Bug hunting.
*  1    Gandalf   1.0         12/23/99 Petr Kuzel      
* $ 
*/ 

