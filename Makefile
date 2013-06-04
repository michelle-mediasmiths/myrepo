#
# Makefile for Foxtel Maven projects
# January 2013
#


# Set up maven binary, also an alias for skipTests.

notest=true
MAVEN_PARALLELISM=4

ifneq ($(notest), false)
	MVN=mvn2 -DskipTests
else
	MVN=mvn2
endif

all: install



#
# pwright dev machine targets
#
pwcarbon: carbon
	~/Code/tomcat/bin/kill.sh
	rm -rf ~/Code/tomcat/webapps/tcadapter*
	rsync fx-TcAdapter/target/fx-TcAdapter.war ~/Code/tomcat/webapps/tcadapter.war
	~/Code/tomcat/bin/start.sh

#
# bmcleod targets
#
bmmqclient: mqclient
	rsync -v fx-MqClient/target/fx-MqClient.war sysadmin@10.111.224.101:mediasmiths/bundle/

bmpa : placeholderagent	
	rsync -v fx-PlaceholderAgent/target/fx-PlaceholderAgent.war sysadmin@10.111.224.101:mediasmiths/bundle/

bmwfa : wfadapter
	rsync -v fx-WFAdapter/target/fx-WFAdapter.war sysadmin@10.111.224.101:mediasmiths/bundle/

bmpdp : mayampdp
	rsync -v fx-MayamPDP/target/fx-MayamPDP.war sysadmin@10.111.224.101:mediasmiths/bundle/

bmpdptest : mayampdp
	rsync -v fx-MayamPDP/target/fx-MayamPDP.war sysadmin@10.111.224.101:mediasmiths/bundle/fx-MayamPDP-test.war

reportslocal : reports
	/opt/tomcat/bin/stop.sh
	cp fx-report-ui/target/fx-report-ui.war /opt/tomcat/webapps
	cp fx-event-ui/target/fx-event-ui.war /opt/tomcat/webapps
	rm -r /opt/tomcat/webapps/fx-report-ui
	rm -r /opt/tomcat/webapps/fx-event-ui
	/opt/tomcat/bin/start.sh
	
wfadapterlocal : wfadapter
	cp fx-WFAdapter/target/fx-WFAdapter.war /opt/tomcat/webapps

bmreports : reports
	rsync -v fx-report-ui/target/fx-report-ui.war root@192.168.2.22:/opt/tomcat/webapps
	rsync -v fx-event-ui/target/fx-event-ui.war root@192.168.2.22:/opt/tomcat/webapps
	
bmevents : events	
	rsync -v fx-event-ui/target/fx-event-ui.war root@192.168.2.22:/opt/tomcat/webapps

uploadall : install
	-mkdir /tmp/bundle/
	-rm /tmp/bundle/*.war
	cp fx-PlaceholderAgent/target/fx-PlaceholderAgent.war /tmp/bundle/
	cp fx-MediaPickupAgent/target/fx-MediaPickupAgent.war /tmp/bundle/
	cp fx-TcAdapter/target/fx-TcAdapter.war /tmp/bundle/
	cp fx-qcAdapter/target/fx-qcAdapter.war /tmp/bundle/
	cp fx-WFAdapter/target/fx-WFAdapter.war /tmp/bundle/
	cp fx-FSAdapter/target/fx-FSAdapter.war /tmp/bundle/
	cp fx-MqClient/target/fx-MqClient.war /tmp/bundle/
	cp fx-event-ui/target/fx-event-ui.war /tmp/bundle/
	cp fx-report-ui/target/fx-report-ui.war /tmp/bundle/
	cp fx-MailAgent/target/fx-MailAgent.war /tmp/bundle/
	cp fx-MayamPDP/target/fx-MayamPDP.war /tmp/bundle/
	rsync -v /tmp/bundle/*.war sysadmin@10.111.224.101:mediasmiths/bundle/

#
# Targets
#
RSYNC=rsync --progress

placeholderagent:
	$(MVN) clean package -am --projects fx-PlaceholderAgent

mqclient:
	$(MVN) clean package -am --projects fx-MqClient

carbon:
	$(MVN) clean package -am --projects fx-TcAdapter

wfadapter:
	$(MVN) clean package -am --projects fx-WFAdapter

mayampdp:
	$(MVN) clean package -am --projects fx-MayamPDP

reports:
	$(MVN) clean package -am --projects fx-report-ui,fx-event-ui

events:
	$(MVN) clean package -am --projects fx-event-ui
	
#
#
# Standard Maven targets
#
#

compile:
	$(MVN) clean compile

dependencies:
	$(MVN) clean dependency:tree

package:
	$(MVN) clean package

install:
	$(MVN) clean install

eclipse:
	$(MVN) eclipse:clean eclipse:eclipse -DdownloadSources=true -DdownloadJavadocs=true

clean:
	$(MVN) clean

release:
	$(MVN) clean release:clean release:prepare -DautoVersionSubmodules=true
	$(MVN) clean release:perform
