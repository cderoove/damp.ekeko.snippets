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

package org.openide.util;

import java.beans.*;
import java.lang.reflect.*;
import java.util.EventListener;
import java.awt.event.FocusListener;
import java.awt.event.FocusEvent;

import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

import org.openide.filesystems.*;
import org.openide.loaders.OperationListener;
import org.openide.loaders.OperationEvent;

import org.openide.nodes.*;

/** Factory for JDK 1.3. Uses java.lang.reflect.Proxy
*
* @author Jaroslav Tulach
*/
final class WeakListener13 extends Object implements WeakListener.Factory {

    public NodeListener node(NodeListener l,Object source) {
        return (NodeListener)create (NodeListener.class, l, source);
    }

    public PropertyChangeListener propertyChange(PropertyChangeListener l,Object source) {
        return (PropertyChangeListener)create (PropertyChangeListener.class, l, source);
    }

    public VetoableChangeListener vetoableChange(VetoableChangeListener l,Object source) {
        return (VetoableChangeListener)create (VetoableChangeListener.class, l, source);
    }

    public FileChangeListener fileChange(FileChangeListener l,Object source) {
        return (FileChangeListener)create (FileChangeListener.class, l, source);
    }

    public FileStatusListener fileStatus(FileStatusListener l,Object source) {
        return (FileStatusListener)create (FileStatusListener.class, l, source);
    }

    public RepositoryListener repository(RepositoryListener l,Object source) {
        return (RepositoryListener)create (RepositoryListener.class, l, source);
    }

    public DocumentListener document(DocumentListener l,Object source) {
        // this is a special listener that cannot be used with the handler
        // because DocumentEvent is not EventObject
        WeakListener.Document wl = new WeakListener.Document (l);
        wl.setSource (source);
        return wl;
    }

    public ChangeListener change(ChangeListener l,Object source) {
        return (ChangeListener) create (ChangeListener.class, l, source);
    }

    public FocusListener focus(FocusListener l,Object source) {
        return (FocusListener) create (FocusListener.class, l, source);
    }

    public OperationListener operation (OperationListener l, Object source) {
        return (OperationListener) create (OperationListener.class, l, source);
    }

    /** Creates a proxy for given class
    */
    private static Object create (Class clazz, EventListener listener, Object source) {
        ProxyListener pl = new ProxyListener (clazz, listener);
        pl.setSource (source);

        return pl.proxy;
    }

    /** Proxy interface that delegates to listeners.
    */
    private static class ProxyListener extends WeakListener implements InvocationHandler {
        /** proxy generated for this listener */
        public final Object proxy;

        /** @param listener listener to delegate to
        */
        public ProxyListener (Class c, java.util.EventListener listener) {
            super (c, listener);

            proxy = Proxy.newProxyInstance (
                        c.getClassLoader (), new Class[] { c }, this
                    );
        }

        public java.lang.Object invoke (
            Object proxy, Method method, Object[] args
        ) throws Throwable {
            if (method.getDeclaringClass () == Object.class) {
                // a method from object => call it on your self
                return method.invoke (this, args);
            }

            // listeners method
            java.util.EventObject ev = (java.util.EventObject)args[0];

            Object listener = super.get (ev);
            if (listener != null) {
                return method.invoke (listener, args);
            } else {
                return null;
            }
        }

        /** Remove method name is composed from the name of the listener.
        */
        protected String removeMethodName () {
            String name = listenerClass.getName ();
            int dot = name.lastIndexOf('.');
            name = name.substring (dot + 1);

            return "remove" + name; // NOI18N
        }

        /** To string prints class.
        */
        public String toString () {
            return super.toString () + "[" + listenerClass + "]"; // NOI18N
        }

        /** Equal is extended to equal also with proxy object.
        */
        public boolean equals (Object obj) {
            return proxy == obj || this == obj;
        }
    }
}

/*
* Log
*  5    Gandalf   1.4         1/12/00  Pavel Buzek     I18N
*  4    Gandalf   1.3         1/5/00   Jaroslav Tulach Added operation listener.
*  3    Gandalf   1.2         12/2/99  Jaroslav Tulach Performance improvements.
*  2    Gandalf   1.1         11/5/99  Jaroslav Tulach 1.3 works better.
*  1    Gandalf   1.0         11/5/99  Jaroslav Tulach 
* $
*/