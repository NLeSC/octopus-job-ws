Osmium
======

[![Build Status](https://travis-ci.org/NLeSC/osmium.svg?branch=develop)](https://travis-ci.org/NLeSC/osmium)

Web service to submit jobs via a Xenon (https://nlesc.github.io/Xenon) supported scheduler.

Submit job to web service using a HTTP POST request.
Reports status of job to submitter using a callback url.

# Requirements

- JDK 7 or 8 (http://www.java.com)

# Build


Build osmium distributions and shadow jar with:

```
./gradlew build shadowJar
```

The distributions will be in `build/distributions`.
The shadow jar will be `build/libs/osmium-*-all.jar`.

# Deployment

1. Copy and extract distribution zip/tarball or the shadow jar to the place you want to run it.
2. Make copy of 'joblauncher.yml-dist' to 'joblauncher.yml'

  * Configure Xenon launchers with a scheduler and a sandbox
  * Configure optional callback basic credentials

3. Run it

Distribution can be run with

```
bin/osmium server joblauncher.yml
```

Shadow jar can be run with

```
java -jar build/libs/osmium-*-all.jar server joblauncher.yml
```

A web service will be started on http://localhost:9998

# Usage

## Submit

Create a directory with an input file and script

    mkdir myjob
    cd myjob
    echo 'Lorem ipsum' > input_file
    echo 'hostname;date;wc -l input_file > output_file' > runme.sh

Create a json file (query.json)

    {
      "launcher": <optional non-default launcher name>,
      "jobdir": "<absolute path to myjob directory>",
      "executable": "/bin/sh",
      "prestaged": ["runme.sh", "input_file"],
      "poststaged": ["output_file"],
      "stderr": "stderr.txt",
      "stdout": "stdout.txt",
      "arguments": ["runme.sh"],
      "status_callback_url": "<optional url where job status is PUT to>"
    }

The `status_callback_url` value is the url where the web service will send the job status with a http PUT request.
Can be used as an alternative for polling the job status.

Then submit it

    curl -H "Content-Type: application/json" -H 'Accept: application/json' \
    -i -X POST -d @query.json http://localhost:9998/job

    HTTP/1.1 201 Created
    Date: Thu, 23 May 2013 11:50:28 GMT
    Location: http://localhost:9998/job/local-1234
    Content-Type: application/json
    Content-Length: 0

The submit response contains no content only headers.
The `Location` header value is the url where the job can be queried for it's status or where it can be canceled.

### Callback authentication

The status callbacks can use basic http authentication.
The credentials must be added to the config file or can be given in the `status_callback_url` of the job submission request.

## Status

In the submit response the url is a relative url to the job.

    curl -H "Content-Type: application/json" -H 'Accept: application/json' \
    http://localhost:9998/job/local-1234

Example response when job is running:

    {
       "request": {
           "launcher": "local",
           "jobdir": "/tmp/jobdir",
           "executable": "/bin/sh",
           "stderr": "stderr.txt",
           "stdout": "stdout.txt",
           "arguments": [
               "runme.sh"
           ],
           "prestaged": [
               "runme.sh", "input.dat"
           ],
           "poststaged": ["output.dat"],
           "status_callback_url": "http://localhost/status",
           "max_time": 15
       },
       "status": {
         "state": "RUNNING",
         "exitCode": null,
         "exception": null,
         "running": true,
         "done": false,
         "schedulerSpecficInformation": null
      }
    }

Example response when job is done:

    {
       "request": {
           "launcher": "local",
           "jobdir": "/tmp/jobdir",
           "executable": "/bin/sh",
           "stderr": "stderr.txt",
           "stdout": "stdout.txt",
           "arguments": [
               "runme.sh"
           ],
           "prestaged": [
               "runme.sh", "input.dat"
           ],
           "poststaged": ["output.dat"],
           "status_callback_url": "http://localhost/status",
           "max_time": 15
       },
       "status": {
         "state": "DONE",
         "exitCode": 0,
         "exception": null,
         "running": false,
         "done": true,
         "schedulerSpecficInformation": null
      }
    }

Example response when job has been canceled (see below for cancel command):

    {
      "request": {
         "launcher": "local",
         "jobdir": "/tmp/myjob",
         "status_callback_url": null,
         "poststaged": [
            "output_file"
         ],
         "stderr": "stderr.txt",
         "executable": "/bin/sh",
         "arguments": [
            "runme.sh"
         ],
         "prestaged": [
            "runme.sh",
            "input_file"
         ],
         "stdout": "stdout.txt"
      },
      "status": {
         "running": false,
         "done": true,
         "exception": "Process cancelled by user.",
         "schedulerSpecficInformation": null,
         "exitCode": null,
         "state": "KILLED"
      }
    }

## Cancel

Cancel a pending or running job.
Deletes any generated output in the sandbox where the job was running.

    curl -H "Content-Type: application/json" -H 'Accept: application/json' \
    -X DELETE http://localhost:9998/job/local-1234

# Development

To open in an IDE like Eclipse or Intellij IDEA, create project files with `./gradlew eclipse` or `./gradlew idea` respectively.

Perform tests with test and coverage reports in `build/reports` directory.
````
./gradlew test jacocoTestReport
````

Run integration tests with
```
./gradlew check
```

To run web service first create config file `joblauncher.yml`, use `joblauncher.yml-dist` as an example and then start service with
````
./gradlew run
````

Run SonarQube analysis with
```
./gradlew sonarqube -Dsonar.host.url=http://localhost:9000 \
-Dsonar.jdbc.url=jdbc:mysql://localhost:3306/sonarqube \
-Dsonar.jdbc.username=sonar -Dsonar.jdbc.password=sonar
```

# Documentation

## Javadoc

A javadoc be generated with
```
./gradlew javadoc
firefox build/docs/javadoc/index.html
```

## Generate web api documentation

API documentation is written in https://apiblueprint.org/ format.

API documentation can be preview with:
````
sudo npm install -g aglio
aglio -i apiary.apib -s
````
