# Project: iRODS WebDav
## Date: 12/01/2015
## Release Version: 4.0.2.5-RC1
## Git tag: 4.0.2.5-RC1


https://github.com/DICE-UNC/irods-webdav

Milton based WebDav interface to iRODS.  This is differentiated from the ModeShape approach by providing a lightweight, no cached implementation that maps web requests directly to iRODS accounts.

See https://github.com/DICE-UNC/irods-webdav/issues for support and known issues

See the INSTALL.md file in this repo for notes on install and configuration.

## NOTE: ##

This is a release candidate, and thus has INFO level logging enabled, this will potentially generate a lot of data, and will be dialed back for release.

This version does not enforce a cap on max file upload, that will be in the next release.  It does honor the max file download size.

### Requirements

* Depends on Java 1.8+
* Built using Apache Maven2, see POM for dependencies
* Built using a Milton.io webdav enterprise version key

### Bug Fixes

### Features

#### Initial release

This is the initial release of iRODS WebDav support using Milton, a lightweight WebDav library.  DFC and the iRODS Consortium, in collaboration, are providing a WebDav connector that includes the Milton 'enterprise' license keys in a pre-packaged deployment supporting WebDav level 2.