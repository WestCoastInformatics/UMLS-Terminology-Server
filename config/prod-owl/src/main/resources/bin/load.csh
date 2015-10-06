#!/bin/csh -f
# Copyright 2015 West Coast Informatics, LLC
#  This script is used to load terminology server data for the development
# environment.  This data can be found in the config/data folder of the
# distribution.

# Configure 
set UMLS_CODE=~/owl/code
set UMLS_CONFIG=~/owl/config/config-load.properties
set UMLS_DATA=~/owl/data
set SERVER=false
echo "------------------------------------------------"
echo "Starting ...`/bin/date`"
echo "------------------------------------------------"
echo "UMLS_CODE = $UMLS_CODE"
echo "UMLS_DATA = $UMLS_DATA"
echo "UMLS_CONFIG = $UMLS_CONFIG"
echo "SERVER = $SERVER"

echo "    Load snomed.owl ...`/bin/date`"
cd $UMLS_CODE/admin/loader
mvn install -POwl-umls -Drun.config.umls=$UMLS_CONFIG -Dmode=create -Dserver=$SERVER -Dterminology=SNOMEDCT -Dversion=latest -Dinput.file=$UMLS_DATA/snomed.owl >&! mvn.log
if ($status != 0) then
    echo "ERROR loading snomed.owl"
    cat mvn.log
    exit 1
endif

echo "    Load go.owl ...`/bin/date`"
cd $UMLS_CODE/admin/loader
mvn install -POwl -Drun.config.umls=$UMLS_CONFIG -Dserver=$SERVER -Dterminology=GO -Dversion=latest -Dinput.file=$UMLS_DATA/go.owl >&! mvn.log
if ($status != 0) then
    echo "ERROR loading go.owl"
    cat mvn.log
    exit 1
endif

echo "    Load Thesaurus.owl ...`/bin/date`"
cd $UMLS_CODE/admin/loader
mvn install -POwl -Drun.config.umls=$UMLS_CONFIG -Dserver=$SERVER -Dterminology=NCI -Dversion=latest -Dinput.file=$UMLS_DATA/Thesaurus.owl >&! mvn.log
if ($status != 0) then
    echo "ERROR loading Thesaurus.owl"
    cat mvn.log
    exit 1
endif

echo "------------------------------------------------"
echo "Finished ...`/bin/date`"
echo "------------------------------------------------"
