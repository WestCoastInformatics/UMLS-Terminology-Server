UMLS Terminology Server
=========================

This is a UMLS terminology server back end project.

This project hosts a set of REST APIs with a Swagger implementation for online documentation of the services. It also contains a Java client for accessing RESET services.

A reference deployment of the REST API and Swagger documentation will soon be be found at:
http://umls.terminology.tools/term-server-rest/index.html

Project Structure
-----------------

* admin: admin tools as maven configurations
* config: sample config files and data
* integration-test: integration tests
* jpa-model: a JPA enabled implementation of "model"
* jpa-services: a JPA enabled implementation of "services"
* model: interfaces representing the domain model
* parent: parent project for managing dependency versions.
* rest: the REST service implementation
* rest-client: a Java client for the REST service
* services: interfaces representing the service APIs

Documentation
-------------
TBD


License
-------
See the included LICENSE.txt file.



