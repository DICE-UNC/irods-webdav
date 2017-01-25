# Project: iRODS WebDav
## Date: 1/25/2017
## Release Verson: 4.1.10.0-beta
## Git tag: 4.1.10.0-beta


https://github.com/DICE-UNC/irods-webdav

Milton based WebDav interface to iRODS.  

See https://github.com/DICE-UNC/irods-webdav/issues for support and known issues

See the INSTALL.md file in this repo for notes on install and configuration.


### Requirements

* Depends on Java 1.8+
* Built using Apache Maven2, see POM for dependencies
* Built using a Milton.io webdav enterprise version key
* Supports iRODS 3.3.1 through 4.1.10, with provisional support for 4.2

### Bug Fixes

### Features


####  Milton and filter chains #33 

Fixed filter chain behavior to close connections properly in iRODS agent.  Milton filter was intercepting and not calling the shutdown filter in the chain

####  delete of a file via finder doesn't seem to stick #27 

Milton configuration and code to ensure that Dav2 support is configured in deployable

#### SSL integration #38

Integrate SSL negotiation support.  Note that this requires an update to the /etc/irods-ext/irods-webdav.properties.  See the example for new fields (for checksum and SSL negotiation).
 
####  Dockerization #39 

Added Docker containerization, with an included Docker file, as well as a Docker.md file that explains how to run the image.  