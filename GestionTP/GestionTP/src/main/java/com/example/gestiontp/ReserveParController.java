package com.example.gestiontp;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.beans.binding.Bindings;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ReserveParController {

    @FXML
    private TableView<Reservation> tablereserv;

    @FXML
    private TableColumn<Reservation, String> date;

    @FXML
    private TableColumn<Reservation, String> heure;

    @FXML
    private TableColumn<Reservation, String> enseignant;


    @FXML
    private Button supprimerLignereserv;

    private final ObservableList<Reservation> data = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        date.setCellValueFactory(new PropertyValueFactory<>("date"));
        heure.setCellValueFactory(new PropertyValueFactory<>("heure"));
        enseignant.setCellValueFactory(new PropertyValueFactory<>("enseignant"));

        tablereserv.setEditable(true);
        date.setCellFactory(TextFieldTableCell.forTableColumn());
        heure.setCellFactory(TextFieldTableCell.forTableColumn());
        enseignant.setCellFactory(TextFieldTableCell.forTableColumn());

        tablereserv.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        if (supprimerLignereserv != null) {
            supprimerLignereserv.disableProperty().bind(
                    Bindings.isEmpty(tablereserv.getSelectionModel().getSelectedItems())
            );
        }

        date.setOnEditCommit(event -> event.getRowValue().setDate(event.getNewValue()));
        heure.setOnEditCommit(event -> event.getRowValue().setHeure(event.getNewValue()));
        enseignant.setOnEditCommit(event -> event.getRowValue().setEnseignant(event.getNewValue()));

        tablereserv.setItems(data);
        adjustTableHeight();

        tablereserv.setOnMouseClicked(event -> {
            if (tablereserv.getSelectionModel().getSelectedItem() != null) {
                System.out.println("Selected: " + tablereserv.getSelectionModel().getSelectedItem().getDate());
            }
        });
        loadReservationsForSalle(PageAcceuilController.NameSalle);

    }

    public void loadReservationsForSalle(String nomSalle) {
        data.clear();

        String selectSQL = """
        SELECT r.id_réservation,
               r.date_réservation,
               r.Heure_Debut AS heure,
               r.Nom_Enseignant
        FROM réservation r
        JOIN est_réserver er ON r.id_réservation = er.id_réservation
        WHERE er.Nom_Salle = ?
    """;

        String autoDeleteSQL = """
        DELETE FROM réservation
        WHERE date_réservation = CURRENT_DATE AND Heure_fin <= CURRENT_TIME
    """;

        try (Connection conn = Database.connectDB()) {

            // 1. Supprimer les réservations terminées automatiquement
            try (PreparedStatement autoDeleteStmt = conn.prepareStatement(autoDeleteSQL)) {
                autoDeleteStmt.executeUpdate();
            }

            // 2. Charger les réservations à jour
            try (PreparedStatement ps = conn.prepareStatement(selectSQL)) {
                ps.setString(1, nomSalle);

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int id = rs.getInt("id_réservation");
                        String date = rs.getString("date_réservation");
                        String heure = rs.getString("heure");
                        String enseignant = rs.getString("Nom_Enseignant");

                        data.add(new Reservation(id, date, heure, enseignant));
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        adjustTableHeight();
    }

    @FXML
    public void supprimerLigne() {
        Reservation selectedItem = tablereserv.getSelectionModel().getSelectedItem();

        if (selectedItem != null) {
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setTitle("Confirmation Suppression");
            confirmation.setHeaderText("Effacer la réservation sélectionnée ?");
            confirmation.setContentText("Êtes-vous sûr de vouloir supprimer cette réservation ?");

            confirmation.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    try (Connection conn = Database.connectDB()) {
                        String sql = "DELETE FROM réservation WHERE id_réservation = ?";
                        try (PreparedStatement ps = conn.prepareStatement(sql)) {
                            ps.setInt(1, selectedItem.getIdReservation());
                            ps.executeUpdate();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                        errorAlert.setTitle("Erreur de suppression");
                        errorAlert.setHeaderText(null);
                        errorAlert.setContentText("Une erreur est survenue lors de la suppression dans la base de données.");
                        errorAlert.showAndWait();
                        return;
                    }

                    // Suppression dans la TableView
                    data.remove(selectedItem);
                    adjustTableHeight();
                }
            });
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Suppression impossible");
            alert.setHeaderText(null);
            alert.setContentText("Veuillez sélectionner une ligne à supprimer.");
            alert.showAndWait();
        }
    }





    private void adjustTableHeight() {
        double rowHeight = 30;
        double headerHeight = 35;
        double totalHeight = headerHeight + (data.size() * rowHeight);
        tablereserv.setPrefHeight(totalHeight);
    }

    public static class Reservation {
        private String date;
        private String heure;
        private String enseignant;
        private int idReservation;

        public Reservation(int idReservation,String date, String heure, String enseignant) {
            this.date = date;
            this.heure = heure;
            this.enseignant = enseignant;
            this.idReservation = idReservation;
        }
        public int getIdReservation() {
            return idReservation;
        }

        public void setIdReservation(int idReservation) {
            this.idReservation = idReservation;
        }


        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public String getHeure() {
            return heure;
        }

        public void setHeure(String heure) {
            this.heure = heure;
        }

        public String getEnseignant() {
            return enseignant;
        }

        public void setEnseignant(String enseignant) {
            this.enseignant = enseignant;
        }

    }
}
