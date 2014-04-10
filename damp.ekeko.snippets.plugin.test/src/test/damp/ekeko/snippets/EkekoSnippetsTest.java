package test.damp.ekeko.snippets;

import static org.junit.Assert.assertEquals;

import org.junit.BeforeClass;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

import test.damp.EkekoTestHelper;
import ccw.util.osgi.ClojureOSGi;
import damp.ekeko.snippets.EkekoSnippetsPlugin;

public class EkekoSnippetsTest {
	
	private static Bundle myBundle;
	
	static {
		myBundle = FrameworkUtil.getBundle(EkekoSnippetsTest.class);
	}

	@BeforeClass
	public static void ensureTestCasesExist() throws Exception {
		EkekoTestHelper.ensureProjectImported(myBundle, "/resources/TestCases/", "Ekeko-JDT");
	}

	@Test
	public void testPluginID() {
		assertEquals(EkekoSnippetsPlugin.PLUGIN_ID, "damp.ekeko.snippets.plugin");		
	}
	
	@Test 
	public void testEkekoSnippetsSuite() {
		//EkekoTestHelper.testClojureNamespace(myBundle, "test.damp.ekeko");
	}

	@Test 
	public void testRequireEkekoSnippets() {
		ClojureOSGi.require(myBundle, "damp.ekeko.snippets");
	}
	

}
