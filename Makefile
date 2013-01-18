#
# Makefile for Foxtel Maven projects
# January 2013
#


# Set up maven binary, also an alias for skipTests.

notest=false
MAVEN_PARALLELISM=4

ifneq ($(notest), false)
	MVN=mvn3 -T$(MAVEN_PARALLELISM) -DskipTests
else
	MVN=mvn3 -T$(MAVEN_PARALLELISM)
endif

all: install

#
# Targets
#
RSYNC=rsync --progress




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
