# vlingo-schemata

[![Gitter chat](https://badges.gitter.im/gitterHQ/gitter.png)](https://gitter.im/vlingo-platform-java/schemata)

The vlingo/PLATFORM schema registry.
 
## Run

You can run the registry with demo data within docker using `docker run -p 9019:9019 wernerw/vlingo-schemata:demo`.

After building the fat jar, you can also simply execute it via `java -jar vlingo-schemata-<version>-jar-with-dependencies.jar`

## Using curl or Postman

### Schema Definitions

`$ curl -i -X POST -H "Content-Type: application/json" -d '{"organizationId":"","name":"Org1","description":"My organization."}' http://localhost:9019/organizations`

`$ curl -i -X POST -H "Content-Type: application/json" -d '{"organizationId":"","unitId":"","name":"Unit1","description":"My unit."}' http://localhost:9019/organizations/{orgId}/units`

`$ curl -i -X POST -H "Content-Type: application/json" -d '{"organizationId":"","unitId":"","contextId":"","namespace":"io.vlingo.schemata","description":"Schemata Context."}' http://localhost:9019/organizations/{orgId}/units/{unitId}/contexts`

`$ curl -i -X POST -H "Content-Type: application/json" -d '{"organizationId":"","unitId":"","contextId":"","schemaId":"","category":"Event","name":"SchemaDefined","description":"Schemata was defined event."}' http://localhost:9019/organizations/{orgId}/units/{unitId}/contexts/{contextId}/schemas`

`$ curl -i -X POST -H "Content-Type: application/json" -d '{"organizationId":"","unitId":"","contextId":"","schemaId":"","schemaVersionId":"","description":"Initial revision.","specification":"event SchemaDefined { type eventType }","status":"Draft","previousVersion":"0.0.0","currentVersion":"1.0.0"}' http://localhost:9019/organizations/{orgId}/units/{unitId}/contexts/{contextId}/schemas/{schemaId}/versions`

### Schema Queries

`$ curl -i -X GET -H "Accept: application/json" http://localhost:9019/organizations`

`$ curl -i -X GET -H "Accept: application/json" http://localhost:9019/organizations/{orgId}`

`$ curl -i -X GET -H "Accept: application/json" http://localhost:9019/organizations/{orgId}/units`

`$ curl -i -X GET -H "Accept: application/json" http://localhost:9019/organizations/{orgId}/units/{unitId}`

`$ curl -i -X GET -H "Accept: application/json" http://localhost:9019/organizations/{orgId}/units/{unitId}/contexts`

`$ curl -i -X GET -H "Accept: application/json" http://localhost:9019/organizations/{orgId}/units/{unitId}/contexts/{contextId}`

`$ curl -i -X GET -H "Accept: application/json" http://localhost:9019/organizations/{orgId}/units/{unitId}/contexts/{contextId}/schemas`

`$ curl -i -X GET -H "Accept: application/json" http://localhost:9019/organizations/{orgId}/units/{unitId}/contexts/{contextId}/schemas/{schemaId}`

`$ curl -i -X GET -H "Accept: application/json" http://localhost:9019/organizations/{orgId}/units/{unitId}/contexts/{contextId}/schemas/{schemaId}/versions`

`$ curl -i -X GET -H "Accept: application/json" http://localhost:9019/organizations/{orgId}/units/{unitId}/contexts/{contextId}/schemas/{schemaId}/versions/{versionId}`

### Schema Modifications

`$ curl -i -X PATCH -H "Content-Type: application/json" -d 'My organization changed.' http://localhost:9019/organizations/{organizationId}/description`
`$ curl -i -X PATCH -H "Content-Type: application/json" -d 'Org123' http://localhost:9019/organizations/{organizationId}/name`

`$ curl -i -X PATCH -H "Content-Type: application/json" -d 'My unit changed.' http://localhost:9019/organizations/{orgId}/units/{unitId}/description`
`$ curl -i -X PATCH -H "Content-Type: application/json" -d 'Unit123' http://localhost:9019/organizations/{orgId}/units/{unitId}/description`

`$ curl -i -X POST -H "Content-Type: application/json" -d 'My context changed.' http://localhost:9019//organizations/{organizationId}/units/{unitId}/contexts/{contextId}/description`
`$ curl -i -X POST -H "Content-Type: application/json" -d 'io.vlingo.schemata.changed' http://localhost:9019//organizations/{organizationId}/units/{unitId}/contexts/{contextId}/namespace`

`$ curl -i -X POST -H "Content-Type: application/json" -d 'Command' http://localhost:9019//organizations/{organizationId}/units/{unitId}/contexts/{contextId}/schemas/{schemaId}/cateogry`
`$ curl -i -X POST -H "Content-Type: application/json" -d 'DefineSchema command defined.' http://localhost:9019//organizations/{organizationId}/units/{unitId}/contexts/{contextId}/schemas/{schemaId}/description`
`$ curl -i -X POST -H "Content-Type: application/json" -d 'DefineSchema' http://localhost:9019//organizations/{organizationId}/units/{unitId}/contexts/{contextId}/schemas/{schemaId}/name`

`$ curl -i -X POST -H "Content-Type: application/json" -d 'Initial revision of SchemaDefined.' http://localhost:9019/organizations/{orgId}/units/{unitId}/contexts/{contextId}/schemas/{schemaId}/versions/{versionId}/description`
`$ curl -i -X POST -H "Content-Type: application/json" -d 'event SchemaDefined { type eventType\n timestamp occurredOn }' http://localhost:9019/organizations/{orgId}/units/{unitId}/contexts/{contextId}/schemas/{schemaId}/versions/{versionId}/specification`
`$ curl -i -X POST -H "Content-Type: application/json" -d 'Published' http://localhost:9019/organizations/{orgId}/units/{unitId}/contexts/{contextId}/schemas/{schemaId}/versions/{versionId}/status`


## Build

Run the full build using `mvn clean package -Pfrontend`. 

Afterwards, you can build the docker container with `docker build . -t vlingo-schemata-demo`.

CI build runs on CircleCI: https://circleci.com/gh/wwerner/vlingo-schemata/.

A docker image containing mock data is published to https://hub.docker.com/r/wernerw/vlingo-schemata.


The maven build takes care of the following:
* Generate sources for the schema grammars in `src/main/antlr4`
* Build the backend application
* Download the dependencies for the UI build (`node` & `npm`) [Maven Profile 'frontend']
* Run the UI build (`npm run build` in `src/main/frontend`) [Maven Profile 'frontend']
* Package the backend, frontend and dependencies within a fat jar

## Development

### Prerequisites

* Java >= 8
* Maven, 3.6.0 is known to work. Alternatively, you can rely on the bundled maven wrapper.
* [frontend only] NodeJS & npm, Node 11.14.0 is known to work. Earlier versions probably too.
* [frontend only; recommended] Chrome Devtools Vue.js extension 

### Backend

* Generate schema specification sources: `mvn generate-sources`
* Test: `mvn test`
* Build fat jar: `mvn package`

### Frontend

vlingo-schemata provides an interface to allow for easy retrieval of schemata and 
schemata meta information, e.g. available versions and publication status.

The UI is built using Vue.js and Vuetify.

* Project setup `npm install`
* Compiles and hot-reloads for development `npm run serve`, makes the UI available on http://localhost:8080/
* Compiles and minifies for production `npm run build`
* Run your tests `npm run test`
* Lints and fixes files `npm run lint`
* Webpack bundle report `npm run report`

When running the UI via `npm run serve`, API calls are proxied to a local backend instance.
It is assumed to be running at http://localhost:9019/api.

You can run the backend ...
* ... from the IDE, in case you want to debug the backend in parallel. `Bootstrap.java` has the `main` method.
* ... from the jar, in case you just need the current backend state and do not want to delve into the backend code: 
`java -jar vlingo-schemata-<version>-jar-with-dependencies.jar`
* ... from Docker, in case you don't want bother w/ Java at all: 
`docker run -p 9019:9019 wernerw/vlingo-schemata:demo`. 
Note that this also pulls in the current UI, so don't get confused.
