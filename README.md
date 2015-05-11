UMLS Terminology Server
=========================

This is a generic terminology server back end project.

This project hosts a basic UI that calls a set of REST APIs built around 
a UMLS data model. The API is fully documented with Swagger (http://swagger.io)


A reference deployment of the system exists here:
https://umls.terminology.tools/

Project Structure
-----------------

* top-level: aggregator for sub-modules:
  * admin: admin tools as maven plugins and poms
  * config: sample config files and data for windows dev environment and the reference deployment.
  * custom: sample code for customizing this tool
  * integration-test: integration tests (JPA, REST, and mojo)
  * jpa-model: a JPA enabled implementation of "model"
  * jpa-services: a JPA enabled implementation of "services"
  * model: interfaces representing the RF2 domain model
  * parent: parent project for managing dependency versions.
  * rest: the REST service implementation
  * rest-client: a Java client for the REST services
  * services: interfaces representing the service APIs

Documentation
-------------
Find comprehensive documentation here: http://wiki.terminology.tools/confluence/display/UTS/UMLS+Terminology+Server+Home

License
-------
See the included LICENSE.txt file.


Note about Dependencies
-----------------------
The "custom" package contains a reference to an LVG library.  In order to
properly resolve this, you must download and install the 2014 edition of LVG
and use Maven to install the .jar file to your local repository, e.g.

mvn install:install-file -Dfile=lvg2014dist.jar -DgroupId=gov.nih.nlm.lvg 
      -DartifactId=lvgdist -Dversion=2014 -Dpackaging=jar
      
Alternatively, you could simply remove "custom" from the "modules" section
of the top-level pom.

We hope that LVG will soon be available as a standard Maven artifact.



  