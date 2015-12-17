Osmium
======

[![Build Status](https://travis-ci.org/NLeSC/osmium.svg?branch=develop)](https://travis-ci.org/NLeSC/osmium)

Web service to submit jobs via a Xenon (https://nlesc.github.io/Xenon) supported scheduler.

Submit job to web service using a HTTP POST request.
Reports status of job to submitter using a callback url.
Copies input files from machine running webservice to machine executing job and copies output files back.

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

Web service api documentation can be found at http://docs.osmium.apiary.io or as un-rendered format at [apiary.apib](apiary.apib).

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

# Documentation

## Javadoc

A javadoc be generated with
```
./gradlew javadoc
firefox build/docs/javadoc/index.html
```

## Generate web api documentation

API documentation is written in https://apiblueprint.org/ format.

API documentation can be previewed with:
````
sudo npm install -g aglio
aglio -i apiary.apib -s
````
