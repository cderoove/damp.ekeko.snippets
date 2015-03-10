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

package org.openide.util.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.io.ObjectInput;
import java.io.ByteArrayInputStream;
import java.util.Stack;
import java.lang.reflect.InvocationTargetException;

import org.openide.TopManager;
import org.openide.util.Utilities;

/**
*
* note: keep method resolveObject consistent with NbObjectOutputStream.replaceObject
*/
public class NbObjectInputStream extends ObjectInputStream {
    /**
    * @param <code>is</code> is an underlying InputStream
    */
    public NbObjectInputStream(InputStream is) throws IOException {
        super (is);
        try {
            enableResolveObject (true);
        } catch (SecurityException ex) {
            throw new IOException (ex.toString ());
        }
    }

    /** Calls super class and if it fails via ClassNotFoundException, try load it
    * using NbClassLoader.
    */
    protected Class resolveClass(ObjectStreamClass v) throws IOException, ClassNotFoundException {
        // mangle name
        return Class.forName(Utilities.translate(v.getName()), false, getNBClassLoader());
    }

    /** Lazy create default NB classloader for use during deserialization. */
    private static ClassLoader getNBClassLoader() {
        TopManager top = TopManager.getDefault ();
        return top == null ? ClassLoader.getSystemClassLoader () : top.currentClassLoader ();
    }

    /** Reads an object from the given object input.
    * The object had to be saved by writeSafely method.
    *
    * @param oi object input
    * @return the read object
    * @exception IOException if IO error occured
    * @exception SafeException if the operation failed but the stream is ok
    *    for further reading
    * @see NbObjectOutputStream#writeSafely
    */
    public static Object readSafely (ObjectInput oi)
    throws IOException {
        int size = oi.readInt ();
        byte[] byteArray = new byte [size];
        oi.readFully (byteArray, 0, size);

        try {
            ByteArrayInputStream bis = new ByteArrayInputStream (byteArray);
            NbObjectInputStream ois = new NbObjectInputStream (bis);
            Object obj = ois.readObject ();
            bis.close ();
            return obj;
        } catch (Throwable exc) {
            // rethrow if death
            if (exc instanceof ThreadDeath)
                throw (ThreadDeath)exc;
            // encapsulate all exceptions into safe exception
            if (exc instanceof Exception)
                throw new SafeException ((Exception)exc);
            else
                throw new SafeException (new InvocationTargetException (exc));
        }
    }

    /** Skips an object from the given object input without loading it.
    * The object had to be saved by writeSafely method.
    *
    * @param oi object input
    * @exception IOException if IO error occured
    * @see NbObjectOutputStream#writeSafely
    * @see NbObjectOutputStream#readSafely
    */
    public static void skipSafely (ObjectInput oi)
    throws IOException {
        int size = oi.readInt ();
        oi.skip (size);
    }

}

/*
 * Log
 *  12   Gandalf   1.11        4/14/00  Ales Novak      repackaging
 *  11   Gandalf   1.10        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  10   Gandalf   1.9         8/14/99  David Simonek   now all exceptions 
 *       during read / write are catched and encapsulated  into SafeException
 *  9    Gandalf   1.8         8/3/99   Ian Formanek    Added method skipSafely,
 *       removed obsoleted comments
 *  8    Gandalf   1.7         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  7    Gandalf   1.6         5/31/99  Jaroslav Tulach Works outside netbeans 
 *       too.
 *  6    Gandalf   1.5         4/19/99  Jesse Glick     [JavaDoc]
 *  5    Gandalf   1.4         4/13/99  Ales Novak      resolveClass changed
 *  4    Gandalf   1.3         3/26/99  Jaroslav Tulach 
 *  3    Gandalf   1.2         1/20/99  Ales Novak      
 *  2    Gandalf   1.1         1/20/99  Petr Hamernik   
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
