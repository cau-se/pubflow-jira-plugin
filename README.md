# PubFlow-Jira-Plugin
This projects contains the application parts of the [DFG] (http://www.dfg.de/) research project [PubFlow] (http://www.pubflow.uni-kiel.de/en).
All presented software is part of a prototype project and may contain bugs and many things to improve.
E.g. Improvements on a central config file oder service discorvery are possible.
The main part of the user interaction in [PubFlow] (http://www.pubflow.uni-kiel.de/en) is written as a Jira plugin.
The different issue types or workflows use the workflows given by the [Workflow Provider](https://github.com/PubFlow/Workflow-Provider)

To start the plugin the [config](https://github.com/PubFlow/PubFlow-Config) in the [Proploader](https://github.com/PubFlow/PubFlow-Jira-Plugin/blob/master/common/src/main/java/de/pubflow/common/PropLoader.java)
has to be set accordingly.

## Setup Pubflow
[PubFlow] (http://www.pubflow.uni-kiel.de/en) is split into two packages,Pubflow-Jira-Plugin and [Workflow Provider](https://github.com/PubFlow/Workflow-Provider). Both packages need to be started separate from each other. 

### Preliminaries
To run the stand-alone version of the PubFlow-Jira-Plugin you need to install the [Atlassian SDK](https://developer.atlassian.com/docs/getting-started/set-up-the-atlassian-plugin-sdk-and-build-a-project).

### Start Pubflow-Jira-Plugin stand-alone (Unix)

```bash
    cd path/to/pubflow/
    atlas-package
    cd /pubflow_jira/
    atlas-run
```

#### Restart Pubflow-Jira-Plugin
If you run the stand-alone version of PubFlow-Jira-Plugin you can restart the plugin by opening a new tab in your terminal and enter
```bash
    cd path/to/pubflow_jira/
    atlas-package
```
### Start Workflow-Service
After you have starte PubFlow-Jira-Plugin you have to run the [Workflow Provider](https://github.com/PubFlow/Workflow-Provider).
```bash
    cd path/to/workflow-service
    mvn spring-boot:run
```
