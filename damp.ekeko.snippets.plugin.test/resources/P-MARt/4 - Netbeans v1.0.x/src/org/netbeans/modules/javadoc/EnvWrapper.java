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

package org.netbeans.modules.javadoc;

import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.lang.reflect.Constructor;
import java.util.List;
import java.util.HashMap;
import java.util.Enumeration;
import java.util.LinkedList;

import sun.tools.util.ModifierFilter;
import sun.tools.java.ClassPath;
import com.sun.javadoc.RootDoc;

import org.openide.TopManager;
import org.netbeans.modules.java.CoronaEnvironment;

/** This class serves as workaround for package private accessibilty of class
 * <CODE>com.sun.tools.javadoc.Env</CODE>. It creates new instance of RootDocImpl
 * through reflection Api. It also calls <CODE>static option(String[][])</CODE> 
 * method for that class.
 *
 * @author Petr Hrebejk
 */
public class EnvWrapper extends Object {

    private Object      envInstance;
    private Class       envClazz;
    private Constructor envCreate;
    private Method      envGetClasses;


    public EnvWrapper ( ClassPath srcPath, ClassPath binPath, int flags, String encoding ) {
        try {
            initStaticFields();

            envClazz = Class.forName( "com.sun.tools.javadoc.Env" ); // NOI18N
            Class createParams[] = new Class[] { ClassPath.class, ClassPath.class };
            envCreate = envClazz.getDeclaredConstructor( createParams );
            envCreate.setAccessible( true );
            Object createArgs[] = new Object[] { srcPath, binPath };
            envInstance = envCreate.newInstance ( createArgs );
        }
        catch (InstantiationException e ) {
            TopManager.getDefault().notifyException( e );
        }
        catch (ClassNotFoundException e) {
            TopManager.getDefault().notifyException( e );
        }
        catch (NoSuchMethodException e) {
            TopManager.getDefault().notifyException( e );
        }
        catch (java.lang.reflect.InvocationTargetException e) {
            TopManager.getDefault().notifyException( e );
            //System.out.println(e + " : " + e.getTargetException()); // NOI18N
            //e.getTargetException().printStackTrace();
        }
        catch (IllegalAccessException e) {
            TopManager.getDefault().notifyException( e );
        }
    }


    /** Initialize static fields. In Sun's Javadoc classes.
    */
    private void initStaticFields() {
        setStaticField( "com.sun.tools.javadoc.ClassDocImpl", "classMap", new HashMap()); // NOI18N
        setStaticField( "com.sun.tools.javadoc.ClassDocImpl", "constructionCompletionQueue", new LinkedList()); // NOI18N

        setStaticField( "com.sun.tools.javadoc.ConstructorDocImpl", "map", new HashMap()); // NOI18N
        setStaticField( "com.sun.tools.javadoc.FieldDocImpl", "map", new HashMap()); // NOI18N
        setStaticField( "com.sun.tools.javadoc.MethodDocImpl", "map", new HashMap()); // NOI18N
        setStaticField( "com.sun.tools.javadoc.PackageDocImpl", "packageMap", new HashMap()); // NOI18N

        setStaticField( "com.sun.tools.javadoc.RootDocImpl", "collator", null ); // NOI18N
        setStaticField( "com.sun.tools.javadoc.RootDocImpl", "locale", null ); // NOI18N
        setStaticField( "com.sun.tools.javadoc.RootDocImpl", "localeName", new String("")); // NOI18N

    }

    /** Sets field in class on value
    */
    void setStaticField( String className, String fieldName, Object value) {
        try {
            Class clazz = Class.forName( className );
            Field field = clazz.getDeclaredField( fieldName );
            field.setAccessible( true );
            field.set( clazz, value );
        }
        catch (ClassNotFoundException e) {
            TopManager.getDefault().notifyException( e );
        }
        catch (IllegalAccessException e) {
            TopManager.getDefault().notifyException( e );
            //return null;
        }
        catch (NoSuchFieldException e) {
            TopManager.getDefault().notifyException( e );
        }
    }

    /** Get the instnce of Env
    *@return Instance of Env 
    */
    public Object getEnv() {
        return envInstance;
    }

    /** Calls getClasses on Evn instance
    */
    public Enumeration getClasses() {
        Class args[] =  new Class[] {};

        try {
            return (Enumeration) envGetClasses.invoke( envInstance, args );
        }
        catch (IllegalAccessException e) {
            TopManager.getDefault().notifyException( e );
            return null;
        }
        catch (java.lang.reflect.InvocationTargetException e) {
            TopManager.getDefault().notifyException( e );
            // System.out.println(e + " : " + e.getTargetException()); // NOI18N
            // e.getTargetException().printStackTrace();
            return null;
        }
    }

    /** Copy all fields of CoronaEnvironment to instance of Env.
    */
    public void copyCoronaEnvironment( CoronaEnvironment src ) {
        copyFields( envClazz, envInstance, CoronaEnvironment.class, src );
        /*
        try {
          copyField( Class.forName( "sun.tools.java.Environment" ), envInstance, src, "Env" );
    }
        catch (ClassNotFoundException e) {
          System.out.println(e);
    }
        */
    }

    /** Copy one field in Env
     */
    private void copyField( Class clazz, Object tObj, Object sObj, String name )
    {
        Field   field;
        Object  sval;


        try {
            field = clazz.getDeclaredField( name );
            field.setAccessible( true );
            sval = field.get( sObj );
            field.set( tObj, sval );
        }
        catch (NoSuchFieldException e) {
            TopManager.getDefault().notifyException( e );
        }
        catch (IllegalAccessException e) {
            TopManager.getDefault().notifyException( e );
        }
    }

    /** Copy all fields from CoronaEnvironment to instanve of Env
     */

    private void copyFields( Class tClazz, Object tObj, Class sClazz, Object sObj ) {
        Field   sField;
        Object  sval;

        do {
            try {
                Field tFields[] = tClazz.getDeclaredFields();

                for( int i = 0; i < tFields.length; i++ ) {
                    tFields[i].setAccessible( true );
                    try {
                        sField = sClazz.getDeclaredField( tFields[i].getName() );
                        sField.setAccessible( true );
                        sval = sField.get( sObj );
                        tFields[i].set( tObj, sval );
                    }
                    catch (NoSuchFieldException e) {
                        TopManager.getDefault().notifyException( e );
                    }
                }
            }
            catch (IllegalAccessException e) {
                TopManager.getDefault().notifyException( e );
            }
            sClazz = sClazz.getSuperclass();
        }
        while ( (tClazz = tClazz.getSuperclass()) != java.lang.Object.class);

    }

    /** Make EnvInstance gc'able.
    */
    protected void finalize() throws Throwable {
        envInstance = null;
        super.finalize();
    }

}

/*
 * Log
 *  6    Gandalf   1.5         1/13/00  Petr Hrebejk    i18n mk3  
 *  5    Gandalf   1.4         1/12/00  Petr Hrebejk    i18n
 *  4    Gandalf   1.3         1/11/00  Petr Hrebejk    setStaticField made 
 *       package private
 *  3    Gandalf   1.2         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  2    Gandalf   1.1         6/11/99  Petr Hrebejk    
 *  1    Gandalf   1.0         4/23/99  Petr Hrebejk    
 * $ 
 */ 