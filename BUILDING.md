README

For notes on building with milton-ce (for WebDav1) versus milton-enterprise, see

https://github.com/DICE-UNC/irods-webdav/wiki/Developer-Notes

The codebase assumes that a Milton key is provided in the settings.xml file for the maven build process.  It is suggested that the drop-in .war deployment process is followed, which is described in the INSTALL.md file.

Note that DFC and the iRODS Consortium have an OEM license to pre-build the .war file.  Configuration has been moved to an /etc/irods-ext/irods-webdav.properties file, and a sample is included in this repo source tree.
