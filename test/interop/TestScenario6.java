/*
 * Copyright  2003-2004 The Apache Software Foundation.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package interop;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.ws.axis.oasis.Scenario6;

/**
 * WS-Security Test Case
 * <p/>
 * 
 * @author Davanum Srinivas (dims@yahoo.com)
 */
public class TestScenario6 extends TestCase {
    /**
     * TestScenario1 constructor
     * <p/>
     * 
     * @param name name of the test
     */
    public TestScenario6(String name) {
        super(name);
    }

    /**
     * JUnit suite
     * <p/>
     * 
     * @return a junit test suite
     */
    public static Test suite() {
        return new TestSuite(TestScenario6.class);
    }

    /**
     * Main method
     * <p/>
     * 
     * @param args command line args
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public void testScenario6() throws Exception {
        Scenario6.main(new String[]{"-lhttp://localhost:8080/axis/services/Ping6"});
    }
}
