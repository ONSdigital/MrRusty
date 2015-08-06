#!/bin/bash

namespace=com.github.onsdigital

base_url="http://localhost:8081"
florence_path="/florence/index.html"

if [ ! -z "$1" ]
  then
    base_url="$1"
fi

if [ ! -z "$2" ]
  then
    export BROWSERSTACK_URL="$2"
fi

echo Running tests against: ${base_url}

export FLORENCE_URL="$base_url$florence_path"
export ZEBEDEE_HOST="$base_url/zebedee"

mvn clean package dependency:copy-dependencies && \
java -jar target/*-jar-with-dependencies.jar

exit $? # return the code from the last command.
