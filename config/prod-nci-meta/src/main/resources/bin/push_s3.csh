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

if ($#argv == 2) then
    setenv INV_OR_MR $1
    setenv SOURCE_NAME $2
else
    echo "ERROR: Wrong number of parameters"
    echo "ERROR: $usage"
    exit 1
endif

echo "INV_OR_MR:          $INV_OR_MR"
echo "SOURCE_NAME:        $SOURCE_NAME"

if ($INV_OR_MR != 'inv' && $INV_OR_MR != 'mr') then
    echo "ERROR: inv or mr must be specified as first parameter"
	echo "ERROR: $usage"
endif

if ($INV_OR_MR == 'inv') then 
	setenv SOURCE_PATH /tmp/sources
else
	setenv SOURCE_PATH /tmp/mr
endif

cd $SOURCE_PATH
if (! -e $SOURCE_PATH/$SOURCE_NAME) then
    echo "ERROR: $SOURCE_PATH/$SOURCE_NAME doesn't exist. The second parameter must be either the name of a source/version folder or the name of a release date folder."
    exit 1
endif

if (! -d $SOURCE_PATH/$SOURCE_NAME) then
	echo "ERROR: SOURCE_NAME $SOURCE_NAME must be a directory." 
	exit 1
else
	echo "Data folder found"
endif


echo "    Put data on s3... `/bin/date`"
if ($INV_OR_MR == 'inv') then
	#aws s3api head-object --bucket wci1 --key NCI/inv/$SOURCE_NAME
	set fileExists = `aws s3api head-object --bucket wci1 --key NCI/inv/$SOURCE_NAME.tgz | grep Metadata | wc -l `
	echo "fileExists $fileExists"
    if ($fileExists == 1) then
	    echo "ERROR: File NCI/inv/$SOURCE_NAME.tgz already exists in bucket"
	    exit 1
	endif
	tar -zcvf $SOURCE_NAME.tgz $SOURCE_NAME
    aws s3 cp $SOURCE_NAME.tgz s3://wci1/NCI/inv/$SOURCE_NAME.tgz
else if ($INV_OR_MR == 'mr') then
	#aws s3api head-object --bucket wci1 --key NCI/mr/$SOURCE_NAME.zip
	set fileExists = `aws s3api head-object --bucket wci1 --key NCI/mr/$SOURCE_NAME.zip | grep Metadata | wc -l `
	echo "fileExists $fileExists"
    if ($fileExists == 1) then
	    echo "ERROR: File NCI/mr/$SOURCE_NAME.zip already exists in bucket"
	    exit 1
	endif
	zip -r $SOURCE_NAME.zip $SOURCE_NAME
    aws s3 cp $SOURCE_NAME.zip s3://wci1/NCI/mr
endif


echo ""


echo "-----------------------------------------------------"
echo "Finished $0 ... `/bin/date`"
echo "-----------------------------------------------------"
