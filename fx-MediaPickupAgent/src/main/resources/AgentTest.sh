#!/bin/bash          
incomingFolder="root@foxtel.local.mediasmithsforge.com:/tmp/foxtel/material/incoming"
outputFolder="root@foxtel.local.mediasmithsforge.com:/tmp/foxtel/material/failed"
#Measured in seconds
waitTime=5

#Transfer all files created in test folder to the specified incoming folder
scp  /tmp/mediaTestData/incomingBackup/*.xml $incomingFolder

#Go to sleep
sleep $waitTime

ssh root@foxtel.local.mediasmithsforge.com 'if [ -f /tmp/foxtel/material/failed/XmlFile1.xml ]; then echo "XmlFile1 exists, test passed!"; else echo "XmlFile1 does not exist, test failed!"; fi'

ssh root@foxtel.local.mediasmithsforge.com 'if [ -f /tmp/foxtel/material/failed/XmlFile2.xml ]; then echo "XmlFile2 exists, test passed!"; else echo "XmlFile2 does not exist, test failed!"; fi'
