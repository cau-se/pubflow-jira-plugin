package de.pubflow.server.assistance;

import de.pubflow.server.common.repository.ScheduledWorkflowProvider;
import de.pubflow.server.common.repository.WorkflowProvider;

public class RepoWiper {
	public static void wipeRepos(){
		WorkflowProvider pro = WorkflowProvider.getInstance();
		ScheduledWorkflowProvider pro2 = ScheduledWorkflowProvider.getInstance(); 

		pro.clear();
		pro2.clear();
	}
	
	public static void main (String [] args){
		wipeRepos();
	}
}
