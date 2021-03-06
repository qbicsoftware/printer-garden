package life.qbic.presenter;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.vaadin.data.util.sqlcontainer.SQLContainer;
import com.vaadin.data.util.sqlcontainer.query.FreeformQuery;
import com.vaadin.ui.Grid;
import life.qbic.model.database.Database;
import life.qbic.model.database.Query;
import life.qbic.model.main.MyPortletUI;
import life.qbic.model.tables.Table;
import life.qbic.model.tables.printer.PrinterFields;
import life.qbic.model.tables.printerProjectAssociation.PrinterProjectAssociation;
import life.qbic.model.tables.printerProjectAssociation.PrinterProjectFields;
import life.qbic.model.tables.project.ProjectFields;
import life.qbic.utils.MyNotification;
import org.javatuples.Pair;
import life.qbic.view.forms.PrinterProjectFormView;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * This class {@link PrinterProjectAssociation} handles operations on the printer project table view.
 *
 * @author fhanssen
 */
class PrinterProjectPresenter {

    private final PrinterProjectFormView form;
    private final Database database;
    private final Grid grid;

    private static final Log log = LogFactoryUtil.getLog(PrinterProjectPresenter.class.getName());


    PrinterProjectPresenter(PrinterProjectFormView form, Database database, Grid grid){
        this.form = form;
        this.database = database;
        this.grid = grid;

        setUpListener();
    }

    private void setUpListener(){
        saveButtonListener();
        deleteButtonListener();

    }

    /**
     * When the save button is pressed, all values of the form fields are read.
     */
    private void saveButtonListener(){
        this.form.getSaveButton().addClickListener(clickEvent -> {
            if (this.form.getPrinterNameLocation() == null || this.form.getProjectName().isEmpty()) {
                MyNotification.notification("Information", "Please enter information!", "" );
                log.info(MyPortletUI.toolname + ": " + "No information to safe was provided in the printer-project form.");

            }else{
                log.info(MyPortletUI.toolname + ": " + "New entry is saved to printer-project table.");
                saveToPrinterProject(form.getFormEntries());
                reload();
                this.form.emptyForm();
            }
        });
    }

    /**
     * A new entry is saved to a printer project. Due to the joint queries when setting up the table, they correct
     * foreign keys need to be extracted to save a database entry.
     * @param entry new Printer Project
     */
    private void saveToPrinterProject(PrinterProjectAssociation entry) {

        log.info(MyPortletUI.toolname + ": " +"Try to save new entry with: \n" +
                "\tprinterName:\t" + entry.getPrinterName() + "\n" +
                "\tprojectName:\t" + entry.getProjectName()  + "\n" +
                "\tstatus:\t" + entry.getStatus());

        List<String> entries = Arrays.asList("printer_id", "project_id", "status");

        String selectPrinterId = Query.selectFromWhereAnd(Collections.singletonList(PrinterFields.ID.toString()),
                Collections.singletonList(Table.labelprinter.toString()),
                Arrays.asList(new Pair<>(PrinterFields.NAME.toString(), entry.getPrinterName()),
                        new Pair<>(PrinterFields.LOCATION.toString(), entry.getPrinterLocation())));
        String selectProjectId = Query.selectFromWhereAnd(Collections.singletonList(ProjectFields.ID.toString()),
                Collections.singletonList(Table.projects.toString()),
                Collections.singletonList(new Pair<>(ProjectFields.OPENBISID.toString(), entry.getProjectName())));

        database.save(Table.printer_project_association.toString(), entries, Arrays.asList(
                "(" + selectPrinterId + ")", "(" + selectProjectId + ")", "'" + entry.getStatus().toString() + "'"), false);

    }

    /**
     * Reloads table views and the delete id combobox, which is dependent on the current table entries.
     */
    private void reload(){
        grid.clearSortOrder();
        try {
            SQLContainer allExisIds = new SQLContainer(new FreeformQuery(
                    Query.selectFrom(Collections.singletonList(PrinterProjectFields.ID.toString()),
                            Collections.singletonList(Table.printer_project_association.toString()))+";",
                    database.getPool()));
            form.setExistingIDs(allExisIds);
        }catch(SQLException e){
            MyNotification.notification("Error", "Database access failed.", "error");
            log.error(MyPortletUI.toolname + ": " +"ID combobox values could not be updated: " + e.getMessage());
        }
    }

    private void deleteButtonListener() {
        this.form.getDeleteButton().addClickListener(clickEvent -> {
            deleteEntry();
            reload();
            this.form.emptyForm();
        });
    }

    /**
     * An entry belonging to a selected ID is deleted.
     */
    private void deleteEntry() {
        if (this.form.getRowID() == null || this.form.getRowID().isEmpty()) {
            log.info(MyPortletUI.toolname + ": " +"No information to delete was provided in the printer-project form.");
            MyNotification.notification("Information", "Please enter information!", "" );
        } else {
            log.info(MyPortletUI.toolname + ": " +"Entry with ID " + this.form.getRowID().getItem(
                    this.form.getRowID().getValue()).toString().split(":")[2] +" is deleted");
            database.delete(Table.printer_project_association.toString(), this.form.getRowID().getItem(
                    this.form.getRowID().getValue()).toString().split(":")[2]);
            this.form.emptyForm();
            reload();

        }
    }
}
