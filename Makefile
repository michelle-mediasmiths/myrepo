#
# Makefile for Foxtel Maven projects
# January 2013
#


# Set up maven binary, also an alias for skipTests.

notest=false
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
