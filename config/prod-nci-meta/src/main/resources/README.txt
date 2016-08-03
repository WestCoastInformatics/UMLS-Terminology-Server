SETUP

mkdir ~/ncim
cd ~/ncim
mkdir config data
git clone https://github.com/WestCoastInformatics/UMLS-Terminology-Server.git code

cd ~/ncim/code
git pull
mvn -Dconfig.artifactId=term-server-config-prod-nci-meta clean install

# unpack sample data
cd ~/ncim/code
unzip ~/ncim/code/config/target/term*.zip -d ~/ncim/data

# unpack config and scripts
cd ~/ncim
unzip ~/ncim/code/config/prod-nci-meta/target/term*.zip -d config
ln -s config/bin
cp config/config.properties config/config-load.properties
# edit config.properties, the -load version uses DEFAULT security
# set the NLM license key

# run load script (after creating DB)
cd ~/ncim
echo "CREATE database ncimdb CHARACTER SET utf8 default collate utf8_unicode_ci;" | mysqlg
bin/load.csh

# Check QA after the load
cd ~/ncim/code/admin/qa
mvn install -PDatabase -Drun.config.umls=/home/ec2-tomcat/config/config-load.properties


# edit /etc/tomcat8/tomcat8.conf file
# add a -Drun.config.ncim property to the JAVA_OPTS property setting that points
# to the /home/ec2-tomcat/ncim/config/config.properties file


COPYING DATA

Use the ~/ncim/bin/prepIntegrationTest.csh script

INTEGRATION TEST

cd ~/ncim/code/integration-test
mvn install -Pjpa,rest -Drun.config.umls=/home/ec2-tomcat/ncim/config/config.properties


REDEPLOY INSTRUCTIONS

cd ~/ncim/code
git pull
mvn clean install -Drun.config.label=ncim -Dconfig.artifactId=term-server-config-prod-nci-meta 

/bin/rm -rf /var/lib/tomcat8/work/Catalina/localhost/ncim-server-rest
/bin/rm -rf /var/lib/tomcat8/webapps/ncim-server-rest
/bin/rm -rf /var/lib/tomcat8/webapps/ncim-server-rest.war
/bin/cp -f ~/ncim/code/rest/target/umls-server-rest*war /var/lib/tomcat8/webapps/ncim-server-rest.war
