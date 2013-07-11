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

package de.pubflow.repository.configurationRepository;

import de.pubflow.repository.abstractRepository.interaction.BasicProvider;
import de.pubflow.repository.abstractRepository.repository.ERepositoryName;
import de.pubflow.repository.abstractRepository.storageAdapter.DBStorageAdapter;
import de.pubflow.shared.entity.ConfigurationEntity;

public class ConfigurationProvider extends BasicProvider<ConfigurationEntity>{

	private static ConfigurationProvider cp;
	
	public ConfigurationProvider() {
		super(ERepositoryName.CONFIGURATION, new DBStorageAdapter());
	}
	
	public static ConfigurationProvider getProvider(){
		if(cp == null){
			cp = new ConfigurationProvider();
		}
		
		return cp;
	}
}
