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

package org.netbeans.modules.group;

import java.beans.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.io.*;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.HashSet;
import java.util.Collections;
import java.text.MessageFormat;

import org.openide.TopManager;
import org.openide.loaders.*;
import org.openide.filesystems.*;
import org.openide.nodes.*;
import org.openide.text.EditorSupport;
import org.openide.util.datatransfer.PasteType;
import org.openide.util.datatransfer.ExTransferable;
import org.openide.util.NbBundle;
import org.openide.util.HelpCtx;
import org.openide.util.actions.SystemAction;
import org.openide.util.RequestProcessor;
import org.openide.cookies.CompilerCookie;

/** Group shadow.
 *  DataObject representing group of dataobjects on filesystem
 *  It also defines rules for templating of group members
 *    property templateALL
 *    property templatePattern defines message format where
 *     {0} file name
 *     {1} name entered by user
 *     {2} posfix got from {0} by using part following last "__"
 *         e.g. for "hello__World" is postfix "World"
 *              for "helloWorld" is postfix ""
 *     {3} backward substitution result
 *
 * @author Martin Ryzl. Pk
 */
public class GroupShadow extends DataObject {

    /** Constants. */
    public static String GS_EXTENSION = "group"; // NOI18N

    /** Children for Group Shadow. */
    private GroupChildren children;

    /** If true, GroupShadow will show targets for all links. */
    private static boolean showLinks = true;

    /** Name of the Show Links Property. */
    public static final String PROP_SHOW_LINKS = "showlinks"; // NOI18N

    /** Name of the Template All Property. */
    public static final String PROP_TEMPLATE_ALL = "templateall"; // NOI18N

    /** Name of the Template Pattern Property */
    public static final String PROP_TEMPLATE_PATTERN = "templatepattern"; // NOI18N

    /** Message format for template pattern */
    public String templatePattern = null;

    /** If true create also group shadow when templating */
    private boolean templateAll = false;

    /** Icon resource string for GroupShadow node */
    static final String GS_ICON_BASE =
        "/org/netbeans/modules/group/resources/groupShadow"; // NOI18N

    /** Format for display name. */
    private static MessageFormat groupformat;

    /** Formats for target names (valid, invalid, invalid). */
    private static MessageFormat vformat, vformat2, iformat, iformat2;

    /** Anti-loop detection. */
    private GroupShadow gsprocessed = null;

    static final long serialVersionUID =-5086491126656157958L;
    /** Constructor
     *
     */
    public GroupShadow(final FileObject fo, DataLoader dl)
    throws DataObjectExistsException, IllegalArgumentException {
        super(fo, dl);
    }

    /** Creates a node for GroupShadow and registers it for listening
     *@return node */
    protected Node createNodeDelegate() {
        GroupShadowNode node = new GroupShadowNode(this, new GroupChildren());
        addPropertyChangeListener(node);
        return node;
    }

    /* Getter for delete action.
     * @return true if the object can be deleted
     */
    public boolean isDeleteAllowed () {
        return !getPrimaryFile ().isReadOnly ();
    }

    /* Getter for copy action.
     * @return true if the object can be copied
     */
    public boolean isCopyAllowed ()  {
        return true;
    }

    /* Getter for move action.
     * @return true if the object can be moved
     */
    public boolean isMoveAllowed ()  {
        return !getPrimaryFile ().isReadOnly ();
    }

    /* Getter for rename action.
     * @return true if the object can be renamed
     */
    public boolean isRenameAllowed () {
        return !getPrimaryFile ().isReadOnly ();
    }

    /* Handles copy of the data object.
     * @param f target folder
     * @return the new data object
     * @exception IOException if an error occures
     */
    protected DataObject handleCopy (DataFolder f) throws IOException {
        return handleCopy(f, getName());
    }

    protected DataObject handleCopy (DataFolder f, String name) throws IOException {
        String newname = FileUtil.findFreeFileName (f.getPrimaryFile (), name, GS_EXTENSION);
        FileObject fo = FileUtil.copyFile (getPrimaryFile (), f.getPrimaryFile (), newname);
        return new GroupShadow(fo, getLoader());
    }

    /* Deals with deleting of the object. Must be overriden in children.
     * @exception IOException if an error occures
     */
    protected void handleDelete () throws IOException {

        FileLock lock = getPrimaryFile ().lock ();
        try {
            getPrimaryFile ().delete (lock);
        } finally {
            lock.releaseLock ();
        }
    }

    /* Handles renaming of the object.
     * Must be overriden in children.
     *
     * @param name name to rename the object to
     * @return new primary file of the object
     * @exception IOException if an error occures
     */
    protected FileObject handleRename (String name) throws IOException {
        FileLock lock = getPrimaryFile ().lock ();
        try {
            getPrimaryFile ().rename (lock, name, GS_EXTENSION);
        } finally {
            lock.releaseLock ();
        }
        return getPrimaryFile ();
    }

    /* Handles move of the object. Must be overriden in children.
     *
     * @param f target data folder
     * @return new primary file of the object
     * @exception IOException if an error occures
     */
    protected FileObject handleMove (DataFolder f) throws IOException {
        String name = FileUtil.findFreeFileName (f.getPrimaryFile (), getName (), GS_EXTENSION);
        return FileUtil.moveFile (getPrimaryFile (), f.getPrimaryFile (), name);
    }


    /* Help context for this object.
     * @return help context
     */
    public org.openide.util.HelpCtx getHelpCtx () {
        return new HelpCtx (GroupShadow.class);
    }

    /** Adds a {@link CompilerCookie compilation cookie}.
    */
    public Node.Cookie getCookie (Class cookie) {
        if (CompilerCookie.class.isAssignableFrom (cookie)) {
            GroupShadowCompiler c = new GroupShadowCompiler (this, cookie);
            return c;
        }
        return super.getCookie (cookie);
    }

    /** Reads whole file to the List.
     *
     * @param fo file object to be read
     *
     * @return List of java.lang.String
     */
    public static List readLinks(FileObject fo) throws IOException {
        String line;
        List list = new ArrayList();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(fo.getInputStream()));

            while ((line = br.readLine()) != null) {
                list.add(line);
            }
        } catch (IOException ex) {
            throw ex;
        }
        finally {
            if (br != null) br.close();
        }
        return list;
    }

    /** Reads whole file to the List.
     *
     * @return List of java.lang.String
     */
    public List readLinks() throws IOException {
        return readLinks(getPrimaryFile());
    }

    /** Writes List as new content of file.
     *
     * @param list List of java.lang.String
     */
    public static void writeLinks(List list, FileObject fo) throws IOException {
        String line;
        Iterator iterator = list.iterator();
        BufferedWriter bw = null;
        FileLock lock = null;
        try {
            lock = fo.lock();
            bw = new BufferedWriter(new OutputStreamWriter(fo.getOutputStream(lock)));
            while (iterator.hasNext()) {
                line = (String) iterator.next();
                bw.write(line); bw.newLine();
            }
        } catch (IOException ex) {
            throw ex;
        }
        finally {
            if (lock != null) lock.releaseLock();
            if (bw != null) {
                bw.close();
            }
        }
    }

    /** Writes List as new content of file.
     *
     * @param list List of java.lang.String
     */
    protected void writeLinks(List list) throws IOException {
        writeLinks(list, getPrimaryFile());
    }

    /** Get link name.
     *
     * @param fo file object
     * @return file name
     */
    public static String getLinkName(FileObject fo) {
        return fo.getPackageNameExt('/', '.');
    }

    /** Get FileObject for given filename.
     *
     * @param filename filename
     * @return FileObject
     */
    private static FileObject findFileObject(String filename) {

        return TopManager.getDefault().getRepository().findResource(filename);
    }

    /** Get DataObject for given filename.
     * @param filename filename
     * @return DataObject
     */
    private static DataObject getDataObjectByName(String filename) throws DataObjectNotFoundException {

        FileObject tempfo = findFileObject(filename);
        return (tempfo != null) ? DataObject.find(tempfo): null;
    }

    /** Reads filenames from shadow and creates an array of DataObjects for them.
     * 
     * @return array that can contain DataObjects or Strings with names of invalid
     * links
     */
    public Object[] getLinks() {
        FileObject pf = getPrimaryFile(), parent = pf.getParent(), tempfo;
        DataObject obj;
        String line;
        HashSet set = new HashSet();
        List linearray;

        try {
            linearray = readLinks(pf);
            Iterator it = linearray.iterator();
            while (it.hasNext()) {
                line = (String)it.next();
                try {
                    if ((obj = getDataObjectByName(line)) != null) {
                        set.add(obj);
                    }
                    else set.add(new String(line));
                } catch (DataObjectNotFoundException ex) {
                    // can be thrown when the link is not recognized by any data loader
                    // in this case I can't help so ignore it
                }
            }
        } catch (IOException ex) {
            // it can be ignored
        }

        // the array can contain DataObjects and Strings !
        return set.toArray();
    }

    /** Replace the oldprefix by a newprefix in name.
     *
     * @param name name
     * @param oldprefix old prefix
     * @param newprefix new prefix
     * @return new name
     */
    public static String createName(String name, String oldprefix, String newprefix) {
        if (name.startsWith(oldprefix)) {
            return newprefix + name.substring(oldprefix.length());
        }
        return name;
    }

    /** Replaces all occurences of __.*__ by a given text
     *
     * @param name name with __.*__ substrings
     * @param pattern replacement
     * @return a string with __.*__ replaced
     */
    private String replaceName0(String name, String pattern) {
        StringBuffer sb = new StringBuffer(256);
        int i = 0, j, k;

        while ((j = name.indexOf("__", i)) != -1) { // NOI18N

            // first occurence found
            k = name.indexOf("__", j + 2); // NOI18N
            if (k != -1) {
                // second occurence found, copy start part and pattern
                sb.append(name.substring(i, j));
                sb.append(pattern);
                i = k + 2;
            } else {
                break;
            }
        }
        // copy the rest
        sb.append(name.substring(i, name.length ()));
        return sb.toString();
    }

    /**Replaces name according to namming pattern defined by templatePattern
    * property or fails to replaceName0() if the property is null
    */
    private String replaceName(String name, String pattern) {
        String fmt = getTemplatePattern();
        if(fmt==null){
            return replaceName0(name,pattern);
        }

        // filter out all characters before "__" // NOI18N
        String postfix = ""; // NOI18N
        try{
            int i = name.lastIndexOf("__"); // NOI18N
            if(i>0){
                postfix = name.substring(i+2);
            }
        }catch(IndexOutOfBoundsException ex){
            //use default value
        }

        String subst = string3(name,pattern);
        return MessageFormat.format(fmt,new String[]{name,pattern,postfix,subst});
    }

    /**backward substitution in name by x
     *SE: it calls recursively itself until whole substituion done
     *    x must not contain substitution pattern !!!
     */
    private String substitute(String name, String x){
        StringBuffer sb = new StringBuffer(name);
        int j = name.length();
        int i = name.lastIndexOf("__",j); // NOI18N
        j = i-1;
        if(i>=0){
            i = name.lastIndexOf("__",j); // NOI18N
            if(i>=0){
                sb.delete(i,j+3);
                sb.insert(i,x);
                return substitute(sb.toString(),x);
            }
        }
        return name;
    }

    /** Substitution wrapper for special cases
     *@returns String representing new name after substitution
     */
    private String string3(String name, String pattern) {

        String patch;
        if(name.startsWith("__")){ // NOI18N
            patch = name;
        }else{
            patch = "__" + name; // NOI18N
        }

        String s3 = substitute(patch,pattern);
        if (s3.startsWith("__")){ // NOI18N
            s3 = s3.substring(2);
        }
        return s3;
    }

    /** HandleCreateFromTemplate implementation for GroupShadow.
     *
     */
    protected DataObject handleCreateFromTemplate(DataFolder df, String name) throws IOException {
        DataObject original;
        String originalName;

        // anti-loop detection
        if (gsprocessed == null) {
            gsprocessed = this;
        } else {
            return this;
        }

        try {
            Object[] objs = getLinks();
            ArrayList list = new ArrayList(objs.length);
            DataObject first = null;

            for(int i = 0; i < objs.length; i++) {
                if (objs[i] instanceof DataObject) {

                    original = (DataObject)objs[i];
                    originalName = original.getName();
                    DataObject obj = original.createFromTemplate(df, replaceName(originalName, name));

                    // if obj == this, the loop was detected and nothing was created
                    if ((first == null) && (obj != this)) first = obj;
                    list.add(getLinkName(obj.getPrimaryFile()));
                }
            }

            // create GroupShadow
            if (templateAll || (list.size() == 0)) {
                GroupShadow gs = new GroupShadow(df.getPrimaryFile().createData(name, GS_EXTENSION), getLoader());
                gs.writeLinks(list);
                return gs;
            }

            if (first == null) return this;

            return first;
        } catch (IOException th) {
            throw th;
        } catch (Error e) {
            throw e;
        } finally {
            // it must be set to null !
            gsprocessed = null;
        }
    }

    /** Setter for showLinks
     *
     * @param show if true also show real packages and names of targets
     */
    public void setShowLinks(boolean show) {
        showLinks = show;
    }

    /** Setter for showLinks
     *
     */
    public boolean getShowLinks() {
        return showLinks;
    }

    /**XSetter for template pattern
    */
    public void setTemplatePattern(String templatePattern) throws IOException{
        final FileObject fo = getPrimaryFile();
        String old = getTemplatePattern();

        fo.setAttribute(PROP_TEMPLATE_PATTERN, templatePattern);

        if (old != templatePattern) {
            firePropertyChange(PROP_TEMPLATE_PATTERN, old, templatePattern);
        }

    }

    /**XGetter for template pattern
    */
    public String getTemplatePattern(){
        Object o = getPrimaryFile().getAttribute(GroupShadow.PROP_TEMPLATE_PATTERN);
        if (o instanceof String) return (String)o;
        else return null;
    }

    /** Getter for template all
     *
     */
    public boolean getTemplateAll() {
        Object o = getPrimaryFile().getAttribute(GroupShadow.PROP_TEMPLATE_ALL);
        if (o instanceof Boolean) return ((Boolean) o).booleanValue();
        else return false;
    }


    /** Setter for template all
     *
     */
    public void setTemplateAll(boolean templateAll) throws IOException {
        final FileObject fo = getPrimaryFile();
        boolean oldtempl = getTemplateAll();

        fo.setAttribute(PROP_TEMPLATE_ALL, (templateAll ? new Boolean(true) : null));

        if (oldtempl != templateAll) {
            firePropertyChange(PROP_TEMPLATE_ALL, new Boolean(oldtempl), new Boolean(templateAll));
        }
    }

    /** Getter for resources */
    static String getLocalizedString (String s) {
        return NbBundle.getBundle (GroupShadow.class).getString (s);
    }



    /* ======================  inner class(es) ======================== */

    /** Node for group shadow. */
    public class GroupShadowNode extends DataNode implements PropertyChangeListener{

        /** Create a folder node with some children.
         *
         * @param ch children to use for the node
         */
        public GroupShadowNode (final DataObject dob, Children children) {
            super (dob, children);
            setIconBase(GS_ICON_BASE);
        }

        /** Getter for display name .
         *
         * @return display name
         */
        public String getDisplayName() {
            if (groupformat == null) {
                groupformat = new MessageFormat(GroupShadow.getLocalizedString("FMT_groupShadowName")); // NOI18N
            }
            String display = groupformat.format(new Object[] {
                                                    getName(), "", getPrimaryFile().toString(), "" // NOI18N
                                                });
            try {
                display = getDataObject ().getPrimaryFile ().getFileSystem ().getStatus ().
                          annotateName (display, getDataObject ().files ());
            } catch (FileStateInvalidException e) {
                // no fs, do nothing
            }
            return display;
            //      return super.getDisplayName() + GroupShadow.getLocalizedString("PROP_group"); // " (group)"; // NOI18N
        }


        /** Initializes sheet of properties.
         *
         * @return sheet
         */
        protected Sheet createSheet() {
            Sheet s = super.createSheet();
            updateSheet(s);
            return s;
        }

        /**Listens for dataobject templatePattern property change
        *according to it updates Experl list visibility
        */
        public void propertyChange(PropertyChangeEvent evt) {
            if(evt.getPropertyName()==DataObject.PROP_TEMPLATE){
                if(evt.getNewValue().equals(evt.getOldValue())) return;
                updateSheet(getSheet());
            }
        }

        /**Conditionally fills the set
        */
        private void fillExpertSet(Sheet.Set set){
            DataObject obj = getDataObject();
            Node.Property p;

            // put properties to set
            try {
                /*X
                  p = new PropertySupport.Reflection (obj, Boolean.TYPE, "getShowLinks", "setShowLinks");
                  p.setName(GroupShadow.PROP_SHOW_LINKS);
                  p.setDisplayName(GroupShadow.getLocalizedString("PROP_showlinks"));
                  p.setShortDescription(GroupShadow.getLocalizedString("HINT_showlinks"));
                  ss.put(p);
                */
                if(getDataObject().isTemplate()){
                    p = new PropertySupport.Reflection (obj, Boolean.TYPE, "getTemplateAll", "setTemplateAll"); // NOI18N
                    p.setName(GroupShadow.PROP_TEMPLATE_ALL);
                    p.setDisplayName(GroupShadow.getLocalizedString("PROP_templateall")); // NOI18N
                    p.setShortDescription(GroupShadow.getLocalizedString("HINT_templateall")); // NOI18N
                    set.put(p);

                    p = new PropertySupport.Reflection (obj, String.class, "getTemplatePattern", "setTemplatePattern"); // NOI18N
                    p.setName(GroupShadow.PROP_TEMPLATE_PATTERN);
                    p.setDisplayName(GroupShadow.getLocalizedString("PROP_templatePattern")); // NOI18N
                    p.setShortDescription(GroupShadow.getLocalizedString("HINT_templatePattern")); // NOI18N
                    set.put(p);
                }
            } catch (Exception ex) {
                throw new InternalError();
            }
        }

        /**On property isTemplate change
        */
        private void updateSheet(Sheet sheet){
            if(getDataObject().isTemplate()){
                Sheet.Set set = sheet.get(Sheet.EXPERT);
                if (set==null){
                    set = Sheet.createExpertSet();
                    fillExpertSet(set);
                    sheet.put(set);
                }else{
                    fillExpertSet(set);
                }
            }else{
                sheet.remove(Sheet.EXPERT);
            }
        }

        /** Augments the default behaviour to test for {@link NodeTransfer#nodeCutFlavor} and
         * {@link NodeTransfer#nodeCopyFlavor}
         * with the {@link DataObject}. If there is such a flavor then adds
         * the cut and copy flavors. Also, if there is a copy flavor and the
         * data object is a template, adds an instantiate flavor.
         *
         * @param t transferable to use
         * @param s list of {@link PasteType}s
         */
        protected void createPasteTypes (Transferable t, java.util.List s) {
            super.createPasteTypes (t, s);

            DataObject obj = null;

            // try copy flavor
            obj = (DataObject)NodeTransfer.cookie (
                      t, NodeTransfer.CLIPBOARD_COPY | NodeTransfer.CLIPBOARD_CUT, DataObject.class
                  );

            if (obj != null) {
                if (obj.isCopyAllowed ()) {

                    // copy and cut
                    s.add (new Paste ("PT_copy", obj, false)); // NOI18N
                }
            }
        }
    }

    /** Paste types for data objects. */
    private class Paste extends PasteType {
        private String resName;
        private DataObject obj;
        private boolean clearClipboard;

        /**
         * @param resName resource name for the name
         * @param obj object to work with
         * @param clear true if we should clear clipboard
         */
        public Paste (String resName, DataObject obj, boolean clear) {
            this.resName = resName;
            this.obj = obj;
            this.clearClipboard = clear;
        }

        /** The name is obtained from the bundle.
         * @return the name
         */
        public String getName () {
            return getLocalizedString (resName);
        }

        /** Paste.
         */
        public final Transferable paste () throws IOException {
            handle (obj);
            // clear clipboard or preserve content
            return clearClipboard ? ExTransferable.EMPTY : null;
        }

        /** Handles the right action
         * @param obj the data object to operate on
         */
        public void handle (DataObject obj2) throws IOException {
            List list = readLinks();
            String name = getLinkName(obj2.getPrimaryFile());
            if (list.indexOf(name) == -1) list.add(name);
            writeLinks(list);
        }
    }

    /** Children for group shadow.   */
    private class GroupChildren extends Children.Keys {
        public GroupChildren() {
            getPrimaryFile().addFileChangeListener(new FileChangeAdapter() {
                                                       public void fileChanged(FileEvent fe) {
                                                           update();
                                                       }
                                                   });
        }

        protected void addNotify() {
            setKeys(Collections.EMPTY_SET);
            RequestProcessor.postRequest(new Runnable() {
                                             public void run() {
                                                 update();
                                             }
                                         });
        }

        protected void removeNotify() {
            setKeys(Collections.EMPTY_SET);
        }

        void update() {
            setKeys(getLinks());
        }

        protected Node[] createNodes(Object key) {
            Node nodes[] = new Node[1];

            if (key instanceof DataObject) {
                nodes[0] = (Node) new GroupFilterNode(((DataObject)key).getNodeDelegate());
            } else {
                nodes[0] = (Node) new ErrorNode((String)key);
            }
            return nodes;
        }
    }

    /** FilterNode representing one link. */
    private class GroupFilterNode extends FilterNode {

        public GroupFilterNode(Node original) {
            super(original);
        }

        public void destroy() throws IOException {
            DataObject obj = (DataObject)this.getCookie(DataObject.class), tempobj;
            String name;
            boolean modified = false;

            if (obj != null) {
                List list = readLinks();
                Iterator it =  list.iterator();

                while (it.hasNext()) {
                    name = (String)it.next();
                    tempobj = getDataObjectByName(name);
                    if ((tempobj != null) && (tempobj.equals(obj))) {
                        it.remove(); modified = true;
                    }
                }
                if (modified) writeLinks(list);
            }
        }

        public String getDisplayName() {
            DataObject obj = (DataObject) this.getCookie(DataObject.class);
            FileObject primary = obj.getPrimaryFile();
            String name = primary.toString();
            int index = name.lastIndexOf('/');
            if (index > -1) name = name.substring(0, index + 1); else name = ""; // NOI18N
            Object[] objs = new Object[] { obj.getName(), primary.toString(), name };

            if (showLinks) {
                if (vformat == null) {
                    vformat = new MessageFormat(GroupShadow.getLocalizedString("FMT_validTargetName")); // NOI18N
                }

                return vformat.format(objs);
            } else {
                if (vformat2 == null) {
                    vformat2 = new MessageFormat(GroupShadow.getLocalizedString("FMT_validTargetName2")); // NOI18N
                }
                return vformat2.format(objs);
            }
        }
    }

    /** Node representing an invalid link
     *
     */
    private class ErrorNode extends AbstractNode {

        String name;

        public ErrorNode(String name) {
            super(Children.LEAF);

            systemActions = new SystemAction[] {
                                SystemAction.get(org.openide.actions.DeleteAction.class),
                                null,
                                SystemAction.get(org.openide.actions.ToolsAction.class),
                                SystemAction.get(org.openide.actions.PropertiesAction.class)
                            };

            this.name = name;

            if (name != null) {
                if (iformat == null) {
                    iformat = new MessageFormat(GroupShadow.getLocalizedString("FMT_invalidTargetName")); // NOI18N
                }
                DataObject obj = (DataObject) this.getCookie(DataObject.class);
                setDisplayName(iformat.format(new Object[] { "", name })); // NOI18N
            } else {
                if (iformat2 == null) {
                    iformat2 = new MessageFormat(GroupShadow.getLocalizedString("FMT_invalidTargetName2")); // NOI18N
                }
                setDisplayName(iformat2.format(new Object[] { "", name })); // NOI18N
            }
        }

        public ErrorNode() {
            this(null);
        }

        public void destroy() throws IOException {

            String name;
            boolean modified = false;

            List list = readLinks();
            Iterator it =  list.iterator();

            while (it.hasNext()) {
                name = (String)it.next();
                if (name.equals(this.name)) {
                    it.remove(); modified = true;
                }
                if (modified) writeLinks(list);
            }
        }

        public boolean canDestroy() {
            return true;
        }
    }
}

/*
* Log
*  8    Gandalf   1.7         1/18/00  Jesse Glick     Filesystem display name 
*       annotation.
*  7    Gandalf   1.6         1/14/00  Ian Formanek    NOI18N
*  6    Gandalf   1.5         1/11/00  Martin Ryzl     update for jdk1.3 
*       compilation
*  5    Gandalf   1.4         11/27/99 Patrik Knakal   
*  4    Gandalf   1.3         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  3    Gandalf   1.2         9/16/99  Petr Kuzel      Name patterns added
*  2    Gandalf   1.1         8/17/99  Martin Ryzl     LoaderBeanInfo added, 
*       some bug corrected and some bananas around ..
*  1    Gandalf   1.0         7/29/99  Jaroslav Tulach 
* $ 
*/ 
