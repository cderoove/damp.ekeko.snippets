/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.jarpackager.options;

import java.io.*;

import org.openide.options.*;
import org.openide.util.*;

import org.netbeans.modules.jarpackager.util.VersionSerializator;
import org.netbeans.modules.jarpackager.HistoryModel;

/** Options for jar packager. Consists of jar content extensions,
* history depth, and initial values for new archives - compressed
* flag, generate manifest file list flag, add to repository flag.
*
* @author  Dafe Simonek
*/
public class JarPackagerOption extends SystemOption {

    /** serial version UID */
    static final long serialVersionUID = 2216855924823089903L;

    /** Content extension property */
    public static final String PROP_CONTENT_EXT = "contentExt"; // NOI18N
    private static String contentExt = "jarContent"; // NOI18N

    /** History maximum depth */
    public static final String PROP_HISTORY_DEPTH = "historyDepth"; // NOI18N
    private static int historyDepth = 100;

    /** Compressed archive on/off flag */
    public static final String PROP_COMPRESSED = "compressed"; // NOI18N
    private static boolean compressed = true;

    /** Compression level of the archive. */
    public static final String PROP_COMPRESSION_LEVEL = "compressionLevel"; // NOI18N
    private static int compressionLevel = 6;

    /** Generate manifest file list on/off flag */
    public static final String PROP_MANIFEST_FILELIST = "manifestFileList"; // NOI18N
    private static boolean manifestFileList = true;

    /** Automatic add to repository on/off flag */
    public static final String PROP_ADD_TO_REPOSITORY = "addToRepository"; // NOI18N
    private static boolean addToRepository = false;

    /** Main attributes automatic generation on/off flag */
    public static final String PROP_MAIN_ATTRIBUTES = "mainAttributes"; // NOI18N
    private static boolean mainAttributes;

    /** Confirmation of automatic inspecting on/off flag. */
    public static final String PROP_CONFIRM_AUTO_CREATION = "confirmAutoCreation"; // NOI18N
    private static boolean confirmAutoCreation;

    /** History data */
    private static HistoryModel historyData = new HistoryModel();

    /** Manager for versioned serialization */
    private static VersionSerializator serializationManager;

    /** Singleton instance */
    private static JarPackagerOption singletonInstance;

    /** Creates new JarPackagerOption. */
    public JarPackagerOption() {
    }

    /** Returns default instance of jar packager system option */
    public static JarPackagerOption singleton () {
        if (singletonInstance == null) {
            singletonInstance = new JarPackagerOption();
        }
        return singletonInstance;
    }

    /** Get a human presentable name of the action.
    * This may be presented as an item in a menu.
    * @return the name of the option
    */
    public String displayName () {
        return NbBundle.getBundle(JarPackagerOption.class).
               getString("CTL_RootOption");
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx (JarPackagerOption.class);
    }

    /** Getter for property compressed.
     *@return Value of property compressed.
     */
    public boolean isCompressed() {
        return compressed;
    }

    /** Setter for property compressed.
     *@param compressed New value of property compressed.
     */
    public void setCompressed (boolean comp) {
        if (compressed == comp)
            return;
        Boolean old = new Boolean(compressed);
        compressed = comp;
        firePropertyChange(PROP_COMPRESSED, old, new Boolean(compressed));
    }

    /** Getter for property compressionLevel.
    * @return Value of property compressionLevel.
    */
    public int getCompressionLevel () {
        return compressionLevel;
    }

    /** Setter for property compressionLevel.
    * @param compressionLevel New value of property compressionLevel.
    */
    public void setCompressionLevel (int compressionLevel) {
        if (this.compressionLevel == compressionLevel)
            return;
        Integer old = new Integer(this.compressionLevel);
        this.compressionLevel= compressionLevel;
        firePropertyChange(PROP_COMPRESSION_LEVEL, old, new Integer(compressionLevel));
    }

    /** Getter for property addToRepository.
     *@return Value of property addToRepository.
     */
    public boolean isAddToRepository() {
        return addToRepository;
    }

    /** Setter for property addToRepository.
     *@param addToRepository New value of property addToRepository.
     */
    public void setAddToRepository(boolean addToRep) {
        if (addToRepository == addToRep)
            return;
        Boolean old = new Boolean(addToRepository);
        addToRepository = addToRep;
        firePropertyChange(PROP_ADD_TO_REPOSITORY, old,
                           new Boolean(addToRepository));
    }

    /** Getter for property manifestFileList.
     *@return Value of property manifestFileList.
     */
    public boolean isManifestFileList() {
        return manifestFileList;
    }

    /** Setter for property manifestFileList.
     *@param manifestFileList New value of property manifestFileList.
     */
    public void setManifestFileList(boolean manFL) {
        if (manifestFileList == manFL)
            return;
        Boolean old = new Boolean(manifestFileList);
        manifestFileList = manFL;
        firePropertyChange(PROP_MANIFEST_FILELIST, old,
                           new Boolean(manifestFileList));
    }

    /** Getter for property contentExt.
     *@return Value of property contentExt.
     */
    public String getContentExt() {
        return contentExt;
    }

    /** Setter for property contentExt.
     *@param contentExt New value of property contentExt.
     */
    public void setContentExt(String contExt) {
        if (contentExt == contExt)
            return;
        String old = contentExt;
        contentExt = contExt;
        firePropertyChange(PROP_CONTENT_EXT, old, contentExt);
    }

    /** Getter for property historyDepth.
     *@return Value of property historyDepth.
     */
    public int getHistoryDepth () {
        return historyDepth;
    }

    /** Setter for property contentExt.
     *@param contentExt New value of property contentExt.
     */
    public void setHistoryDepth (int histDepth) {
        if (historyDepth == histDepth)
            return;
        Integer old = new Integer(historyDepth);
        historyDepth = histDepth;
        firePropertyChange(PROP_CONTENT_EXT, old, new Integer(historyDepth));
    }

    /** Getter for property mainAttributes.
    *@return Value of property mainAttributes.
    */
    public boolean isMainAttributes () {
        return mainAttributes;
    }

    /** Setter for property mainAttributes.
    * @param mainAttributes New value of property mainAttributes.
    */
    public void setMainAttributes (boolean mainAttr) {
        if (mainAttributes == mainAttr)
            return;
        Boolean old = new Boolean(mainAttributes);
        mainAttributes = mainAttr;
        firePropertyChange(PROP_MAIN_ATTRIBUTES, old,
                           new Boolean(mainAttributes));
    }

    /** Getter for property confirmAutoCreation.
    * @return Value of property confirmAutoCreation.
    */
    public boolean isConfirmAutoCreation () {
        return confirmAutoCreation;
    }

    /** Setter for property confirmAutoCreation.
    * @param confirmAutoCreation New value of property confirmAutoCreation.
    */
    public void setConfirmAutoCreation (boolean confirm) {
        if (confirmAutoCreation == confirm)
            return;
        Boolean old = new Boolean(confirmAutoCreation);
        confirmAutoCreation = confirm;
        firePropertyChange(PROP_CONFIRM_AUTO_CREATION, old,
                           new Boolean(confirmAutoCreation));
    }

    /** Accessor for history data */
    public HistoryModel historyData () {
        return historyData;
    }

    /** @return False to signalize that this option should be a part of
    * project settings */
    private boolean isGlobal () {
        return false;
    }

    /** Serializes values of all properties */
    public void writeExternal (ObjectOutput out)
    throws IOException {
        super.writeExternal(out);
        serializationManager().writeLastVersion(out);
    }

    /** Deserializes values of all properties */
    public void readExternal (ObjectInput in)
    throws IOException, ClassNotFoundException {
        super.readExternal(in);
        serializationManager().readVersion(in);
    }

    private static VersionSerializator serializationManager () {
        if (serializationManager == null) {
            serializationManager = new VersionSerializator();
            serializationManager.putVersion(new Version1());
        }
        return serializationManager;
    }

    /** First version of persistence state for our option */
    private static final class Version1 implements VersionSerializator.Versionable {

        /** Identification of the version */
        public String getName () {
            return "Version_1.0"; // NOI18N
        }

        /** read the data of the version from given input */
        public void readData (ObjectInput in)
        throws IOException, ClassNotFoundException {
            JarPackagerOption.historyData.readData(in);
        }

        /** write the data of the version to given output */
        public void writeData (ObjectOutput out)
        throws IOException {
            JarPackagerOption.historyData.writeData(out);
        }

    }
}

/*
* <<Log>>
*  6    Gandalf   1.5         1/25/00  David Simonek   Various bugfixes and i18n
*  5    Gandalf   1.4         1/16/00  David Simonek   i18n
*  4    Gandalf   1.3         11/5/99  Jesse Glick     Context help jumbo patch.
*  3    Gandalf   1.2         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  2    Gandalf   1.1         10/4/99  David Simonek   
*  1    Gandalf   1.0         9/8/99   David Simonek   
* $ 
*/ 
