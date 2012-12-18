#!/bin/bash --

mkdir /tmp/bundle
rm /tmp/bundle/*

cp ../fx-PlaceholderAgent/target/fx-PlaceholderAgent.war /tmp/bundle
cp ../fx-MediaPickupAgent/target/fx-MediaPickupAgent.war /tmp/bundle
cp ../fx-TcAdapter/target/fx-TcAdapter.war /tmp/bundle
cp ../fx-qcAdapter/target/fx-qcAdapter.war /tmp/bundle
cp ../fx-WFAdapter/target/fx-WFAdapter.war /tmp/bundle
cp ../fx-FSAdapter/target/fx-FSAdapter.war /tmp/bundle
cp ../fx-MqClient/target/fx-MqClient-3.0-SNAPSHOT.war /tmp/bundle/fx-MqClient.war


cd /tmp/bundle
tar -cvzf mediasmiths-wfe-bundle.tar.gz *



