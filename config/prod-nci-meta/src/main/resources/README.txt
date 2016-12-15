TOMCAT SETUP (as tomcata)
* edit /local/content/tomcat/meme-8080/bin/setenv.sh
** add -Drun.config.umls=/meme_work/ncim/config/config.properties
** add -Xms4G -Xmx15G

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
cd /meme_work/ncim/data
wget https://wci1.s3.amazonaws.com/TermServer/SAMPLE_NCI.zip
unzip SAMPLE_NCI.zip

cd /meme_work/ncim/code
unzip config/target/term*zip -d /meme_work/ncim/data

# unpack config and scripts
cd /meme_work/ncim
unzip /meme_work/ncim/code/config/prod-nci-meta/target/term*.zip -d config
ln -s config/bin
cp config/config.properties config/config-load.properties
# edit config.properties, the -load version uses DEFAULT security
# set the NLM license key

RESTORE DEV

cd /meme_work/ncim/code/integration-test
mvn install -Preset -Drun.config.umls=/meme_work/ncim/config/config.properties \
  -Dinput.dir=/meme_work/ncim/data/SAMPLE_NCI


REDEPLOY INSTRUCTIONS

cd /meme_work/ncim/code
git pull
mvn clean install -Drun.config.label=ncim -Dconfig.artifactId=term-server-config-prod-nci-meta 

/bin/rm -rf /var/lib/tomcat8/work/Catalina/localhost/ncim-server-rest
/bin/rm -rf /var/lib/tomcat8/webapps/ncim-server-rest
/bin/rm -rf /var/lib/tomcat8/webapps/ncim-server-rest.war
/bin/cp -f /meme_work/ncim/code/rest/target/umls-server-rest*war /var/lib/tomcat8/webapps/ncim-server-rest.war
