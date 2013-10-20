#!/bin/bash --


mkdir /tmp/sourceBundle
rm -r /tmp/sourceBundle/*

cd ..
mvn clean

tar -czf /tmp/sourceBundle/source.tar.gz .

export MAVEN_OPTS="-Xmx512m -XX:MaxPermSize=128m"
mvn package dependency:copy-dependencies

cd /tmp/sourceBundle/lib
tar -czf /tmp/sourceBundle/dependencies.tar.gz .
cd ..
rm -R lib