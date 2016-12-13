SETUP

# Create a "mysqldev" alias in .cshrc for connecting to the database

mkdir /meme_work/ncim
cd /meme_work/ncim
mkdir config data
git clone https://github.com/WestCoastInformatics/UMLS-Terminology-Server.git code

cd /meme_work/ncim/code
git pull
mvn -Dconfig.artifactId=term-server-config-prod-nci-meta clean install

# unpack sample data
cd /meme_work/ncim/code
unzip /meme_work/ncim/code/config/target/term*.zip -d /meme_work/ncim/data

# unpack config and scripts
cd /meme_work/ncim
unzip /meme_work/ncim/code/config/prod-nci-meta/target/term*.zip -d config
ln -s config/bin
cp config/config.properties config/config-load.properties
# edit config.properties, the -load version uses DEFAULT security
# set the NLM license key

# run load script (after creating DB)
cd /meme_work/ncim
echo "CREATE database ncimdb CHARACTER SET utf8 default collate utf8_unicode_ci;" | mysqlg
bin/load.csh

# Check QA after the load
cd /meme_work/ncim/code/admin/qa
mvn install -PDatabase -Drun.config.umls=/home//config/config-load.properties


# edit /etc/tomcat8/tomcat8.conf file
# add a -Drun.config.ncim property to the JAVA_OPTS property setting that points
# to the /meme_work/ncim/config/config.properties file


COPYING DATA

Use the /meme_work/ncim/bin/prepIntegrationTest.csh script

INTEGRATION TEST

cd /meme_work/ncim/code/integration-test
mvn install -Pjpa,rest -Drun.config.umls=/meme_work/ncim/config/config.properties


REDEPLOY INSTRUCTIONS

cd /meme_work/ncim/code
git pull
mvn clean install -Drun.config.label=ncim -Dconfig.artifactId=term-server-config-prod-nci-meta 

/bin/rm -rf /var/lib/tomcat8/work/Catalina/localhost/ncim-server-rest
/bin/rm -rf /var/lib/tomcat8/webapps/ncim-server-rest
/bin/rm -rf /var/lib/tomcat8/webapps/ncim-server-rest.war
/bin/cp -f /meme_work/ncim/code/rest/target/umls-server-rest*war /var/lib/tomcat8/webapps/ncim-server-rest.war
