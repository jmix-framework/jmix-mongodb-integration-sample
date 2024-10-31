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

// tag::class[]
@Route(value = "visitLogs/:id", layout = MainView.class)
@ViewController("petclinic_VisitLog.detail")
@ViewDescriptor("visit-log-detail-view.xml")
@EditedEntityContainer("visitLogDc")
public class VisitLogDetailView extends StandardDetailView<VisitLog> {

    private Visit visit;
    @Autowired
    private VisitLogService visitLogService;

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
    @Install(target = Target.DATA_CONTEXT) // <1>
    private Set<Object> saveDelegate(final SaveContext saveContext) {
        VisitLog visitLog = getEditedEntity();

        if (visitLog.getVisit() == null && visit != null) {
            visitLog.setVisit(visit); // <2>
        }

        VisitLog savedVisitLog = visitLogService.saveVisitLog(visitLog); // <3>

        return Set.of(savedVisitLog);
    }

    // end::class[]

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

    // tag::end-class[]
}
// end::end-class[]