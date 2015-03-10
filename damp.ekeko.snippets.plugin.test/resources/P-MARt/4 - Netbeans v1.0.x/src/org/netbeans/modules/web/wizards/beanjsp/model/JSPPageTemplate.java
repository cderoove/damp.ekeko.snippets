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

package  org.netbeans.modules.web.wizards.beanjsp.model;

import org.netbeans.modules.web.wizards.beanjsp.util.*;
import org.netbeans.modules.web.util.*;

import org.netbeans.modules.web.wizards.beanjsp.ide.netbeans.*;

import java.io.*;
import java.net.*;
import java.util.*;


/**
 *   all the template tags we should have in the page
 * 
 */

public class JSPPageTemplate extends Object {

    /**
     *  These are the elements ( tokens ) in this order we will find in any template
     * that we use to generate the JSP file. 
     */

    public static final String BEGIN_PAGE_DIRECTIVE = "<!-- $$BEGIN PAGE DIRECTIVE -->";			 // NOI18N
    public final static String END_PAGE_DIRECTIVE = "<!-- $$END PAGE DIRECTIVE -->";				 // NOI18N

    public static final String BEGIN_USE_BEAN = "<!-- $$BEGIN USE BEAN -->";						 // NOI18N
    public static final String END_USE_BEAN = "<!-- $$END USE BEAN -->";							 // NOI18N

    public static final String BEGIN_INPUT_DATA = "<!-- $$BEGIN POST/GET VALIDATION -->";			 // NOI18N
    public static final String END_INPUT_DATA = "<!-- $$END POST/GET VALIDATION -->";				 // NOI18N

    public static final String BEGIN_BIZ_METHODS = "<!-- $$BEGIN EXEC BIZ METHODS -->";			 // NOI18N
    public static final String END_BIZ_METHODS = "<!-- $$END EXEC BIZ METHODS -->";				 // NOI18N

    public static final String BEGIN_DYNAMIC_FORM = "<!-- $$BEGIN DYNAMIC FORM -->";				 // NOI18N
    public static final String END_DYNAMIC_FORM = "<!-- $$END DYNAMIC FORM -->";					 // NOI18N



    //// default template names

    public final static String DEF_JSPPAGE_TLT = "Default JSP Page";		 // NOI18N

    public final static String DEF_INPUT_TLT = "Input Page";				 // NOI18N
    public final static String DEF_RESULT_TLT = "Result Page";			 // NOI18N
    public final static String DEF_ERROR_TLT = "Error JSP Page";			 // NOI18N
    public final static String DEF_IO_TLT = "Input/Result Page";			 // NOI18N

    //// default template name files

    public final static String DEF_JSPPAGE_TLT_FILE = "/org/netbeans/modules/web/wizards/beanjsp/resources/tlt/DefJSPPage.tlt";	 // NOI18N

    public final static String DEF_INPUT_TLT_FILE = "/org/netbeans/modules/web/wizards/beanjsp/resources/tlt/InputPage.tlt";		 // NOI18N
    public final static String DEF_RESULT_TLT_FILE = "/org/netbeans/modules/web/wizards/beanjsp/resources/tlt/ResultPage.tlt";		 // NOI18N
    public final static String DEF_ERROR_TLT_FILE = "/org/netbeans/modules/web/wizards/beanjsp/resources/tlt/ErrorPage.tlt";		 // NOI18N
    public final static String DEF_IO_TLT_FILE = "/org/netbeans/modules/web/wizards/beanjsp/resources/tlt/IOPage.tlt";				 // NOI18N

    //// other variables

    String jspTemplateFileName;

    StringBuffer pageDirectiveBuffer;
    StringBuffer useBeanBuffer;

    StringBuffer inputDataBuffer;
    StringBuffer bizMethodsBuffer;


    StringBuffer dynamicFormBuffer;

    StringBuffer templateEndBuffer;



    CharArrayWriter pageDirectiveWriter;
    CharArrayWriter useBeanWriter;
    CharArrayWriter inputDataWriter;
    CharArrayWriter bizMethodsWriter;

    CharArrayWriter dynamicFormWriter;

    String templateData = new String();

    /** Constructor */
    public JSPPageTemplate() {
        resetTemplateWriters();
        resetTemplateBuffers();
    }

    public void resetTemplateBuffers() {

        pageDirectiveBuffer = new StringBuffer();
        useBeanBuffer = new StringBuffer();
        inputDataBuffer = new StringBuffer();
        bizMethodsBuffer = new StringBuffer();

        dynamicFormBuffer = new StringBuffer();

        templateEndBuffer = new StringBuffer();

        templateData = new String();

    }

    public void resetTemplateWriters() {

        pageDirectiveWriter = createCharArrayWriter();
        useBeanWriter = createCharArrayWriter();
        inputDataWriter = createCharArrayWriter();
        bizMethodsWriter = createCharArrayWriter();
        dynamicFormWriter = createCharArrayWriter();
    }

    private CharArrayWriter createCharArrayWriter() {
        return new CharArrayWriter ();
    }

    private PrintWriter createPrintWriter(Writer out) {
        return new PrintWriter(out,true);
    }

    public PrintWriter getPageDirectiveWriter() {
        return createPrintWriter(pageDirectiveWriter);
    }

    public PrintWriter getUseBeanWriter() {
        return createPrintWriter(useBeanWriter);
    }

    public PrintWriter getInputDataWriter() {
        return createPrintWriter(inputDataWriter);
    }

    public PrintWriter getBizMethodsWriter() {
        return createPrintWriter(bizMethodsWriter);
    }

    public PrintWriter getInputFormWriter() {
        // return createPrintWriter(inputFormWriter);
        return getDynamicFormWriter();   //// backward compatibility
    }

    public PrintWriter getResultFormWriter() {
        return getDynamicFormWriter();   //// backward compatibility
    }

    public PrintWriter getDynamicFormWriter() {
        return createPrintWriter(dynamicFormWriter);
    }



    public int makeTemplateDataChunk(StringBuffer chunkBuffer,
                                     String token,
                                     int startIdx) {
        int idx = -1;
        idx = templateData.indexOf(token);
        if(idx >= 0 ) {
            chunkBuffer.append(templateData.substring(startIdx,idx+token.length()));
            return idx+token.length();
        }else
            return startIdx;
    }

    public void createTemplateDataChunks() {

        int startIdx = 0;
        startIdx = makeTemplateDataChunk(pageDirectiveBuffer,END_PAGE_DIRECTIVE,startIdx);
        startIdx = makeTemplateDataChunk(useBeanBuffer,END_USE_BEAN,startIdx);
        startIdx = makeTemplateDataChunk(inputDataBuffer,END_INPUT_DATA,startIdx);
        startIdx = makeTemplateDataChunk(bizMethodsBuffer,END_BIZ_METHODS,startIdx);
        startIdx = makeTemplateDataChunk(dynamicFormBuffer,END_DYNAMIC_FORM,startIdx);
        templateEndBuffer.append(templateData.substring(startIdx));

    }

    public void applyDynamicDataToTemplateDataChunk(StringBuffer chunkBuffer,
            String token,
            CharArrayWriter charWriter) {

        int chunkLen = chunkBuffer.length();
        int tokenLen = token.length();
        if(chunkLen > 0 && chunkLen >= tokenLen ) {
            chunkBuffer.insert(chunkLen-tokenLen,charWriter.toString());
        }
    }

    public void applyDynamicDataFromWriters() {

        applyDynamicDataToTemplateDataChunk(pageDirectiveBuffer,END_PAGE_DIRECTIVE,pageDirectiveWriter);
        applyDynamicDataToTemplateDataChunk(useBeanBuffer,END_USE_BEAN,useBeanWriter);
        applyDynamicDataToTemplateDataChunk(inputDataBuffer,END_INPUT_DATA,inputDataWriter);
        applyDynamicDataToTemplateDataChunk(bizMethodsBuffer,END_BIZ_METHODS,bizMethodsWriter);
        applyDynamicDataToTemplateDataChunk(dynamicFormBuffer,END_DYNAMIC_FORM,dynamicFormWriter);
    }

    public void loadTemplateData(String templateFilePath) {
        try {
            File templateFile = new File(templateFilePath);
            FileReader templateReader = new FileReader(templateFile);

            loadTemplateData(templateReader);
            templateReader.close();

        }catch(Exception ex) { Debug.print(ex);}

    }

    public void loadTemplateData(URL templateURL) {
        try {
            InputStreamReader templateReader = new  InputStreamReader(templateURL.openStream());
            loadTemplateData(templateReader);
            templateReader.close();

        }catch(Exception ex) { Debug.print(ex);}
    }

    public void loadTemplateData(Reader templateReader) {
        try {

            StringBuffer templateDataBuffer = new StringBuffer();

            int size = 0;
            char[] data = new char[1024];
            for(;;) {
                size = templateReader.read(data);
                if(size <= 0 )
                    break;
                templateDataBuffer.append(data,0,size);
            }

            templateData = templateDataBuffer.toString();
            createTemplateDataChunks();
            templateData = new String();  // release memory

        }catch(Exception ex) { Debug.print(ex);}
    }


    public StringBuffer getJSPFileContent() {
        try {

            StringBuffer jspFileBuffer = new StringBuffer();

            applyDynamicDataFromWriters();

            jspFileBuffer.append(pageDirectiveBuffer.toString());
            jspFileBuffer.append(useBeanBuffer.toString());
            jspFileBuffer.append(inputDataBuffer.toString());
            jspFileBuffer.append(bizMethodsBuffer.toString());
            jspFileBuffer.append(dynamicFormBuffer.toString());
            jspFileBuffer.append(templateEndBuffer.toString());

            return jspFileBuffer;

        }catch(Exception ex) { Debug.print(ex);}

        return null;
    }

    public void saveAs(Writer jspFileWriter) {

        try {

            StringBuffer jspFileBuffer = getJSPFileContent();

            if(jspFileBuffer != null) {
                jspFileWriter.write(jspFileBuffer.toString());
            }else {
                // Debug.println("Fatal Error: No JSP Content");
            }

            jspFileWriter.close();

        }catch(Exception ex) { Debug.print(ex);}
    }

    public void saveAs(File jspFile) {
        try {
            FileWriter jspFileWriter = new FileWriter(jspFile);
            saveAs(jspFileWriter);
        }catch(Exception ex) { Debug.print(ex);}
    }

    public void saveAs(String jspFilePath) {
        try{
            File jspFile = new File(jspFilePath);
            saveAs(jspFile);
        }catch(Exception ex) { Debug.print(ex);}
    }

    public void saveAsInRepository(String name, String ext,boolean overwrite) {
        try {
            //NB here take IDE repository help to save the file
            StringBuffer jspContent = getJSPFileContent();
            IDEHelper.saveJSPFileAs(name,ext,jspContent,overwrite);
            //FileWriter jspFileWriter = new FileWriter(jspFile);
            //saveAs(jspFileWriter);
        }catch(Exception ex) { Debug.print(ex);}
    }


    public static Vector getAvailableTemplates() {
        // return new Vector();
        Vector templates = new Vector();
        try {
            Class templateClass = JSPPageTemplate.class;
            JSPPageTemplate tlt = new JSPPageTemplate();
            Enumeration templateURLs = tlt.getClass().getClassLoader().getResources("org/netbeans/modules/web/core/wizard/jasper/webapp/model/*.tlt");		 // NOI18N
            for(;templateURLs.hasMoreElements(); ) {
                URL templateURL = (URL)templateURLs.nextElement();
                templates.add(templateURL.getFile());
            }
        }catch(Exception ex){Debug.print(ex);}

        return templates;
    }

    public Reader getTemplateReader(String name) {
        try {
            Reader templateReader = new InputStreamReader(this.getClass().getResourceAsStream(name));
            return templateReader;
        }catch(Exception ex) { Debug.print(ex);}
        return null;
    }

    public Reader getDefaultTemplate() {
        return getTemplateReader(DEF_JSPPAGE_TLT_FILE);
    }


    public Reader getDefaultInputPageTemplate() {
        return getTemplateReader(DEF_INPUT_TLT_FILE);
    }

    public Reader getDefaultResultPageTemplate() {
        return getTemplateReader(DEF_RESULT_TLT_FILE);
    }

    public Reader getDefaultErrorPageTemplate() {
        return getTemplateReader(DEF_ERROR_TLT_FILE);
    }

    public Reader getDefaultIOPageTemplate() {
        return getTemplateReader(DEF_IO_TLT_FILE);
    }


    public static String getPageTemplateFileName(String tltDisplayName) {
        // Debug.println("Comparing template Disp Name "+tltDisplayName);
        if(tltDisplayName.equals(DEF_INPUT_TLT))
            return DEF_INPUT_TLT_FILE;
        else if(tltDisplayName.equals(DEF_RESULT_TLT)) {
            return DEF_RESULT_TLT_FILE;
        }else if(tltDisplayName.equals(DEF_ERROR_TLT)) {
            return DEF_ERROR_TLT_FILE;
        }else if(tltDisplayName.equals(DEF_IO_TLT)) {
            return DEF_IO_TLT_FILE;
        }else {
            // Debug.println("Returning default!!! for "+tltDisplayName);
            return DEF_JSPPAGE_TLT_FILE;
        }
    }

    public static Vector getPageTemplateNames() {
        Vector tltNames = new Vector();
        tltNames.addElement(DEF_JSPPAGE_TLT);
        tltNames.addElement(DEF_INPUT_TLT);
        tltNames.addElement(DEF_RESULT_TLT);
        tltNames.addElement(DEF_ERROR_TLT);
        tltNames.addElement(DEF_IO_TLT);
        return tltNames;
    }


    public static void main(String[] args) {

    }

}


