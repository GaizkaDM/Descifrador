package com.gaizkaFrost;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * <h2>Clase Main</h2>
 * Clase principal de la aplicación JavaFX encargada de iniciar la interfaz
 * gráfica y lanzar en segundo plano el backend en Python necesario para el funcionamiento
 * del programa.
 *
 * <p>Esta clase inicia un proceso externo que ejecuta una API desarrollada en Python,
 * y posteriormente carga la vista inicial definida en <i>MainView.fxml</i>.</p>
 *
 * @author Gaizka
 * @author Diego
 * @version 1.0
 * @since 2025
 */
public class Main extends Application
{
    /**
     * <h3>Método start</h3>
     * Punto de entrada de la aplicación JavaFX.
     * Inicializa el proceso Python, captura su salida en un hilo independiente y
     * finalmente carga la ventana principal de la aplicación.
     *
     * @param stage Ventana principal de la interfaz gráfica.
     * @throws Exception Puede lanzar excepciones relacionadas con la carga del FXML
     *                   o con la inicialización de recursos de JavaFX.
     */
    @Override
    public void start(Stage stage) throws Exception {
        try {
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/imagenes/icono.png")));
            String env = System.getProperty("env", "prod");
            String rutaApi = "prod".equals(env)
                    ? "C:\\Users\\GaizkaClase\\IdeaProjects\\Descifrador\\Python_backend"
                    : "Python_backend";

            ProcessBuilder pb = new ProcessBuilder("python", "app.py");
            pb.directory(new File(rutaApi));
            pb.redirectErrorStream(true); // junta error y salida

            Process process = pb.start();
            /**
             * <h4>Hilo de lectura de la API Python</h4>
             * Este hilo independiente evita bloquear el hilo principal de JavaFX
             * mientras se recibe la salida del backend.
             */
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

            // Esperar 3 segundos para que la API arranque (mejor hacer check HTTP si puedes)
            Thread.sleep(3000);

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/MainView.fxml"));
        Scene scene = new Scene(loader.load(), 600, 500);
        stage.setScene(scene);
        stage.setMinWidth(790);
        stage.setMinHeight(500);
        stage.setTitle("Descifrador");
        stage.show();
    }


    /**
     * <h3>Método main</h3>
     * Método principal del programa.
     * Lanza la aplicación JavaFX mediante el método <code>launch()</code>.
     *
     * @param args Argumentos de ejecución recibidos por consola.
     */
    public static void main( String[] args ) {
        launch();
    }

}