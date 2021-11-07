# data_mining_app (still in Progress..)
 Full Stack Application to use data mining algorithm on MongoDB collection data.
 
 Currently implemented:
 
 * Apriori algorithm (for shopping cart analysis)


Future implementations:

* KMeans
* KNearest-Neighbors
* etc.

Future ideas:

Provide a python cli to run the algorithms based on file (json, csv, etc), or a REST connection string, with the parameter for a specific field or fields.
 
 ## Backend
 
 Here we have a Spring Boot Kotlin Backend, that is connecting to a MongoDB hosted on MongoDB Atlas. The backend provides several REST Endpoints to communicate to the Frontend.
 
 For Further information see Backend folder.
 
 ## Frontend
 
 The Frontend application is web application based on the quasar.js framework. The application is developed with vue.js and typescript support.
 
 For Further information see Backend folder.
 
 * host backend somewhere
 * host frontend on netlify
