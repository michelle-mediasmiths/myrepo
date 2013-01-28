MVN=mvn


event: install
	rm -f /usr/local/apache-tomcat-7.0.29/webapps/event.war
	rm -rf /usr/local/apache-tomcat-7.0.29/webapps/event
	scp target/*.war /usr/local/apache-tomcat-7.0.29/webapps/event.war



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
