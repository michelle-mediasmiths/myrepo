#!/bin/bash --

target='root@foxtel:/opt/tomcat/webapps/'

#scp ../fx-PlaceholderAgent/target/fx-PlaceholderAgent.war "${target}"
#scp ../fx-MediaPickupAgent/target/fx-MediaPickupAgent.war "${target}"
scp ../fx-TcAdapter/target/fx-TcAdapter.war "${target}"
scp ../fx-qcAdapter/target/fx-qcAdapter.war "${target}"
scp ../fx-WFAdapter/target/fx-WFAdapter.war "${target}"
scp ../fx-FSAdapter/target/fx-FSAdapter.war "${target}"



