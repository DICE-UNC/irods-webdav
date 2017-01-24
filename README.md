# Project: iRODS WebDav
## Date: 
## Release Verson: 4.1.10-SNAPSHOT
## Git tag: 


https://github.com/DICE-UNC/irods-webdav

Milton based WebDav interface to iRODS.  This is differentiated from the ModeShape approach by providing a lightweight, no cached implementation that maps web requests directly to iRODS accounts.

See https://github.com/DICE-UNC/irods-webdav/issues for support and known issues

See the INSTALL.md file in this repo for notes on install and configuration.

## NOTE: ##

This version does not enforce a cap on max file upload, that will be in the next release.  It does honor the max file download size.

### Requirements

* Depends on Java 1.8+
* Built using Apache Maven2, see POM for dependencies
* Built using a Milton.io webdav enterprise version key

### Bug Fixes

### Features

#### Initial release

This is the initial release of iRODS WebDav support using Milton, a lightweight WebDav library.  DFC and the iRODS Consortium, in collaboration, are providing a WebDav connector that includes the Milton 'enterprise' license keys in a pre-packaged deployment supporting WebDav level 2.

####  Milton and filter chains #33 

Fixed filter chain behavior to close connections properly in iRODS agent.  Milton filter was intercepting and not calling the shutdown filter in the chain

####  delete of a file via finder doesn't seem to stick #27 

Milton configuration and code to ensure that Dav2 support is configured in deployable

#### SSL integration #38

Integrate SSL negotiation support.  Note that this requires an update to the /etc/irods-ext/irods-webdav.properties.  See the example for new fields (for checksum and SSL negotiation).