#!/bin/bash

set -e

echo "Building web-stress-simulator..."

BUILD_VERSION=1.0.0

#Build web-app war
docker run -it --rm -v "$PWD"/src/web-app:/opt/workspace -w /opt/workspace flaviostutz/maven-build:3.3.3-jdk-8 mvn clean install

#Build web-stress-simulator container
docker build -t flaviostutz/web-stress-simulator:$BUILD_VERSION .

echo "Finished building web-stress-simulator."
