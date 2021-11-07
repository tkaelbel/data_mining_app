# data_mining_app

Just a new implementation of the Data Mining Tool. 
Instead of plain Java here we are using Kotlin with Spring Boot.
Also we have a WebFlux REST Endpoint to stream the data during the execution of an algorithm
to the client.

## MongoDB

For the persitence layer we are using the MongoDB database, which is hosted on the cloud service MongoDB Atlas.

## Data REST endpoints

The data REST endpoints are used to display information about the MongoDB database on the Frontend.

## Webflux/Algorithm REST endpoints

These REST endpoints are implemented with Webflux to provide a "stream" like response for the Frontend. All of this services are only for the algorithms. It's used to show the processing of the algorithm calculations.

## Next things...
* Swagger
* KMeans
* KNearest-Neighbors
* etc


## run the application

Currently the application is only available in dev mode. So just import the project in your favorite IDE and run it.
