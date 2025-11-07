package com.gaizkaFrost;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.util.Locale;
import java.util.ResourceBundle;

import static javafx.application.Application.launch;

/**
 * Hello world!
 *
 */
public class Main extends Application
{
    @Override
    public void start(Stage stage) throws Exception {
        //logger.info("Lanzando la aplicación");


        FXMLLoader loader = new FXMLLoader(getClass().getResource("/MainView.fxml"));
        Scene scene = new Scene(loader.load(), 600, 500);
        stage.setScene(scene);
        stage.setMinWidth(543);
        stage.setMinHeight(500);
        stage.setTitle("Adding/Deleting Rows in a TableView");
        stage.show();

        //logger.info("Ventana principal mostrada correctamente");
    }
    public static void main( String[] args ) {
        launch();
    }

}
