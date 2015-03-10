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

package org.openide.modules;

import java.beans.Beans;
import java.io.*;
import java.net.URL;
import java.net.MalformedURLException;
import java.text.MessageFormat;
import java.util.*;
import java.util.jar.Manifest;
import java.util.jar.Attributes;

import org.openide.TopManager;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

// Informations extracted from the manifest then instructs the IDE to
// install actions, loaders, filesystem, etc.
/** A description of a module that is constructed from the module's manifest file.
* This provides a convenient way to parse a manifest file.
* @author Jaroslav Tulach, Ian Formanek, Jesse Glick
*/
public final class ModuleDescription extends Object {


    private static final boolean VERBOSE = Boolean.getBoolean ("org.openide.modules.ModuleDescription.VERBOSE"); // NOI18N

    // -----------------------------------------------------------------------------
    // Global tags

    /** Global tag for whole module. Identifies the JAR as a module and gives its code name. */
    public static final Attributes.Name TAG_MAGIC = new Attributes.Name ("OpenIDE-Module"); // NOI18N
    /** Display name of module. May be localized, e.g. <code>OpenIDE-Module-Name_cs</code>. */
    public static final Attributes.Name TAG_NAME = new Attributes.Name ("OpenIDE-Module-Name"); // NOI18N

    private static final Comparator codeNameComparator = new Comparator () {
                public int compare (Object o1, Object o2) {
                    ModuleDescription md1 = (ModuleDescription) o1;
                    ModuleDescription md2 = (ModuleDescription) o2;
                    return md1.getCodeName ().compareTo (md2.getCodeName ());
                }
            };

    // -----------------------------------------------------------------------------
    // Versioning tags

    /** Specification version tag for whole module. Identifies the specification version of the module. */
    public static final Attributes.Name TAG_SPEC_VERSION = new Attributes.Name ("OpenIDE-Module-Specification-Version"); // NOI18N
    /** Implementation version tag for whole module. Identifies the implementation version of the module. */
    public static final Attributes.Name TAG_IMPL_VERSION = new Attributes.Name ("OpenIDE-Module-Implementation-Version"); // NOI18N

    // -----------------------------------------------------------------------------
    // Dependency tags

    /** Module dependency tag for whole module. Identifies the modules on which this module depends. */
    public static final Attributes.Name TAG_MODULE_DEPENDENCIES = new Attributes.Name ("OpenIDE-Module-Module-Dependencies"); // NOI18N
    /** Package dependency tag for whole module. Identifies the package versions on which this module depends. */
    public static final Attributes.Name TAG_PACKAGE_DEPENDENCIES = new Attributes.Name ("OpenIDE-Module-Package-Dependencies"); // NOI18N
    /** Java dependency tag for whole module. Identifies the Java version on which this module depends. */
    public static final Attributes.Name TAG_JAVA_DEPENDENCIES = new Attributes.Name ("OpenIDE-Module-Java-Dependencies"); // NOI18N
    /** IDE dependency tag for whole module. Identifies the version of the core IDE on which this module depends. */
    public static final Attributes.Name TAG_IDE_DEPENDENCIES = new Attributes.Name ("OpenIDE-Module-IDE-Dependencies"); // NOI18N

    // -----------------------------------------------------------------------------
    // ModuleInstall tags

    /** Name of (optional) main class. Must be public with a no-argument public constructor, and
    * implement {@link ModuleInstall}.
    */
    public static final Attributes.Name TAG_MAIN = new Attributes.Name ("OpenIDE-Module-Install"); // NOI18N

    // -----------------------------------------------------------------------------
    // Module content tags

    /** Name of an (optional) JavaHelp-style help set.
    * May be localized.
    * <p>For example, the value <code>org.netbeans.module.Index</code> will look for one of the following, according to locale:
    * <p><code><PRE>
    * /org/netbeans/module/Index_cs.html
    * /org/netbeans/module/Index.html
    * </PRE></code>
    */
    public static final Attributes.Name TAG_DESCRIPTION = new Attributes.Name ("OpenIDE-Module-Description"); // NOI18N

    // [PENDING] icon tag

    /** Tag for a section. Identifies that the entry is specially treated somehow.
    */
    public static final Attributes.Name TAG_SECTION_CLASS = new Attributes.Name ("OpenIDE-Module-Class"); // NOI18N

    /** "Action" module section. */ // NOI18N
    public static final String SECTION_ACTION = "Action"; // NOI18N

    /** "Option" module section. */ // NOI18N
    public static final String SECTION_OPTION = "Option"; // NOI18N

    /** "Loader" module section. */ // NOI18N
    public static final String SECTION_LOADER = "Loader"; // NOI18N
    /** Option to install this loader before another. */
    public static final Attributes.Name TAG_INSTALL_BEFORE = new Attributes.Name ("Install-Before"); // NOI18N
    /** Option to install this loader after another. */
    public static final Attributes.Name TAG_INSTALL_AFTER  = new Attributes.Name ("Install-After"); // NOI18N

    /** "Filesystem" module section. */ // NOI18N
    public static final String SECTION_FILESYSTEM = "Filesystem"; // NOI18N
    /** Display name of a file system type. Used e.g. in popup menus to add a new instance to the Repository. */
    public static final Attributes.Name TAG_FILESYSTEM_NAME = new Attributes.Name ("Display-Name"); // NOI18N
    /** Help resource for a file system. */
    public static final Attributes.Name TAG_FILESYSTEM_HELP = new Attributes.Name ("Help"); // NOI18N

    /** "Service" module section. */ // NOI18N
    public static final String SECTION_SERVICE = "Service"; // NOI18N
    // UNUSED--
    /** Display name of an service.
    public static final Attributes.Name TAG_SERVICE_NAME = new Attributes.Name ("Display-Name");
    */
    /** Whether this service should be the default for its category. */
    public static final Attributes.Name TAG_SERVICE_DEFAULT = new Attributes.Name ("Default"); // NOI18N

    /** "Debugger" module section. */ // NOI18N
    public static final String SECTION_DEBUGGER = "Debugger"; // NOI18N

    /** "Node" module section. */ // NOI18N
    public static final String SECTION_NODE = "Node"; // NOI18N
    /** Option to distingiush between different types of nodes.
    * <p>Currently the possible types are <code>Environment</code>, <code>Roots</code>,
    * and <code>Session</code>. If the attribute
    * is missing, <code>Environment</code> is assumed.
    */
    public static final Attributes.Name TAG_NODE_TYPE = new Attributes.Name ("Type"); // NOI18N

    /** "ClipboardConvertor" module section. */ // NOI18N
    public static final String SECTION_CLIPBOARD_CONVERTOR = "ClipboardConvertor"; // NOI18N





    // -----------------------------------------------------------------------------
    // private variables

    /** (display) name of module */
    private String moduleName;
    // code name of module
    private String codeName;

    /** main class instance class */
    private String mainClass;
    /** main class instance */
    private ModuleInstall main;

    /** specification vesion */
    private String specVersion;
    /** implementation version */
    private String implVersion;

    // Dependencies of various sorts. Elements are of type Dependency.
    private Set dependenciesSet;

    /** resource for description */
    private String description;

    /** list of sections in the module */
    private ManifestSection[] sections;

    private static ResourceBundle bundle;

    // -----------------------------------------------------------------------------
    // Constructor

    /** Create new description from a provided manifest file.
    * It is assumed that the JAR file is already known to the class loader.
    * @param name name of the JAR file
    * @param man the manifest file within that JAR
    * @exception IllegalModuleException if there is an error reading the description
    */
    public ModuleDescription (String name, Manifest man) throws IllegalModuleException {
        createDescription (name, man);
        if (VERBOSE) {
            System.err.println ("Making module description for " + this);
            Enumeration en = dependencies ();
            while (en.hasMoreElements ())
                System.err.println ("\t=>" + (Dependency) en.nextElement ());
        }
    }

    /** Create new description from a fixed JAR file on disk (for testing).
    * Note that getting the actual install or check classes will not generally work
    * unless the JAR is already in the class path.
    * This constructor may be conveniently used to test parsing of a module JAR,
    * as well as testing cross-dependencies and so on.
    * @param jar the JAR file
    * @deprecated Only for testing.
    * @throws IllegalModuleException if there is an error reading the description
    * @throws IOException if the JAR file could not be opened or read
    */
    public ModuleDescription (File jar) throws IllegalModuleException, IOException {
        this (jar.getPath (), new Manifest (new FileInputStream (jar)));
    }

    /** Create new description from a string (for testing).
    * This constructor may be conveniently used to test parsing of a module JAR,
    * as well as testing cross-dependencies and so on.
    * @param text the full text of the manifest file
    * @deprecated Only for testing.
    * @throws IllegalModuleException if there is an error reading the description
    * @throws IOException should not be thrown
    */
    public ModuleDescription (String text) throws IllegalModuleException, IOException {
        this ("/no/path/to/testManifest.mf", new Manifest (new ByteArrayInputStream (text.getBytes ()))); // NOI18N
    }

    // -----------------------------------------------------------------------------
    // Public interface

    /** Get display name of the module.
    * @return the name
    * @see #TAG_NAME
    */
    public String getName () {
        return moduleName;
    }

    /** Get code name of the module.
    * @return the code name (should not normally change between releases except to indicate incompatible changes)
    * @see #TAG_MAGIC
    */
    public String getCodeName () {
        return codeName;
    }

    /** Get code name base of the module.
    * E.g. for the code name <code>foo/3</code>, this would give <code>foo</code>.
    * @return the code name base (should not change between releases)
    * @see #getCodeName
    */
    public String getCodeNameBase () {
        int slash = codeName.indexOf ("/"); // NOI18N
        if (slash == -1)
            return codeName;
        else
            return codeName.substring (0, slash);
    }

    /** Get the major release number of the module code name.
    * E.g. for the code name <code>foo/3</code>, this would give <code>3</code>.
    * @return the release number (should change between releases to indicate incompatible changes), or <code>-1</code> if unspecified
    * @see #getCodeName
    */
    public int getCodeNameRelease () {
        int slash = codeName.indexOf ("/"); // NOI18N
        if (slash == -1)
            return -1;
        else
            return Integer.parseInt (codeName.substring (slash + 1));
    }

    /** Get the main hook object of the module (to run hooks from).
    * If the module did not specify a main class, a dummy will be returned instead.
    *
    * @return main object of the module
    * @see #TAG_MAIN
    */
    public synchronized ModuleInstall getModule () {
        try {
            if (mainClass != null) {
                // [PENDING] systemClassLoader() maybe?  --jglick
                // Pls. don't it would make impossible to load modules from
                // repository in the test mode. - Petr Hrebejk
                main = (ModuleInstall)Beans.instantiate (TopManager.getDefault ().currentClassLoader (), mainClass);
            }
            // do not try anymore
            mainClass = null;
        } catch (Exception ex) {
            TopManager.getDefault ().notifyException (ex);
        }
        if (main == null) {
            main = MODULE_NONE;
        }
        return main;
    }

    /** Get a URL to a page describing the module.
    * @return the URL of a JavaHelp HelpSet file, or
    *    <code>null</code> if the module did not specify a description
    * @see #TAG_DESCRIPTION
    * @exception IllegalStateException if the tag is specified but the HelpSet file was not found
    */
    public URL getDescription () {
        // Please do not cache result--need to retry for ModuleItem to look elsewhere for help
        // if not found at first. (Will change currentClassLoader.)
        if (description == null) return null;
        // tries to find localized version of HelpSet (hs) file
        // again, do not change to systemClassLoader lest test-mode break
        try {
            return NbBundle.getLocalizedFile (description, "hs", Locale.getDefault (), TopManager.getDefault ().currentClassLoader ()); // NOI18N
        } catch (MissingResourceException mre) {
            throw new IllegalStateException (getStringFormatted ("EXC_NoHelpSetFile", description)); // NOI18N
        }
    }

    /** Iterates over all entries found in the module. It sends all the entries
    * to the provided iterator's callback methods.
    *
    * @param it iterator over all sections
    */
    public synchronized void forEachSection (ManifestSection.Iterator it) {
        int s = sections.length;
        List al = new LinkedList ();
        for (int i = 0; i < s; i++) {
            try {
                sections[i].invokeIterator (it);
                al.add (sections[i]);
            } catch (Exception ex) {
                TopManager.getDefault().notifyException(ex); // extract the section
            }
        }
        if (sections.length > al.size ())
            sections = (ManifestSection[]) al.toArray (new ManifestSection[al.size ()]);
    }

    /** Get all dependencies.
    * @return an enumeration of {@link ModuleDescription.Dependency}s
    * @see #getDependencies
    */
    public Enumeration dependencies () {
        return Collections.enumeration (dependenciesSet);
    }

    /** Get a list of all dependencies.
    * @return the dependencies
    * @see #dependencies
    */
    public Dependency[] getDependencies () {
        return (Dependency[]) dependenciesSet.toArray (new Dependency[0]);
    }

    /** Get the specification version of this module.
    * @return the spec version, or <code>null</code>
    * @see #TAG_SPEC_VERSION
    */
    public String getSpecVersion () {
        return specVersion;
    }

    /** Get the implementation version of this module.
    * @return the impl version, or <code>null</code>
    * @see #TAG_IMPL_VERSION
    */
    public String getImplVersion () {
        return implVersion;
    }

    /** Check whether this description satisfies all of its dependencies, and if not say why.
    * @param otherModules other modules which this module might require
    * @return <code>null</code> if satisfied, else text explaining why it was not
    * @see ModuleDescription.Dependency#checkForMiss
    */
    public String reasonWhyUnsatisfied (ModuleDescription[] otherModules) throws IllegalModuleException {
        if (VERBOSE)
            System.err.println ("Checking all dependencies for " + this);
        String reason = null;
        Iterator it = dependenciesSet.iterator ();
        while (it.hasNext ()) {
            Dependency dep = (Dependency) it.next ();
            String miss = dep.checkForMiss (otherModules);
            if (miss != null) {
                // String text = getStringFormatted ("MSG_Why_Dep_Failed", getName (), getCodeName (), dep.toString (), miss); // NOI18N
                if (reason == null)
                    reason = getStringFormatted ("MSG_Why_Dep_Failed", getName (), getCodeName () ) + miss; // NOI18N
                //reason = text;
                else
                    reason += "\n" + miss; // NOI18N
            }
        }
        return reason;
    }

    /** Check whether this description depends on another module.
    * This does <em>not</em> check whether the dependency is fully satisfied (i.e. the versions match);
    * it only checks whether there is some sort of dependency or not.
    * Also note that implicit dependencies within sections, e.g. the implicit dependency one module
    * may have on another based on loader pool installation or whatnot, is not considered at all.
    * Modules are considered to depend on themselves.
    * @param other the other module to compare to
    * @return <code>true</code> if this module states that it depends on the other in its dependency list, <code>false</code> if it makes no such statement
    * @see #TAG_MODULE_DEPENDENCIES
    */
    public boolean dependsOnModule (ModuleDescription other) {
        if (this == other || codeName.equals (other.codeName)) return true;
        Iterator it = dependenciesSet.iterator ();
        while (it.hasNext ()) {
            Dependency dep = (Dependency) it.next();
            if (dep.getType () == Dependency.TYPE_MODULE && dep.getName ().equals (other.getCodeName ()))
                return true;
        }
        return false;
    }

    public String toString () {
        return getStringFormatted ("DBG_Module_ToString", getCodeName (), getName ()); // NOI18N
    }

    /** Resolve the proper ordering of a set of modules.
    * Checks the dependencies among the modules and attempts to order them
    * according to a topological sort based on cross-dependencies.
    * Where the ordering is not otherwise specified, orders modules alphabetically based on code name.
    * @param modules a set of <code>ModuleDescription</code>s to be installed
    * @return a list of the same <code>ModuleDescription</code>s in the order in which they should be installed
    * @throws IllegalModuleException if the ordering cannot be resolved (for example, due to a cyclic dependency)
    * @see #dependsOnModule
    */
    public static List resolveOrdering (Set modules) throws IllegalModuleException {
        if (VERBOSE) {
            System.err.println ("Incoming module list: ");
            Iterator it0 = modules.iterator ();
            while (it0.hasNext ())
                System.err.println ("\t" + (ModuleDescription) it0.next ());
        }
        // Not a Knuth-quality topological sort here by any means! Oh well.
        // Predetermine all cross-dependencies (for speed).
        // This is a map from MD to set of MD's it depends on (not incl. itself).
        Map crossdeps = new HashMap (); // Map<ModuleDescription, Set<ModuleDescription>>
        Iterator it1 = modules.iterator ();
        while (it1.hasNext ()) {
            ModuleDescription md = (ModuleDescription) it1.next ();
            Set thisdeps = new HashSet (); // Set<ModuleDescription>
            Iterator it2 = modules.iterator ();
            while (it2.hasNext ()) {
                ModuleDescription other = (ModuleDescription) it2.next ();
                if (md == other) continue;
                if (md.dependsOnModule (other))
                    thisdeps.add (other);
            }
            crossdeps.put (md, thisdeps);
        }
        // Order the modules alphabetically into a new set.
        SortedSet sorted = new TreeSet (codeNameComparator); // SortedSet<ModuleDescription>
        sorted.addAll (modules);
        if (sorted.size () != modules.size ()) throwOverlapException (modules, sorted);
        // The result list.
        List result = new ArrayList (); // List<ModuleDescription>
        // Iteratively look for modules with no remaining dependencies.
        // We will remove items from sorted as we go and add them to result.
pullin_em_out:
        while (sorted.size () > 0) {
            // System.err.println ("Sorted set size at " + sorted.size ());
            Iterator it3 = sorted.iterator ();
            while (it3.hasNext ()) {
                ModuleDescription test = (ModuleDescription) it3.next ();
                Set remainingDeps = (Set) crossdeps.get (test);
                if (remainingDeps.size () == 0) {
                    // System.err.println ("Removing " + test.getName () + " (leaf count at " + leafCount + ")");
                    result.add (test);
                    it3.remove ();
                    // Kill deps on this from other modules.
                    Iterator it4 = sorted.iterator ();
                    while (it4.hasNext ()) {
                        ModuleDescription othermod = (ModuleDescription) it4.next ();
                        if (othermod == test) throw new IllegalModuleException ("Should not happen."); // NOI18N
                        Set otherdeps = (Set) crossdeps.get (othermod);
                        otherdeps.remove (test);
                    }
                    continue pullin_em_out;
                }
            }
            // OK, there were none pulled out on this round => cyclic dependency.
            // Display a list of all modules that had cyclic dependencies, sep'd by commas.
            Iterator it5 = sorted.iterator ();
            boolean first = true;
            StringBuffer buf = new StringBuffer ();
            while (it5.hasNext ()) {
                if (first)
                    first = false;
                else
                    buf.append ("; "); // NOI18N
                ModuleDescription bad = (ModuleDescription) it5.next ();
                buf.append (bad.getName ());
                buf.append (" (=> "); // NOI18N
                boolean firstagain = true;
                Iterator it6 = ((Set) crossdeps.get (bad)).iterator ();
                while (it6.hasNext ()) {
                    if (firstagain)
                        firstagain = false;
                    else
                        buf.append (", "); // NOI18N
                    buf.append (((ModuleDescription) it6.next ()).getName ());
                }
                buf.append (")"); // NOI18N
            }
            throw new IllegalModuleException (getStringFormatted ("EXC_Cyclic", buf.toString ())); // NOI18N
        }
        if (VERBOSE) {
            System.err.println ("Outgoing module list: ");
            Iterator hookah2 = result.iterator ();
            while (hookah2.hasNext ())
                System.err.println ("\t" + (ModuleDescription) hookah2.next ());
        }
        return result;
    }

    /** Throw IllegalModuleException indicating a module code name overlap. For convenience.
    * @param incoming raw set of modules with duplicates
    * @param checked sorted set of modules with duplicates removed
    * @throws IllegalModuleException always
    */
    private static void throwOverlapException (Set incoming, SortedSet checked) throws IllegalModuleException {
        // By popular request, this message should be more helpful now:
        StringBuffer overlaps = new StringBuffer ();
        Map occurrences = new HashMap (); // Map<String,Integer>
        Iterator it = incoming.iterator ();
        while (it.hasNext ()) {
            ModuleDescription md = (ModuleDescription) it.next ();
            String cn = md.getCodeName ();
            Integer count = (Integer) occurrences.get (cn);
            if (count == null)
                occurrences.put (cn, new Integer (1));
            else
                occurrences.put (cn, new Integer (count.intValue () + 1));
        }
        boolean firstOverlap = true;
        Iterator it2 = occurrences.keySet ().iterator ();
        while (it2.hasNext ()) {
            String codeName = (String) it2.next ();
            Integer count2 = (Integer) occurrences.get (codeName);
            if (count2.intValue () > 1) {
                if (firstOverlap)
                    firstOverlap = false;
                else
                    overlaps.append (' ');
                overlaps.append (codeName);
                overlaps.append ('(');
                overlaps.append (count2.toString ());
                overlaps.append (')');
            }
        }
        throw new IllegalModuleException (getStringFormatted ("EXC_Overlapping_Code_Names", // NOI18N
                                          String.valueOf (checked.size ()),
                                          String.valueOf (incoming.size () - checked.size ()),
                                          ModuleDescription.TAG_MAGIC.toString (),
                                          overlaps.toString ()));
    }

    /** Actually generate a list of newly-installable modules in this IDE (but do not install them).
    * Checks all of their dependencies, and orders them properly.
    * If any are missing dependencies, they are removed from the set
    * (after notifying the user), and the remainder are reexamined in case
    * other modules are now missing a dependency. They are ordered in the normal fashion.
    * If there are exceptions in any calculations (<em>not</em> just missed dependencies),
    * these are propagated without any attempt at further error recovery.
    * @param restored a set of <code>ModuleDescription</code>s for already-installed modules which should have already been restored
    * @param installed a set of <code>ModuleDescription</code>s for modules which are intended for installation and may be returned
    * @return a (possibly empty) list of <code>ModuleDescription</code>s for modules which may be installed, in the order in which they should be installed; will be a subset of <code>installed</code>
    * @throws IllegalModuleException if any problem is encountered other than missed dependencies
    */
    public static List resolveOrderingForRealInstall (Set restored, Set installed)
    throws IllegalModuleException {
        if (VERBOSE)
            System.err.println ("rOFRI called.");
        {
            // Check that there are not overlaps anywhere.
            SortedSet doubleCheck = new TreeSet (codeNameComparator);
            doubleCheck.addAll (restored);
            doubleCheck.addAll (installed);
            int actual = doubleCheck.size ();
            int presumed = restored.size () + installed.size ();
            if (actual != presumed) {
                Set combined = new HashSet ();
                combined.addAll (restored);
                combined.addAll (installed);
                throwOverlapException (combined, doubleCheck);
            }
        }
        // List of messages for modules which are *not* to be installed.
        List missed = new ArrayList (); // List<String>
        // Set of modules which will actually be installed.
        Set actual = new HashSet (); // Set<ModuleDescription>
        actual.addAll (installed);
        int misscount;              // this time around
        do {
            misscount = 0;
            Iterator it = actual.iterator ();
            while (it.hasNext ()) {
                ModuleDescription test = (ModuleDescription) it.next ();
                Set whatCanIStillUse = new HashSet (); // Set<ModuleDescription>
                whatCanIStillUse.addAll (restored);
                whatCanIStillUse.addAll (actual);
                String miss = test.reasonWhyUnsatisfied ((ModuleDescription[]) whatCanIStillUse.toArray (new ModuleDescription[0]));
                if (miss != null) {
                    misscount++;
                    it.remove ();
                    missed.add (miss);
                }
            }
        } while (misscount > 0);
        /*
        if (missed.size () > 0) {
          TopManager.getDefault ().notify (new NotifyDescriptor.Message (new Object[] {
            getStringFormatted ("MSG_Some_Missed", "" + missed.size (), "" + actual.size (), "" + (actual.size () + missed.size ())),
              (String[]) missed.toArray (new String[missed.size ()])
              }));
    }
        */
        return resolveOrdering (actual);
    }

    /** Check whether specification versions are compatible.
     * True if older version is less-than-or-equal-to newer version acc. to a Dewey-decimal lexicographic compare.
     * <p>This algorithm should hopefully match that used by the Java Versioning specification
     * (which unfortunately does not make its algorithm public).
     * @param older the presumed older version
     * @param newer the presumed newer version
     * @throws IllegalModuleException in case of a number format error
     * @see Package
     */
    public static boolean compatibleWith (String older, String newer) throws IllegalModuleException {
        if (older == null || newer == null) return false;
        StringTokenizer oldTok = new StringTokenizer (older, "."); // NOI18N
        StringTokenizer newTok = new StringTokenizer (newer, "."); // NOI18N
        while (oldTok.hasMoreTokens () || newTok.hasMoreTokens ()) {
            if (! newTok.hasMoreTokens ())
                // E.g. 1.2.3 vs. 1.2:
                return false;
            if (! oldTok.hasMoreTokens ())
                // E.g. 1.2 vs. 1.2.3:
                return true;
            String oldElt=oldTok.nextToken ();
            String newElt=newTok.nextToken ();
            int oldNum = 0, newNum = 0;
            try {
                oldNum = Integer.parseInt (oldElt);
                newNum = Integer.parseInt (newElt);
            } catch (NumberFormatException e) {
                throw new IllegalModuleException (e);
            }
            if (oldNum < 0 || newNum < 0) return false;
            if (oldNum < newNum)
                // E.g. 1.2 vs. 1.3:
                return true;
            if (oldNum > newNum)
                // E.g. 1.3 vs. 1.2:
                return false;
            // Continue...
        }
        // Equal:
        return true;
    }

    // -----------------------------------------------------------------------------
    // Private methods

    /** Check whether a possible code name is valid. */
    static void checkCodeName (String codeName) throws IllegalModuleException {
        checkCodeName ("", codeName); // NOI18N
    }
    /** Check whether a possible code name is valid.
    * @param moduleName name of module that contains this code name or "" if not  known
    * @param codeName code name of the module
    */
    static void checkCodeName (String moduleName, String codeName) throws IllegalModuleException {
        String base;
        int slash = codeName.indexOf ("/"); // NOI18N
        int release;
        if (slash == -1) {
            base = codeName;
            release = -1;
        } else {
            base = codeName.substring (0, slash);
            try {
                release = Integer.parseInt (codeName.substring (slash + 1));
            } catch (NumberFormatException e) {
                throw new IllegalModuleException (getStringFormatted ("EXC_Non_Numeric_Release", codeName, moduleName)); // NOI18N
            }
        }
        for (int ch = 0; ch < base.length (); ch++) {
            char c = base.charAt (ch);
            if (! (Character.isJavaIdentifierPart (c) || c == '.'))
                throw new IllegalModuleException (getStringFormatted ("EXC_Bad_Char_In_Code_Name", codeName, moduleName)); // NOI18N
        }
    }

    /** Check whether a possible specification version is valid. */
    static void checkSpec (String spec) throws IllegalModuleException {
        try {
            if (! compatibleWith ("0", spec)) // NOI18N
                throw new IllegalModuleException (getStringFormatted ("EXC_Bad_Spec", spec)); // NOI18N
        } catch (IllegalModuleException e) {
            throw new IllegalModuleException (getStringFormatted ("EXC_Bad_Spec_Why", spec, e.getMessage ())); // NOI18N
        }
    }

    /** Creates description of the module grabbed from the jar archive manifest.
    * @return module description
    * @exception IllegalModuleException if there is error reading the description
    */
    private void createDescription (String name, Manifest man)
    throws IllegalModuleException {
        Attributes attr = man.getMainAttributes ();

        // -----------------------------------------------------------------------------
        // Global tags : required: TAG_MAGIC

        codeName = attr.getValue (TAG_MAGIC);
        if (codeName == null) {
            // not module
            throw new IllegalModuleException (getStringFormatted ("EXC_Not_A_Module", TAG_MAGIC.toString (), name)); // NOI18N
        }
        checkCodeName (name, codeName);

        moduleName = (String) NbBundle.getLocalizedValue (attr, TAG_NAME);

        if (moduleName == null) {
            String str = name;
            int from = str.lastIndexOf('/');
            int till = str.lastIndexOf('.');
            moduleName = str.substring((from == -1) ? 0 : from + 1, (till == -1) ? str.length() : till);
        }

        // -----------------------------------------------------------------------------
        // Versioning tags

        specVersion = attr.getValue (TAG_SPEC_VERSION);
        if (specVersion != null) checkSpec (specVersion);
        implVersion = attr.getValue (TAG_IMPL_VERSION);

        // -----------------------------------------------------------------------------
        // Dependency tags

        dependenciesSet = new HashSet ();
        parseDependencies (Dependency.TYPE_MODULE, attr.getValue (TAG_MODULE_DEPENDENCIES), dependenciesSet);
        parseDependencies (Dependency.TYPE_PACKAGE, attr.getValue (TAG_PACKAGE_DEPENDENCIES), dependenciesSet);
        parseDependencies (Dependency.TYPE_JAVA, attr.getValue (TAG_JAVA_DEPENDENCIES), dependenciesSet);
        parseDependencies (Dependency.TYPE_IDE, attr.getValue (TAG_IDE_DEPENDENCIES), dependenciesSet);

        // -----------------------------------------------------------------------------
        // ModuleInstall tags

        mainClass = attr.getValue (TAG_MAIN);
        if (mainClass != null) {
            mainClass = createPackageName(mainClass);
        }

        // -----------------------------------------------------------------------------
        // other tags

        description = attr.getValue (TAG_DESCRIPTION);

        // iterator of Attributes
        Iterator en = man.getEntries ().entrySet ().iterator ();

        ArrayList v = new ArrayList ();
        while (en.hasNext ()) {
            Map.Entry entry = (Map.Entry)en.next ();
            Attributes a = (Attributes) entry.getValue ();
            ManifestSection s = ManifestSection.createSection ((String)entry.getKey (), a);
            if (s != null) {
                // if the entry describes section class
                v.add (s);
            }
        }
        sections = new ManifestSection[v.size ()];
        v.toArray (sections);
    }

    /** Parse dependencies from tags.
    * @param type like Dependency.type
    * @param body actual text of tag body; if <code>null</code>, does nothing
    * @param deps set of dependencies to add to
    * @throws IllegalModuleException if in bad format
    */
    private static void parseDependencies (int type, String body, Set deps) throws IllegalModuleException {
        if (body == null) return;
        // First split on commas.
        StringTokenizer tok = new StringTokenizer (body, ","); // NOI18N
        if (! tok.hasMoreTokens ())
            throw new IllegalModuleException (getStringFormatted ("EXC_No_Deps_Given", body)); // NOI18N
        while (tok.hasMoreTokens ()) {
            String onedep = tok.nextToken ();
            StringTokenizer tok2 = new StringTokenizer (onedep, " \t\n\r"); // NOI18N
            if (! tok2.hasMoreTokens ())
                throw new IllegalModuleException (getStringFormatted ("EXC_No_Name_In_Dep", onedep)); // NOI18N
            String name = tok2.nextToken ();
            int comparison;
            String version;
            if (tok2.hasMoreTokens ()) {
                String compthing = tok2.nextToken ();
                if (compthing.equals (">")) // NOI18N
                    comparison = Dependency.COMPARE_SPEC;
                else if (compthing.equals ("=")) // NOI18N
                    comparison = Dependency.COMPARE_IMPL;
                else
                    throw new IllegalModuleException (getStringFormatted ("EXC_Unrec_Comp_Str", compthing)); // NOI18N
                if (! tok2.hasMoreTokens ())
                    throw new IllegalModuleException (getStringFormatted ("EXC_Comp_Str_Without_Vers", onedep)); // NOI18N
                version = tok2.nextToken ();
                if (tok2.hasMoreTokens ())
                    throw new IllegalModuleException (getStringFormatted ("EXC_Garbage", onedep)); // NOI18N
            } else {
                comparison = Dependency.COMPARE_ANY;
                version = null;
            }
            deps.add (new Dependency (type, name, comparison, version));
        }
    }

    /** Convert a class file name to a resource name suitable for Beans.instantiate.
    * @param name resource name of class file
    * @return class name without the <code>.class</code>/<code>.ser</code> extension, and using dots as package separator
    * @throws IllegalModuleException if the name did not have a valid extension, or originally contained dots outside the extension, etc.
    */
    static String createPackageName(String name) throws IllegalModuleException {
        String clExt = ".class"; // NOI18N
        if (!name.endsWith(clExt)) {
            // try different extension
            clExt = ".ser"; // NOI18N
        }
        if (name.endsWith(clExt)) {
            String bareName = name.substring(0, name.length() - clExt.length());
            if (bareName.length () == 0) // ".class" // NOI18N
                throw new IllegalModuleException (getStringFormatted ("EXC_Bad_Class_File_Name", name)); // NOI18N
            if (bareName.charAt (0) == '/') // "/foo/bar.class" // NOI18N
                throw new IllegalModuleException (getStringFormatted ("EXC_Bad_Class_File_Name", name)); // NOI18N
            if (bareName.charAt (bareName.length () - 1) == '/') // "foo/bar/.class" // NOI18N
                throw new IllegalModuleException (getStringFormatted ("EXC_Bad_Class_File_Name", name)); // NOI18N
            if (bareName.indexOf ('.') != -1) // "foo.bar.class" // NOI18N
                throw new IllegalModuleException (getStringFormatted ("EXC_Bad_Class_File_Name", name)); // NOI18N
            return bareName.replace('/', '.');
        } else { // "foo/bar" or "foo.bar" // NOI18N
            throw new IllegalModuleException (getStringFormatted ("EXC_Bad_Class_File_Name", name)); // NOI18N
        }
    }

    static ResourceBundle getBundle () {
        if (bundle == null)
            bundle = NbBundle.getBundle (ModuleDescription.class);
        return bundle;
    }

    static String getString (String key) {
        return getBundle ().getString (key);
    }

    static String getStringFormatted (String key, Object[] args) {
        return MessageFormat.format (getString (key), args);
    }
    static String getStringFormatted (String key, String arg1) {
        return getStringFormatted (key, new Object[] { arg1 });
    }
    static String getStringFormatted (String key, String arg1, String arg2) {
        return getStringFormatted (key, new Object[] { arg1, arg2 });
    }
    static String getStringFormatted (String key, String arg1, String arg2, String arg3) {
        return getStringFormatted (key, new Object[] { arg1, arg2, arg3 });
    }
    static String getStringFormatted (String key, String arg1, String arg2, String arg3, String arg4) {
        return getStringFormatted (key, new Object[] { arg1, arg2, arg3, arg4 });
    }

    /** A type of dependency that the module can have on its environment.
    * @see ModuleDescription
    */
    public static final class Dependency extends Object {

        /** Dependency on another module. */
        public final static int TYPE_MODULE = 1;
        /** Dependency on a package. */
        public final static int TYPE_PACKAGE = 2;
        /** Dependency on Java. */
        public final static int TYPE_JAVA = 3;
        /** Dependency on the IDE. */
        public final static int TYPE_IDE = 4;
        // Type of dependency.
        private int type;

        // Name of dependency.
        private String name;

        /** Comparison by specification version.
        * The actual version must equal or exceed the requested
        * version according to Dewey decimal numbering.
        * @see ModuleDescription#compatibleWith
        */
        public final static int COMPARE_SPEC = 1;
        /** Comparison by implementation version.
        * The actual and requested versions must match exactly as strings.
        */
        public final static int COMPARE_IMPL = 2;
        /** No comparison, just require the dependency to be present. */
        public final static int COMPARE_ANY = 3;
        // Type of comparison.
        private int comparison;

        // Requested version of dependency.
        private String version;

        /** Create a new dependency object.
        * @param type the type of dependency (one of {@link #TYPE_MODULE}, {@link #TYPE_PACKAGE}, {@link #TYPE_JAVA}, or {@link #TYPE_IDE})
        * @param name the name of the dependency, e.g. <code>com.mycom.myothermodule/2</code> or <code>Java</code>
        * @param comparison the type of comparison (one of {@link #COMPARE_SPEC}, {@link #COMPARE_IMPL}, or {@link #COMPARE_ANY})
        * @param version the string version requested; may be <code>null</code>
        * @throws IllegalModuleException if there was something invalid about the parameters
        */
        Dependency (int type, String name, int comparison, String version) throws IllegalModuleException {
            this.type = type;
            this.name = name;
            this.comparison = comparison;
            this.version = version;
            // Sanity.
            if (comparison == COMPARE_SPEC)
                ModuleDescription.checkSpec (version);
            switch (type) {
            case TYPE_MODULE:
                ModuleDescription.checkCodeName (name);
                break;
            case TYPE_PACKAGE:
                // Close enough: would permit a slash, but that's not a big deal really.
                ModuleDescription.checkCodeName (name);
                break;
            case TYPE_JAVA:
                if (! (name.equals ("Java") || name.equals ("VM"))) // NOI18N
                    throw new IllegalModuleException (ModuleDescription.getStringFormatted ("EXC_Bad_Java_Dep", toString ())); // NOI18N
                break;
            case TYPE_IDE:
                if (! (name.equals ("IDE"))) { // NOI18N
                    int slash = name.indexOf ("/"); // NOI18N
                    boolean ok;
                    if (slash == -1) {
                        ok = false;
                    } else {
                        try {
                            Integer.parseInt (name.substring (slash + 1));
                            ok = true;
                        } catch (NumberFormatException e) {
                            ok = false;
                        }
                    }
                    if (! ok) throw new IllegalModuleException (ModuleDescription.getStringFormatted ("EXC_Bad_IDE_Dep", toString ())); // NOI18N
                }
                if (comparison == COMPARE_ANY)
                    throw new IllegalModuleException (ModuleDescription.getStringFormatted ("EXC_IDE_Dep_Uncompared", toString ())); // NOI18N
                break;
            default:
                throw new IllegalModuleException ("unknown type"); // NOI18N
            }
        }

        /** Get the type.
        * @return the type
        */
        public int getType () {
            return type;
        }

        /** Get the name.
        * @return the name
        */
        public String getName () {
            return name;
        }

        /** Get the comparison type.
        * @return the comparison type
        */
        public int getComparison () {
            return comparison;
        }

        /** Get the version.
        * @return the version (may be <code>null</code>)
        */
        public String getVersion () {
            return version;
        }

        /** Check whether this dependency is currently satisfied by the supplied parameters.
        * Note that for dependencies of type {@link #TYPE_PACKAGE}, the check is against whether
        * that package is loaded into the classloader used by the invoking class.
        * <p>The following system properties, with sample values, are used to check dependencies
        * of type {@link #TYPE_IDE}:
        * <table border=1><tr><th>Name</th><th>Description</th><th>Sample</th></tr>
        * <tr><td><code>org.openide.specification.version<code></td><td>Specification version</td><td><code>1.0.12</code></td></tr>
        * <tr><td><code>org.openide.version<code></td><td>Implementation version</td><td><code>build #999</code></td></tr>
        * <tr><td><code>org.openide.major.version<code></td><td>"Code name", i.e. IDE incompatible release</td><td><code>IDE/2</code></td></tr>
        * </table>
        * @param otherModules other modules which this dependency might require
        * @return <code>null</code> if satisfied, else a message explaining why it was not satisfied
        */
        public String checkForMiss (ModuleDescription[] otherModules)
        throws IllegalModuleException {
            switch (type) {

            case TYPE_MODULE:
                for (int i = 0; i < otherModules.length; i++) {
                    ModuleDescription other = otherModules[i];
                    if (name.equals (other.getCodeName ())) {
                        if (comparison == COMPARE_ANY) {
                            return null;
                        } else if (comparison == COMPARE_SPEC) {
                            if (other.getSpecVersion () == null)
                                return ModuleDescription.getStringFormatted ("MSG_Module_Spec_None", other.getName ()); // NOI18N
                            else if (! ModuleDescription.compatibleWith (version, other.getSpecVersion ()))
                                return ModuleDescription.getStringFormatted ("MSG_Module_Spec_Bad", other.getName (), other.getSpecVersion (), version); // NOI18N
                            else
                                return null;
                        } else {
                            // COMPARE_IMPL
                            if (other.getImplVersion () == null)
                                return ModuleDescription.getStringFormatted ("MSG_Module_Impl_None", other.getName ()); // NOI18N
                            else if (! other.getImplVersion ().equals (version))
                                return ModuleDescription.getStringFormatted ("MSG_Module_Impl_Bad", other.getName (), other.getImplVersion (), version); // NOI18N
                            else
                                return null;
                        }
                    }
                }
                return ModuleDescription.getStringFormatted ("MSG_Module_None", name); // NOI18N

            case TYPE_PACKAGE:
                Package pkg = Package.getPackage (name);
                if (pkg == null)
                    return ModuleDescription.getStringFormatted ("MSG_Package_None", name); // NOI18N
                if (comparison == COMPARE_ANY) {
                    return null;
                } else if (comparison == COMPARE_SPEC) {
                    if (pkg.getSpecificationVersion () == null)
                        return ModuleDescription.getStringFormatted ("MSG_Package_Spec_None", pkg.getName ()); // NOI18N
                    else if (! ModuleDescription.compatibleWith (version, pkg.getSpecificationVersion ()))
                        return ModuleDescription.getStringFormatted ("MSG_Package_Spec_Bad", pkg.getName (), pkg.getSpecificationVersion (), version); // NOI18N
                    else
                        return null;
                } else {
                    // COMPARE_IMPL
                    if (pkg.getImplementationVersion () == null)
                        return ModuleDescription.getStringFormatted ("MSG_Package_Impl_None", pkg.getName ()); // NOI18N
                    else if (! pkg.getImplementationVersion ().equals (version))
                        return ModuleDescription.getStringFormatted ("MSG_Package_Impl_Bad", pkg.getName (), pkg.getImplementationVersion (), version); // NOI18N
                    else
                        return null;
                }

            case TYPE_JAVA:
                // Assume all versions are set, and that COMPARE_ANY was not used.
                if (name.equals ("Java")) { // NOI18N
                    if (comparison == COMPARE_SPEC) {
                        return ModuleDescription.compatibleWith (version, System.getProperty ("java.specification.version")) ? null :
                               ModuleDescription.getStringFormatted ("MSG_Java_Spec", version); // NOI18N
                    } else {
                        // COMPARE_IMPL
                        return System.getProperty ("java.version").equals (version) ? null :
                               ModuleDescription.getStringFormatted ("MSG_Java_Impl", version); // NOI18N
                    }
                } else {
                    // VM
                    if (comparison == COMPARE_SPEC) {
                        return ModuleDescription.compatibleWith (version, System.getProperty ("java.vm.specification.version")) ? null :
                               ModuleDescription.getStringFormatted ("MSG_VM_Spec", version); // NOI18N
                    } else {
                        // COMPARE_IMPL
                        return System.getProperty ("java.vm.version").equals (version) ? null :
                               ModuleDescription.getStringFormatted ("MSG_VM_Impl", version); // NOI18N
                    }
                }

            case TYPE_IDE:
                String IDEName = System.getProperty ("org.openide.major.version", "IDE");
                String IDESpecVersion = System.getProperty ("org.openide.specification.version", "0.0");
                String IDEImplVersion = System.getProperty ("org.openide.version", "<unknown>");
                if (! IDEName.equals (name))
                    return ModuleDescription.getStringFormatted ("MSG_IDE_Name", name, IDEName); // NOI18N
                if (comparison == COMPARE_SPEC) {
                    return ModuleDescription.compatibleWith (version, IDESpecVersion) ? null :
                           ModuleDescription.getStringFormatted ("MSG_IDE_Spec", version, IDESpecVersion); // NOI18N
                } else if (comparison == COMPARE_IMPL) {
                    return version.equals (IDEImplVersion) ? null :
                           ModuleDescription.getStringFormatted ("MSG_IDE_Impl", version, IDEImplVersion); // NOI18N
                } else {
                    // COMPARE_ANY
                    return null;
                }
            }
            throw new IllegalModuleException ("should never happen"); // NOI18N
        }

        public boolean equals (Object o) {
            if (o == null || ! (o instanceof Dependency))
                return false;
            Dependency oo = (Dependency) o;
            return type == oo.type &&
                   name.equals (oo.name) &&
                   comparison == oo.comparison &&
                   ((version == null && oo.version == null) ||
                    (version != null && oo.version != null && version.equals (oo.version)));
        }

        public int hashCode () {
            // Jesse's super-scientific technique:
            return (type * 57) ^
                   name.hashCode () ^
                   (comparison * 231) ^
                   (version == null ? 111 : version.hashCode ());
        }

        public String toString () {
            // Too late at night to bother with ChoiceFormat.
            return ModuleDescription.getStringFormatted ("DBG_Dependency_ToString", // NOI18N
                    (type == TYPE_MODULE ? "Module" : // NOI18N
                     type == TYPE_PACKAGE ? "Package" : // NOI18N
                     type == TYPE_JAVA ? "Java" : // NOI18N
                     type == TYPE_IDE ? "IDE" : "???"), // NOI18N
                    name,
                    comparison == COMPARE_ANY ? "" : // NOI18N
                    ModuleDescription.getStringFormatted ("DBG_Dependency_Comparison", // NOI18N
                                                          (comparison == COMPARE_SPEC ? ">" : // NOI18N
                                                           comparison == COMPARE_IMPL ? "=" : "???"), // NOI18N
                                                          version));
        }

    }

    /** Empty instance for used if the module is not present in the jar file.
    * Note that default ModuleInstall instance does nothing, so that is fine.
    */
    private final static ModuleInstall MODULE_NONE = new ModuleInstall ();

}

/*
* Log
*  39   Gandalf   1.38        1/13/00  Ian Formanek    NOI18N
*  38   Gandalf   1.37        1/12/00  Ian Formanek    NOI18N
*  37   Gandalf   1.36        1/6/00   Martin Balin    Removed final on some 
*       fields to make build process happy
*  36   Gandalf   1.35        1/6/00   Petr Hrebejk    Messages about 
*       unsatisfied dependencies modified
*  35   Gandalf   1.34        11/6/99  Jesse Glick     Bugfix: getDescription 
*       was passing on MissingResourceException rather than throwing 
*       IllegalStateException.
*  34   Gandalf   1.33        10/27/99 Petr Hrebejk    Testing of modules added
*  33   Gandalf   1.32        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  32   Gandalf   1.31        10/8/99  Jesse Glick     Bugfix: 
*       NullPointerException after one or more sections failed their iterator, 
*       and a second iterator was run.
*  31   Gandalf   1.30        10/4/99  Jesse Glick     Removed unused service 
*       type name.
*  30   Gandalf   1.29        9/30/99  Jesse Glick     #3713 & #1716--module 
*       install messaging cleanup.
*  29   Gandalf   1.28        9/13/99  Jesse Glick     [JavaDoc], and removed 
*       long-obsolete TAG_TOOLBAR/ TAG_MENU/ TAG_KEY from the actions area.
*  28   Gandalf   1.27        9/10/99  Jaroslav Tulach Service section.
*  27   Gandalf   1.26        8/27/99  Jesse Glick     Fixed #3590.  Cleaned up 
*       random stuff in ManifestSection.  Removed deprecated call from 
*       ModuleDescription test constructor.
*  26   Gandalf   1.25        8/9/99   Petr Hrebejk    Update-Loacation tag end 
*       method removed from Open API
*  25   Gandalf   1.24        6/29/99  Jesse Glick     [JavaDoc]
*  24   Gandalf   1.23        6/9/99   Ian Formanek    manifest tags changed to 
*       NetBeans-
*  23   Gandalf   1.22        6/8/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  22   Gandalf   1.21        6/2/99   Jesse Glick     Modules may specify an 
*       update location.
*  21   Gandalf   1.20        5/27/99  Jesse Glick     Bye-bye InstallCheck!
*  20   Gandalf   1.19        5/27/99  Jesse Glick     [JavaDoc]
*  19   Gandalf   1.18        5/27/99  Jaroslav Tulach Executors rearanged.
*  18   Gandalf   1.17        5/10/99  Jesse Glick     Module versioning--IDE 
*       version numbers refined, made into system properties.
*  17   Gandalf   1.16        5/10/99  Jesse Glick     [JavaDoc]
*  16   Gandalf   1.15        5/7/99   Jaroslav Tulach Help.
*  15   Gandalf   1.14        5/7/99   Jesse Glick     Module localization.
*  14   Gandalf   1.13        5/5/99   Jaroslav Tulach Gives name of module with
*       wrong version on startup.
*  13   Gandalf   1.12        5/4/99   Jesse Glick     More useful messages 
*       after failed dep check.
*  12   Gandalf   1.11        5/4/99   Jesse Glick     Debug verbosity, OFF BY 
*       DEFAULT. Use -Dcom.netbeans.ide.modules.ModuleDescription.VERBOSE=true 
*       to enable.
*  11   Gandalf   1.10        4/28/99  Jesse Glick     More thorough error 
*       checking. Also, resolveOrdering generates an order which is as close as 
*       possible to the alphabetical order, given the dependencies--which is 
*       what it should do, I think, according to the documentation I gave it.
*  10   Gandalf   1.9         4/28/99  Jesse Glick     Added 
*       resolveOrderingForRealInstall.  getModule & getCheck notify exceptions.
*  9    Gandalf   1.8         4/28/99  Jesse Glick     resolveOrdering more 
*       informative about errors.  Dependency onstructor package-private.
*  8    Gandalf   1.7         4/28/99  Jaroslav Tulach Version back. Sorry.
*  7    Gandalf   1.6         4/28/99  Jaroslav Tulach resolveOrdring takes 
*       Collection, Set is not necessary
*  6    Gandalf   1.5         4/28/99  Jesse Glick     [JavaDoc]
*  5    Gandalf   1.4         4/28/99  Jesse Glick     Lots of localizations, 
*       and a bit more JavaDoc.
*  4    Gandalf   1.3         4/28/99  Jesse Glick     Implementation of real 
*       module versioning.
*  3    Gandalf   1.2         4/7/99   Ian Formanek    Rename 
*       Section->ManifestSection
*  2    Gandalf   1.1         4/7/99   Ian Formanek    Added 
*       versioning/dependency stuff
*  1    Gandalf   1.0         4/7/99   Ian Formanek    
* $
*/
