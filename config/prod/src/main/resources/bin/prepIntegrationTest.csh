#!/bin/csh -f
# Prep environment for integration testing

# Configure
set UMLS_CODE=/home/ec2-tomcat/umls/code
set UMLS_CONFIG=/home/ec2-tomcat/umls/config/config.properties
set UMLS_DATA=/home/ec2-tomcat/umls/data
set SERVER=false
echo "------------------------------------------------"
echo "Starting ...`/bin/date`"
echo "------------------------------------------------"
echo "UMLS_CODE = $UMLS_CODE"
echo "UMLS_DATA = $UMLS_DATA"
echo "UMLS_CONFIG = $UMLS_CONFIG"
echo "SERVER = $SERVER"
echo ""

# NO server deployed for mojo integration tests

echo "    Pull code ...`/bin/date`"
cd $UMLS_CODE
git pull | sed 's/^/      /'
if ($status != 0) then
    echo "Failed to pull project"
    exit 1
endif

echo "    Rebuild code ...`/bin/date`"
mvn -Dconfig.artifactId=term-server-config-prod clean install | sed 's/^/      /' >&! /tmp/x.$$
if ($status != 0) then
    cat /tmp/x.$$
    echo "Failed to build maven project"
    exit 1
endif

echo "    Clear indexes ...`/bin/date`"
/bin/rm -rf /var/lib/tomcat8/testindexes/*

# NO sample data to load, the mojo tests load their own data

echo "------------------------------------------------"
echo "Finished ...`/bin/date`"
echo "------------------------------------------------"
