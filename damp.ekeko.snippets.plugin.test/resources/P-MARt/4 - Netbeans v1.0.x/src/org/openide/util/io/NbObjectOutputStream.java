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

import java.awt.Frame;
import java.awt.Image;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.ObjectOutput;
import java.io.ByteArrayOutputStream;

import java.lang.reflect.InvocationTargetException;

/**
*
* note: keep method NbObjectInputStream.resolveObject
* consistent with replaceObject method
*/
public class NbObjectOutputStream extends ObjectOutputStream {
    /**
    * @param os is an underlying outputstream
    */
    public NbObjectOutputStream(OutputStream os) throws IOException {
        super (os);
        try {
            enableReplaceObject (true);
        } catch (SecurityException ex) {
            throw new IOException (ex.toString ());
        }
    }

    /**
    * @param obj is an Object to be checked for replace
    */
    public Object replaceObject (Object obj) throws IOException {
        if (obj instanceof Image) {
            return null;
            // [LIGHT]
            // additional code needed for full version
        }
        return super.replaceObject(obj);
    }

    /** Writes an object safely to the object output. Can be read by readSafely.
    * @param oo object output to write to
    * @param obj the object to write
    * @exception SafeException if the obj fails to be serialized but
    *
    * @exception IOException if something fails
    */
    public static void writeSafely (ObjectOutput oo, Object obj)
    throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream (200);
        try {
            NbObjectOutputStream oos = new NbObjectOutputStream (bos);
            oos.writeObject (obj);
            oos.flush ();
            bos.close ();
        } catch (Throwable exc) {
            // rethrow if death
            if (exc instanceof ThreadDeath)
                throw (ThreadDeath)exc;
            // exception during safe of the object
            // encapsulate all exceptions into safe exception
            oo.writeInt (0);
            if (exc instanceof Exception)
                throw new SafeException ((Exception)exc);
            else
                throw new SafeException (new InvocationTargetException (exc));
        }

        oo.writeInt (bos.size ());
        oo.write (bos.toByteArray ());
    }

}

/*
 * Log
 *  8    Gandalf   1.7         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  7    Gandalf   1.6         8/14/99  David Simonek   now all exceptions 
 *       during read / write are catched and encapsulated  into SafeException
 *  6    Gandalf   1.5         8/3/99   Ian Formanek    Removed obsoleted  
 *       code/comments
 *  5    Gandalf   1.4         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  4    Gandalf   1.3         4/13/99  Ales Novak      the same format as for 
 *       input stream
 *  3    Gandalf   1.2         1/20/99  Ales Novak      
 *  2    Gandalf   1.1         1/20/99  Petr Hamernik   
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
