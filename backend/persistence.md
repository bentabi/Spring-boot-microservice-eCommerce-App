The product and recommendation service will use Spring Data for MongoDB 
and review microservice will use Spring Data for the JPA to access mySQL.

To access the database manually , we should use the CLI tool provided in Docker images 
used to run Database.

3306 mySQL 27017 MongoDB

Use `MapStruct` , java bean mapping tool. This makes easy to transform between 
Spring Data entity objects and the API model classes.

TestContainer - library that simplifies the running automated integration test.
To start only mongodb using docker-compose

docker-compose exec mongodb mongo ––quiet
docker-compose exec mysql mysql -uuser -p review-db

docker-compose exec mongodb mongo recommendation-db --quiet --eval "db.recommendations.find()"
docker-compose exec mysql mysql -uuser -p review-db -e "select * from reviews"

http://localhost:8080/openapi/swagger-ui.html



