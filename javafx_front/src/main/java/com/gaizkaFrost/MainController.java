package com.gaizkaFrost;

import com.gaizkaFrost.AES.CryptoException;
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

    @FXML
    private void initialize() {
        // Valor por defecto del combo si viene vacío
        if (algoritmoCombo != null && (algoritmoCombo.getValue() == null || algoritmoCombo.getValue().isBlank())) {
            algoritmoCombo.getItems().setAll("Vigenère", "AES");
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
                // AES local simplificado (fachada)
                resultado = AESCryptoService.cifrar(texto, clave);
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
                // AES local simplificado (fachada)
                resultado = AESCryptoService.descifrar(textoCifrado, clave);
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
            File f = fc.showSaveDialog(getWindow());
            if (f != null) {
                Files.writeString(f.toPath(), nonNull(textoSalidaArea.getText()), StandardCharsets.UTF_8);
                actualizarStatus("Resultado guardado en: " + f.getName());
            }
        } catch (Exception e) {
            mostrarError("No se pudo guardar el archivo: " + e.getMessage());
        }
    }

    @FXML
    private void handleCopiar() {
        String texto = nonNull(textoSalidaArea.getText());
        if (!texto.isBlank()) {
            ClipboardContent content = new ClipboardContent();
            content.putString(texto);
            Clipboard.getSystemClipboard().setContent(content);
            actualizarStatus("Resultado copiado al portapapeles");
        } else {
            actualizarStatus("Nada que copiar");
        }
    }

    @FXML
    private void handlePegarEntrada() {
        Clipboard clipboard = Clipboard.getSystemClipboard();
        if (clipboard.hasString()) {
            textoEntradaArea.setText(clipboard.getString());
            actualizarStatus("Texto pegado desde el portapapeles");
        } else {
            actualizarStatus("No hay texto en el portapapeles");
        }
    }

    @FXML
    private void handleLimpiarEntrada() {
        if (textoEntradaArea != null) {
            textoEntradaArea.clear();
        }
        actualizarStatus("Entrada limpiada");
    }

    @FXML
    private void handleLimpiarSalida() {
        if (textoSalidaArea != null) {
            textoSalidaArea.clear();
        }
        actualizarStatus("Salida limpiada");
    }

    // === Utils internos ===

    private void actualizarStatus(String msg) {
        if (statusLabel != null) {
            statusLabel.setText(Objects.requireNonNullElse(msg, ""));
        }
    }

    private void mostrarError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error en cifrado");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
        actualizarStatus("Ocurrió un error");
    }

    private String safeGet(ComboBox<String> combo) {
        if (combo == null || combo.getValue() == null) {
            return "";
        }
        return combo.getValue();
    }

    private String nonNull(String s) {
        return s == null ? "" : s;
    }

    private Window getWindow() {
        return statusLabel != null ? statusLabel.getScene().getWindow() : null;
    }

    // === Vigenère local sencillo (por si no tienes aún tu APIClient) ===
    private static String cifrarVigenereLocal(String texto, String clave) {
        StringBuilder sb = new StringBuilder();
        int n = clave.length();
        if (n == 0) return texto;
        for (int i = 0; i < texto.length(); i++) {
            char ch = texto.charAt(i);
            char key = clave.charAt(i % n);
            sb.append(shiftVigenere(ch, key, true));
        }
        return sb.toString();
    }

    private static String descifrarVigenereLocal(String texto, String clave) {
        StringBuilder sb = new StringBuilder();
        int n = clave.length();
        if (n == 0) return texto;
        for (int i = 0; i < texto.length(); i++) {
            char ch = texto.charAt(i);
            char key = clave.charAt(i % n);
            sb.append(shiftVigenere(ch, key, false));
        }
        return sb.toString();
    }

    private static char shiftVigenere(char ch, char key, boolean encrypt) {
        // Versión básica: rota en ASCII visible (32..126)
        int base = 32, span = 95; // 126-32+1
        int c = ch, k = key;
        int shift = (k - base) % span;
        if (shift < 0) shift += span;
        if (!encrypt) shift = span - shift;
        if (c < base || c > 126) return ch; // deja fuera de rango tal cual
        return (char) (base + ((c - base + shift) % span));
    }

}



