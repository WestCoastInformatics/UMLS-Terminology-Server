#!/bin/tcsh -f
#
# This script is used to resizean ec2 instance.  The instance will be stopped and  restarted after the resize is complete.
#

setenv usage 'resize_ec2.csh {nciws-d2391-c|ncias-q3009-c|ncias-q3004-c}  {t3.nano|t3.large|t3.xlarge}'
setenv TEST_INSTANCE ncias-q3009-c
setenv DEV_INSTANCE nciws-d2391-c
setenv RELEASE_INSTANCE ncias-q3004-c

echo "--------------------------------------------------------"
echo "Starting `/bin/date`"
echo "--------------------------------------------------------"

if ($#argv == 2) then
    setenv INSTANCE_NAME $1
	setenv INSTANCE_TYPE $2
else
    echo "ERROR: Wrong number of parameters"
    echo "usage: $usage"
    exit 1
endif

echo "INSTANCE_NAME:    $INSTANCE_NAME"
echo "INSTANCE_TYPE:    $INSTANCE_TYPE"

if ($INSTANCE_NAME != $DEV_INSTANCE && $INSTANCE_NAME != $TEST_INSTANCE && $INSTANCE_NAME != $RELEASE_INSTANCE) then    
	echo "ERROR: INSTANCE must be $DEV_INSTANCE or $TEST_INSTANCE or $RELEASE_INSTANCE";
    exit 1
endif
#if ($INSTANCE_TYPE != 't3.nano' && $INSTANCE_TYPE != 't3.large' && $INSTANCE_TYPE != 't3.xlarge' && $INSTANCE_TYPE != 't3.2xlarge') then    
#	echo "ERROR: INSTANCE TYPE must be t3.nano, t3.large, t3.xlarge or t3.2xlarge";
#    exit 1
#endif

set instanceId = `aws ec2 describe-instances --profile meme --query "Reservations[*].Instances[*].{InstanceId:InstanceId,PublicIP:PublicIpAddress,Type:InstanceType,Name:Tags[?Key=='Name']|[0].Value,Status:State.Name}" --filters "Name=instance-state-name,Values=running" "Name=tag:Name,Values='$INSTANCE_NAME'" | jq -r '.[0][0].InstanceId'`
echo "instanceId:	$instanceId"

aws ec2 stop-instances --profile meme --instance-ids $instanceId  --output table
set stopped = null
while ($stopped == null)
   echo "stopping"
   set stopped = `aws ec2 describe-instances --profile meme --query "Reservations[*].Instances[*].{InstanceId:InstanceId,PublicIP:PublicIpAddress,Type:InstanceType,Name:Tags[?Key=='Name']|[0].Value,Status:State.Name}" --filters "Name=instance-state-name,Values=stopped" "Name=tag:Name,Values='$INSTANCE_NAME'" | jq '.[0][0].InstanceId'`
end
echo "stop completed"

aws ec2 modify-instance-attribute --profile meme --instance-id $instanceId --instance-type $INSTANCE_TYPE --output table

echo "resize requested to $INSTANCE_TYPE"
echo ""

aws ec2 start-instances --profile meme --instance-ids $instanceId --output table
set started = null
while ($started == null)
   echo "starting"
   set started = `aws ec2 describe-instances --profile meme --query "Reservations[*].Instances[*].{InstanceId:InstanceId,PublicIP:PublicIpAddress,Type:InstanceType,Name:Tags[?Key=='Name']|[0].Value,Status:State.Name}" --filters "Name=instance-state-name,Values=running" "Name=tag:Name,Values='$INSTANCE_NAME'" | jq '.[0][0].InstanceId'`
end
echo "running - start completed"

aws ec2 describe-instances --profile meme\
--query "Reservations[*].Instances[*].{InstanceId:InstanceId,PublicIP:PublicIpAddress,Type:InstanceType,Name:Tags[?Key=='Name']|[0].Value,Status:State.Name}"  \
--filters "Name=instance-state-name,Values=running" "Name=tag:Name,Values='$INSTANCE_NAME'"  \
--output table

echo "-----------------------------------------------------"
echo "Finished $0 ... `/bin/date`"
echo "-----------------------------------------------------"
