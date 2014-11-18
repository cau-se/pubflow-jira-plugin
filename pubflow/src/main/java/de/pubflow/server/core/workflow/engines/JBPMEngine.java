package de.pubflow.server.core.workflow.engines;

import java.util.ArrayList;
import java.util.List;

import org.drools.KnowledgeBase;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.io.ResourceFactory;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.process.ProcessInstance;
import org.jfree.util.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.pubflow.server.common.entity.workflow.JBPMPubflow;
import de.pubflow.server.common.entity.workflow.ParameterType;
import de.pubflow.server.common.entity.workflow.PubFlow;
import de.pubflow.server.common.entity.workflow.WFParameter;
import de.pubflow.server.common.entity.workflow.WFParameterList;
import de.pubflow.server.common.enumeration.WFType;
import de.pubflow.server.common.exceptions.WFException;
import de.pubflow.server.common.exceptions.WFOperationNotSupported;
import de.pubflow.server.core.workflow.WorkflowEngine;

public class JBPMEngine extends WorkflowEngine {

	private JBPMPubflow myWF;
	WFParameterList parameter;
	static Logger myLogger;
	private ProcessInstance processInstance = null;
	private KnowledgeBase kbase= null;


	/**
	 * @return the myWF
	 */
	public synchronized PubFlow getMyWF() {
		return myWF;
	}



	/**
	 * @param myWF the myWF to set
	 */
	public synchronized void setMyWF(PubFlow myWF) {
		if (!(myWF instanceof JBPMPubflow))
		{
			myLogger.error("Wrong workflow type!");
			return;
		}
		this.myWF = (JBPMPubflow)myWF;
	}



	/**
	 * @return the parameter
	 */
	public synchronized WFParameterList getParameter() {
		return parameter;
	}



	/**
	 * @param parameter the parameter to set
	 */
	public synchronized void setParameter(WFParameterList parameter) {
		this.parameter = parameter;
	}



	/**
	 * @return the getProcessInstance
	 */
	public synchronized ProcessInstance getProcessInstance() {
		return processInstance;
	}



	/**
	 * @param getProcessInstance the getProcessInstance to set
	 */
	public synchronized void setProcessInstance(
			ProcessInstance getProcessInstance) {
		this.processInstance = getProcessInstance;
	}

	static{
		myLogger = LoggerFactory.getLogger(JBPMEngine.class);
	}


	public JBPMEngine() {


	}
	public JBPMEngine(JBPMPubflow wf) {

		myWF = wf;
	}



	@Override
	public void deployWF(PubFlow wf) throws WFException {
		myWF = (JBPMPubflow)wf;
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
	 * Loads a process (processType BPMN2.0!) from the given location in a new knowledgeBase and returns
	 * the knowledgebase
	 * 
	 * @param processFile (String) : the absolute filename
	 * @return (KnowledgeBase) : the KnowledgeBase
	 * @throws Exception
	 */
	private void createKnowledgeBase(JBPMPubflow wf) throws Exception {
		myLogger.info("Trying to add WF to knowledgebase");
		KnowledgeBuilder kbuilder = null;
		try{
			kbuilder = KnowledgeBuilderFactory
					.newKnowledgeBuilder();
			kbuilder.add(ResourceFactory.newByteArrayResource(wf.getWfDef()),
					ResourceType.BPMN2);
			myLogger.info("Knowledgebase created");
		}

		catch (Exception e)
		{
			myLogger.error("Couldn't create knowledgebase");
			e.printStackTrace();
		}
		kbase = kbuilder.newKnowledgeBase();
	}

	/**
	 * Starts a given process in its knowledge base env and returns the process instance
	 * 
	 * @param kbase (KnowledgeBase) : the knowledge base the process was added to
	 * @param processID (String) : the id of the process (The one defined in the process file - NOT the PubFlow ID)
	 * @return (ProcessInstance) : the instance of the running workflow
	 * @throws Exception
	 */
	private void runWF() throws Exception
	{
		myLogger.info("Trying to start workflow: "+myWF.getWFID());
		WFParameterList params = parameter;
		ProcessInstance instance = null;
		try{
			myLogger.info("Creating Knowledgebase ...");
			StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();
			myLogger.info("Setting process parameter");
			for(WFParameter wfParam : params.getParameterList()){

				//set the parameter name to lower case, remove all spaces and the workflow appendix 
				String key = wfParam.getKey().replace(" ", "").toLowerCase();

				if(key.contains("_")){
					key = key.substring(0, (key.indexOf("_")));
				}

				ParameterType payloadClazz = wfParam.getPayloadClazz();

				try{
					
				switch (payloadClazz) {
				case INTEGER:
					int valueI = ((Integer)wfParam.getValue()).intValue();
					myLogger.info("Setting parameter >>"+key+"<< to >>"+valueI+"<<");
					ksession.setGlobal(key, valueI);
					break;
				case STRING:
					String valueS = (String) wfParam.getValue();
					myLogger.info("Setting parameter >>"+key+"<< to >>"+valueS+"<<");
					ksession.setGlobal(key, valueS);
					break;
				case DOUBLE:
					double valueD = ((Double)wfParam.getValue()).doubleValue();
					myLogger.info("Setting parameter >>"+key+"<< to >>"+valueD+"<<");
					ksession.setGlobal(key, valueD);
					break;
				case LONG:
					long valueL = ((Long)wfParam.getValue()).longValue();
					myLogger.info("Setting parameter >>"+key+"<< to >>"+valueL+"<<");
					ksession.setGlobal(key, valueL);
					break;
				default:
					break;
				}
				}catch(RuntimeException e){
					e.printStackTrace();
					Log.error(e.getMessage());
				}
			}
			
			myLogger.info("Starting process : " + myWF.getWFID());

			instance = ksession.startProcess(myWF.getWFID());

			myLogger.info("Workflow executed sucessfuly");
		}
		catch (Exception ex){
			myLogger.error("Couldn't start workflow");
			ex.printStackTrace();
		}
		processInstance = instance;
	}

	@Override
	public List<WFType> getCompatibleWFTypes() {
		List<WFType> result = new ArrayList<WFType>();
		result.add(WFType.BPMN2);
		return result;
	}

	@Override
	public void run() {
		try {
			myLogger.info("Starting ...");
			createKnowledgeBase((JBPMPubflow)myWF);
			runWF();
			myLogger.info("Success!");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}

	@Override
	public void setParams(WFParameterList params) throws WFException {
		parameter = params;

	}


}
