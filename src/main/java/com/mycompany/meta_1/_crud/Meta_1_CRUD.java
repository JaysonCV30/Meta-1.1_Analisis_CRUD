package com.mycompany.meta_1._crud;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Meta_1_CRUD extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Meta_1_CRUD.class.getResource("vista.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 800, 600); // Tamaño de la ventana
        
        stage.setTitle("Gestión de Agenda - CRUD");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
