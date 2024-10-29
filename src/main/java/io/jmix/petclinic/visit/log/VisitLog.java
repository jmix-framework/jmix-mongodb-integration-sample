package io.jmix.petclinic.visit.log;

import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.petclinic.entity.visit.Visit;
import jakarta.validation.constraints.NotNull;

// tag::imports[]
import io.jmix.core.entity.annotation.JmixId;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.JmixProperty;

// end::imports[]

/**
 * DTO entity used for the UI representation of visit logs in Jmix.
 * <p>
 * This class is designed for interaction with the Jmix framework and is primarily used in data grids,
 * forms, and other UI components where user interaction occurs. Unlike the {@link VisitLogDocument}
 * entity, which handles persistence to MongoDB, this `VisitLog` entity acts as a Data Transfer Object (DTO)
 * specifically for UI manipulation and display purposes.
 * </p>
 * <p>
 * The {@link JmixEntity} annotation registers this class as a Jmix-managed entity, enabling it to be
 * used in data containers and views within the Jmix UI. The {@link JmixId} annotation indicates the primary
 * identifier for this entity, while {@link InstanceName} marks the `description` field as the name
 * to be displayed in Jmix views.
 * </p>
 * <p>
 * Field descriptions:
 * <ul>
 *     <li><b>id</b> - Unique identifier used within Jmix for referencing this `VisitLog` instance in the UI.</li>
 *     <li><b>visit</b> - Represents an association to the {@link Visit} entity, enabling the UI to display and interact with
 *     associated visit data directly. In the persistent {@link VisitLogDocument} entity, only the `visitId` (identifier)
 *     is stored to simplify data management and prevent deep serialization issues.</li>
 *     <li><b>title</b> - Title or summary of the visit log, often displayed in list views or summary panels.</li>
 *     <li><b>description</b> - Detailed description or notes for the visit log, marked with {@link InstanceName}
 *     for easy identification within Jmix components.</li>
 * </ul>
 * </p>
 * <p>
 * For additional details on using DTO entities in Jmix, refer to the
 * <a href="https://docs.jmix.io/jmix/data-model/entities.html#dto">Jmix documentation on DTO entities</a>.
 * </p>
 *
 * @see VisitLogDocument
 * @see Visit
 * @see JmixEntity
 * @see InstanceName
 */

// tag::visit-log-entity[]
@JmixEntity(name = "petclinic_VisitLog")
public class VisitLog {

    @JmixId
    private String id;

    @JmixProperty(mandatory = true)
    @NotNull
    private Visit visit;

    private String title;

    @InstanceName
    private String description;

    // ...

    // end::visit-log-entity[]

    public Visit getVisit() {
        return visit;
    }

    public void setVisit(Visit visit) {
        this.visit = visit;
    }

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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

// tag::closing-class[]

}
// end::closing-class[]