#!/bin/bash --

scp ../fx-PlaceholderAgent/target/fx-PlaceholderAgent.war root@foxtel:/opt/jboss/standalone/deployments/
scp ../fx-MediaPickupAgent/target/fx-MediaPickupAgent.war root@foxtel:/opt/jboss/standalone/deployments/
scp ../fx-TcAdapter/target/fx-TcAdapter.war  root@foxtel:/opt/jboss/standalone/deployments/
scp ../fx-qcAdapter/target/fx-qcAdapter.war root@foxtel:/opt/jboss/standalone/deployments/
scp ../fx-WFAdapter/target/fx-WFAdapter.war root@foxtel:/opt/jboss/standalone/deployments/

