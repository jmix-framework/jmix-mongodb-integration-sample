package io.jmix.petclinic.visit.log;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * MongoDB persistence class representing a `VisitLog` document for storage and retrieval from the database.
 * <p>
 * This class serves as the dedicated persistence entity for `VisitLog`, while the UI-facing {@link VisitLog}
 * entity is transient. This separation enhances the layering in the application by isolating persistence concerns
 * (handled by `VisitLogDocument`) from the presentation layer (handled by {@link VisitLog}).
 * This approach supports improved serialization and a cleaner division of responsibilities.
 * </p>
 * <p>
 * The {@link Document} annotation maps this class to a MongoDB document, enabling Spring Data MongoDB to manage
 * its lifecycle and provide convenient access through repository interfaces.
 * </p>
 * <p>
 * Fields:
 * <ul>
 *     <li><b>id</b> - Unique identifier for each `VisitLogDocument` entry in MongoDB. By convention, MongoDB treats
 *     this `id` field as the document’s primary key, or "_id" field, which serves as the unique identifier and index for each document.</li>
 *     <li><b>visitId</b> - Corresponds to the visit entity's identifier, linking `VisitLogDocument` and related visit records.</li>
 *     <li><b>title</b> - Title or summary of the visit log.</li>
 *     <li><b>description</b> - Detailed description or notes for the visit log.</li>
 * </ul>
 * </p>
 * <p>
 * For more details on using Spring Data MongoDB’s {@link Document} annotation, see the
 * <a href="https://docs.spring.io/spring-data/mongodb/docs/current/reference/html/#mapping-usage">Spring Data MongoDB mapping documentation</a>.
 * </p>
 *
 * @see VisitLog
 * @see org.springframework.data.mongodb.core.mapping.Document
 */
@Document
public class VisitLogDocument {

    @Id
    private String id;
    private String visitId;
    private String title;
    private String description;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getVisitId() {
        return visitId;
    }

    public void setVisitId(String visitId) {
        this.visitId = visitId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}