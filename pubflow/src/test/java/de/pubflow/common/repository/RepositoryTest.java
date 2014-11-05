package de.pubflow.common.repository;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import de.pubflow.server.common.entity.WorkflowEntity;
import de.pubflow.server.common.enumeration.WFType;
import de.pubflow.server.common.persistence.entities.ObjectEntity;
import de.pubflow.server.common.repository.WorkflowProvider;
public class RepositoryTest {

	@Test
	public void fsRepo() {
				
				byte [] sampleBytes = {3,4,5,6,3,2,1};
		
				WorkflowProvider wp = WorkflowProvider.getInstance();
				
				WorkflowEntity we = new WorkflowEntity();
				
				we.setPubFlowWFID(1);
				we.setType(WFType.BPMN2);
				we.setWorkflowName("Test");
				we.setgBpmn(sampleBytes);
				we.setWFID("wfID");
				
				long id = wp.addEntry(we);
				
				WorkflowEntity we1 = wp.getEntry(id);
				
				assertTrue(we1!=null);

				assertTrue(we1.getPubFlowWFID() == 1l);
				assertTrue(we1.getType().equals(WFType.BPMN2));
				assertTrue(we1.getWorkflowName().equals("Test"));
				assertTrue(Arrays.equals(we1.getgBpmn(), sampleBytes));
				assertTrue(we1.getWFID().equals("wfID"));
				
				
				
	}

	@Test
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
