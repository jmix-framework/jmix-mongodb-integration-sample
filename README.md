<p align="center">
  <img src="https://raw.githubusercontent.com/Haulmont/jmix-petclinic/main/img/petclinic_logo_with_slogan.svg"/>
</p>

# Jmix - MongoDB Integration

This example application demonstrates how to integrate MongoDB into a Jmix application to manage `VisitLog` entries associated with `Visit` entities.

The application uses Jmix and Spring Data MongoDB to create, read, update, and delete `VisitLog` entries in MongoDB. Each `VisitLog` entry is linked to a `Visit`, enabling a logbook-like structure for visits in the system.


### Screenshots

To demonstrate the functionality, here are example screenshots of the various screens:

1. **Visit List View with Visit Log Button**

   ![Visit List View with Visit Log Button](img/1-visit-log-button.png)

2. **Visit Log Entries List View**

   ![Visit Log Entries List View](img/2-visit-logs.png)

3. **Visit Log Detail View**

   ![Visit Log Detail View](img/3-create-visit-log.png)


## Table of Contents

0. [Adding Dependencies](#0-adding-dependencies)
1. [MongoDB Setup](#1-mongodb-setup)
2. [MongoDB Configuration](#2-mongodb-configuration)
3. [Creating a VisitLog Entity](#3-creating-a-visitlog-entity)
4. [Spring Data MongoDB Repository](#4-spring-data-mongodb-repository)
5. [Service Layer for Visit Logs](#5-service-layer-for-visit-logs)
6. [UI Integration with Jmix](#6-ui-integration-with-jmix)

---

### 0. Adding Dependencies

To integrate MongoDB with Jmix, add the following dependency to your build.gradle file. This includes the spring-boot-starter-data-mongodb dependency, which provides the necessary configuration and utilities to use Spring Data MongoDB:

```groovy
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-data-mongodb'
}
```

This starter package simplifies interactions with MongoDB, enabling Spring Data repositories, annotations, and other MongoDB utilities.

The version of spring-boot-starter-data-mongodb is automatically derived based on the compatible Spring Boot version, which in turn aligns with the Jmix version specified in the project. This means you don’t need to specify a dedicated version for MongoDB, as it will use the correct, compatible version based on the Jmix BOM (Bill of Materials). This setup ensures compatibility and minimizes manual dependency management.

### 1. MongoDB Setup

To set up MongoDB and Mongo Express, use Docker Compose. Mongo Express provides a web interface for managing the MongoDB database.

```yaml
version: '3.8'

services:
  # MongoDB Service
  mongo:
    image: mongo
    ports:
      - "27017:27017"
    environment:
      MONGO_INITDB_ROOT_USERNAME: root
      MONGO_INITDB_ROOT_PASSWORD: petclinic

  # Mongo Express for MongoDB UI management
  mongo-express:
    image: mongo-express
    restart: always
    ports:
      - "8081:8081"
    environment:
      ME_CONFIG_MONGODB_ADMINUSERNAME: root
      ME_CONFIG_MONGODB_ADMINPASSWORD: petclinic
      ME_CONFIG_MONGODB_URL: mongodb://root:petclinic@mongo:27017/
```

To launch MongoDB and Mongo Express, run:

```bash
docker-compose up
```

### 2. MongoDB Configuration

In your Jmix application, configure the MongoDB connection URI in `application.properties`. The URI uses the credentials defined in Docker Compose.

```properties
spring.data.mongodb.uri=mongodb://root:petclinic@localhost:27017/petclinic?authSource=admin
```

This configuration ensures that Jmix connects to MongoDB for managing `VisitLog` entities.

### 3. Creating a VisitLog Entity

n this application, we separate the persistence and presentation layers by using two classes:

* VisitLog: A DTO entity used in the UI for managing and displaying visit log data.
* VisitLogDocument: A dedicated persistence entity mapped to MongoDB for storing and retrieving visit logs.

This separation improves the layering and structure of the application by isolating persistence details from the UI layer.

#### Defining the VisitLog DTO Entity

The VisitLog entity is used in the Jmix UI as a transient representation of visit logs. It does not directly interact with MongoDB but provides a clean model for the UI components.

```java
package io.jmix.petclinic.visit.log;

import io.jmix.core.entity.annotation.JmixId;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.petclinic.entity.visit.Visit;

import java.util.UUID;

@JmixEntity(name = "petclinic_VisitLog")
public class VisitLog {

   @JmixId
   private String id;

   private Visit visit;
   private String title;
   @InstanceName
   private String description;

   // Getters and setters
}
```

#### Defining the VisitLogDocument Persistence Entity

The VisitLogDocument class is annotated with `@Document` to map it to a MongoDB collection. It serves as the persistent representation of VisitLog and is used in the service layer to interact with MongoDB.

```java
package io.jmix.petclinic.visit.log;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class VisitLogDocument {

    @Id
    private String id;
    private String visitId;
    private String title;
    private String description;

    // Getters and setters
}
```

In this setup:

* The VisitLogDocument entity is responsible for database interactions. Fields like visitId are used as simple identifiers for the related Visit entity, rather than full references.
* The VisitLog DTO provides a UI-facing model, with fields like visit representing direct associations, useful for displaying data and managing interactions in the Jmix UI.

When saving or retrieving data, the VisitLogService handles the conversion between VisitLog and VisitLogDocument to maintain a clean separation of concerns between persistence and presentation layers.

### 4. Spring Data MongoDB Repository

Define a Spring Data repository to interact with MongoDB. This repository enables CRUD operations for `VisitLog` entries.

```java
package io.jmix.petclinic.visit.log;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface VisitLogDocumentRepository extends MongoRepository<VisitLogDocument, String> {
    List<VisitLogDocument> findByVisitId(String visitId);
}
```

### 5. Service Layer for Visit Logs

The VisitLogService provides methods to manage VisitLog entries by transforming them between their UI representation (VisitLog) and their persistent form (VisitLogDocument). This service ensures that VisitLog entries are correctly stored in MongoDB while allowing the UI to work with a more user-friendly DTO.

The service utilizes:

* VisitLogDocumentRepository to perform MongoDB operations for saving, retrieving, and deleting VisitLogDocument entities.
* DataManager and EntityStates for handling entity states and references in the Jmix environment.

Here’s the structure of the VisitLogService:

```java
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
   private VisitLog toVisitLog(VisitLogDocument visitLogDocument) {
      VisitLog visitLog = dataManager.create(VisitLog.class);
      entityStates.setNew(visitLog, false);

      visitLog.setId(visitLogDocument.getId());
      visitLog.setVisit(dataManager.getReference(Visit.class, UUID.fromString(visitLogDocument.getVisitId())));
      visitLog.setTitle(visitLogDocument.getTitle());
      visitLog.setDescription(visitLogDocument.getDescription());

      return visitLog;
   }

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
```
This service enables smooth interaction with the UI and MongoDB by handling data conversion, making it possible to:

1. Retrieve VisitLog entries for a specific visit and display them in the UI.
2. Save VisitLog entries by converting them to VisitLogDocument format and persisting them in MongoDB.
3. Delete multiple VisitLog entries at once by leveraging MongoDB’s bulk deletion capabilities.

The service ensures that the persistence and presentation layers are effectively decoupled, making it easier to maintain and extend the application.

### 6. UI Integration with Jmix

The UI in this example is built to use VisitLog DTO entities for displaying visit logs associated with specific visits. Instead of directly interacting with MongoDB, the Jmix UI components interact with VisitLogService, which handles the conversion between VisitLog (UI DTO) and VisitLogDocument (MongoDB persistence entity).

Here’s how each part of the UI integrates with the MongoDB database:

#### VisitLog List View

The VisitLogListView provides a data grid view where users can:

1. View a list of VisitLog entries related to a specific Visit.
2. Create new VisitLog entries, with the current Visit reference automatically associated.
3. Remove selected VisitLog entries.

This list view displays all VisitLog entries associated with a Visit. It utilizes VisitLogService to handle CRUD operations through the following methods:

```java
package io.jmix.petclinic.view.visitlog;

import com.vaadin.flow.router.Route;
import io.jmix.core.LoadContext;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.view.*;
import io.jmix.petclinic.entity.visit.Visit;
import io.jmix.petclinic.view.main.MainView;
import io.jmix.petclinic.visit.log.VisitLog;
import io.jmix.petclinic.visit.log.VisitLogService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.List;
/**
 * List view for managing {@link VisitLog} entries associated with a specific {@link Visit}.
 * <p>
 * This view provides CRUD operations for `VisitLog` entities within a data grid, allowing users to
 * append new entries, view existing entries, and remove selected entries. The view utilizes
 * {@link VisitLogService} for data access and {@link DialogWindows} for handling dialog interactions.
 * </p>
 * <p>
 * The `Visit` entity associated with this view is set from an external source, typically another view
 * or controller. This `visit` parameter ensures that only `VisitLog` entries related to the specific
 * visit are loaded and displayed, offering a contextualized view for the user.
 * </p>
 * <p>
 * Key components and methods:
 * <ul>
 *     <li><b>setVisit(Visit visit)</b> - Setter for the `Visit` parameter that filters `VisitLog` entries
 *     displayed in this view based on the specified visit.</li>
 *     <li><b>onVisitLogsDataGridAppend(ActionPerformedEvent event)</b> - Handles the append action, opening
 *     a dialog for creating a new `VisitLog`. The `visit` reference is set in the dialog to ensure the
 *     `VisitLog` is linked to the correct `Visit` entity.</li>
 *     <li><b>visitLogsDlLoadDelegate(LoadContext loadContext)</b> - Loads `VisitLog` entries associated
 *     with the set `Visit` by using {@link VisitLogService#findByVisit}.</li>
 *     <li><b>visitLogsDataGridRemoveDelegate(Collection visitLogsToRemove)</b> - Handles the remove action
 *     by delegating to {@link VisitLogService#removeVisitLogs} to delete selected entries.</li>
 * </ul>
 * </p>
 *
 * @see VisitLogService
 * @see VisitLogDetailView
 */
@Route(value = "visitLogs", layout = MainView.class)
@ViewController("petclinic_VisitLog.list")
@ViewDescriptor("visit-log-list-view.xml")
@LookupComponent("visitLogsDataGrid")
@DialogMode(width = "50em")
public class VisitLogListView extends StandardListView<VisitLog> {

   private Visit visit;

   @Autowired
   private VisitLogService visitLogService;
   @Autowired
   private DialogWindows dialogWindows;

   /**
    * Opens a dialog to create a new {@link VisitLog} associated with the current {@link Visit}.
    * <p>
    * This method is triggered by the append action in the data grid. A dialog is opened for creating
    * a new `VisitLog`, and the current `visit` reference is injected into the dialog to ensure the new
    * entry is associated with the correct visit. After saving, the data grid is reloaded to display the
    * updated list of `VisitLog` entries.
    * </p>
    *
    * @param event Action event triggering this method.
    */
   @Subscribe("visitLogsDataGrid.append")
   public void onVisitLogsDataGridAppend(final ActionPerformedEvent event) {
      DialogWindow<VisitLogDetailView> dialog = dialogWindows.detail(this, VisitLog.class)
              .withViewClass(VisitLogDetailView.class)
              .withAfterCloseListener(e -> {
                 if (e.closedWith(StandardOutcome.SAVE)) {
                    getViewData().loadAll();
                 }
              })
              .build();

      dialog.getView().setVisit(visit);

      dialog.open();
   }

   /**
    * Loads `VisitLog` entries associated with the specified {@link Visit}.
    * <p>
    * This method delegates the data loading to {@link VisitLogService#findByVisit}, ensuring that
    * only the logs associated with the current `visit` are retrieved and displayed.
    * </p>
    *
    * @param loadContext The load context provided by Jmix.
    * @return A list of `VisitLog` entries associated with the current visit.
    */
   @Install(to = "visitLogsDl", target = Target.DATA_LOADER)
   protected List<VisitLog> visitLogsDlLoadDelegate(LoadContext<VisitLog> loadContext) {
      return visitLogService.findByVisit(visit);
   }

   /**
    * Removes the selected {@link VisitLog} entries from the database.
    * <p>
    * This method is triggered by the remove action in the data grid and uses {@link VisitLogService#removeVisitLogs}
    * to delete the selected `VisitLog` entries.
    * </p>
    *
    * @param visitLogsToRemove The collection of `VisitLog` entries selected for removal.
    */
   @Install(to = "visitLogsDataGrid.remove", subject = "delegate")
   private void visitLogsDataGridRemoveDelegate(final Collection<VisitLog> visitLogsToRemove) {
      visitLogService.removeVisitLogs(visitLogsToRemove);
   }

   /**
    * Sets the {@link Visit} reference for this view.
    * <p>
    * This method provides the visit context for the current `VisitLogListView`, ensuring that
    * only `VisitLog` entries related to this `Visit` are loaded and displayed.
    * </p>
    *
    * @param visit The {@link Visit} entity associated with the `VisitLog` entries displayed in this view.
    */
   public void setVisit(Visit visit) {
      this.visit = visit;
   }
}
```

#### VisitLog Detail View

The VisitLogDetailView allows users to view and edit details for a selected VisitLog entry associated with a specific Visit. This view uses VisitLogService to handle the loading and saving of VisitLog entities, ensuring smooth interaction between the UI and MongoDB.

The VisitLogDetailView view includes several essential methods to manage VisitLog entries, allowing it to interact with the database via VisitLogService:

```java
package io.jmix.petclinic.view.visitlog;

import com.vaadin.flow.router.Route;
import io.jmix.core.LoadContext;
import io.jmix.core.SaveContext;
import io.jmix.flowui.view.*;
import io.jmix.petclinic.entity.visit.Visit;
import io.jmix.petclinic.view.main.MainView;
import io.jmix.petclinic.visit.log.VisitLog;
import io.jmix.petclinic.visit.log.VisitLogService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;

/**
 * Detail view for managing {@link VisitLog} entities within the Jmix UI.
 * <p>
 * This view is routed at `"visitLogs/:id"` and displays details for a selected `VisitLog`. The view uses
 * {@link VisitLogService} to handle save operations, ensuring that the `VisitLog` entity is correctly
 * persisted in the database.
 * </p>
 * <p>
 * The {@link Visit} entity is provided as a parameter to the view, setting up the context of the specific visit
 * associated with this `VisitLog`. Before saving, the `Visit` reference is injected into the `VisitLog` entity,
 * ensuring it is fully prepared for persistence by {@link VisitLogService}.
 * </p>
 * <p>
 * Key components and methods:
 * <ul>
 *     <li><b>setVisit(Visit visit)</b> - Setter method that provides the {@link Visit} entity associated with this
 *     `VisitLog`. The `Visit` reference is later injected into the `VisitLog` before saving.</li>
 *     <li><b>saveDelegate(SaveContext saveContext)</b> - This method overrides the save process, injecting the `Visit`
 *     into the `VisitLog` and delegating the save operation to {@link VisitLogService#saveVisitLog}. The updated
 *     `VisitLog` is returned as a singleton set, satisfying the Jmix save requirements.</li>
 *     <li><b>customerDlLoadDelegate(LoadContext loadContext)</b> - Loads the `VisitLog` by its ID, using
 *     {@link VisitLogService} to retrieve and manage the entity state.</li>
 * </ul>
 * </p>
 *
 * @see VisitLogService
 * @see Visit
 */
@Route(value = "visitLogs/:id", layout = MainView.class)
@ViewController("petclinic_VisitLog.detail")
@ViewDescriptor("visit-log-detail-view.xml")
@EditedEntityContainer("visitLogDc")
public class VisitLogDetailView extends StandardDetailView<VisitLog> {

   private Visit visit;
   @Autowired
   private VisitLogService visitLogService;

   /**
    * Loads the {@link VisitLog} entity based on the given ID.
    * <p>
    * This method retrieves the `VisitLog` entity by its ID using {@link VisitLogService#loadVisitLog(String)}.
    * Once loaded, the entity can be used in the UI, ensuring its state is up-to-date and managed correctly.
    * </p>
    *
    * @param loadContext The load context containing entity information.
    * @return The loaded {@link VisitLog} entity or {@code null} if not implemented.
    */
   @Install(to = "visitLogDl", target = Target.DATA_LOADER)
   private VisitLog customerDlLoadDelegate(final LoadContext<VisitLog> loadContext) {
      return visitLogService.loadVisitLog((String) loadContext.getId());
   }

   /**
    * Saves the {@link VisitLog} entity using {@link VisitLogService}.
    * <p>
    * Before saving, this method sets the associated {@link Visit} reference on the `VisitLog` entity to ensure
    * completeness. The `VisitLogService` then handles the persistence, and the saved `VisitLog` instance
    * is returned in a singleton set as required by the Jmix framework.
    * </p>
    *
    * @param saveContext The save context provided by Jmix.
    * @return A set containing the saved {@link VisitLog} entity.
    */
   @Install(target = Target.DATA_CONTEXT)
   private Set<Object> saveDelegate(final SaveContext saveContext) {
      VisitLog entity = getEditedEntity();

      if (entity.getVisit() == null && visit != null) {
         entity.setVisit(visit);
      }

      return Set.of(visitLogService.saveVisitLog(entity));
   }

   /**
    * Sets the {@link Visit} reference for this view.
    * <p>
    * This method sets the visit context for the current `VisitLog` view. The provided `Visit` reference
    * is later injected into the `VisitLog` entity before saving, establishing the correct association.
    * </p>
    *
    * @param visit The {@link Visit} entity associated with this `VisitLog`.
    */
   public void setVisit(Visit visit) {
      this.visit = visit;
   }
}
```