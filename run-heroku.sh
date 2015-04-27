#!/bin/bash

export output=$(java $JAVA_OPTS -cp 'target/*' com.github.onsdigital.TestRunner)

echo "Tests complete"

if [ ! $? -eq 0 ]
then
    echo "Tests failed"
    echo $output
    curl --data "Automation tests failed: $output" $'https://onsbeta.slack.com/services/hooks/slackbot?token=ZhoG2gy2TTTBMhtODncuprDQ&channel=%23github'
else
    echo "Tests passed!"
fi
