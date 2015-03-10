package test.net.sourceforge.pmd.stat;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * tests for the net.sourceforge.pmd.stat package
 *
 * @author Boris Gruschko ( boris at gruschko.org )
 * @version $Id: StatTests.java,v 1.1 2004/07/14 16:37:21 ngjanice Exp $
 */
public class StatTests
{
  /**
   * test suite
   *
   * @return test suite
   */
  public static Test suite(  )
  {
    TestSuite suite = new TestSuite( "Test for test.net.sourceforge.pmd.stat" );

    //$JUnit-BEGIN$
    suite.addTestSuite( MetricTest.class );
    suite.addTestSuite( StatisticalRuleTest.class );

    //$JUnit-END$
    return suite;
  }
}


/*
 * $Log: StatTests.java,v $
 * Revision 1.1  2004/07/14 16:37:21  ngjanice
 * 14 juillet 2004 - 12h32
 *
 * Revision 1.1  2003/09/29 14:32:32  tomcopeland
 * Committed regression test suites, thanks to Boris Gruschko
 *
 */
