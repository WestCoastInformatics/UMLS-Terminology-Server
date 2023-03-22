#!/bin/tcsh -f
#
# This script is used to push inversion source folders or release packages to s3.  INversion folders will be compressed as part of the upload process.
#
# Usage examples:
# ./push_s3.csh inv NCI_2022_10E
# ./push_s3.csh ncim 202208
# ./push_s3.csh umls 2022AB
#

set usage = 'push_s3.csh {inv|ncim|umls} {source_name|release_date}'
setenv S3_BUCKET s3://nci-evs-meme

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

if ($INV_OR_MR != 'inv' && $INV_OR_MR != 'ncim' && $INV_OR_MR != 'umls') then
    echo "ERROR: inv or ncim or umls  must be specified as first parameter"
	echo "ERROR: $usage"
endif

if ($INV_OR_MR == 'inv') then 
	setenv SOURCE_PATH /local/content/MEME/MEME5/inv/sources
endif
if ($INV_OR_MR == 'ncim') then 
	setenv SOURCE_PATH /local/content/MEME/MEME5/mr/ncim
endif
if ($INV_OR_MR == 'umls') then 
	setenv SOURCE_PATH /local/content/MEME/MEME5/mr/umls
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
	set fileExists = `aws s3api head-object --bucket nci-evs-meme --key inv/sources/$SOURCE_NAME.tgz | grep Metadata | wc -l `
    if ($fileExists == 1) then
	    echo "ERROR: File inv/sources/$SOURCE_NAME.tgz already exists in bucket"
	    exit 1
	endif
	set fileExists = `aws s3api head-object --bucket nci-evs-meme --key inv/sources/$SOURCE_NAME.tar.gz | grep Metadata | wc -l `
    if ($fileExists == 1) then
	    echo "ERROR: File inv/sources/$SOURCE_NAME.tar.gz already exists in bucket"
	    exit 1
	endif
	tar -zcvf $SOURCE_NAME.tgz $SOURCE_NAME
    aws s3 cp $SOURCE_NAME.tgz $S3_BUCKET/inv/sources/$SOURCE_NAME.tgz
else if ($INV_OR_MR == 'ncim') then
	set fileExists = `aws s3api head-object --bucket nci-evs-meme --key mr/ncim/$SOURCE_NAME/$SOURCE_NAME.zip | grep Metadata | wc -l `
    if ($fileExists == 1) then
	    echo "ERROR: File mr/ncim/$SOURCE_NAME already exists in bucket"
	    exit 1
	endif
    aws s3 cp $SOURCE_NAME $S3_BUCKET/mr/ncim/$SOURCE_NAME --recursive
else if ($INV_OR_MR == 'umls') then
	set fileExists = `aws s3api head-object --bucket nci-evs-meme --key mr/umls/$SOURCE_NAME/META/MRSAB.RRF | grep Metadata | wc -l `
    if ($fileExists == 1) then
	    echo "ERROR: File mr/umls/$SOURCE_NAME already exists in bucket"
	    exit 1
	endif
    aws s3 cp $SOURCE_NAME $S3_BUCKET/mr/umls/$SOURCE_NAME --recursive
endif


echo ""


echo "-----------------------------------------------------"
echo "Finished $0 ... `/bin/date`"
echo "-----------------------------------------------------"
