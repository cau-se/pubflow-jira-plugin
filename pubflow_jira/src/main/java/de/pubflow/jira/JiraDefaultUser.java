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

import java.math.BigInteger;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.exception.GroupNotFoundException;
import com.atlassian.crowd.exception.OperationFailedException;
import com.atlassian.crowd.exception.OperationNotPermittedException;
import com.atlassian.crowd.exception.UserNotFoundException;
import com.atlassian.crowd.exception.embedded.InvalidGroupException;
import com.atlassian.jira.exception.AddException;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.exception.PermissionException;
import com.atlassian.jira.user.ApplicationUser;

import de.pubflow.jira.accessors.JiraObjectCreator;
import de.pubflow.jira.accessors.JiraObjectManipulator;
import de.pubflow.jira.accessors.JiraObjectRemover;

/**
 * Contains all default users that are initialized at the first Jira startup.
 * Used to avoid publication of passwords to Github.
 * 
 * @author Marc Adolf
 *
 */
public class JiraDefaultUser {
	/**
	 * The data manager group in Jira
	 */
	private final Group groupDataManager;
	/**
	 * The scientists group in Jira
	 */
	private final Group groupScientists;
	/**
	 * The librarian group in Jira
	 */
	private final Group groupLibrarian;
	/**
	 * The data manager notification group in Jira
	 */
	private final Group groupDataManagersFn;
	/**
	 * The librarian notification group in Jira
	 */
	private final Group groupLibrariansFn;
	/**
	 * The software users group in Jira
	 */
	private final String jirasoftwareusers = "jira-software-users";

	/**
	 * Pubflow uses different user groups and a special pubflow user. This class creates this groups.
	 * 
	 * @throws OperationNotPermittedException
	 * @throws InvalidGroupException
	 */
	
	public JiraDefaultUser() throws OperationNotPermittedException, InvalidGroupException {

		groupDataManager = JiraObjectCreator.createGroup("datamanagers");
		groupScientists = JiraObjectCreator.createGroup("scientists");
		groupLibrarian = JiraObjectCreator.createGroup("librarian");
		groupLibrariansFn = JiraObjectCreator.createGroup("librarian-notification");
		groupDataManagersFn = JiraObjectCreator.createGroup("scientists-notification");

	}

	/**
	 * Inits default users and returns the root user for further actions.
	 * 
	 * @param project
	 * @param projectKey
	 * @throws PermissionException
	 * @throws CreateException
	 * @throws AddException
	 * @throws OperationNotPermittedException
	 * @throws InvalidGroupException
	 * @throws GroupNotFoundException
	 * @throws UserNotFoundException
	 * @throws OperationFailedException
	 */
	public ApplicationUser addDefaultUser()
			throws PermissionException, CreateException, AddException, OperationNotPermittedException,
			InvalidGroupException, GroupNotFoundException, UserNotFoundException, OperationFailedException {

		//
		final ApplicationUser userPubFlow = JiraObjectCreator.createUser("PubFlow",
				new BigInteger(130, JiraManagerPlugin.SECURERANDOM).toString(32));
		JiraObjectManipulator.addUserToGroup(userPubFlow, "jira-administrators");
		JiraObjectManipulator.addUserToGroup(userPubFlow, "jira-core-users");
		JiraObjectManipulator.addUserToGroup(userPubFlow, jirasoftwareusers);
		JiraObjectManipulator.addUserToGroup(userPubFlow, groupScientists);
		JiraObjectManipulator.addUserToGroup(userPubFlow, groupDataManager);
		JiraObjectManipulator.addUserToGroup(userPubFlow, groupLibrarian);

		final ApplicationUser userLibrarianFn = JiraObjectCreator.createUser("Librarian - Functional Account",
				new BigInteger(130, JiraManagerPlugin.SECURERANDOM).toString(32));
		JiraObjectManipulator.addUserToGroup(userLibrarianFn, groupLibrariansFn);

		final ApplicationUser userDatamanagerFn = JiraObjectCreator.createUser("DataManager - Functional Account",
				new BigInteger(130, JiraManagerPlugin.SECURERANDOM).toString(32));
		JiraObjectManipulator.addUserToGroup(userDatamanagerFn, groupDataManagersFn);

		// this.createTestUsers();

		this.createTestUsers();

		return userPubFlow;
		// log.debug("initPubfowProject: created users and usergroups for
		// PubFlow");

	}

	/**
	 * 
	 * @throws GroupNotFoundException
	 * @throws UserNotFoundException
	 * @throws PermissionException
	 * @throws AddException
	 * @throws OperationNotPermittedException
	 * @throws OperationFailedException
	 * @throws CreateException
	 */
	private void createTestUsers() throws GroupNotFoundException, UserNotFoundException, PermissionException,
			AddException, OperationNotPermittedException, OperationFailedException, CreateException {
		final ApplicationUser userRoot = JiraObjectCreator.createUser("root", "$Boogie3");
		JiraObjectManipulator.addUserToGroup(userRoot, groupDataManager);
		JiraObjectManipulator.addUserToGroup(userRoot, groupScientists);
		JiraObjectManipulator.addUserToGroup(userRoot, "jira-administrators");
		JiraObjectManipulator.addUserToGroup(userRoot, groupLibrarian);

		// The Group "jira-developers" does not exist in Jira 7.x.x. Use
		// "jira-core-users"
		// The Group "jira-users" does not exist in Jira 7.x.x. Use
		// "jira-software-users"
		JiraObjectManipulator.addUserToGroup(userRoot, "jira-core-users");
		JiraObjectManipulator.addUserToGroup(userRoot, "jira-software-users");
		JiraObjectRemover.deleteUser(userRoot, "admin");
		//
		final ApplicationUser userDataManager = JiraObjectCreator.createUser("SampleDataManager", "ilovedata");
		JiraObjectManipulator.addUserToGroup(userDataManager, groupDataManager);
		JiraObjectManipulator.addUserToGroup(userDataManager, groupScientists);
		JiraObjectManipulator.addUserToGroup(userDataManager, "jira-core-users");
		JiraObjectManipulator.addUserToGroup(userDataManager, jirasoftwareusers);

		final ApplicationUser userScientist = JiraObjectCreator.createUser("SampleScientist", "sciencerulez");
		JiraObjectManipulator.addUserToGroup(userScientist, groupScientists);
		JiraObjectManipulator.addUserToGroup(userScientist, jirasoftwareusers);
	}
}
