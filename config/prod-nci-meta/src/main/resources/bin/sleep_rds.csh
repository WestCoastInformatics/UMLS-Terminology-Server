#!/bin/tcsh -f
#
# This script is used to delete a rds db but will create a final snapshot of the db before doing so.
#

setenv usage 'sleep_rds.csh {test|release} '
setenv TEST_DB meme-test
setenv DEV_DB meme-dev
setenv RELEASE_DB meme-release
set awspath = '/usr/local/bin'

echo "--------------------------------------------------------"
echo "Starting `/bin/date`"
echo "--------------------------------------------------------"

if ($#argv == 1) then
    setenv DB_NAME $1
	setenv SNAPSHOT_DATE `/bin/date  +"%Y%m%d"`
else
    echo "ERROR: Wrong number of parameters"
    echo "usage: $usage"
    exit 1
endif

echo "DB_NAME:    $DB_NAME"
echo "SNAPSHOT_DATE:    $SNAPSHOT_DATE"

if ($DB_NAME != $DEV_DB && $DB_NAME != $TEST_DB && $DB_NAME != $RELEASE_DB) then    
	echo "ERROR: DB must be $DEV_DB or $TEST_DB or $RELEASE_DB";
    exit 1
endif

set exists = `$awspath/aws rds describe-db-snapshots --profile meme --query "DBSnapshots[?DBSnapshotIdentifier=='$DB_NAME-$SNAPSHOT_DATE-final-snapshot'].[Status][0][0]" `
echo "exists: $exists"
if ($exists =~ 'available') then
	echo "ERROR: Snapshot $DB_NAME-$SNAPSHOT_DATE-final-snapshot has already been created today.  It must be removed manually if another snapshot is desired."
	exit 1
endif

$awspath/aws rds delete-db-instance \
    --profile meme --db-instance-identifier $DB_NAME\
	    --final-db-snapshot-identifier $DB_NAME-$SNAPSHOT_DATE-final-snapshot

echo ""

set started = 'deleting' 
while ($started =~ 'deleting')
   echo "deleting"
   set started = `awspath/aws rds describe-db-instances --profile meme --query "DBInstances[?DBInstanceIdentifier=='$DB_NAME'].[DBInstanceStatus][0][0]" `
end

echo "deleting done - creating snapshot done"

echo "-----------------------------------------------------"
echo "Finished $0 ... `/bin/date`"
echo "-----------------------------------------------------"
