#!/bin/csh -f
# Copyright 2015 West Coast Informatics, LLC
#  This script is used to load terminology server data for the development
# environment.  This data can be found in the config/data folder of the
# distribution.

# Configure 
set SNOMEDCT_CODE=~/code
set SNOMEDCT_CONFIG=~/config/config-load.properties
set SNOMEDCT_DATA=~/data
set SERVER=false
echo "------------------------------------------------"
echo "Starting ...`/bin/date`"
echo "------------------------------------------------"
echo "SNOMEDCT_CODE = $SNOMEDCT_CODE"
echo "SNOMEDCT_DATA = $SNOMEDCT_DATA"
echo "SNOMEDCT_CONFIG = $SNOMEDCT_CONFIG"
echo "SERVER = $SERVER"

echo "    Run Createdb ...`/bin/date`"
cd $SNOMEDCT_CODE/admin/db
mvn install -PCreatedb -Drun.config.ts=$SNOMEDCT_CONFIG >&! mvn.log
if ($status != 0) then
    echo "ERROR running createdb"
    cat mvn.log
    exit 1
endif

echo "    Clear indexes ...`/bin/date`"
cd $SNOMEDCT_CODE/admin/lucene
mvn install -PReindex -Drun.config.ts=$SNOMEDCT_CONFIG -Dserver=$SERVER >&! mvn.log
if ($status != 0) then
    echo "ERROR running lucene"
    cat mvn.log
    exit 1
endif

echo "    Load SNOMEDCT ...`/bin/date`"
cd $SNOMEDCT_CODE/admin/loader
mvn install -PRF2-snapshot -Drun.config.ts=$SNOMEDCT_CONFIG -Dserver=$SERVER -Dterminology=SNOMEDCT -Dversion=latest -Dinput.dir=$SNOMEDCT_DATA/snomedct-20140731-mini >&! mvn.log
if ($status != 0) then
    echo "ERROR loading SNOMEDCT"
    cat mvn.log
    exit 1
endif

echo "    Add SNOMEDCT project ...`/bin/date`"
cd $SNOMEDCT_CODE/admin/loader
mvn install -PProject -Drun.config.ts=$SNOMEDCT_CONFIG -Dserver=$SERVER \
  -Dname="Sample Project" -Ddescription="Sample project." \
  -Dterminology=SNOMEDCT -Dversion=latest \
  -Dadmin.user=admin >&! mvn.log
if ($status != 0) then
    echo "ERROR adding project for SNOMEDCT"
    cat mvn.log
    exit 1
endif


echo "    Start SNOMEDCT editing ...`/bin/date`"
cd $SNOMEDCT_CODE/admin/release
mvn install -PStartEditingCycle -Drun.config.ts=$SNOMEDCT_CONFIG \
  -Dserver=$SERVER -Drelease.version=20150131 -Dterminology=SNOMEDCT \
  -Dversion=latest >&! mvn.log
if ($status != 0) then
    echo "ERROR starting editing for SNOMEDCT"
    cat mvn.log
    exit 1
endif

echo "------------------------------------------------"
echo "Finished ...`/bin/date`"
echo "------------------------------------------------"
