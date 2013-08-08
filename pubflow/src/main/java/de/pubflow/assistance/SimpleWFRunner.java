package de.pubflow.assistance;

import org.drools.KnowledgeBase;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.io.ResourceFactory;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.process.ProcessInstance;
import org.drools.runtime.process.WorkflowProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SimpleWFRunner {

	private static SimpleWFRunner instance;
	
	private volatile KnowledgeBuilder knowledgeBuilder;
	private volatile KnowledgeBase knowledgeBase;
	private static Logger logger;
	
	private SimpleWFRunner()
	{
		logger = LoggerFactory.getLogger(SimpleWFRunner.class);
		knowledgeBuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
		//knowledgeBuilder.add(ResourceFactory.newClassPathResource("processes/MonteCarlo.bpmn"), ResourceType.BPMN2);
		knowledgeBuilder.add(ResourceFactory.newClassPathResource("OCN.bpmn"), ResourceType.BPMN2);
		knowledgeBase = knowledgeBuilder.newKnowledgeBase();
	}

	public static SimpleWFRunner getInstance()
	{
		if(instance==null)
		{
			instance=new SimpleWFRunner();
		}
		return instance;
	}
	
	public synchronized StatefulKnowledgeSession initWFSession()
	{
		return knowledgeBase.newStatefulKnowledgeSession();
	}
	
	public synchronized ProcessInstance startWF(String wf_ID, StatefulKnowledgeSession p_Session)
	{
		 return p_Session.startProcess(wf_ID);
		
	}
	
	public static void main(String[] args) {
		
		StatefulKnowledgeSession session = SimpleWFRunner.getInstance().initWFSession();
		session.setGlobal("legID", 3);
		ProcessInstance instance = SimpleWFRunner.getInstance().startWF("de.pubflow.OCN", session);
		//float result = (Float) ((WorkflowProcessInstance) instance).getVariable("pi");
		//System.out.println("Approximation for PI: "+result);
	}
}
