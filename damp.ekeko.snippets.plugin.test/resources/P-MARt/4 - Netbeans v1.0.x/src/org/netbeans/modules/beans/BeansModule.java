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

package org.netbeans.modules.beans;

import java.io.File;
import java.lang.reflect.Method;

import org.openide.src.nodes.FilterFactory;
import org.openide.modules.ModuleInstall;
import org.openide.TopManager;
import org.openide.util.NbBundle;

/** Class for initializing BeansModule module on IDE startup.
 *
 * @author Petr Hrebejk
 */
public class BeansModule extends ModuleInstall {


    static final long serialVersionUID =-7687075984661316111L;

    private transient FilterFactory exJava = null;
    private transient FilterFactory exClazz = null;
    private transient FilterFactory brJava = null;
    private transient FilterFactory brClazz = null;


    /** Installs the templates used in JavaBeans Module and
     * calls restored()
     */
    public void installed() {
        copyTemplates();
        restored();
    }

    /** Called on IDE startup. Dynamicaly registers new
     * {@link org.openie.src.nodes.FilterFactory FilterFactories} into
     * JavaDataObject and ClazzDataObject.
     */
    public void restored() {

        invokeDynamic( "org.netbeans.modules.java.JavaDataObject", // NOI18N
                       "addExplorerFilterFactory", // NOI18N
                       exJava = new PatternsExplorerFactory( true ) );
        invokeDynamic( "org.netbeans.modules.clazz.ClassDataObject", // NOI18N
                       "addExplorerFilterFactory", // NOI18N
                       exClazz = new PatternsExplorerFactory( false ) );
        invokeDynamic( "org.netbeans.modules.java.JavaDataObject", // NOI18N
                       "addBrowserFilterFactory", // NOI18N
                       brJava = new PatternsBrowserFactory( true ) );
        invokeDynamic( "org.netbeans.modules.clazz.ClassDataObject", // NOI18N
                       "addBrowserFilterFactory", // NOI18N
                       brClazz = new PatternsBrowserFactory( false ) );
    }

    /** Called when the module is uninstalled */
    public void uninstalled() {
        invokeDynamic( "org.netbeans.modules.java.JavaDataObject", // NOI18N
                       "removeExplorerFilterFactory", // NOI18N
                       exJava );
        invokeDynamic( "org.netbeans.modules.clazz.ClassDataObject", // NOI18N
                       "removeExplorerFilterFactory", // NOI18N
                       exClazz );
        invokeDynamic( "org.netbeans.modules.java.JavaDataObject", // NOI18N
                       "removeBrowserFilterFactory", // NOI18N
                       brJava );
        invokeDynamic( "org.netbeans.modules.clazz.ClassDataObject", // NOI18N
                       "removeBrowserFilterFactory", // NOI18N
                       brClazz );
    }

    // UTILITY METHODS ----------------------------------------------------------

    /** Dynamicaly registers ElementFactory.
     * @param className Name of class which registers the factories.
     * @param methodName Name of method for registering factories.
     * @param factory The factory to register.
     */
    private void invokeDynamic( String className, String methodName, FilterFactory factory ) {

        try {
            Class dataObject = TopManager.getDefault().systemClassLoader().loadClass( className );

            if ( dataObject == null )
                return;

            Method method = dataObject.getDeclaredMethod( methodName, new Class[] { FilterFactory.class }  );
            if ( method == null )
                return;

            method.invoke( null, new Object[] { factory } );
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


    /** Installs templates used by JavaBeans Module into System folder.
     */
    private void copyTemplates () {
        try {
            org.openide.filesystems.FileUtil.extractJar (
                org.openide.TopManager.getDefault ().getPlaces ().folders().templates ().getPrimaryFile (),
                NbBundle.getLocalizedFile ("org.netbeans.modules.beans.resources.templates", "jar").openStream () // NOI18N
            );
        } catch (java.io.IOException e) {
            org.openide.TopManager.getDefault ().notifyException (e);
        }
    }


}


/*
 * Log
 *  12   Gandalf   1.11        1/16/00  Jesse Glick     Localized jars.
 *  11   Gandalf   1.10        1/12/00  Petr Hrebejk    i18n  
 *  10   Gandalf   1.9         1/4/00   Petr Hrebejk    Various bugfixes - 5036,
 *       5044, 5045
 *  9    Gandalf   1.8         11/27/99 Patrik Knakal   
 *  8    Gandalf   1.7         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  7    Gandalf   1.6         10/1/99  Petr Hrebejk    org.openide.modules.ModuleInstall
 *        changed to class + some methods added
 *  6    Gandalf   1.5         9/13/99  Petr Hrebejk    Creating multiple 
 *       Properties/EventSet with the same name vorbiden. Forms made i18n
 *  5    Gandalf   1.4         7/26/99  Petr Hrebejk    Better implementation of
 *       patterns resolving
 *  4    Gandalf   1.3         7/16/99  Petr Hrebejk    Template installation 
 *       added
 *  3    Gandalf   1.2         7/9/99   Petr Hrebejk    Factory chaining fix
 *  2    Gandalf   1.1         7/1/99   Jan Jancura     Object Browser support
 *  1    Gandalf   1.0         6/28/99  Petr Hrebejk    
 * $ 
 */ 