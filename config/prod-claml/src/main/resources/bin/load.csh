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
mvn install -PClaML -Drun.config.claml=$ICD10_CONFIG -Dmode=create -Dserver=$SERVER -Dterminology=ICD10CM -Dversion=latest -Dinput.file=$ICD10_DATA/icd10cm.xml >&! mvn.log
if ($status != 0) then
    echo "ERROR loading ICD10CM"
    cat mvn.log
    exit 1
endif

echo "    Add ICD10CM project ...`/bin/date`"
cd $ICD10_CODE/admin/loader
mvn install -PProject -Drun.config.claml=$ICD10_CONFIG -Dserver=$SERVER \
  -Dname="ICD10CM Project" -Ddescription="ICD10CM project." \
  -Dterminology=ICD10CM -Dversion=latest \
  -Dadmin.user=admin >&! mvn.log
if ($status != 0) then
    echo "ERROR adding project for ICD10CM"
    cat mvn.log
    exit 1
endif


echo "    Start ICD10CM editing ...`/bin/date`"
cd $ICD10_CODE/admin/release
mvn install -PStartEditingCycle -Drun.config.claml=$ICD10_CONFIG \
  -Dserver=$SERVER -Drelease.version=20150131 -Dterminology=ICD10CM \
  -Dversion=latest >&! mvn.log
if ($status != 0) then
    echo "ERROR starting editing for ICD10CM"
    cat mvn.log
    exit 1
endif


echo "    Load ICD9CM ...`/bin/date`"
cd $ICD10_CODE/admin/loader
mvn install -PClaML -Drun.config.claml=$ICD10_CONFIG -Dserver=$SERVER -Dterminology=ICD9CM -Dversion=latest -Dinput.file=$ICD10_DATA/icd9cm-2013.xml >&! mvn.log
if ($status != 0) then
    echo "ERROR loading ICD9CM"
    cat mvn.log
    exit 1
endif

echo "    Add ICD9CM project ...`/bin/date`"
cd $ICD10_CODE/admin/loader
mvn install -PProject -Drun.config.claml=$ICD10_CONFIG -Dserver=$SERVER \
  -Dname="ICD9CM Project" -Ddescription="ICD9CM project." \
  -Dterminology=ICD9CM -Dversion=latest \
  -Dadmin.user=admin >&! mvn.log
if ($status != 0) then
    echo "ERROR adding project for ICD9CM"
    cat mvn.log
    exit 1
endif

echo "    Start ICD9CM editing ...`/bin/date`"
cd $ICD10_CODE/admin/release
mvn install -PStartEditingCycle -Drun.config.claml=$ICD10_CONFIG \
  -Dserver=$SERVER -Drelease.version=20150131 -Dterminology=ICD9CM \
  -Dversion=latest >&! mvn.log
if ($status != 0) then
    echo "ERROR starting editing for ICD9CM"
    cat mvn.log
    exit 1
endif

echo "------------------------------------------------"
echo "Finished ...`/bin/date`"
echo "------------------------------------------------"
