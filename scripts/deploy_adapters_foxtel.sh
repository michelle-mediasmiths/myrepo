#!/bin/bash --

target='sysadmin@10.111.224.101:/opt/ipwf/applications/apache-tomcat-7.0.32/webapps/'

scp ../fx-PlaceholderAgent/target/fx-PlaceholderAgent.war "${target}"
scp ../fx-MediaPickupAgent/target/fx-MediaPickupAgent.war "${target}"
scp ../fx-TcAdapter/target/fx-TcAdapter.war "${target}"
scp ../fx-qcAdapter/target/fx-qcAdapter.war "${target}"
scp ../fx-WFAdapter/target/fx-WFAdapter.war "${target}"
scp ../fx-FSAdapter/target/fx-FSAdapter.war "${target}"



