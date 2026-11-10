First, we will add two projects `(api and util)` that will contain code that is shared by the microservice projects, 
and then we will implement the RESTful APIs.

-   First, we will set up a separate Gradle project where we 
    can place our API definitions. 
    
-   We will use Java interfaces in order to describe our 
    RESTful APIs and model classes to describe the data 
    that the API uses in its requests and responses. 
    
-   To describe what types of errors can be returned by the API, 
    a number of exception classes are also defined. 
    
-   Describing a RESTful API in a Java interface instead of 
    directly in the Java class is, to me, a good way of 
    separating the API definition from its implementation. 
    
-   We will further extend this pattern later,  
    when we add more API information in the Java interfaces 
    to be exposed in an OpenAPI specification. 


**Note**
    `- It is debatable whether it is good practice to store API
    definitions for a group of microservices in a common API
    module.` 
    
    `-   It could potentially cause undesired dependencies 
    between the microservices, resulting in monolithic characteristics,
    for example, causing a more complex and slow development 
    process.` 
    
    -   To me, it is a good choice for microservices that 
    are part of the same delivery organization, 
    that is, whose releases are governed by the same organization 
    (compare this to a bounded context in domain-driven design, 
    where our microservices are placed in a single bounded context).
        
    -   Microservices within the same bounded context need to 
    have API definitions that are based on a common information model,
    so storing these API definitions in the same API 
    module doesn't add any undesired dependencies. `
        
-   Next, we will create a `util` project that can hold some helper classes 
    that are shared by our microservices, 
    for example, for handling errors in a uniform way.
    
`Again, from a DevOps perspective, it would be preferable to build all 
the projects in their own build pipeline and 
have version-controlled dependencies for the **api and util** projects 
in the microservice projects, that is, so that each 
microservice can choose what versions of the **api and util** projects to use.
But to keep the build and deployment steps simple in the context of **POC**, 
we will make the **api and util** projects part of the multi-project build`

**The API project**

-   The api project will be packaged as a library; that is, it won't have its own main application class. 

-   Unfortunately, Spring Initializr doesn't support the creation of library projects.

-   Instead, a library project has to be created manually from scratch.


The structure of a library project is the same as for an application project,
except that we no longer have the main application class, 
as well as some minor differences in the build.gradle file. 

The Gradle plugin `org.springframework.boot` is replaced with an implementation platform section

**Note**

`This type of POJO class is also known as a Data Transfer Object (DTO) 
as it is used to transfer data between the API implementation 
and the caller of the API. When we get to `Adding Persistence`, we will look at another type of POJO that can be used to describe how data is stored in the databases, also known as entity objects.`

The creation of the new plain jar file has been disabled by adding the following lines in the build.gradle file for each microservice:

`jar {
enabled = false
}`
