package test.damp.ekeko.snippets;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.BeforeClass;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

import test.damp.EkekoTestHelper;
import ccw.util.osgi.ClojureOSGi;
import damp.ekeko.EkekoPlugin;
import damp.ekeko.snippets.EkekoSnippetsPlugin;

public class EkekoSnippetsTest {

	private static Bundle myBundle;
	private static Bundle ekekoBundle;


	static {
		myBundle = FrameworkUtil.getBundle(EkekoSnippetsTest.class);
		ekekoBundle = FrameworkUtil.getBundle(EkekoTestHelper.class);

	}

	
	public static File getResourceFile(String relativePath) throws Exception {
		return EkekoTestHelper.getFileFromBundle(myBundle, relativePath);
	}
	
	
	@BeforeClass
	public static void ensureTestCasesExist() throws Exception {
		EkekoTestHelper.ensureProjectImported(myBundle, "/resources/", "Ekeko-JDT");
	}

	@Test
	public void testPluginID() {
		assertEquals(EkekoSnippetsPlugin.PLUGIN_ID, "damp.ekeko.snippets.plugin");		
	}

	@Test 
	public void testEkekoSnippetsMatching() {
		EkekoTestHelper.testClojureNamespace(myBundle, "test.damp.ekeko.snippets.matching");
	}
	
	@Test 
	public void testEkekoSnippetsOperators() {
		EkekoTestHelper.testClojureNamespace(myBundle, "test.damp.ekeko.snippets.operators");
	}

	@Test 
	public void testRequireEkekoSnippets() {
		ClojureOSGi.require(myBundle, "damp.ekeko.snippets");
	}
	
	@Test 
	public void testPersistence() {
		EkekoTestHelper.testClojureNamespace(myBundle, "test.damp.ekeko.snippets.persistence");
	}


	/*
	//disabled because exception is not caught by junit 
	@Test 
	public void testOpenAndMatchSavedTemplates() {
		//should run in UI thread
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				try {
					File templateFile = EkekoTestHelper.getFileFromBundle(myBundle, "/resources/Templates/emptyclass.ekxt");
					IFileStore fileStore = EFS.getLocalFileSystem().getStore(templateFile.toURI());
					IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
					IEditorPart openedEditor = IDE.openEditorOnFileStore(page, fileStore); 
					if(!(openedEditor instanceof TemplateEditor)) 
						throw new Exception("Template file was not opened in correct editor: " + openedEditor.toString());
					TemplateEditor templateEditor = (TemplateEditor) openedEditor;
					templateEditor.runQuery();
				}
				catch(Exception ex) {
					//evil, but need to communicate exceptions to junit runner
					Thread t = Thread.currentThread();
					t.getUncaughtExceptionHandler().uncaughtException(t, ex);
				}
			}
		});
	}

*/



}