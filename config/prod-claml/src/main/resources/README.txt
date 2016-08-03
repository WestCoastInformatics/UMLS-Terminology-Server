SETUP

mkdir ~/claml
cd ~/claml
mkdir config data
git clone https://github.com/WestCoastInformatics/UMLS-Terminology-Server.git code

cd ~/claml/code
git pull
mvn -Dconfig.artifactId=term-server-config-prod-claml clean install

# unpack sample data
cd ~/claml/code
unzip ~/claml/code/config/target/term*.zip -d ~/claml/data

# unpack config and scripts
cd ~/claml
unzip ~/claml/code/config/prod-claml/target/term*.zip -d config
ln -s config/bin
cp config/config.properties config/config-load.properties
# edit config.properties, the -load version uses DEFAULT security
# set the NLM license key

# run load script (after creating DB)
cd ~/claml
echo "CREATE database clamldb CHARACTER SET utf8 default collate utf8_unicode_ci;" | mysqlc
bin/load.csh

# Check QA after the load
cd ~/claml/code/admin/qa
mvn install -PDatabase -Drun.config.claml=/home/ec2-tomcat/config/config-load.properties


# edit /etc/tomcat8/tomcat8.conf file
# add a -Drun.config.claml property to the JAVA_OPTS property setting that points
# to the /home/ec2-tomcat/claml/config/config.properties file


RELOADING DATA

cd ~/claml/code
git pull
mvn -Dconfig.artifactId=term-server-config-prod-claml clean install

* undeploy app and put up maintenance page
/bin/rm -rf /var/lib/tomcat8/work/Catalina/localhost/claml-server-rest
/bin/rm -rf /var/lib/tomcat8/webapps/claml-server-rest
/bin/rm -rf /var/lib/tomcat8/webapps/claml-server-rest.war
/opt/maint/getMaintHtml.sh start claml

cd ~/claml/bin
./load.csh >&! load.log

* deploy app and take down  maintenance page
/bin/cp -f ~/claml/code/rest/target/umls-server-rest*war /var/lib/tomcat8/webapps/claml-server-rest.war
/opt/maint/getMaintHtml.sh stop claml


REDEPLOY INSTRUCTIONS

cd ~/claml/code
git pull
mvn -Dconfig.artifactId=term-server-config-prod-claml clean install

/bin/rm -rf /var/lib/tomcat8/work/Catalina/localhost/claml-server-rest
/bin/rm -rf /var/lib/tomcat8/webapps/claml-server-rest
/bin/rm -rf /var/lib/tomcat8/webapps/claml-server-rest.war
/bin/cp -f ~/claml/code/rest/target/umls-server-rest*war /var/lib/tomcat8/webapps/claml-server-rest.war

a.terminology.tools:/home/ec2-tomcat/claml:ec2-tomcat% mv README  README.txt
a.terminology.tools:/home/ec2-tomcat/claml:ec2-tomcat% cat README.txt
SETUP

mkdir ~/claml
cd ~/claml
mkdir config data
git clone https://github.com/WestCoastInformatics/UMLS-Terminology-Server.git code

cd ~/claml/code
git pull
mvn -Dconfig.artifactId=term-server-config-prod-claml clean install

# unpack sample data
cd ~/claml/code
unzip ~/claml/code/config/target/term*.zip -d ~/claml/data

# unpack config and scripts
cd ~/claml
unzip ~/claml/code/config/prod-claml/target/term*.zip -d config
ln -s config/bin
cp config/config.properties config/config-load.properties
# edit config.properties, the -load version uses DEFAULT security
# set the NLM license key

# run load script (after creating DB)
cd ~/claml
echo "CREATE database clamldb CHARACTER SET utf8 default collate utf8_unicode_ci;" | mysqlc
bin/load.csh

# Check QA after the load
cd ~/claml/code/admin/qa
mvn install -PDatabase -Drun.config.claml=/home/ec2-tomcat/config/config-load.properties


# edit /etc/tomcat8/tomcat8.conf file
# add a -Drun.config.claml property to the JAVA_OPTS property setting that points
# to the /home/ec2-tomcat/claml/config/config.properties file


RELOADING DATA

cd ~/claml/code
git pull
mvn -Drun.config.label=claml -Dconfig.artifactId=term-server-config-prod-claml clean install

* undeploy app and put up maintenance page
/bin/rm -rf /var/lib/tomcat8/work/Catalina/localhost/claml-server-rest
/bin/rm -rf /var/lib/tomcat8/webapps/claml-server-rest
/bin/rm -rf /var/lib/tomcat8/webapps/claml-server-rest.war
/opt/maint/getMaintHtml.sh start claml

cd ~/claml/bin
./load.csh >&! load.log

* deploy app and take down  maintenance page
/bin/cp -f ~/claml/code/rest/target/umls-server-rest*war /var/lib/tomcat8/webapps/claml-server-rest.war
/opt/maint/getMaintHtml.sh stop claml


REDEPLOY INSTRUCTIONS

cd ~/claml/code
git pull
mvn -Dconfig.artifactId=term-server-config-prod-claml clean install

/bin/rm -rf /var/lib/tomcat8/work/Catalina/localhost/claml-server-rest
/bin/rm -rf /var/lib/tomcat8/webapps/claml-server-rest
/bin/rm -rf /var/lib/tomcat8/webapps/claml-server-rest.war
/bin/cp -f ~/claml/code/rest/target/umls-server-rest*war /var/lib/tomcat8/webapps/claml-server-rest.war
