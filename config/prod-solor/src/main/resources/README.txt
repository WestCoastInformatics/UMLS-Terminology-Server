SETUP

mkdir ~/solor
cd ~/solor
mkdir config data
git clone https://github.com/WestCoastInformatics/UMLS-Terminology-Server.git code

cd ~/solor/code
git pull
mvn -Dconfig.artifactId=term-server-config-prod-solor clean install

# unpack sample data
cd ~/solor/code
unzip ~/solor/code/config/target/term*.zip -d ~/solor/data

# unpack config and scripts
cd ~/solor
unzip ~/solor/code/config/prod-solor/target/term*.zip -d config
ln -s config/bin

# Create database
echo "CREATE database solordb CHARACTER SET utf8 default collate utf8_bin;" | mysqlsol

# Check QA after the load
cd ~/solor/code/admin/qa
mvn install -PDatabase -Drun.config.solor=/home/ec2-tomcat/config/config.properties


# edit /etc/tomcat8/tomcat8.conf file
# add a -Drun.config.solor property to the JAVA_OPTS property setting that points
# to the /home/ec2-tomcat/solor/config/config.properties file


RELOADING DATA

* Undeploy and start maintenance page
/bin/rm -rf /var/lib/tomcat8/work/Catalina/localhost/solor-server-rest
/bin/rm -rf /var/lib/tomcat8/webapps/solor-server-rest
/bin/rm -rf /var/lib/tomcat8/webapps/solor-server-rest.war
/opt/maint/getMaintHtml.sh start solor

# deploy data
cd ~/solor/data
wget https://wci1.s3.amazonaws.com/TermServer/solor-sql.zip
mysqlsol < ~/solor/code/admin/mojo/src/main/resources/truncate_all.sql
unzip -p solor-sql.zip "solor.sql" | mysqlsol &
wait
mysqlsol < ~/fixWindowsExportData.sql
/bin/rm ~/solor/data/solor-sql.zip

# recompute indexes (make sure latest code is built)
/bin/rm -rf /var/lib/tomcat8/indexes/solor/*
cd ~/solor/code/admin/lucene
mvn install -PReindex  -Drun.config.umls=/home/ec2-tomcat/solor/config/config.properties >&! mvn.log &

# Deploy and remove maintenance page
/bin/cp -f ~/solor/code/rest/target/umls-server-rest*war /var/lib/tomcat8/webapps/solor-server-rest.war
/opt/maint/getMaintHtml.sh stop solor

# Remember to remove solor.sql when finished (it takes a lot of space)

REDEPLOY INSTRUCTIONS

cd ~/solor/code
git pull
mvn -Drun.config.label=solor -Dconfig.artifactId=term-server-config-prod-solor clean install

/bin/rm -rf /var/lib/tomcat8/work/Catalina/localhost/solor-server-rest
/bin/rm -rf /var/lib/tomcat8/webapps/solor-server-rest
/bin/rm -rf /var/lib/tomcat8/webapps/solor-server-rest.war
/bin/cp -f ~/solor/code/rest/target/umls-server-rest*war /var/lib/tomcat8/webapps/solor-server-rest.war

