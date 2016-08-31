/**
 * Copyright (C) 2016 Marc Adolf, Arnd Plumhoff (http://www.pubflow.uni-kiel.de/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.pubflow.server.common.persistence.dao;

import de.pubflow.server.common.persistence.entities.ObjectEntity;


public class RepositoryDAO extends BasicDAO<ObjectEntity>{

	public RepositoryDAO() {
		super(ObjectEntity.class, "ObjectEntity");
		// TODO Auto-generated constructor stub
	}

	public ObjectEntity getObject(long l){
		logger.info("Get");

		p.startTransaction();
		ObjectEntity result = p.<ObjectEntity>getObject(clazz, l);
		p.stopTransaction();
		return result;
	}

	public void remove(long l){
		logger.info("Remove");

		p.startTransaction();
		ObjectEntity o = p.getObject(clazz, l);
		p.remove(o);
		p.stopTransaction();
	}
}
