package com.example.gestiontp;



import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javafx.util.Duration;
import org.mindrot.jbcrypt.BCrypt;



public class LoginController {
    @FXML
    private TextField username;
    @FXML
    private PasswordField password;
    @FXML
    private Label notificationLabel;
    @FXML
    private Text errorMessage;

    //Database Tools
    private Connection connect;
    private PreparedStatement prepare;
    private ResultSet result;

    @FXML
    private void handleLogin() throws IOException {
        String usernameText = username.getText();
        String passwordText = password.getText();

        if (usernameText.isEmpty() || passwordText.isEmpty()) {
            errorMessage.setText("Veuillez remplir tous les champs !");
        }

        else {
            String sql="SELECT * FROM utilisateur WHERE Nom_utilisateur = ? ";
            connect=Database.connectDB();

            try{
                prepare=connect.prepareStatement(sql);
                prepare.setString(1,username.getText());

                result= prepare.executeQuery();

                if(result.next())
                {
                    String storedHashedPassword  = result.getString("Mot_de_passe");
                    // Compare the stored hashed password with the entered password
                    if (BCrypt.checkpw(passwordText, storedHashedPassword)) {
                        System.out.println("Login successful");

                        if (BCrypt.checkpw(passwordText, storedHashedPassword)) {
                            showNotification("✅ Connexion réussie !");

                            PauseTransition pause = new PauseTransition(Duration.seconds(0.5));
                            pause.setOnFinished(e -> {
                                try {
                                    Main.switchScene("PageAcceuil.fxml");

                                    PageAcceuilController Controller =Main.getLoader1().getController();
                                    Controller.showNotificationbien("Bienvenue " + usernameText + " !");


                                } catch (IOException ex) {
                                    ex.printStackTrace();
                                }

                            });
                            pause.play();


                        }

                    } else {
                        System.out.println("Mot de passe incorrect !");
                        errorMessage.setText("Mot de passe incorrect !");
                        password.clear();
                    }

                }
                else
                {
                    System.out.println("user incorrect");
                    errorMessage.setText("Utilisateur introuvable !");

                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
    private void showNotification(String message) {
        notificationLabel.setText(message);
        notificationLabel.setVisible(true);
        notificationLabel.setManaged(true);

        PauseTransition pause = new PauseTransition(Duration.seconds(1));
        pause.setOnFinished(e -> {
            notificationLabel.setVisible(false);
            notificationLabel.setManaged(false);
        });
        pause.play();
    }

    @FXML
    private void openSignUp() throws IOException {
        Main.switchScene("signup.fxml");
    }
}
