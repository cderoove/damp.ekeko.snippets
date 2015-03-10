package org.acm.seguin.ide.netbeans;

import java.io.*;
import javax.swing.*;

import org.acm.seguin.ide.common.*;
import org.openide.*;
import org.openide.cookies.*;
import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.openide.nodes.*;
import org.openide.windows.*;

public class NetBeansEditorOperations extends EditorOperations {

    private final int NEW_LINE_LENGTH = 1;    
    
    /**
     * Return the current text selected in the IDE
     *
     * @return currently selected text
     */
    public String getSelectionFromIDE() {
        return getCurrentEditorPane().getSelectedText();        
    }
    
    /**
     * Returns true if the current file being edited is a java file
     *
     * @return true if the current file is a java file
     */
    public boolean isJavaFile() {
        return (getFileObject().getExt().equals("java"));
    }
    
    /**
     * Return the current File being edited
     *
     * @return Current file being edited or null if nothing selected
     */
    public File getFile() {
        return new File(getFileObject().getNameExt());
    }
    
    private FileObject getFileObject() {
        Node[] nodes = TopComponent.getRegistry().getCurrentNodes();
        for (int i = 0; i < nodes.length; i++) {
            Node.Cookie cookie = nodes[i].getCookie(DataObject.class);
            if (cookie != null) {
                return (FileObject) ((DataObject) cookie).files().iterator().next();
            }
        }

        //(PENDING) should be an exception
        return null;
    }
    
    /**
     * Return the current line number
     *
     * @return The 1-based line number
     */
    public int getLineNumber() {
        BufferedReader reader = getDocumentTextReader();

        int offset = getCurrentEditorPane().getCaretPosition();

        int lineNumber = 0;
        int currOffset = 0;

        while (currOffset <= offset) {
            String currLine = null;
            try {
                currLine = reader.readLine();
            } catch (IOException ioe) {
                ioe.printStackTrace();
                return -1;
            }
            currOffset += currLine.length() + NEW_LINE_LENGTH;
            lineNumber++;
        }
        
        return lineNumber;          
    }
    
    /**
     * Sets the line number
     *
     * @param  value  New 1-based line number
     */
    public void setLineNumber(int lineNumber) {
        if (lineNumber < 1) {
            throw new IllegalArgumentException(
             "lineNumber must be 1 or greater: " + lineNumber);
        }
        
        int targetOffset = 0;
        int lineCount = 1;
        
        BufferedReader reader = getDocumentTextReader();

        String currLine = null;
        try {
            currLine = reader.readLine();
            while (currLine != null && lineCount < lineNumber) {
                targetOffset += currLine.length() + NEW_LINE_LENGTH;
                lineCount++;
                currLine = reader.readLine();
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
            return;
        }
        
        if (currLine == null) { 
            if (lineCount < lineNumber) {
                throw new IllegalArgumentException(
                 "lineNumber is past end of document!: " + lineNumber);
            }
            
            if (lineCount > 0) {
                // no new line after last line
                targetOffset--;
            }
        }
        
        getCurrentEditorPane().setCaretPosition(targetOffset);        
    }
    
    /**
     * Returns the frame that contains the editor. If this is not available or
     * you want dialog boxes to be centered on the screen return null from this
     * operation.
     *
     * @return  The frame that contains the editor
     */
    public JFrame getEditorFrame() {
        return null;
    }
    
    /**
     * Return the text of the file being edited
     *
     * @return Text of file being edited
     */
    public String getStringFromIDE() {
        return getCurrentEditorPane().getText();
    }
    
    /**
     * Set the text of the file being edited
     *
     * @param  text
     */
    public void setStringInIDE(String text) {
        getCurrentEditorPane().setText(text);
    }
    
    private JEditorPane getCurrentEditorPane() {
        TopComponent comp =
            TopManager.getDefault().getWindowManager().getRegistry().getActivated();
        Node[] nodes = comp.getRegistry().getActivatedNodes();

        EditorCookie cookie = null;
        if (nodes.length == 1) {
            cookie = (EditorCookie) nodes[0].getCookie(EditorCookie.class);
        }

        JEditorPane[] panes = cookie.getOpenedPanes();
        if (panes.length == 1) {
            return panes[0];
        }

        return null;
    }

    private BufferedReader getDocumentTextReader() {
        BufferedReader reader = new BufferedReader(new StringReader(
            getCurrentEditorPane().getText()));
        return reader;
    }
    
}
