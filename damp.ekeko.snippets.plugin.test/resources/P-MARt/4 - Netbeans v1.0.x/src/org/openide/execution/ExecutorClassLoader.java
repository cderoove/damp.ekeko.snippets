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

/** Classloader that makes the first loaded class public.
* So its main method can be accessed.
*
* @author Jaroslav Tulach
*/
class ExecutorClassLoader extends NbClassLoader {
    /** is to be access bit of "root" class set?
    */
    private boolean accessBitEnabled = true;

    /** Allows subclasses to do special actions when defining class from
    * byte array.
    * @param name name of class
    * @param data byte array for such class
    * @return the defined class
    */
    protected Class defineFromData (String name, byte[] data)
    throws ClassFormatError, NoClassDefFoundError {
        if (accessBitEnabled) {
            accessBitEnabled = false;
            setAccessBit (data);
        }
        return defineClass(name, data, 0, data.length);
    }


    /** Sets access bit of class in the array to public
    * @param classdata an array with a class
    */
    private static void setAccessBit(byte[] classdata) {
        if (classdata == null || classdata.length < 14) return;
        int magic = (0xff & classdata[0]) << 24 | (0xff & classdata[1]) << 16 | (0xff & classdata[2]) << 8 | 0xff & classdata[3];
        int minor = (0xff & classdata[4]) << 8 | 0xff & classdata[5];
        int major = (0xff & classdata[6]) << 8 | 0xff & classdata[7];
        if(magic != 0xcafebabe || major != 45 || minor > 3)
            return;
        int poolcount = (0xff & classdata[8]) << 8 | 0xff & classdata[9];
        int i = 10;
        for (poolcount--; poolcount > 0; poolcount--) {
            switch(classdata[i] & 0xff) { // see VM Spec The class File Format
            case 7: /* CONSTANT_Class */
            case 8: /* CONSTANT_String */
                // skip two bytes
                i += 3;
                break;

                // four bytes items
            case 3: /* CONSTANT_Integer */
            case 4: /* CONSTANT_Float */
            case 9: /* CONSTANT_Fieldref */
            case 10: /* CONSTANT_Methodref */
            case 11: /* CONSTANT_InterfaceMethodref */
            case 12: /* CONSTANT_NameAndType */
                i += 5;
                break;

                // eight byte items
            case 5: /* CONSTANT_Long */
            case 6: /* CONSTANT_Double */
                // see specification !!!
                poolcount--;
                i += 9;
                break;

            case 1: /* CONSTANT_Utf8 */
                ++i;
                int utflength = (0xff & classdata[i]) << 8 | 0xff & classdata[i + 1];
                i += utflength + 2;
                break;

                // case 2: // not in specification
            default:
                return;
            }
        }
        i++;
        classdata[i] |= 0x01;
    }

}

/*
 * Log
 *  4    Gandalf   1.3         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  3    Gandalf   1.2         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  2    Gandalf   1.1         3/26/99  Jaroslav Tulach 
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
