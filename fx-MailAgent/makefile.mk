MVN=mvn


emailservice: install
	scp target/*.war /usr/local/Backup-apache-tomcat-7/webapps/mailagent.war



#
#
# Standard Maven targets
#
#

compile:
	$(MVN) clean compile

dependencies:
	$(MVN) dependency:tree

package:
	$(MVN) clean package

install:
	$(MVN) clean install

eclipse:
	$(MVN) eclipse:clean eclipse:eclipse -DdownloadSources=true -DdownloadJavadocs=true

idea:
	$(MVN) idea:idea -DdownloadSources=true -DdownloadJavadocs=true

clean:
	$(MVN) clean

release:
	$(MVN) release:clean release:prepare -DautoVersionSubmodules=true
	$(MVN) release:perform
