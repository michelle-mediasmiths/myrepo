#!/bin/bash --

mkdir /tmp/bundle
rm /tmp/bundle/*

cp ../fx-PlaceholderAgent/target/fx-PlaceholderAgent.war /tmp/bundle
cp ../fx-MediaPickupAgent/target/fx-MediaPickupAgent.war /tmp/bundle
cp ../fx-TcAdapter/target/fx-TcAdapter.war /tmp/bundle
cp ../fx-qcAdapter/target/fx-qcAdapter.war /tmp/bundle
cp ../fx-WFAdapter/target/fx-WFAdapter.war /tmp/bundle
cp ../fx-FSAdapter/target/fx-FSAdapter.war /tmp/bundle
cp ../fx-MqClient/target/fx-MqClient.war /tmp/bundle
cp ../fx-event-ui/target/fx-event-ui.war /tmp/bundle
cp ../fx-report-ui/target/fx-report-ui.war /tmp/bundle
cp ../fx-MailAgent/target/fx-MailAgent.war /tmp/bundle
cp ../fx-MayamPDP/target/fx-MayamPDP.war /tmp/bundle/

mkdir /tmp/bundle/config
rm -r /tmp/bundle/config/*

cp ../configuration/*.properties /tmp/bundle/config
cp -r ../configuration/services /tmp/bundle/config

mkdir /tmp/bundle/muleapps
rm -r /tmp/bundle/muleapps/*

cp ../muleapps/mamWFSProxies/bin/mamWFSProxies.zip /tmp/bundle/muleapps
cp ../muleapps/wfs-intalio/bin/wfs-intalio.zip /tmp/bundle/muleapps

