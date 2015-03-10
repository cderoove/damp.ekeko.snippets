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

package org.netbeans.modules.projects;


import org.openide.TopManager;
import java.lang.reflect.Method;
/**
 *
 * @author  phrebejk
 * @version 
 */
public class JavaDocHack extends Object {

    /** Creates new Hack */
    public JavaDocHack() {
    }

    public static void installJavaDoc() {
        invokeDynamic( "org.netbeans.modules.javadoc.JavadocModule", "installJavadocDirectories" ); // NOI18N
    }

    /** Dynamicaly invokes a method
     */
    private static void invokeDynamic( String className, String methodName  ) {

        try {
            Class dataObject = TopManager.getDefault().systemClassLoader().loadClass( className );

            if ( dataObject == null )
                return;

            Method method = dataObject.getDeclaredMethod( methodName, new Class[] {}  );
            if ( method == null )
                return;

            method.invoke( null, new Object[] {} );
        }
        catch ( java.lang.ClassNotFoundException e ) {
        }
        catch ( java.lang.NoSuchMethodException e ) {
        }
        catch ( java.lang.IllegalAccessException e ) {
        }
        catch ( java.lang.reflect.InvocationTargetException e ) {
        }
    }

}