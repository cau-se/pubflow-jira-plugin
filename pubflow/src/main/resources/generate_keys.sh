keytool -genkey -alias server -keystore keystore_jira.ks
keytool -genkey -alias server -keystore keystore_pubflow.ks

keytool -export -alias server -keystore keystore_jira.ks -file jira_cert.cer
keytool -export -alias server -keystore keystore_pubflow.ks -file pubflow_cert.cer

keytool -import -alias client -keystore truststore_pubflow.ks -file jira_cert.cer
keytool -import -alias client -keystore truststore_jira.ks -file pubflow_cert.cer

rm *.cer
