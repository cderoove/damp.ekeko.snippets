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

package org.netbeans.modules.icebrowser;

import ice.iblite.Document;
import ice.iblite.Browser;
import ice.iblite.MouseOverLinkListener;
import ice.iblite.MouseOverLinkEvent;
import ice.iblite.BrowserClassLoader;

import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.awt.Point;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.net.URL;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.util.Hashtable;
import java.util.ArrayList;
import java.util.Date;
import java.util.ResourceBundle;
import javax.swing.SwingUtilities;

import org.openide.actions.FindAction;
import org.openide.awt.HtmlBrowser.Impl;
import org.openide.util.NbBundle;


/**
* ICE Browser support.
*
* @author Jan Jancura
*/
public class IceBrowserImpl extends Impl {

    // static ....................................................................

    /** Cache of classloaders in the icebrowser */
    private static Hashtable      loadersCache;
    /** Cache of loaded classes in the icebrowser */
    private static Hashtable      clazzCache;
    private static FindManager    findManager;

    /**
     * @associates Node 
     */
    static ArrayList              history = new ArrayList ();


    /**
    * Clear caches.
    */
    static synchronized void setCaches() {
        try {
            if (loadersCache == null) {
                Class clazz = BrowserClassLoader.class;
                Field field = clazz.getDeclaredField("$Fc"); // NOI18N
                field.setAccessible(true);
                loadersCache = (Hashtable) field.get(null);
                field = clazz.getDeclaredField("$Ec"); // NOI18N
                field.setAccessible(true);
                clazzCache = (Hashtable) field.get(null);
            }
        } catch (Exception e) {
            // ignore it
            if (Boolean.getBoolean("netbeans.debug.exceptions")) { // NOI18N
                e.printStackTrace();
            }
        }
    }

    /**
    * Clear all classloaders.
    */
    private static synchronized void clearClassLoader() {
        setCaches ();
        loadersCache.clear ();
        clazzCache.clear ();
    }


    // variables .................................................................

    /** browser visual component */
    private Browser                 browser;
    /** current URL */
    private URL                     url;
    /** standart helper variable */
    private PropertyChangeSupport   pcs;
    /** Current status message. */
    private String                  statusMessage = ""; // NOI18N
    /** Helper variable for reverting status message to default one. */
    private String                  oldStatus;
    /** Current value of title property. */
    private String                  title = ""; // NOI18N
    /** Helper instance of settings. */
    private ICEBrowserSettings      settings = new ICEBrowserSettings ();
    /** Forward property value. */
    private boolean                 forward;
    /** Backward property value. */
    private boolean                 backward;


    // init ......................................................................

    /**
    * Creates instance of Ice Browser.
    */
    public IceBrowserImpl () {
        pcs = new PropertyChangeSupport (this);
        findManager = new FindManager (this);
        browser = new Browser () {
                      public void requestFocus () {
                          ((FindAction) FindAction.get (FindAction.class)).setActionPerformer (
                              findManager
                          );
                          super.requestFocus ();
                      }
                  };
        IceMouseListener iml = new IceMouseListener ();
        browser.addMouseOverLinkListener (iml);
        browser.addPropertyChangeListener (iml);
        settings.addPropertyChangeListener (iml);

        browser.setDefaultBackground (settings.getDefaultBackground ());
        browser.setFixedFont (settings.getFixedFont ());
        browser.setProportionalFont (settings.getProportionalFont ());
        browser.setEncoding (settings.getEncoding ());
    }


    // HtmpBrowser.Impl implementation ...........................................

    /**
    * Returns visual component of html browser.
    *
    * @return visual component of html browser.
    */
    public java.awt.Component getComponent () {
        return browser;
    }

    /**
    * Reloads current html page.
    */
    public void reloadDocument () {
        clearClassLoader();
        browser.clearCache();
        browser.reload ();
    }

    /**
    * Stops loading of current html page.
    */
    public void stopLoading () {
        browser.htmlInterrupt ();
    }

    /**
    * Sets current URL.
    *
    * @param url URL to show in the browser.
    */
    public void setURL (URL url) {
        URL old = getURL ();
        if ((old != null) && old.equals (url)) {
            browser.reload ();
        } else {
            this.url = url;
            put (url);
            browser.setCurrentLocation (url.toString ());
            pcs.firePropertyChange (PROP_URL, old, url);
        }
    }

    /**
    * Returns current URL.
    *
    * @return current URL.
    */
    public URL getURL () {
        return url;
    }

    /**
    * Returns status message representing status of html browser.
    *
    * @return status message.
    */
    public String getStatusMessage () {
        return statusMessage;
    }

    /**
    * Shows given string in the html browser.
    *
    * @param str String to show in html browser.
    */
    public void showString (String str) {
        browser.htmlClear ();
        browser.htmlAppend (str);
    }

    /** Returns title of the displayed page.
    * @return title 
    */
    public String getTitle () {
        return title;
    }

    /** Is forward button enabled?
    * @return true if it is
    */
    public boolean isForward () {
        return forward;
    }

    /** Moves the browser forward. Failure is ignored.
    */
    public void forward () {
        /*    try {
              URL old = getURL ();
              String sUrl = getURL (++index);
              url = new URL (sUrl);
              browser.setCurrentLocation (sUrl);
              pcs.firePropertyChange (PROP_URL, old, url);
              if (index == 1) 
                pcs.firePropertyChange (PROP_BACKWARD, Boolean.FALSE, Boolean.TRUE);
              if (index == (history.size () - 1)) 
                pcs.firePropertyChange (PROP_FORWARD, Boolean.TRUE, Boolean.FALSE);
            } catch (java.net.MalformedURLException e) {
            }*/
        try {
            URL old = getURL ();
            browser.goForward ();
            String sUrl = browser.getCurrentLocation ();
            url = new URL (sUrl);
            pcs.firePropertyChange (PROP_URL, old, url);
            update ();
        } catch (java.net.MalformedURLException e) {
        }
    }

    /** Is backward button enabled?
    * @return true if it is
    */
    public boolean isBackward () {
        return backward;
    }

    /** Moves the browser forward. Failure is ignored.
    */
    public void backward () {
        /*    try {
              URL old = getURL ();
              String sUrl = getURL (--index);
              url = new URL (sUrl);
              browser.setCurrentLocation (sUrl);
              pcs.firePropertyChange (PROP_URL, old, url);
              if (index == 0) 
                pcs.firePropertyChange (PROP_BACKWARD, Boolean.TRUE, Boolean.FALSE);
              if (index == (history.size () - 2)) 
                pcs.firePropertyChange (PROP_FORWARD, Boolean.FALSE, Boolean.TRUE);
            } catch (java.net.MalformedURLException e) {
            }*/
        try {
            URL old = getURL ();
            browser.goBack ();
            String sUrl = browser.getCurrentLocation ();
            url = new URL (sUrl);
            pcs.firePropertyChange (PROP_URL, old, url);
            update ();
        } catch (java.net.MalformedURLException e) {
        }
    }

    /** Is history button enabled?
    * @return true if it is
    */
    public boolean isHistory () {
        return true;
    }

    /** Invoked when the history button is pressed.
    */
    public void showHistory () {
        ResourceBundle bundle = NbBundle.getBundle (FindManager.class);
        StringBuffer sb = new StringBuffer ();
        sb.append ("<HTML><HEAD><TITLE>"). // NOI18N
        append (bundle.getString ("CTL_History")).
        append ("</TITLE></HEAD>\n<BODY><H2>"). // NOI18N
        append (bundle.getString ("CTL_History")).
        append (":</H2>\n"); // NOI18N
        DateFormat df = DateFormat.getDateTimeInstance (DateFormat.MEDIUM, DateFormat.SHORT);
        int i, k = history.size ();
        for (i = 0; i < k; i++) {
            Node n = (Node) history.get (i);
            sb.append ("<BR>"). // NOI18N
            append (df.format (n.date)).
            append ("&nbsp;&nbsp;"). // NOI18N
            append ("<A HREF=\""). // NOI18N
            append (n.url).
            append ("\">"). // NOI18N
            append (n.url).
            append ("</A> "). // NOI18N
            append ("\n") ; // NOI18N
        }
/*        sb.append ("<BR><BR><BR><form name=\"clear\" action=\"http://clear/\">\n"); // NOI18N
        sb.append ("<input type=\"Submit\" value=\""); // NOI18N
        sb.append (bundle.getString ("CTL_Clear_history")); // NOI18N
        sb.append ("\"></FORM>\n"); // NOI18N
*/
        sb.append ("</BODY></HTML>"); // NOI18N
        browser.htmlClear ();
        browser.htmlAppend (new String (sb));
    }

    /**
    * Adds PropertyChangeListener to this browser.
    *
    * @param l Listener to add.
    */
    public void addPropertyChangeListener (PropertyChangeListener l) {
        pcs.addPropertyChangeListener (l);
    }

    /**
    * Removes PropertyChangeListener from this browser.
    *
    * @param l Listener to remove.
    */
    public void removePropertyChangeListener (PropertyChangeListener l) {
        pcs.removePropertyChangeListener (l);
    }


    // other methods ..............................................................

    /**
    * Returns instance of ICE Browser Document.
    */
    public Document getDocument () {
        return browser;
    }

    /**
    * Stores given URL in history.
    */
    private void put (URL url) {
        String U = url.toString ();
        Node n = new Node (U);
        history.add (n);
        update ();
    }

    /**
    * Clear all items from history.
    */
    private void clearHistory () {
        history = new ArrayList ();
        browser.htmlInterrupt ();
        reloadDocument ();
    }

    /**
    * Update values of backward & forward properties.
    */
    private void update () {
        boolean nBackward = browser.getBackHistory ().size () > 0;
        boolean nForward = browser.getForwardHistory ().size () > 0;
        if (nBackward != backward) {
            backward = nBackward;
            pcs.firePropertyChange (
                PROP_BACKWARD,
                new Boolean (!backward),
                new Boolean (backward)
            );
        }
        if (nForward != forward) {
            forward = nForward;
            pcs.firePropertyChange (
                PROP_FORWARD,
                new Boolean (!forward),
                new Boolean (forward)
            );
        }
    }


    // innerclasses ..............................................................

    /**
    * Represents one position in history.
    */
    private static class Node implements java.io.Serializable {

        /** serialVersionUID */
        private static final long serialVersionUID = -6252984103555792114L;

        Point                   position;
        Date                    date;
        String                  url;

        Node (
            String   url
        ) {
            this.position = position;
            this.url = url;
            this.date = new Date ();
        }
    }


    /**
    * Listens on Ice browser on mouse state changes and property changes and
    * on ICEBrowserSettions on changes of settings.
    */
    private class IceMouseListener implements MouseOverLinkListener,
        PropertyChangeListener {

        /**
        * Propagates current link to status message.
        */
        public void mouseOverLinkEntered (MouseOverLinkEvent e) {
            pcs.firePropertyChange (PROP_STATUS_MESSAGE, oldStatus, statusMessage = e.getLink ());
        }

        /**
        * Reverts status message to default value.
        */
        public void mouseOverLinkExited (MouseOverLinkEvent e) {
            String old = statusMessage;
            pcs.firePropertyChange (PROP_STATUS_MESSAGE, old, statusMessage = oldStatus);
        }

        /**
        * Does nothink.
        */
        public void mouseOverLinkMoved (MouseOverLinkEvent e) {
        }

        /**
        * Performs mouse click.
        */
        public void mouseOverLinkClicked (final MouseOverLinkEvent e) {
            SwingUtilities.invokeLater (new Runnable () {
                                            public void run () {
                                                try {

                                                    if (e.getLink ().equals ("http://clear/?"))  // NOI18N
                                                        clearHistory ();
                                                    else {
                                                        URL old = getURL ();
                                                        url = new URL (e.getLink ());
                                                        put (url);
                                                        pcs.firePropertyChange (PROP_URL, old, url);
                                                    }
                                                } catch (java.net.MalformedURLException ee) {
                                                    ee.printStackTrace (); //PENDING
                                                }
                                            }
                                        });
        }

        /**
        * Listens on document title changes.
        */
        public void propertyChange (PropertyChangeEvent e){
            if (e.getPropertyName ().equals ("documentTitle")) {
                String old = statusMessage;
                pcs.firePropertyChange (PROP_STATUS_MESSAGE, old, statusMessage = browser.getDocumentTitle ());
                old = title;
                pcs.firePropertyChange (PROP_TITLE, old, title = browser.getDocumentTitle ());
                oldStatus = statusMessage;
            } else

                if (e.getPropertyName ().equals (ICEBrowserSettings.PROP_DEFAULT_BACKGROUND)) {
                    browser.setDefaultBackground (settings.getDefaultBackground ());
                } else

                    if (e.getPropertyName ().equals (ICEBrowserSettings.PROP_FIXED_FONT)) {
                        browser.setFixedFont (settings.getFixedFont ());
                    } else

                        if (e.getPropertyName ().equals (ICEBrowserSettings.PROP_PROPORTIONAL_FONT)) {
                            browser.setProportionalFont (settings.getProportionalFont ());
                        } else

                            if (e.getPropertyName ().equals (ICEBrowserSettings.PROP_ENCODING)) {
                                browser.setEncoding (settings.getEncoding ());
                                reloadDocument ();
                            }
        }
    }
}

/*
 * Log
 *  11   Gandalf-post-FCS1.7.1.2     4/5/00   Jan Jancura     Encoding editor added
 *  10   Gandalf-post-FCS1.7.1.1     4/3/00   Jan Jancura     
 *  9    Gandalf-post-FCS1.7.1.0     4/3/00   Jan Jancura     Encoding support
 *  8    src-jtulach1.7         1/13/00  Ian Formanek    NOI18N
 *  7    src-jtulach1.6         12/23/99 Jan Jancura     New version of Ice 
 *       Browser support
 *  6    src-jtulach1.5         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  5    src-jtulach1.4         10/6/99  Ales Novak      #3023
 *  4    src-jtulach1.3         10/6/99  Jan Jancura     Clear cache of applets 
 *       on reload of html page.
 *  3    src-jtulach1.2         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  2    src-jtulach1.1         3/2/99   Jan Jancura     BrowserFactory & 
 *       BrowserImpl moved to HtmlBrowser
 *  1    src-jtulach1.0         2/17/99  Jan Jancura     
 * $
 * Beta Change History:
 */
