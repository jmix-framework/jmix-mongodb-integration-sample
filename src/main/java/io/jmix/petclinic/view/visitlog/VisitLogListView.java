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
// tag::class[]
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
     * Sets the {@link Visit} reference for this view.
     * <p>
     * This method provides the visit context for the current `VisitLogListView`, ensuring that
     * only `VisitLog` entries related to this `Visit` are loaded and displayed.
     * </p>
     *
     * @param visit The {@link Visit} entity associated with the `VisitLog` entries displayed in this view.
     */
    public void setVisit(Visit visit) {
        this.visit = visit; // <1>
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
    @Install(to = "visitLogsDl", target = Target.DATA_LOADER) // <2>
    protected List<VisitLog> visitLogsDlLoadDelegate(LoadContext<VisitLog> loadContext) {
        return visitLogService.findByVisit(visit); // <3>
    }

    // end::class[]

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
    // tag::end-class[]
}

// end::end-class[]