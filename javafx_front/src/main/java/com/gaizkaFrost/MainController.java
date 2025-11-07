package com.gaizkaFrost;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;

public class MainController {

    @FXML
    private TextField claveField;

    @FXML
    private ComboBox<String> algoritmoCombo;

    @FXML
    private TextArea textoEntradaArea;

    @FXML
    private TextArea textoSalidaArea;

    @FXML
    private Label statusLabel;

    @FXML
    private Button cifrarBtn;

    @FXML
    private Button descifrarBtn;

    @FXML
    public void initialize() {
        // Seleccionar Vigenère por defecto
        algoritmoCombo.getSelectionModel().select("Vigenère");
        actualizarStatus("Listo para cifrar/descifrar");
    }

    @FXML
    private void handleCifrar() {
        String texto = textoEntradaArea.getText();
        String clave = claveField.getText();
        String algoritmo = algoritmoCombo.getValue();

        if (texto.isEmpty()) {
            mostrarError("Por favor, introduce un texto para cifrar");
            return;
        }

        if (clave.isEmpty()) {
            mostrarError("Por favor, introduce una clave");
            return;
        }

        try {
            String resultado;

            if ("Vigenère".equals(algoritmo)) {
                // Llamar a la API Python
                resultado = APIClient.cifrarVigenere(texto, clave);
                actualizarStatus("Texto cifrado con Vigenère");
            } else {
                // Usar AES local
                resultado = AESCipher.cifrar(texto, clave);
                actualizarStatus("Texto cifrado con AES");
            }

            textoSalidaArea.setText(resultado);

        } catch (Exception e) {
            mostrarError("Error al cifrar: " + e.getMessage());
        }
    }

    @FXML
    private void handleDescifrar() {
        String textoCifrado = textoEntradaArea.getText();
        String clave = claveField.getText();
        String algoritmo = algoritmoCombo.getValue();

        if (textoCifrado.isEmpty()) {
            mostrarError("Por favor, introduce un texto para descifrar");
            return;
        }

        if (clave.isEmpty()) {
            mostrarError("Por favor, introduce una clave");
            return;
        }

        try {
            String resultado;

            if ("Vigenère".equals(algoritmo)) {
                // Llamar a la API Python
                resultado = APIClient.descifrarVigenere(textoCifrado, clave);
                actualizarStatus("Texto descifrado con Vigenère");
            } else {
                // Usar AES local
                resultado = AESCipher.descifrar(textoCifrado, clave);
                actualizarStatus("Texto descifrado con AES");
            }

            textoSalidaArea.setText(resultado);

        } catch (Exception e) {
            mostrarError("Error al descifrar: " + e.getMessage());
        }
    }

    @FXML
    private void handleCargarArchivo() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar archivo de texto");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Archivos de texto", "*.txt")
        );

        File file = fileChooser.showOpenDialog(textoEntradaArea.getScene().getWindow());

        if (file != null) {
            try {
                String contenido = Files.readString(file.toPath());
                textoEntradaArea.setText(contenido);
                actualizarStatus("Archivo cargado: " + file.getName());
            } catch (Exception e) {
                mostrarError("Error al cargar el archivo: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleGuardarResultado() {
        String resultado = textoSalidaArea.getText();

        if (resultado.isEmpty()) {
            mostrarError("No hay resultado para guardar");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar resultado");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Archivos de texto", "*.txt")
        );
        fileChooser.setInitialFileName("resultado.txt");

        File file = fileChooser.showSaveDialog(textoSalidaArea.getScene().getWindow());

        if (file != null) {
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(resultado);
                actualizarStatus("Resultado guardado en: " + file.getName());
            } catch (Exception e) {
                mostrarError("Error al guardar el archivo: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleCopiar() {
        String resultado = textoSalidaArea.getText();

        if (resultado.isEmpty()) {
            mostrarError("No hay texto para copiar");
            return;
        }

        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putString(resultado);
        clipboard.setContent(content);

        actualizarStatus("Texto copiado al portapapeles");
    }

    @FXML
    private void handleLimpiarEntrada() {
        textoEntradaArea.clear();
        textoSalidaArea.clear();
        actualizarStatus("Campos limpiados");
    }

    private void actualizarStatus(String mensaje) {
        statusLabel.setText(mensaje);
    }

    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();

        actualizarStatus("Error: " + mensaje);
    }
}

