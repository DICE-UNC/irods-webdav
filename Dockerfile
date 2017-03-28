FROM tomcat:jre8-alpine
LABEL organization="RENCI"
LABEL maintainer="michael_conway@unc.edu"
LABEL description="iRODS Core REST API."
ADD target/irods-webdav.war /usr/local/tomcat/webapps/
ADD runit.sh /
CMD ["/runit.sh"]
#CMD ["sh"]
# build: docker build -t diceunc/webdav:4.2.0.0-SNAPSHOT .

# run:  docker run -d --rm -p 8080:8080 -v /etc/irods-ext:/etc/irods-ext  --add-host irods420.irodslocal:172.16.250.101 diceunc/webdav:4.2.0.0-SNAPSHOT

# docker run -d --rm -p 8080:8080 -v /etc/irods-ext:/etc/irods-ext  --add-host irods.data2discovery.org:152.54.2.71 diceunc/webdav:4.2.0.0-SNAPSHOT


# run:  docker run -d --rm -p 8080:8080 -v /etc/irods-ext:/etc/irods-ext -v /home/mconway/webdavcert:/tmp/cert  --add-host irods420.irodslocal:172.16.250.101 diceunc/webdav:4.2.0.0-SNAPSHOT
