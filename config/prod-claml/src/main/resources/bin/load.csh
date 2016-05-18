#!/bin/csh -f
# Copyright 2015 West Coast Informatics, LLC
#  This script is used to load terminology server data for the development
# environment.  This data can be found in the config/data folder of the
# distribution.

# Configure 
set ICD10_CODE=~/claml/code
set ICD10_CONFIG=~/claml/config/config-load.properties
set ICD10_DATA=~/claml/data
set SERVER=false
echo "------------------------------------------------"
echo "Starting ...`/bin/date`"
echo "------------------------------------------------"
echo "ICD10_CODE = $ICD10_CODE"
echo "ICD10_DATA = $ICD10_DATA"
echo "ICD10_CONFIG = $ICD10_CONFIG"
echo "SERVER = $SERVER"

echo "    Load ICD10CM ...`/bin/date`"
cd $ICD10_CODE/admin/loader
mvn install -PClaML -Drun.config.umls=$ICD10_CONFIG -Dmode=create -Dserver=$SERVER -Dterminology=ICD10CM -Dversion=latest -Dinput.file=$ICD10_DATA/icd10cm.xml >&! mvn.log
if ($status != 0) then
    echo "ERROR loading ICD10CM"
    cat mvn.log
    exit 1
endif

echo "    Load ICD9CM ...`/bin/date`"
cd $ICD10_CODE/admin/loader
mvn install -PClaML -Drun.config.umls=$ICD10_CONFIG -Dserver=$SERVER -Dterminology=ICD9CM -Dversion=latest -Dinput.file=$ICD10_DATA/icd9cm-2013.xml >&! mvn.log
if ($status != 0) then
    echo "ERROR loading ICD9CM"
    cat mvn.log
    exit 1
endif



echo "------------------------------------------------"
echo "Finished ...`/bin/date`"
echo "------------------------------------------------"
