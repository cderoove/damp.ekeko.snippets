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

package org.netbeans.modules.i18n;

import java.util.ResourceBundle;
import java.io.IOException;
import java.beans.Introspector;
import java.awt.BorderLayout;
import java.awt.event.*;
import java.awt.Dialog;
import javax.swing.text.StyledDocument;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.text.Caret;
import javax.swing.KeyStroke;
import javax.swing.text.JTextComponent;
import javax.swing.text.BadLocationException;

import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;
import org.openide.cookies.SourceCookie;
import org.openide.cookies.EditorCookie;
import org.openide.nodes.Node;
import org.openide.loaders.DataObject;
import org.openide.NotifyDescriptor;
import org.openide.DialogDescriptor;
import org.openide.TopManager;

import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.GuardedDocument;
import org.netbeans.editor.BaseCaret;
import org.netbeans.editor.Utilities;

import org.netbeans.modules.properties.ResourceBundleStringEditor;
import org.netbeans.modules.properties.ResourceBundleString;
import org.netbeans.modules.properties.PropertiesDataObject;
import org.netbeans.modules.properties.Util;

import org.netbeans.modules.form.RADComponent;
import org.netbeans.modules.form.FormDataObject;

/**
* Localize support class. Dependent on the editor module and the form module.
*
* @author   Petr Jiricka
*/
public class LocalizeSupport {

    public LocalizeSupport() {}

    public static final String LOCALIZE_FINDER_PROP = "org.netbeans.modules.i18n.LOCALIZE_FINDER";

    private static LocalizeSupport instance;
    private LocalizeInfo locInfoInstance;
    private Dialog localizeDialog;
    private LocalizePanel localizePanel;
    private JEditorPane currentComponent;
    private EditorCookie editCook;

    private ResourceBundleStringEditor strEdit = new ResourceBundleStringEditor();

    protected BaseDocument myDoc;
    protected DataObject obj;

    public static LocalizeSupport getLocalizeSupport() {
        if (instance == null)
            instance = new LocalizeSupport();
        return instance;
    }

    public void localize(BaseDocument doc, DataObject obj) {
        this.obj = obj;
        if (this.myDoc != doc) {
            this.myDoc = doc;
            reset();

            // initialize the component
            editCook = (EditorCookie)obj.getCookie(EditorCookie.class);
            if (editCook == null)
                return;  // PENDING
            JEditorPane[] panes = editCook.getOpenedPanes();
            if (panes.length == 0) {
                NotifyDescriptor.Message message = new NotifyDescriptor.Message(NbBundle.getBundle(LocalizeSupport.class).
                                                   getString("MSG_CouldNotOpen"), NotifyDescriptor.ERROR_MESSAGE);
                TopManager.getDefault().notify(message);
                return;
            }
            currentComponent = panes[0];
            currentComponent.getCaret().setDot(0);
            // initializes the finder
            getLocalizeFinder(doc).initialize();

            // do the search
            if (find(-1)) {
                showDialog();
                fillDialogValues();
            }
            else {
                NotifyDescriptor.Message message = new NotifyDescriptor.Message(NbBundle.getBundle(LocalizeSupport.class).
                                                   getString("MSG_NoLocalizableString"), NotifyDescriptor.ERROR_MESSAGE);
                TopManager.getDefault().notify(message);
            }
        }
    }

    /** Ensures that the component has not been closed.
    *  @param position position to set the cursor to. If position == -1, sets to the cursor position in
    *  the previously used component.
    * Returns true if the component was validated. */
    private boolean ensureComponentValid(int position) {
        JEditorPane[] panes = editCook.getOpenedPanes();
        if (panes.length == 0)
            return false;
        // try the ones which are open now
        for (int i=0; i < panes.length; i++) {
            if (panes[i] == currentComponent) {
                currentComponent.requestFocus();
                if (position != -1)
                    currentComponent.getCaret().setDot(position);
                return true;
            }
        }
        // not found
        int dot = (position != -1) ?
                  position :                                // case 1
                  ((currentComponent == null) ?             // case 2
                   0 :                                     // case 2a
                   currentComponent.getCaret().getDot());  // case 2b
        currentComponent = panes[0];
        currentComponent.getCaret().setDot(dot);
        return true;
    }

    private LocalizeInfo getLocalizeInfo() {
        if (locInfoInstance == null)
            locInfoInstance = new LocalizeInfo();
        return locInfoInstance;
    }

    private void reset() {
        locInfoInstance = null;
        currentComponent = null;
        hideDialog();
    }

    /* Replace button handler. */
    private void doReplace() {
        ResourceBundleString str = null;
        try {
            str = (ResourceBundleString)localizePanel.getPropertyValue();
        }
        catch (IllegalStateException e) {
            NotifyDescriptor.Message nd = new NotifyDescriptor.Message(NbBundle.getBundle(LocalizeSupport.class).
                                          getString("EXC_BadKey"), NotifyDescriptor.ERROR_MESSAGE);
            TopManager.getDefault().notify(nd);
            return;
        }
        int position = getLocalizeInfo().getPosition();
        int len = getLocalizeInfo().getLength();
        if (str instanceof ResourceBundleStringForm) {
            // form
            replaceInForm(getComponent(getLocalizeInfo().getComponentName()),
                          getLocalizeInfo().getPropertyName(), (ResourceBundleStringForm)str);
        }
        else {
            // no form
            strEdit.setValue(str);
            strEdit.setClassName(obj.getName());
            try {
                replaceDirect(position, len, strEdit.getJavaInitializationString());
            }
            catch (BadLocationException e) {
                throw new InternalError();
            }
        }
        if (find(position)) {
            showDialog();
            fillDialogValues();
        }
        else
            doCancel();
    }

    /* Replace All button handler. */
    private void doReplaceAll() {
        // pending
    }

    /* Skip button handler. */
    private void doSkip() {
        if (find(-1)) {
            showDialog();
            fillDialogValues();
        }
        else
            doCancel();
    }

    /* Cancel button handler. */
    private void doCancel() {
        hideDialog();
        myDoc = null;
    }

    private boolean replaceDirect(int startPos, int len, String replaceWith) throws BadLocationException {
        if (ensureComponentValid(startPos)) {
            Caret caret = currentComponent.getCaret();
            /*if (!caret.isSelectionVisible()) {
              if (!find(-1)) { // nothing found
                return false;
              }
        }*/
            // now there's selected text to be replaced
            BaseDocument doc = (BaseDocument)currentComponent.getDocument();
            /*int startPos = currentComponent.getSelectionStart();
            int len = currentComponent.getSelectionEnd() - startPos;*/
            try {
                doc.atomicLock();
                if (len > 0) {
                    doc.remove(startPos, len);
                }
                if (replaceWith != null && replaceWith.length() > 0) {
                    doc.insertString(startPos, replaceWith, null);
                }
            } finally {
                doc.atomicUnlock();
            }
        }
        return true;
    }

    private boolean replaceInForm(RADComponent component, String propertyName,
                                  ResourceBundleStringForm newValue) {
        RADComponent.RADProperty property = component.getPropertyByName(propertyName);
        try {
            property.setCurrentEditor(new ResourceBundleStringFormEditor());
            property.setValue(newValue);
            return true;
        }
        catch (Exception e) {
            // pending
            TopManager.getDefault().notifyException(e);
            return false;
        }
    }

    /** Finds from the position (or the current position if position == -1,
    * returns true if a hardcoded string was found 
    */
    public boolean find(int position) {
        if (ensureComponentValid(position)) {
            Utilities.clearStatusText(currentComponent);
            Caret caret = currentComponent.getCaret();
            BaseDocument doc = (BaseDocument)currentComponent.getDocument();
            int dotPos = caret.getDot();
            int[] ret = find(currentComponent, dotPos, -1);
            if (ret != null) {
                //System.out.println("find: returned [" + ret[0] + ", " + ret[1] + ", " +ret[2] + ", " +ret[3] + "]");
                try {
                    getLocalizeInfo().setPosition(ret[0]);
                    getLocalizeInfo().setLength(ret[1]);
                    getLocalizeInfo().setHardString(extractString(doc.getText(ret[0], ret[1])));
                    getLocalizeInfo().setHardLine(doc.getText(ret[2], ret[3]));
                    getLocalizeInfo().setGuarded((doc instanceof GuardedDocument) &&
                                                 ((GuardedDocument)doc).isPosGuarded(ret[0]));
                }
                catch (BadLocationException e) {
                    throw new InternalError();
                }
                caret.setDot(ret[0]);
                caret.moveDot(ret[0] + ret[1]);
                Utilities.setStatusText(currentComponent,
                                        java.text.MessageFormat.format(NbBundle.getBundle(LocalizeSupport.class).
                                                                       getString("CTL_HardStringFound"),
                                                                       new Object[] {Utilities.debugPosition((BaseDocument)currentComponent.getDocument(), ret[0])}));
                /*"Hardcoded string found at " +
                  Utilities.debugPosition((BaseDocument)currentComponent.getDocument(), ret[0]));*/
                return true;
            } else { // not found
                Utilities.setStatusBoldText(currentComponent, NbBundle.getBundle(LocalizeSupport.class).
                                            getString("CTL_HardStringNotFound"));
            }
        }
        return false;
    }

    private String extractString(String sourceString) {
        if ((sourceString.length() >= 2) &&
                (sourceString.charAt(0) == '"') &&
                (sourceString.charAt(sourceString.length() - 1) == '"'))
            sourceString = sourceString.substring(1, sourceString.length() - 1);
        StringBuffer result = new StringBuffer();
        for (int i = 0; i < sourceString.length() ; i++) {
            if ((sourceString.charAt(i) == '\\') && (i + 1 < sourceString.length()))
                i++;
            result.append(sourceString.charAt(i));
        }
        return result.toString();
    }

    /** Find the searched expression
    * @startPos position where the start of the search will occur. It must
    *   be valid position greater or equal than zero
    * @endPos position where the search will stop. -1 doesn't mean the end
    *   of document in this case but rather the default behavior which
    *   depends on the direction and wrapping.
    * @return position and length of the text found provided in array
    *    containing these two ints; returns null if nothing is found
    */
    public int[] find(JTextComponent c, int startPos, int endPos) {
        boolean wrap = false;
        BaseDocument doc = (BaseDocument)c.getDocument();
        if (c != null) {
            LocalizeFinder fnd = getLocalizeFinder(doc);
            int pos = -1;
            try {
                int docLen = doc.getLength();
                if (startPos == -1) {
                    startPos = docLen;
                }

                int restPatch = 2; // !!! quick infinite loop patch
                while (true && restPatch-- > 0) {
                    int limitPos;
                    if (endPos == -1) { // invalid pos
                        limitPos = docLen;
                    } else {
                        if (startPos < endPos) {
                            limitPos = endPos;
                        } else {
                            limitPos = docLen;
                        }
                    }

                    pos = doc.find(fnd, startPos, limitPos);
                    //System.out.println("searched area [" + startPos + ", " + limitPos + "], docLen=" + docLen + ", pos=" + pos);

                    if (pos != -1) {
                        break;
                    }

                    if (wrap) {
                        if (endPos == -1) {
                            if (limitPos == docLen) {
                                startPos = 0;
                            } else {
                                break;
                            }
                        } else { // endPos != -1 (&& wrap)
                            if (limitPos == endPos) {
                                break;
                            }
                            startPos = 0;
                        }
                    } else { // no wrap set
                        break;
                    }
                }

            } catch (BadLocationException e) {
                throw new Error(); // shouldn't happen
            }

            if (pos != -1) {
                int[] ret = new int[4];
                ret[0] = pos;
                ret[1] = fnd.getFoundLength();
                ret[2] = fnd.getLineStart();
                ret[3] = fnd.getLineLength();
                return ret;
            }
        }
        return null;
    }

    /** Get localize finder */
    public LocalizeFinder getLocalizeFinder(BaseDocument doc) {
        LocalizeFinder localizeFinder = (LocalizeFinder)doc.getProperty(LOCALIZE_FINDER_PROP);

        if (localizeFinder == null) {
            localizeFinder = new LocalizeFinder();
            doc.putProperty(LOCALIZE_FINDER_PROP, localizeFinder);
        }

        return localizeFinder;
    }

    private void showDialog() {
        if (localizeDialog == null) {
            localizePanel = new LocalizePanel();
            final JButton[] buttons = localizePanel.getButtons();
            localizeDialog = createLocalizeDialog(localizePanel, buttons, 0, 3,
                                                  new ActionListener() {
                                                      public void actionPerformed(ActionEvent evt) {
                                                          if (evt.getSource() == buttons[0])
                                                              doReplace();
                                                          if (evt.getSource() == buttons[1])
                                                              doReplaceAll();
                                                          if (evt.getSource() == buttons[2])
                                                              doSkip();
                                                          if (evt.getSource() == buttons[3])
                                                              doCancel();
                                                      }
                                                  }
                                                 );
            localizeDialog.addWindowListener(new WindowAdapter() {
                                                 public void windowClosing(WindowEvent e) {
                                                     doCancel();
                                                 }
                                             });
            localizeDialog.setVisible(true);
        }
    }

    private void hideDialog() {
        if (localizeDialog != null) {
            localizeDialog.setVisible(false);
            localizeDialog.dispose();
            localizeDialog = null;
        }
    }

    private void fillDialogValues() {
        localizePanel.setLocalizeInfo(getLocalizeInfo());
        PropertiesDataObject pdo = (localizePanel.getValue() == null) ? null : localizePanel.getValue().getResourceBundle();
        localizePanel.setValue(getLocalizeInfo().getDefaultBundleString(pdo));
    }

    private Dialog createLocalizeDialog(JPanel localizePanel, final JButton[] buttons,
                                        final int defaultButtonIndex, final int cancelButtonIndex,
                                        final ActionListener l) {
        DialogDescriptor dd = new DialogDescriptor(localizePanel, NbBundle.getBundle(LocalizeSupport.class).
                              getString("CTL_LocalizeDialogTitle") + " " + obj.getName(), false, l);
        dd.setOptionType(DialogDescriptor.DEFAULT_OPTION);
        dd.setOptions(new Object[0]);
        dd.setAdditionalOptions(new Object[0]);
        dd.setHelpCtx (new HelpCtx (LocalizePanel.class));
        // add listener to buttons
        for (int i = 0; i < buttons.length; i++) {
            buttons[i].addActionListener(l);
        }
        return TopManager.getDefault().createDialog(dd);
        /*
            
            JDialog d = new JDialog();
            d.setTitle(NbBundle.getBundle(LocalizeSupport.class).getString("CTL_LocalizeDialogTitle") + 
              " " + obj.getName());
            d.getContentPane().add(localizePanel, BorderLayout.CENTER);
            // add listener to buttons
            for (int i = 0; i < buttons.length; i++) {
              buttons[i].addActionListener(l);
            }
            d.getRootPane().setDefaultButton(buttons[defaultButtonIndex]);
            d.getRootPane().registerKeyboardAction(
              new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                  l.actionPerformed(
                    new ActionEvent(buttons[cancelButtonIndex], 0, null));
                }
              },
              KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, true),
              JComponent.WHEN_IN_FOCUSED_WINDOW
            );
            d.pack();
            d.setLocation(100, 100);
            return d;*/
    }

    /** Returns the RADComponent or null if not found. Must be called after initing componentName. */
    private RADComponent getComponent(String compName) {
        if (compName == null)
            throw new IllegalStateException();
        if ((compName.length() > 0) && (obj instanceof FormDataObject)) {
            return ((FormDataObject)obj).getFormEditor().getFormManager().findRADComponent(compName);
        }
        else
            return null;
    }


    /** Class providing information about localization parameters.
    */
    public class LocalizeInfo {

        private ResourceBundleStringEditor     editor = new ResourceBundleStringEditor ();
        private ResourceBundleStringFormEditor editorForm = new ResourceBundleStringFormEditor ();

        /** Holds value of property hardString. */
        private String hardString;

        /** Holds value of property hardLine. */
        private String hardLine;

        /** Holds value of property guarded. */
        private boolean guarded;

        /** Holds the position where hardcored string was found. */
        private int position;

        /** Holds the lenght of the hardcored string. */
        private int len;

        private String componentName;
        private String propertyName;

        /** Getter for property hardString.
        *@return Value of property hardString.
        */
        public String getHardString() {
            return hardString;
        }

        /** Setter for property hardString.
        *@param hardString New value of property hardString.
        */
        public void setHardString(String hardString) {
            this.hardString = hardString;
            componentName = null;
            propertyName = null;
        }

        /** Getter for property hardLine.
         *@return Value of property hardLine.
         */
        public String getHardLine() {
            return hardLine;
        }

        /** Setter for property hardLine.
         *@param hardLine New value of property hardLine.
         */
        public void setHardLine(String hardLine) {
            this.hardLine = hardLine;
            componentName = null;
            propertyName = null;
        }

        /** Getter for property position.
        *@return Value of property position.
        */
        public int getPosition() {
            return position;
        }

        /** Setter for property position.
        *@param position New value of property position.
        */
        public void setPosition(int position) {
            this.position = position;
        }

        /** Getter for property lenght.
        *@return Value of property length.
        */
        public int getLength() {
            return len;
        }

        /** Setter for property length.
        *@param position New value of property length.
        */
        public void setLength(int len) {
            this.len = len;
        }

        /** Getter for property guarded.
             *@return Value of property guarded.
             */
        public boolean isGuarded() {
            return guarded;
        }

        /** Setter for property guarded.
         *@param guarded New value of property guarded.
         */
        public void setGuarded(boolean guarded) {
            this.guarded = guarded;
        }

        /** Getter for property componentName.
         *@return Value of property componentName.
         */
        public String getComponentName() {
            if (componentName == null)
                analyzeForm();
            return componentName;
        }

        /** Getter for property propertyName.
         *@return Value of property propertyName.
         */
        public String getPropertyName() {
            if (propertyName == null)
                analyzeForm();
            return propertyName;
        }

        /** Gets the default bundle string given a PropertiesDataObject. May return null if the value
        * can not be replaced (i.e. is in a guraded block and a form property has not been found). */
        public ResourceBundleString getDefaultBundleString(PropertiesDataObject pdo) {
            if (isGuarded()) {
                // guarded block
                if (getComponentName().length() > 0) {
                    // form
                    ResourceBundleStringForm str = new ResourceBundleStringForm();
                    str.setResourceBundle(pdo);
                    str.setDefaultValue(getHardString());
                    RADComponent rcomp = getComponent(componentName);
                    RADComponent.RADProperty rprop = rcomp.getPropertyByName (propertyName);
                    editorForm.setRADComponent(rcomp, rprop);
                    editorForm.setValue(str);
                    return (ResourceBundleStringForm)editorForm.getValue();
                }
                else {
                    // not found
                    InvalidResourceBundleString str = new InvalidResourceBundleString();
                    str.setResourceBundle(pdo);
                    return str;
                }
            }
            else {
                // no form
                ResourceBundleString str;
                if (pdo == null) {
                    editor.setValue(null);
                    str = (ResourceBundleString)editor.getValue();
                }
                else {
                    str = new ResourceBundleString();
                    str.setResourceBundle(pdo);
                }
                putDefaultStringKey(str);
                str.setDefaultValue(getHardString());
                return str;
            }
        }

        private void putDefaultStringKey(ResourceBundleString str) {
            String baseKey = Util.stringToKey(hardString);
            int index = 0;
            str.setKey(baseKey);
            while (str.getPropertyValue() != null) {
                index ++;
                str.setKey(baseKey + "." + index);
            }
        }

        /** Analyzes a line of text in a guraded block, tries to find the name
        *  of the component and of the property, so it can be changed via FormEditor API
        */                     
        private void analyzeForm() {
            componentName = "";
            propertyName = "";

            // must be guarded
            if (!isGuarded())
                return;

            // must be a form
            if (!(obj instanceof FormDataObject))
                return;

            int firstDot = hardLine.indexOf('.');
            // must contain a dot
            if (firstDot == -1)
                return;

            String compName = hardLine.substring(0, firstDot).trim();
            // find the component name
            RADComponent comp = getComponent(compName);
            // must be a valid component
            if (comp == null)
                return;

            int leftPar = hardLine.indexOf('(', firstDot);
            // must contain ( after .
            if (leftPar == -1)
                return;

            String setterName = hardLine.substring(firstDot + 1, leftPar).trim();
            // must be a setter
            if (!setterName.startsWith("set"))
                return;

            String propName = Introspector.decapitalize(setterName.substring("set".length()));

            RADComponent.RADProperty property = comp.getPropertyByName(propName);
            if (property == null)
                return;

            // now it's fine
            componentName = compName;
            propertyName = propName;
        }

    }

    /** ResourceBundleString representing invalid value. */
    public static class InvalidResourceBundleString extends ResourceBundleString {

        static final long serialVersionUID =8135168688193465968L;
        public InvalidResourceBundleString() {
        }

    }
}

/*
 * <<Log>>
 *  13   Gandalf-post-FCS1.11.1.0    3/8/00   Miloslav Metelka StyledGuardedDocument ->
 *       GuardedDocument
 *  12   Gandalf   1.11        11/27/99 Patrik Knakal   
 *  11   Gandalf   1.10        11/5/99  Jesse Glick     Context help jumbo 
 *       patch.
 *  10   Gandalf   1.9         10/25/99 Petr Jiricka    Various bugfixes
 *  9    Gandalf   1.8         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  8    Gandalf   1.7         10/15/99 Petr Jiricka    Fixed bug - after 
 *       replace in guarded block the cursor jumps at the end of the guarded 
 *       block.
 *  7    Gandalf   1.6         10/13/99 Petr Jiricka    Debug messages removed
 *  6    Gandalf   1.5         10/12/99 Petr Jiricka    Form eidtor component 
 *       search fixed
 *  5    Gandalf   1.4         10/10/99 Petr Hamernik   console debug messages 
 *       removed.
 *  4    Gandalf   1.3         9/13/99  Petr Jiricka    Reflects changes in 
 *       Editor module
 *  3    Gandalf   1.2         9/13/99  Ian Formanek    FormAwareEditor.setRADComponent
 *        change
 *  2    Gandalf   1.1         9/8/99   Petr Jiricka    
 *  1    Gandalf   1.0         9/2/99   Petr Jiricka    
 * $
 */
