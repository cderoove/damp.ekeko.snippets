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

package org.netbeans.modules.apisupport.beanbrowser;

import java.lang.reflect.Method;
import java.util.*;

import org.openide.cookies.*;
import org.openide.src.*;
import org.openide.filesystems.Repository;
import org.openide.nodes.*;
import org.openide.loaders.*;
import org.openide.util.*;

class CookieKids extends Children.Keys {

    private Method method;
    private Object holder;

    public CookieKids (Method cookieMethod, Object cookieHolder) {
        method = cookieMethod;
        holder = cookieHolder;
    }

    protected void addNotify () {
        RequestProcessor.postRequest (new Runnable () {
                                          public void run () {
                                              java.util.Map cookies = new HashMap ();
                                              Class[] clazzes = new Class[] {
                                                                    ArgumentsCookie.class,
                                                                    CloseCookie.class,
                                                                    CompilerCookie.Build.class,
                                                                    CompilerCookie.Clean.class,
                                                                    CompilerCookie.Compile.class,
                                                                    ConnectionCookie.class,
                                                                    ConnectionCookie.Listener.class,
                                                                    DebuggerCookie.class,
                                                                    EditCookie.class,
                                                                    EditorCookie.class,
                                                                    ElementCookie.class,
                                                                    ExecCookie.class,
                                                                    FilterCookie.class,
                                                                    Index.class,
                                                                    InstanceCookie.class,
                                                                    InstanceCookie.Origin.class,
                                                                    LineCookie.class,
                                                                    OpenCookie.class,
                                                                    PrintCookie.class,
                                                                    ProjectCookie.class,
                                                                    SaveCookie.class,
                                                                    SourceCookie.class,
                                                                    SourceCookie.Editor.class, // subclass
                                                                    ViewCookie.class,
                                                                    XMLDataObject.Processor.class,
                                                                    InitializerElement.class,
                                                                    SourceElement.class,
                                                                    ClassElement.class,
                                                                    FieldElement.class,
                                                                    ConstructorElement.class,
                                                                    MethodElement.class, // subclass
                                                                    DataObject.class,
                                                                    DataFolder.class, // subclass
                                                                    Repository.class,
                                                                };
                                              for (int i = 0; i < clazzes.length; i++) {
                                                  try {
                                                      Node.Cookie cookie = (Node.Cookie) method.invoke (holder, new Object[] { clazzes[i] });
                                                      if (cookie != null) {
                                                          Set which = (Set) cookies.get (cookie);
                                                          if (which == null)
                                                              cookies.put (cookie, which = new HashSet ());
                                                          which.add (clazzes[i]);
                                                      }
                                                  } catch (Exception e) {
                                                      e.printStackTrace ();
                                                  }
                                              }
                                              setKeys0 (cookies.entrySet ());
                                          }
                                      });
    }
    private void setKeys0 (Collection c) {
        setKeys (c);
    }

    protected void removeNotify () {
        setKeys (Collections.EMPTY_SET);
    }

    protected Node[] createNodes (Object key) {
        java.util.Map.Entry entry = (java.util.Map.Entry) key;
        Object cookie = entry.getKey ();
        Set types = (Set) entry.getValue ();
        Node n = PropSetKids.makeObjectNode (cookie);
        StringBuffer dname = new StringBuffer ();
        boolean first = true;
        Iterator it = types.iterator ();
        while (it.hasNext ()) {
            if (first)
                first = false;
            else
                dname.append (" / ");
            Class type = (Class) it.next ();
            String name = type.getName ();
            int idx = name.lastIndexOf ('.');
            if (idx != -1) name = name.substring (idx + 1);
            dname.append (name.replace ('$', '.'));
        }
        dname.append (" = ");
        dname.append (n.getDisplayName ());
        n.setDisplayName (dname.toString ());
        return new Node[] { n };
    }

}

/*
 * Log
 *  2    Gandalf   1.1         1/22/00  Jesse Glick     More pleasant cookie 
 *       display.
 *  1    Gandalf   1.0         12/23/99 Jesse Glick     
 * $
 */
