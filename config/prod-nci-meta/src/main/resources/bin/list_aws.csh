#!/bin/tcsh -f
#
# This script is used to list ec2 and rds instances
#

set rootdir = `dirname $0`
set abs_rootdir = `cd $rootdir && pwd`
set usage = 'list_aws.csh'

echo "--------------------------------------------------------"
echo "Starting `/bin/date`"
echo "--------------------------------------------------------"


aws ec2 describe-instances --profile meme\
--query "Reservations[*].Instances[*].{InstanceId:InstanceId,PublicIP:PublicIpAddress,Type:InstanceType,Name:Tags[?Key=='Name']|[0].Value,Status:State.Name}"  \
--filters "Name=instance-state-name,Values=running" "Name=tag:Name,Values='*'"  \
--output table

aws rds describe-db-instances --profile meme --query "DBInstances[*].{DBInstance:DBInstanceIdentifier,Type:DBInstanceClass,Status:DBInstanceStatus}" --output table

aws rds describe-db-instances --profile meme --query "DBInstances[*].{DBInstance:DBInstanceArn}" | jq -r '.[].DBInstance' > /tmp/rdsArns.txt


foreach i (`cat /tmp/rdsArns.txt`)
echo ""
echo $i
aws rds describe-pending-maintenance-actions --profile meme --resource-identifier $i --output table
end

aws rds describe-db-snapshots --profile meme --query "DBSnapshots[*].{DBSnapshot:DBInstanceIdentifier,CreateTime:"InstanceCreateTime"}" \
             --output table

echo ""

echo "-----------------------------------------------------"
echo "Finished $0 ... `/bin/date`"
echo "-----------------------------------------------------"
