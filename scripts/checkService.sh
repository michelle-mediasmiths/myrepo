#/bin/bash --

#check direct connections to carbon wfs and ceritalk
curl -v http://10.111.227.21:8731/Rhozet.JobManager.JMServices/SOAP
curl -v http://10.111.227.41:80/protected/ListJobs.do

#check mule esb proxied connections to carbon wfs and ceritalk
curl -v http://10.111.224.101:8087/Rhozet.JobManager.JMServices/SOAP
curl -v http://10.111.224.101:8086/protected/ListJobs.do

#call ping endpoints of adapters (doesnt include placehodler mangement or mediapickup agents!)
#direct
curl -v http://10.111.224.101:8080/fx-TCAdapter/tc/ping
curl -v http://10.111.224.101:8080/fx-qcAdapter/rest/qc/ping
curl -v http://10.111.224.101:8080/fx-WFAdapter/rest/wf/ping
curl -v http://10.111.224.101:8080/fx-FSAdapter/rest/fs/ping

#via mule esb
curl -v http://10.111.224.101:8082/fx-TCAdapter/tc/ping
curl -v http://10.111.224.101:8082/fx-qcAdapter/rest/qc/ping
curl -v http://10.111.224.101:8082/fx-WFAdapter/rest/wf/ping
curl -v http://10.111.224.101:8082/fx-FSAdapter/rest/fs/ping


