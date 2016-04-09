package de.pubflow;


import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
/**
 * Unit test suite for PubFlow.
 */

public class PubFlowCoreTest 
    extends TestCase
{
	public static Test suite()
	  {
	    TestSuite mySuite = new TestSuite( "PubFlow Testsuite" );
	    // Add your tests here
	    // >>>
	    // mySuite.addTestSuite( MsgBus_Test.class ); TODO fix me
	    
	    // <<<
	    return mySuite;
	  }
}
