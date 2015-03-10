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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Enumeration;
import java.util.List;
import java.util.ResourceBundle;
import java.text.MessageFormat;
import java.net.URL;
import java.lang.reflect.Field;

import sun.tools.util.ModifierFilter;
import sun.tools.java.ClassDeclaration;
import sun.tools.java.MemberDefinition;
import sun.tools.java.Constants;
import sun.tools.java.ClassPath;
import com.sun.javadoc.RootDoc;

import org.openide.nodes.Node;
import org.openide.TopManager;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

import org.netbeans.modules.java.JavaDataObject;
import org.netbeans.modules.java.CoronaEnvironment;
import org.netbeans.modules.java.CoronaClassPath;
import org.netbeans.modules.java.CoronaClassFile;
import org.netbeans.modules.java.ErrConsumer;
import org.netbeans.modules.javadoc.settings.JavadocSettings;
import org.openide.filesystems.FileObject;

/** This class provides internal access to the Javadoc
 *
 * @author Petr Hrebejk
 */
public class JavadocInvoker extends Thread {

    /** The ResourceBundle */
    private static final ResourceBundle bundle = NbBundle.getBundle( JavadocInvoker.class );

    static final String oneoneDocletClassName = "com.sun.tools.doclets.oneone.OneOne"; // NOI18N
    static final String standardDocletClassName = "com.sun.tools.doclets.standard.Standard"; // NOI18N

    /** Stop javadoc running more than once
    */
    static private boolean isRunning = false;

    /** Ouput Tab and output streams for Javadoc
    */
    static InputOutput   ioTab = null;
    static OutputWriter  out = null;
    static OutputWriter  err = null;


    /** The environment where all sources will be parsed in.
    */
    protected CoronaEnvironment   ce;

    /** source path and classpath determined by current contens of repository
    */
    private CoronaClassPath srcPath = null;
    private CoronaClassPath binPath = null;

    /** Options for Javadoc run
    */
    protected ArrayList options;

    /** The nodes on which will Javadoc run
    */
    protected Node activatedNodes[];

    /** List of packages and classes Javadoc generates docs for. These list contain only
     * unique classes and packages. Any subpackages or classe contained in packages and
     *  subpackages are removed
     */

    private LinkedList pckgList = new LinkedList();
    private LinkedList clssList = new LinkedList();

    /** List and variables needed by RootDocImpl
    */
    private List userClasses;
    private List userPckgs;
    private ModifierFilter showAccess;

    /** Construtor
    */
    public JavadocInvoker(Node[] activatedNodes) {

        srcPath = new CoronaClassPath(false);
        binPath = new CoronaClassPath(true);
        ce = new CoronaEnvironment ( srcPath, binPath, new myConsumer() );

        options = OptionListProducer.getOptionList();

        /*
        ArrayList adding = new ArrayList();
        adding.add("-sourcepath");
        adding.add("");
        options.add( adding );
        */

        userClasses = new ArrayList();
        userPckgs = new ArrayList();

        showAccess = new ModifierFilter(OptionListProducer.getMembers());

        this.activatedNodes = activatedNodes;

        setName( "JavaDocThread" ); // NOI18N
    }


    /** Tests if javadoc generation is in progress
    */
    public static boolean isRunning() {
        return isRunning;
    }

    /** Parses all the files, creates envronment and RootDocImpl object and then
     * invokes the doclet. 
     */

    void invoke() {

        isRunning = true;

        // Parse all elements
        InheritanceChecksSwitch.turnOffInheritanceChecks();

        ListIterator iterator = clssList.listIterator();
        while( iterator.hasNext() )
            parseJdo( (JavaDataObject)iterator.next() );

        for ( Enumeration e1 = ce.getClasses();  e1.hasMoreElements(); ) {
            ClassDeclaration decl = (ClassDeclaration)e1.nextElement();
            userClasses.add(decl);
        }

        iterator = pckgList.listIterator();
        while( iterator.hasNext() )
            parseFolder( (DataFolder)iterator.next() );
        TopManager.getDefault().setStatusText( bundle.getString( "MSG_Constructing" ) );

        // Create Env and feed it with fields of CoronaEnvironment
        EnvWrapper envWrapper = new EnvWrapper( (ClassPath)srcPath, (ClassPath)binPath, Constants.F_WARNINGS, "" ); // NOI18N
        envWrapper.copyCoronaEnvironment( ce );

        ce = null;
        System.gc(); // Let the compiler stuff go

        RootDocImplWrapper rdiWrapper = new RootDocImplWrapper(
                                            envWrapper.getEnv(),
                                            userClasses,
                                            userPckgs,
                                            showAccess,
                                            options );

        rdiWrapper.setIO( out, err );

        //BhmDebug.BHM_Memory();

        envWrapper = null;

        TopManager.getDefault().setStatusText( bundle.getString( "MSG_RunningDoclet" ) );

        String docletClassName = OptionListProducer.isStyle1_1() ?
                                 oneoneDocletClassName :
                                 standardDocletClassName;

        //System.out.println (docletClassName);

        NbDocletInvoker dclInvkr = new NbDocletInvoker( docletClassName, null, err );
        dclInvkr.validOptions( rdiWrapper.options( options ) );
        dclInvkr.start( rdiWrapper );

        dclInvkr = null;


        // allows to free the memory after running javadoc

        setStaticField( "com.sun.tools.doclets.Configuration", "root", null); // NOI18N
        setStaticField( "com.sun.tools.doclets.HtmlDocWriter", "configuration", null); // NOI18N

        setStaticField( "com.sun.tools.doclets.standard.HtmlStandardWriter", "configuration", null); // NOI18N
        setStaticField( "com.sun.tools.doclets.standard.HtmlStandardWriter", "currentcd", null); // NOI18N

        setStaticField( "com.sun.tools.javadoc.MethodDocImpl", "map", null); // NOI18N
        setStaticField( "com.sun.tools.javadoc.FieldDocImpl", "map", null); // NOI18N
        setStaticField( "com.sun.tools.javadoc.ConstructorDocImpl", "map", null); // NOI18N
        setStaticField( "com.sun.tools.javadoc.ClassDocImpl", "classMap", null); // NOI18N
        setStaticField( "com.sun.tools.javadoc.PackageDocImpl", "packageMap", null); // NOI18N

        // Resets group setting after running javadoc

        rdiWrapper = null;

        System.gc(); // All the stuff becomes trash - hopefully

        TopManager.getDefault().setStatusText( "" ); // NOI18N
        InheritanceChecksSwitch.turnOnInheritanceChecks();
        isRunning = false;

        String destDir = OptionListProducer.getDestinationDirectory();

        String mssg = MessageFormat.format( bundle.getString( "FMT_GeneratingFinished" ),
                                            new Object[] { destDir } );

        NotifyDescriptor nd = new NotifyDescriptor.Confirmation ( mssg, NotifyDescriptor.YES_NO_OPTION );
        TopManager.getDefault().notify( nd );
        if ( nd.getValue().equals ( NotifyDescriptor.YES_OPTION ) ) {
            //System.out.println ( nd.getValue() );
            try {

                URL url = null;

                if ( OptionListProducer.isStyle1_1() )
                    url = new URL( "file:///" + destDir + java.io.File.separator + "packages.html" ); // NOI18N
                else
                    url = new URL( "file:///" + destDir + java.io.File.separator + "index.html" ); // NOI18N

                TopManager.getDefault().showUrl( url );
            }
            catch ( java.net.MalformedURLException e ) {
                throw new InternalError( "Can't find documentation index fier" ); // NOI18N
            }
        }
    }

    /** Parses one JavaDatObject
     */
    private void parseJdo( JavaDataObject jdo ) {
        try {
            CoronaClassFile ccf = new CoronaClassFile( jdo.getPrimaryFile( ) );
            TopManager.getDefault().setStatusText( bundle.getString( "MSG_Parsing" ) + " " + ccf.getName());
            ce.x_parseFile ( ccf );
        }
        catch (java.io.FileNotFoundException e) {
            TopManager.getDefault().notifyException( e );
        }
    }

    /** Parses all java files in folder and it's subfolders
    */
    private void parseFolder(DataFolder folder) {
        boolean isPackage = false;

        DataObject dobj[] = folder.getChildren();

        for( int i = 0; i < dobj.length; i++ )
            if ( dobj[i] instanceof JavaDataObject ) {
                try {
                    CoronaClassFile ccf = new CoronaClassFile( dobj[i].getPrimaryFile( ) );
                    TopManager.getDefault().setStatusText( bundle.getString( "MSG_Parsing" ) + " " + ccf.getName());
                    ce.parseFile ( ccf );
                    if (!isPackage) {
                        isPackage = true;
                        userPckgs.add( folder.getPrimaryFile().getPackageName('.') );
                    }
                }
                catch (java.io.FileNotFoundException e) {
                    TopManager.getDefault().notifyException( e );
                }
            }
            else if (dobj[i] instanceof DataFolder) {
                parseFolder( (DataFolder)dobj[i] );
            }
    }

    /** Inner class consuming parser's errors
    */
    class myConsumer implements ErrConsumer {
        public void pushError (FileObject errorFile,
                               int line,
                               int column,
                               String message,
                               String referenceText) {
            err.println ( bundle.getString( "MSG_Error" ) + " " + line + ":" + column + " " + message );
        }
    }


    /** Adds package to list only if is not subpackage of any othetr and remove any
     * present subpackages of this package
     */

    private void addPackage( DataFolder df ) {

        ListIterator iterator = pckgList.listIterator();
        DataFolder cdf;

        while (iterator.hasNext()) {
            cdf = (DataFolder)iterator.next();
            if ( df.getPrimaryFile().getPackageName('.').startsWith(
                        cdf.getPrimaryFile().getPackageName('.')))
                return;
            else if ( cdf.getPrimaryFile().getPackageName('.').startsWith(
                          df.getPrimaryFile().getPackageName('.')))
                iterator.remove();
        }
        pckgList.add( df );

    }


    /** Tests if the class is in any package
    */
    private boolean classInPackage( JavaDataObject jdo ) {

        ListIterator iterator = pckgList.listIterator(0);

        while (iterator.hasNext())
            if ( jdo.getPrimaryFile().getPackageName('.').startsWith(
                        ((DataObject)iterator.next()).getPrimaryFile().getPackageName('.')))
                return true;

        return false;
    }

    /** Makes lists of packages and classes and removes not uniques entries
    */
    private void createLists() {

        DataFolder df;
        JavaDataObject jdo;

        TopManager.getDefault().setStatusText( bundle.getString( "MSG_GeneratingList" ) );

        for( int i = 0; i < activatedNodes.length; ++i )
            if ((df = (DataFolder)activatedNodes[i].getCookie( DataFolder.class )) != null ) {
                addPackage( df );
            }
            else if ((jdo = (JavaDataObject)activatedNodes[i].getCookie( JavaDataObject.class )) != null ) {
                clssList.add( jdo );
            }

        // Remove all classes contained in packages

        ListIterator iterator = clssList.listIterator();
        while (iterator.hasNext())
            if ( classInPackage( (JavaDataObject)iterator.next()))
                iterator.remove();
    }

    /** The run method creates lists of packages and classes to be processed and
     * class invoke().
     */
    public void run() {
        createLists();
        invoke();
    }


    /** Gets the Input/Output tab */
    static InputOutput getIO() {

        // if ( ioTab != null ) {
        //System.out.println("IOC " + ioTab.isClosed() ); // NOI18N
        // ioTab.closeInputOutput();
        // }

        if ( ioTab == null ) {
            ioTab = TopManager.getDefault().getIO( bundle.getString( "CTL_Javadoc_IOTab" ) );
            ioTab.setErrSeparated (true);
            ioTab.setOutputVisible (true);
            ioTab.setErrVisible (true);
            out = ioTab.getOut ();
            ioTab.setFocusTaken (true);
            err = ioTab.getErr ();
            ioTab.select ();
        }
        else {
            try {
                out.reset();
                err.reset();
                ioTab.select();
            }
            catch (java.io.IOException e) {
                TopManager.getDefault().notifyException( e );
            }
        }

        return ioTab;
    }


    /** Sets field in class on value
    */
    private static void setStaticField( String className, String fieldName, Object value) {

        try {
            Class clazz = Class.forName( className );
            Field field = clazz.getDeclaredField( fieldName );
            field.setAccessible( true );
            field.set( clazz, value );
        }
        catch (ClassNotFoundException e) {
        }
        catch (IllegalAccessException e) {
        }
        catch (NoSuchFieldException e) {
        }
    }

}

/*
 * Log
 *  17   Gandalf   1.16        2/11/00  Petr Hrebejk    Memory leak in javadoc 
 *       generation fixed
 *  16   Gandalf   1.15        1/13/00  Petr Hrebejk    i18n mk3  
 *  15   Gandalf   1.14        1/12/00  Petr Hrebejk    i18n
 *  14   Gandalf   1.13        1/11/00  Petr Hrebejk    Better handling of ioTab
 *  13   Gandalf   1.12        1/10/00  Petr Hrebejk    Bug 4747 - closing of 
 *       output tab fixed
 *  12   Gandalf   1.11        1/3/00   Petr Hrebejk    Bugfix 4747
 *  11   Gandalf   1.10        11/25/99 Petr Hrebejk    Parser change in Java 
 *       loader module reflected
 *  10   Gandalf   1.9         11/10/99 Petr Hrebejk    Displaying packages.html
 *       instad of index.html for 1.1 style documentation
 *  9    Gandalf   1.8         11/9/99  Petr Hrebejk    Javadoc runs in 
 *       ExecEngine and captures all output
 *  8    Gandalf   1.7         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  7    Gandalf   1.6         9/15/99  Petr Hrebejk    New status texts + 
 *       localization
 *  6    Gandalf   1.5         6/11/99  Petr Hrebejk    
 *  5    Gandalf   1.4         6/9/99   Ian Formanek    Fixed to compile
 *  4    Gandalf   1.3         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  3    Gandalf   1.2         5/14/99  Petr Hrebejk    
 *  2    Gandalf   1.1         4/23/99  Ian Formanek    better capitalization of
 *       output window tab
 *  1    Gandalf   1.0         4/23/99  Petr Hrebejk    
 * $ 
 */ 
