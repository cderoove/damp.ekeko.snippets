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

package org.netbeans.core;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.*;
import java.io.*;
import java.util.MissingResourceException;
import java.util.StringTokenizer;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.*;
import javax.swing.text.html.*;

import org.openide.TopManager;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 * This class implements "Tip Of The Day" dialog.  Tips are stored in
 * "org.netbeans.docs.tips.tip#.html" files.  The IDE should
 * remembered the last shown tip (after closing this dialog), so that the next
 * time a new tip will be shown.  It is also possible to determine (via
 * checkbox) if the dialog should appear the next time the IDE starts.
 *
 * @author   Jan Palka, Ian Formanek, David Peroutka
 * @version  1.4, Sep 11, 1998
 */
public class TipsOnStartup extends JPanel {
    /** generated Serialized Version UID */
    static final long serialVersionUID = -211831677338914061L;

    // default size of the dialog
    private static final int DEFAULT_WIDTH = 400;
    private static final int DEFAULT_HEIGHT = 200;
    private static final String DEFAULT_PREFIX = "org.netbeans.docs.tips.tip"; // NOI18N

    /** total number of tips*/
    private int number = 0;
    /** current tip */
    int last = 1;
    /** proxy of current IDESettings */
    IDESettings ideSettings;

    private JScrollPane nextPane;
    private JPanel TipsPanel;
    private JTextArea jta;
    private JCheckBox jcx;
    private JScrollPane textScroll;
    private CardLayout cardLayout;
    private JLabel imageLabel;
    private JEditorPane browser;

    /** Creates TipsOnStartup dialog.
    * @param ide current ide settings
    */
    public TipsOnStartup () {
        super();
        this.ideSettings = new IDESettings ();

        final java.util.ResourceBundle bundle = NbBundle.getBundle(TipsOnStartup.class);

        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(5, 7, 5, 7));

        JLabel jlbl =  new JLabel (new javax.swing.ImageIcon (
                                       java.awt.Toolkit.getDefaultToolkit ().getImage (
                                           getClass ().getResource ("/org/netbeans/core/resources/startupTips.gif")))); // NOI18N
        jlbl.setBorder (new EmptyBorder (0, 0, 0, 5));

        JLabel dynText =  new JLabel (bundle.getString("CTL_DID_YOU_KNOW"));
        Font dynFont = dynText.getFont();
        dynText.setFont(new Font(dynFont.getName(), Font.BOLD, dynFont.getSize() + 4));

        JPanel northPanel = new JPanel ();
        northPanel.setLayout(new BorderLayout ());
        northPanel.add(jlbl, BorderLayout.WEST);
        northPanel.add(dynText, BorderLayout.CENTER);
        northPanel.setBorder(new EmptyBorder(5, 0, 5, 0));
        add(northPanel, BorderLayout.NORTH);

        // initialize styled document browser
        browser = new JEditorPane();
        browser.setEditable(false);
        browser.setEditorKit (new javax.swing.text.html.HTMLEditorKit ());
        //    browser.setContentType("text/html"); // NOI18N
        browser.setBackground (java.awt.Color.white);
        browser.setBorder(new EtchedBorder(EtchedBorder.LOWERED));
        browser.addHyperlinkListener(new HyperlinkListener() {
                                         public void hyperlinkUpdate(HyperlinkEvent e) {
                                             if (e.getEventType () == HyperlinkEvent.EventType.ACTIVATED) {
                                                 // close our dialog first
                                                 if (Main.tipsDialog != null)
                                                     Main.tipsDialog.setVisible(false);
                                                 TopManager tom = TopManager.getDefault();
                                                 tom.setStatusText(bundle.getString("CTL_OpeningBrowser"));
                                                 tom.showUrl(e.getURL());
                                                 TopManager.getDefault().setStatusText(""); // NOI18N
                                             }
                                         }
                                     }
                                    );
        add(new JScrollPane(browser), BorderLayout.CENTER);

        jcx = new JCheckBox(bundle.getString("CTL_NEXTSTARTUP"), true);
        jcx.setBorder(new EmptyBorder(5, 0, 0, 0));
        jcx.setSelected(ideSettings.getShowTipsOnStartup());
        jcx.addItemListener(new ItemListener() {
                                public void itemStateChanged (ItemEvent e) {
                                    ideSettings.setShowTipsOnStartup(e.getStateChange() == ItemEvent.SELECTED);
                                }
                            }
                           );
        add (jcx, BorderLayout.SOUTH);
    }

    /** Called when the tips dialog is about to be displayed to allow the TipsOnStartup to perform initialization */
    void restore () {
        last = ideSettings.getLastTip();
        jcx.setSelected(ideSettings.getShowTipsOnStartup());
        nextTip();
    }

    /**
     * Shows next tip into given text area
     */
    void nextTip() {
        java.net.URL url;
        try {
            url = NbBundle.getLocalizedFile(DEFAULT_PREFIX + last++, "html"); // NOI18N
        } catch (MissingResourceException e) {
            last = 1;
            try {
                url = NbBundle.getLocalizedFile(DEFAULT_PREFIX + last++, "html"); // NOI18N
            } catch (MissingResourceException e2) {
                url = null;
            }
        }
        if (url == null) {
            TopManager.getDefault ().notify (
                new NotifyDescriptor.Message (
                    NbBundle.getBundle (TipsOnStartup.class).getString ("ERR_NoTipsFound"),
                    NotifyDescriptor.WARNING_MESSAGE));
            return;
        }

        ideSettings.setLastTip (last);

        // Assertion.assert(url != null);
        try {
            browser.setPage(url);
        } catch (IOException e) {
            // PENDING(david) EXC_URL_Not_found bundle
            TopManager.getDefault().notifyException(e);
        }
    }

    /**
    * Returns default preffered size.
    */
    public Dimension getPreferredSize () {
        return new Dimension (DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

}

/*
 * Log
 *  7    Gandalf   1.6         03/26/99 Ian Formanek    TipsOfTheDay "Gandalfed"
 *  6    Gandalf   1.5         03/09/99 Jaroslav Tulach ButtonBar
 *
 *  5    Gandalf   1.4         03/09/99 Jan Jancura     Bundles moved
 *  4    Gandalf   1.3         01/20/99 Jaroslav Tulach
 *  3    Gandalf   1.2         01/06/99 David Simonek
 *  2    Gandalf   1.1         01/06/99 Ian Formanek    Reflecting changes in
 *                                                      location of package "awt"
 *  1    Gandalf   1.0         01/05/99 Ian Formanek
 * $
 */
