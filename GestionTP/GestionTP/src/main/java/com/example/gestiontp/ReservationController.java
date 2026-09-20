package com.example.gestiontp;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

public class ReservationController {

    @FXML
    private FlowPane sallesPane;

    @FXML
    private TextField nomEnseignant;

    private List<String> sallesDisponibles;
    private StackPane salleSelectionnee = null;


    private void afficherSalles() {
        sallesPane.getChildren().clear();

        if (sallesDisponibles == null || sallesDisponibles.isEmpty()) {
            Label label = new Label("Aucune salle disponible pour le moment.");
            label.setStyle("-fx-text-fill: red; -fx-font-style: italic;");
            sallesPane.getChildren().add(label);
            return;
        }

        for (String salle : sallesDisponibles) {
            StackPane stack = new StackPane();
            stack.setPrefSize(70, 40);
            stack.getStyleClass().add("stack-salle");

            Label nomSalle = new Label(salle);
            nomSalle.getStyleClass().add("label-salle");
            stack.getChildren().add(nomSalle);

            stack.setOnMouseClicked(event -> {
                // Deselect the previously selected salle if any
                if (salleSelectionnee != null) {
                    salleSelectionnee.getStyleClass().removeAll("stack-salle-selected");
                    salleSelectionnee.getStyleClass().add("stack-salle");
                    salleSelectionnee.setUserData(false);
                }

                // Select the clicked salle
                stack.getStyleClass().removeAll("stack-salle");
                stack.getStyleClass().add("stack-salle-selected");
                stack.setUserData(true);
                salleSelectionnee = stack;
            });

            stack.setUserData(false); // initially not selected
            sallesPane.getChildren().add(stack);
        }
    }
    //Database Tools
    private Connection connect;
    private PreparedStatement prepare;
    private ResultSet result;

    @FXML
    private void handleReservation() {

        if (salleSelectionnee != null && (Boolean) salleSelectionnee.getUserData()) {
            String nomSalle = ((Label) salleSelectionnee.getChildren().get(0)).getText();
            String enseignant = nomEnseignant.getText();

            if (enseignant != null && !enseignant.trim().isEmpty()) {
                try {
                    connect = Database.connectDB();

                    // Valeurs depuis le filtre
                    LocalDate selectedDate = FiltrageSalleController.selectedDate;
                    LocalTime heureDebut = FiltrageSalleController.selectedHeureDebut;
                    LocalTime heureFin = FiltrageSalleController.selectedHeureFin;
                    String jour = FiltrageSalleController.selectedJour;

                    // Vérification de conflit
                    String checkSQL = "SELECT COUNT(*) FROM réservation r " +
                            "JOIN est_réserver er ON r.id_réservation = er.id_réservation " +
                            "WHERE r.date_réservation = ? AND r.Heure_Debut = ? AND r.Heure_Fin = ? AND er.Nom_Salle = ?";
                    prepare = connect.prepareStatement(checkSQL);
                    prepare.setDate(1, Date.valueOf(selectedDate));
                    prepare.setTime(2, Time.valueOf(heureDebut));
                    prepare.setTime(3, Time.valueOf(heureFin));
                    prepare.setString(4, nomSalle);
                    result = prepare.executeQuery();

//                    // Debug conflict query result
//                    if (result.next()) {
//                        int count = result.getInt(1);
//                        if (count > 0) {
//                            showAlert("Conflit", "Une réservation existe déjà pour la salle " + nomSalle + " à ce créneau.");
//                            return;
//                        }
//                    }

                    result.close();
                    prepare.close();

                    // Insertion dans réservation
                    String insertSQL = "INSERT INTO réservation (date_réservation, jour, Heure_Debut, Heure_fin, Nom_Enseignant) VALUES (?, ?, ?, ?, ?)";
                    prepare = connect.prepareStatement(insertSQL, Statement.RETURN_GENERATED_KEYS);
                    prepare.setDate(1, Date.valueOf(selectedDate));
                    prepare.setString(2, jour);
                    prepare.setTime(3, Time.valueOf(heureDebut));
                    prepare.setTime(4, Time.valueOf(heureFin));
                    prepare.setString(5, enseignant);
                    prepare.executeUpdate();

                    ResultSet generatedKeys = prepare.getGeneratedKeys();
                    int idReservation = -1;
                    if (generatedKeys.next()) {
                        idReservation = generatedKeys.getInt(1);
                    }
                    System.out.println("New idReservation = " + idReservation); // Debug ID

                    generatedKeys.close();
                    prepare.close();

                    // Insertion dans est_réserver
                    String insertEstReserverSQL = "INSERT INTO est_réserver (id_réservation, Nom_Salle) VALUES (?, ?)";
                    prepare = connect.prepareStatement(insertEstReserverSQL);
                    prepare.setInt(1, idReservation);
                    prepare.setString(2, nomSalle);
                    prepare.executeUpdate();

                    showAlert("Réservation", "Salle " + nomSalle + " réservée par " + enseignant + ".");

                } catch (SQLException e) {
                    showAlert("Erreur BD", "Erreur lors de la réservation : " + e.getMessage());
                    e.printStackTrace();
                } finally {
                    try { if (result != null) result.close(); } catch (SQLException ignored) {}
                    try { if (prepare != null) prepare.close(); } catch (SQLException ignored) {}
                    try { if (connect != null) connect.close(); } catch (SQLException ignored) {}
                }
            } else {
                showAlert("Erreur", "Veuillez entrer le nom de l'enseignant.");
            }
        } else {
            showAlert("Avertissement", "Veuillez sélectionner une salle pour la réservation.");
        }
    }



    private void showAlert(String titre, String contenu) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titre);
        alert.setContentText(contenu);
        alert.showAndWait();
    }


    @FXML
    private ChoiceBox<String> salleChoiceBox;

    public void setAvailableRooms(ObservableList<String> rooms) {
        this.sallesDisponibles = rooms;
        afficherSalles();
    }

}