#!/bin/csh -f
#
# Nightly tasks
# 1. Bounce server (if switch is on)
#

# set MEME_HOME 
set rootdir = `dirname $0`
set abs_rootdir = `cd $rootdir && pwd`
setenv MEME_HOME $abs_rootdir:h


echo "--------------------------------------------------------"
echo "Starting `/bin/date`"
echo "--------------------------------------------------------"
echo "Collect settings..."
set host = `grep 'javax.persistence.jdbc.url' $MEME_HOME/config/config.properties | perl -ne '@_ = split/=/; $_[1] =~ /jdbc:mysql:\/\/(.*):(\d*)\/(.*)\?/; print "$1"'`
set port = `grep 'javax.persistence.jdbc.url' $MEME_HOME/config/config.properties | perl -ne '@_ = split/=/; $_[1] =~ /jdbc:mysql:\/\/(.*):(\d*)\/(.*)\?/; print "$2"'`
set db = `grep 'javax.persistence.jdbc.url' $MEME_HOME/config/config.properties | perl -ne '@_ = split/=/; $_[1] =~ /jdbc:mysql:\/\/(.*):(\d*)\/(.*)\?/; print "$3"'`
set user = `grep 'javax.persistence.jdbc.user' $MEME_HOME/config/config.properties | perl -ne '@_ = split/=/; print $_[1];'`
set pwd = `grep 'javax.persistence.jdbc.password' $MEME_HOME/config/config.properties | perl -ne '@_ = split/=/; print $_[1];'`
set mysql = "mysql -h$host -P$port -u$user -p$pwd $db"
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
	
	# Login as admin
	echo "  Login ... `/bin/date`"
	set authToken = `curl -H "Content-type: text/plain" -X POST -d "$adminPwd" $url/security/authenticate/$adminUser  | perl -pe 's/.*"authToken":"([^"]*).*/$1/;'`
	 	
    # find the "validation" process
    # TODO: select id from processConfig where project_id=... and name = 'VALIDATE_MID';

    # TODO: execute
else
    echo "  DISABLED"
endif

echo "--------------------------------------------------------"
echo "Finished
echo "--------------------------------------------------------"



