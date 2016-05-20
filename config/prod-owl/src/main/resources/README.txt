SETUP

mkdir ~/owl
cd ~/owl
mkdir config data
git clone https://github.com/WestCoastInformatics/UMLS-Terminology-Server.git code

cd ~/owl/code
git pull
mvn -Dconfig.artifactId=term-server-config-prod-owl clean install

# unpack sample data
cd ~/owl/code
unzip ~/owl/code/config/target/term*.zip -d ~/owl/data

# unpack config and scripts
cd ~/owl
unzip ~/owl/code/config/prod-owl/target/term*.zip -d config
ln -s config/bin
cp config/config.properties config/config-load.properties
# edit config.properties, set the NLM license key and security=UTS
# the -load version uses DEFAULT security

# run load script (after creating DB)
cd ~/owl
echo "CREATE database owldb CHARACTER SET utf8 default collate utf8_unicode_ci;" | mysqlotf
bin/load.csh

# Check QA after the load
cd ~/owl/code/admin/qa
mvn install -PDatabase -Drun.config.owl=/home/ec2-tomcat/owl/config/config-load.properties


# edit /etc/tomcat8/tomcat8.conf file
# add a -Drun.config.owl property to the JAVA_OPTS property setting that points
# to the /home/ec2-tomcat/owl/config/config.properties file


COPYING DATA
* put maintenance page and undeploy
/opt/maint/getMaintHtml.sh start owl
/bin/rm -rf /var/lib/tomcat8/work/Catalina/localhost/owl-server-rest
/bin/rm -rf /var/lib/tomcat8/webapps/owl-server-rest
/bin/rm -rf /var/lib/tomcat8/webapps/owl-server-rest.war


* sftp the file and put into ~/owl/data and unpack it (e.g. owl.sql)
mysqlo < owl.sql
mysqlo < ~/fixWindowsExportData.sql
cd ~/owl/code/admin
mvn install -PReindex -Drun.config.owl=/home/ec2-tomcat/owl/config/config-load.properties

* redeply and takedown maintenance page
/bin/cp -f ~/owl/code/rest/target/umls-server-rest*war /var/lib/tomcat8/webapps/owl-server-rest.war
/opt/maint/getMaintHtml.sh stop owl



REDEPLOY INSTRUCTIONS

cd ~/owl/code
git pull
mvn -Dconfig.artifactId=term-server-config-prod-owl clean install

/bin/rm -rf /var/lib/tomcat8/work/Catalina/localhost/owl-server-rest
/bin/rm -rf /var/lib/tomcat8/webapps/owl-server-rest
/bin/rm -rf /var/lib/tomcat8/webapps/owl-server-rest.war
/bin/cp -f ~/owl/code/rest/target/umls-server-rest*war /var/lib/tomcat8/webapps/owl-server-rest.war

