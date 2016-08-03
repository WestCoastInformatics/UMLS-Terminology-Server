SETUP

mkdir ~/vet
cd ~/vet
mkdir config data
git clone https://github.com/WestCoastInformatics/UMLS-Terminology-Server.git code

cd ~/vet/code
git pull
mvn -Dconfig.artifactId=term-server-config-prod-vet clean install

# unpack data (may also have get specific data in to this dir)
cd ~/vet/code
unzip ~/vet/code/config/target/term*.zip -d ~/vet/data

# unpack config and scripts
cd ~/vet
unzip ~/vet/code/config/prod-vet/target/term*.zip -d config
ln -s config/bin
cp config/config.properties config/config-load.properties
# edit config.properties, set NLM license and security=UTS
# the -load version uses DEFAULT security

# run load script (after creating DB)
cd ~/vet
echo "CREATE database vetdb CHARACTER SET utf8 default collate utf8_unicode_ci;" | mysqlotf
bin/load.csh

COPYING DATA

* Undeploy and start maintenance page
/bin/rm -rf /var/lib/tomcat8/work/Catalina/localhost/vet-server-rest
/bin/rm -rf /var/lib/tomcat8/webapps/vet-server-rest
/bin/rm -rf /var/lib/tomcat8/webapps/vet-server-rest.war
/opt/maint/getMaintHtml.sh start vet

b.terminology.tools:/home/ec2-tomcat/vet:ec2-tomcat% cat README.txt
SETUP

mkdir ~/vet
cd ~/vet
mkdir config data
git clone https://github.com/WestCoastInformatics/UMLS-Terminology-Server.git code

cd ~/vet/code
git pull
mvn -Dconfig.artifactId=term-server-config-prod-vet clean install

# unpack data (may also have get specific data in to this dir)
cd ~/vet/code
unzip ~/vet/code/config/target/term*.zip -d ~/vet/data

# unpack config and scripts
cd ~/vet
unzip ~/vet/code/config/prod-vet/target/term*.zip -d config
ln -s config/bin
cp config/config.properties config/config-load.properties
# edit config.properties, set NLM license and security=UTS
# the -load version uses DEFAULT security

# run load script (after creating DB)
cd ~/vet
echo "CREATE database vetdb CHARACTER SET utf8 default collate utf8_unicode_ci;" | mysqlotf
bin/load.csh

COPYING DATA

* Undeploy and start maintenance page
/bin/rm -rf /var/lib/tomcat8/work/Catalina/localhost/vet-server-rest
/bin/rm -rf /var/lib/tomcat8/webapps/vet-server-rest
/bin/rm -rf /var/lib/tomcat8/webapps/vet-server-rest.war
/opt/maint/getMaintHtml.sh start vet

# load data
cd ~/vet/data
wget https://s3.amazonaws.com/wci1/TermServer/vet-sql.zip
unzip vet-sql.zip
mysqlv < `find ~/vet/code -name "truncate_all.sql" | head -1`
mysqlv < ~/vet/data/vet.sql
mysqlv < ~/fixWindowsExportData.sql

# download and unpack indexes
cd /var/lib/tomcat8/indexes
/bin/rm -rf vet/*
wget https://s3.amazonaws.com/wci1/TermServer/vet-indexes.zip
unzip vet-indexes.zip

# Deploy and remove maintenance page
/bin/cp -f ~/snomed/code/rest/target/umls-server-rest*war /var/lib/tomcat8/webapps/vet-server-rest.war
/opt/maint/getMaintHtml.sh stop vet

# Remember to remove vet.sql when finished (it takes a lot of space)


REDEPLOY INSTRUCTIONS

cd ~/vet/code
git pull
mvn -Drun.config.label=vet -Dconfig.artifactId=term-server-config-prod-vet clean install

/bin/rm -rf /var/lib/tomcat8/work/Catalina/localhost/vet-server-rest
/bin/rm -rf /var/lib/tomcat8/webapps/vet-server-rest
/bin/rm -rf /var/lib/tomcat8/webapps/vet-server-rest.war
/bin/cp -f ~/vet/code/rest/target/umls-server-rest*war /var/lib/tomcat8/webapps/vet-server-rest.war

