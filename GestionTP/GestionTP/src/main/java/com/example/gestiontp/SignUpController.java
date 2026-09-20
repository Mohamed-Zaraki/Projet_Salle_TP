package com.example.gestiontp;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.util.Duration;
import org.mindrot.jbcrypt.BCrypt;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class SignUpController {

    @FXML
    private TextField username;

    @FXML
    private PasswordField password;

    @FXML
    private Text errorMessage;
    @FXML
    private Label notificationLabel;
    //Database Tools
    private Connection connect;
    private PreparedStatement prepare;
    private ResultSet result;


    @FXML
    private void handleSignUp() {
        String usernameText = username.getText();
        String passwordText = password.getText();

        // Vérifications simples
        if (usernameText.isEmpty() || passwordText.isEmpty()) {
            errorMessage.setText("Veuillez remplir tous les champs !");
            return;
        }

        if (usernameText.length() < 4) {
            errorMessage.setText("Le nom d'utilisateur doit contenir au moins 4 caractères !");
            return;
        }

        if (passwordText.length() < 6) {
            errorMessage.setText("Le mot de passe doit contenir au moins 6 caractères !");
            return;
        }

        if (usernameText.equals(passwordText)) {
            errorMessage.setText("Le mot de passe doit être différent du nom d'utilisateur !");
            return;
        }

        String hashedPassword = BCrypt.hashpw(passwordText, BCrypt.gensalt());

        // Base de données
        try (Connection connect = Database.connectDB()) {

            // Vérifier si l'utilisateur existe déjà
            String checkUserQuery = "SELECT 1 FROM utilisateur WHERE Nom_utilisateur = ?";
            try (PreparedStatement checkStmt = connect.prepareStatement(checkUserQuery)) {
                checkStmt.setString(1, usernameText);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next()) {
                        errorMessage.setText("Le nom d'utilisateur \"" + usernameText + "\" existe déjà !");
                        return;
                    }
                }
            }

            // Vérifier le nombre d'utilisateurs
            String countQuery = "SELECT COUNT(*) AS count FROM utilisateur";
            try (PreparedStatement countStmt = connect.prepareStatement(countQuery);
                 ResultSet rs = countStmt.executeQuery()) {

                if (rs.next() && rs.getInt("count") >= 3) {
                    errorMessage.setText("Impossible d'ajouter plus de 3 utilisateurs !");
                    return;
                }
            }

            // Insérer le nouvel utilisateur
            String insertQuery = "INSERT INTO utilisateur (Nom_utilisateur, Mot_de_passe) VALUES (?, ?)";
            try (PreparedStatement insertStmt = connect.prepareStatement(insertQuery)) {
                insertStmt.setString(1, usernameText);
                insertStmt.setString(2, hashedPassword);
                insertStmt.executeUpdate();
            }

            showNotification("✅ Inscription réussie !");

        } catch (Exception e) {
            e.printStackTrace();
            errorMessage.setText("❌ Erreur lors de l'inscription.");
        }
    }




    private void showNotification(String message) {
        notificationLabel.setText(message);
        notificationLabel.setVisible(true);
        notificationLabel.setManaged(true);

        PauseTransition pause = new PauseTransition(Duration.seconds(3));
        pause.setOnFinished(e -> {
            notificationLabel.setVisible(false);
            notificationLabel.setManaged(false);
        });
        pause.play();
    }

    @FXML
    private void handleBack() throws IOException {
        Main.switchScene("Login.fxml");
    }


}
