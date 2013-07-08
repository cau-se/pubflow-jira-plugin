package de.cau.tf.ifi.se.pubflow.workflow.engine.jbpm;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.drools.KnowledgeBase;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.io.ResourceFactory;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.process.ProcessInstance;

import de.cau.tf.ifi.se.pubflow.common.entity.workflow.JBPMPubflow;
import de.cau.tf.ifi.se.pubflow.common.entity.workflow.PubFlow;
import de.cau.tf.ifi.se.pubflow.common.entity.workflow.WFParameter;
import de.cau.tf.ifi.se.pubflow.common.enumerartion.WFType;
import de.cau.tf.ifi.se.pubflow.common.exception.WFException;
import de.cau.tf.ifi.se.pubflow.common.exception.WFOperationNotSupported;
import de.cau.tf.ifi.se.pubflow.workflow.engine.IWorkflowEngine;

public class JBPMEngine implements IWorkflowEngine {

	private static JBPMEngine instance;
	private Hashtable<Long, JBPMPubflow> processTable;
	private static final String inputChannel = "activemq:JBPMworkflow.input";

	private JBPMEngine() {
		processTable = new Hashtable<Long, JBPMPubflow>();
	}

	public static JBPMEngine getInstance() {
		if (instance == null) {
			instance = new JBPMEngine();
		}
		return instance;
	}
	

	@Override
	public long deployWF(PubFlow wf) throws WFException {
		if (!(wf instanceof JBPMPubflow))
		{
			throw new WFException();
		}
		long tempID = isProcessInProcessTable(wf);
		if(tempID==-1)
		{
			tempID = getNextFreeID();
			processTable.put(tempID, (JBPMPubflow)wf);
		}
		return tempID;
	}

	@Override
	public void startWF(long wfID, ArrayList<WFParameter> params) throws WFException {
		
		JBPMPubflow wf = processTable.get(wfID);
		
		try {
			KnowledgeBase knowledgeBase = createKnowledgeBase(wf);
			//TODO PARAMS!!!
			runWF(knowledgeBase, wf.getWFID());
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public void undeployWF(long wfID) throws WFException {
		throw new WFOperationNotSupported();
	}

	@Override
	public void stopWF(long wfID) throws WFException {
		throw new WFOperationNotSupported();
	}
	
	/**
	 * Checks, if a given PubFlow is already listed in the process table.
	 * If the process is listed the Pubflow id (the key in the processtable IT IS NOT the workflow id) is returned, otherwise -1 is returned
	 * @param wf (PubFlow) : the workflow
	 * @return (long) : the id
	 */
	private long isProcessInProcessTable(PubFlow wf)
	{
		Set<Entry<Long, JBPMPubflow>> entryset = processTable.entrySet();
		for (Entry<Long, JBPMPubflow> entry : entryset) {
			PubFlow localPubFlow = entry.getValue();
			boolean isEqual = localPubFlow.equals(wf);
			if(isEqual)
			{
				return entry.getKey();
			}
		}
		return -1;
	}
	
	/**
	 * Returns the next free id for the process table
	 * 
	 * @return (long) : the id
	 */
	private long getNextFreeID()
	{
		long maxID = 0;
		Set<Long> entryset = processTable.keySet();
		for (Long idValue : entryset) {
			if(idValue>=maxID)
			{
				maxID = idValue+1;
			}
		}
		return maxID;
	}

	/**
	 * Loads a process (processType BPMN2.0!) from the given location in a new knowledgeBase and returns
	 * the knowledgebase
	 * 
	 * @param processFile (String) : the absolute filename
	 * @return (KnowledgeBase) : the KnowledgeBase
	 * @throws Exception
	 */
	private static KnowledgeBase createKnowledgeBase(JBPMPubflow wf) throws Exception {
		KnowledgeBuilder kbuilder = KnowledgeBuilderFactory
				.newKnowledgeBuilder();
		kbuilder.add(ResourceFactory.newByteArrayResource(wf.getWFAsByteArray()),
				ResourceType.BPMN2);
		return kbuilder.newKnowledgeBase();
	}
	
	/**
	 * Starts a given process in its knowledge base env and returns the process instance
	 * 
	 * @param kbase (KnowledgeBase) : the knowledge base the process was added to
	 * @param processID (String) : the id of the process (The one defined in the process file - NOT the PubFlow ID)
	 * @return (ProcessInstance) : the instance of the running workflow
	 * @throws Exception
	 */
	private ProcessInstance runWF(KnowledgeBase kbase, String processID) throws Exception
	{
		StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();
		return ksession.startProcess(processID);
	}

	@Override
	public List<WFType> getCompatibleWFTypes() {
		List<WFType> result = new ArrayList<WFType>();
		result.add(WFType.BPMN2);
		return result;
	}

}
