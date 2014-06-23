$JAVA_HOME/bin/keytool -genkeypair -storepass changeit -keyalg RSA -sigalg SHA1withRSA -alias server -keystore keystore_jira.ks
$JAVA_HOME/bin/keytool -genkeypair -storepass changeit -keyalg RSA -sigalg SHA1withRSA -alias server -keystore keystore_pubflow.ks

$JAVA_HOME/bin/keytool -export -storepass changeit -alias server -keystore keystore_jira.ks -file jira_cert.cer
$JAVA_HOME/bin/keytool -export -storepass changeit -alias server -keystore keystore_pubflow.ks -file pubflow_cert.cer

$JAVA_HOME/bin/keytool -import -storepass changeit -alias client -keystore truststore_pubflow.ks -file jira_cert.cer
$JAVA_HOME/bin/keytool -import -storepass changeit -alias client -keystore truststore_jira.ks -file pubflow_cert.cer

rm *.cer
