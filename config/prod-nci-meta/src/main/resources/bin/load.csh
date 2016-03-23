#!/bin/csh -f
# Copyright 2015 West Coast Informatics, LLC
#  This script is used to load terminology server data for the development
# environment.  This data can be found in the config/data folder of the
# distribution.

# Configure 
set CODE=~/code
set CONFIG=~/config/config-load.properties
set DATA=~/data
set SERVER=false
echo "------------------------------------------------"
echo "Starting ...`/bin/date`"
echo "------------------------------------------------"
echo "CODE = $CODE"
echo "DATA = $DATA"
echo "CONFIG = $CONFIG"
echo "SERVER = $SERVER"

echo "    Run Createdb ...`/bin/date`"
cd $CODE/admin/db
mvn install -PCreatedb -Drun.config.umls=$CONFIG >&! mvn.log
if ($status != 0) then
    echo "ERROR running createdb"
    cat mvn.log
    exit 1
endif

echo "    Clear indexes ...`/bin/date`"
cd $CODE/admin/lucene
mvn install -PReindex -Drun.config.umls=$CONFIG -Dserver=$SERVER >&! mvn.log
if ($status != 0) then
    echo "ERROR running lucene"
    cat mvn.log
    exit 1
endif

echo "    Load NCI-META ...`/bin/date`"
cd $CODE/admin/loader
mvn install -PRRF-umls -Drun.config.umls=$CONFIG -Dserver=$SERVER -Dterminology=NCI-META -Dversion=latest -Dinput.dir=$DATA/META_201508 >&! mvn.log
if ($status != 0) then
    echo "ERROR loading NCI-META"
    cat mvn.log
    exit 1
endif

echo "    Add NCI-META project ...`/bin/date`"
cd $CODE/admin/loader
mvn install -PProject -Drun.config.umls=$CONFIG -Dserver=$SERVER \
  -Dname="Sample Project" -Ddescription="Sample project." \
  -Dterminology=NCI-META -Dversion=latest \
  -Dscope.concepts=138875005 -Dscope.descendants.flag=true \
  -Dadmin.user=admin >&! mvn.log
if ($status != 0) then
    echo "ERROR adding project for NCI-META"
    cat mvn.log
    exit 1
endif


echo "    Start NCI-META editing ...`/bin/date`"
cd $CODE/admin/release
mvn install -PStartEditingCycle -Drun.config.umls=$CONFIG \
  -Dserver=$SERVER -Drelease.version=2015AA -Dterminology=NCI-META \
  -Dversion=latest >&! mvn.log
if ($status != 0) then
    echo "ERROR starting editing for NCI-META"
    cat mvn.log
    exit 1
endif

echo "------------------------------------------------"
echo "Finished ...`/bin/date`"
echo "------------------------------------------------"
