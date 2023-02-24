#!/bin/tcsh -f
#
# This script is used to shut down an ec2 instance.  The instance can later be restarted using the wake_ec2.csh script.
#

setenv usage 'sleep_ec2.csh {test|release} '
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

set instanceId = `aws ec2 describe-instances --query "Reservations[*].Instances[*].{InstanceId:InstanceId,PublicIP:PublicIpAddress,Type:InstanceType,Name:Tags[?Key=='Name']|[0].Value,Status:State.Name}" --filters "Name=instance-state-name,Values=running" "Name=tag:Name,Values='$INSTANCE_NAME'" | jq -r '.[0][0].InstanceId'`
echo "instanceId:	$instanceId"

aws ec2 stop-instances --instance-ids $instanceId --output table

set stopped = null
while ($stopped == null)
   echo "stopping"
   set stopped = `aws ec2 describe-instances --query "Reservations[*].Instances[*].{InstanceId:InstanceId,PublicIP:PublicIpAddress,Type:InstanceType,Name:Tags[?Key=='Name']|[0].Value,Status:State.Name}" --filters "Name=instance-state-name,Values=stopped" "Name=tag:Name,Values='$INSTANCE_NAME'" | jq '.[0][0].InstanceId'`
end
echo "stop completed"

echo ""

aws ec2 describe-instances \
--query "Reservations[*].Instances[*].{InstanceId:InstanceId,PublicIP:PublicIpAddress,Type:InstanceType,Name:Tags[?Key=='Name']|[0].Value,Status:State.Name}"  \
--filters "Name=instance-state-name,Values=stopped" "Name=tag:Name,Values='$INSTANCE_NAME'"  \
--output table

echo "-----------------------------------------------------"
echo "Finished $0 ... `/bin/date`"
echo "-----------------------------------------------------"
