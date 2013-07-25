package de.pubflow.common.repository;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import de.pubflow.common.enumerartion.WFType;
import de.pubflow.common.repository.context.ContextEntity;
import de.pubflow.common.repository.context.ContextProvider;
import de.pubflow.common.repository.workflow.WorkflowEntity;
import de.pubflow.common.repository.workflow.WorkflowProvider;

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
				
				long id = wp.setEntry(we);
				
				WorkflowEntity we1 = wp.getEntry(id);
				
				assertTrue(we1.getPubFlowWFID() == 1l);
				assertTrue(we1.getType().equals(WFType.BPMN2));
				assertTrue(we1.getWorkflowName().equals("Test"));
				assertTrue(Arrays.equals(we1.getgBpmn(), sampleBytes));
				assertTrue(we1.getWFID().equals("wfID"));
	}

	@Test
	public void dbRepo() {	

				ContextProvider cp = ContextProvider.getInstance();
				
				assertTrue(cp!=null);
				
				ContextEntity ce = new ContextEntity();
				
				ce.setServiceUrl("test");
				
				Map<String, String> parameters = new HashMap<String, String>();
				
				parameters.put("1", "2");
				parameters.put("3", "4");
				parameters.put("5", "6");
				
				ce.setParameters(parameters);
				
				assertTrue(parameters == ce.getParameters());
				
				long id = cp.setEntry(ce);
				ce = null;
				
				assertTrue(ce==null);
				
				ce = cp.getEntry(id);
				
				assertTrue(ce.getServiceUrl() == "test");
				assertTrue(ce.getParameters() == parameters);

	}
}
