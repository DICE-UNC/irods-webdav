FROM tomcat:jre8-alpine
LABEL organization="RENCI"
LABEL maintainer="michael_conway@unc.edu"
LABEL description="iRODS Core REST API."
ADD target/irods-webdav.war /usr/local/tomcat/webapps/
ADD runit.sh /
CMD ["/runit.sh"]
#CMD ["sh"]
# build: docker build -t diceunc/webdav:4.1.10.0-beta .

# run:  docker run -i -t --rm -p 8080:8080 -v /etc/irods-ext:/etc/irods-ext  --add-host irods419.irodslocal:172.16.250.100 diceunc/webdav:4.1.10.0-RC1

# run:  docker run -d --rm -p 8080:8080 -v /etc/irods-ext:/etc/irods-ext -v /home/mconway/webdavcert:/tmp/cert  --add-host irods419.irodslocal:172.16.250.100 diceunc/webdav:4.1.10.0-beta
