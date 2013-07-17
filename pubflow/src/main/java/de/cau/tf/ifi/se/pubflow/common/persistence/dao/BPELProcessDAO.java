/***************************************************************************
 * Copyright 2012 by
 *  + Christian-Albrechts-University of Kiel
 *    + Department of Computer Science
 *      + Software Engineering Group 
 *  and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ***************************************************************************/
/**
 * @author arl
 *
 */

package de.cau.tf.ifi.se.pubflow.common.persistence.dao;

import java.util.List;

import org.hibernate.Hibernate;

import de.cau.tf.ifi.se.pubflow.common.entity.BPELProcess;
import de.cau.tf.ifi.se.pubflow.common.entity.Parameter;
import de.cau.tf.ifi.se.pubflow.common.entity.ProcessLog;
import de.cau.tf.ifi.se.pubflow.common.persistence.PersistenceProvider;


public class BPELProcessDAO extends BasicDAO<BPELProcess>{

	PersistenceProvider p = new PersistenceProvider();

	public BPELProcessDAO() {
		super(BPELProcess.class, "BPELProcess");
	}

	public List<ProcessLog> getLogs(BPELProcess o){
		logger.info("getLogs");

		ProcessLogDAO processLogDAO = new ProcessLogDAO();
		List<ProcessLog> logs = processLogDAO.execQuery("select l from ProcessLog l where l.myProcess.id = " + o.getId());
		return logs;
	}

	public List<Parameter> getParameters(BPELProcess o){
		logger.info("getParameters");

		ParameterDAO parameterDAO = new ParameterDAO();
		List<Parameter> params = parameterDAO.execQuery("select l from Parameter l where l.myProcess.id = " + o.getId());
		return params;
	}

	public List<BPELProcess> getDeployed(boolean deployed){
		logger.info("getDeployed");

		List<BPELProcess> deployedProcesses = execQuery("select p from BPELProcess p where p.deployed = " + deployed);
		return deployedProcesses;
	}

	public BPELProcess getByPortName(String portName) throws Exception{
		logger.info("getByPortName");

		List<BPELProcess> deployedProcesses = execQuery("select p from BPELProcess p where p.portName = '" + portName + "'");

		if(deployedProcesses.size() == 1){
			return deployedProcesses.get(0);
		}else{
			throw new Exception("No Element found with portName = " + portName);
		}
	}
	

	public BPELProcess getByPortNameEager(String portName) throws Exception{
		logger.info("getByPortNameEager");

		List<BPELProcess> deployedProcesses = execQuery("select p from BPELProcess p where p.portName = '" + portName + "'");

		if(deployedProcesses.size() == 1){
			BPELProcess process = deployedProcesses.get(0);

			p.startTransaction();
			process = p.getObject(BPELProcess.class, process.getId());
			Hibernate.initialize(process.getLogs());
			Hibernate.initialize(process.getParameters());
			Hibernate.initialize(process.getZippedProcessFile());
			p.stopTransaction();

			return process;

		}else{
			throw new Exception("No Element found with portName = " + portName);
		}
	}

	public List<BPELProcess> getDeployedEager(boolean deployed) throws Exception{
		logger.info("getDeployedEager : " + deployed);
		
		List<BPELProcess> processes = execQuery("select p from BPELProcess p where p.deployed = " + deployed);
		
		p.startTransaction();
		
		for(int i = 0; i < processes.size(); i++){
			
			BPELProcess process = p.getObject(BPELProcess.class, processes.get(i).getId());
			processes.set(i, process);
			Hibernate.initialize(process.getLogs());
			Hibernate.initialize(process.getParameters());
			Hibernate.initialize(process.getZippedProcessFile());
		}

		p.stopTransaction();

		return processes;
	}
}