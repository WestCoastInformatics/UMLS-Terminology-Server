#!/bin/tcsh -f
#
# This script is used to start up an ec2 instance.  
#

setenv usage 'wake_ec2.csh {test|release} '
setenv TEST_INSTANCE meme-test
setenv DEV_INSTANCE meme-dev
setenv RELEASE_INSTANCE meme-release

echo "--------------------------------------------------------"
echo "Starting `/bin/date`"
echo "--------------------------------------------------------"

if ($#argv == 1) then
    setenv INSTANCE_NAME $1
else
    echo "ERROR: Wrong number of parameters"
    echo "usage: $usage"
    exit 1
endif

echo "INSTANCE_NAME:    $INSTANCE_NAME"

if ($INSTANCE_NAME != $DEV_INSTANCE && $INSTANCE_NAME != $TEST_INSTANCE && $INSTANCE_NAME != $RELEASE_INSTANCE) then    
	echo "ERROR: INSTANCE must be $DEV_INSTANCE or $TEST_INSTANCE or $RELEASE_INSTANCE";
    exit 1
endif

set instanceId = `aws ec2 describe-instances --query "Reservations[*].Instances[*].{InstanceId:InstanceId,PublicIP:PublicIpAddress,Type:InstanceType,Name:Tags[?Key=='Name']|[0].Value,Status:State.Name}" --filters "Name=instance-state-name,Values=stopped" "Name=tag:Name,Values='$INSTANCE_NAME'" | jq -r '.[0][0].InstanceId'`
echo "instanceId:	$instanceId"

aws ec2 start-instances --instance-ids $instanceId --output table
set started = null
while ($started == null)
   echo "starting"
   set started = `aws ec2 describe-instances --query "Reservations[*].Instances[*].{InstanceId:InstanceId,PublicIP:PublicIpAddress,Type:InstanceType,Name:Tags[?Key=='Name']|[0].Value,Status:State.Name}" --filters "Name=instance-state-name,Values=running" "Name=tag:Name,Values='$INSTANCE_NAME'" | jq '.[0][0].InstanceId'`
end
echo "running - start completed"


echo ""

aws ec2 describe-instances \
--query "Reservations[*].Instances[*].{InstanceId:InstanceId,PublicIP:PublicIpAddress,Type:InstanceType,Name:Tags[?Key=='Name']|[0].Value,Status:State.Name}"  \
--filters "Name=instance-state-name,Values=running" "Name=tag:Name,Values='$INSTANCE_NAME'"  \
--output table

echo "-----------------------------------------------------"
echo "Finished $0 ... `/bin/date`"
echo "-----------------------------------------------------"
