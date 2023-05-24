#!/bin/tcsh -f
#
# This script is used to pull/download inversion source folders or release packages from s3
# Usage examples:
# ./pull_s3.csh inv NCI_2022_09D
# ./pull_s3.csh ncim 202208
# ./pull_s3.csh umls 2022AB
#

set rootdir = `dirname $0`
set abs_rootdir = `cd $rootdir && pwd`

setenv usage 'pull_s3.csh {inv|ncim|umls} {source_name|release_date}'
setenv S3_BUCKET s3://nci-evs-meme

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

if ($INV_OR_MR != 'inv' && $INV_OR_MR != 'ncim' && $INV_OR_MR != 'umls') then
    echo "ERROR: inv or ncim or umls must be specified as first parameter"
	echo "ERROR: $usage"
endif

if ($INV_OR_MR == 'inv') then 
	setenv TARGET_PATH /local/content/MEME/MEME5/inv/sources
else
	setenv TARGET_PATH /local/content/MEME/MEME5/mr
endif


if (! -e $TARGET_PATH) then
    echo "ERROR: $TARGET_PATH must exist."
	exit 1
endif

cd $TARGET_PATH
if ( $INV_OR_MR == 'inv' && -e $TARGET_PATH/$TARGET_NAME) then
    echo "ERROR: $TARGET_PATH/$TARGET_NAME already exists.  To refresh, please delete the folder and run the pull again."
	exit 1
endif
if ( $INV_OR_MR == 'ncim' && -e $TARGET_PATH/ncim/$TARGET_NAME) then
    echo "ERROR: $TARGET_PATH/ncim/$TARGET_NAME already exists.  To refresh, please delete the folder and run the pull again."
	exit 1
endif
if ( $INV_OR_MR == 'umls' && -e $TARGET_PATH/umls/$TARGET_NAME) then
    echo "ERROR: $TARGET_PATH/umls/$TARGET_NAME already exists.  To refresh, please delete the folder and run the pull again."
	exit 1
endif

echo "    Download data from s3 and decompress it... `/bin/date`"
if ($INV_OR_MR == 'inv') then
    set fileExists = `aws s3api head-object --bucket nci-evs-meme --key inv/sources/$TARGET_NAME.tgz | grep Metadata | wc -l `
    if ($fileExists == 1) then
	    echo "    Downloading $TARGET_NAME.tgz"
        aws s3 cp $S3_BUCKET/inv/sources/$TARGET_NAME.tgz . 
	    echo "    Extracting $TARGET_NAME.tgz"
	    tar -xvf $TARGET_NAME.tgz
	    rm -f $TARGET_NAME.tgz
    else
        set fileExists = `aws s3api head-object --bucket nci-evs-meme --key inv/sources/$TARGET_NAME.tar.gz | grep Metadata | wc -l `    
		if ($fileExists == 1) then
	        echo "    Downloading $TARGET_NAME.tar.gz"
	        aws s3 cp $S3_BUCKET/inv/sources/$TARGET_NAME.tar.gz . 
	        echo "    Extracting $TARGET_NAME.tar.gz"
	        tar -xvf $TARGET_NAME.tar.gz
	        rm -f $TARGET_NAME.tar.gz
		else
			echo "    Neither $TARGET_NAME.tar.gz nor $TARGET_NAME.tgz were found.  Exiting..."
		    exit 1
		endif
    endif

else if ($INV_OR_MR == 'ncim') then
    set fileExists = `aws s3api head-object --bucket nci-evs-meme --key mr/ncim/$TARGET_NAME/$TARGET_NAME.zip | grep Metadata | wc -l `    
	if ($fileExists == 1) then
	    echo "    Downloading $TARGET_NAME"
    	mkdir $TARGET_PATH/ncim/$TARGET_NAME
	    cd $TARGET_PATH/ncim/$TARGET_NAME
        aws s3 cp $S3_BUCKET/mr/ncim/$TARGET_NAME . --recursive
 	else
		echo "    $TARGET_NAME was not found.  Exiting..."
	    exit 1
    endif	
else if ($INV_OR_MR == 'umls') then
    set fileExists = `aws s3api head-object --bucket nci-evs-meme --key mr/umls/$TARGET_NAME/META/MRSAB.RRF | grep Metadata | wc -l `    
	if ($fileExists == 1) then
	    echo "    Downloading $TARGET_NAME"
    	mkdir $TARGET_PATH/umls/$TARGET_NAME
		cd $TARGET_PATH/umls/$TARGET_NAME
    	aws s3 cp $S3_BUCKET/mr/umls/$TARGET_NAME . --recursive
 	else
		echo "    $TARGET_NAME was not found.  Exiting..."
	    exit 1
    endif	
endif


echo ""


echo "-----------------------------------------------------"
echo "Finished $0 ... `/bin/date`"
echo "Check $TARGET_PATH/$TARGET_NAME for results"
echo "-----------------------------------------------------"
