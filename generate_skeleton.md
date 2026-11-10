### Generate Skeleton Code 

To get started with developing our microservices, we will use a tool called `Spring Initializr` to generate skeleton code for us. 

Spring Initializr is provided by the Spring team and can be used to configure and generate new Spring Boot applications.

To make it easier to reproduce the creation of the microservices, we will use the command-line tool.

For each microservice, we will create a Spring Boot project that does the following:

- Uses Gradle as a build tool
- Generates code for Java 8
- Packages the project as a fat JAR file
- Brings in dependencies for the Actuator and WebFlux Spring modules
- Is based on Spring Boot v(latest stable) 

Refer the script file uplaoded to repository.


**Setting up multi-project builds in Gradle**

Create a project root folder, example - backend
download the attached script(create*) from repository.
Update the "spring-cli" bin path.
run the script "./create*" and it should initialize the microservices.


-   First, we create the `settings.gradle` file inside the root folder, which describes what projects Gradle should build:
       

    > `cat <<EOF > settings.gradle`
    
    >`include ':microservices:product-service'`
    
    >`include ':microservices:review-service'`
    
    >`include ':microservices:recommendation-service'`
    
    >`include ':microservices:product-composite-service'`
    
    >`EOF`

-   Next, we copy the Gradle executable files that were generated from one of the projects so that we can reuse them for the multi-project builds:
    

    `cp -r microservices/product-service/gradle .
    cp microservices/product-service/gradlew .
    cp microservices/product-service/gradlew.bat .`

-   We no longer need the generated Gradle executable files in each project, so we can remove them with the following commands:
    
   > `find microservices -depth -name "gradle" -exec rm -rfv "{}" \;
   > find microservices -depth -name "gradlew*" -exec rm -fv "{}" \; `

-   Now run below command to build all microservice.

    `./gradlew build`

### Note :

> `From a DevOps perspective, a multi-project setup might not be preferred. Instead, to enable each microservice to have its own build and release cycle, setting up a separate build pipeline for each microservice project would probably be preferred.`

#### API
Create a separate API gradle project. We are going to define Java interfaces in order to describe the our RESTFul API and model classes to describe the data that API uses during its request and response.

Unfortunately spring initializer does not support the library projects

We need to create manually . 

