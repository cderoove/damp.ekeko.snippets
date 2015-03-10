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

import java.util.List;
import java.util.ArrayList;

import org.netbeans.modules.javadoc.settings.JavadocSettings;
import org.netbeans.modules.javadoc.settings.StdDocletSettings;
import org.netbeans.modules.javadoc.settings.MemberConstants;


/** This singleton produces ArrayList with Javadoc options compatible with
 * method RootDocImpl.options.
 *   
 * 
 * @author Petr Hrebejk
 */
class  OptionListProducer extends Object {

    static JavadocSettings javadocS = null;
    static StdDocletSettings docletS = null;

    static ArrayList getOptionList() {
        ArrayList optionList = new ArrayList();

        if ( javadocS == null )
            javadocS = new JavadocSettings();
        if ( docletS == null )
            docletS = new StdDocletSettings();

        if (javadocS.getOverview() != null )
            setStringOption( javadocS.getOverview().getAbsolutePath(), "-d", optionList ); // NOI18N

        long members = javadocS.getMembers();

        if (members == MemberConstants.PUBLIC)
            setBooleanOption( true, "-public", optionList ); // NOI18N
        else if (members == MemberConstants.PACKAGE)
            setBooleanOption( true, "-package", optionList ); // NOI18N
        else if (members == MemberConstants.PRIVATE)
            setBooleanOption( true, "-private", optionList ); // NOI18N
        else
            setBooleanOption( true, "-protected", optionList ); // NOI18N


        setBooleanOption( javadocS.isVerbose(), "-verbose", optionList ); // NOI18N
        setBooleanOption( javadocS.isStyle1_1(), "-1.1", optionList ); // NOI18N
        setStringOption( javadocS.getEncoding(), "-encoding", optionList ); // NOI18N
        setStringOption( javadocS.getLocale(), "-locale", optionList ); // NOI18N

        if (docletS.getDirectory() != null )
            setStringOption( docletS.getDirectory().getAbsolutePath(), "-d", optionList ); // NOI18N

        setBooleanOption( docletS.isUse(), "-use", optionList ); // NOI18N
        setBooleanOption( docletS.isVersion(), "-version", optionList ); // NOI18N
        setBooleanOption( docletS.isAuthor(), "-author", optionList ); // NOI18N
        setBooleanOption( docletS.isSplitindex(), "-splitindex", optionList ); // NOI18N
        setBooleanOption( docletS.isNodeprecated(), "-nodeprecated", optionList ); // NOI18N
        setBooleanOption( docletS.isNodeprecatedlist(), "-nodeprecatedlist", optionList ); // NOI18N

        setStringOption( docletS.getWindowtitle(), "-windowtitle", optionList ); // NOI18N
        setStringOption( docletS.getDoctitle(), "-doctitle", optionList ); // NOI18N
        setStringOption( docletS.getHeader(), "-header", optionList ); // NOI18N
        setStringOption( docletS.getFooter(), "-footer", optionList ); // NOI18N
        setStringOption( docletS.getBottom(), "-bottom", optionList ); // NOI18N
        setStringOption( docletS.getLink(), "-link", optionList ); // NOI18N


        String[] ga = docletS.getGroup();

        if (ga != null)
            for (int i = 0; i < ga.length / 2; ++i ) {
                List subList = new ArrayList();
                subList.add( "-group" ); // NOI18N
                if (ga[i*2] != null && !ga[i*2].trim().equals("")) // NOI18N
                    subList.add( ga[i*2] );
                if (ga[i*2+1] != null && !ga[i*2+1].trim().equals("")) // NOI18N
                    subList.add( ga[i*2+1] );
                optionList.add( subList );
            }

        setBooleanOption( docletS.isNotree(), "-notree", optionList ); // NOI18N
        setBooleanOption( docletS.isNoindex(), "-noindex", optionList ); // NOI18N
        setBooleanOption( docletS.isNohelp(), "-nohelp", optionList ); // NOI18N
        setBooleanOption( docletS.isNonavbar(), "-nonavbar", optionList ); // NOI18N

        if (docletS.getHelpfile() != null )
            setStringOption( docletS.getHelpfile().getAbsolutePath(), "-helpfile", optionList ); // NOI18N

        if (docletS.getStylesheetfile() != null )
            setStringOption( docletS.getStylesheetfile().getAbsolutePath(), "-stylesheetfile", optionList ); // NOI18N

        setStringOption( docletS.getCharset(), "-charset", optionList ); // NOI18N

        return optionList;
    }

    static void setBooleanOption( boolean value, String option, List dest ) {
        if (value) {
            List subList =  new ArrayList();
            subList.add( option );
            dest.add( subList );
            //System.out.println ( option );
        }
    }

    static void setStringOption( String value, String option, List dest ) {
        if (value != null && !value.trim().equals("")) { // NOI18N
            List subList =  new ArrayList();
            subList.add( option );
            subList.add( value );
            dest.add( subList );
            //System.out.println ( option + " " + value ); // NOI18N
        }
    }


    static long getMembers() {
        return javadocS.getMembers();
    }

    static boolean isStyle1_1() {
        return javadocS.isStyle1_1();
    }

    static String getDestinationDirectory() {
        return docletS.getDirectory().getAbsolutePath();
    }
}

/*
 * Log
 *  6    Gandalf   1.5         1/13/00  Petr Hrebejk    i18n mk3  
 *  5    Gandalf   1.4         1/12/00  Petr Hrebejk    i18n
 *  4    Gandalf   1.3         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  3    Gandalf   1.2         9/15/99  Petr Hrebejk    Option -docencoding 
 *       changed to -charset
 *  2    Gandalf   1.1         5/14/99  Petr Hrebejk    
 *  1    Gandalf   1.0         4/23/99  Petr Hrebejk    
 * $ 
 */ 