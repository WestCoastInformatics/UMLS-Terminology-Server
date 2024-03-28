#!/bin/tcsh -f
set rootdir = `dirname $0`
set abs_rootdir = `cd $rootdir && pwd`
setenv MEME_HOME $abs_rootdir:h
setenv S3_BUCKET s3://nci-evs-meme

echo "--------------------------------------------------------"
echo "Starting `/bin/date`"
echo "--------------------------------------------------------"
echo "MEME_HOME = $MEME_HOME"

echo "Collect settings..."
set host = `grep 'javax.persistence.jdbc.url' $MEME_HOME/config/config.properties | perl -ne '@_ = split/=/; $_[1] =~ /jdbc:mysql:\/\/(.*):(\d*)\/(.*)\?/; print "$1"'`
set port = `grep 'javax.persistence.jdbc.url' $MEME_HOME/config/config.properties | perl -ne '@_ = split/=/; $_[1] =~ /jdbc:mysql:\/\/(.*):(\d*)\/(.*)\?/; print "$2"'`
set db = `grep 'javax.persistence.jdbc.url' $MEME_HOME/config/config.properties | perl -ne '@_ = split/=/; $_[1] =~ /jdbc:mysql:\/\/(.*):(\d*)\/(.*)\?/; print "$3"'`
set user = `grep 'javax.persistence.jdbc.user' $MEME_HOME/config/config.properties | perl -ne '@_ = split/=/; print $_[1];'`
set pwd = `grep 'javax.persistence.jdbc.password' $MEME_HOME/config/config.properties | perl -ne '@_ = split/=/; print $_[1];'`
set mysql = "mysql -h$host -P$port -u$user -p$pwd $db"
echo "mysql= $mysql"
set url = `grep 'base.url' $MEME_HOME/config/config.properties | perl -ne '@_ = split/=/; print $_[1];'`
set adminUser = `grep 'admin.user' $MEME_HOME/config/config.properties | perl -ne '@_ = split/=/; print $_[1];'`
set adminPwd = `grep 'admin.password' $MEME_HOME/config/config.properties | perl -ne '@_ = split/=/; print $_[1];'`
set enabled = `echo "select if(automationsEnabled,'true','false') from projects;" | $mysql | tail -1`
set projectId = `echo "select id from projects;" | $mysql | tail -1`

echo "project: $projectId"
echo "enabled: $enabled"
echo ""



# if enabled, run stuff
if ($enabled == "true") then

cd $MEME_HOME/archive/indexes
echo `pwd`
if ($#argv == 1) then
    set dayofweek='manual'
    set todaysdate=`date +"manual_%Y%m%d"`
else
    set dayofweek=`date +"%a"`
    set todaysdate=`date +"%a_%Y%m%d"`
endif

echo "todaysdate: $todaysdate"

mkdir $MEME_HOME/archive/indexes/$todaysdate
cp -R /local/content/MEME/MEME5/ncim/data/indexes/* $MEME_HOME/archive/indexes/$todaysdate

cd $MEME_HOME/archive/indexes/$todaysdate
tar -cvf $todaysdate.tar *


    set fileExists = `aws s3api list-objects-v2 --bucket nci-evs-meme --max-items 10 --prefix indexes/$dayofweek --output json | jq -r '.Contents | .[] |[.Key]' | grep $dayofweek | wc -l `
    if ($fileExists == 1) then
            echo "INFO: replacing $dayofweek indexes file"
	# remove last week's file from s3
        set lastWeekFile = `aws s3api list-objects-v2 --bucket nci-evs-meme --max-items 10 --prefix indexes/$dayofweek --output json | jq -r '.Contents | .[0] |[.Key][0]' | grep $dayofweek |  perl -pe 's/"//g;'`
	echo "INFO: lastWeekFile to remove: $lastWeekFile"
	aws s3 rm $S3_BUCKET/$lastWeekFile
    else
        echo "INFO: no previous $dayofweek indexes file"
    endif
    aws s3 cp $todaysdate.tar $S3_BUCKET/indexes/$todaysdate.tar

    rm $todaysdate.tar
    rm -rf $MEME_HOME/archive/indexes/$todaysdate


else
    echo "  DISABLED"
endif

echo "--------------------------------------------------------"
echo "Finished ... `/bin/date`"
echo "--------------------------------------------------------"
