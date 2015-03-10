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

package org.netbeans.modules.java;

import java.io.*;
import java.beans.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;

import org.openide.*;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.*;
import org.openide.nodes.*;
import org.openide.loaders.DataNode;
import org.openide.loaders.ExecSupport;
import org.openide.loaders.CompilerSupport;
import org.openide.loaders.MultiDataObject;
import org.openide.src.*;
import org.openide.src.nodes.SourceChildren;
import org.openide.util.HelpCtx;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;

/** The node representation of <code>JavaDataObject</code> for Java sources.
*
* @author Petr Hamernik
*/
public class JavaNode extends DataNode {

    /** generated Serialized Version UID */
    static final long serialVersionUID = -7396485743899766258L;

    private static final String EXECUTION_SET_NAME = "Execution"; // NOI18N

    private static final String PROP_SYNCHRONIZATION_MODE = "SynchMode"; // NOI18N

    private static final String ICON_BASE = "org/netbeans/modules/java/resources/"; // NOI18N

    private static final String[] ICONS = {
        "class", "classMain", "classError", "class", "classMain" // NOI18N
    };

    private static final byte ICON_CLASS = 0;
    private static final byte ICON_CLASS_MAIN = 1;
    private static final byte ICON_CLASS_ERROR = 2;
    private static final byte ICON_BEAN = 3;
    private static final byte ICON_BEAN_MAIN = 4;

    private byte currentIcon;

    /** Create a node for the Java data object using the default children.
    * @param jdo the data object to represent
    */
    public JavaNode (JavaDataObject jdo) {
        /* Changed for multiple factories *
        this(jdo, new SourceChildren(JavaElementNodeFactory.DEFAULT, jdo.getSource()));
        */
        this(jdo, new SourceChildren(JavaDataObject.getExplorerFactory(), jdo.getSource()));
    }

    /** Create a node for the Java data object with configurable children.
    * Subclasses should use this constructor if they wish to provide special display for the child nodes.
    * Typically this would involve creating a subclass of {@link SourceChildren} based
    * on the {@link JavaDataObject#getSource provided source element}; the children list
    * may have extra nodes {@link Children#add(Node[]) added}, either at the {@link Children.Keys#setBefore beginning or end}.
    * @param jdo the data object to represent
    * @param children the children for this node
    */
    public JavaNode (JavaDataObject jdo, Children children) {
        super (jdo, children);
        currentIcon = ICON_CLASS;
        initialize();
    }

    private void initialize () {
        setIconBase(getIconBase() + getIcons()[currentIcon]);
        getJavaDataObject().getSource().addPropertyChangeListener(new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent evt) {
                        sourcePropertyChange(evt);
                    }
                });
    }

    void sourcePropertyChange(PropertyChangeEvent evt) {
        firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
        if (ElementProperties.PROP_STATUS.equals(evt.getPropertyName()))
            requestResolveIcons();
    }

    private void readObject(ObjectInputStream is) throws IOException, ClassNotFoundException {
        is.defaultReadObject();
        initialize();
    }

    /** Create the property sheet.
    * Subclasses may want to override this and add additional properties.
    * @return the sheet
    */
    protected Sheet createSheet () {
        Sheet sheet = super.createSheet();

        Sheet.Set ps = sheet.get(Sheet.PROPERTIES);
        ps.put(new PropertySupport.ReadWrite (
                   PROP_SYNCHRONIZATION_MODE,
                   Byte.class,
                   Util.getString("PROP_synchMode"),
                   Util.getString("HINT_synchMode")
               ) {
                   public Object getValue() {
                       return new Byte(getJavaDataObject().getSynchronizationType());
                   }
                   public void setValue (Object val) throws InvocationTargetException {
                       if (val instanceof Byte) {
                           try {
                               getJavaDataObject().setSynchronizationType(((Byte) val).byteValue());
                               return;
                           }
                           catch(IllegalArgumentException e) {
                           }
                       }
                       throw new IllegalArgumentException();
                   }
                   public PropertyEditor getPropertyEditor() {
                       return new SyncModeEditor();
                   }
               });

        ps = new Sheet.Set ();
        ps.setName(EXECUTION_SET_NAME);
        ps.setDisplayName(Util.getString("PROP_executionSetName"));
        ps.setShortDescription(Util.getString("HINT_executionSetName"));

        ExecSupport es = (ExecSupport) getCookie (ExecSupport.class);
        if (es != null)
            es.addProperties (ps);
        CompilerSupport cs = (CompilerSupport) getCookie (CompilerSupport.class);
        if (cs != null)
            cs.addProperties (ps);

        sheet.put(ps);

        return sheet;
    }

    /** Get the associated Java data object.
    * ({@link #getDataObject} is protected; this provides access to it.)
    * @return the data object
    */
    protected JavaDataObject getJavaDataObject() {
        return (JavaDataObject) getDataObject();
    }

    void requestResolveIcons() {
        SourceElementImpl.PARSING_RP.postRequest(
            new Runnable () {
                public void run () {
                    resolveIcons ();
                }
            }
        );
    }

    /** Get the icon base.
    * This should be a resource path, e.g. <code>/some/path/</code>,
    * where icons are held. Subclasses may override this.
    * @return the icon base
    * @see #getIcons
    */
    protected String getIconBase() {
        return ICON_BASE;
    }

    /** Get the icons.
    * This should be a list of bare icon names (i.e. no extension or path) in the icon base.
    * It should contain five icons in order for:
    * <ul>
    * <li>a regular class
    * <li>a class with a main method
    * <li>a class with a parse error
    * <li>a JavaBean class
    * <li>a JavaBean class with a main method
    * </ul>
    * Subclasses may override this.
    * @return the icons
    * @see #getIconBase
    */
    protected String[] getIcons() {
        return ICONS;
    }

    /** Update the icon for this node based on parse status.
    * Called automatically at the proper times.
    * @see #getIconBase
    * @see #getIcons
    */
    protected void resolveIcons() {
        SourceElement source = getJavaDataObject().getSource();
        byte newIcon = ICON_CLASS;
        String desc = null;

        switch (source.getStatus()) {
        case SourceElement.STATUS_NOT :
            newIcon = currentIcon;
            break;

        case SourceElement.STATUS_ERROR:
        case SourceElement.STATUS_PARTIAL:
            desc = Util.getString("HINT_ParsingErrors");
            newIcon = ICON_CLASS_ERROR;
            break;

        case SourceElement.STATUS_OK:
            Task task = source.prepare();
            FileObject fo = getJavaDataObject().getPrimaryEntry().getFile();
            String name = fo.getName();
            fo = fo.getParent();
            String pack = (fo == null) ? "" : fo.getPackageName('.'); // NOI18N

            // check the package
            Identifier id = source.getPackage();
            String pack2 = (id == null) ? "" : id.getFullName(); // NOI18N
            if (!pack.equals(pack2)) {
                desc = new MessageFormat(Util.getString("FMT_Bad_Package")).format(new Object[] { pack2 });
                //        newIcon = ICON_CLASS_ERROR;
                break;
            }

            // check the runnable and javabean
            ClassElement[] classes = source.getClasses();
            boolean runnable = false;
            boolean javaBean = false;
            for (int i = 0; i < classes.length; i++) {
                if (classes[i].getName().getName().equals(name)) {
                    runnable = classes[i].hasMainMethod();
                    javaBean = classes[i].isDeclaredAsJavaBean();
                    break;
                }
            }

            newIcon = (runnable) ?
          (javaBean ? ICON_BEAN_MAIN : ICON_CLASS_MAIN) :
                      (javaBean ? ICON_BEAN : ICON_CLASS);
            break;
        }

        setShortDescription(desc);
        if (currentIcon != newIcon) {
            currentIcon = newIcon;
            setIconBase(getIconBase() + getIcons()[currentIcon]);
        }
    }

    /** Simple property editor for two-item pulldown. */
    static class SyncModeEditor extends PropertyEditorSupport {

        private static final String[] tags;

        static {
            tags = new String[3];
            tags[JavaDataObject.CONNECT_NOT] = Util.getString("CTL_CONNECT_NOT");
            tags[JavaDataObject.CONNECT_CONFIRM] = Util.getString("CTL_CONNECT_CONFIRM");
            tags[JavaDataObject.CONNECT_AUTO] = Util.getString("CTL_CONNECT_AUTO");
        }

        public String[] getTags () {
            return tags;
        }

        public String getAsText () {
            return tags[((Byte) getValue ()).byteValue ()];
        }

        public void setAsText (String text) throws IllegalArgumentException {
            for (byte i = 0; i < tags.length; i++) {
                if (tags[i].equals (text)) {
                    setValue (new Byte(i));
                    return;
                }
            }
            throw new IllegalArgumentException ();
        }
    }

    /** Called when an object is to be copied to clipboard.
    * @return the transferable object dedicated to represent the
    *    content of clipboard
    * @exception NodeAccessException is thrown when the
    *    operation cannot be performed
    */
    /*
    public TransferableOwner clipboardCopy () throws IOException {
      JavaDataObject obj = (JavaDataObject) getDataObject ();
      if (obj.decideJavaBeanFromClass ()) {
        Class c = obj.getClazz ();
        return new BeanTransferableOwner (super.clipboardCopy (), c.getName (), c);
      }
      else
        return super.clipboardCopy ();
} */


    /*
    static class BeanTransferableOwner extends TransferableOwner.Filter {
      String beanName;

      BeanTransferableOwner (TransferableOwner transferable, String beanName, Class beanClass) {
        super (transferable, new DataFlavor[] {new TransferFlavors.BeanFlavor (beanClass)});
        this.beanName = beanName;
      } */

    /** Creates transferable data for this flavor.
    */
    /*
      public Object getTransferData (DataFlavor flavor)
      throws UnsupportedFlavorException, IOException {
        if (isDataFlavorSupported(flavor)) {
          if (flavor instanceof TransferFlavors.BeanFlavor) return beanName;
          return super.getTransferData (flavor);
        }
        else {
          // not supported flavor
          throw new UnsupportedFlavorException (flavor);
        }
      }
} */
}

/*
 * Log
 *  49   Gandalf   1.48        1/12/00  Petr Hamernik   i18n: perl script used (
 *       //NOI18N comments added )
 *  48   Gandalf   1.47        1/3/00   Petr Jiricka    Properties for execution
 *       and compilation are added only when the corresponding cookies are found
 *       (fixes NullPointerException when replacing ExecSupport and 
 *       CompilerSupport)
 *  47   Gandalf   1.46        10/29/99 Jesse Glick     Using undeprecated 
 *       variant of *Support.addProperties.
 *  46   Gandalf   1.45        10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems copyright in file comment
 *  45   Gandalf   1.44        10/7/99  Petr Hamernik   Java module has its own 
 *       RequestProcessor for source parsing.
 *  44   Gandalf   1.43        9/10/99  Petr Hamernik   some import removed
 *  43   Gandalf   1.42        9/10/99  Jaroslav Tulach Changes to services.
 *  42   Gandalf   1.41        8/6/99   Petr Hamernik   Working with threads 
 *       improved
 *  41   Gandalf   1.40        8/2/99   Petr Hamernik   hopefully deadlock 
 *       prevention
 *  40   Gandalf   1.39        7/28/99  Ian Formanek    Bean icon removed (the 
 *       basic class icon is used for beans as well)
 *  39   Gandalf   1.38        7/15/99  Ian Formanek    Fixed bug 1741 - 
 *       Canceling String Property Editor issues exception
 *  38   Gandalf   1.37        6/28/99  Petr Hrebejk    Multiple node factories 
 *       added
 *  37   Gandalf   1.36        6/10/99  Petr Hamernik   wrong package doesn't 
 *       affect the icon
 *  36   Gandalf   1.35        6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  35   Gandalf   1.34        6/7/99   Petr Hamernik   resolving icons 
 *       transferred to AWT-EventQueue
 *  34   Gandalf   1.33        6/5/99   Petr Hamernik   temporary - resolving 
 *       icons disabled!
 *  33   Gandalf   1.32        6/2/99   Petr Hamernik   connections of java 
 *       sources
 *  32   Gandalf   1.31        5/12/99  Petr Hamernik   ide.src.Identifier 
 *       changed
 *  31   Gandalf   1.30        5/10/99  Jaroslav Tulach DataNode.canRename
 *  30   Gandalf   1.29        5/6/99   Jesse Glick     [JavaDoc]
 *  29   Gandalf   1.28        5/6/99   Petr Hamernik   constructor with 
 *       Children added
 *  28   Gandalf   1.27        5/6/99   Jesse Glick     [JavaDoc]
 *  27   Gandalf   1.26        4/23/99  Petr Hamernik   icons bugfix
 *  26   Gandalf   1.25        4/21/99  Petr Hamernik   debugs removed
 *  25   Gandalf   1.24        4/21/99  Petr Hamernik   Java module updated
 *  24   Gandalf   1.23        4/15/99  Martin Ryzl     getters for icons and 
 *       icon base added
 *  23   Gandalf   1.22        4/7/99   Petr Hamernik   package property changed
 *  22   Gandalf   1.21        4/6/99   Petr Hamernik   bugfixes
 *  21   Gandalf   1.20        4/2/99   Petr Hamernik   
 *  20   Gandalf   1.19        4/2/99   Petr Hamernik   
 *  19   Gandalf   1.18        4/2/99   Petr Hamernik   
 *  18   Gandalf   1.17        4/1/99   Petr Hamernik   
 *  17   Gandalf   1.16        4/1/99   Petr Hamernik   
 *  16   Gandalf   1.15        3/29/99  Petr Hamernik   
 *  15   Gandalf   1.14        3/22/99  Ian Formanek    Icons moved from 
 *       modules/resources to this package
 *  14   Gandalf   1.13        3/19/99  Ales Novak      
 *  13   Gandalf   1.12        3/18/99  Petr Hamernik   
 *  12   Gandalf   1.11        3/16/99  Petr Hamernik   renaming static fields
 *  11   Gandalf   1.10        3/15/99  Petr Hamernik   
 *  10   Gandalf   1.9         3/12/99  Petr Hamernik   
 *  9    Gandalf   1.8         3/10/99  Petr Hamernik   
 *  8    Gandalf   1.7         2/25/99  Petr Hamernik   
 *  7    Gandalf   1.6         2/18/99  Petr Hamernik   
 *  6    Gandalf   1.5         2/11/99  Petr Hamernik   
 *  5    Gandalf   1.4         1/20/99  Petr Hamernik   
 *  4    Gandalf   1.3         1/15/99  Petr Hamernik   
 *  3    Gandalf   1.2         1/15/99  Petr Hamernik   
 *  2    Gandalf   1.1         1/13/99  Petr Hamernik   
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 * Beta Change History:
 *  0    Tuborg    0.14        --/--/98 Jan Formanek    icons added
 *  0    Tuborg    0.15        --/--/98 Petr Hamernik   new text editor, parsing added...
 *  0    Tuborg    0.16        --/--/98 Petr Hamernik   Parsing...
 *  0    Tuborg    0.17        --/--/98 Jan Formanek    removed isCompilationAllowed (the inherited impl already works)
 *  0    Tuborg    0.18        --/--/98 Petr Hamernik   small change
 *  0    Tuborg    0.19        --/--/98 Jan Formanek    static reference to parserManager moved to JavaLoader
 *  0    Tuborg    0.20        --/--/98 Petr Hamernik   localization,...
 *  0    Tuborg    0.22        --/--/98 Jan Jancura     moved to propertySet
 *  0    Tuborg    0.23        --/--/98 Jan Formanek    changed parseChanged semantics - it does not create and
 *  0    Tuborg    0.23        --/--/98 Jan Formanek    set the children if the node has not been initialized.
 *  0    Tuborg    0.23        --/--/98 Jan Formanek    This allows the children to be created from the createInitNodes
 *  0    Tuborg    0.23        --/--/98 Jan Formanek    method (so that the subclasses can do something in there)
 *  0    Tuborg    0.24        --/--/98 Jan Formanek    createJavaItemNodes renamed to createSubNodes and made protected
 *  0    Tuborg    0.25        --/--/98 Petr Hamernik   readobject
 *  0    Tuborg    0.26        --/--/98 Jan Formanek    reflecting changes in cookies
 *  0    Tuborg    0.28        --/--/98 Jan Formanek    templates
 */
