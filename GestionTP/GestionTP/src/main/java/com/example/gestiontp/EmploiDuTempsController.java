package com.example.gestiontp;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.Optional;
import java.util.ResourceBundle;

public class EmploiDuTempsController implements Initializable {

    @FXML
    private AnchorPane EmploiDuTempsInterf;
    @FXML
    private Button Filtrer;
    @FXML
    private TabPane mainTabPane; // Référence au TabPane principal

    // Menu links
    @FXML
    private Hyperlink acceuilLink;
    @FXML
    private Hyperlink emploidutempsLink;
    @FXML
    private Hyperlink stockLink;
    @FXML
    private Hyperlink loggingLink;
    @FXML
    private Hyperlink deconnecterLink;
    @FXML
    private ToggleButton ModeToggle;
    @FXML
    private Label Mode;

    private String sallename;

    @FXML
    private Button ManuelButton;

    // Room groups
    @FXML
    private VBox A21Grp, A22Grp, A23Grp, A24Grp, A25Grp;
    @FXML
    private VBox A31Grp, A32Grp, A33Grp, A34Grp, UNIXGrp;
    @FXML
    private VBox A41Grp, A42Grp, A43Grp;

    // Room titles
    @FXML
    private Text A21Edt, A22Edt, A23Edt, A24Edt, A25Edt;
    @FXML
    private Text A31Edt, A32Edt, A33Edt, A34Edt, UNIXEdt;
    @FXML
    private Text A41Edt, A42Edt, A43Edt;

    // TableViews
    @FXML
    private TableView<ScheduleRow> timeTableView21, timeTableView22, timeTableView23, timeTableView24, timeTableView25;
    @FXML
    private TableView<ScheduleRow> timeTableView31, timeTableView32, timeTableView33, timeTableView34, timeTableViewUNIX;
    @FXML
    private TableView<ScheduleRow> timeTableView41, timeTableView42, timeTableView43;

    // Upload buttons
    @FXML
    private Button A21Button, A22Button, A23Button, A24Button, A25Button;
    @FXML
    private Button A31Button, A32Button, A33Button, A34Button, UNIXButton;
    @FXML
    private Button A41Button, A42Button, A43Button;

    public void EffacerEmploi() {
        String SQL = "DELETE FROM emploi_du_temps ";
        try(Connection conn = Database.connectDB();
            PreparedStatement ps = conn.prepareStatement(SQL);)
        {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation");
            alert.setHeaderText(" Confirmer cette decision");
            alert.setContentText("Voulez-vous supprimer tous les emplois du temps des salles ?");
            Optional<ButtonType> result = alert.showAndWait();


            if (result.isPresent() && result.get() == ButtonType.OK) {
                ps.executeUpdate();

            } else {
                return;
            }
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    // Data model for schedule rows
    public static class ScheduleRow {
        private final SimpleStringProperty day;
        private final SimpleStringProperty slot1;
        private final SimpleStringProperty slot2;
        private final SimpleStringProperty slot3;
        private final SimpleStringProperty slot4;
        private final SimpleStringProperty slot5;
        private final SimpleStringProperty slot6;

        public ScheduleRow(String day, String slot1, String slot2, String slot3, String slot4, String slot5, String slot6) {
            this.day = new SimpleStringProperty(day);
            this.slot1 = new SimpleStringProperty(slot1);
            this.slot2 = new SimpleStringProperty(slot2);
            this.slot3 = new SimpleStringProperty(slot3);
            this.slot4 = new SimpleStringProperty(slot4);
            this.slot5 = new SimpleStringProperty(slot5);
            this.slot6 = new SimpleStringProperty(slot6);
        }

        public String getDay() { return day.get(); }
        public void setDay(String value) { day.set(value); }
        public SimpleStringProperty dayProperty() { return day; }

        public String getSlot1() { return slot1.get(); }
        public void setSlot1(String value) { slot1.set(value); }
        public SimpleStringProperty slot1Property() { return slot1; }

        public String getSlot2() { return slot2.get(); }
        public void setSlot2(String value) { slot2.set(value); }
        public SimpleStringProperty slot2Property() { return slot2; }

        public String getSlot3() { return slot3.get(); }
        public void setSlot3(String value) { slot3.set(value); }
        public SimpleStringProperty slot3Property() { return slot3; }

        public String getSlot4() { return slot4.get(); }
        public void setSlot4(String value) { slot4.set(value); }
        public SimpleStringProperty slot4Property() { return slot4; }

        public String getSlot5() { return slot5.get(); }
        public void setSlot5(String value) { slot5.set(value); }
        public SimpleStringProperty slot5Property() { return slot5; }

        public String getSlot6() { return slot6.get(); }
        public void setSlot6(String value) { slot6.set(value); }
        public SimpleStringProperty slot6Property() { return slot6; }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Debug: vérifier le nombre de tabs
        System.out.println("Nombre de tabs: " + (mainTabPane != null ? mainTabPane.getTabs().size() : "TabPane null"));

        // Appliquer le style CSS seulement si le TabPane existe et a des tabs
        if (mainTabPane != null && !mainTabPane.getTabs().isEmpty()) {
            System.out.println("Application du style CSS...");
            mainTabPane.getStyleClass().add("equal-width-tabs");
        } else {
            System.out.println("ERREUR: TabPane vide ou null!");
        }

        // Initialize all tables with sample data
        initializeAllTables();

        // Charger les données dans chaque table après initialisation des colonnes
        loadScheduleData("A21", timeTableView21);
        loadScheduleData("A22", timeTableView22);
        loadScheduleData("A23", timeTableView23);
        loadScheduleData("A24", timeTableView24);
        loadScheduleData("A25", timeTableView25);
        loadScheduleData("A31", timeTableView31);
        loadScheduleData("A32", timeTableView32);
        loadScheduleData("A33", timeTableView33);
        loadScheduleData("A34", timeTableView34);
        loadScheduleData("UNIX", timeTableViewUNIX);
        loadScheduleData("A41", timeTableView41);
        loadScheduleData("A42", timeTableView42);
        loadScheduleData("A43", timeTableView43);
    }

    // Reste du code du contrôleur inchangé...
    // (toutes les autres méthodes restent identiques)

    private void autoRefreshTable(String salle, TableView<ScheduleRow> tableView) {
        loadScheduleData(salle, tableView);
    }

    private void initializeAllTables() {
        // Initialize each table with columns and empty days
        initializeTable(timeTableView21);
        initializeTable(timeTableView22);
        initializeTable(timeTableView23);
        initializeTable(timeTableView24);
        initializeTable(timeTableView25);
        initializeTable(timeTableView31);
        initializeTable(timeTableView32);
        initializeTable(timeTableView33);
        initializeTable(timeTableView34);
        initializeTable(timeTableViewUNIX);
        initializeTable(timeTableView41);
        initializeTable(timeTableView42);
        initializeTable(timeTableView43);
    }

    private void initializeTable(TableView<ScheduleRow> tableView) {
        tableView.setEditable(false);

        TableColumn<ScheduleRow, String> dayColumn = (TableColumn<ScheduleRow, String>) tableView.getColumns().get(0);
        dayColumn.setCellValueFactory(cellData -> cellData.getValue().dayProperty());
        dayColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        dayColumn.setOnEditCommit(event -> event.getRowValue().setDay(event.getNewValue()));

        TableColumn<ScheduleRow, String> slot1Column = (TableColumn<ScheduleRow, String>) tableView.getColumns().get(1);
        slot1Column.setCellValueFactory(cellData -> cellData.getValue().slot1Property());
        slot1Column.setCellFactory(TextFieldTableCell.forTableColumn());
        slot1Column.setOnEditCommit(event -> event.getRowValue().setSlot1(event.getNewValue()));

        TableColumn<ScheduleRow, String> slot2Column = (TableColumn<ScheduleRow, String>) tableView.getColumns().get(2);
        slot2Column.setCellValueFactory(cellData -> cellData.getValue().slot2Property());
        slot2Column.setCellFactory(TextFieldTableCell.forTableColumn());
        slot2Column.setOnEditCommit(event -> event.getRowValue().setSlot2(event.getNewValue()));

        TableColumn<ScheduleRow, String> slot3Column = (TableColumn<ScheduleRow, String>) tableView.getColumns().get(3);
        slot3Column.setCellValueFactory(cellData -> cellData.getValue().slot3Property());
        slot3Column.setCellFactory(TextFieldTableCell.forTableColumn());
        slot3Column.setOnEditCommit(event -> event.getRowValue().setSlot3(event.getNewValue()));

        TableColumn<ScheduleRow, String> slot4Column = (TableColumn<ScheduleRow, String>) tableView.getColumns().get(4);
        slot4Column.setCellValueFactory(cellData -> cellData.getValue().slot4Property());
        slot4Column.setCellFactory(TextFieldTableCell.forTableColumn());
        slot4Column.setOnEditCommit(event -> event.getRowValue().setSlot4(event.getNewValue()));

        TableColumn<ScheduleRow, String> slot5Column = (TableColumn<ScheduleRow, String>) tableView.getColumns().get(5);
        slot5Column.setCellValueFactory(cellData -> cellData.getValue().slot5Property());
        slot5Column.setCellFactory(TextFieldTableCell.forTableColumn());
        slot5Column.setOnEditCommit(event -> event.getRowValue().setSlot5(event.getNewValue()));

        TableColumn<ScheduleRow, String> slot6Column = (TableColumn<ScheduleRow, String>) tableView.getColumns().get(6);
        slot6Column.setCellValueFactory(cellData -> cellData.getValue().slot6Property());
        slot6Column.setCellFactory(TextFieldTableCell.forTableColumn());
        slot6Column.setOnEditCommit(event -> event.getRowValue().setSlot6(event.getNewValue()));

        ObservableList<ScheduleRow> data = FXCollections.observableArrayList(
                new ScheduleRow("SAMEDI", "", "", "", "", "", ""),
                new ScheduleRow("DIMANCHE", "", "", "", "", "", ""),
                new ScheduleRow("LUNDI", "", "", "", "", "", ""),
                new ScheduleRow("MARDI", "", "", "", "", "", ""),
                new ScheduleRow("MERCREDI", "", "", "", "", "", ""),
                new ScheduleRow("JEUDI", "", "", "", "", "", "")
        );

        tableView.setItems(data);
    }

    // Navigation methods
    @FXML
    private void AfficherAcceuil(javafx.event.ActionEvent event) throws IOException {
        Main.switchScene("PageAcceuil.fxml");
    }

    @FXML
    private void AfficherEmploiDuTemps(javafx.event.ActionEvent event) throws IOException {
        Main.switchScene("EmploiDuTemps.fxml");
    }

    @FXML
    private void AfficherStock(javafx.event.ActionEvent event) throws IOException {
        Main.switchScene("Stock.fxml");
    }

    @FXML
    private void AfficherLogging(javafx.event.ActionEvent event) throws IOException {
        Main.switchScene("Logging.fxml");
    }

    @FXML
    private void Deconnecter(ActionEvent event) throws IOException {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation Decconection");
        confirmation.setHeaderText("Êtes-vous sûr de vouloir vous déconnecter ?");
        confirmation.setContentText(" Si vous voulez vous déconnecter, cliquez sur le bouton OK.");
        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                AnchorPane root = (AnchorPane) ((Node) event.getSource()).getScene().getRoot();

                Label notification = new Label(" Déconnexion réussie !");
                notification.setStyle(
                        "-fx-background-color: #d4edda;" +
                                "-fx-text-fill: #155724;" +
                                "-fx-padding: 20 40 20 40;" +
                                "-fx-font-size: 16px;" +
                                "-fx-background-radius: 8;" +
                                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 10, 0, 0, 2);"
                );
                notification.setLayoutX(-9999);
                root.getChildren().add(notification);
                Platform.runLater(() -> {
                    notification.setLayoutX((root.getWidth() - notification.getWidth()) / 2);
                    notification.setLayoutY(30);
                });


                PauseTransition pause = new PauseTransition(Duration.seconds(0.5));
                pause.setOnFinished(e -> {
                    root.getChildren().remove(notification);
                    try {
                        Main.switchScene("Login.fxml");
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                });
                pause.play();
            }

        });
    }

    @FXML
    private void changertheme() {
        Main.setDarkMode(ModeToggle.isSelected());
    }

    public void openfiltrage(ActionEvent event) throws IOException {
        Stage popupWindow = Main.openPopupWindow2("FiltrageSalle.fxml", "filtrer", 550, 350);
        popupWindow.setX(40);
        popupWindow.setY(160);
    }

    // Database Tools
    private Connection connect;
    private PreparedStatement prepare;
    private ResultSet result;

    // Upload button handlers (toutes les méthodes handleUpload...)
    @FXML
    private void handleUploadA21(ActionEvent event) {
        handleUploadButton(A21Button, "A21");
    }

    @FXML
    private void handleUploadA22(ActionEvent event) {
        handleUploadButton(A22Button, "A22");
    }

    @FXML
    private void handleUploadA23(ActionEvent event) {
        handleUploadButton(A23Button, "A23");
    }

    @FXML
    private void handleUploadA24(ActionEvent event) {
        handleUploadButton(A24Button, "A24");
    }

    @FXML
    private void handleUploadA25(ActionEvent event) {
        handleUploadButton(A25Button, "A25");
    }

    @FXML
    private void handleUploadA31(ActionEvent event) {
        handleUploadButton(A31Button, "A31");
    }

    @FXML
    private void handleUploadA32(ActionEvent event) {
        handleUploadButton(A32Button, "A32");
    }

    @FXML
    private void handleUploadA33(ActionEvent event) {
        handleUploadButton(A33Button, "A33");
    }

    @FXML
    private void handleUploadA34(ActionEvent event) {
        handleUploadButton(A34Button, "A34");
    }

    @FXML
    private void handleUploadAUNIX(ActionEvent event) {
        handleUploadButton(UNIXButton, "UNIX");
    }

    @FXML
    private void handleUploadA41(ActionEvent event) {
        handleUploadButton(A41Button, "A41");
    }

    @FXML
    private void handleUploadA42(ActionEvent event) {
        handleUploadButton(A42Button, "A42");
    }

    @FXML
    private void handleUploadA43(ActionEvent event) {
        handleUploadButton(A43Button, "A43");
    }

    // Méthodes utilitaires (getExcelFilePath, handleUploadButton, etc.)
    public static String getExcelFilePath(Stage stage) {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Sélectionner un fichier Excel");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Fichiers Excel", "*.xlsx")
            );

            if (stage == null) {
                System.out.println("Erreur: Stage non valide.");
                return null;
            }

            File selectedFile = fileChooser.showOpenDialog(stage);
            if (selectedFile != null) {
                return selectedFile.getAbsolutePath();
            } else {
                return null;
            }
        } catch (Exception e) {
            System.out.println("Erreur lors de l'ouverture du FileChooser : " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private void handleUploadButton(Button button, String Salle) {
        Stage stage = (Stage) button.getScene().getWindow();
        if (stage == null) {
            new Alert(Alert.AlertType.ERROR, "Erreur: Stage non valide.").showAndWait();
            return;
        }

        String filePath = getExcelFilePath(stage);
        if (filePath != null) {
            System.out.println("Fichier sélectionné: " + filePath);
            insertDataIntoDB(filePath, Salle);

            switch (Salle) {
                case "A21" -> autoRefreshTable("A21", timeTableView21);
                case "A22" -> autoRefreshTable("A22", timeTableView22);
                case "A23" -> autoRefreshTable("A23", timeTableView23);
                case "A24" -> autoRefreshTable("A24", timeTableView24);
                case "A25" -> autoRefreshTable("A25", timeTableView25);
                case "A31" -> autoRefreshTable("A31", timeTableView31);
                case "A32" -> autoRefreshTable("A32", timeTableView32);
                case "A33" -> autoRefreshTable("A33", timeTableView33);
                case "A34" -> autoRefreshTable("A34", timeTableView34);
                case "UNIX" -> autoRefreshTable("UNIX", timeTableViewUNIX);
                case "A41" -> autoRefreshTable("A41", timeTableView41);
                case "A42" -> autoRefreshTable("A42", timeTableView42);
                case "A43" -> autoRefreshTable("A43", timeTableView43);
            }
        } else {
            new Alert(Alert.AlertType.ERROR, "Aucun fichier sélectionné").showAndWait();
        }
    }

    private String getCellValue(org.apache.poi.ss.usermodel.Cell cell) {
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf(cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getCellFormula();
            default -> "";
        };
    }

    public void insertDataIntoDB(String path, String salle) {
        try (FileInputStream fis = new FileInputStream(path);
             Workbook workbook = new XSSFWorkbook(fis)) {

            connect = Database.connectDB();

            if (connect == null) {
                new Alert(Alert.AlertType.ERROR, "Impossible de se connecter à la base de données").showAndWait();
                return;
            }

            Sheet sheet = workbook.getSheetAt(0);
            Row horairesRow = sheet.getRow(0);
            if (horairesRow == null) {
                new Alert(Alert.AlertType.ERROR, "La première ligne du fichier Excel (horaires) est vide.").showAndWait();
                return;
            }

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                org.apache.poi.ss.usermodel.Cell jourCell = row.getCell(0);
                String jour = (jourCell != null) ? getCellValue(jourCell) : "Inconnu";

                for (int j = 1; j < row.getLastCellNum(); j++) {
                    org.apache.poi.ss.usermodel.Cell heureCell = horairesRow.getCell(j);
                    String heure = (heureCell != null) ? getCellValue(heureCell) : "Heure inconnue";

                    org.apache.poi.ss.usermodel.Cell coursCell = row.getCell(j);
                    String cours = (coursCell != null) ? getCellValue(coursCell) : "";

                    if (!cours.isBlank()) {
                        String[] parts = cours.split(" - ");
                        String courName = "", profName = "", groupeName = "";

                        if (parts.length == 3) {
                            courName = parts[0].trim();
                            profName = parts[1].trim();
                            groupeName = parts[2].trim();
                        } else if (parts.length == 2) {
                            courName = parts[0].trim();
                            profName = "";
                            groupeName = parts[1].trim();
                        }

                        String heureDebut = heure;
                        String heureFin = "17:00:00";
                        if (j + 1 < row.getLastCellNum()) {
                            org.apache.poi.ss.usermodel.Cell nextHeureCell = horairesRow.getCell(j + 1);
                            if (nextHeureCell != null) {
                                heureFin = getCellValue(nextHeureCell);
                            }
                        }

                        String checkQuery = "SELECT * FROM emploi_du_temps WHERE jour=? AND Heure_Debut=? AND Heure_Fin=? AND Nom_Salle=?";
                        try (PreparedStatement checkStmt = connect.prepareStatement(checkQuery)) {
                            checkStmt.setString(1, jour);
                            checkStmt.setString(2, heureDebut);
                            checkStmt.setString(3, heureFin);
                            checkStmt.setString(4, salle);

                            try (ResultSet rs = checkStmt.executeQuery()) {
                                if (rs.next()) {
                                    boolean needsUpdate =
                                            !rs.getString("Nom_Enseignant").equals(profName) ||
                                                    !rs.getString("Module").equals(courName) ||
                                                    !rs.getString("Groupe").equals(groupeName);

                                    if (needsUpdate) {
                                        String updateSQL = "UPDATE emploi_du_temps SET Nom_Enseignant=?, Module=?, Groupe=? WHERE jour=? AND Heure_Debut=? AND Heure_Fin=? AND Nom_Salle=?";
                                        try (PreparedStatement updateStmt = connect.prepareStatement(updateSQL)) {
                                            updateStmt.setString(1, profName);
                                            updateStmt.setString(2, courName);
                                            updateStmt.setString(3, groupeName);
                                            updateStmt.setString(4, jour);
                                            updateStmt.setString(5, heureDebut);
                                            updateStmt.setString(6, heureFin);
                                            updateStmt.setString(7, salle);
                                            updateStmt.executeUpdate();
                                        }
                                    }
                                } else {
                                    String insertSQL = "INSERT INTO emploi_du_temps (jour, Heure_Debut, Heure_Fin, Nom_Enseignant, Module, Groupe, Nom_Salle) VALUES (?, ?, ?, ?, ?, ?, ?)";
                                    try (PreparedStatement insertStmt = connect.prepareStatement(insertSQL)) {
                                        insertStmt.setString(1, jour);
                                        insertStmt.setString(2, heureDebut);
                                        insertStmt.setString(3, heureFin);
                                        insertStmt.setString(4, profName);
                                        insertStmt.setString(5, courName);
                                        insertStmt.setString(6, groupeName);
                                        insertStmt.setString(7, salle);
                                        insertStmt.executeUpdate();
                                    }
                                }
                            }
                        }
                    }
                }
            }

            new Alert(Alert.AlertType.INFORMATION, "Emploi du temps importé dans la BDD avec succès !").showAndWait();

        } catch (IOException | SQLException e) {
            new Alert(Alert.AlertType.ERROR, "Erreur lors de l'importation : " + e.getMessage()).showAndWait();
            e.printStackTrace();
        } finally {
            try {
                if (result != null) result.close();
                if (prepare != null) prepare.close();
                if (connect != null) connect.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void loadScheduleData(String salle, TableView<ScheduleRow> tableView) {
        ObservableList<ScheduleRow> scheduleRows = FXCollections.observableArrayList();

        try (Connection connect = Database.connectDB()) {
            if (connect == null) {
                new Alert(Alert.AlertType.ERROR, "Erreur de connexion à la base de données").showAndWait();
                return;
            }

            String[] jours = {"SAMEDI", "DIMANCHE", "LUNDI", "MARDI", "MERCREDI", "JEUDI"};

            for (String jour : jours) {
                String slot1 = "", slot2 = "", slot3 = "", slot4 = "", slot5 = "", slot6 = "";

                String query = "SELECT * FROM emploi_du_temps WHERE jour = ? AND Nom_Salle = ?";
                try (PreparedStatement statement = connect.prepareStatement(query)) {
                    statement.setString(1, jour);
                    statement.setString(2, salle);

                    try (ResultSet result = statement.executeQuery()) {
                        while (result.next()) {
                            String heureDebut = result.getString("Heure_Debut");
                            String heureFin = result.getString("Heure_Fin");
                            String module = result.getString("Module");
                            String enseignant = result.getString("Nom_Enseignant");
                            String groupe = result.getString("Groupe");

                            String content = module + "\n" + enseignant + "\n" + groupe;
                            String timeSlot = heureDebut + "-" + heureFin;

                            switch (timeSlot) {
                                case "08:00:00-09:30:00" -> slot1 = content;
                                case "09:30:00-11:00:00" -> slot2 = content;
                                case "11:00:00-12:30:00" -> slot3 = content;
                                case "12:30:00-14:00:00" -> slot4 = content;
                                case "14:00:00-15:30:00" -> slot5 = content;
                                case "15:30:00-17:00:00" -> slot6 = content;
                            }
                        }
                    }
                }

                ScheduleRow row = new ScheduleRow(jour, slot1, slot2, slot3, slot4, slot5, slot6);
                scheduleRows.add(row);
            }

            tableView.setItems(scheduleRows);

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
    public void ManButton(ActionEvent actionEvent) {
        try {
            // Get current directory (whether JAR or IDE)
            String baseDir = new File(getClass().getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI()).getParent();

            File pdfFile = new File(baseDir + File.separator + "pdfs" + File.separator + "Guidev3.pdf");

            if (pdfFile.exists()) {
                Desktop.getDesktop().open(pdfFile);
            } else {
                System.out.println("PDF not found at: " + pdfFile.getAbsolutePath());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
