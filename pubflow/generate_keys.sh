keytool -genkey -alias webservice0 -keystore jira_keystore.ks
keytool -genkey -alias webservice1 -keystore pubflow_keystore.ks

keytool -export -alias webservice0 -keystore jira_keystore.ks -file jira_keystore.cer
keytool -export -alias webservice1 -keystore pubflow_keystore.ks -file pubflow_keystore.cer

keytool -import -alias webservice0 -keystore pubflow_keystore.ks -file jira_keystore.cer
keytool -import -alias webservice1 -keystore jira_keystore.ks -file pubflow_keystore.cer
