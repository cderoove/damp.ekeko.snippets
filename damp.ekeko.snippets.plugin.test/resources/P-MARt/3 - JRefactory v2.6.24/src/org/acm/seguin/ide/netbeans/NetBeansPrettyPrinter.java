package org.acm.seguin.ide.netbeans;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import javax.swing.JEditorPane;

import org.acm.seguin.pretty.PrettyPrintFromIDE;

public class NetBeansPrettyPrinter extends PrettyPrintFromIDE {

    private JEditorPane _editorPane;
    // NOTE: A new line is actually 2 characters long but 1 reflects how the 
    // caret positioning works
    private final int NEW_LINE_LENGTH = 1;
    
    public NetBeansPrettyPrinter(EditorCookie editorCookie) {
        super();

        _editorPane = getCurrentEditorPane(editorCookie);
    }

    /** 
     * @return the initial line number, -1 if failed
     */
    protected int getLineNumber() {
        BufferedReader reader = getDocumentTextReader();

        int offset = _editorPane.getCaretPosition();

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
    
    protected void setLineNumber(int lineNumber) {
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
        _editorPane.setCaretPosition(targetOffset);        
    }
    
    /**
     * Gets the initial string from the IDE
     * @return    The file in string format
     */
    protected String getStringFromIDE() {
        return _editorPane.getText();
    }
    
    /**
     * Sets the string in the IDE
     * @param  value  The new file contained in a string
     */
    protected void setStringInIDE(String text) {
        _editorPane.setText(text);
    }

    private JEditorPane getCurrentEditorPane(EditorCookie cookie) {
        JEditorPane[] panes = cookie.getOpenedPanes();
System.err.println("Panes: " + panes);        
        if (panes.length == 1) {
            return panes[0];
        }
        
        return null;
    }
    
    private BufferedReader getDocumentTextReader() {
        BufferedReader reader = new BufferedReader(new StringReader(
            _editorPane.getText()));
        return reader;
    }
    
}
