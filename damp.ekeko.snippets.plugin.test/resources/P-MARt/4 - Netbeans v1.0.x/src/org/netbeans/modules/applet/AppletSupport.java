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

package org.netbeans.modules.applet;

import java.io.*;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.net.URL;
import java.net.MalformedURLException;
import java.net.UnknownHostException;

import org.openide.util.NbBundle;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.HttpServer;
import org.openide.NotifyDescriptor;
import org.openide.TopManager;

/** Support for execution applets for applets
*
* @author Ales Novak
* @version 0.10 May 07, 1998
*/
class AppletSupport {

    /** bundle to obtain text information from */
    static ResourceBundle bundle = NbBundle.getBundle(AppletSupport.class);

    /** constant for html extension */
    private static final String HTML_EXT = "html"; // NOI18N
    /** constant for class extension */
    private static final String CLASS_EXT = "class"; // NOI18N
    /** constant for java extension */
    private static final String JAVA_EXT  = "java"; // NOI18N

    /** reference to html file with the applet */
    private FileObject htmlFile;

    /**
    * no-arg constructor
    */
    AppletSupport() {
    }

    /** Mutates string name to a FileObject.
    * @param file
    * @return FileObject
    */
    static FileObject class2File(String file) {
        // try *.class
        file = file.replace('.', '/');
        FileObject fo = TopManager.getDefault().getRepository().findResource(file + '.' + CLASS_EXT);
        if (fo == null) {
            // class file may not be known by the filesystem yet
            fo = TopManager.getDefault().getRepository().findResource(file + '.' + JAVA_EXT);
        }
        return fo;
    }


    /**
    * @param sibling is a FileObject (.java and Applet) for that
    * the html is to be generated
    * @return html file with the same name as sibling
    */
    static FileObject generateHtml(FileObject sibling)
    throws IOException {
        FileObject parent = sibling.getParent(); // must be non null
        FileObject me = parent.getFileObject(sibling.getName(), HTML_EXT);
        if (me == null) {
            me = parent.createData(sibling.getName(), HTML_EXT);
            FileLock lock = me.lock();
            PrintWriter writer = null;
            try {
                writer = new PrintWriter(me.getOutputStream(lock));
                fillInFile(writer, me.getPackageName('/') + "." + CLASS_EXT); // NOI18N
            } finally {
                lock.releaseLock();
                if (writer != null)
                    writer.close();
            }
        }
        return me;
    }

    /**
    * @param sibling is a FileObject (.java and Applet) for that
    * the URL of the html is to be generated. The html page itself is generated as well
    * @return URL of the html file with the same name as sibling
    */
    static URL generateHtmlFileURL(FileObject sibling) throws HttpServerNotFoundException {
        FileObject html = null;
        IOException ex = null;
        try {
            html = generateHtml(sibling);
        } catch (IOException iex) {
            ex = iex;
        }
        URL url;
        try {
            if (ex == null)
                url = HttpServer.getResourceURL(html.getPackageNameExt('/','.'));
            else
                url = HttpServer.getRepositoryRoot();
        }
        catch (UnknownHostException e) {
            throw new HttpServerNotFoundException();
        }
        catch (MalformedURLException e) {
            throw new HttpServerNotFoundException();
        }
        return url;
    }

    /** fills in file with html source so it is html file with applet
    * @param file is a file to be filled
    * @param name is name of the applet                                     
    */
    private static void fillInFile(PrintWriter writer, String name) {
        writer.println("<HTML>"); // NOI18N
        writer.println("<HEAD>"); // NOI18N

        writer.print("   <TITLE>"); // NOI18N
        writer.print(bundle.getString("GEN_title"));
        writer.println("</TITLE>"); // NOI18N

        writer.println("</HEAD>"); // NOI18N
        writer.println("<BODY>\n"); // NOI18N

        writer.print("<H3><HR WIDTH=\"100%\">"); // NOI18N
        writer.print(bundle.getString("GEN_header"));
        writer.println("<HR WIDTH=\"100%\"></H3>\n"); // NOI18N

        writer.println("<P>"); // NOI18N
        String codebase = getCodebase (name);
        if (codebase == null)
            writer.print("<APPLET code="); // NOI18N
        else
            writer.print("<APPLET " + codebase + " code="); // NOI18N
        writer.print ("\""); // NOI18N

        writer.print(name);
        writer.print ("\""); // NOI18N

        writer.println(" width=350 height=200></APPLET>"); // NOI18N
        writer.println("</P>\n"); // NOI18N

        writer.print("<HR WIDTH=\"100%\"><FONT SIZE=-1><I>"); // NOI18N
        writer.print(bundle.getString("GEN_copy"));
        writer.println("</I></FONT>"); // NOI18N

        writer.println("</BODY>"); // NOI18N
        writer.println("</HTML>"); // NOI18N
        writer.flush();
    }

    /** creates codebase string */
    private static String getCodebase(String name) {
        StringTokenizer tokens = new StringTokenizer(name, "/"); // NOI18N
        int count = tokens.countTokens() - 1;
        switch (count) {
        case 0: return null;
        default: {
                StringBuffer buff = new StringBuffer(3*count + "codebase".length()); // NOI18N
                buff.append("codebase=.."); // NOI18N
                for (int i = 0; i < count - 1; i++)
                    buff.append("/.."); // NOI18N
                return buff.toString();
            }
        }
    }

    public static void reportNoHttpServer() {
        NotifyDescriptor.Message message = new NotifyDescriptor.Message(NbBundle.getBundle(AppletSupport.class).
                                           getString("EXC_NoHttpServer"), NotifyDescriptor.ERROR_MESSAGE);
        TopManager.getDefault().notify(message);
    }

}

/*
 * Log
 *  11   Gandalf   1.10        1/15/00  Petr Jiricka    Bugfix 5087
 *  10   Gandalf   1.9         1/12/00  Petr Jiricka    i18n
 *  9    Gandalf   1.8         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  8    Gandalf   1.7         10/5/99  Petr Jiricka    Method class2File moved 
 *       from AppletExecutor, most methods made static
 *  7    Gandalf   1.6         7/15/99  Petr Jiricka    
 *  6    Gandalf   1.5         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  5    Gandalf   1.4         5/11/99  Petr Jiricka    
 *  4    Gandalf   1.3         3/8/99   Petr Hamernik   localization
 *  3    Gandalf   1.2         2/18/99  Ian Formanek    Fixed bug #1212 - HTML 
 *       file for applet is badly generated
 *  2    Gandalf   1.1         1/25/99  Ian Formanek    Fixed bug #1068 - Applet
 *       HTML template ought to use double-quotes around values of CODE and 
 *       CODEBASE attributes in APPLET tag.
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
