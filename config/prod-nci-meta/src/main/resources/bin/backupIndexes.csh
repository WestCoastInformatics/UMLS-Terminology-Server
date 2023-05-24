#!/bin/tcsh -f
set rootdir = `dirname $0`
set abs_rootdir = `cd $rootdir && pwd`
setenv MEME_HOME $abs_rootdir:h

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
set dayofweek=`date +"%a"`
set z="$MEME_HOME/archive/indexes"
foreach i (`ls $z`)
     if ($i =~ *$dayofweek*) then
       echo "matched:" $i
       rm -rf $i
     endif
end
set todaysdate=`date +"%Y%m%d_%a"`
mkdir $MEME_HOME/archive/indexes/indexes_$todaysdate
cp -R /local/content/MEME/MEME5/ncim/data/indexes/* $MEME_HOME/archive/indexes/indexes_$todaysdate

else
    echo "  DISABLED"
endif

echo "--------------------------------------------------------"
echo "Finished ... `/bin/date`"
echo "--------------------------------------------------------"
