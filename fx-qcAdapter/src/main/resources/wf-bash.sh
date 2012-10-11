#!/bin/bash --

qcAdapterEndpoint="http://localhost:8080/fx-qcAdapter/rest/qc"

#media location being used
location=foxtel

function getProfileForMaterial(){
	local mID
	mID=$1;
	
	#TODO call lookup adapter\query db
	echo "FoxtelK2";
}

function transferToMediaLocation(){
   local mID
   mID=$1
   
   #TODO make request to mayam\ardome api to transfer file to media location and return the filename in media location
   echo "${mID}.mxf"
}

#id of material to be qc'd
materialID=$1
ident=$2


##############################################
#pick a qc profile to use					 #
##############################################
qcProfile=$(getProfileForMaterial $materialID)
echo "using profile $qcProfile "

##############################################
#move media to location available to cerify  #
##############################################
pathInMediaLocation=$(transferToMediaLocation $materialID)
echo "media path in MediaLocation is $pathInMediaLocation "

##############################################
#start qc									 #	
##############################################
payload="<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>
<qcStartRequest>
  <ident>${ident}</ident>
  <profile>${qcProfile}</profile>
  <file>${pathInMediaLocation}</file>
</qcStartRequest>" 
echo "sending payload ${payload}"

response=$(echo "$payload" | curl -s -S -X PUT -d @- -H "Content-Type: application/xml" "${qcAdapterEndpoint}/start")
echo "response is ${response} "

status=$(echo "$response" | xpath "//qcStartResponse/status/text()")
echo "status is $status "

case "$status" in
	ERROR)
		echo "QC Start failed"
		exit 1
		;;
	STARTED)
		echo "QC Started"
		;;
	*)
		echo "Unexpected status";
		exit 1
esac


##############################################
#poll status								 #	
##############################################
jobname=$(echo "$response" | xpath "//qcStartResponse/identifier/jobname/text()" 2> /dev/null)
echo "jobname is $jobname"

finished=false

while [[ "$finished" != "true" ]]; do

	echo "sleeping for 10 seconds"
	sleep 10

	finished=$(curl -s -S "${qcAdapterEndpoint}/job/${jobname}/finished")
	echo "finished == ${finished} "
		
done

##############################################
#fetch result								 #	
##############################################
response=$(curl -s -S "${qcAdapterEndpoint}/result/file?path=${pathInMediaLocation}")
echo "response is ${response} "

result=$(echo "$response" | xpath "//qcJobResult/result/text()"  2> /dev/null)

echo "result is ${result}"

if [[ "$result" == "success" ]]; then
	exit 0
else
	exit 1
fi
