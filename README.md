# ELK Reasoner Web

Exposes the elk interface as a web server. 

The ontology that should be loaded can be specified with the environment variable ONTOLOGY_PATH. 
The ontology can be classified and then written into a postgresqs DB. 

## Prerequiries:

Publish el2db locally using sbt as described in [el2db](https://github.com/wko/el2db)

## Running on host machine: 

Make sure you have sbt installed and setup and execute the following commands:

    sbt 
    jetty:start
    
## Running as a docker container
    
    docker build --tag elk-reasoner-web
    docker run -it -v $(pwd):/root/myapp -v /PATH/TO/ONTOLOGY/DIRECTORY:/root/data/ontologies -e ONTOLOGY_PATH=/root/data/ontologies/ontology.ofn -p 8080:8080  elk-reasoner-web


## Using the web interface 

The server expects POST Requests with the following parameters: 

* command: the command to be executed
* data: the IRI of the object (class, object property, individual)  
* options: options like return only direct subclasses

  
For example with curl a request can be send using the following command: 

    curl --request POST -H "Accept: application/json" -d "command=getSuperClasses&data=NAMEOFCONCEPT&options=" http://localhost:8080/
    
### Available commands

* getSubClasses,
* getSuperClasses,
* getEquivalentClasses,
* getLabelForClass,
* getLabelForObjectProperty
* getInstances

### Commands to be implemented 

* getTypes,
* getUnsatisfiableClasses,
* isConsistent,
* isSatisfiable,
