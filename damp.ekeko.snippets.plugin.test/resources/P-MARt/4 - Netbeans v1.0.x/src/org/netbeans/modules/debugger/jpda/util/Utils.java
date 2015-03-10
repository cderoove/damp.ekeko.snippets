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

package org.netbeans.modules.debugger.jpda.util;

import com.sun.jdi.*;
import com.sun.jdi.event.*;
import com.sun.jdi.request.*;
import com.sun.jdi.connect.*;

import org.openide.cookies.LineCookie;
import org.openide.text.Line;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.filesystems.FileObject;
import org.openide.TopManager;

import java.util.*;


/**
* Utilities for debugger.
*
* @author Jan Jancura
*/
public class Utils extends org.netbeans.modules.debugger.support.util.Utils {


    // testing methods .........................................................................

    public static void showMethods (ReferenceType rt) {
        System.out.println ("  ============================================"); // NOI18N
        System.out.println ("  Methods for " + rt.name ()); // NOI18N
        List l = rt.methods ();
        int i, k = l.size ();
        for (i = 0; i < k; i++)
            System.out.println (((Method) l.get (i)).name () + " ; " + // NOI18N
                                ((Method) l.get (i)).signature ());

        System.out.println ("  ============================================"); // NOI18N
    }

    public static void showLinesForClass (ReferenceType rt) {
        System.out.println ("  ============================================"); // NOI18N
        System.out.println ("  Lines for " + rt.name ()); // NOI18N
        List l = null;
        try {
            l = rt.allLineLocations ();
        } catch (AbsentInformationException e) {
        }
        int i, k = l.size ();
        for (i = 0; i < k; i++)
            System.out.println ("   " + ((Location) l.get (i)).lineNumber () + " : " + // NOI18N
                                ((Location) l.get (i)).codeIndex ()
                               );

        System.out.println ("  ============================================"); // NOI18N
    }

    public static void showRequests (EventRequestManager requestManager) {
        System.out.println ("  ============================================"); // NOI18N
        System.out.println ("  Break request: " + requestManager.breakpointRequests ().size ()); // NOI18N
        System.out.println ("  Class prepare request: " + requestManager.classPrepareRequests ().size ()); // NOI18N
        System.out.println ("  Access watch request: " + requestManager.accessWatchpointRequests ().size ()); // NOI18N
        System.out.println ("  Class unload request: " + requestManager.classUnloadRequests ().size ()); // NOI18N
        System.out.println ("  Exception request: " + requestManager.exceptionRequests ().size ()); // NOI18N
        System.out.println ("  Method entry request: " + requestManager.methodEntryRequests ().size ()); // NOI18N
        System.out.println ("  Method exit request: " + requestManager.methodExitRequests ().size ()); // NOI18N
        System.out.println ("  Modif watch request: " + requestManager.modificationWatchpointRequests ().size ()); // NOI18N
        System.out.println ("  Step request: " + requestManager.stepRequests ().size ()); // NOI18N
        System.out.println ("  Thread death entry request: " + requestManager.threadDeathRequests ().size ()); // NOI18N
        System.out.println ("  Thread start request: " + requestManager.threadStartRequests ().size ()); // NOI18N
        System.out.println ("  ============================================"); // NOI18N
    }

    public static void showConnectors (List l) {
        int i, k = l.size ();
        for (i = 0; i < k; i++) showConnector ((Connector) l.get (i));
    }

    public static void showConnector (Connector connector) {
        System.out.println ("  ============================================"); // NOI18N
        System.out.println ("  Connector: " + connector); // NOI18N
        System.out.println ("    name: " + connector.name ()); // NOI18N
        System.out.println ("    description: " + connector.description ()); // NOI18N
        System.out.println ("    transport: " + connector.transport ().name ()); // NOI18N
        showProperties (connector.defaultArguments ());
        System.out.println ("  ============================================"); // NOI18N
    }

    public static void showThread (ThreadReference tr) {
        System.out.println ("  ============================================"); // NOI18N
        System.out.println ("  Thread: " + tr.name ()); // NOI18N
        System.out.println ("    status: " + tr.status ()); // NOI18N
        try {
            System.out.println ("    location: " + tr.frame (0)); // NOI18N
        } catch (Exception e) {
        }
        System.out.println ("  ============================================"); // NOI18N
    }


    private static void showProperties (Map properties) {
        Iterator i = properties.keySet ().iterator ();
        while (i.hasNext ()) {
            Object k = i.next ();
            Connector.Argument a = (Connector.Argument) properties.get (k);
            System.out.println ("    property: " + k + " > " + a.name ()); // NOI18N
            System.out.println ("      desc: " + a.description ()); // NOI18N
            System.out.println ("      mustSpecify: " + a.mustSpecify ()); // NOI18N
            System.out.println ("      value: " + a.value ()); // NOI18N
        }
    }

    public static void listGroup (String s, ThreadGroupReference g) {
        List l = g.threadGroups ();
        int i, k = l.size ();
        for (i = 0; i < k; i++) {
            System.out.println (s + "Thread Group: " + l.get (i) + " : " + // NOI18N
                                ((ThreadGroupReference)l.get (i)).name ()
                               );
            listGroup (s + "  ", (ThreadGroupReference)l.get (i)); // NOI18N
        }
        l = g.threads ();
        k = l.size ();
        for (i = 0; i < k; i++) {
            System.out.println (s + "Thread: " + l.get (i) + " : " + // NOI18N
                                ((ThreadReference)l.get (i)).name ()
                               );
        }
    }

    private static void listGroups (List g) {
        System.out.println ("  ============================================"); // NOI18N
        int i, k = g.size ();
        for (i = 0; i < k; i++) {
            System.out.println ("Thread Group: " + g.get (i) + " : " + // NOI18N
                                ((ThreadGroupReference)g.get (i)).name ()
                               );
            listGroup ("  ", (ThreadGroupReference)g.get (i)); // NOI18N
        }
        System.out.println ("  ============================================"); // NOI18N
    }
}

/*
 * Log
 *  5    Gandalf   1.4         1/13/00  Daniel Prusa    NOI18N
 *  4    Gandalf   1.3         11/29/99 Jan Jancura     
 *  3    Gandalf   1.2         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  2    Gandalf   1.1         9/2/99   Jan Jancura     
 *  1    Gandalf   1.0         7/13/99  Jan Jancura     
 * $
 */
