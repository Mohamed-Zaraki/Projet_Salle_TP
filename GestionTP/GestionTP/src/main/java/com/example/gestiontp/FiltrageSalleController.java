package com.example.gestiontp;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Stage;
import javafx.util.converter.DefaultStringConverter;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;


public class FiltrageSalleController implements Initializable {

    @FXML private ChoiceBox<String> heurestartchoice;
    @FXML private ChoiceBox<String> heureendchoice;
    @FXML private ChoiceBox<String> datejourchoice;
    @FXML private ChoiceBox<String> datemoischoice;

    @FXML private TableView<FiltrageCaracteristiques> filtreTable;
    @FXML private TableColumn<FiltrageCaracteristiques, String> ramColumn2;
    @FXML private TableColumn<FiltrageCaracteristiques, String> logicielsColumn2;
    @FXML private TableColumn<FiltrageCaracteristiques, String> capaciteColumn2;
    @FXML private TableColumn<FiltrageCaracteristiques, String> seColumn2;
    @FXML private CheckBox check;

    @FXML private Button samedibutton;
    @FXML private Button dimanchebutton;
    @FXML private Button lundibutton;
    @FXML private Button mardibutton;
    @FXML private Button mercredibutton;
    @FXML private Button jeudibutton;

    private Button selectedDayButton = null;
    public static LocalDate selectedDate;
    public static LocalTime selectedHeureDebut;
    public static LocalTime selectedHeureFin;
    public static String selectedJour;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Populate time choice boxes
        ObservableList<String> heures = FXCollections.observableArrayList("08:00", "09:30", "11:00", "12:30", "14:00", "15:30", "17:00");
        heurestartchoice.setItems(heures);
        heureendchoice.setItems(heures);
        heurestartchoice.setValue("08:00");
        heureendchoice.setValue("09:30");

        // Populate day choice box
        ObservableList<String> jours = FXCollections.observableArrayList();
        for (int i = 1; i <= 31; i++) {
            jours.add(String.valueOf(i));
        }
        datejourchoice.setItems(jours);
        datejourchoice.setValue("27");

        // Populate month choice box
        ObservableList<String> mois = FXCollections.observableArrayList("01", "02", "03", "04", "05", "09", "10", "11", "12");
        datemoischoice.setItems(mois);
        datemoischoice.setValue("05");

        // Configure the table view
        ramColumn2.setCellValueFactory(new PropertyValueFactory<>("ram"));
        logicielsColumn2.setCellValueFactory(new PropertyValueFactory<>("logiciels"));
        capaciteColumn2.setCellValueFactory(new PropertyValueFactory<>("capaciteSalle"));
        seColumn2.setCellValueFactory(new PropertyValueFactory<>("se"));

        filtreTable.setEditable(true);
        ramColumn2.setCellFactory(TextFieldTableCell.forTableColumn(new DefaultStringConverter()));
        logicielsColumn2.setCellFactory(TextFieldTableCell.forTableColumn(new DefaultStringConverter()));
        capaciteColumn2.setCellFactory(TextFieldTableCell.forTableColumn(new DefaultStringConverter()));
        seColumn2.setCellFactory(TextFieldTableCell.forTableColumn(new DefaultStringConverter()));

        ramColumn2.setOnEditCommit(event -> event.getRowValue().setRam(event.getNewValue()));
        logicielsColumn2.setOnEditCommit(event -> event.getRowValue().setLogiciels(event.getNewValue()));
        capaciteColumn2.setOnEditCommit(event -> event.getRowValue().setCapaciteSalle(event.getNewValue()));
        seColumn2.setOnEditCommit(event -> event.getRowValue().setSe(event.getNewValue()));

        ObservableList<FiltrageCaracteristiques> data = FXCollections.observableArrayList(new FiltrageCaracteristiques("", "", "", "", "", ""));
        filtreTable.setItems(data);
        filtreTable.setFixedCellSize(40);
        filtreTable.setPrefHeight(40 * data.size() + 30);
        filtreTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        filtreTable.setPlaceholder(new Label(""));
    }

    @FXML
    private void ChoisirJourButton(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();

        if (selectedDayButton != null && selectedDayButton != clickedButton) {
            selectedDayButton.setStyle("-fx-background-color: #C3CED3;");
        }

        if (clickedButton == selectedDayButton) {
            clickedButton.setStyle("-fx-background-color: #C3DED3;");
            selectedDayButton = null;
        } else {
            clickedButton.setStyle("-fx-background-color: #012A4A;");
            selectedDayButton = clickedButton;
        }
    }

    private void showAlert(String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    //Database Tools
    private Connection connect;
    private PreparedStatement prepare;
    private ResultSet result;

    @FXML
    public void ConfirmerFiltrage(ActionEvent event) throws IOException {
        String jourStr = datejourchoice.getSelectionModel().getSelectedItem();
        String moisStr = datemoischoice.getSelectionModel().getSelectedItem();
        String year = String.valueOf(LocalDate.now().getYear());

        if (jourStr == null || moisStr == null) {
            showAlert("Sélectionnez un jour/mois valide.");
            return;
        }

        int jour = Integer.parseInt(jourStr.trim());
        int mois = Integer.parseInt(moisStr.trim());
        LocalDate date = LocalDate.of(Integer.parseInt(year), mois, jour);

        String dayOfWeek = date.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.FRENCH);
        dayOfWeek = dayOfWeek.substring(0, 1).toUpperCase() + dayOfWeek.substring(1);

        String heureDebutRaw = heurestartchoice.getSelectionModel().getSelectedItem();
        String heureFinRaw = heureendchoice.getSelectionModel().getSelectedItem();


        // Vérifie que les deux ne sont pas nulles
        if (heureDebutRaw == null || heureFinRaw == null) {
            showAlert("Veuillez sélectionner une heure de début et de fin.");
            return;
        }

       // Compare les deux heures en tant que chaînes
        if (heureDebutRaw.compareTo(heureFinRaw) >= 0) {
            showAlert("L'heure de début doit être inférieure à l'heure de fin.");
            return;
        }


        String heureDebut = heureDebutRaw.trim() + ":00";
        String heureFin = heureFinRaw.trim() + ":00";

        FiltrageSalleController.selectedDate = date;
        FiltrageSalleController.selectedHeureDebut = LocalTime.parse(heureDebut);
        FiltrageSalleController.selectedHeureFin = LocalTime.parse(heureFin);
        FiltrageSalleController.selectedJour = dayOfWeek;

        FiltrageCaracteristiques filter = filtreTable.getItems().get(0);
        String requiredRAM = filter.getRam();
        String requiredLogiciels = filter.getLogiciels();
        String requiredCapacite = filter.getCapaciteSalle();
        String requiredSE = filter.getSe();

        ObservableList<String> availableRooms = FXCollections.observableArrayList();
        ObservableList<String> filteredAvailableRooms = FXCollections.observableArrayList();

        try (Connection connect = Database.connectDB()) {
            //Récupérer les salles disponibles
            String query = """
            SELECT Nom_Salle
            FROM salle_tp
            WHERE Nom_Salle NOT IN (
                SELECT salle_tp.Nom_Salle
                FROM salle_tp
                JOIN emploi_du_temps e ON salle_tp.Nom_Salle = e.Nom_Salle
                WHERE e.jour = ?
                  AND e.Heure_Debut = ?
                  AND e.Heure_fin = ?
                GROUP BY salle_tp.Nom_Salle
            )
            AND Nom_Salle NOT IN (
                     SELECT er.Nom_Salle
                     FROM est_réserver er
                     JOIN réservation r ON er.id_réservation = r.id_réservation
                     WHERE r.date_réservation = ?
                       AND r.Heure_Debut = ?
                       AND r.Heure_Fin = ?
                 )
             
        """;

            try (PreparedStatement prepare = connect.prepareStatement(query)) {
                prepare.setString(1, dayOfWeek);
                prepare.setString(2, heureDebut);
                prepare.setString(3, heureFin);
                prepare.setDate(4, java.sql.Date.valueOf(date)); // date from LocalDate
                prepare.setString(5, heureDebut);
                prepare.setString(6, heureFin);

                ResultSet result = prepare.executeQuery();
                while (result.next()) {
                    availableRooms.add(result.getString("Nom_Salle"));
                }
            }

            // filtres RAM, capacité, logiciels
            if (!requiredRAM.isBlank() || !requiredLogiciels.isBlank() || !requiredCapacite.isBlank() || !requiredSE.isBlank() || check.isSelected()) {
                StringBuilder sql = new StringBuilder("""
                SELECT salle_tp.Nom_Salle
                FROM salle_tp
                JOIN ordinateur ON ordinateur.Nom_Salle = salle_tp.Nom_Salle
                LEFT JOIN instalés ON salle_tp.Nom_Salle = instalés.Nom_Salle
                LEFT JOIN logiciel ON logiciel.id_logiciel = instalés.id_logiciel
                WHERE 1=1
            """);

                List<Object> parameters = new ArrayList<>();

                if (!requiredRAM.isBlank()) {
                    sql.append(" AND ordinateur.ram >= ? ");
                    parameters.add(Integer.parseInt(requiredRAM.trim()));
                }

                if (!requiredCapacite.isBlank()) {
                    sql.append(" AND salle_tp.Capacité >= ?");
                    parameters.add(Integer.parseInt(requiredCapacite.trim()));
                }

                List<String> seList = new ArrayList<>();
                if (!requiredSE.isBlank()) {
                    seList = Arrays.stream(requiredSE.split(","))
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .map(String::toUpperCase)
                            .collect(Collectors.toList());
                }

                if (!seList.isEmpty()) {
                    sql.append(" AND (");
                    for (int i = 0; i < seList.size(); i++) {
                        sql.append("UPPER(ordinateur.Type_SE) LIKE ?");
                        parameters.add("%" + seList.get(i) + "%");
                        if (i < seList.size() - 1) {
                            sql.append(" AND ");
                        }
                    }
                    sql.append(")");
                }


                if (check.isSelected()) {
                    sql.append(" AND salle_tp.Internet = 1 ");
                }


                List<String> logicielsList = new ArrayList<>();
                if (!requiredLogiciels.isBlank()) {
                    logicielsList = Arrays.stream(requiredLogiciels.split(","))
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .map(String::toUpperCase)
                            .collect(Collectors.toList());
                }

                if (!logicielsList.isEmpty()) {
                    sql.append(" GROUP BY salle_tp.Nom_Salle HAVING COUNT(DISTINCT CASE ");
                    for (String logiciel : logicielsList) {
                        sql.append(" WHEN UPPER(logiciel.Nom_logiciel) LIKE ? THEN logiciel.Nom_logiciel ");
                        parameters.add("%" + logiciel + "%");
                    }
                    sql.append(" END) = ? ");
                    parameters.add(logicielsList.size());
                }

                try (PreparedStatement charStmt = connect.prepareStatement(sql.toString())) {
                    for (int i = 0; i < parameters.size(); i++) {
                        charStmt.setObject(i + 1, parameters.get(i));
                    }

                    ResultSet charResult = charStmt.executeQuery();
                    while (charResult.next()) {
                        String matchedRoom = charResult.getString("Nom_Salle");
                        if (availableRooms.contains(matchedRoom) && !filteredAvailableRooms.contains(matchedRoom)) {
                            filteredAvailableRooms.add(matchedRoom);
                        }
                    }
                }
            } else {
                filteredAvailableRooms.addAll(availableRooms);
            }

            if (filteredAvailableRooms.isEmpty()) {
                showAlert("Aucune salle disponible ne correspond aux critères.");
                return;
            }

            Stage popupStage = Main.openPopupWindow2("Reservation.fxml", "Reservation", 450, 400);
            popupStage.setX(80);
            popupStage.setY(160);

            FXMLLoader loader = Main.getLoader();
            ReservationController controller = loader.getController();
            controller.setAvailableRooms(filteredAvailableRooms);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void CheckInternet(ActionEvent event) throws IOException {

        /*back-end*/
    }




    public class FiltrageCaracteristiques {
        private final StringProperty ram;
        private final StringProperty logiciels;
        private final StringProperty capaciteSalle;
        private final StringProperty autre1;
        private final StringProperty autre2;

        public FiltrageCaracteristiques(String ram, String logiciels, String capaciteSalle, String autre1, String autre2, String se) {
            this.ram = new SimpleStringProperty(ram);
            this.logiciels = new SimpleStringProperty(logiciels);
            this.capaciteSalle = new SimpleStringProperty(capaciteSalle);
            this.autre1 = new SimpleStringProperty(autre1);
            this.autre2 = new SimpleStringProperty(autre2);
            this.se = new SimpleStringProperty(se);
        }

        public String getRam() { return ram.get(); }
        public void setRam(String value) { ram.set(value); }
        public StringProperty ramProperty() { return ram; }

        public String getLogiciels() { return logiciels.get(); }
        public void setLogiciels(String value) { logiciels.set(value); }
        public StringProperty logicielsProperty() { return logiciels; }

        public String getCapaciteSalle() { return capaciteSalle.get(); }
        public void setCapaciteSalle(String value) { capaciteSalle.set(value); }
        public StringProperty capaciteSalleProperty() { return capaciteSalle; }

        public String getSe() { return se.get(); }
        public void setSe(String value) { se.set(value); }
        private final StringProperty se;
    }

    public static LocalDate getDate() {
        String jourStr = "27";
        String moisStr = "05";
        int year = LocalDate.now().getYear();
        return LocalDate.of(year, Integer.parseInt(moisStr), Integer.parseInt(jourStr));
    }

    public static String getJour() {
        return getDate().getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.FRENCH);
    }

    public static LocalTime getHeureDebut() {
        return LocalTime.parse("08:00");
    }

}