package com.gaizkaFrost;

import com.gaizkaFrost.AES.CryptoException;
import com.gaizkaFrost.AES.UseCases;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Objects;

public class MainController {

    // === FXML ===
    @FXML private TextField claveField;
    @FXML private TextArea textoEntradaArea;
    @FXML private TextArea textoSalidaArea;
    @FXML private ComboBox<String> algoritmoCombo;
    @FXML private Label statusLabel;

    // Puedes cambiar el AAD si quieres versionar/etiquetar
    private static final String AAD = "app=Encriptador;v=1";

    @FXML
    private void initialize() {
        // Valor por defecto del combo si viene vacío
        if (algoritmoCombo != null && (algoritmoCombo.getValue() == null || algoritmoCombo.getValue().isBlank())) {
            algoritmoCombo.setValue("AES");
        }
        actualizarStatus("Listo");
    }

    // === Handlers de botones (coinciden con tu FXML) ===

    @FXML
    private void handleCifrar() {
        String algoritmo = safeGet(algoritmoCombo);
        String clave = nonNull(claveField.getText());
        String texto = nonNull(textoEntradaArea.getText());

        try {
            String resultado;

            if ("Vigenère".equals(algoritmo)) {
                // Si ya tienes un APIClient propio, cambia esta línea por tu llamada:
                resultado = APIClient.cifrarVigenere(texto, clave);
                // resultado = cifrarVigenereLocal(texto, clave);
                actualizarStatus("Texto cifrado con Vigenère");
            } else {
                // AES local (Base64)
                resultado = UseCases.encryptToBase64(
                        texto.getBytes(StandardCharsets.UTF_8),
                        clave.toCharArray(),
                        AAD
                );
                actualizarStatus("Texto cifrado con AES");
            }

            textoSalidaArea.setText(resultado);

        } catch (Exception e) {
            mostrarError("Error al cifrar: " + e.getMessage());
        }
    }

    @FXML
    private void handleDescifrar() {
        String algoritmo = safeGet(algoritmoCombo);
        String clave = nonNull(claveField.getText());
        String textoCifrado = nonNull(textoEntradaArea.getText());

        try {
            String resultado;

            if ("Vigenère".equals(algoritmo)) {

                resultado = APIClient.descifrarVigenere(textoCifrado, clave);
                actualizarStatus("Texto descifrado con Vigenère");
            } else {
                // AES local
                byte[] plain = UseCases.decryptFromBase64(textoCifrado, clave.toCharArray());
                resultado = new String(plain, StandardCharsets.UTF_8);
                actualizarStatus("Texto descifrado con AES");
            }

            textoSalidaArea.setText(resultado);

        } catch (CryptoException e) {
            // Tag inválido / contraseña errónea / datos corruptos
            mostrarError("Contraseña incorrecta o datos corruptos");
        } catch (Exception e) {
            mostrarError("Error al descifrar: " + e.getMessage());
        }
    }

    @FXML
    private void handleCargarArchivo() {
        try {
            FileChooser fc = new FileChooser();
            fc.setTitle("Abrir archivo de entrada");
            File f = fc.showOpenDialog(getWindow());
            if (f != null) {
                String content = Files.readString(f.toPath(), StandardCharsets.UTF_8);
                textoEntradaArea.setText(content);
                actualizarStatus("Archivo cargado: " + f.getName());
            }
        } catch (Exception e) {
            mostrarError("No se pudo leer el archivo: " + e.getMessage());
        }
    }

    @FXML
    private void handleGuardarResultado() {
        try {
            FileChooser fc = new FileChooser();
            fc.setTitle("Guardar resultado");
            fc.setInitialFileName("resultado.txt");
            File f = fc.showSaveDialog(getWindow());
            if (f != null) {
                Files.writeString(f.toPath(), nonNull(textoSalidaArea.getText()), StandardCharsets.UTF_8);
                actualizarStatus("Resultado guardado: " + f.getName());
            }
        } catch (Exception e) {
            mostrarError("No se pudo guardar: " + e.getMessage());
        }
    }

    @FXML
    private void handleLimpiarEntrada() {
        textoEntradaArea.clear();
        actualizarStatus("Entrada limpiada");
    }

    @FXML
    private void handleCopiar() {
        String out = nonNull(textoSalidaArea.getText());
        ClipboardContent content = new ClipboardContent();
        content.putString(out);
        Clipboard.getSystemClipboard().setContent(content);
        actualizarStatus("Resultado copiado al portapapeles");
    }

    // === Utilidades de UI ===

    private void actualizarStatus(String msg) {
        if (statusLabel != null) statusLabel.setText(Objects.requireNonNullElse(msg, ""));
    }

    private void mostrarError(String msg) {
         Alert a = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        a.showAndWait();
    }

    private Window getWindow() {
        // Intenta obtener una ventana para los diálogos de FileChooser si hace falta
        if (statusLabel != null && statusLabel.getScene() != null) {
            return statusLabel.getScene().getWindow();
        }
        return null;
    }

    private static String nonNull(String s) {
        return s == null ? "" : s;
    }

    private static String safeGet(ComboBox<String> cb) {
        return (cb != null && cb.getValue() != null) ? cb.getValue() : "";
    }


    private void mostrarErrorAlert(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Ha ocurrido un error");
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}


