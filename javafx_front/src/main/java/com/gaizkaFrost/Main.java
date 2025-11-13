package com.gaizkaFrost;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Main extends Application
{
    @Override
    public void start(Stage stage) throws Exception {
        try {
            ProcessBuilder pb = new ProcessBuilder("python", "../Python_backend/app.py");
            pb.redirectErrorStream(true);
            Process process = pb.start();

            // Leer salida de la API en hilo aparte para no bloquear UI
            new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println("API: " + line);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

            // Aquí puedes poner un delay o lógica para esperar a que la API esté lista si es necesario

        } catch (IOException e) {
            e.printStackTrace();
            // Manejo de fallo al arrancar API
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/MainView.fxml"));
        Scene scene = new Scene(loader.load(), 600, 500);
        stage.setScene(scene);
        stage.setMinWidth(790);
        stage.setMinHeight(500);
        stage.setTitle("Descifrador");
        stage.show();
    }

    public static void main( String[] args ) {
        launch();
    }

}
