#!/bin/csh -f
# Copyright 2015 West Coast Informatics, LLC
#  This script is used to load terminology server data for the development
# environment.  This data can be found in the config/data folder of the
# distribution.

# Configure 
set UMLS_CODE=~/code
set UMLS_CONFIG=~/config/config.properties
set UMLS_DATA=~/data
set SERVER=false
echo "------------------------------------------------"
echo "Starting ...`/bin/date`"
echo "------------------------------------------------"
echo "UMLS_CODE = $UMLS_CODE"
echo "UMLS_DATA = $UMLS_DATA"
echo "UMLS_CONFIG = $UMLS_CONFIG"
echo "SERVER = $SERVER"

echo "    Run Createdb ...`/bin/date`"
cd $UMLS_CODE/admin/db
mvn install -PCreatedb -Drun.config.umls=$UMLS_CONFIG >&! mvn.log
if ($status != 0) then
    echo "ERROR running createdb"
    cat mvn.log
    exit 1
endif

echo "    Clear indexes ...`/bin/date`"
cd $UMLS_CODE/admin/lucene
mvn install -PReindex -Drun.config.umls=$UMLS_CONFIG -Dserver=$SERVER >&! mvn.log
if ($status != 0) then
    echo "ERROR running lucene"
    cat mvn.log
    exit 1
endif

echo "    Load UMLS ...`/bin/date`"
cd $UMLS_CODE/admin/loader
mvn install -PRRF-umls -Drun.config.umls=$UMLS_CONFIG -Dserver=$SERVER -Dterminology=UMLS -Dversion=latest -Dinput.dir=$UMLS_DATA/SCTMSH_2014AB >&! mvn.log
if ($status != 0) then
    echo "ERROR loading UMLS"
    cat mvn.log
    exit 1
endif

echo "    Add UMLS project ...`/bin/date`"
cd $UMLS_CODE/admin/loader
mvn install -PProject -Drun.config.umls=$UMLS_CONFIG -Dserver=$SERVER \
  -Dname="Sample Project" -Ddescription="Sample project." \
  -Dterminology=UMLS -Dversion=latest \
  -Dscope.concepts=138875005 -Dscope.descendants.flag=true \
  -Dadmin.user=admin >&! mvn.log
if ($status != 0) then
    echo "ERROR adding project for UMLS"
    cat mvn.log
    exit 1
endif


echo "    Start UMLS editing ...`/bin/date`"
cd $UMLS_CODE/admin/release
mvn install -PStartEditingCycle -Drun.config.umls=$UMLS_CONFIG \
  -Dserver=$SERVER -Drelease.version=2015AA -Dterminology=UMLS \
  -Dversion=latest >&! mvn.log
if ($status != 0) then
    echo "ERROR starting editing for UMLS"
    cat mvn.log
    exit 1
endif

echo "------------------------------------------------"
echo "Finished ...`/bin/date`"
echo "------------------------------------------------"
