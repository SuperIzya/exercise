Exercise with zookeeper, akka-actor, akka-stream, akka-http, React, mobx

# Prerequisites

## To run

* docker see [instructions](https://docs.docker.com/cs-engine/1.12/) for installation
* docker-compose see [instructions](https://docs.docker.com/compose/install/) for installation
* bash

## To develop

* Java 8 install from [here](https://www.java.com/en/download/help/download_options.xml)
* Sbt see installation instructions [here](https://www.scala-sbt.org/1.0/docs/Setup.html)
* Node.js with npm. Installation instructions are [here](https://nodejs.org/en/download/)

After all tools are installed, run `npm i` from root folder. This will install all node packages. 

# Build & run

## For "production"

From root folder run 
```bash
sbt pack
./start.sh
```  

After everything has started, open in browser [http://localhost](http://localhost)

## For development

From root folder run  
```bash
sbt pack
./dev.sh
```
and in another terminal run
```bash
npm run dev
```
After everything has started, open in browser [http://localhost:8080](http://localhost:8080)

