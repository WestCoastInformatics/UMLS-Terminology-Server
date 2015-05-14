#!/bin/sh

# Args -
# start downloads the file to here
# finish removes the file from here

maintFile=maintain.html
stageDir=/home/ec2-tomcat/config
maintDir=/opt/maint

echo "Action = $1 "
if [ $1 = "start" ];
then /bin/cp -f $stageDir/$maintFile $maintDir
fi

if [ $1 = "stop" ];
then rm -f $maintDir/$maintFile
fi