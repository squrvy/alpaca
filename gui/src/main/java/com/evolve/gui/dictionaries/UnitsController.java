package com.evolve.gui.dictionaries;

import com.evolve.gui.StageManager;
import com.evolve.gui.events.StageReadyEvent;
import com.evolve.services.UnitsService;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.rgielen.fxweaver.core.FxmlView;
import org.apache.commons.lang.StringUtils;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.Comparator;
import java.util.ResourceBundle;

import static com.evolve.gui.StageManager.APPLICATION_ICON;

@Getter
@Component
@FxmlView("units-dialog.fxml")
@RequiredArgsConstructor
@Slf4j
public class UnitsController implements Initializable, ApplicationListener<StageReadyEvent> {
    private static final String UNITS_DIALOG_TITLE = "Jednostki";
    private final UnitsService unitsService;
    private final ObservableList<UnitEntry> units = FXCollections.observableArrayList();
    private final BooleanProperty listWasModifiedProperty = new SimpleBooleanProperty(false);

    private Stage primaryStage;
    private Stage stage;

    private StageManager stageManager;

    @FXML HBox unitsDialog;
    @FXML TableView<UnitEntry> unitsTable;
    @FXML TableColumn<UnitEntry, String> unitNumberColumn;
    @FXML TableColumn<UnitEntry, String> unitDescriptionColumn;

    @FXML Button btnAddUnit;
    @FXML Button btnReloadUnits;
    @FXML Button btnSaveUnits;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.stage = new Stage();
        stage.initOwner(primaryStage);
        stage.setScene(new Scene(unitsDialog));
        stage.setTitle(UNITS_DIALOG_TITLE);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.getIcons().add(APPLICATION_ICON);

        unitNumberColumn.setCellValueFactory(new PropertyValueFactory<>("unitNumber"));
        unitDescriptionColumn.setCellValueFactory(new PropertyValueFactory<>("unitDescription"));

        listWasModifiedProperty.addListener((observableValue, oldValue, newValue) -> {
            stage.setTitle(UNITS_DIALOG_TITLE + (newValue ? " *" : ""));
            //btnSaveUnits.setDisable(!newValue);
        });

        loadUnits();

        unitsTable.setRowFactory(tableView -> {
            final TableRow<UnitEntry> row = new TableRow<>();
            final ContextMenu contextMenu = new ContextMenu();

            final MenuItem editMenuItem = new MenuItem("Edytuj");
            editMenuItem.setOnAction(event -> {
//                new AuthorizedPersonDialog(row.getItem().getAuthorizedPerson())
//                        .showDialog(stageManager.getWindow())
//                        .ifPresent(person -> {
//                            row.getItem().setAuthorizedPerson(person);
//                            tableView.refresh();
//                        });
            });
            //editMenuItem.disableProperty().bind(disabledProperty);

            final MenuItem removeMenuItem = new MenuItem("Usuń");
            removeMenuItem.setOnAction(event -> {
//                list.remove(row.getItem());
//                tableView.refresh();
            });
//            removeMenuItem.disableProperty().bind(disabledProperty);

            contextMenu.getItems().add(editMenuItem);
            contextMenu.getItems().add(removeMenuItem);

            // Set context menu on row, but use a binding to make it only show for non-empty rows:
            row.contextMenuProperty().bind(Bindings.when(row.emptyProperty()).then((ContextMenu) null)
                    .otherwise(contextMenu));
            return row;
        });

    }

    public void loadUnits() {
        unitsService.fetchList()
                .forEach(unit -> this.units.add(new UnitEntry(unit.getId(), StringUtils.trimToEmpty(unit.getName()))));
        unitsTable.setItems(this.units);
    }

    public void show() {
        stage.show();
    }

    @Override
    public void onApplicationEvent(StageReadyEvent event) {
        this.primaryStage = event.getStage();
        log.info("primaryStage set");
    }

    public void onAddUnit(ActionEvent actionEvent) {
        new NewUnitDialog(units)
                .showDialog(stage.getOwner())
                .ifPresent(newUnit -> {
                    unitsService.insertUnit(newUnit);

                    final UnitEntry newEntry = new UnitEntry(newUnit.getId(), newUnit.getName());
                    units.add(newEntry);
                    units.sort(Comparator.comparing(UnitEntry::getUnitNumber));
                    unitsTable.refresh();
                    unitsTable.scrollTo(newEntry);
                    listWasModifiedProperty.set(true);
                });
    }

    @AllArgsConstructor
    @Getter
    public static class UnitEntry {
        private String unitNumber;
        private String unitDescription;
    }
}
