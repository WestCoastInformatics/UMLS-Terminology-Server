#!/bin/tcsh -f
#
# This script is used to reconstitue a rds db from its final snapshot.
# TODO: Check on status until available T
#

setenv usage 'wake_rds.csh {test|release} '
setenv TEST_DB memedb2
setenv DEV_DB memedb2
setenv RELEASE_DB memedb2

echo "--------------------------------------------------------"
echo "Starting `/bin/date`"
echo "--------------------------------------------------------"

if ($#argv == 1) then
    setenv DB_NAME $1
else
    echo "ERROR: Wrong number of parameters"
    echo "usage: $usage"
    exit 1
endif

echo "DB_NAME:    $DB_NAME"

if ($DB_NAME != $DEV_DB && $DB_NAME != $TEST_DB && $DB_NAME != $RELEASE_DB) then	
	echo "ERROR: DB must be $DEV_DB or $TEST_DB or $RELEASE_DB" 
	exit 1
endif

set snapshot_name = `aws rds describe-db-snapshots \
  --db-instance-identifier=$DB_NAME\
    --query="max_by(DBSnapshots, &SnapshotCreateTime).DBSnapshotArn" \
	  --output text | perl -ne '@_ = split/:/; print "$_[-1]"'`
echo "snapshot name:  $snapshot_name"

  aws rds restore-db-instance-from-db-snapshot \
      --db-instance-identifier $DB_NAME\
	  --db-snapshot-identifier $snapshot_name \
      --db-parameter-group-name memedb

echo ""

set started = null
while ($started != 'available')
   echo "restoring"
   set started = `aws rds describe-db-instances --query "DBInstances[?DBInstanceIdentifier=='$DB_NAME'].[DBInstanceStatus][0][0]" | jq -r`
end

echo "restore completed"

echo "-----------------------------------------------------"
echo "Finished $0 ... `/bin/date`"
echo "-----------------------------------------------------"
