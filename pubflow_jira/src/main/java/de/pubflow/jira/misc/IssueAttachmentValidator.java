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
package de.pubflow.jira.misc;

import java.util.Map;

import com.atlassian.jira.issue.Issue;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.workflow.InvalidInputException;
import com.opensymphony.workflow.Validator;
import com.opensymphony.workflow.loader.DescriptorFactory;
import com.opensymphony.workflow.loader.ValidatorDescriptor;

public class IssueAttachmentValidator implements Validator {

  @SuppressWarnings ("unchecked")
  public static ValidatorDescriptor makeDescriptor(final String issueType)
  {
      final ValidatorDescriptor issueAttachmentValidator = DescriptorFactory.getFactory().createValidatorDescriptor();
      issueAttachmentValidator.setType("class");
      issueAttachmentValidator.getArgs().put("class.name", IssueAttachmentValidator.class.getName());
      issueAttachmentValidator.getArgs().put("issuetype", issueType);
      return issueAttachmentValidator;
  }

  @Override
  public void validate(final Map transientVars, final Map args, final PropertySet ps) throws InvalidInputException
  {
      hasAttachment(args, transientVars);
  }

  public boolean hasAttachment(final Map args, final Map transientVars)
          throws InvalidInputException {

      Issue issue = (Issue) transientVars.get("issue");

      // Check Issue permission
      if (issue.getAttachments().size() > 0) {
        return true;
      } else {
        throw new InvalidInputException("This issue has no attachment.");
      }
  } 
  
}