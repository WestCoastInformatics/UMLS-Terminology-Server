SETUP

mkdir ~/gene
cd ~/gene
mkdir config data
git clone https://github.com/WestCoastInformatics/UMLS-Terminology-Server.git code

cd ~/gene/code
git pull
mvn -Dconfig.artifactId=term-server-config-prod-gene clean install

# unpack sample data
cd ~/gene/code
unzip ~/gene/code/config/target/term*.zip -d ~/gene/data

# unpack config and scripts
cd ~/gene
unzip ~/gene/code/config/prod-gene/target/term*.zip -d config
ln -s config/bin
cp config/config.properties config/config-load.properties
# edit config.properties, the -load version uses DEFAULT security
# set the NLM license key

# run load script (after creating DB)
cd ~/gene
echo "CREATE database genedb CHARACTER SET utf8 default collate utf8_unicode_ci;" | mysqlg
bin/load.csh

# Check QA after the load
cd ~/gene/code/admin/qa
mvn install -PDatabase -Drun.config.umls=/home/ec2-tomcat/config/config-load.properties


# edit /etc/tomcat8/tomcat8.conf file
# add a -Drun.config.gene property to the JAVA_OPTS property setting that points
# to the /home/ec2-tomcat/gene/config/config.properties file


COPYING DATA

* undeploy and put up maintenance page
/opt/maint/getMaintHtml.sh start gene
/bin/rm -rf /var/lib/tomcat8/work/Catalina/localhost/gene-server-rest
/bin/rm -rf /var/lib/tomcat8/webapps/gene-server-rest
/bin/rm -rf /var/lib/tomcat8/webapps/gene-server-rest.war

wget https://s3.amazonaws.com/wci1/TermServer/gene-sql.zip
unzip gene-sql.zip
/bin/rm -f gene-sql.zip
mysqlg < ~/gene/code/admin/mojo/src/main/resources/truncate_all.sql
mysqlg < ~/gene/data/gene.sql
mysqlg < ~/fixWindowsExportData.sql

wget https://s3.amazonaws.com/wci1/TermServer/gene-indexes.zip
/bin/rm /var/lib/tomcat8/indexes/gene/*
unzip gene-indexes.zip -d /var/lib/tomcat8/indexes



* deploy and take down maintenance page
/bin/cp -f ~/gene/code/rest/target/gene-server-rest*war /var/lib/tomcat8/webapps/gene-server-rest.war
/opt/maint/getMaintHtml.sh stop gene

# Remember to remove snomed.sql when finished (it takes a lot of space)

REDEPLOY INSTRUCTIONS

cd ~/gene/code
git pull
mvn -Dconfig.artifactId=term-server-config-prod-gene clean install

/bin/rm -rf /var/lib/tomcat8/work/Catalina/localhost/gene-server-rest
/bin/rm -rf /var/lib/tomcat8/webapps/gene-server-rest
/bin/rm -rf /var/lib/tomcat8/webapps/gene-server-rest.war
/bin/cp -f ~/gene/code/rest/target/gene-server-rest*war /var/lib/tomcat8/webapps/gene-server-rest.war
