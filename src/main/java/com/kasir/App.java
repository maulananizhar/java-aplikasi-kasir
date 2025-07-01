package com.kasir;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

import atlantafx.base.theme.Dracula;

/**
 * JavaFX App
 */
public class App extends Application {

    public static Scene rootScene;

    @Override
    public void start(Stage stage) throws IOException {
        App.setUserAgentStylesheet(new Dracula().getUserAgentStylesheet());
        rootScene = new Scene(loadFXML("login"));
        stage.setScene(rootScene);
        stage.setTitle("Aplikasi Kasir");
        stage.getIcons().add(new Image("file:src/main/resources/com/kasir/robot.png"));
        stage.show();
    }

    public static void setRoot(String fxml) throws IOException {
        rootScene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch(args);
    }

}