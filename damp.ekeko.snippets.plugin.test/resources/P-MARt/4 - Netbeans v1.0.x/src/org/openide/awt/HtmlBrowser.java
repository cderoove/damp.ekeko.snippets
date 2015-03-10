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

import java.awt.*;
import java.awt.event.*;
import java.awt.datatransfer.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.text.DateFormat;
import java.util.ResourceBundle;
import java.util.Vector;
import java.util.Date;
import javax.swing.*;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.text.html.HTMLFrameHyperlinkEvent;
import javax.swing.text.html.HTMLDocument;
import javax.swing.border.EmptyBorder;

import org.openide.awt.Toolbar;
import org.openide.awt.ToolbarButton;
import org.openide.*;
import org.openide.loaders.*;
import org.openide.filesystems.*;
import org.openide.windows.CloneableTopComponent;
import org.openide.text.*;
import org.openide.actions.CopyAction;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.io.*;
import org.openide.util.actions.*;

/**
* Object that provides viewer for html.
*
* @author Jan Jancura
*/
public class HtmlBrowser extends JPanel {

    // static ....................................................................

    /** generated Serialized Version UID */
    static final long                 serialVersionUID = 2912844785502987960L;

    /** Preferred width of the browser */
    public static final int           DEFAULT_WIDTH = java.awt.Toolkit.
            getDefaultToolkit ().getScreenSize ().
            width / 2;
    /** Preferred height of the browser */
    public static final int           DEFAULT_HEIGHT = java.awt.Toolkit.
            getDefaultToolkit ().getScreenSize ().
            height / 2;
    /** current implementation of html browser */
    private static Factory            browserFactory;
    /** home page URL */
    private static String             homePage = "http://www.sun.com/forte/ffj/"; // NOI18N

    /** Resource for all localized strings in local file system. */
    private static ResourceBundle     resourceBundle;
    /** browser title format support */
    private static MessageFormat      msgFormat;

    /** Icons for buttons. */
    private static Icon               iBack;
    private static Icon               iForward;
    private static Icon               iHome;
    private static Icon               iReload;
    private static Icon               iStop;
    private static Icon               iHistory;


    /** Sets the home page.
    * @param u the home page
    */
    public static void setHomePage (String u) {
        homePage = u;
    }

    /** Getter for the home page
    * @return the home page
    */
    public static String getHomePage () {
        return homePage;
    }

    /**
    * Sets a new implementation of browser visual component
    * for all HtmlBrowers.
    */
    public static void setFactory (Factory brFactory) {
        browserFactory = brFactory;
    }


    // variables .................................................................

    /** currently used implementation of browser */
    private Impl                        browserImpl;
    /** true = do not listen on changes of URL on cbLocation */
    private boolean                     everythinkIListenInCheckBoxIsUnimportant = false;
    /** toolbar visible property */
    private boolean                     toolbarVisible = false;
    /** status line visible property */
    private boolean                     statusLineVisible = false;
    /**  Listens on changes in HtmlBrowser.Impl and HtmlBrowser visual components.
    */
    private BrowserListener             browserListener;

    // visual components .........................................................

    private JButton                     bBack,
    bForward,
    bHome,
    bReload,
    bStop,
    bHistory;
    /** URL chooser */
    private JComboBox                   cbLocation;
    private JLabel                      cbLabel;
    private JLabel                      lStatusLine;
    private Component                   browserComponent;
    private JPanel                      head;


    // init ......................................................................

    /**
    * Creates new html browser with toolbar and status line.
    */
    public HtmlBrowser () {
        this (true, true);
    }

    /**
    * Creates new html browser.
    */
    public HtmlBrowser (boolean toolbar, boolean statusLine) {
        init ();

        try {
            if (browserFactory != null) {
                browserImpl = browserFactory.createHtmlBrowserImpl ();
            }
        } catch (Error e) {
            // browser was uninstlled ?
            TopManager.getDefault ().notify (new NotifyDescriptor.Exception (
                                                 e,
                                                 resourceBundle.getString ("EXC_Module")
                                             ));
        }

        if (browserFactory == null) {
            browserImpl = new SwingBrowserImpl ();
        }

        setLayout (new BorderLayout ());

        browserComponent = browserImpl.getComponent ();
        add (new JScrollPane (browserComponent), "Center"); // NOI18N

        browserListener = new BrowserListener ();
        if (toolbar) initToolbar ();
        if (statusLine) initStatusLine ();

        browserImpl.addPropertyChangeListener (browserListener);
    }

    /**
    * Default initializations.
    */
    private static void init () {
        if (iBack != null) return;

        resourceBundle = NbBundle.getBundle(HtmlBrowser.class);
        iBack = new ImageIcon (HtmlBrowser.class.getResource (
                                   "/org/openide/resources/html/back.gif" // NOI18N
                               ));
        iForward = new ImageIcon (HtmlBrowser.class.getResource (
                                      "/org/openide/resources/html/forward.gif" // NOI18N
                                  ));
        iHome = new ImageIcon (HtmlBrowser.class.getResource (
                                   "/org/openide/resources/html/home.gif" // NOI18N
                               ));
        iReload = new ImageIcon (HtmlBrowser.class.getResource (
                                     "/org/openide/resources/html/refresh.gif" // NOI18N
                                 ));
        iStop = new ImageIcon (HtmlBrowser.class.getResource (
                                   "/org/openide/resources/html/stop.gif" // NOI18N
                               ));
        iHistory = new ImageIcon (HtmlBrowser.class.getResource (
                                      "/org/openide/resources/html/history.gif" // NOI18N
                                  ));
        msgFormat = new MessageFormat (resourceBundle.getString (
                                           "CTL_Html_viewer_title" // NOI18N
                                       ));
    }

    /**
    * Default initialization of toolbar.
    */
    private void initToolbar () {
        toolbarVisible = true;

        // create visual compoments .............................
        head = new JPanel ();
        head.setLayout (new BorderLayout ());
        head.setBorder (new EmptyBorder (2, 2, 2, 2));

        JPanel p = new JPanel (new FlowLayout (FlowLayout.LEFT, 0, 0));
        p.add (bBack = new ToolbarButton (iBack));
        bBack.setToolTipText (resourceBundle.getString ("CTL_Back"));
        bBack.setEnabled (browserImpl.isBackward ());
        p.add (bForward = new ToolbarButton (iForward));
        bForward.setToolTipText (resourceBundle.getString ("CTL_Forward"));
        bForward.setEnabled (browserImpl.isForward ());
        p.add (bStop = new ToolbarButton (iStop));
        bStop.setToolTipText (resourceBundle.getString ("CTL_Stop"));
        p.add (bReload = new ToolbarButton (iReload));
        bReload.setToolTipText (resourceBundle.getString ("CTL_Reload"));
        p.add (bHome = new ToolbarButton (iHome));
        bHome.setToolTipText (resourceBundle.getString ("CTL_Home"));
        p.add (bHistory = new ToolbarButton (iHistory));
        bHistory.setToolTipText (resourceBundle.getString ("CTL_History"));
        Toolbar.Separator ts = new Toolbar.Separator ();
        p.add (ts);
        ts.updateUI ();
        p.add ( cbLabel = new JLabel (resourceBundle.getString ("CTL_Location") +
                                      " : " // NOI18N
                                     ));
        head.add ("West", p); // NOI18N

        head.add ("Center", cbLocation = new JComboBox ()); // NOI18N
        cbLocation.setEditable (true);
        add (head, "North"); // NOI18N

        // add listeners ..................... .............................
        cbLocation.addActionListener (browserListener);
        bHistory.addActionListener (browserListener);
        bBack.addActionListener (browserListener);
        bForward.addActionListener (browserListener);
        bReload.addActionListener (browserListener);
        bHome.addActionListener (browserListener);
        bStop.addActionListener (browserListener);
    }

    /**
    * Default initialization of toolbar.
    */
    private void destroyToolbar () {
        remove (head);
        head = null;
        toolbarVisible = false;
    }

    /**
    * Default initialization of status line.
    */
    private void initStatusLine () {
        statusLineVisible = true;
        add (
            lStatusLine = new JLabel (resourceBundle.getString ("CTL_Loading")),
            "South" // NOI18N
        );
    }

    /**
    * Destroyes status line.
    */
    private  void destroyStatusLine () {
        remove (lStatusLine);
        lStatusLine = null;
        statusLineVisible = false;
    }


    // public methods ............................................................

    /**
    * Sets new URL.
    *
    * @param str URL to show in this browser.
    */
    public void setURL (String str) {
        URL URL;
        try {
            URL = new java.net.URL (str);
        } catch (java.net.MalformedURLException ee) {
            try {
                URL = new java.net.URL ("http://" + str); // NOI18N
            } catch (java.net.MalformedURLException e) {
                org.openide.TopManager.getDefault ().notifyException (e);
                return;
            }
        }
        setURL (URL);
    }

    /**
    * Sets new URL.
    *
    * @param str URL to show in this browser.
    */
    public void setURL (final URL url) {
        SwingUtilities.invokeLater (new Runnable () {
                                        public void run () {
                                            if (url.equals (browserImpl.getURL ())) {
                                                browserImpl.reloadDocument ();
                                            } else {
                                                browserImpl.setURL (url);
                                            }
                                        }
                                    }
                                   );
    }

    /**
    * Gets current document url.
    */
    public final URL getDocumentURL () {
        return browserImpl.getURL ();
    }

    /**
    * Enables/disables Home button.
    */
    public final void setEnableHome (boolean b) {
        bHome.setEnabled (b);
        bHome.setVisible (b);
    }

    /**
    * Enables/disables location.
    */
    public final void setEnableLocation (boolean b) {
        cbLocation.setEditable (b);
        cbLocation.setVisible (b);
        cbLabel.setVisible (b);
    }

    /**
    * Gets status line state.
    */
    public boolean isStatusLineVisible () {
        return statusLineVisible;
    }

    /**
    * Shows/hides status line.
    */
    public void setStatusLineVisible (boolean v) {
        if (v == statusLineVisible) return;
        if (v) initStatusLine ();
        else destroyStatusLine ();
    }

    /**
    * Gets status toolbar.
    */
    public boolean isToolbarVisible () {
        return toolbarVisible;
    }

    /**
    * Shows/hides toolbar.
    */
    public void setToolbarVisible (boolean v) {
        if (v == toolbarVisible) return;
        if (v) initToolbar ();
        else destroyToolbar ();
    }

    // helper methods .......................................................................

    /** Fire prop. change.
    */
    private final void firePropertyChangeAccess (String name, String o, String n) {
        super.firePropertyChange (name, o, n);
    }

    /**
    * Returns preferred size.
    */
    public java.awt.Dimension getPreferredSize () {
        java.awt.Dimension superPref = super.getPreferredSize ();
        return new java.awt.Dimension (
                   Math.max (DEFAULT_WIDTH, superPref.width),
                   Math.max (DEFAULT_HEIGHT, superPref.height)
               );
    }


    // innerclasses ..............................................................

    /**
    * Listens on changes in HtmlBrowser.Impl and HtmlBrowser visual components.
    */
    private class BrowserListener implements ActionListener,
        PropertyChangeListener {

        /**
        * Listens on changes in HtmlBrowser.Impl.
        */
        public void propertyChange (PropertyChangeEvent evt) {
            String property = evt.getPropertyName ();
            if (property == null) return;

            if (property.equals (Impl.PROP_URL)) {
                if (toolbarVisible) {
                    everythinkIListenInCheckBoxIsUnimportant = true;
                    String url = browserImpl.getURL ().toString ();
                    cbLocation.setSelectedItem (url);
                    everythinkIListenInCheckBoxIsUnimportant = false;
                }
            } else

                if (property.equals (Impl.PROP_STATUS_MESSAGE)) {
                    String s = browserImpl.getStatusMessage ();
                    if ((s == null) || (s.length () < 1))
                        s = resourceBundle.getString ("CTL_Document_done");
                    if (lStatusLine != null) lStatusLine.setText (s);
                } else

                    if (property.equals (Impl.PROP_FORWARD)) {
                        bForward.setEnabled (browserImpl.isForward ());
                    } else

                        if (property.equals (Impl.PROP_BACKWARD)) {
                            bBack.setEnabled (browserImpl.isBackward ());
                        } else

                            if (property.equals (Impl.PROP_HISTORY)) {
                                bHistory.setEnabled (browserImpl.isHistory ());
                            }
        }

        /**
        * Listens on changes in HtmlBrowser visual components.
        */
        public void actionPerformed (ActionEvent e) {
            if (e.getSource () == cbLocation) {
                // URL manually changed
                if (everythinkIListenInCheckBoxIsUnimportant) return;
                JComboBox cb = (JComboBox)e.getSource ();
                Object o = cb.getSelectedItem ();
                setURL ((String)o);
                ListModel lm = cb.getModel ();
                int i, k = lm.getSize ();
                for (i = 0; i < k; i++) if (o.equals (lm.getElementAt (i))) break;
                if (i != k) return;
                if (k == 20) cb.removeItem (lm.getElementAt (k - 1));
                cb.insertItemAt (o, 0);
            } else

                if (e.getSource () == bHistory) {
                    browserImpl.showHistory ();
                } else

                    if (e.getSource () == bBack) {
                        browserImpl.backward ();
                    } else

                        if (e.getSource () == bForward) {
                            browserImpl.forward ();
                        } else

                            if (e.getSource () == bReload) {
                                browserImpl.reloadDocument ();
                            } else

                                if (e.getSource () == bHome) {
                                    setURL (homePage);
                                } else

                                    if (e.getSource () == bStop) {
                                        browserImpl.stopLoading ();
                                    }
        }
    }

    public static class BrowserComponent extends CloneableTopComponent {
        /** generated Serialized Version UID */
        static final long                   serialVersionUID = 2912844785502987960L;

        // variables .........................................................................................

        /** Delegating component */
        private HtmlBrowser browserComponent;


        // initialization ....................................................................................

        /**
        * Creates new html browser with toolbar and status line.
        */
        public BrowserComponent () {
            this (true, true);
        }

        /**
        * Creates new html browser.
        */
        public BrowserComponent (boolean toolbar, boolean statusLine) {
            setName (NbBundle.getBundle (HtmlBrowser.class).getString ("CTL_WebBrowser"));
            setLayout (new BorderLayout ());
            add (browserComponent = new HtmlBrowser (toolbar, statusLine), "Center"); // NOI18N

            // listen on changes of title and set name of top component
            browserComponent.browserImpl.addPropertyChangeListener (
                new PropertyChangeListener () {
                    public void propertyChange (PropertyChangeEvent e) {
                        if (!e.getPropertyName ().equals (Impl.PROP_TITLE)) return;
                        String title = browserComponent.browserImpl.getTitle ();
                        if ((title == null) || (title.length () < 1)) return;
                        BrowserComponent.this.setName (title);
                    }
                });
        }

        /* Serialize this top component.
        * @param out the stream to serialize to
        */
        public void writeExternal (ObjectOutput out)
        throws IOException {
            super.writeExternal (out);
            out.writeBoolean (isStatusLineVisible ());
            out.writeBoolean (isToolbarVisible ());
            out.writeObject (browserComponent.getDocumentURL ());
        }

        /* Deserialize this top component.
        * @param in the stream to deserialize from
        */
        public void readExternal (ObjectInput in)
        throws IOException, ClassNotFoundException {
            super.readExternal (in);
            setStatusLineVisible (in.readBoolean ());
            setToolbarVisible (in.readBoolean ());
            browserComponent.setURL ((URL) in.readObject ());
        }


        // TopComponent support ...................................................................

        protected CloneableTopComponent createClonedObject () {
            BrowserComponent bc = new BrowserComponent ();
            bc.setURL (getDocumentURL ());
            return bc;
        }

        public HelpCtx getHelpCtx () {
            return new HelpCtx (BrowserComponent.class);
        }

        protected void componentActivated () {
            browserComponent.browserImpl.getComponent ().requestFocus ();
            super.componentActivated ();
        }


        // public methods ....................................................................................

        /**
        * Sets new URL.
        *
        * @param str URL to show in this browser.
        */
        public void setURL (String str) {
            browserComponent.setURL (str);
        }

        /**
        * Sets new URL.
        *
        * @param str URL to show in this browser.
        */
        public void setURL (final URL url) {
            browserComponent.setURL (url);
        }

        /**
        * Gets current document url.
        */
        public final URL getDocumentURL () {
            return browserComponent.getDocumentURL ();
        }

        /**
        * Enables/disables Home button.
        */
        public final void setEnableHome (boolean b) {
            browserComponent.setEnableHome (b);
        }

        /**
        * Enables/disables location.
        */
        public final void setEnableLocation (boolean b) {
            browserComponent.setEnableLocation (b);
        }

        /**
        * Gets status line state.
        */
        public boolean isStatusLineVisible () {
            return browserComponent.isStatusLineVisible ();
        }

        /**
        * Shows/hides status line.
        */
        public void setStatusLineVisible (boolean v) {
            browserComponent.setStatusLineVisible (v);
        }

        /**
        * Gets status toolbar.
        */
        public boolean isToolbarVisible () {
            return browserComponent.isToolbarVisible ();
        }

        /**
        * Shows/hides toolbar.
        */
        public void setToolbarVisible (boolean v) {
            browserComponent.setToolbarVisible (v);
        }

        /**
        * Adds property change listener.
        */
        public void addPropertyChangeListener (PropertyChangeListener l) {
            browserComponent.addPropertyChangeListener (l);
        }

        /**
        * Removes property change listener.
        */
        public void removePropertyChangeListener (PropertyChangeListener l) {
            browserComponent.removePropertyChangeListener (l);
        }
    }

    /**
    * This interface represents an implementation of html browser used in HtmlBrowser. Each BrowserImpl
    * implementation corresponds with some BrowserFactory implementation.
    */
    public static abstract class Impl {

        /** generated Serialized Version UID */
        static final long            serialVersionUID = 2912844785502962114L;

        /** The name of property representing status of html browser. */
        public static final String PROP_STATUS_MESSAGE = "statusMessage"; // NOI18N
        /** The name of property representing current URL. */
        public static final String PROP_URL = "url"; // NOI18N
        /** Title property */
        public static final String PROP_TITLE = "title"; // NOI18N
        /** forward property */
        public static final String PROP_FORWARD = "forward"; // NOI18N
        /** backward property name */
        public static final String PROP_BACKWARD = "backward"; // NOI18N
        /** history property name */
        public static final String PROP_HISTORY = "history"; // NOI18N

        /**
        * Returns visual component of html browser.
        *
        * @return visual component of html browser.
        */
        public abstract java.awt.Component getComponent ();

        /**
        * Reloads current html page.
        */
        public abstract void reloadDocument ();

        /**
        * Stops loading of current html page.
        */
        public abstract void stopLoading ();

        /**
        * Sets current URL.
        *
        * @param url URL to show in the browser.
        */
        public abstract void setURL (URL url);

        /**
        * Returns current URL.
        *
        * @return current URL.
        */
        public abstract URL getURL ();

        /**
        * Returns status message representing status of html browser.
        *
        * @return status message.
        */
        public abstract String getStatusMessage ();

        /** Returns title of the displayed page.
        * @return title 
        */
        public abstract String getTitle ();


        /** Is forward button enabled?
        * @return true if it is
        */
        public abstract boolean isForward ();

        /** Moves the browser forward. Failure is ignored.
        */
        public abstract void forward ();

        /** Is backward button enabled?
        * @return true if it is
        */
        public abstract boolean isBackward ();

        /** Moves the browser forward. Failure is ignored.
        */
        public abstract void backward ();

        /** Is history button enabled?
        * @return true if it is
        */
        public abstract boolean isHistory ();

        /** Invoked when the history button is pressed.
        */
        public abstract void showHistory ();

        /**
        * Adds PropertyChangeListener to this browser.
        *
        * @param l Listener to add.
        */
        public abstract void addPropertyChangeListener (PropertyChangeListener l);

        /**
        * Removes PropertyChangeListener from this browser.
        *
        * @param l Listener to remove.
        */
        public abstract void removePropertyChangeListener (PropertyChangeListener l);
    }

    /**
    * Implementation of BrowerFactory creates new instances of some Browser implementation.
    *
    * @see HtmlBrowser.BrowserImpl
    */
    public interface Factory {
        /**
        * Returns a new instance of BrowserImpl implementation.
        */
        public Impl createHtmlBrowserImpl ();
    }
}

/*
 * Log
 *  37   Gandalf-post-FCS1.35.1.0    4/5/00   Jan Jancura     
 *  36   Gandalf   1.35        1/13/00  Ian Formanek    NOI18N
 *  35   Gandalf   1.34        1/13/00  Ian Formanek    NOI18N
 *  34   Gandalf   1.33        1/12/00  Ian Formanek    NOI18N
 *  33   Gandalf   1.32        12/23/99 Jan Jancura     New version of HTML 
 *       browser support.
 *  32   Gandalf   1.31        12/17/99 David Simonek   #3087
 *  31   Gandalf   1.30        11/26/99 Patrik Knakal   
 *  30   Gandalf   1.29        10/27/99 Petr Hrebejk    When Location or home 
 *       butond are disabled it becomes invisible now
 *  29   Gandalf   1.28        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  28   Gandalf   1.27        10/10/99 Petr Hamernik   console debug messages 
 *       removed.
 *  27   Gandalf   1.26        9/30/99  Jan Jancura     Bug 3188
 *  26   Gandalf   1.25        7/19/99  Jesse Glick     Context help.
 *  25   Gandalf   1.24        7/12/99  Jesse Glick     Context help.
 *  24   Gandalf   1.23        7/11/99  David Simonek   window system change...
 *  23   Gandalf   1.22        6/9/99   Ian Formanek    Fixed resources for 
 *       package change
 *  22   Gandalf   1.21        6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  21   Gandalf   1.20        5/12/99  Jan Jancura     
 *  20   Gandalf   1.19        5/12/99  Jan Jancura     
 *  19   Gandalf   1.18        5/12/99  Jan Jancura     WWWBrowser support API 
 *       changed
 *  18   Gandalf   1.17        5/9/99   Ian Formanek    Preferred browser size 
 *       is 1/2 of screen in both axes
 *  17   Gandalf   1.16        4/19/99  Jesse Glick     [JavaDoc]
 *  16   Gandalf   1.15        4/9/99   Ian Formanek    Removed debug printlns
 *  15   Gandalf   1.14        4/8/99   Ian Formanek    Changed Object.class -> 
 *       getClass ()
 *  14   Gandalf   1.13        3/29/99  Ian Formanek    Added better preferred 
 *       size
 *  13   Gandalf   1.12        3/27/99  David Simonek   
 *  12   Gandalf   1.11        3/5/99   Ales Novak      
 *  11   Gandalf   1.10        3/2/99   Jan Jancura     BrowserFactory & 
 *       BrowserImpl moved to HtmlBrowser
 *  10   Gandalf   1.9         2/17/99  Ian Formanek    Updated icons to point 
 *       to the right package (under ide/resources)
 *  9    Gandalf   1.8         2/17/99  Jan Jancura     
 *  8    Gandalf   1.7         2/17/99  Jan Jancura     
 *  7    Gandalf   1.6         2/16/99  Jan Jancura     
 *  6    Gandalf   1.5         2/16/99  Jan Jancura     
 *  5    Gandalf   1.4         1/20/99  Petr Hamernik   
 *  4    Gandalf   1.3         1/7/99   Ian Formanek    fixed resource names
 *  3    Gandalf   1.2         1/6/99   Ian Formanek    Reflecting changes in 
 *       location of package "awt"
 *  2    Gandalf   1.1         1/6/99   Jaroslav Tulach 
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 * Beta Change History:
 */
