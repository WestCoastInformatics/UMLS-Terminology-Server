#!/bin/tcsh -f
#
# This script is used to pull/download inversion source folders or release packages from s3
#

set rootdir = `dirname $0`
set abs_rootdir = `cd $rootdir && pwd`

#setenv TARGET_PATH /meme_work/inv/sources
#setenv TARGET_PATH /meme_work/mr
setenv usage 'pull_s3.csh {inv|mr} {source_name|release_date}'
setenv S3_BUCKET s://wci1/NCI

echo "--------------------------------------------------------"
echo "Starting `/bin/date`"
echo "--------------------------------------------------------"

if ($#argv == 2) then
    setenv INV_OR_MR $1
    setenv TARGET_NAME $2
else
    echo "ERROR: Wrong number of parameters"
    echo "usage: $usage"
    exit 1
endif

echo "INV_OR_MR:    $INV_OR_MR"
echo "TARGET_NAME: 	$TARGET_NAME"

if ($INV_OR_MR != 'inv' && $INV_OR_MR != 'mr') then
    echo "ERROR: inv or mr must be specified as first parameter"
	echo "ERROR: $usage"
endif

if ($INV_OR_MR == 'inv') then 
	setenv TARGET_PATH /tmp/sources
else
	setenv TARGET_PATH /tmp/mr
endif


if (! -e $TARGET_PATH) then
    echo "ERROR: $TARGET_PATH must exist."
	exit 1
endif

cd $TARGET_PATH
if (-e $TARGET_PATH/$TARGET_NAME) then
    echo "ERROR: $TARGET_PATH/$TARGET_NAME already exists.  To refresh, please delete the folder and run the pull again."
	exit 1
endif

echo "    Download data from s3 and decompress it... `/bin/date`"
if ($INV_OR_MR == 'inv') then
    aws s3 cp $s3_BUCKET/inv/$TARGET_NAME.tgz . 
	tar -xvf $TARGET_NAME.tgz
	rm -f $TARGET_NAME.tgz
else if ($INV_OR_MR == 'mr') then
    aws s3 cp $S3_BUCKET/mr/$TARGET_NAME.zip .
	unzip $TARGET_NAME.zip
	rm -f $TARGET_NAME.zip
endif


echo ""


echo "-----------------------------------------------------"
echo "Finished $0 ... `/bin/date`"
echo "-----------------------------------------------------"
