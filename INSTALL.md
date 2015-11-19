# Install notes

RENCI and DFC have arranged to OEM the milton 'enterprise' libraries as part of iRODS WebDav support, providing full WebDav 2
capability, free for you to deploy and use with your iRODS grid.  Therefore, we have developed a drop-in .war file that contains
all of the necessary compiled code and keys, and exported the configuration to /etc/irods-ext/irods-webdav.properties.

Install is thus rather straight forward:

* Install the release .war file on your container (e.g. Tomcat)

* Copy the irods-webdav.properties to /etc/irods-ext and make it readable by the tomcat or other container service user

* Configure irods-webdav.properties to your particular grid.  Note that WebDav uses a preset host/port/zone and translates the Basic Authentication credentials of the user to set the logged in account

Note that WebDav needs to be at a root url (e.g. http://mywebdav.org versus http://mywebdav.org/somecontext) at port 80, or with https:// on the standard port.  If it is not thus configured, certain
clients may not properly connect (such as Mac Finder or Windows Explorer).  

See the [compatability notes](http://milton.io/guide/compat/index.html) at Milton for tips




