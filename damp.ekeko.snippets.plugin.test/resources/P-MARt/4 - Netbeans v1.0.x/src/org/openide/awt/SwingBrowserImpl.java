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

package org.openide.awt;

import java.awt.BorderLayout;
import java.net.URL;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import javax.swing.event.HyperlinkListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLFrameHyperlinkEvent;
import javax.swing.JPanel;
import javax.swing.JEditorPane;

/**
* Implementation of BrowserImpl in Swing.
*/
final class SwingBrowserImpl extends HtmlBrowser.Impl {

    /** Current URL. */
    private URL                     url;
    private PropertyChangeSupport   pcs;
    private String                  statusMessage = ""; // NOI18N
    private SwingBrowser            swingBrowser;


    SwingBrowserImpl () {
        pcs = new PropertyChangeSupport (this);
        swingBrowser = new SwingBrowser ();
    }

    /**
    * Returns visual component of html browser.
    *
    * @return visual component of html browser.
    */
    public java.awt.Component getComponent () {
        return swingBrowser;
    }

    /**
    * Reloads current html page.
    */
    public void reloadDocument () {
        setURL (url);
    }

    /**
    * Stops loading of current html page.
    */
    public void stopLoading () {
    }

    /**
    * Sets current URL.
    *
    * @param url URL to show in the browser.
    */
    public void setURL (URL url) {
        try {
            URL old = getURL ();
            this.url = url;
            swingBrowser.setPage (url);
            pcs.firePropertyChange (PROP_URL, old, url);
        } catch (Exception e) {
            if (System.getProperty ("netbeans.debug.exceptions") != null) e.printStackTrace(); //PENDING
            pcs.firePropertyChange (PROP_STATUS_MESSAGE, null, statusMessage = "" + e); // NOI18N
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

    /** Returns title of the displayed page.
    * @return title 
    */
    public String getTitle () {
        return ""; // NOI18N
    }


    /** Is forward button enabled?
    * @return true if it is
    */
    public boolean isForward () {
        return false;
    }

    /** Moves the browser forward. Failure is ignored.
    */
    public void forward () {
    }

    /** Is backward button enabled?
    * @return true if it is
    */
    public boolean isBackward () {
        return false;
    }

    /** Moves the browser forward. Failure is ignored.
    */
    public void backward () {
    }

    /** Is history button enabled?
    * @return true if it is
    */
    public boolean isHistory () {
        return false;
    }

    /** Invoked when the history button is pressed.
    */
    public void showHistory () {
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


    // innerclasses ..............................................................

    private class SwingBrowser extends JEditorPane {

        private SwingBrowser () {
            setEditable (false);
            addHyperlinkListener (new HyperlinkListener () {
                                      public void hyperlinkUpdate (HyperlinkEvent e) {
                                          if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                                              if (e instanceof HTMLFrameHyperlinkEvent) {
                                                  HTMLFrameHyperlinkEvent  evt = (HTMLFrameHyperlinkEvent) e;
                                                  HTMLDocument doc = (HTMLDocument) getDocument ();
                                                  URL old = getURL ();
                                                  doc.processHTMLFrameHyperlinkEvent (evt);
                                                  pcs.firePropertyChange (PROP_URL, old, e.getURL ());
                                              } else {
                                                  try {
                                                      SwingBrowserImpl.this.setURL (e.getURL ());
                                                  } catch (Exception ex) {
                                                      if (System.getProperty ("netbeans.debug.exceptions") != null)
                                                          ex.printStackTrace ();
                                                  }
                                              }
                                          }
                                      }
                                  });
        }
    }
}

/*
 * Log
 *  9    src-jtulach1.8         1/12/00  Ian Formanek    NOI18N
 *  8    src-jtulach1.7         12/23/99 Jan Jancura     New version of HTML 
 *       browser support.
 *  7    src-jtulach1.6         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  6    src-jtulach1.5         10/10/99 Petr Hamernik   console debug messages 
 *       removed.
 *  5    src-jtulach1.4         8/17/99  Ian Formanek    Generated serial version
 *       UID
 *  4    src-jtulach1.3         7/25/99  Ian Formanek    Exceptions printed to 
 *       console only on "netbeans.debug.exceptions" flag
 *  3    src-jtulach1.2         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  2    src-jtulach1.1         3/2/99   Jan Jancura     BrowserFactory & 
 *       BrowserImpl moved to HtmlBrowser
 *  1    src-jtulach1.0         2/16/99  Jan Jancura     
 * $
 * Beta Change History:
 */
