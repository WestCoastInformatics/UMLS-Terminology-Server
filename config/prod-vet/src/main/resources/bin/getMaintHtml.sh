#!/bin/sh

# Args -
# start downloads the file to here
# finish removes the file from here

maintFile=maintain.html
maintFile2=maintain.$2.html
stageDir=/home/ec2-tomcat/$2/config
maintDir=/opt/maint

echo "Action = $1 "
echo "Type = $2 "
if [ $1 = "start" ];
then /bin/cp -f $stageDir/$maintFile $maintDir/$maintFile2
fi

if [ $1 = "stop" ];
then rm -f $maintDir/$maintFile2
fi
