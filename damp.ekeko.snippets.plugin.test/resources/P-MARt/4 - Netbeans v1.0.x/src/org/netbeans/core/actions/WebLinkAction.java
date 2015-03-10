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

package org.netbeans.core.actions;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.io.File;
import java.util.MissingResourceException;
import java.util.Locale;

import org.openide.TopManager;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;

/** The actions for opening a browser with specified web link
*
* @author Ales Novak, Jesse Glick
*/
public abstract class WebLinkAction extends CallableSystemAction {

    public WebLinkAction () {
        // Fixes 1.2.x Javac bug
    }

    protected String iconResource () {
        return "/org/netbeans/core/resources/actions/webLink.gif"; // NOI18N
    }

    public void performAction() {
        try {
            URL url = getUrl ();
            TopManager.getDefault ().setStatusText (
                NbBundle.getBundle (WebLinkAction.class).getString("CTL_OpeningBrowser"));
            TopManager.getDefault ().showUrl (url);
            TopManager.getDefault ().setStatusText (""); // NOI18N
        } catch (MalformedURLException mue) {
            if (Boolean.getBoolean ("netbeans.debug.exceptions")) // NOI18N
                mue.printStackTrace ();
        }
    }

    protected abstract URL getUrl () throws MalformedURLException;

    public HelpCtx getHelpCtx () {
        try {
            return new HelpCtx (getUrl ());
        } catch (MalformedURLException mue) {
            if (Boolean.getBoolean ("netbeans.debug.exceptions")) // NOI18N
                mue.printStackTrace ();
            return HelpCtx.DEFAULT_HELP;
        }
    }

    private static abstract class LocalizedDocsLink extends WebLinkAction {
        protected abstract String getDocResource ();
        protected URL getUrl () throws MalformedURLException {
            URL url;
            try {
                url = new File (System.getProperty ("netbeans.home")).getCanonicalFile ().toURL ();
            } catch (java.io.IOException ioe) {
                throw new MalformedURLException (ioe.toString ());
            }
            ClassLoader loader = new URLClassLoader (new URL[] { url });
            String name = getDocResource ();
            try {
                return NbBundle.getLocalizedFile (name, "html", Locale.getDefault (), loader); // NOI18N
            } catch (MissingResourceException mre) {
                try {
                    return NbBundle.getLocalizedFile (name, "htm", Locale.getDefault (), loader); // NOI18N
                } catch (MissingResourceException mre2) {
                    throw new MalformedURLException (mre2.toString ());
                }
            }
        }
        protected String iconResource () {
            return "/org/netbeans/core/resources/actions/webHelpLink.gif"; // NOI18N
        }
    }

    public static class DocsTutorialLink extends LocalizedDocsLink {
        /** generated Serialized Version UID */
        static final long serialVersionUID = 8340067743223425817L;
        public String getName() {
            return NbBundle.getBundle (WebLinkAction.class).getString("DocsTutorialLink");
        }
        protected String getDocResource () {
            return "docs.Tutorial.index"; // NOI18N
        }
    }

    public static class GettingStartedLink extends LocalizedDocsLink {
        private static final long serialVersionUID = 8140827723705425637L;
        public String getName () {
            return NbBundle.getBundle (WebLinkAction.class).getString ("GettingStartedLink");
        }
        protected String getDocResource () {
            return "docs.GettingStarted.index"; // NOI18N
        }
    }

    public static final class NetbeansOpenApiWebLink extends WebLinkAction {
        static final long serialVersionUID =5890649010623515821L;
        protected URL getUrl() throws MalformedURLException {
            return new URL (NbBundle.getBundle (WebLinkAction.class).getString("NetbeansOpenApiWebLinkURL"));
        }
        public String getName() {
            return NbBundle.getBundle (WebLinkAction.class).getString("NetbeansOpenApiWebLink");
        }
    }

    public static final class NetbeansEapWebLink extends WebLinkAction {
        static final long serialVersionUID =-4907774783115159247L;
        protected URL getUrl() throws MalformedURLException {
            return new URL (NbBundle.getBundle (WebLinkAction.class).getString("NetbeansEapWebLinkURL"));
        }
        public String getName() {
            return NbBundle.getBundle (WebLinkAction.class).getString("NetbeansEapWebLink");
        }
    }

    public static final class NetbeansWebLink extends WebLinkAction {
        /** generated Serialized Version UID */
        static final long serialVersionUID = 8750043230338549282L;
        protected URL getUrl() throws MalformedURLException {
            return new URL (NbBundle.getBundle (WebLinkAction.class).getString("NetbeansLinkURL"));
        }
        public String getName() {
            return NbBundle.getBundle (WebLinkAction.class).getString("NetbeansLink");
        }
    }

    public static final class SubmitFeedbackLink extends WebLinkAction {
        static final long serialVersionUID = 8720523330348719271L;
        protected URL getUrl() throws MalformedURLException {
            return new URL (NbBundle.getBundle (WebLinkAction.class).getString("SubmitFeedbackLinkURL"));
        }
        public String getName() {
            return NbBundle.getBundle (WebLinkAction.class).getString("SubmitFeedbackLink");
        }
    }

}

/*
 * Log
 *  29   Gandalf   1.28        3/10/00  Jesse Glick     Submit Feedback
 *  28   Gandalf   1.27        1/20/00  Jesse Glick     Coudl not open Tutorial 
 *       or Getting Started links under batch launcher (i.e. ../ in 
 *       netbeans.home).
 *  27   Gandalf   1.26        1/18/00  Jesse Glick     Compiler bug workaround.
 *  26   Gandalf   1.25        1/18/00  Jesse Glick     WebLink: added 
 *       GettingStarted; made URLs localizable; made positions of disk files 
 *       also localizable.
 *  25   Gandalf   1.24        1/13/00  Jaroslav Tulach I18N
 *  24   Gandalf   1.23        1/12/00  Ales Novak      i18n
 *  23   Gandalf   1.22        1/10/00  Jesse Glick     Updated APIs url. 
 *       Removed some obsoleted links.
 *  22   Gandalf   1.21        11/15/99 Patrick Keegan  
 *  21   Gandalf   1.20        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  20   Gandalf   1.19        10/8/99  Jesse Glick     Added tutorial link, 
 *       cleaned up WebLinkAction a little more.
 *  19   Gandalf   1.18        10/6/99  Jesse Glick     Cleaned out a lot of old
 *       crap, no longer trying to use HelpCtx's to display.
 *  18   Gandalf   1.17        8/9/99   Ian Formanek    Generated Serial Version
 *       UID
 *  17   Gandalf   1.16        8/2/99   Ian Formanek    Beta Feedback action in 
 *       Help menu
 *  16   Gandalf   1.15        6/11/99  Ian Formanek    Changed URL to submit 
 *       bugs to public page
 *  15   Gandalf   1.14        6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  14   Gandalf   1.13        5/26/99  Ian Formanek    Actions cleanup
 *  13   Gandalf   1.12        5/17/99  Ian Formanek    Modules Wishlist link 
 *       added to WebLinkAction
 *  12   Gandalf   1.11        4/28/99  Ian Formanek    Changed URLs to EAP 
 *       sites to correct ones (submitting bugs + open api pages)
 *  11   Gandalf   1.10        4/27/99  Jesse Glick     new HelpCtx () -> 
 *       HelpCtx.DEFAULT_HELP.
 *  10   Gandalf   1.9         4/12/99  Ian Formanek    Fixed link to Open APIs 
 *       On-line
 *  9    Gandalf   1.8         4/12/99  Ian Formanek    Open API Docs link works
 *  8    Gandalf   1.7         4/11/99  Ian Formanek    New weblink actions
 *  7    Gandalf   1.6         3/27/99  David Simonek   
 *  6    Gandalf   1.5         3/26/99  Ian Formanek    Fixed use of obsoleted 
 *       NbBundle.getBundle (this)
 *  5    Gandalf   1.4         3/2/99   David Simonek   icons repair
 *  4    Gandalf   1.3         1/21/99  David Simonek   Removed references to 
 *       "Actions" class
 *  3    Gandalf   1.2         1/7/99   Ian Formanek    fixed resource names
 *  2    Gandalf   1.1         1/6/99   David Simonek   
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 * Beta Change History:
 *  0    Tuborg    0.11        --/--/98 Jan Formanek    uses TopManager.showHelp() to show the browser
 *  0    Tuborg    0.12        --/--/98 Jan Formanek    DocsHelpLink added
 *  0    Tuborg    0.13        --/--/98 Jan Formanek    DocsTutorialLink
 *  0    Tuborg    0.14        --/--/98 Jan Formanek    removed alphatest links
 *  0    Tuborg    0.15        --/--/98 Jan Formanek    xelfi -> netbeans, alpha -> bugreports
 */
