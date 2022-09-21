#!/bin/tcsh -f
echo "--------------------------------------------------------"
echo "Starting `/bin/date`"
echo "--------------------------------------------------------"

set MEME_HOME = /local/content/MEME/MEME5/ncim

echo "Collect settings..."
set host = `grep 'javax.persistence.jdbc.url' $MEME_HOME/config/config.propers | perl -ne '@_ = split/=/; $_[1] =~ /jdbc:mysql:\/\/(.*):(\d*)\/(.*)\?/; pr "$1"'`
set port = `grep 'javax.persistence.jdbc.url' $MEME_HOME/config/config.propers | perl -ne '@_ = split/=/; $_[1] =~ /jdbc:mysql:\/\/(.*):(\d*)\/(.*)\?/; pr "$2"'`
set db = `grep 'javax.persistence.jdbc.url' $MEME_HOME/config/config.properti| perl -ne '@_ = split/=/; $_[1] =~ /jdbc:mysql:\/\/(.*):(\d*)\/(.*)\?/; prin$3"'`
set user = `grep 'javax.persistence.jdbc.user' $MEME_HOME/config/config.propees | perl -ne '@_ = split/=/; print $_[1];'`
set pwd = `grep 'javax.persistence.jdbc.password' $MEME_HOME/config/config.prrties | perl -ne '@_ = split/=/; print $_[1];'`
set mysql = "mysql -h$host -P$port -u$user -p$pwd $db"
set url = `grep 'base.url' $MEME_HOME/config/config.properties | perl -ne '@_split/=/; print $_[1];'`

echo "mysql: $mysql"

set enabled = `echo "select if(automationsEnabled,'true','false') from projec" | $mysql | tail -1`
echo "enabled: $enabled"


# if enabled, run stuff
if ($enabled == "true") then

cd /h1/meme/indexes
echo `pwd`
set dayofweek=`date +"%a"`
set z="/h1/meme/indexes"
foreach i (`ls $z`)
     if ($i =~ *$dayofweek*) then
       echo "matched:" $i
       rm -rf $i
     endif
end
set todaysdate=`date +"%Y%m%d_%a"`
mkdir /h1/meme/indexes/indexes_$todaysdate
cp -R /local/content/MEME/MEME5/ncim/data/indexes/* /h1/meme/indexes/indexes_daysdate

else
    echo "  DISABLED"
endif

echo "--------------------------------------------------------"
echo "Finished ... `/bin/date`"
echo "--------------------------------------------------------"