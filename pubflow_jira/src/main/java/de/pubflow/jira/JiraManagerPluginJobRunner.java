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
package de.pubflow.jira;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import org.ofbiz.core.entity.GenericEntityException;

import com.atlassian.scheduler.JobRunner;
import com.atlassian.scheduler.JobRunnerRequest;
import com.atlassian.scheduler.JobRunnerResponse;

public class JiraManagerPluginJobRunner implements JobRunner {

	public JiraManagerPluginJobRunner() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public JobRunnerResponse runJob(final JobRunnerRequest request) {

		try {
			return JobRunnerResponse.success(initPubflow());
		} catch (Exception e) {
			return JobRunnerResponse.failed(e);
		}
	}

	private String initPubflow() throws KeyManagementException, UnrecoverableKeyException, GenericEntityException,
			NoSuchAlgorithmException, KeyStoreException, CertificateException, IOException {
		final JiraManagerInitializer jiraManagerInitializer = new JiraManagerInitializer();
		jiraManagerInitializer.initPubFlowProject();
		return "Initilizing pubflow.";
	}
}