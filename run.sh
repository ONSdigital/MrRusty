#!/bin/bash

# usage: ./run.sh [test namespace] [base url] [BrowserStack url]
# e.g. ./run.sh browser localhost:8081  (run browser tests against localhost)
#      ./run.sh api develop.carboni.uk  (run api tests against develop)
# "http://carlhembrough2:FJCaeMyLNLBGxDVgACLp@hub.browserstack.com/wd/hub"

# Remote debug:
#export JAVA_OPTS="-Xdebug -Xrunjdwp:transport=dt_socket,address=8001,server=y,suspend=n"
#export PORT="8081"



namespace=com.github.onsdigital

# if the first argument has been passed parse as test namespace override
if [ ! -z "$1" ]
  then
    namespace="${namespace}.test.$1"
fi

base_url="http://localhost:8081"
florence_path="/florence/index.html"

if [ ! -z "$2" ]
  then
    base_url="$2"
fi

if [ ! -z "$3" ]
  then
    export BROWSERSTACK_URL="$3"
fi

echo Running tests for namespace: ${namespace} against: ${base_url}

export FLORENCE_URL="$base_url$florence_path"
export ZEBEDEE_HOST="$base_url/zebedee"

mvn clean package dependency:copy-dependencies && \
java -jar target/*-jar-with-dependencies.jar ${namespace}


#mvn clean package dependency:copy-dependencies && \
#java -cp "target/classes:target/dependency/*" com.github.onsdigital.TestRunner

#java $JAVA_OPTS -Drestolino.files=$RESTOLINO_STATIC -Drestolino.classes=$RESTOLINO_CLASSES -Drestolino.packageprefix=$RESTOLINO_PACKAGEPREFIX -cp "target/dependency/*" com.github.davidcarboni.restolino.Main
