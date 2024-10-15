<p align="center">
  <img src="https://raw.githubusercontent.com/Haulmont/jmix-petclinic/main/img/petclinic_logo_with_slogan.svg"/>
</p>

# Jmix - MongoDB Integration

This example application demonstrates how to integrate MongoDB into a Jmix application to manage `VisitLog` entries associated with `Visit` entities.

The application uses Jmix and Spring Data MongoDB to create, read, update, and delete `VisitLog` entries in MongoDB. Each `VisitLog` entry is linked to a `Visit`, enabling a logbook-like structure for visits in the system.

## Table of Contents

0. [Adding Dependencies](#0-adding-dependencies)
1. [MongoDB Setup](#1-mongodb-setup)
2. [MongoDB Configuration](#2-mongodb-configuration)
3. [Creating a VisitLog Entity](#3-creating-a-visitlog-entity)
4. [Spring Data MongoDB Repository](#4-spring-data-mongodb-repository)
5. [Service Layer for Visit Logs](#5-service-layer-for-visit-logs)
6. [UI Integration with Jmix](#6-ui-integration-with-jmix)
7. [Usage Screenshots](#7-usage-screenshots)

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

Define a `VisitLog` entity as a DTO in Jmix and annotate it to indicate it should not persist in the relational database. Instead, it will be stored in MongoDB as a document.

```java
package io.jmix.petclinic.visit.log;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.JmixId;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

@Document
@JmixEntity(name = "petclinic_VisitLog")
public class VisitLog {

    @JmixId
    private String id;

    private UUID visitId;
    private String title;
    @InstanceName
    private String description;

    // Getters and setters
}
```

The `VisitLog` entity is marked with `@Document` for MongoDB storage, and `@JmixEntity` for Jmix support in the UI.

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

Create a service to encapsulate the logic for saving, loading, and deleting `VisitLog` entries. This service interacts with `VisitLogDocumentRepository` to perform the database operations.

```java
package io.jmix.petclinic.visit.log;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class VisitLogService {

    private final VisitLogDocumentRepository visitLogDocumentRepository;

    public VisitLogService(VisitLogDocumentRepository visitLogDocumentRepository) {
        this.visitLogDocumentRepository = visitLogDocumentRepository;
    }

    public List<VisitLog> findByVisit(UUID visitId) {
        return visitLogDocumentRepository.findByVisitId(visitId.toString());
    }

    public VisitLog saveVisitLog(VisitLog visitLog) {
        return visitLogDocumentRepository.save(visitLog);
    }

    public void removeVisitLogs(List<VisitLog> visitLogs) {
        visitLogDocumentRepository.deleteAll(visitLogs);
    }
}
```

### 6. UI Integration with Jmix

Integrate `VisitLog` in the Jmix UI by defining views for listing and editing logs.

#### Adding VisitLog List View

Define `VisitLogListView` to display all logs for a selected visit. This view uses `VisitLogService` to fetch data and manage entries.

```java
package io.jmix.petclinic.view.visitlog;

import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.StandardListView;
import io.jmix.petclinic.visit.log.VisitLog;
import io.jmix.petclinic.visit.log.VisitLogService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Route("visitLogs")
public class VisitLogListView extends StandardListView<VisitLog> {

    private final VisitLogService visitLogService;

    @Autowired
    public VisitLogListView(VisitLogService visitLogService) {
        this.visitLogService = visitLogService;
    }

    protected List<VisitLog> loadVisitLogs(UUID visitId) {
        return visitLogService.findByVisit(visitId);
    }
}
```

#### Adding VisitLog Detail View

Define `VisitLogDetailView` for editing `VisitLog` entries. This view allows users to add, edit, and save entries through the UI.

```java
package io.jmix.petclinic.view.visitlog;

import io.jmix.flowui.view.StandardDetailView;
import io.jmix.petclinic.visit.log.VisitLog;
import io.jmix.petclinic.visit.log.VisitLogService;
import org.springframework.beans.factory.annotation.Autowired;

public class VisitLogDetailView extends StandardDetailView<VisitLog> {

    @Autowired
    private VisitLogService visitLogService;

    protected void saveVisitLog() {
        visitLogService.saveVisitLog(getEditedEntity());
    }
}
```

### 7. Usage Screenshots

To demonstrate the functionality, here are example screenshots of the various screens:

1. **Visit List View with Visit Log Button**

   ![Visit List View with Visit Log Button](img/screenshots/1-visit-log-button.png)

2. **Visit Log Entries List View**

   ![Visit Log Entries List View](img/screenshots/2-visit-log-list.png)

3. **Visit Log Detail View**

   ![Visit Log Detail View](img/screenshots/3-visit-log-detail.png)

### Additional Notes

- **Running the Application**: Start MongoDB with Docker Compose, then run the Jmix application to manage visit logs.
- **Database Configuration**: Use the MongoDB URI in `application.properties` based on the Docker credentials to ensure connectivity.

This sample demonstrates a simple integration of MongoDB in Jmix for managing entity logs and can be extended with more complex features as needed.
