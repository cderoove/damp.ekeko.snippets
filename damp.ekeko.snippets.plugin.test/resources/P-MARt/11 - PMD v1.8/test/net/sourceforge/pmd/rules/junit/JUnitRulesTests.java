/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
*/
package test.net.sourceforge.pmd.rules.junit;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * tests for the net.sourceforge.pmd.rules.junit package
 *
 * @author Boris Gruschko ( boris at gruschko.org )
 * @version $Id: JUnitRulesTests.java,v 1.1 2004/07/14 16:37:20 ngjanice Exp $
 */
public class JUnitRulesTests
{
  /**
   * test suite
   *
   * @return test suite
   */
  public static Test suite(  )
  {
    TestSuite suite =
      new TestSuite( "Test for test.net.sourceforge.pmd.rules.junit" );

    //$JUnit-BEGIN$
    suite.addTestSuite( JUnitAssertionsShouldIncludeMessageRuleTest.class );
    suite.addTestSuite( JUnitSpellingRuleTest.class );
    suite.addTestSuite( JUnitStaticSuiteRuleTest.class );

    //$JUnit-END$
    return suite;
  }
}


/*
 * $Log: JUnitRulesTests.java,v $
 * Revision 1.1  2004/07/14 16:37:20  ngjanice
 * 14 juillet 2004 - 12h32
 *
 * Revision 1.2  2003/11/20 16:53:10  tomcopeland
 * Changing over license headers in the source code, cleaned up a test
 *
 * Revision 1.1  2003/09/29 14:32:32  tomcopeland
 * Committed regression test suites, thanks to Boris Gruschko
 *
 */
