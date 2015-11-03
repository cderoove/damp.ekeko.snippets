package test.damp.ekeko.snippets.experiments;

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

public class GeneticSearchTest {

	private static Bundle myBundle;
	private static Bundle ekekoBundle;


	static {
		myBundle = FrameworkUtil.getBundle(GeneticSearchTest.class);
		ekekoBundle = FrameworkUtil.getBundle(EkekoTestHelper.class);
	}

	
	public static File getResourceFile(String relativePath) throws Exception {
		return EkekoTestHelper.getFileFromBundle(myBundle, relativePath);
	}
	
	
//	@BeforeClass
//	public static void ensureTestCasesExist() throws Exception {
//		EkekoTestHelper.ensureProjectImported(myBundle, "/resources/P-MARt/", "6%20-%20JHotDraw v5.1");
//	}

	@Test 
	public void testExperiments() {
		EkekoTestHelper.testClojureNamespace(myBundle, "test.damp.ekeko.snippets.experiments.experiments");
	}
}