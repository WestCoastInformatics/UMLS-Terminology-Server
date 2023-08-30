#!/bin/tcsh -f
#
# This script is used to create a new rds db from the latest backup of meme-edit.  If you don't want the latest automatic backup or if you want a backup other than the meme-edit db, do the refresh manually from the UI.  
#

setenv usage 'refresh_rds.csh {meme-test|meme-release|meme-dev} '
setenv TEST_DB meme-test
setenv DEV_DB meme-dev
setenv RELEASE_DB meme-release

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
	echo "ERROR: DB created must be $DEV_DB or $TEST_DB or $RELEASE_DB" 
	exit 1
endif

if ($DB_NAME == 'meme-dev') then
    setenv VPC sg-0a42ddabf8c260525
else 
    setenv VPC sg-05993d12d18c40cae
endif

echo "VPC:    $VPC"


aws rds restore-db-instance-to-point-in-time --profile meme --source-db-instance-identifier meme-edit --target-db-instance-identifier $DB_NAME --use-latest-restorable-time --db-parameter-group-name meme-db --availability-zone us-east-1d --db-subnet-group-name mysql-subnet-group --vpc-security-group-ids $VPC --tags "Key"="autostart","Value"="true" "Key"="autostop","Value"="true"

echo ""

set started = null
while ($started != 'available')
   echo "refreshing"
   set started = `aws rds describe-db-instances --profile meme --query "DBInstances[?DBInstanceIdentifier=='$DB_NAME'].[DBInstanceStatus][0][0]" | jq -r`
end

echo "refresh completed"

echo "-----------------------------------------------------"
echo "Finished $0 ... `/bin/date`"
echo "-----------------------------------------------------"
