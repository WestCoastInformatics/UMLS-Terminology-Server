README

These scripts allow you to download files that require UMLS Terminology Services (UTS) authentication by executing a single command. When combined with a scheduling tool, they allow you to automate the download of files.

These scripts download files into your current working directory. If you want the file saved in a different directory, make sure you change your current working directory before running the scripts.

----------------------------------------

For Windows users:

1) curl is not installed by default. Download and install curl. Contact your system administrator for assistance, if necessary.

2) Unzip terminology_download_script.zip to some location. (For example, c:\terminology_download_script)

3) Open curl-uts-download.bat in an text editor and enter your UTS username and password and the path to curl: (Do not include spaces or quotation marks.)
           SET UTS_USERNAME=
           SET UTS_PASSWORD=
           SET CURL_HOME= (path where curl is installed in Step 1)

4) Using the command line, navigate to the terminology_download_script directory:
        > cd c:\terminology_download_script\

5)  Run curl-uts-download.bat with the appropriate download file URL: (Replace the RxNorm URL example with URL of the file you wish to download.)
        > curl-uts-download.bat http://download.nlm.nih.gov/umls/kss/rxnorm/RxNorm_full_current.zip
        
*To schedule a download you may run the above command using the Windows task scheduler or another scheduling tool. 

----------------------------------------

For Mac/Linux/Solaris users:

1) curl is installed by default. (If it is not present you need to install it yourself or contact your system administrator.)

2) Unzip terminology_download_script.zip some location. (For example, /tmp)

3) Open curl-uts-download.sh in a text editor and enter your UTS username and password: (Do not include spaces or quotation marks.)
           export UTS_USERNAME=
           export UTS_PASSWORD=

4) Using the command line, navigate to the download-with-curl directory:
           > cd /tmp/terminology_download_script

5) Make sure the curl-uts-download.sh file is executable by modifying its permissions:
   	   > chmod 755 curl-uts-download.sh

6) Run curl-uts-download.sh with the appropriate download file URL: (Replace the RxNorm URL example with URL of the file you wish to download.)
           > sh curl-uts-download.sh http://download.nlm.nih.gov/umls/kss/rxnorm/RxNorm_full_current.zip
        
*To schedule a download you may run the above command using crontab or another scheduling tool.

----------------------------------------

URLs for frequently downloaded files:

UMLS (Run curl-uts-download.sh for each file) -
http://download.nlm.nih.gov/umls/kss/2013AA/full/2013AA.CHK
http://download.nlm.nih.gov/umls/kss/2013AA/full/2013AA.MD5
http://download.nlm.nih.gov/umls/kss/2013AA/full/2013aa-1-meta.nlm
http://download.nlm.nih.gov/umls/kss/2013AA/full/2013aa-2-meta.nlm
http://download.nlm.nih.gov/umls/kss/2013AA/full/2013aa-otherks.nlm
http://download.nlm.nih.gov/umls/kss/2013AA/full/mmsys.zip
http://download.nlm.nih.gov/umls/kss/2013AA/full/Copyright_Notice.txt
http://download.nlm.nih.gov/umls/kss/2013AA/full/README.txt

U.S. Edition of SNOMED CT -
http://download.nlm.nih.gov/mlb/utsauth/USExt/SnomedCT_Release_US1000124_yyyymmdd.zip

RxNorm Full Monthly Release -
http://download.nlm.nih.gov/umls/kss/rxnorm/RxNorm_full_mmddyyyy.zip
OR
http://download.nlm.nih.gov/umls/kss/rxnorm/RxNorm_full_current.zip

RxNorm Weekly Update - 
http://download.nlm.nih.gov/umls/kss/rxnorm/RxNorm_weekly_mmddyyyy.zip
OR
http://download.nlm.nih.gov/umls/kss/rxnorm/RxNorm_weekly_current.zip



For the full list of download file URLs, visit the downloads page:
http://nih.nih.gov/research/umls/licensedcontent/downloads.html
