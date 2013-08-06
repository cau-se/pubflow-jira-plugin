package de.pubflow.communication.message.workflow;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import de.pubflow.common.entity.Timestamp;
import de.pubflow.common.entity.User;
import de.pubflow.common.entity.workflow.WFParamList;
import de.pubflow.common.enumerartion.UserRole;
import de.pubflow.common.enumerartion.WFState;
import de.pubflow.common.enumerartion.WFType;
import de.pubflow.communication.message.Message;

@XmlRootElement(namespace = "http://pubflow.de/message/workflow")
public class WorkflowMessage extends Message {

	protected long 			dataset 				= -1;
	protected long 			workflowID 				= -1;
	protected WFType 		wftype 					= null;
	protected WFState 		wfstate 				= null;
	protected WFParamList 	wfparams 				= null;
	protected User 			user 					= null;
	protected boolean 		userAuthenticated 		= false;
	protected UserRole 		userRole 				= null;
	protected User 			datamanager 			= null;
	protected boolean 		datamanagerApproved 	= false;
	protected Timestamp 	msgCreatedTimestamp 	= null;
	protected Timestamp 	wfStartedTimestamp 		= null;
	protected Timestamp 	wfEndedTimestamp 		= null;
	protected Timestamp 	userResponseTimestamp 	= null;
	protected String 		comments 				= "empty";
	protected long 			pubflowVersion			= -1;
	protected long 			wfEngine				= -1;

//	public static WorkflowMessage getTestMsg()
//	{
//		WorkflowMessage ret = new WorkflowMessage();
//		
//		
//		
//		return ret;
//	}
//

//	@Override
//	public boolean isValid() {
//		// TODO Auto-generated method stub
//		return true;
//	}
//
//	
	// -----------------------------------
	// Getters & Setters
	// -----------------------------------

	/**
	 * @return the dataset
	 */
	@XmlElement(name="DataSet")
	public synchronized long getDataset() {
		return dataset;
	}

	/**
	 * @param dataset the dataset to set
	 */
	public synchronized void setDataset(long dataset) {
		this.dataset = dataset;
	}

	/**
	 * @return the workflowID
	 */
	@XmlElement(name="WorkflowRef")
	public synchronized long getWorkflowID() {
		return workflowID;
	}

	/**
	 * @param workflowID the workflowID to set
	 */
	public synchronized void setWorkflowID(long workflowID) {
		this.workflowID = workflowID;
	}

	/**
	 * @return the wftype
	 */
	@XmlElement(name="WorkFlowType")
	public synchronized WFType getWftype() {
		return wftype;
	}

	/**
	 * @param wftype the wftype to set
	 */
	public synchronized void setWftype(WFType wftype) {
		this.wftype = wftype;
	}

	/**
	 * @return the wfstate
	 */
	@XmlElement(name="WorkflowState")
	public synchronized WFState getWfstate() {
		return wfstate;
	}

	/**
	 * @param wfstate the wfstate to set
	 */
	public synchronized void setWfstate(WFState wfstate) {
		this.wfstate = wfstate;
	}

	/**
	 * @return the wfparams
	 */
	@XmlElement(name="Parameterlist")
	public synchronized WFParamList getWfparams() {
		return wfparams;
	}

	/**
	 * @param wfparams the wfparams to set
	 */
	public synchronized void setWfparams(WFParamList wfparams) {
		this.wfparams = wfparams;
	}

	/**
	 * @return the user
	 */
	@XmlElement(name="User")
	public synchronized User getUser() {
		return user;
	}

	/**
	 * @param user the user to set
	 */
	public synchronized void setUser(User user) {
		this.user = user;
	}

	/**
	 * @return the userAuthenticated
	 */
	@XmlElement(name="UserAuthenticated")
	public synchronized boolean isUserAuthenticated() {
		return userAuthenticated;
	}

	/**
	 * @param userAuthenticated the userAuthenticated to set
	 */
	public synchronized void setUserAuthenticated(boolean userAuthenticated) {
		this.userAuthenticated = userAuthenticated;
	}

	/**
	 * @return the userRole
	 */
	@XmlElement(name="UserRole")
	public synchronized UserRole getUserRole() {
		return userRole;
	}

	/**
	 * @param userRole the userRole to set
	 */
	public synchronized void setUserRole(UserRole userRole) {
		this.userRole = userRole;
	}

	/**
	 * @return the datamanager
	 */
	@XmlElement(name="Datamanager")
	public synchronized User getDatamanager() {
		return datamanager;
	}

	/**
	 * @param datamanager the datamanager to set
	 */
	public synchronized void setDatamanager(User datamanager) {
		this.datamanager = datamanager;
	}

	/**
	 * @return the datamanagerApproved
	 */
	@XmlElement(name="DatamanagerApproved")
	public synchronized boolean isDatamanagerApproved() {
		return datamanagerApproved;
	}

	/**
	 * @param datamanagerApproved the datamanagerApproved to set
	 */
	public synchronized void setDatamanagerApproved(boolean datamanagerApproved) {
		this.datamanagerApproved = datamanagerApproved;
	}

	/**
	 * @return the msgCreatedTimestamp
	 */
	@XmlElement(name="MsgCreatedAt")
	public synchronized Timestamp getMsgCreatedTimestamp() {
		return msgCreatedTimestamp;
	}

	/**
	 * @param msgCreatedTimestamp the msgCreatedTimestamp to set
	 */
	public synchronized void setMsgCreatedTimestamp(Timestamp msgCreatedTimestamp) {
		this.msgCreatedTimestamp = msgCreatedTimestamp;
	}

	/**
	 * @return the wfStartedTimestamp
	 */
	@XmlElement(name="WFStartedAt")
	public synchronized Timestamp getWfStartedTimestamp() {
		return wfStartedTimestamp;
	}

	/**
	 * @param wfStartedTimestamp the wfStartedTimestamp to set
	 */
	public synchronized void setWfStartedTimestamp(Timestamp wfStartedTimestamp) {
		this.wfStartedTimestamp = wfStartedTimestamp;
	}

	/**
	 * @return the wfEndedTimestamp
	 */
	@XmlElement(name="WFEndedAt")
	public synchronized Timestamp getWfEndedTimestamp() {
		return wfEndedTimestamp;
	}

	/**
	 * @param wfEndedTimestamp the wfEndedTimestamp to set
	 */
	public synchronized void setWfEndedTimestamp(Timestamp wfEndedTimestamp) {
		this.wfEndedTimestamp = wfEndedTimestamp;
	}

	/**
	 * @return the userResponseTimestamp
	 */
	@XmlElement(name="UserResponseSendAt")
	public synchronized Timestamp getUserResponseTimestamp() {
		return userResponseTimestamp;
	}

	/**
	 * @param userResponseTimestamp the userResponseTimestamp to set
	 */
	public synchronized void setUserResponseTimestamp(
			Timestamp userResponseTimestamp) {
		this.userResponseTimestamp = userResponseTimestamp;
	}

	/**
	 * @return the comments
	 */
	@XmlElement(name="Comments")
	public synchronized String getComments() {
		return comments;
	}

	/**
	 * @param comments the comments to set
	 */
	public synchronized void setComments(String comments) {
		this.comments = comments;
	}

	/**
	 * @return the pubflowVersion
	 */
	@XmlElement(name="PubFlowVersion")
	public synchronized long getPubflowVersion() {
		return pubflowVersion;
	}

	/**
	 * @param pubflowVersion the pubflowVersion to set
	 */
	public synchronized void setPubflowVersion(long pubflowVersion) {
		this.pubflowVersion = pubflowVersion;
	}

	/**
	 * @return the wfEngine
	 */
	@XmlElement(name="WFEngineRef")
	public synchronized long getWfEngine() {
		return wfEngine;
	}

	/**
	 * @param wfEngine the wfEngine to set
	 */
	public synchronized void setWfEngine(long wfEngine) {
		this.wfEngine = wfEngine;
	}

	@Override
	public boolean isValid() {
		// TODO Auto-generated method stub
		return false;
	}


}
