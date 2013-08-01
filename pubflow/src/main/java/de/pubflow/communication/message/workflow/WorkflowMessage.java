package de.pubflow.communication.message.workflow;

import de.pubflow.common.entity.Parameter;
import de.pubflow.common.entity.StringSerializable;
import de.pubflow.common.entity.Timestamp;
import de.pubflow.common.entity.User;
import de.pubflow.common.entity.WFParamList;
import de.pubflow.common.entity.workflow.PubFlow;
import de.pubflow.common.enumerartion.UserRole;
import de.pubflow.common.enumerartion.WFState;
import de.pubflow.common.enumerartion.WFType;
import de.pubflow.communication.message.Message;

public class WorkflowMessage extends Message {

	protected long 			dataset 				= -1;
	protected String 		workflowID 				= "empty";
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
	

	// -----------------------------------
	// Implementation of abstract methods
	// -----------------------------------
	
	@Override
	public String transformToString() {
		StringBuffer msgBody=new StringBuffer();
		msgBody.append(""+dataset);
		msgBody.append(fieldSeperatorSeq);
		msgBody.append(""+workflowID);
		msgBody.append(fieldSeperatorSeq);
		msgBody.append(wftype.toString());
		msgBody.append(fieldSeperatorSeq);
		msgBody.append(wfstate.toString());
		msgBody.append(fieldSeperatorSeq);
		msgBody.append(wfparams.toString());
		msgBody.append(fieldSeperatorSeq);
		msgBody.append(user.transformToString());
		msgBody.append(fieldSeperatorSeq);
		msgBody.append(userAuthenticated);
		msgBody.append(fieldSeperatorSeq);
		msgBody.append(wfparams.toString());
		msgBody.append(fieldSeperatorSeq);
		
		String serial = clazz + coreSeperatorSeq + msgBody.toString();
		
		return serial;
	}

	@Override
	public void initFromString(String content) {
		// TODO Auto-generated method stub

	}
	

	@Override
	public boolean isValid() {
		// TODO Auto-generated method stub
		return true;
	}

	
	// -----------------------------------
	// Getters & Setters
	// -----------------------------------

	/**
	 * @return the dataset
	 */
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
	public synchronized String getWorkflowID() {
		return workflowID;
	}

	/**
	 * @param workflowID the workflowID to set
	 */
	public synchronized void setWorkflowID(String workflowID) {
		this.workflowID = workflowID;
	}

	/**
	 * @return the wftype
	 */
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
	public synchronized long getWfEngine() {
		return wfEngine;
	}

	/**
	 * @param wfEngine the wfEngine to set
	 */
	public synchronized void setWfEngine(long wfEngine) {
		this.wfEngine = wfEngine;
	}

}
