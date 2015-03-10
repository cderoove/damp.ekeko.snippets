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

package org.openide.execution;
import java.beans.*;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.util.Enumeration;

import org.openide.*;

/** Patch for backward compatibility of executors and debugger types.
*
* @author Jaroslav Tulach
*/
final class ExecutorType extends Object {
    static final class Handle extends Object implements java.io.Serializable {
        /** generated Serialized Version UID */
        static final long serialVersionUID = 7233109534462148872L;

        /** name executor */
        private String name;
        /** name of class of the executor */
        private String className;

        /** Old compatibility version.
        */
        private void readObject (ObjectInputStream ois) throws IOException, ClassNotFoundException {
            name = (String)ois.readObject ();
            className = (String)ois.readObject ();
        }

        /** Has also save the object.
        */
        private void writeObject (ObjectOutputStream oos) throws IOException {
            oos.writeObject (name);
            oos.writeObject (className);
        }

        /** Convert to the new class.
        */
        private Object readResolve () {
            ServiceType exc = null;

            // try to find the executor by name
            TopManager tm = TopManager.getDefault ();
            ServiceType.Registry r = tm.getServices ();
            exc = r.find (name);
            if (exc == null) {
                // try to find it by class
                try {
                    exc = r.find (
                              Class.forName (className, true, tm.systemClassLoader ())
                          );
                } catch (ClassNotFoundException ex) {
                }
            }
            return exc == null ? null : new org.openide.ServiceType.Handle (exc);
        }
    }
}

/*
 * Log
 *  4    Gandalf   1.3         1/16/00  Jaroslav Tulach Both inner and outer 
 *       class defined.
 *  3    Gandalf   1.2         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  2    Gandalf   1.1         9/21/99  Jaroslav Tulach If executor does not 
 *       exists => null is returned from readObject.
 *  1    Gandalf   1.0         9/19/99  Jaroslav Tulach 
 * $
 * Beta Change History:
 *  0    Tuborg    0.13        --/--/98 Jan Formanek    reflecting changes in ExecInfo (#$@$#$@%@$@%!!!!)
 */
