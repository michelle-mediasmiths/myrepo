#!/bin/bash --

tcAdapterEndpoint="http://localhost:8080/fx-TcAdapter/rest/tc"

#id of package to be transcoded from source material
packageID=$1
ident=$2   

function getProfileForPackage(){
	local pID
	pID=$1;
	
	#TODO call lookup adapter\query db for profile to use
	echo "8b9cbc5a-2070-4e13-9936-e15c8682db8a";
}

function transferToTcInput(){
local pID
pID=$1

   #TODO make request to mayam\ardome api to transfer file to transcode input location and return the full (windows) filepath to the media
   echo "F:\\tcinput\\${pID}.mxf"
}

##############################################
#pick a transcode profile to use					 #
##############################################
tcProfile=$(getProfileForPackage $packageID)
echo "using profile $tcProfile "

##############################################
#move media to location available to transcoder  #
##############################################
pathOnTC=$(echo $(transferToTcInput $packageID) | xxd -plain | tr -d '\n' | sed 's/\(..\)/%\1/g')
echo "media path for transcoder is $pathOnTC "

outputPath=$(echo "F:\tcoutput" | xxd -plain | tr -d '\n' | sed 's/\(..\)/%\1/g')

##############################################
#start tc									 #	
##############################################

jobName=${packageID}_${ident}

response=$(curl -v -s -S -X PUT  "${tcAdapterEndpoint}/job/start?jobname=${jobName}&input=${pathOnTC}&output=${outputPath}&preset=${tcProfile}")
echo "response is ${response} "

##############################################
#poll status								 #	
##############################################

finished=false

while [[ "$finished" != "true" ]]; do

	echo "sleeping for 10 seconds"
	sleep 10

	finished=$(curl -s -S "${tcAdapterEndpoint}/job/${jobname}/finished")
	echo "finished == ${finished} "
		
done

