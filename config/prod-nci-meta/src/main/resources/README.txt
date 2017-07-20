TOMCAT SETUP (as tomcata)
* su - tomcata (password)
* TOMCAT_HOME = /local/content/tomcat/meme-8080
* APACHE_HOME = /local/content/apache
* edit /local/content/tomcat/meme-8080/bin/setenv.sh
** add -Drun.config.umls=/local/content/MEME/MEME5/ncim/config/config.properties
** add -Xms4G -Xmx15G
* webapps directory
** /local/content/tomcat/meme-8080/webapps
* Make the meme indexes directory writeable by all
chmod 777 

SETUP

# Create a "mysqldev" alias in .cshrc for connecting to the database

mkdir /local/content/MEME/MEME5/ncim
cd /local/content/MEME/MEME5/ncim
mkdir config data
git clone https://github.com/WestCoastInformatics/UMLS-Terminology-Server.git code

cd /local/content/MEME/MEME5/ncim/code
git pull
mvn -Dconfig.artifactId=term-server-config-prod-nci-meta clean install

# unpack sample data
cd /local/content/MEME/MEME5/ncim/data
wget https://wci1.s3.amazonaws.com/TermServer/SAMPLE_NCI.zip
unzip SAMPLE_NCI.zip

cd /local/content/MEME/MEME5/ncim/code
unzip config/target/term*zip -d /local/content/MEME/MEME5/ncim/data

# unpack config and scripts
cd /local/content/MEME/MEME5/ncim
unzip /local/content/MEME/MEME5/ncim/code/config/prod-nci-meta/target/term*.zip -d config
ln -s config/bin
cp config/config.properties config/config-load.properties
# edit config.properties, the -load version uses DEFAULT security
# set the NLM license key

RESTORE DEV (use default dir)

cd /local/content/MEME/MEME5/ncim/code/integration-test
mvn install -Preset-meta -Drun.config.umls=/local/content/MEME/MEME5/ncim/config/config.properties \
  -DskipTests=false -Dmaven.home=/h1/meme/apache-maven-3.3.9

REDEPLOY INSTRUCTIONS

cd /local/content/MEME/MEME5/ncim/code
git pull
mvn clean install -Drun.config.label=ncim -Dconfig.artifactId=term-server-config-prod-nci-meta 
chmod -R 777 /local/content/MEME/MEME5/ncim/data/indexes/

As tomcata:
/bin/rm -rf /local/content/tomcat/meme-8080/work/Catalina/localhost/ncim-server-rest
/bin/rm -rf /local/content/tomcat/meme-8080/webapps/ncim-server-rest
/bin/rm -rf /local/content/tomcat/meme-8080/webapps/ncim-server-rest.war
/bin/cp -f /local/content/MEME/MEME5/ncim/code/rest/target/umls-server-rest*war /local/content/tomcat/meme-8080/webapps/ncim-server-rest.war
