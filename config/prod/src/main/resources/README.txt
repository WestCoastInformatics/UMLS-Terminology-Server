SETUP

mkdir ~/umls
cd ~/umls
mkdir config data
git clone https://github.com/WestCoastInformatics/UMLS-Terminology-Server.git code

cd ~/umls/code
git pull
mvn -Dconfig.artifactId=term-server-config-prod clean install

# unpack sample data
cd ~/umls/code
unzip ~/umls/code/config/target/term*.zip -d ~/umls/data

# unpack config and scripts
cd ~/umls
unzip ~/umls/code/config/prod/target/term*.zip -d config
ln -s config/bin
cp config/config.properties config/config-load.properties
# edit config.properties, the -load version uses DEFAULT security
# set the NLM license key

# run load script (after creating DB)
cd ~/umls
echo "CREATE database umlsdb CHARACTER SET utf8 default collate utf8_unicode_ci;" | mysqlu
bin/load.csh

# Check QA after the load
cd ~/umls/code/admin/qa
mvn install -PDatabase -Drun.config.umls=/home/ec2-tomcat/config/config-load.properties


# edit /etc/tomcat8/tomcat8.conf file
# add a -Drun.config.umls property to the JAVA_OPTS property setting that points
# to the /home/ec2-tomcat/umls/config/config.properties file


COPYING DATA

* undeploy and put up maintenance page
/opt/maint/getMaintHtml.sh start umls
/bin/rm -rf /var/lib/tomcat8/work/Catalina/localhost/umls-server-rest
/bin/rm -rf /var/lib/tomcat8/webapps/umls-server-rest
/bin/rm -rf /var/lib/tomcat8/webapps/umls-server-rest.war

wget https://s3.amazonaws.com/wci1/TermServer/umls-sql.zip
unzip umls-sql.zip
/bin/rm -f umls-sql.zip
mysqlu < code/admin/mojo/src/main/resources/truncate_all.sql
mysqlu < ~/umls/data/umls.sql
mysqlu < ~/fixWindowsExportData.sql

wget https://s3.amazonaws.com/wci1/TermServer/umls-indexes.zip
/bin/rm

* deploy and take down maintenance page
/bin/cp -f ~/umls/code/rest/target/umls-server-rest*war /var/lib/tomcat8/webapps/umls-server-rest.war
/opt/maint/getMaintHtml.sh stop umls

# Remember to remove snomed.sql when finished (it takes a lot of space)

REDEPLOY INSTRUCTIONS

cd ~/umls/code
git pull
mvn -Dconfig.artifactId=term-server-config-prod clean install

/bin/rm -rf /var/lib/tomcat8/work/Catalina/localhost/umls-server-rest
/bin/rm -rf /var/lib/tomcat8/webapps/umls-server-rest
/bin/rm -rf /var/lib/tomcat8/webapps/umls-server-rest.war
/bin/cp -f ~/umls/code/rest/target/umls-server-rest*war /var/lib/tomcat8/webapps/umls-server-rest.war

