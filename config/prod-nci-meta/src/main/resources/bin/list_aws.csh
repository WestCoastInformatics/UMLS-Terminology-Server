#!/bin/tcsh -f
#
# This script is used to push inversion source folders or release packages to s3
#

set rootdir = `dirname $0`
set abs_rootdir = `cd $rootdir && pwd`
set usage = 'push_s3.csh {inv|mr} {source_name|release_date}'

echo "--------------------------------------------------------"
echo "Starting `/bin/date`"
echo "--------------------------------------------------------"


aws ec2 describe-instances \
--query "Reservations[*].Instances[*].{InstanceId:InstanceId,PublicIP:PublicIpAddress,Type:InstanceType,Name:Tags[?Key=='Name']|[0].Value,Status:State.Name}"  \
--filters "Name=instance-state-name,Values=running" "Name=tag:Name,Values='*'"  \
--output table

aws rds describe-db-instances --query "DBInstances[*].{DBInstance:DBInstanceIdentifier,Type:DBInstanceClass}" --output table

aws rds describe-db-snapshots --query "DBSnapshots[*].{DBSnapshot:DBInstanceIdentifier,CreateTime:"InstanceCreateTime"}" \
             --output table

echo ""

echo "-----------------------------------------------------"
echo "Finished $0 ... `/bin/date`"
echo "-----------------------------------------------------"
