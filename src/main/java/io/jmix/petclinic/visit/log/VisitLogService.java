package io.jmix.petclinic.visit.log;

import io.jmix.core.DataManager;
import io.jmix.core.EntityStates;
import io.jmix.petclinic.entity.visit.Visit;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.UUID;


/**
 * Service component for managing {@link VisitLog} entities in the Jmix application.
 * <p>
 * This service provides methods for saving, retrieving, and deleting `VisitLog` entities by converting them to
 * and from their persistent MongoDB representation, {@link VisitLogDocument}. It ensures that `VisitLog` entities
 * are properly mapped for database storage and retrieved with relevant associations resolved.
 * </p>
 * <p>
 * The `VisitLogService` uses:
 * <ul>
 *     <li>{@link VisitLogDocumentRepository} - Repository for saving, retrieving, and deleting {@link VisitLogDocument} entities in MongoDB.</li>
 *     <li>{@link DataManager} - Facilitates entity creation and reference management within the Jmix environment.</li>
 *     <li>{@link EntityStates} - Manages entity states, ensuring entities loaded from the database are not marked as new.</li>
 * </ul>
 * </p>
 */
// tag::imports[]
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

// tag::class[]
@Component("petclinic_VisitLogService")
public class VisitLogService {

    private final EntityStates entityStates;
    private final VisitLogDocumentRepository visitLogDocumentRepository;
    private final DataManager dataManager;

    public VisitLogService(EntityStates entityStates, VisitLogDocumentRepository visitLogDocumentRepository, DataManager dataManager) {
        this.entityStates = entityStates;
        this.visitLogDocumentRepository = visitLogDocumentRepository;
        this.dataManager = dataManager;
    }

    /**
     * Retrieves a list of {@link VisitLog} entries associated with a specific {@link Visit}.
     * <p>
     * This method queries MongoDB for {@link VisitLogDocument} records matching the provided visit ID.
     * Each result is converted to a {@link VisitLog} DTO entity for UI representation. During conversion,
     * the `visit` reference is re-resolved to ensure it is correctly associated for display and interaction in Jmix.
     * </p>
     *
     * @param visit The {@link Visit} entity to retrieve visit logs for.
     * @return A list of {@link VisitLog} entries linked to the specified visit.
     */
    public List<VisitLog> findByVisit(Visit visit) {
        return visitLogDocumentRepository.findByVisitId(visit.getId().toString()).stream()
                .map(this::toVisitLog)
                .toList();
    }

    // end::class[]

    /**
     * Saves a {@link VisitLog} entry to the database by converting it into its persistent form, {@link VisitLogDocument}.
     * <p>
     * This method converts the given `VisitLog` DTO entity to a `VisitLogDocument` and saves it using the repository.
     * After saving, the document is re-converted to a `VisitLog` to return the updated DTO entity.
     * </p>
     *
     * @param visitLog The {@link VisitLog} entity to be saved.
     * @return The saved and updated {@link VisitLog} entity.
     */
    public VisitLog saveVisitLog(VisitLog visitLog) {
        VisitLogDocument visitLogDocument = toVisitLogDocument(visitLog);
        VisitLogDocument savedDocument = visitLogDocumentRepository.save(visitLogDocument);
        return toVisitLog(savedDocument);
    }

    /**
     * Loads a {@link VisitLog} by its ID, converting it from a {@link VisitLogDocument} to a DTO entity.
     * <p>
     * This method uses {@link VisitLogDocumentRepository} to retrieve the `VisitLogDocument` based on the provided ID.
     * If the document is found, it is converted to a `VisitLog` entity for use in the UI. If the document is not found,
     * a {@link VisitLogNotFoundException} is thrown.
     * </p>
     *
     * @param visitLogId The ID of the {@link VisitLog} entity to load.
     * @return The loaded and converted {@link VisitLog} entity.
     */
    public VisitLog loadVisitLog(String visitLogId) {
        return visitLogDocumentRepository.findById(visitLogId)
                .map(this::toVisitLog)
                .orElseThrow(() -> new VisitLogNotFoundException(visitLogId));
    }

    /**
     * Removes multiple {@link VisitLog} entities by their IDs from the MongoDB database.
     * <p>
     * This method converts each `VisitLog` entity's ID to a list, which is then passed to
     * {@link VisitLogDocumentRepository#deleteAllById}. This enables bulk deletion of entities based on their IDs.
     * </p>
     *
     * @param visitLogs A collection of {@link VisitLog} entities to remove.
     */
    public void removeVisitLogs(Collection<VisitLog> visitLogs) {
        visitLogDocumentRepository.deleteAllById(
                visitLogs.stream().map(VisitLog::getId).toList()
        );
    }

    /**
     * Converts a {@link VisitLogDocument} to a {@link VisitLog} DTO entity for UI usage.
     * <p>
     * During conversion, the `visit` reference is resolved using {@link DataManager#getReference} to maintain
     * UI associations, and {@link EntityStates#setNew} is set to false to indicate that the entity
     * is not new. This helps avoid re-persisting the entity when interacting with Jmix UI components.
     * </p>
     *
     * @param visitLogDocument The MongoDB document to convert.
     * @return The converted {@link VisitLog} entity.
     */

    // tag::to-visit-log[]
    private VisitLog toVisitLog(VisitLogDocument visitLogDocument) {
        VisitLog visitLog = dataManager.create(VisitLog.class);
        entityStates.setNew(visitLog, false);

        visitLog.setId(visitLogDocument.getId());
        visitLog.setVisit(dataManager.getReference(Visit.class, UUID.fromString(visitLogDocument.getVisitId())));
        visitLog.setTitle(visitLogDocument.getTitle());
        visitLog.setDescription(visitLogDocument.getDescription());

        return visitLog;
    }
    // end::to-visit-log[]

    /**
     * Converts a {@link VisitLog} DTO entity to its persistent form, {@link VisitLogDocument}.
     * <p>
     * This method prepares the `VisitLog` DTO for storage by creating a new `VisitLogDocument`
     * and setting relevant fields, including converting the associated `visit` entity to its identifier
     * (`visitId`) for database compatibility.
     * </p>
     *
     * @param visitLog The {@link VisitLog} entity to convert.
     * @return The resulting {@link VisitLogDocument} for persistence.
     */
    private VisitLogDocument toVisitLogDocument(VisitLog visitLog) {
        VisitLogDocument visitLogDocument = new VisitLogDocument();

        visitLogDocument.setId(visitLog.getId());
        visitLogDocument.setVisitId(visitLog.getVisit().getId().toString());
        visitLogDocument.setTitle(visitLog.getTitle());
        visitLogDocument.setDescription(visitLog.getDescription());

        return visitLogDocument;
    }
}