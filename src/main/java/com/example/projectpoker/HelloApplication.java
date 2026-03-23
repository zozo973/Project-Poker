package com.example.projectpoker;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 320, 240);

        // TODO implement stylesheet for welcome to app screen
        // String stylesheet = HelloApplication.class.getResource("welcome-stylesheet.css").toExternalForm();
        // scene.getStylesheets().add(stylesheet);
        //

        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.show();
    }
}
