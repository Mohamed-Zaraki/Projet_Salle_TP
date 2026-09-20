package com.example.gestiontp;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class Main extends Application {
    private static Stage mainStage;
    private static boolean darkMode = false; // Add a static variable to track dark mode
    private static FXMLLoader loader,loader1;

    public static FXMLLoader getLoader() {
        return loader;
    }
    public static FXMLLoader getLoader1() {
        return loader1;
    }

    @Override
    public void start(Stage primaryStage) throws IOException {

        // Load fonts

        // Load fonts manually from resources
        try {
            String[] fontFiles = {
                    "/Fonts/Kanit-Medium.ttf",
                    "/Fonts/Montserrat-Black.ttf",
                    "/Fonts/Montserrat-Bold.ttf",
                    "/Fonts/Montserrat-Regular.ttf",
                    "/Fonts/Poppins-Light.ttf",
                    "/Fonts/Poppins-Bold.ttf",
                    "/Fonts/Poppins-Medium.ttf",
                    "/Fonts/Poppins-Regular.ttf"
            };

            for (String fontPath : fontFiles) {
                Font font = Font.loadFont(getClass().getResource(fontPath).toExternalForm(), 12);
                if (font != null) {
                    System.out.println("Font loaded: " + font.getName());
                } else {
                    System.out.println("Failed to load font: " + fontPath);
                }
            }
        } catch (Exception e) {
            System.out.println("Error loading fonts: " + e.getMessage());
            e.printStackTrace();
        }


        mainStage = primaryStage;

        switchScene("/com/example/gestiontp/Login.fxml");
        mainStage.setTitle("Système de gestion des salles TP");
        mainStage.getIcons().add(
                new Image(getClass().getResourceAsStream("/imagesApp/Logo.png"))
        );


        mainStage.show();
    }

    public static void switchScene(String fxmlFile) throws IOException {
        if (mainStage == null) {
            System.out.println("Error: mainStage is not initialized!");
            return;
        }

        URL resource = Main.class.getResource(fxmlFile);
        if (resource == null) {
            throw new IOException("FXML file not found: " + fxmlFile);
        }

        loader1 = new FXMLLoader(resource);
        Scene scene = new Scene(loader1.load(), 1000, 1000);

        mainStage.setScene(scene);
        mainStage.setWidth(1920);
        mainStage.setHeight(1080);
        mainStage.setMaximized(true);

        loadCSS(scene);
        applyDarkMode(scene); // Apply dark mode if enabled
    }

    // Basic popup window (without auto-close on focus loss)
    public static Stage openPopupWindow(String fxmlFile, String title, double width, double height) throws IOException {
        return createPopupStage(fxmlFile, title, width, height, false);
    }

    // Popup window that closes when clicking outside or when main closes
    public static Stage openPopupWindow2(String fxmlFile, String title, double width, double height) throws IOException {
        return createPopupStage(fxmlFile, title, width, height, true);
    }

    // Shared logic for creating a popup
    private static Stage createPopupStage(String fxmlFile, String title, double width, double height, boolean autoCloseOnFocusLoss) throws IOException {
        URL fxmlUrl = Main.class.getResource(fxmlFile);
        if (fxmlUrl == null) {
            throw new IOException("FXML not found: " + fxmlFile);
        }

        loader = new FXMLLoader(fxmlUrl);
        Parent root = loader.load();

        Stage popupStage = new Stage();
        Scene scene = new Scene(root, width, height);
        popupStage.setScene(scene);
        popupStage.initStyle(StageStyle.UNDECORATED);
        popupStage.setTitle(title);
        popupStage.initOwner(mainStage);
        popupStage.initModality(Modality.NONE);

        if (autoCloseOnFocusLoss) {
            popupStage.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                if (!isNowFocused) {
                    popupStage.close();
                }
            });

            mainStage.setOnCloseRequest(event -> {
                if (popupStage.isShowing()) {
                    popupStage.close();
                }
            });
        }

        // Optional: Make the popup draggable
        final double[] xOffset = {0};
        final double[] yOffset = {0};
        root.setOnMousePressed(event -> {
            xOffset[0] = event.getSceneX();
            yOffset[0] = event.getSceneY();
        });
        root.setOnMouseDragged(event -> {
            popupStage.setX(event.getScreenX() - xOffset[0]);
            popupStage.setY(event.getScreenY() - yOffset[0]);
        });

        loadCSS(scene);
        applyDarkMode(scene); // Apply dark mode if enabled
        popupStage.show();
        return popupStage;
    }

    // Helper to load CSS
    // Helper to load CSS from classpath (safe for JAR and EXE)
    private static void loadCSS(Scene scene) {
        try {
            URL cssUrl = Main.class.getResource("/CSS/Style.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
                System.out.println("CSS added to scene successfully");
            } else {
                System.out.println("Warning: Style.css not found in classpath!");
            }
        } catch (Exception e) {
            System.out.println("Error loading CSS: " + e.getMessage());
            e.printStackTrace();
        }
    }


    // Helper to resolve FXML file path
    private static URL resolveFXMLResource(String fxmlPath) throws IOException {
        URL resource = Main.class.getResource(fxmlPath);
        if (resource == null) {
            System.out.println("FXML not found: " + fxmlPath);
            throw new IOException("Could not find FXML: " + fxmlPath);
        }
        return resource;
    }


    public static void main(String[] args) {
        launch();
    }

    public static void setDarkMode(boolean darkMode) {
        Main.darkMode = darkMode;
        if (mainStage != null && mainStage.getScene() != null) {
            applyDarkMode(mainStage.getScene());
        }
    }

    private static void applyDarkMode(Scene scene) {
        if (darkMode) {
            scene.getRoot().getStyleClass().add("Dark-mode");
        } else {
            scene.getRoot().getStyleClass().remove("Dark-mode");
        }
    }



    public static boolean isDarkMode() {
        return darkMode;
    }
}