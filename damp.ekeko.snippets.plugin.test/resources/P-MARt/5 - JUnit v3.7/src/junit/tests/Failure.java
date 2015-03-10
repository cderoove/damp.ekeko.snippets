package junit.tests;

import junit.framework.TestCase;

/**
 * A test case testing the testing framework.
 *
 */
public class Failure extends TestCase {
	
	public Failure(String name) {
		super(name);
	}
	public void test() {
		fail();
	}
}