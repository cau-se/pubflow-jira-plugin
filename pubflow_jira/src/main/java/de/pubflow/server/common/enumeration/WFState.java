package de.pubflow.server.common.enumeration;

import java.io.Serializable;

public enum WFState implements Serializable{

	READY,
	STARTED,
	RUNNING,
	CANCELED,
	STOPPED,
	DELETED;
}