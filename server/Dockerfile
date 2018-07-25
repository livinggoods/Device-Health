FROM tomcat:9.0.10-jre8-alpine

MAINTAINER LG

RUN apk update && apk add

WORKDIR /usr/local/tomcat

COPY docker-entrypoint.sh /usr/local/bin/

RUN ["chmod", "+x","/usr/local/bin/docker-entrypoint.sh"]

ENTRYPOINT ["sh","/usr/local/bin/docker-entrypoint.sh"]

EXPOSE 8080

ENTRYPOINT ["catalina.sh", "run"]