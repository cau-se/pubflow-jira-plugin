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
package de.pubflow.common.repository;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;

import de.pubflow.server.common.entity.WorkflowEntity;
import de.pubflow.server.common.enumeration.WFType;
import de.pubflow.server.common.repository.WorkflowProvider;
public class RepositoryTest {

//	@Test
	public void fsRepo() {
				
				byte [] sampleBytes = {3,4,5,6,3,2,1};
		
				WorkflowProvider wp = WorkflowProvider.getInstance();
				
				WorkflowEntity we = new WorkflowEntity();
				
				we.setWorkflowId("de.pubflow.OCN");
				we.setType(WFType.BPMN2);
				we.setWorkflowName("Test");
				we.setgBpmn(sampleBytes);
				
				long id = wp.addEntry(we);
				
				WorkflowEntity we1 = wp.getEntry(id);
				
				assertTrue(we1!=null);

				assertTrue(we1.getWorkflowId().equals(("de.pubflow.OCN")));
				assertTrue(we1.getType().equals(WFType.BPMN2));
				assertTrue(we1.getWorkflowName().equals("Test"));
				assertTrue(Arrays.equals(we1.getgBpmn(), sampleBytes));				
				
				
	}

//	@Test
	public void dbRepo() {	
//		ServiceProvider sp = ServiceProvider.getInstance();
//		
//		ServiceEntity se = new ServiceEntity();
//		
//		se.setId(1);
//		se.setServiceDescription("lululu");
//		se.setServiceName("nonono");
//		
//		long id = sp.setEntry(se);
//		
//		ServiceEntity se1 = sp.getEntry(id);
//		
//
//		assertTrue(se1.getId());
		

//				ContextProvider cp = ContextProvider.getInstance();
//				
//				assertTrue(cp!=null);
//				
//				ContextEntity ce = new ContextEntity();	
//				ce.setServiceUrl("test");
//				
//				Map<String, String> parameters = new HashMap<String, String>();
//				
//				parameters.put("1", "2");
//				parameters.put("3", "4");
//				parameters.put("5", "6");
//				
//				ce.setParameters(parameters);
//								
//				long id = cp.addEntry(ce);
//				
//				ContextEntity ce1;
//								
//				ce1 = cp.getEntry(id);
//				
//				assertTrue(ce!=null);
//				
//				assertTrue(ce1.getServiceUrl().equals("test"));
//				assertTrue(ce1.getParameters().equals(parameters));
//				

	}
}
