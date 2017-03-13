/**
 * Copyright (C) 2016 Marc Adolf, Arnd Plumhoff (http://www.pubflow.uni-kiel.de/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.pubflow;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test suite for PubFlow.
 */

@SuppressWarnings("PMD.TestClassWithoutTestCases")
public class PubFlowCoreTest extends TestCase {
	
	/**
	 * 
	 * @return
	 */
	public static Test suite() {
		final TestSuite mySuite = new TestSuite("PubFlow Testsuite");
		// Add your tests here
		// >>>
		// mySuite.addTestSuite( MsgBus_Test.class ); TODO fix me

		// <<<
		return mySuite;
	}
}
