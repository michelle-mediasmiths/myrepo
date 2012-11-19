#!/bin/bash --

target='/opt/tomcat/webapps/'

cp ../fx-PlaceholderAgent/target/fx-PlaceholderAgent.war "${target}"
cp ../fx-MediaPickupAgent/target/fx-MediaPickupAgent.war "${target}"
cp ../fx-TcAdapter/target/fx-TcAdapter.war "${target}"
cp ../fx-qcAdapter/target/fx-qcAdapter.war "${target}"
cp ../fx-WFAdapter/target/fx-WFAdapter.war "${target}"
cp ../fx-FSAdapter/target/fx-FSAdapter.war "${target}"


