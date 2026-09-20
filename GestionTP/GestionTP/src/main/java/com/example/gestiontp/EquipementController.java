package com.example.gestiontp;

import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.w3c.dom.Text;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class EquipementController implements Initializable {





    @FXML private TableView<Equipement> EquipementTable;
    @FXML private TableColumn<Equipement, String> codeColumn;
    @FXML private TableColumn<Equipement, String> marqueColumn;
    @FXML private TableColumn<Equipement, String> ramColumn;
    @FXML private TableColumn<Equipement, String> seColumn;
    @FXML private TableColumn<Equipement, String> disqueColumn;
    @FXML private TableColumn<Equipement, String> cpuColumn;


    @FXML
    private Hyperlink minus;

    @FXML
    private   Label labelC;

    private String salleId;
    private   ObservableList<Equipement> equipementList ;
    private Runnable onCloseCallback;
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize table columns with cell value factories

        equipementList = FXCollections.observableArrayList();


        EquipementTable.setEditable(true);
        codeColumn.setCellValueFactory(new PropertyValueFactory<>("code"));
        marqueColumn.setCellValueFactory(new PropertyValueFactory<>("marque"));
        ramColumn.setCellValueFactory(new PropertyValueFactory<>("ram"));
        seColumn.setCellValueFactory(new PropertyValueFactory<>("se"));
        disqueColumn.setCellValueFactory(new PropertyValueFactory<>("disque"));
        cpuColumn.setCellValueFactory(new PropertyValueFactory<>("cpu"));

        // Enable editing with TextFieldTableCell
        codeColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        marqueColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        ramColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        seColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        disqueColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        cpuColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        codeColumn.setOnEditCommit(event -> {
            Equipement equipement = event.getRowValue();
            equipement.setCode(event.getNewValue());
            insertEquipement(equipement);

        });

        marqueColumn.setOnEditCommit(event -> {
            Equipement equipement = event.getRowValue();
            equipement.setMarque(event.getNewValue());
            updateEquipement(equipement);

        });

        cpuColumn.setOnEditCommit(event -> {
            Equipement equipement = event.getRowValue();
            equipement.setCpu(event.getNewValue());


            updateEquipement(equipement);

        });

        ramColumn.setOnEditCommit(event -> {
            Equipement equipement = event.getRowValue();
            equipement.setRam(event.getNewValue());
            updateEquipement(equipement);

        });

        disqueColumn.setOnEditCommit(event -> {
            Equipement equipement = event.getRowValue();
            equipement.setDisque(event.getNewValue());

            updateEquipement(equipement);
        });
        seColumn.setOnEditCommit(event -> {
            Equipement equipement = event.getRowValue();
            equipement.setSe(event.getNewValue());
            updateEquipement(equipement);

        });
        equipementList.addListener((ListChangeListener.Change<? extends Equipement> c) -> {
            updateTableHeight();
        });

        EquipementTable.setItems(equipementList);

        // Set initial table size to be small
        EquipementTable.setPrefHeight(50); // Start with a small height




//        setupEditCommitHandlers();
    }
//    private void setupEditCommitHandlers() {
//        codeColumn.setOnEditCommit(event -> event.getRowValue().setCode(event.getNewValue()));
//        marqueColumn.setOnEditCommit(event -> event.getRowValue().setMarque(event.getNewValue()));
//        ramColumn.setOnEditCommit(event -> event.getRowValue().setRam(event.getNewValue()));
//        seColumn.setOnEditCommit(event -> event.getRowValue().setSe(event.getNewValue()));
//        disqueColumn.setOnEditCommit(event -> event.getRowValue().setDisque(event.getNewValue()));
//        cpuColumn.setOnEditCommit(event -> event.getRowValue().setCpu(event.getNewValue()));
//    }

    private void updateTableHeight() {
        // Calculate new height based on number of rows
        // Each row is approximately 25 pixels high, plus some padding for header
        int rowCount = equipementList.size();
        double headerHeight = 30; // Approximate height of the header
        double rowHeight = 25;    // Approximate height of each row
        double borderHeight = 2;  // Border thickness

        // Calculate new height with a minimum size
        double newHeight = Math.max(50, headerHeight + (rowCount * rowHeight) + borderHeight);

        // Set maximum height to avoid excessive growth
        double maxHeight = 300;
        newHeight = Math.min(newHeight, maxHeight);

        // Update table height
        EquipementTable.setPrefHeight(newHeight);
    }

    public void loadEquipements(String salleId) {
        this.salleId = salleId;
        equipementList.addAll(loadEquipementsBySalle(salleId));

        System.out.println(equipementList.size());

        EquipementTable.setItems(equipementList);
    }

    @FXML
    private void handleAddRow() {

        Equipement newEquipment = new Equipement("", "", "", "", "", "");

        equipementList.add(newEquipment);
        newEquipment.setSalleId(salleId);
        newEquipment.setNew(true);
        EquipementTable.getSelectionModel().select(equipementList.size() - 1);
        EquipementTable.scrollTo(newEquipment);
        EquipementTable.refresh();

    }

    @FXML
    private void handleDeleteRow(ActionEvent event) {
        Equipement selected = EquipementTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            deleteEquipement(selected.getCode());
            equipementList.remove(selected);
        }
        else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Selection");
            alert.setHeaderText("No Row Selected");
            alert.setContentText("Please select a row to delete.");
            alert.showAndWait();

        }
    }




    public ObservableList<Equipement> loadEquipementsBySalle(String salleId) {
        ObservableList<Equipement> equipements = FXCollections.observableArrayList();
        try {
            Connection conn = Database.connectDB();
            String sql = "SELECT * FROM ordinateur WHERE Nom_Salle = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, salleId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Equipement equipement = new Equipement(
                        rs.getString("Code_Pc"),
                        rs.getString("Marque"),
                        rs.getString("Ram"),
                        rs.getString("Type_SE"),
                        rs.getString("Disque_Dur"),
                        rs.getString("Processeur")
                );
                equipement.setSalleId(rs.getString("Nom_Salle"));
                equipements.add(equipement);
            }
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return equipements;
    }

    public  void insertEquipement(Equipement equipement) {



        try {
            Connection conn = Database.connectDB();

            String sql = "INSERT INTO ordinateur (Code_Pc, Marque, Ram, Type_SE, Disque_Dur, Processeur, Nom_Salle) VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, equipement.codeProperty().get());
            stmt.setString(2, equipement.marqueProperty().get());
            stmt.setString(3, equipement.ramProperty().get());
            stmt.setString(4, equipement.seProperty().get());
            stmt.setString(5, equipement.disqueProperty().get());
            stmt.setString(6, equipement.cpuProperty().get());
            stmt.setString(7, equipement.salleIdProperty().get());

            stmt.executeUpdate();


            String SQL = "UPDATE Salle_Tp SET Nombre_Poste = Nombre_Poste + 1 WHERE Nom_Salle = ?";
            PreparedStatement Update = conn.prepareStatement(SQL);
            Update.setString(1, equipement.salleIdProperty().get());

            Update.executeUpdate();

            conn.close();


        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public  void deleteEquipement(String code) {
        try {
            Connection conn = Database.connectDB();
            String sql = "DELETE FROM ordinateur WHERE Code_Pc = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, code);
            stmt.executeUpdate();

            String sql1 = "UPDATE Salle_Tp SET Nombre_Poste = Nombre_Poste -1  WHERE Nom_Salle = ?";
            PreparedStatement Update = conn.prepareStatement(sql1);
            Update.setString(1, PageAcceuilController.NameSalle);
            Update.executeUpdate();

            conn.close();


        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public static void updateEquipement(Equipement equipement) {
        try {
            Connection conn = Database.connectDB();
            String sql = "UPDATE ordinateur SET Code_pc= ?, Marque = ?, Ram = ?, Type_SE = ?, Disque_Dur = ?, Processeur = ? WHERE Code_Pc = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, equipement.codeProperty().get());
            stmt.setString(2, equipement.marqueProperty().get());
            stmt.setString(3, equipement.ramProperty().get());
            stmt.setString(4, equipement.seProperty().get());
            stmt.setString(5, equipement.disqueProperty().get());
            stmt.setString(6, equipement.cpuProperty().get());
            stmt.setString(7, equipement.codeProperty().get());
            stmt.executeUpdate();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void closewindow(ActionEvent event) throws IOException {

        if (onCloseCallback != null) {
            onCloseCallback.run();
        }
        Stage stage = (Stage) minus.getScene().getWindow();
        stage.close();
    }

    public ObservableList<Equipement> getEquipementList() {
        return equipementList;
    }
    public void setOnCloseCallback(Runnable callback) {
        this.onCloseCallback = callback;
    }

//

    public Label GetLabelC()
    {
        return labelC;
    }
    public void SetLabelC(Label labelC)
    {
        this.labelC = labelC;
    }



}

