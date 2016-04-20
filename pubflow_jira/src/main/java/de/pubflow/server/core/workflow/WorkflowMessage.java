package de.pubflow.server.core.workflow;

import java.io.IOException;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import de.pubflow.server.common.entity.workflow.WFParameter;
import de.pubflow.server.common.enumeration.WFState;
import de.pubflow.server.common.enumeration.WFType;
import de.pubflow.server.common.repository.abstractRepository.misc.IDPool;

@XmlRootElement(namespace = "http://pubflow.de/message/workflow")
public class WorkflowMessage implements Serializable{

	private static final long serialVersionUID = -4931074023209271264L;

	protected long instanceId;
	protected String workflowID = "";
	protected WFType type = null;
	protected WFState state = null;
	protected List<WFParameter> parameters = null;
	protected boolean userAuthenticated = false;
	//protected UserRole userRole = null;
	protected boolean datamanagerApproved = false;
	protected Timestamp msgCreatedTimestamp = null;
	protected Timestamp startedTimestamp = null;
	protected Timestamp endedTimestamp = null;
	protected Timestamp userResponseTimestamp = null;
	protected String comments = "empty";
	protected long pubflowVersion = -1;
	//protected long wfEngine = -1;


	public WorkflowMessage() throws IOException {
		instanceId = IDPool.getUniqueID();
	}

	public long getInstanceId() {
		return instanceId;
	}

	public void setInstanceId(long instanceId) {
		this.instanceId = instanceId;
	}

	/**
	 * @return the workflowID
	 */
	@XmlElement(name="WorkflowRef")
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
	@XmlElement(name="WorkFlowType")
	public synchronized WFType geType() {
		return type;
	}

	/**
	 * @param wftype the wftype to set
	 */
	public synchronized void setType(WFType wftype) {
		this.type = wftype;
	}

	/**
	 * @return the wfstate
	 */
	@XmlElement(name="WorkflowState")
	public synchronized WFState getState() {
		return state;
	}

	/**
	 * @param wfstate the wfstate to set
	 */
	public synchronized void setState(WFState wfstate) {
		this.state = wfstate;
	}

	/**
	 * @return the wfparams
	 */
	@XmlElement(name="Parameters")
	public synchronized List<WFParameter> getParameters() {
		return parameters;
	}

	/**
	 * @param parameters the wfparams to set
	 */
	public synchronized void setParameters(List<WFParameter> parameters) {
		this.parameters = parameters;
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
	public synchronized Timestamp getStartedTimestamp() {
		return startedTimestamp;
	}

	/**
	 * @param startedTimestamp the wfStartedTimestamp to set
	 */
	public synchronized void setStartedTimestamp(Timestamp startedTimestamp) {
		this.startedTimestamp = startedTimestamp;
	}

	/**
	 * @return the wfEndedTimestamp
	 */
	@XmlElement(name="WFEndedAt")
	public synchronized Timestamp getEndedTimestamp() {
		return endedTimestamp;
	}

	/**
	 * @param endedTimestamp the wfEndedTimestamp to set
	 */
	public synchronized void setEndedTimestamp(Timestamp endedTimestamp) {
		this.endedTimestamp = endedTimestamp;
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

	public boolean isValid() {
		// TODO Auto-generated method stub
		return true;
	}
}
