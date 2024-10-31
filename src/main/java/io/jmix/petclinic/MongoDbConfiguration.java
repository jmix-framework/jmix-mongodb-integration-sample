package io.jmix.petclinic;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * MongoDB configuration class that activates scanning for Spring Data MongoDB repositories.
 * <p>
 * Placed in the top-level `io.jmix.petclinic` package, this class enables repository support for all sub-packages
 * under `io.jmix.petclinic` by leveraging the {@link EnableMongoRepositories} annotation, which triggers Spring's
 * repository scanning functionality. This configuration ensures that any interfaces extending {@link MongoRepository}
 * are automatically detected and registered as beans within the Spring application context.
 * </p>
 * <p>
 * The {@link Configuration} annotation marks this as a Spring-managed configuration class, enabling
 * MongoDB-specific setup, including repository and template management.
 * </p>
 * <p>
 * Refer to the <a href="https://docs.spring.io/spring-data/mongodb/reference/mongodb/repositories/repositories.html">Spring Data MongoDB Repositories</a> documentation for more details on repository scanning,
 * as well as the general <a href="https://docs.spring.io/spring-data/mongodb/reference/repositories/core-concepts.html">core concepts of repository support</a> in Spring Data.
 * </p>
 *
 * @see org.springframework.data.mongodb.repository.config.EnableMongoRepositories
 * @see org.springframework.data.mongodb.core.MongoTemplate
 */
// tag::class[]
@Configuration
@EnableMongoRepositories
public class MongoDbConfiguration {

}
// end::class[]