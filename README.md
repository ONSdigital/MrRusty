# MrRusty
Simple HTTP client framework for testing REST APIs

Running browser tests locally requires the Chrome browser to be installed and the chrome driver to be running. You can find the latest version of chrome driver here: 

https://sites.google.com/a/chromium.org/chromedriver/downloads

By default using either the run.sh script or running the test runner class will default to running all the tests against localhost.

## Choose a limited namespace to run tests

If you want to just run the API tests, you can pass a parameter to the run script (or TestRunner class) to specify this:

  ./run "api"
  
Likewise you can run just the browser tests:

  ./run "browser"
  
If you want to run the tests against an environment other than localhost, you can pass a second parameter to the run.sh script to specifiy the base URL of the environment:

  ./run.sh "" http://user:pass@develop.carboni.uk
  
  Note the empty first parameter to run all the tests. The second parameter contains basic http auth credentials to access the develop environment. You will need to replace the "user:pass" part of the URL with the credentials.
  
## Running tests using BrowserStack
