package io.jmix.petclinic.visit.log;

// tag::imports[]
import org.springframework.data.mongodb.repository.MongoRepository;

// end::imports[]

import java.util.List;


/**
 * Repository interface for managing {@link VisitLogDocument} entities in MongoDB.
 * <p>
 * This repository extends {@link MongoRepository}, which provides various standard data access methods out of the box.
 * By inheriting from MongoRepository, this interface includes numerous CRUD methods for working with
 * {@link VisitLogDocument} instances, such as `save`, `findById`, `findAll`, and `deleteById`. These methods
 * simplify data access by offering a standard API without requiring explicit implementation.
 * </p>
 * <p>
 * In addition to the default CRUD methods, this interface defines a custom query method:
 * <ul>
 *     <li><b>findByVisitId(String visitId)</b> - Retrieves a list of `VisitLogDocument` entities associated
 *     with a specific `visitId`.</li>
 * </ul>
 * Spring Data MongoDB will automatically generate the query for `findByVisitId` based on its naming convention,
 * making it straightforward to add custom finder methods without additional configuration.
 * </p>
 * <p>
 * For more details on working with MongoDB repositories and custom query methods, refer to the
 * <a href="https://docs.spring.io/spring-data/mongodb/reference/mongodb/repositories/repositories.html">Spring Data MongoDB repository documentation</a>.
 * </p>
 *
 * @see VisitLogDocument
 * @see MongoRepository
 */
// tag::repository[]

public interface VisitLogDocumentRepository extends MongoRepository<VisitLogDocument, String> {

    /**
     * Finds all {@link VisitLogDocument} entries associated with the specified visit ID.
     *
     * @param visitId The ID of the visit associated with the logs to retrieve.
     * @return A list of {@link VisitLogDocument} instances matching the specified visit ID.
     */
    List<VisitLogDocument> findByVisitId(String visitId);
}
// end::repository[]