#!/bin/bash
docker build -t bbund/device_health .
#docker run -d -p 8080:8080 --name device_health bbund/device_health
docker run -it --rm -p 8080:8080 -v $(pwd)/target/:/usr/local/tomcat/webapps/ -v $(pwd)/target/:/usr/local/tomcat/logs/ --name device_health bbund/device_health
