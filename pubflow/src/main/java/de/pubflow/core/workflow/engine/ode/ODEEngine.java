package de.pubflow.core.workflow.engine.ode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import net.scherp.tf.application.WorkflowExecutorResult;
import net.scherp.tf.application.pubflow.PubFlowWorkflowDeployer;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMText;
import org.apache.axiom.om.util.Base64;
import org.apache.axis2.AxisFault;
import org.apache.ode.axis2.service.ServiceClientUtil;

import de.pubflow.common.entity.repository.WorkflowEntity;
import de.pubflow.common.entity.workflow.BPELProcess;
import de.pubflow.common.entity.workflow.PubFlow;
import de.pubflow.common.entity.workflow.WFParamList;
import de.pubflow.common.entity.workflow.WFParameter;
import de.pubflow.common.enumerartion.WFType;
import de.pubflow.common.exception.WFException;
import de.pubflow.common.properties.PropLoader;
import de.pubflow.common.repository.workflow.WorkflowLocationInformation;
import de.pubflow.common.repository.workflow.WorkflowProvider;
import de.pubflow.core.server.AppServer;
import de.pubflow.core.workflow.engine.IWorkflowEngine;

public class ODEEngine{

//	static String odeUrl;
//	static 	OMFactory factory;
//	static ServiceClientUtil serviceClient;
//	static String port;
//
//	static{
//		factory = OMAbstractFactory.getOMFactory();
//		serviceClient = new ServiceClientUtil();
//		port = PropLoader.getInstance().getProperty("port", ODEEngine.class.toString(), AppServer.DEFAULT_PORT);
//		odeUrl = "http://localhost:" + port + "/ode/processes/DeploymentService";
//	}
//
//	private static OMElement sendToODE(OMElement msg) throws AxisFault {
//		return serviceClient.send(msg, odeUrl);
//	}
//
//	@Override
//	public long deployWF(PubFlow wf) throws WFException {
//		try{
//
//			//TODO type transformation to PubFlow??
//			BPELProcess bpel = null;
//
//			// create elements for deployment
//			OMNamespace pmapi = factory.createOMNamespace("http://www.apache.org/ode/pmapi", "pmapi");
//			OMElement root = factory.createOMElement("deploy", pmapi); // qualified operation name
//			OMElement namePart = factory.createOMElement("name", null);
//			namePart.setText(bpel.getProcessName());
//			OMElement zipPart = factory.createOMElement("package", null);
//			OMElement zipElmt = factory.createOMElement("zip", null);
//
//			String base64Enc = Base64.encode(bpel.getZippedProcessFile());
//			OMText zipContent = factory.createOMText(base64Enc, "application/zip", true);
//			root.addChild(namePart);
//			root.addChild(zipPart);
//			zipPart.addChild(zipElmt);
//			zipElmt.addChild(zipContent);
//
//			// deploy
//			sendToODE(root); 
//
//			//TODO: RETURN 
//			//.getFirstElement().getFirstElement().getText();	
//
//			return 1l;
//
//		} catch (AxisFault e) {
//			e.printStackTrace();
//			throw new WFException();
//		}
//	}
//
//	@Override
//	public void startWF(long wfID, WFParamList params) throws WFException {
//		try {
//
//			WorkflowProvider wfp = WorkflowProvider.getInstance();
//			WorkflowEntity wfe = wfp.getEntry(wfID);
//			WorkflowLocationInformation wli;
//
//			wli = wfe.writeToTempFS();
//
//			String bpmnFile = wli.getBpmnFile();
//			String workingDir = wli.getWorkingDir();
//			String baseDir = wli.getBaseDir();
//			String processName = wfe.getWorkflowName();
//
//			System.out.println("XXXXXXXXXXXXXXXXXXXXXXX");
//			System.out.println(bpmnFile);
//			System.out.println(workingDir);
//			System.out.println(baseDir);
//			System.out.println(processName);
//			System.out.println(odeUrl);
//
//			PubFlowWorkflowDeployer deployer = new PubFlowWorkflowDeployer();
//
//
//			Properties props = new Properties();
//
//			for(WFParameter wfParam : params.getParameter()){
//				//props.setProperty(wfParam., value)
//			}
//
//			WorkflowExecutorResult deployResult = deployer.deploy(bpmnFile, workingDir, baseDir, odeUrl, processName, new Properties());
//
//			String deployedOdeProcessName = deployResult.getStatusCode();
//
//			//TODO persist data?
//			//			ContextProvider cp = ContextProvider.getProvider();
//			//			ContextEntity ce = new ContextEntity();
//			//			ce.setParameters(parameters);
//			//			ce.setServiceUrl(deployedOdeProcessName);	
//			//			cp.setEntry(ce);
//
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
//
//
//	@Override
//	public void undeployWF(long wfID) throws WFException {
//		OMNamespace pmapi = factory.createOMNamespace("http://www.apache.org/ode/pmapi", "pmapi");
//		OMElement root = factory.createOMElement("undeploy", pmapi);  // qualified operation name
//		OMElement part = factory.createOMElement("processName", pmapi);
//
//		//TODO: getOdeName -- wfID + # ? 
//		//part.setText(p.getOdeName() );
//		part.setText(wfID + "");
//		root.addChild(part);
//
//		// undeployment
//		try {
//			sendToODE(root);
//		} catch (AxisFault e) {
//			e.printStackTrace();
//			throw new WFException();
//		}
//	}
//
//	@Override
//	public void stopWF(long wfID) throws WFException {
//		//not supported yet
//	}
//
//	@Override
//	public List<WFType> getCompatibleWFTypes() {
//
//		//TODO: ??
//		return null;
//	}

}
