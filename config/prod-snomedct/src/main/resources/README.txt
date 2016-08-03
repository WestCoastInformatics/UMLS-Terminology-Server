
REDEPLOY INSTRUCTIONS

cd ~/snomed/code
git pull
mvn -Dconfig.artifactId=term-server-config-prod-snomedct clean install

/bin/rm -rf /var/lib/tomcat8/work/Catalina/localhost/snomed-server-rest
/bin/rm -rf /var/lib/tomcat8/webapps/snomed-server-rest
/bin/rm -rf /var/lib/tomcat8/webapps/snomed-server-rest.war
/bin/cp -f ~/snomed/code/rest/target/umls-server-rest*war /var/lib/tomcat8/webapps/snomed-server-rest.war

a.terminology.tools:/home/ec2-tomcat/snomed:ec2-tomcat% /opt/maint/getMaintHtml.sh start snomed
Action = start
Type = snomed
a.terminology.tools:/home/ec2-tomcat/snomed:ec2-tomcat% cat RE
README.txt~  README.txt
a.terminology.tools:/home/ec2-tomcat/snomed:ec2-tomcat% cat README.txt
SETUP

mkdir ~/snomed
cd ~/snomed
mkdir config data
git clone https://github.com/WestCoastInformatics/UMLS-Terminology-Server.git code

cd ~/snomed/code
git pull
mvn -Dconfig.artifactId=term-server-config-prod-snomedct clean install

# unpack sample data
cd ~/snomed/code
unzip ~/snomed/code/config/target/term*.zip -d ~/snomed/data

# unpack config and scripts
cd ~/snomed
unzip ~/snomed/code/config/prod-snomedct/target/term*.zip -d config
ln -s config/bin

# Create database
echo "CREATE database snomeddb CHARACTER SET utf8 default collate utf8_unicode_ci;" | mysqls

# Check QA after the load
cd ~/snomed/code/admin/qa
mvn install -PDatabase -Drun.config.snomed=/home/ec2-tomcat/config/config.properties


# edit /etc/tomcat8/tomcat8.conf file
# add a -Drun.config.snomed property to the JAVA_OPTS property setting that points
# to the /home/ec2-tomcat/snomed/config/config.properties file


RELOADING DATA

* Undeploy and start maintenance page
/bin/rm -rf /var/lib/tomcat8/work/Catalina/localhost/snomed-server-rest
/bin/rm -rf /var/lib/tomcat8/webapps/snomed-server-rest
/bin/rm -rf /var/lib/tomcat8/webapps/snomed-server-rest.war
/opt/maint/getMaintHtml.sh start snomed

# deploy data
cd ~/snomed/data
wget https://wci1.s3.amazonaws.com/TermServer/snomed-sql.zip
unzip snomed-sql.zip
mysqls < ~/snomed/code/admin/mojo/src/main/resources/truncate_all.sql
mysqls < snomed.sql
mysqls < ~/fixWindowsExportData.sql
/bin/rm ~/snomed/data/snomed.sql

# deploy indexes
cd ~/snomed/data
wget https://wci1.s3.amazonaws.com/TermServer/snomed-indexes.zip
/bin/rm -rf /var/lib/tomcat8/indexes/snomedct/*
unzip snomed-indexes.zip -d /var/lib/tomcat8/indexes

# Deploy and remove maintenance page
/bin/cp -f ~/snomed/code/rest/target/umls-server-rest*war /var/lib/tomcat8/webapps/snomed-server-rest.war
/opt/maint/getMaintHtml.sh stop snomed

# Remember to remove snomed.sql when finished (it takes a lot of space)

REDEPLOY INSTRUCTIONS

cd ~/snomed/code
git pull
mvn -Drun.config.label=ts -Dconfig.artifactId=term-server-config-prod-snomedct clean install

/bin/rm -rf /var/lib/tomcat8/work/Catalina/localhost/snomed-server-rest
/bin/rm -rf /var/lib/tomcat8/webapps/snomed-server-rest
/bin/rm -rf /var/lib/tomcat8/webapps/snomed-server-rest.war
/bin/cp -f ~/snomed/code/rest/target/umls-server-rest*war /var/lib/tomcat8/webapps/snomed-server-rest.war

