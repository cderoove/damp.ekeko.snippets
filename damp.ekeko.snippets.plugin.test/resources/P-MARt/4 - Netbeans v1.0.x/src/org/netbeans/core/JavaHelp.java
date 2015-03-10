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

import java.awt.*;
import java.beans.*;
import java.io.*;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.*;
import java.util.List; // override java.awt.List
import java.util.Map; // override javax.help.Map
import javax.swing.*;
import javax.swing.plaf.ComponentUI;

import org.openide.TopManager;
import org.openide.awt.HtmlBrowser;
import org.openide.text.EditorSupport;
import org.openide.util.*;
import org.openide.util.io.SafeException;
import org.openide.windows.*;

import javax.help.*;
import javax.help.event.*;
import javax.help.plaf.HelpContentViewerUI;
import javax.help.plaf.basic.BasicHelpUI;

/** Help implementation using the JavaHelp 1.0 system.
* Should only be loaded if JavaHelp is installed in the class path.
* @author Jesse Glick
*/
class JavaHelp implements Help.Impl {

    /** The default instance of the JavaHelp implementation; there
     *should not be more than one.
     */
    private static JavaHelp DEFAULT =null;

    /** Make a JavaHelp implementation of the Help.Impl interface.
     *Or, use {@link #getDefaultJavaHelp}.
     */
    public JavaHelp() {
        DEFAULT = this;
    }

    /** Get the default instance of the JavaHelp implementation.
     * @return the default instance
     */
    static JavaHelp getDefaultJavaHelp() {
        if (DEFAULT == null)
            return new JavaHelp ();
        else
            return DEFAULT;
    }

    /** Base resource string to the dummy master help set.
     *Just contains a title and empty standard content navigators
     *permitting other help sets to be merged into it.
     */
    private static final String MASTER_RESOURCE ="org.netbeans.core.resources.masterHelpSet"; // NOI18N
    /** As MASTER_RESOURCE, but now the file extension. */
    private static final String MASTER_RESOURCE_EXT = "xml"; // NOI18N
    /** A helpID present only in the master help set;
     *however, when displayed by {@link #showHelp} as the helpID in a context,
     *the master help set (with merged-in children) will be shown instead,
     *with no change made to the content pane.
     *Also, this is the help ID mapped to the "default" page in the master help viewer.
     */
    private static final String MASTER_ID ="org.netbeans.core.JavaHelp.MASTER_ID"; // NOI18N

    /** Internal name of the window manager {@link Mode} to be used
     *by default for {@link HelpComponent}s.
     */
    private static final String JAVAHELP_MODE ="JavaHelp"; // window manager mode // NOI18N

    /** Map from help set URLs to the help sets themselves.
     *Type <CODE>Map&lt;URL,HelpSet&rt;</CODE>.
     */
    private Map setsByUrl = new HashMap (); // Map<URL,HelpSet>
    /** Map from module code name to the home ID for that module.
     *Of type <CODE>Map&lt;String,String&gt;</CODE>.
     */
    private Map homesByCode = new HashMap (); // Map<String,String>
    /** Map from display names of modules (or the displayed name under Features,
     *often help set title) to the home IDs. Note that the display names
     *are guaranteed to be unique, using suffixes if needed.
     *Of type <CODE>Map&lt;String,String&gt;</CODE>.
     */
    private Map homesByDisplay =new HashMap (); // Map<String,String>
    /** Map from help set URLs to module code names.
     *Of type <CODE>Map&lt;URL,String&gt;</CODE>.
     * @associates String
     */
    private Map codesByUrl =new HashMap (); // Map<URL,String>
    /** Map from help set URLs to module display names (or help set titles).
     *Of type <CODE>Map&lt;URL,String&gt;</CODE>.
     * @associates String
     */
    private Map displaysByUrl =new HashMap (); // Map<URL,String>
    /** Set of home IDs which should be prominently displayed.
    * Of type <code>Set&lt;String&rt;</code>.
    */
    private Set distinguishedHomeIDs = new HashSet (); // Set<String>
    /** The master help set.
     */
    private HelpSet master =null;

    /** Get the master help set that others will be merged into.
     * @return the master help set
     */
    private HelpSet getMaster() {
        if (master == null) {
            ClassLoader loader = JavaHelp.class.getClassLoader ();
            try {
                master = new HelpSet (loader, NbBundle.getLocalizedFile
                                      (MASTER_RESOURCE, MASTER_RESOURCE_EXT, Locale.getDefault (), loader));
            } catch (HelpSetException hse) {
                TopManager.getDefault ().notifyException (hse);
            }
        }
        return master;
    }

    /** Add a help set to the system from a module.
     * @param resource The URL to the help set (<CODE>.hs</CODE>) file.
     * @param moduleCodeName The module code name (mostly used to look up context
     *help from {@link ModuleNode.Item}).
     * @param moduleDisplayName The module's display name, used as a backup display
     *name for the help in case the <CODE>&lt;TITLE&rt;</CODE>
     *tag is missing.
     */
    public void addHelpSet(URL resource,String moduleCodeName,String moduleDisplayName) {
        try {
            HelpSet sub = new HelpSet (TopManager.getDefault ().currentClassLoader (), resource);
            String displayName = sub.getTitle ();
            boolean distinguished = displayName != null && displayName.endsWith ("*"); // NOI18N
            if (distinguished) {
                displayName = displayName.substring (0, displayName.length () - 1);
                sub.setTitle (displayName);
                //System.err.println("Found distinguished help set: " + displayName);
            }
            if (displayName == null || displayName.equals ("")) displayName = moduleDisplayName; // NOI18N
            if (homesByDisplay.containsKey (displayName)) {
                int i = 2;
                String test;
                while (homesByDisplay.containsKey (test = Main.getString ("LBL_HelpSet_Duplicate", displayName, String.valueOf (i)))) i++;
                displayName = test;
            }
            javax.help.Map.ID homeID = sub.getHomeID ();
            if (homeID != null) {
                homesByCode.put (moduleCodeName, homeID.id);
                homesByDisplay.put (displayName, homeID.id);
                if (distinguished) distinguishedHomeIDs.add (homeID.id);
            }
            setsByUrl.put (resource, sub);
            codesByUrl.put (resource, moduleCodeName);
            displaysByUrl.put (resource, displayName);
            getMaster ().add (sub);
        } catch (HelpSetException hse) {
            TopManager.getDefault ().notifyException (hse);
        }
    }

    /** Remove a help set from the system (i.e. when the module is uninstalled).
     * @param resource The URL to the help set to be removed.
     */
    public void removeHelpSet(URL resource) {
        String code = (String) codesByUrl.remove (resource);
        if (code != null) homesByCode.remove (code);
        String display = (String) displaysByUrl.remove (resource);
        if (display != null) {
            String home = (String) homesByDisplay.remove (display);
            if (home != null) distinguishedHomeIDs.remove (home);
        }
        HelpSet hs = (HelpSet) setsByUrl.remove (resource);
        if (hs != null) getMaster ().remove (hs);
    }

    /** Get the master index name.
    * @return a display name
    */
    public String getMasterDisplayName () {
        return Main.getString ("LBL_MasterIndex");
    }

    /** Get the master ID.
    * Will instead return nothing if either:
    * <ol>
    * <li>There are no help sets installed, in which case the whole
    * thing would not even be useful to display.
    * <li>There is exactly one help set installed, and it specifies
    * a home ID, meaning that all possible help information is already
    * displayed in its own frame.
    * </ol>
    * @return the dummy help ID, or <code>null</code>
    */
    public String getMasterID () {
        if (setsByUrl.size () == 0 ||
                (setsByUrl.size () == 1 && homesByCode.size () == 1))
            return null;
        else
            return MASTER_ID;
    }

    /** Test whether a helpset should be displayed prominently.
    * Currently this is only true if the title of the help set
    * as originally found in XML ended with an asterisk, as a
    * special hack marker.
    * @param homeID the home ID to test
    * @return whether the helpset should be prominent
    */
    public boolean isDistinguished (String homeID) {
        return distinguishedHomeIDs.contains (homeID);
    }

    /** Show some help.
     *This is the basic call which should be used externally
     *and is the result of {@link TopManager#showHelp}.
     *Handles null contexts, missing or null help IDs, and null URLs.
     *If possible, looks for an existing help window in the proper mode
     *on this workspace that is showing the same help set as this context
     *help object requests. If there is any problem, shows the master set
     *instead, or it may also create a new help window.
     *All display is done using {@link HelpComponent}.
     *Works correctly if invoked while a dialog is open--creates a new modal
     *dialog with the help.
     * @param ctx the help context to display
     */
    public void showHelp(HelpCtx ctx) {
        //System.err.println("showing help: " + ctx);
        if (ctx == null) ctx = HelpCtx.DEFAULT_HELP;
        NbPresenter dlg = NbPresenter.currentModalDialog;
        if (dlg == null) {
            //System.err.println("showing as non-dialog");
            String helpID = ctx.getHelpID ();
            HelpSet hs = findHelpSetForID (helpID);
            //System.err.println("desired helpset: " + hs.getTitle ());
            HelpComponent comp = findExistingViewer (hs);
            //System.err.println("existing viewer: " + comp);
            if (comp == null) {
                //System.err.println("creating new viewer");
                comp = new HelpComponent (getJHelp (ctx));
                Workspace ws = TopManager.getDefault ().getWindowManager ().getCurrentWorkspace ();
                Mode m = ws.findMode (JAVAHELP_MODE);
                if (m == null) m = ws.createMode (JAVAHELP_MODE, Main.getString ("LBL_JavaHelp_mode"),
                                                      JavaHelp.class.getClassLoader ().getResource
                                                      ("org/netbeans/core/resources/actions/moduleHelp.gif")); // NOI18N
                // Reuse editor mode bounds, possibly scaled to 3/4, but assuming 920x660 desirable size:
                Mode edmode = ws.findMode (EditorSupport.EDITOR_MODE);
                if (edmode != null) {
                    Rectangle r = edmode.getBounds ();
                    int width = r.width * 3 / 4;
                    if (width < 920) width = r.width;
                    int height = r.height * 3 / 4;
                    if (height < 660) height = r.height;
                    m.setBounds (new Rectangle (r.x, r.y, width, height));
                }
                if (m.canDock (comp)) m.dockInto (comp);
            }
            if (hs.equals (getMaster ())) {
                //System.err.println("desired was master help set");
                if (helpID != null && ! helpID.equals (MASTER_ID))
                    warnBadID (helpID);
            } else {
                //System.err.println("regular help display");
                try {
                    comp.getJHelp ().setCurrentID (helpID);
                } catch (BadIDException bide) {
                    if (Boolean.getBoolean ("netbeans.debug.exceptions")) // NOI18N
                        bide.printStackTrace ();
                }
            }
            //System.err.println("opening " + comp + "...");
            comp.open ();
            comp.requestFocus ();
        } else {
            //System.err.println("showing as dialog");
            JDialog d1 = new JDialog (dlg);
            d1.getContentPane ().add ("Center", getJHelp (ctx)); // NOI18N
            d1.setModal (true);
            d1.setTitle (Main.getString ("CTL_Help"));
            d1.pack ();
            d1.show ();
        }
    }

    /** Find the proper help set for an ID.
    * @param id the ID to look up
    * @return the proper help set (master if not otherwise found)
    */
    private HelpSet findHelpSetForID (String id) {
        if (id == null) return getMaster ();
        Iterator it = setsByUrl.values ().iterator ();
        while (it.hasNext ()) {
            HelpSet hs = (HelpSet) it.next ();
            if (hs.getCombinedMap ().isValidID (id, hs))
                return hs;
        }
        return getMaster ();
    }

    /** Find the best viewer to show this help set in.
    * Looks for one that is in the current workspace; has the same current help set
    * (and not the master help set, unless the master was requested); is visible;
    * and is in the JavaHelp mode. Criteria in that order.
    * <p>Note: since deserialization now discards previously hidden viewers, this
    * method is no longer so important: especially searching hidden viewers is
    * not critical. To force it to be visible in order to return it, use:
    * spec=4 vis=8 ws=2 mod=1 min=11 (bestScore = 10)
    * @param hs the desired help set
    * @return an existing viewer for that help set, or for the master set, or <code>null</code>
    */
    private HelpComponent findExistingViewer (HelpSet hs) {
        boolean isMaster = hs.equals (getMaster ());
        // Current best match
        HelpComponent best = null;
        // Current best score; minimum cut-off before we create a new one
        int bestScore = 6;
        //System.err.println("bestScore=" + bestScore);
        Set visible = TopComponent.getRegistry ().getOpened ();
        WindowManager wm = TopManager.getDefault ().getWindowManager ();
        Workspace curr = wm.getCurrentWorkspace ();
        Workspace[] wss = wm.getWorkspaces ();
        for (int i = 0; i < wss.length; i++) {
            Workspace ws = wss[i];
            //System.err.println("ws=" + ws.getName ());
            boolean thisIsCurrWs = curr.equals (ws);
            Iterator modes = ws.getModes ().iterator ();
            while (modes.hasNext ()) {
                Mode mode = (Mode) modes.next ();
                if (mode == null) continue;
                //System.err.println("mode=" + mode.getName());
                // Sometimes we get a null mode, or maybe a mode with a null name...?
                boolean thisIsCurrMode = JAVAHELP_MODE.equals (mode.getName ());
                TopComponent[] tcs = mode.getTopComponents ();
                for (int j = 0; j < tcs.length; j++) {
                    if (! (tcs[j] instanceof HelpComponent)) continue;
                    HelpComponent hc = (HelpComponent) tcs[j];
                    JHelp jh = hc.getJHelp ();
                    if (jh == null) {
                        //System.err.println("uninitialized HelpComponent found!");
                        continue;
                    }
                    HelpSet tesths = jh.getModel ().getHelpSet ();
                    //System.err.println("tesths=" + tesths.getTitle ());
                    boolean testIsMaster = tesths.equals (getMaster ());
                    if (! hs.equals (tesths) && ! testIsMaster) {
                        //System.err.println("skipping inappropriate helpset");
                        continue;
                    }
                    int score = 0;
                    // Help component is visible:
                    if (visible.contains (hc)) score += 4;
                    // Specific help:
                    if (isMaster || ! testIsMaster) score += 8;
                    // On the same workspace:
                    if (thisIsCurrWs) score += 2;
                    // On the same mode:
                    if (thisIsCurrMode) score += 1;
                    //System.err.println("score=" + score);
                    if (score > bestScore) {
                        //System.err.println("(new best)");
                        bestScore = score;
                        best = hc;
                    }
                }
            }
        }
        return best;
    }

    /** Get home IDs by code name.
     * @see #homesByCode
     * @return a map
     */
    public Map getHomesByCode() {
        return homesByCode;
    }

    /** Get the home IDs by display name.
     * @see #homesByDisplay
     * @return a map
     */
    public Map getHomesByDisplay() {
        return homesByDisplay;
    }

    /** Create a new JHelp component displaying the given help.
     * @param ctx the help to display (may be incorrect or partial)
     * @return a new JHelp component, hopefully displaying the desired help
     */
    private JHelp getJHelp(HelpCtx ctx) {
        return getJHelp (ctx.getHelpID (), ctx.getHelp ());
    }

    /** Warn that an ID was not found in any help set.
    * @param id the help ID
    */
    private static void warnBadID (String id) {
        // PLEASE DO NOT COMMENT OUT...localized warning
        System.err.println (Main.getString ("MSG_jh_id_not_found", id));
    }

    /** Create a new JHelp component displaying some help.
     *Handles {@link #MASTER_ID}, as well as help IDs
     *that were not found in any help set, various exceptions, etc.
     *Should always return a valid JHelp, and not throw any exceptions
     *based on the arguments.
     * @param helpID a help ID string to display, may be <CODE>null</CODE>
     * @param url a URL to display, may be <CODE>null</CODE>; lower priority than the help ID
     * @return a valid JHelp, hopefully displaying the requested help
     */
    private synchronized JHelp getJHelp(String helpID,URL url) {
        //System.err.println("getting a helpAndSet: " + helpID + " " + url);
        try {
            if (MASTER_ID.equals (helpID)) {
                // Special "helpID" only used to display master help. // NOI18N
                //System.err.println("uncached master");
                return createMasterJHelp ();
            } else {
                // Regular help context.
                HelpSet hs = getMaster ();
                javax.help.Map.ID id = null;
                if (helpID != null) {
                    Iterator it = setsByUrl.values ().iterator ();
                    while (it.hasNext ()) {
                        HelpSet test = (HelpSet) it.next ();
                        if (test.getCombinedMap ().isValidID (helpID, test)) {
                            hs = test;
                            id = javax.help.Map.ID.create (helpID, test);
                            //System.err.println("found in helpset cache: " + hs.getTitle () + " " + id);
                            break;
                        }
                    }
                    if (hs == getMaster ()) {
                        warnBadID (helpID);
                    }
                }
                JHelp jHelp = new JHelp (hs);
                if (id != null)
                    jHelp.setCurrentID (id);
                else if (url != null)
                    jHelp.setCurrentURL (url);
                //System.err.println("uncached: " + jHelp + " " + hs.getTitle ());
                return jHelp;
            }
        } catch (BadIDException bide) {
            // Should not happen:
            TopManager.getDefault ().notifyException (bide);
            return createMasterJHelp ();
        } catch (InvalidHelpSetContextException ihsce) {
            // Should not happen:
            TopManager.getDefault ().notifyException (ihsce);
            return createMasterJHelp ();
        }
    }

    /** Create &amp; return a JHelp with the master set.
    * @return the new JHelp
    */
    private JHelp createMasterJHelp () {
        JHelp jh = new JHelp (getMaster ());
        try {
            jh.setCurrentID (MASTER_ID);
        } catch (BadIDException bide) {
            if (Boolean.getBoolean ("netbeans.debug.exceptions")) // NOI18N
                bide.printStackTrace ();
        }
        return jh;
    }

    /** A top component displaying a single JHelp.
    * Support cloning and window system serialization.
    */
    public static class HelpComponent extends CloneableTopComponent {

        /** The JHelp component being displayed (usually non-null).
         */
        private JHelp jHelp =null;

        /** Serial version.
         */
        private static final long serialVersionUID =882544693090803112L;

        /** Whether to discard rather than deserialize. */
        private transient boolean discard = false;

        /** Public constructor, only for use during deserialization.
         */
        public HelpComponent() {
            //System.err.println("creating HelpComponent");
            SwingUtilities.invokeLater (new Runnable () {
                                            public void run () {
                                                //System.err.println("creating HelpComponent (II)");
                                                // Should not normally be visible:
                                                setName (Main.getString ("LBL_Empty_HelpComponent"));
                                            }
                                        });
        }

        /** Regular constructor.
         * @param jHelp the JHelp component to display
         */
        public HelpComponent(JHelp jHelp) {
            this ();
            setJHelp (jHelp);
        }

        /** Get the help context.
         * @return context help for this component
         */
        public HelpCtx getHelpCtx() {
            return new HelpCtx (HelpComponent.class);
        }

        /** Get the associated JHelp component.
         * @return the JHelp component
         */
        public JHelp getJHelp() {
            return jHelp;
        }

        /** Install a JHelp component during deserialization.
         *Should not be called twice, or after the regular constructor.
         * @param nue the JHelp to install
         */
        public synchronized void setJHelp(JHelp nue) {
            //System.err.println("installing jHelp");
            if (jHelp != null) throw new IllegalArgumentException ("Cannot reinstall the JHelp component"); // NOI18N
            if (nue == null) throw new NullPointerException ("Not supported to un-set JHelp components"); // NOI18N
            jHelp = nue;
            SwingUtilities.invokeLater (new Runnable () {
                                            public void run () {
                                                //System.err.println("setJHelp (II)");
                                                removeAll ();
                                                setLayout (new BorderLayout ());
                                                add (jHelp, BorderLayout.CENTER);
                                                setName (jHelp.getModel ().getHelpSet ().getTitle ());
                                                revalidate ();
                                            }
                                        });
        }

        /** Read component from disk.
         * @serialData Super method; then Boolean (not boolean!) discard; String id; then URL url.
         * Either id or url may be <code>null</code>.
         * @param in the input stream to read from
         * @throws IOException for the usual reasons
         * @throws ClassNotFoundException for the usual reasons
         */
        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
            super.readExternal (in);
            //System.err.println("reading HelpComponent");
            // Backwards compatiblity: accepts missing "discard". // NOI18N
            String id;
            Object readMe = in.readObject ();
            if (readMe instanceof String) {
                discard = false;
                id = (String) readMe;
            } else {
                discard = ((Boolean) readMe).booleanValue ();
                id = (String) in.readObject ();
            }
            URL url = (URL) in.readObject ();
            if (! discard) {
                try {
                    setJHelp (getDefaultJavaHelp ().getJHelp (id, url));
                } catch (Exception e) {
                    if (Boolean.getBoolean ("netbeans.debug.exceptions")) // NOI18N
                        e.printStackTrace ();
                    throw new SafeException (e);
                }
            }
        }

        /** Writes to serialization.
         * @serialData Super, then Boolean discard, String id, URL url.
         * Either id or url may be <code>null</code>.
         * @param out stream to write to
         * @throws IOException for the usual reasons
         */
        public void writeExternal(ObjectOutput out) throws IOException {
            super.writeExternal (out);
            //System.err.println("writing: " + jHelp);
            // If not open anywhere, please discard me:
            out.writeObject (new Boolean (! isOpened ()));
            Object toWrite1 = null;
            Object toWrite2 = null;
            try {
                if (jHelp != null) {
                    javax.help.Map.ID id = jHelp.getModel ().getCurrentID ();
                    if (id != null) toWrite1 = id.id;
                    toWrite2 = jHelp.getModel ().getCurrentURL ();
                }
            } catch (Exception e) {
                if (Boolean.getBoolean ("netbeans.debug.exceptions")) // NOI18N
                    e.printStackTrace ();
                throw new SafeException (e);
            } finally {
                out.writeObject (toWrite1);
                out.writeObject (toWrite2);
            }
        }

        /** Possibly discard this component rather than deserialize.
        * @return this, or <code>null</code> if it was hidden when saved and should not be deserialized
        * @throws ObjectStreamException will not be thrown
        */
        public Object readResolve () throws ObjectStreamException {
            //System.err.println("HelpComponent.readResolve; discard=" + discard);
            return discard ? null : this;
        }

        /** Create a cloned viewer.
        * Tries to open in the correct mode.
         * @return a viewer displaying the same help ID and/or URL
         */
        protected CloneableTopComponent createClonedObject() {
            //System.err.println("cloning component...");
            CloneableTopComponent toRet;
            if (jHelp == null) {
                toRet = new HelpComponent ();
            } else {
                javax.help.Map.ID id = jHelp.getModel ().getCurrentID ();
                toRet = new HelpComponent (getDefaultJavaHelp ().getJHelp (id == null ? null : id.id,
                                           jHelp.getModel ().getCurrentURL ()));
            }
            Mode m = TopManager.getDefault ().getWindowManager ().getCurrentWorkspace ().findMode (JAVAHELP_MODE);
            if (m != null && m.canDock (toRet)) m.dockInto (toRet);
            return toRet;
        }

        /** Avoid problems during L&F changes.
         * JavaHelp components seem to blank out if updateUI() is called
         * on them, so it is necessary to make sure they are not part
         * of the container while this method is being called. It also
         * works to just throw an exception and stop the process, but
         * that is rather rude!
         */
        public void updateUI () {
            //System.err.println("HelpComponent.updateUI");
            if (jHelp != null) {
                //System.err.println("removing jHelp...");
                remove (jHelp);
                //System.err.println("super.updateUI...");
                super.updateUI ();
                //System.err.println("will call rest later...");
                SwingUtilities.invokeLater (new Runnable () {
                                                public void run () {
                                                    //System.err.println("re-adding fresh jHelp...");
                                                    // Reusing the same model does not seem to work, the last-selected
                                                    // ID will not be correctly reselected.
                                                    JHelp jh2 = new JHelp (jHelp.getModel ().getHelpSet ());
                                                    add (jh2, BorderLayout.CENTER);
                                                    jHelp = jh2;
                                                    //System.err.println("revalidating...");
                                                    revalidate ();
                                                }
                                            });
            }
        }

    }

    /** The current UIDefaults object, whose property changes will be listened to.
     */
    private static UIDefaults curr = UIManager.getLookAndFeelDefaults ();
    /** A listener to changes in the UI which attempts to keep the custom UIs
     *installed over the JavaHelp defaults.
     */
    private static PropertyChangeListener uiListener =new PropertyChangeListener () {
                public void propertyChange (PropertyChangeEvent ev) {
                    //System.err.println("uiListener.pC");
                    if (! BrowserContentViewerUI.class.getName ().equals (curr.get ("HelpContentViewerUI")) || // NOI18N
                            ! ToolbarlessHelpUI.class.getName ().equals (curr.get ("HelpUI"))) { // NOI18N
                        //System.err.println("uiListener: out of date");
                        //System.err.println("HelpContentViewerUI=" + curr.get ("HelpContentViewerUI"));
                        //System.err.println("HelpUI=" + curr.get ("HelpUI"));
                        curr.removePropertyChangeListener (uiListener);
                        curr.putDefaults (new Object[] {
                                              "HelpContentViewerUI", BrowserContentViewerUI.class.getName (), // NOI18N
                                              "HelpUI", ToolbarlessHelpUI.class.getName () // NOI18N
                                          });
                        curr.addPropertyChangeListener (uiListener);
                    }
                }
            };
    static {
        curr.addPropertyChangeListener (uiListener);
        uiListener.propertyChange (null);
        UIManager.addPropertyChangeListener (new PropertyChangeListener () {
                                                 public void propertyChange (PropertyChangeEvent ev) {
                                                     //System.err.println("UIManager PCE: " + ev.getPropertyName ());
                                                     if ("lookAndFeel".equals (ev.getPropertyName ())) {
                                                         curr.removePropertyChangeListener (uiListener);
                                                         curr = UIManager.getLookAndFeelDefaults ();
                                                         curr.addPropertyChangeListener (uiListener);
                                                         uiListener.propertyChange (null);
                                                     }
                                                 }
                                             });
    }

    /** Hack to take the toolbar out of the JHelp window. It is big and ugly
    * and the web browser already has a toolbar which contains back and
    * forward buttons which also work better (e.g. with frames).
    */
    public static class ToolbarlessHelpUI extends BasicHelpUI {

        /** Create the proper UI.
         * @param comp the JHelp component to render
         * @return the desired UI
         */
        public static ComponentUI createUI(JComponent comp) {
            return new ToolbarlessHelpUI ((JHelp) comp);
        }

        static final long serialVersionUID =-4593005151581696837L;
        /** Create a new UI.
         * @param jHelp the component to render
         */
        public ToolbarlessHelpUI(JHelp jHelp) {
            super (jHelp);
        }

        /** Installs the UI.
         *Just tries to remove the JToolBar.
         * @param c the component to render
         */
        public void installUI(JComponent c) {
            super.installUI (c);
            Component[] components = c.getComponents ();
            for (int i = 0; i < components.length; i++) {
                if (components[i] instanceof JToolBar) {
                    c.remove (components[i]);
                    return;
                }
            }
            // PLEASE DO NOT COMMENT OUT
            if (Boolean.getBoolean ("netbeans.debug.exceptions")) // NOI18N
                System.err.println ("Warning: could not remove toolbar from JHelp UI");
        }

    }

    /** Alternate UI for the content browser.
    * Instead of using the slow and buggy Swing Text HTML support,
    * uses {@link #HtmlBrowser} embedded browser.
    */
    public static class BrowserContentViewerUI extends HelpContentViewerUI implements Serializable, PropertyChangeListener, HelpModelListener {

        /** Create UI for a component.
         * @param comp the component to render
         * @return the UI
         */
        public static ComponentUI createUI(JComponent comp) {
            return new BrowserContentViewerUI ();
        }

        /** The actual browser component.
         */
        private HtmlBrowser browser;
        /** The content viewer being rendered.
         */
        private JHelpContentViewer viewer;

        static final long serialVersionUID =2565972402655318909L;
        /** Create a new UI.
         */
        public BrowserContentViewerUI() {
            //System.err.println("creating UI...");
        }

        /** Install UI for the component.
         * @param c the content navigator to render
         */
        public void installUI(JComponent c) {
            //System.err.println("installing UI to " + c);
            viewer = (JHelpContentViewer) c;
            viewer.addPropertyChangeListener (this);
            TextHelpModel model = viewer.getModel ();
            // [PENDING] should viewer.addHelpModelListener (this) be used instead??
            if (model != null) model.addHelpModelListener (this);
            // No TextHelpModelListener--we do not support highlighting.
            browser = new HtmlBrowser ();
            browser.setEnableHome (false);
            browser.setEnableLocation (false);
            browser.addPropertyChangeListener (this);
            viewer.setLayout (new BorderLayout ());
            viewer.add (browser, BorderLayout.CENTER);
        }

        /** Uninstall UI from the component.
         * @param c the content navigator
         */
        public void uninstallUI(JComponent c) {
            //System.err.println("uninstalling UI from " + c);
            JHelpContentViewer jhcv = (JHelpContentViewer) c;
            viewer.removePropertyChangeListener (this);
            TextHelpModel model = viewer.getModel ();
            if (model != null) model.removeHelpModelListener (this);
            if (browser != null) browser.removePropertyChangeListener (this);
            browser = null;
            viewer.setLayout (null);
            viewer.removeAll ();
            viewer = null;
        }

        /** Handle changes in browser.title and viewer.helpModel.
         * @param ev the event
         */
        public void propertyChange(PropertyChangeEvent ev) {
            //System.err.println("UI prop chg: [" + ev.getPropertyName () + "] " + ev.getOldValue () + " -> " + ev.getNewValue ());
            if (ev.getSource () == browser) {
                // Really a PROP_TITLE change, but what the heck.
                if (browser == null) return;
                URL url = browser.getDocumentURL ();
                if (url == null) return; // Probably should never happen.
                TextHelpModel model = viewer.getModel ();
                if (model != null) {
                    //System.err.println("Browser url set to " + url);
                    try {
                        model.removeHelpModelListener (this);
                        // model.setDocumentTitle (???);
                        javax.help.Map.ID id = model.getHelpSet ().getCombinedMap ().getIDFromURL (url);
                        if (id != null) {
                            //System.err.println("(setting by id)");
                            try {
                                model.setCurrentID (id);
                            } catch (InvalidHelpSetContextException ihsce) {
                                // Should probably not happen:
                                TopManager.getDefault ().notifyException (ihsce);
                                model.setCurrentURL (url);
                            }
                        } else {
                            //System.err.println("(setting by URL)");
                            model.setCurrentURL(url);
                        }
                    } finally {
                        model.addHelpModelListener (this);
                    }
                }
            } else {
                // Viewer.
                if ("helpModel".equals (ev.getPropertyName ())) {
                    //System.err.println("Changing help model...");
                    HelpModel old = (HelpModel) ev.getOldValue ();
                    if (old != null) old.removeHelpModelListener (this);
                    HelpModel nue = (HelpModel) ev.getNewValue ();
                    if (nue != null) nue.addHelpModelListener (this);
                }
            }
        }

        /** Handle changes in the help model's ID.
         * @param ev the event
         */
        public void idChanged(HelpModelEvent ev) {
            //System.err.println("UI: ID changed to " + ev.getID () + " " + ev.getURL ());
            if (browser != null) {
                try {
                    browser.removePropertyChangeListener (this);
                    browser.setURL (ev.getURL ());
                } finally {
                    browser.addPropertyChangeListener (this);
                }
            }
        }

    }

}

/*
 * Log
 *  26   Gandalf   1.25        1/18/00  Jesse Glick     Setting mode bounds to 
 *       something reasonable.
 *  25   Gandalf   1.24        1/13/00  Jaroslav Tulach I18N
 *  24   Gandalf   1.23        12/30/99 Jaroslav Tulach New dialog for 
 *       notification of exceptions.
 *  23   Gandalf   1.22        12/21/99 Jesse Glick     Putting User's Guide off
 *       from the rest of the help menu items to visually distinguish it.
 *  22   Gandalf   1.21        12/20/99 Jesse Glick     Finally got L&F changes 
 *       to completely work despite JavaHelp bug.
 *  21   Gandalf   1.20        12/20/99 Jesse Glick     Serialization 
 *       improvements.
 *  20   Gandalf   1.19        12/20/99 Jesse Glick     Reorganized Help | 
 *       Features to be Help | Documentation, killing old UG browse action, 
 *       better labelling of master help set, etc.
 *  19   Gandalf   1.18        11/26/99 Patrik Knakal   
 *  18   Gandalf   1.17        11/10/99 Jesse Glick     Fixing exception during 
 *       L&F change with JavaHelp component open.
 *  17   Gandalf   1.16        11/5/99  Jesse Glick     Workaround for winsys 
 *       bug.
 *  16   Gandalf   1.15        11/4/99  Jesse Glick     Overhauled JavaHelp: 
 *       esp. treatment of existing and new components. Should simplify impl and
 *       fix some bugs and improve UI too.
 *  15   Gandalf   1.14        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  14   Gandalf   1.13        10/6/99  Jesse Glick     Changing UI setting to 
 *       occur more deterministically (prop chg listeners etc.).
 *  13   Gandalf   1.12        10/5/99  Jesse Glick     Aggressiveness of UI 
 *       overriding seems to be improved.
 *  12   Gandalf   1.11        10/5/99  Jesse Glick     Less ugly prevention of 
 *       feedback loops with propchg's and idevent's in browser UI.
 *  11   Gandalf   1.10        10/5/99  Jesse Glick     Forcibly removing the 
 *       oversized ugly toolbar from the help window. It does not work as well 
 *       as the browser's own buttons anyway.
 *  10   Gandalf   1.9         10/5/99  Jesse Glick     Setting Help | Features 
 *       entry names by JavaHelp helpset title, rather than module display name,
 *       whenever possible.
 *  9    Gandalf   1.8         9/30/99  Jesse Glick     
 *  8    Gandalf   1.7         9/29/99  Jesse Glick     Commented-out printlns.
 *  7    Gandalf   1.6         9/29/99  Jesse Glick     Bugfix (infinite loop).
 *  6    Gandalf   1.5         9/29/99  Jesse Glick     Added custom UI based on
 *       installed HtmlBrowser.
 *  5    Gandalf   1.4         9/29/99  Jesse Glick     Now have distinction 
 *       between the HelpSet's used for individual module help, and the master 
 *       merged help which is available separately. Also using TopComponent's 
 *       for the help browser (unless invoked in a dialog).
 *  4    Gandalf   1.3         9/28/99  Jesse Glick     Starting with a nonempty
 *       master help set, so that modules can add navigator views to it 
 *       correctly.
 *  3    Gandalf   1.2         9/28/99  Jesse Glick     Just removing CR's.
 *  2    Gandalf   1.1         9/28/99  Jesse Glick     
 *  1    Gandalf   1.0         9/27/99  Jesse Glick     
 * $
 */
