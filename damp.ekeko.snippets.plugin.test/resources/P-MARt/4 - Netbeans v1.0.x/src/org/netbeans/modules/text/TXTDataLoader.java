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

package org.netbeans.modules.text;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.ResourceBundle;

import org.openide.loaders.UniFileLoader;
import org.openide.loaders.ExtensionList;
import org.openide.loaders.DataObject;
import org.openide.loaders.MultiDataObject;
import org.openide.filesystems.FileObject;
import org.openide.actions.*;
import org.openide.util.actions.SystemAction;
import org.openide.util.NbBundle;

/** Data loader which recognizes txt files.
* This class is final only for performance reasons,
* can be unfinaled if desired.
*
* @author Petr Hamernik, Jaroslav Tulach, Dafe Simonek
*/
public final class TXTDataLoader extends UniFileLoader {

    /** file attribute which forces a file to be considered a text file */
    static final String ATTR_IS_TEXT_FILE = "org.netbeans.modules.text.IsTextFile"; // NOI18N

    static final long serialVersionUID =-3658061894653334886L;
    /** Creates new TXTDataLoader */
    public TXTDataLoader() {
        super(TXTDataObject.class);
    }

    /** Does initialization. Initializes display name,
    * extension list and the actions. */
    protected void initialize () {
        setDisplayName(NbBundle.getBundle(TXTDataLoader.class).
                       getString("PROP_TXTLoader_Name"));
        ExtensionList ext = new ExtensionList();
        ext.addExtension("txt"); // NOI18N
        ext.addExtension("doc"); // NOI18N
        ext.addExtension("me"); // for read.me files // NOI18N
        ext.addExtension("policy"); // NOI18N
        ext.addExtension("properties"); // provided for case when PropertiesModule is not installed // NOI18N
        ext.addExtension("mf"); // for manifest.mf files // NOI18N
        ext.addExtension("MF"); //  -""- // NOI18N
        ext.addExtension("dtd"); // when xml module isn't installed, you can view dtd as plain text // NOI18N
        setExtensions(ext);
        setActions(new SystemAction[] {
                       SystemAction.get(OpenAction.class),
                       SystemAction.get (FileSystemAction.class),
                       null,
                       SystemAction.get(CutAction.class),
                       SystemAction.get(CopyAction.class),
                       SystemAction.get(PasteAction.class),
                       null,
                       SystemAction.get(DeleteAction.class),
                       SystemAction.get(RenameAction.class),
                       null,
                       SystemAction.get(SaveAsTemplateAction.class),
                       null,
                       SystemAction.get(ToolsAction.class),
                       SystemAction.get(PropertiesAction.class),
                   });
    }

    /** Check whether a file is recognized.
    * It will be if the extension matches, or if it is marked to be a text file.
    */
    protected FileObject findPrimaryFile (FileObject fo) {
        if (Boolean.TRUE.equals (fo.getAttribute (ATTR_IS_TEXT_FILE)))
            return fo;
        return super.findPrimaryFile (fo);
    }

    /** Creates new TXTDataObject for this FileObject.
    * @param fo FileObject
    * @return new TXTDataObject
    */
    protected MultiDataObject createMultiObject(final FileObject fo)
    throws IOException {
        return new TXTDataObject(fo, this);
    }

}

/*
 * Log
 *  19   Gandalf-post-FCS1.17.1.0    3/24/00  Jesse Glick     ConvertToTextAction.
 *  18   Gandalf   1.17        1/13/00  Ian Formanek    NOI18N #2
 *  17   Gandalf   1.16        1/5/00   Ian Formanek    NOI18N
 *  16   Gandalf   1.15        12/2/99  Libor Kramolis  Added dtd to extension 
 *       list
 *  15   Gandalf   1.14        11/27/99 Patrik Knakal   
 *  14   Gandalf   1.13        10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  13   Gandalf   1.12        10/5/99  Petr Kuzel      "MF" extension added
 *  12   Gandalf   1.11        10/1/99  Jaroslav Tulach Loaders extends 
 *       SharedClassObject
 *  11   Gandalf   1.10        8/27/99  Ian Formanek    Now consumes also .doc, 
 *       .mf and .me (see ya, read.me) files 
 *  10   Gandalf   1.9         7/15/99  Ian Formanek    Added .policy to list of
 *       recognized files
 *  9    Gandalf   1.8         7/7/99   Michal Fadljevic FileSystemAction added
 *  8    Gandalf   1.7         7/7/99   Michal Fadljevic FileSystemAction added  
 *       
 *  7    Gandalf   1.6         6/27/99  Ian Formanek    Now consumes also 
 *       .properties files for the case when PropertiesModule is not installed
 *  6    Gandalf   1.5         6/9/99   Ian Formanek    ToolsAction
 *  5    Gandalf   1.4         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  4    Gandalf   1.3         3/26/99  Ian Formanek    Fixed use of obsoleted 
 *       NbBundle.getBundle (this)
 *  3    Gandalf   1.2         1/25/99  Ian Formanek    .properties extensions 
 *       recognition commented out
 *  2    Gandalf   1.1         1/7/99   David Simonek   
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 * Beta Change History:
 *  0    Tuborg    0.36        --/--/98 Jan Formanek    reflecting locales move to org.netbeans.modules.locales
 *  0    Tuborg    0.39        --/--/98 Jaroslav Tulach recognizes property files
 */
