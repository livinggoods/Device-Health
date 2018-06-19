Device Health Dashboard
=========

The technical Documentation for the Device Health Application

## Installation

 

## Requirements

## Usage

## Tests
You can run tests using Karma

### Unit Tests

 ##### 1. Components Should get configuration file
 Each component should read the config file
 
 ##### 2. Should Update Lg-map-viz props when location data changes


### E2E Tests

 ##### 1. Should Connect to the API
 
 ##### 2. Should get Location Data from the Api 
 
 ##### 3. Should get
 
 
 ### Running on Docker
 
 - Run the following command to create a docker image: 
 
 ```docker build -t lgapps/devicehealth_frontend ```  
 
 - Then create a container from the image using the following command:
 
 ```$xslt
    docker run -d --name devicehealth_frontend -p 80:80 -v /opt/device-health-front/dist:/Device-Health/frontend lgapps/devicehealth_frontend
```
 

