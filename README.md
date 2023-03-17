# **Crawler microservice**
#### _A remote microservice web crawler_

![Maven](https://img.shields.io/badge/Apache%20Maven-C71A36.svg?style=for-the-badge&logo=Apache-Maven&logoColor=white)![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=java&logoColor=white)

This is a microservice written in Java using the Spark framework.
It does web crawling and sends all URLs found to user in a JSON response

## Usage
All you will need is Java Runtime Environment (JRE), the the curl tool and the crawler-microservice.jar:
```bash
java -jar crawler-microservice.jar [Bind IP (IPV4 format)] [Port] [BaseURL] [Timeout in sec
onds] [Max number of threads]
java -jar crawler-microservice.jar 127.0.0.1 4567 https://example.com 60 8
```
In another terminal, use the curl tool with the POST method to create a new crawler looking for the "example" keyword:
```bash
curl -X POST http://127.0.0.1:4567/search -H "Content-Type: application/json" -d "{\"keyword\": \"example\"}"
```
The response will be a JSON string (id value will always be different):
```bash
{"id":"4638b31c"}
```
With the id value you can check the current results found by the crawler with the curl tool or a browser using a GET request:
```bash
curl http://127.0.0.1:4567/search/:id
curl http://127.0.0.1:4567/search/4638b31c
```
The microservice will send the id, status and all URLs where the keyword was found:
```bash
{"id":"4638b31c","status":"done","urls":["https://example.com"]}
```

## Build from source
I'll need the Java Development Kit (JDK) version 19 (at least) and if you don't have maven installed you can use the wrapper:
```bash
git clone https://github.com/mazoti/microservice
cd microservice
mvn package
```
It will compile, run all unit tests and deploy the release .jar file on target folder.

## Documentation
To generate the api documentation:
```bash
mvn javadoc:javadoc
```
The documentation will be on target/site/apidocs

## Donations
You can become a [sponsor](https://github.com/sponsors/mazoti) or donate directly:

BTC: 3JpkXivH11xQU37Lwk5TFBqLUo8gytLH84

[![License](https://img.shields.io/badge/License-BSD_3--Clause-blue.svg)](https://opensource.org/licenses/BSD-3-Clause)

**Thanks for your time and have fun!**
