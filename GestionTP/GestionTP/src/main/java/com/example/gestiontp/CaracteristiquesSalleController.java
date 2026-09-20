package com.example.gestiontp;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;


public class CaracteristiquesSalleController {

    @FXML
    private  TextField matlab;
    @FXML
    private  TextField javajdk;
    @FXML
    private  TextField devc;
    @FXML
    private  TextField logiciel4;
    @FXML
    private  TextField logiciel5;
    @FXML
    private  TextField logiciel6;
    @FXML
    private  TextField logiciel7;
    @FXML
    private   TextField logiciel8;
    @FXML
    private  TextField logiciel9;
    @FXML
    private  TextField logiciel10;


    @FXML
    private CheckBox Internet;
    @FXML
    private CheckBox matlabbutton;
    @FXML
    private CheckBox javaJdkbutton;
    @FXML
    private CheckBox devcbutton;
    @FXML
    private CheckBox logiciel4button;
    @FXML
    private CheckBox logiciel5button;
    @FXML
    private CheckBox logiciel6button;
    @FXML
    private CheckBox logiciel7button;
    @FXML
    private CheckBox logiciel8button;
    @FXML
    private CheckBox logiciel9button; // Added button
    @FXML
    private CheckBox logiciel10button;// Added button
    @FXML
    private Label TitleA;
    @FXML
    private Label total;
    @FXML
    private Label posts;
    @FXML
    private Label groupe;
    @FXML
    private Label professeur;
    @FXML
    private Label module;
    @FXML
    private Label tables;
    private int post;
    private int Total;


    private final Set<CheckBox> logicielButtons = new HashSet<>();
    private final Color defaultColor = Color.TRANSPARENT; // Or any default color you have
    private final Color clickedColor = Color.LIGHTBLUE; // Or any color you want on click
    private final Set<CheckBox> selectedLogicielButtons = new HashSet<>();
    private List<TextField> logicielTextFields = new ArrayList<>();

    private Map<TextField , CheckBox> Logiciel = new HashMap<>();
    private Map<String , TextField>Setlogiciel = new HashMap<>();
    public CaracteristiquesSalleController() {

    }
    @FXML
    public void initialize() {
        // Add all the logiciel buttons to the set
        logicielButtons.add(matlabbutton);
        logicielButtons.add(javaJdkbutton);
        logicielButtons.add(devcbutton);
        logicielButtons.add(logiciel4button);
        logicielButtons.add(logiciel5button);
        logicielButtons.add(logiciel6button);
        logicielButtons.add(logiciel7button);
        logicielButtons.add(logiciel8button);
        logicielButtons.add(logiciel9button);
        logicielButtons.add(logiciel10button);

        logicielTextFields.add(matlab);
        logicielTextFields.add(devc);
        logicielTextFields.add(javajdk);
        logicielTextFields.add(logiciel4);
        logicielTextFields.add(logiciel5);
        logicielTextFields.add(logiciel6);
        logicielTextFields.add(logiciel7);
        logicielTextFields.add(logiciel8);
        logicielTextFields.add(logiciel9);
        logicielTextFields.add(logiciel10);



        Logiciel.put(matlab, matlabbutton);
        Logiciel.put(devc, devcbutton);
        Logiciel.put(javajdk, javaJdkbutton);
        Logiciel.put(logiciel4, logiciel4button);
        Logiciel.put(logiciel5, logiciel5button);
        Logiciel.put(logiciel6, logiciel6button);
        Logiciel.put(logiciel7, logiciel7button);
        Logiciel.put(logiciel8, logiciel8button);
        Logiciel.put(logiciel9, logiciel9button);
        Logiciel.put(logiciel10, logiciel10button);

        for (TextField tf : logicielTextFields) {
            String text = tf.getText().trim().toUpperCase();
            if (!text.isEmpty()) {
                Setlogiciel.put(text, tf);
            }
        }

        // Initialize the background color of the buttons
        for (CheckBox check : logicielButtons) {

            check.setSelected(true);
        }

    }

    public void loadSalleData(String salleName) {
        // Set the room title
        TitleA.setText(salleName);


        try {
            Connection connection = Database.connectDB();

            // Load room characteristics
            String charSql = "SELECT Capacité, Nombre_poste, Nombre_tables FROM Salle_Tp WHERE Nom_Salle = ?";
            PreparedStatement charStmt = connection.prepareStatement(charSql);
            charStmt.setString(1, salleName);
            ResultSet charRs = charStmt.executeQuery();

            if (charRs.next()) {
                total.setText(String.valueOf(charRs.getInt("Capacité")));
                posts.setText(String.valueOf(charRs.getInt("Nombre_Poste")));
                tables.setText(String.valueOf(charRs.getInt("Nombre_tables")));
            }


            String occSql = "SELECT Module, Nom_Enseignant, Groupe " +
                    "FROM emploi_du_temps " +
                    "WHERE Nom_Salle = ? AND jour = ? AND ? BETWEEN Heure_Debut AND Heure_fin";

            PreparedStatement occStmt = connection.prepareStatement(occSql);
            occStmt.setString(1, salleName);
            occStmt.setString(2, CurrentDayInFrensh.getCurrentDayInFrench());

            LocalTime currentTime = LocalTime.now().truncatedTo(ChronoUnit.SECONDS);
            occStmt.setTime(3, java.sql.Time.valueOf(currentTime));

            ResultSet occRs = occStmt.executeQuery();

            if (occRs.next()) {
                module.setText(occRs.getString("Module"));
                professeur.setText(occRs.getString("Nom_Enseignant"));
                groupe.setText(occRs.getString("Groupe"));
            } else {
                module.setText("--");
                professeur.setText("--");
                groupe.setText("--");
            }
            for (CheckBox check : logicielButtons) {
                check.setSelected(false);
            }

            loadlogiciel();
            String sqlInt = "SELECT internet FROM Salle_Tp WHERE Nom_Salle = ?";
            try (Connection conn = Database.connectDB();
                 PreparedStatement stmt = conn.prepareStatement(sqlInt);) {
                stmt.setString(1, salleName);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    if (rs.getInt("internet") == 1)
                        Internet.setSelected(true);
                    else
                        Internet.setSelected(false);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private void insertIntoInstalles(String logicielName) {



        String insertSQL = "INSERT INTO instalés (id_logiciel, Nom_Salle) " +
                "VALUES ((SELECT id_logiciel FROM logiciel WHERE Nom_Logiciel = ? ), ?)";
        try (Connection connection = Database.connectDB();
             PreparedStatement pstmt = connection.prepareStatement(insertSQL)) {

            pstmt.setString(1, logicielName);
            pstmt.setString(2, PageAcceuilController.NameSalle);

            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("SQL Error: " + e.getMessage());
            e.printStackTrace();
        }


    }


    private String getTextFieldValueFromHBox(HBox hbox) {
        for (Node node : hbox.getChildren()) {
            if (node instanceof TextField) {
                TextField textField = (TextField) node;
                System.out.println(textField.getText());
                return textField.getText().trim();
            }
        }
        return null;
    }

    @FXML
    private void ChoisirlogicielCheckBox(ActionEvent event) {
        CheckBox clickedButton = (CheckBox) event.getSource();


        HBox parentHBox = (HBox) clickedButton.getParent();

        // Find the associated TextField
        String logicielName = null;
        for (Node node : parentHBox.getChildren()) {
            if (node instanceof TextField) {
                TextField associatedTextField = (TextField) node;
                logicielName = associatedTextField.getText();
                break; // We found it, no need to continue
            }
        }

        if (logicielName == null) {
            System.out.println("No associated TextField found!");
            return;
        }

        System.out.println("Logiciel selected: " + logicielName);

        if (selectedLogicielButtons.contains(clickedButton)) {
            // If the button is already selected, deselect it
            selectedLogicielButtons.remove(clickedButton);
            deletelogiciel(logicielName);
            clickedButton.setSelected(false);
        } else {
            // If the button is not selected, select it
            selectedLogicielButtons.add(clickedButton);
            insertIntoInstalles(logicielName);

            clickedButton.setSelected(true);
        }

        System.out.println("Selected logiciels: " +
                selectedLogicielButtons.stream()
                        .map(button -> {
                            HBox hbox = (HBox) button.getParent();
                            for (Node node : hbox.getChildren()) {
                                if (node instanceof TextField) {
                                    return ((TextField) node).getText();
                                }
                            }
                            return button.getText(); // fallback
                        })
                        .toList());
    }


    @FXML
    private void closewindow(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    @FXML
    private void openEquipement(ActionEvent actionEvent) throws IOException {
        Stage popupStage = Main.openPopupWindow("EquipementSalle.fxml", "Caracteristiques", 640, 640);
        popupStage.setX(650);
        popupStage.setY(20);

        EquipementController equipementController = Main.getLoader().getController();
        equipementController.GetLabelC().setText(PageAcceuilController.NameSalle.toUpperCase().trim());
        equipementController.setOnCloseCallback(() -> {
            getposttable(PageAcceuilController.NameSalle);



            this.SetPosts(post);
            this.SetTotal(Total);
        });
        equipementController.loadEquipements(PageAcceuilController.NameSalle);

    }

    @FXML
    private void openReservePar(ActionEvent actionEvent) throws IOException {
        Stage popupStage = Main.openPopupWindow2("ReservePar.fxml", "Caracteristiques", 400, 350);
        popupStage.setX(650);
        popupStage.setY(50);

    }

    @FXML
    private void checkInternet(ActionEvent actionEvent) throws IOException {

        if (Internet.isSelected()) {
            String SQL = "UPDATE Salle_Tp SET Internet = 1 WHERE Nom_Salle = ?";
            try (Connection conn = Database.connectDB();
                 PreparedStatement stmt = conn.prepareStatement(SQL)) {
                stmt.setString(1, PageAcceuilController.NameSalle);
                stmt.executeUpdate();


            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            String SQL1 = "UPDATE Salle_Tp SET Internet = 0 WHERE Nom_Salle = ?";
            try (Connection conn = Database.connectDB();
                 PreparedStatement stmt = conn.prepareStatement(SQL1)) {
                stmt.setString(1, PageAcceuilController.NameSalle);
                stmt.executeUpdate();

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void ChangeLogiciel(ActionEvent actionEvent) throws IOException {
        TextField source = (TextField) actionEvent.getSource();
        String enteredText = source.getText().strip().toUpperCase();

        if (enteredText.isEmpty()) return;

//        boolean duplicate = logicielTextFields.stream()
//                .filter(tf -> tf != source)
//                .anyMatch(tf -> enteredText.equalsIgnoreCase(tf.getText().trim()));

        String checkSql= "SELECT 1 from logiciel  WHERE Nom_Logiciel = ?";

        try(Connection conn = Database.connectDB();
            PreparedStatement stmt = conn.prepareStatement(checkSql);) {
            stmt.setString(1, enteredText);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String checkInsert = "SELECT 1 FROM instalés JOIN logiciel ON instalés.id_logiciel=logiciel.id_logiciel WHERE Nom_Logiciel = ?";
                try (Connection connect = Database.connectDB();
                     PreparedStatement stmt1 = connect.prepareStatement(checkInsert);
                ) {
                    stmt.setString(1, enteredText);
                    ResultSet rs1 = stmt1.executeQuery();
                    if (rs1.next()) {
                        Alert alert = new Alert(Alert.AlertType.WARNING);
                        alert.setTitle("Logiciel Exists");
                        alert.setHeaderText("Logiciel already exists");
                        alert.setContentText("Please choose another logiciel or remove the existing one.");
                        alert.showAndWait();
                        source.setText("");
                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                return;

            }

        } catch (SQLException e) {
            e.printStackTrace();
        }



        // Check if this logiciel has a predefined field
        if (Logiciel.containsKey(enteredText)) {
            TextField predefinedField = Setlogiciel.get(enteredText);
            if (predefinedField != source) {
                predefinedField.setText(enteredText); // Set it in its default place

                return;

            }
        } else {
            source.setText(enteredText);
            loadlogiciel();
        }

        String SQL = "INSERT INTO logiciel (Nom_Logiciel) VALUES (?)";
        try {
            Connection conn = Database.connectDB();
            PreparedStatement stsmt = conn.prepareStatement(SQL);
            stsmt.setString(1, enteredText);
            stsmt.executeUpdate();
            stsmt.close();

            insertIntoInstalles(enteredText);

        } catch (SQLException e) {
            e.printStackTrace();
        }

        loadlogiciel();
    }

    private void deletelogiciel(String logicielName) {
        String SQL = "DELETE FROM instalés " +
                "WHERE id_logiciel = (SELECT id_logiciel FROM logiciel WHERE Nom_Logiciel = ?) " +
                "AND Nom_Salle = ?";

        try (Connection connect = Database.connectDB();
             PreparedStatement deletestat = connect.prepareStatement(SQL)) {

            deletestat.setString(1, logicielName);
            deletestat.setString(2, PageAcceuilController.NameSalle);
            int delete = deletestat.executeUpdate();

            if (delete > 0) {
                System.out.println(logicielName + " deleted successfully from installés.");
            } else {
                System.out.println("No entry found to delete for " + logicielName);
            }


        } catch (SQLException e) {
            e.printStackTrace();
        }


    }

    public void getposttable(String NomSalle) {
        String SQL = "SELECT Nombre_Poste , Capacité  FROM Salle_Tp  WHERE Nom_Salle = ? ";
        try (Connection conn = Database.connectDB();
             PreparedStatement selectstat = conn.prepareStatement(SQL)
        ) {
            selectstat.setString(1, PageAcceuilController.NameSalle);
            ResultSet rs = selectstat.executeQuery();
            if (rs.next()) {
                post = rs.getInt("Nombre_Poste");
                Total = rs.getInt("Capacité");


            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public void loadlogiciel() {
        String logiSql = "SELECT logiciel.Nom_Logiciel " +
                "FROM instalés " +
                "JOIN logiciel ON instalés.id_logiciel = logiciel.id_logiciel " +
                "WHERE instalés.Nom_Salle = ?";
        try (Connection conn = Database.connectDB();
             PreparedStatement logiStmt = conn.prepareStatement(logiSql)) {

            logiStmt.setString(1, PageAcceuilController.NameSalle);
            try (ResultSet logiRs = logiStmt.executeQuery()) {

                List<String> salleLogiciels = new ArrayList<>();
                while (logiRs.next()) {
                    String logic = logiRs.getString("Nom_Logiciel").trim();
                    if (!salleLogiciels.contains(logic)) {
                        salleLogiciels.add(logic);
                    }
                }

                List<String> defaultLogiciels = logicielTextFields.stream()
                        .map(tf -> tf.getText().trim().toUpperCase())
                        .filter(s -> !s.isEmpty())
                        .distinct()
                        .collect(Collectors.toList());

                // Clear all text fields before setting new ones
                for (TextField tf : logicielTextFields) {
                    tf.setText("");
                }

                // Fill text fields with salle logiciels first
                List<TextField> fields = new ArrayList<>(logicielTextFields);
                int i = 0;
                for (; i < salleLogiciels.size() && i < fields.size(); i++) {
                    TextField tf = fields.get(i);
                    CheckBox btn = Logiciel.get(tf);

                    tf.setText(salleLogiciels.get(i));
                    btn.setSelected(true);
                    selectedLogicielButtons.add(btn);
                }

                // Fill remaining fields with default logiciels (excluding duplicates)
                for (String defLogiciel : defaultLogiciels) {
                    if (i >= fields.size()) break;
                    if (!salleLogiciels.contains(defLogiciel)) {
                        TextField tf = fields.get(i);
                        tf.setText(defLogiciel);
                        i++;
                    }
                }

            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }




    public void SetPosts(int posts)
    {
        if(this.posts !=null) {
            this.posts.setText(String.valueOf(posts));
        }

    }
    public void SetTotal(int total)
    {
        if(this.total !=null) {
            this.total.setText(String.valueOf(total));
        }
    }
    public int GetTable() {
        if (this.tables != null) {
            return Integer.parseInt(tables.getText());
        }
        return 0;
    }
    public void setMatlab(TextField matlab) {
        this.matlab = matlab;
    }

    public void setJavajdk(TextField javajdk) {
        this.javajdk = javajdk;
    }

    public void setDevc(TextField devc) {
        this.devc = devc;
    }

    public void setLogiciel4(TextField logiciel4) {
        this.logiciel4 = logiciel4;
    }

    public void setLogiciel5(TextField logiciel5) {
        this.logiciel5 = logiciel5;
    }

    public void setLogiciel6(TextField logiciel6) {
        this.logiciel6 = logiciel6;
    }

    public void setLogiciel7(TextField logiciel7) {
        this.logiciel7 = logiciel7;
    }

    public void setLogiciel8(TextField logiciel8) {
        this.logiciel8 = logiciel8;
    }

    public void setLogiciel9(TextField logiciel9) {
        this.logiciel9 = logiciel9;
    }

    public void setLogiciel10(TextField logiciel10) {
        this.logiciel10 = logiciel10;
    }

}
