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

import java.net.URL;
import java.net.MalformedURLException;

import org.openide.util.HelpCtx;

/**
 * A NetworkOptions JavaBean.
 * @author Ales Novak
 */
public class NetworkOptions extends org.openide.options.SystemOption {

    /** homeURL property name */
    public final static String PROP_HOME_URL = "homeURL"; // NOI18N
    /** home */
    private final static String DEFAULT_HOME =
        org.openide.util.NbBundle.getBundle (NetworkOptions.class).getString ("URL_default_home_page");

    /** "homeURL" property value. */ // NOI18N
    private static URL homeURL;

    static final long serialVersionUID =-4392395920331209408L;
    /**
     * Constructs a new NetworkOptions JavaBean.
     */
    public NetworkOptions() {
    }

    /**
     * Getter method for the "homeURL" property.
     *
     * @return  The current value of this property.
     */
    public String getHomeURL() {
        return getStaticHomeURL().toString();
    }

    /**
     * Setter method for the "homeURL" property.
     *
     * @param homeURL The new value of this property.
     */
    public void setHomeURL(String value) {
        if (homeURL == null) initializeHomeURL();
        if (value == null || value.equals("") || // NOI18N
                value.equals(homeURL.toString())) return;
        URL u = null;
        try {
            u = new URL(value);
        } catch (MalformedURLException ex) {
            return;
        }
        String oldValue = homeURL.toString();
        homeURL = u;
        // fire the PropertyChange
        firePropertyChange(PROP_HOME_URL, oldValue, homeURL.toString());
    }

    /**
     * Getter method for the "smtpServer" property.
     *
     * @return  The current value of this property.
     */
    public String getSmtpServer() {
        return System.getProperty("mail.host", "");
    }

    /**
     * Setter method for the "smtpServer" property.
     *
     * @param smtpServer The new value of this property.
     */
    public void setSmtpServer(String value) {
        if (value == null) return;
        System.getProperties().put("mail.host", value); // NOI18N
    }

    /** Display name */
    public String displayName() {
        return org.openide.util.NbBundle.getBundle(NetworkOptions.class).
               getString("CTL_Network_opt_name");
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx (NetworkOptions.class);
    }

    /** Returns homeURL property, initializes if desired */
    static URL getStaticHomeURL () {
        if (homeURL != null) return homeURL;
        initializeHomeURL();
        return homeURL;
    }

    private static void initializeHomeURL () {
        try {
            homeURL = new URL (DEFAULT_HOME);
        } catch (MalformedURLException e) {
            try {
                homeURL = new URL ("http://"); // NOI18N
            } catch (MalformedURLException e2) {
                throw new InternalError ("Error initializing Home URL"); // NOI18N
            }
        }
    }

}

/*
 * Log
 */
