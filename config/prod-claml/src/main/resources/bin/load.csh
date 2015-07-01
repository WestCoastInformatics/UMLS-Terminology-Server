#!/bin/csh -f
# Copyright 2015 West Coast Informatics, LLC
#  This script is used to load terminology server data for the development
# environment.  This data can be found in the config/data folder of the
# distribution.

# Configure 
set ICD10_CODE=~/claml/code
set ICD10_CONFIG=~/claml/config/config.properties
set ICD10_DATA=~/claml/data
set SERVER=false
echo "------------------------------------------------"
echo "Starting ...`/bin/date`"
echo "------------------------------------------------"
echo "ICD10_CODE = $ICD10_CODE"
echo "ICD10_DATA = $ICD10_DATA"
echo "ICD10_CONFIG = $ICD10_CONFIG"
echo "SERVER = $SERVER"

echo "    Run Createdb ...`/bin/date`"
cd $ICD10_CODE/admin/db
mvn install -PCreatedb -Drun.config.icd=$ICD10_CONFIG >&! mvn.log
if ($status != 0) then
    echo "ERROR running createdb"
    cat mvn.log
    exit 1
endif

echo "    Clear indexes ...`/bin/date`"
cd $ICD10_CODE/admin/lucene
mvn install -PReindex -Drun.config.icd=$ICD10_CONFIG -Dserver=$SERVER >&! mvn.log
if ($status != 0) then
    echo "ERROR running lucene"
    cat mvn.log
    exit 1
endif

echo "    Load ICD10 ...`/bin/date`"
cd $ICD10_CODE/admin/loader
mvn install -PClaML -Drun.config.icd=$ICD10_CONFIG -Dserver=$SERVER -Dterminology=ICD10 -Dversion=latest -Dinput.dir=$ICD10_DATA/icd10.xml >&! mvn.log
if ($status != 0) then
    echo "ERROR loading ICD10"
    cat mvn.log
    exit 1
endif

echo "    Add ICD10 project ...`/bin/date`"
cd $ICD10_CODE/admin/loader
mvn install -PProject -Drun.config.icd=$ICD10_CONFIG -Dserver=$SERVER \
  -Dname="Sample Project" -Ddescription="Sample project." \
  -Dterminology=ICD10 -Dversion=latest \
  -Dadmin.user=admin >&! mvn.log
if ($status != 0) then
    echo "ERROR adding project for ICD10"
    cat mvn.log
    exit 1
endif


echo "    Start ICD10 editing ...`/bin/date`"
cd $ICD10_CODE/admin/release
mvn install -PStartEditingCycle -Drun.config.icd=$ICD10_CONFIG \
  -Dserver=$SERVER -Drelease.version=20150131 -Dterminology=ICD10 \
  -Dversion=latest >&! mvn.log
if ($status != 0) then
    echo "ERROR starting editing for ICD10"
    cat mvn.log
    exit 1
endif

echo "    Load ICD10CM ...`/bin/date`"
cd $ICD10_CODE/admin/loader
mvn install -PClaML -Drun.config.icd=$ICD10_CONFIG -Dserver=$SERVER -Dterminology=ICD10CM -Dversion=latest -Dinput.dir=$ICD10_DATA/icd10cm.xml >&! mvn.log
if ($status != 0) then
    echo "ERROR loading ICD10CM"
    cat mvn.log
    exit 1
endif

echo "    Add ICD10CM project ...`/bin/date`"
cd $ICD10_CODE/admin/loader
mvn install -PProject -Drun.config.icd=$ICD10_CONFIG -Dserver=$SERVER \
  -Dname="Sample Project" -Ddescription="Sample project." \
  -Dterminology=ICD10CM -Dversion=latest \
  -Dadmin.user=admin >&! mvn.log
if ($status != 0) then
    echo "ERROR adding project for ICD10CM"
    cat mvn.log
    exit 1
endif


echo "    Start ICD10CM editing ...`/bin/date`"
cd $ICD10_CODE/admin/release
mvn install -PStartEditingCycle -Drun.config.icd=$ICD10_CONFIG \
  -Dserver=$SERVER -Drelease.version=20150131 -Dterminology=ICD10CM \
  -Dversion=latest >&! mvn.log
if ($status != 0) then
    echo "ERROR starting editing for ICD10CM"
    cat mvn.log
    exit 1
endif

echo "------------------------------------------------"
echo "Finished ...`/bin/date`"
echo "------------------------------------------------"
