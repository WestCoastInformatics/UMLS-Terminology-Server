#!/bin/csh -f
# Copyright 2015 West Coast Informatics, LLC
#  This script is used to load terminology server data for the development
# environment.  This data can be found in the config/data folder of the
# distribution.

# Configure 
set SNOMEDCT_CODE=~/vet/code
set SNOMEDCT_CONFIG=~/vet/config/config-load.properties
set SNOMEDCT_DATA=~/vet/data
set SERVER=false
echo "------------------------------------------------"
echo "Starting ...`/bin/date`"
echo "------------------------------------------------"
echo "SNOMEDCT_CODE = $SNOMEDCT_CODE"
echo "SNOMEDCT_DATA = $SNOMEDCT_DATA"
echo "SNOMEDCT_CONFIG = $SNOMEDCT_CONFIG"
echo "SERVER = $SERVER"

echo "    Load SNOMEDCT ...`/bin/date`"
cd $SNOMEDCT_CODE/admin/loader
mvn install -PRF2-snapshot -Drun.config.vet=$SNOMEDCT_CONFIG -Dserver=$SERVER -Dmode=create -Dterminology=SNOMEDCT -Dversion=latest -Dinput.dir=$SNOMEDCT_DATA/snomedct-vet-data >&! mvn.log
if ($status != 0) then
    echo "ERROR loading SNOMEDCT"
    cat mvn.log
    exit 1
endif

echo "------------------------------------------------"
echo "Finished ...`/bin/date`"
echo "------------------------------------------------"
