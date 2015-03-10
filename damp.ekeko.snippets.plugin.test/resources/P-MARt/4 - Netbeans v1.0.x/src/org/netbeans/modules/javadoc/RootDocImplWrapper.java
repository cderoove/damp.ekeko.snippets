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
import java.lang.reflect.Constructor;
import java.util.List;
import sun.tools.util.ModifierFilter;

import com.sun.javadoc.*;

import org.openide.TopManager;
import org.openide.windows.OutputWriter;


/** This class serves as workaround for package private accessibilty of class
 * <CODE>com.sun.tools.javadoc.RootDocImpl</CODE>. It creates new instance of RootDocImpl
 * through reflection Api. It also calls <CODE>static option(String[][])</CODE> 
 * method for that class.
 *
 * @author Petr Hrebejk
 */
public class RootDocImplWrapper extends Object implements RootDoc {

    private RootDoc     rdiInstance;
    private Class       rdiClazz;
    private Constructor rdiConstructor;
    private Method      rdiOptions;

    private OutputWriter  out;
    private OutputWriter  err;

    static final long serialVersionUID =-7498848877297162664L;
    public RootDocImplWrapper ( Object env, List userClasses, List userPkgs, ModifierFilter showAccess, List options ) {
        try {
            // Get the class
            rdiClazz = Class.forName( "com.sun.tools.javadoc.RootDocImpl" ); // NOI18N

            // Get the constructor and make it accessible
            Class envClazz = Class.forName( "com.sun.tools.javadoc.Env" ); // NOI18N
            Class constructorParams[] = new Class[] { envClazz, java.util.List.class, java.util.List.class, sun.tools.util.ModifierFilter.class, java.util.List.class };
            rdiConstructor = rdiClazz.getDeclaredConstructor( constructorParams );
            rdiConstructor.setAccessible( true );

            // Get the option method and make it accesible
            Class optionsParams[] = new Class[] { java.util.List.class };
            rdiOptions = rdiClazz.getDeclaredMethod( "options", optionsParams ); // NOI18N
            rdiOptions.setAccessible( true );

            // Call the constructor
            Object constructorArgs[] = new Object[] { env, userClasses, userPkgs, showAccess, options };
            rdiInstance = (RootDoc) rdiConstructor.newInstance( constructorArgs );
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
        catch (InstantiationException e ) {
            TopManager.getDefault().notifyException( e );
        }
    }

    /**
    *@return Instance of RootDocImpl casted to interface com.sun.javadoc.RootDoc.
    */
    public RootDoc getRootDoc() {
        return rdiInstance;
    }

    /** Sets output streams in otput tab
    */
    public void setIO ( OutputWriter out, OutputWriter err ) {
        this.out = out;
        this.err = err;
    }

    /** Dynamic invocation of RootDocImpl.options( options ) )
    */

    public String[][] options( List options ) {
        Object optionsArgs[] = new Object[] { options };

        try {
            return (String[][])rdiOptions.invoke( rdiClazz, optionsArgs );
        }
        catch (IllegalAccessException e) {
            TopManager.getDefault().notifyException( e );
            return null;
        }
        catch (java.lang.reflect.InvocationTargetException e) {
            TopManager.getDefault().notifyException( e );
            //System.out.println(e + " : " + e.getTargetException()); // NOI18N
            //e.getTargetException().printStackTrace();
            return null;
        }

    }

    // Wraper implementation Doc

    public String commentText() { return rdiInstance.commentText (); }
    public Tag[] tags() { return rdiInstance.tags (); }
    public Tag[] tags(String tagname) { return rdiInstance.tags (tagname);}
    public SeeTag[] seeTags() { return rdiInstance.seeTags ();}
    public Tag[] inlineTags() { return rdiInstance.inlineTags();}
    public Tag[] firstSentenceTags() { return rdiInstance.firstSentenceTags();}
    public String getRawCommentText() { return rdiInstance.getRawCommentText();}
    public void setRawCommentText(String rawDocumentation) { rdiInstance.setRawCommentText(rawDocumentation);}
    public String name() { return rdiInstance.name();}
    public int compareTo(Object obj) { return rdiInstance.compareTo(obj);}
    public boolean isField() { return rdiInstance.isField();}
    public boolean isMethod() { return rdiInstance.isMethod();}
    public boolean isConstructor() { return rdiInstance.isConstructor();}
    public boolean isInterface() { return rdiInstance.isInterface();}
    public boolean isException() { return rdiInstance.isException();}
    public boolean isError() { return rdiInstance.isError();}
    public boolean isOrdinaryClass(){ return rdiInstance.isOrdinaryClass();}
    public boolean isClass() { return rdiInstance.isClass();}
    public boolean isIncluded() { return rdiInstance.isIncluded();}

    // Wraper implemantation RootDoc

    public String[][] options() { return rdiInstance.options ();}
    public PackageDoc[] specifiedPackages() { return rdiInstance.specifiedPackages();}
    public ClassDoc[] specifiedClasses()  { return rdiInstance.specifiedClasses();}
    public ClassDoc[] classes()  { return rdiInstance.classes();}
    public PackageDoc packageNamed(String name)  { return rdiInstance.packageNamed(name);}
    public ClassDoc classNamed(String qualifiedName) { return rdiInstance.classNamed(qualifiedName);}

    // Implementation DocErrorReporter

    public void printError(String msg) {
        err.println ( msg );
        err.flush();
        //Res.printError(msg);
    }

    public void printWarning(String msg) {
        err.println ( msg );
        err.flush();
        //Res.printWarning(msg);
    }

    public void printNotice(String msg) {
        out.println ( msg );
        out.flush();
        //Res.printNotice(msg);
    }
}

/*
 * Log
 *  8    Gandalf   1.7         1/13/00  Petr Hrebejk    i18n mk3  
 *  7    Gandalf   1.6         1/12/00  Petr Hrebejk    i18n
 *  6    Gandalf   1.5         1/10/00  Petr Hrebejk    Bug 4747 - closing of 
 *       output tab fixed
 *  5    Gandalf   1.4         11/27/99 Patrik Knakal   
 *  4    Gandalf   1.3         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  3    Gandalf   1.2         6/11/99  Petr Hrebejk    
 *  2    Gandalf   1.1         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  1    Gandalf   1.0         4/23/99  Petr Hrebejk    
 * $ 
 */ 