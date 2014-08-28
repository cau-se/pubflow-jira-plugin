#!/bin/sh

#export JAVA_HOME=/home/arl/sunjdk7
export MAVEN_OPTS="-XX:+CMSClassUnloadingEnabled -XX:+CMSPermGenSweepingEnabled -XX:MaxPermSize=512M -Xmx2048M"

mvn clean compile exec:java
