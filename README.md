* Project: iRODS Rest API
* Date: 9/17/2014 
* Release Version: 4.0.2.1-SNAPSHOT	
* Git tag: MASTER

https://github.com/DICE-UNC/irods-rest

iRODS Rest API based on Jargon 4.0.2, certified against iRODS 3.0+ as well as iRODS Consortium 4.0+ releases.  See included docs folder for comprehensive user documentation and install instructions

See https://github.com/DICE-UNC/irods-rest/issues for support and known issues


### Requirements

* Depends on Java 1.6+
* Built using Apache Maven2, see POM for dependencies


### Bug Fixes

### Features

#### #2 CORS header support

Add support for Cross Origin Resource Sharing through customizable configuration

#### Add PAM support #6

Add support for PAM authentication through customizable configuration

#### add temp password for user #10

New /user/userName/temppassword signatures available to obtain a temporary iRODS password, including in admin mode.
