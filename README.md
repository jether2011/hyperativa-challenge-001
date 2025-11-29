# Getting Started

## Running the application
This project is a Spring Boot application that uses Gradle as the build tool. It is designed to be run in a Docker container, but it can also be run locally using Gradle.
Additionally, it uses Spring Security for authentication and authorization and Spring Data JPA for database access and is using Java 21 and Spring Boot 3.4.4.
The application is configured to use a PostgreSQL MySQL, and it uses Flyway for database migrations. It also includes a REST API for managing users and cards.
Finally, the application doesn't use the profiling mode, such as develop, homologation, and production, however, it uses the default profile, and in production, the configuration should be externalized.

## Docker
To run the application in a Docker container, you need to have Docker installed on your machine. You can find instructions for installing Docker: [here](https://docs.docker.com/get-docker/).
Running the database container:

```bash
docker compose -f docker/mysql.yml up -d
```

## Java application
To run the application, you can use the following command (under the project root folder):

```bash
 ./gradlew bootRun
```        
After the application runs, you can access the API using a Postman collection (`HyperativaChallenge.postman_collection.json`) included in the project. 

## Final Considerations
The application is designed to be a simple example of how to use Spring Boot, Spring Security, and Spring Data JPA to create a REST API. It is not intended to be a complete or production-ready application.

### Future Improvements
- Security
   - Add Authorities for the User
   - Add the ROLEs to the generated JWT token
   - When validating the token, retrieve the roles from it and add them to the Spring Security Authentication Context
   - Use the `@Secured` annotation on methods to restrict access based on user roles and prevent unauthorized API usage
- Webfkux: Implement asynchronous requests to improve responsiveness
- Actuator + Prometheus: Integrate Actuator with Prometheus to enhance observability and metrics tracking
- Implement caching using a provider like Redis and utilize a distributed Hibernate second-level cache to improve application performance
- Create mappers to convert between DTOs and entities
- Scale horizontally using Docker Swarm or Kubernetes, and utilize both for improved scalability
- Achieve high availability through the use of a load balancer along with service discovery mechanisms + scalling
- For production, refactor file uploads for cards to use asynchronous processors that consume from storage solutions like S3
- Change the embedded web server from `Tomcat` to `Undertow` in the current version to improve thread request pooling
- Blocking requests in Spring MVC can degrade performance or throughput. To enhance performance, consider using a Spring Event Driven JVM (Application Event Context) or messaging brokers like RabbitMQ or Kafka instead of relying on the Reactor project (Webflux).
