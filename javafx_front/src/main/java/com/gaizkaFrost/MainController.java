package com.gaizkaFrost;

import com.gaizkaFrost.AES.CryptoException;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Objects;

public class MainController {

    private static final Logger logger = LoggerFactory.getLogger(MainController.class);

    // === FXML ===
    @FXML private TextField claveField;
    @FXML private TextArea textoEntradaArea;
    @FXML private TextArea textoSalidaArea;
    @FXML private ComboBox<String> algoritmoCombo;
    @FXML private Label statusLabel;

    @FXML
    private void initialize() {
        logger.info("Inicializando MainController");

        if (algoritmoCombo != null && (algoritmoCombo.getValue() == null || algoritmoCombo.getValue().isBlank())) {
            algoritmoCombo.getItems().setAll("Vigenère", "AES");
            algoritmoCombo.setValue("AES");
        }
        actualizarStatus("Listo");
    }

    // === Handlers de botones ===

    @FXML
    private void handleCifrar() {
        String algoritmo = safeGet(algoritmoCombo);
        String clave = nonNull(claveField.getText());
        String texto = nonNull(textoEntradaArea.getText());

        logger.info("Solicitado CIFRADO. Algoritmo={}, longitudTexto={}", algoritmo, texto.length());

        if (texto.isBlank()) {
            logger.warn("Intento de cifrado con texto vacío");
            mostrarError("No hay texto para cifrar");
            return;
        }
        if (clave.isBlank()) {
            logger.warn("Intento de cifrado con clave vacía");
            mostrarError("La clave no puede estar vacía");
            return;
        }

        try {
            String resultado;

            if ("Vigenère".equals(algoritmo)) {
                // Vigenère → log y manejo los hace vuestro código (API / lo que tenga tu compi)
                resultado = APIClient.cifrarVigenere(texto, clave);
                // resultado = cifrarVigenereLocal(texto, clave);
                actualizarStatus("Texto cifrado con Vigenère");
            } else {
                // AES local con logger aquí
                resultado = AESCryptoService.cifrar(texto, clave);
                actualizarStatus("Texto cifrado con AES");
            }

            textoSalidaArea.setText(resultado);

        } catch (Exception e) {
            logger.error("Error al cifrar", e);
            mostrarError("Error al cifrar: " + e.getMessage());
        }
    }

    @FXML
    private void handleDescifrar() {
        String algoritmo = safeGet(algoritmoCombo);
        String clave = nonNull(claveField.getText());
        String textoCifrado = nonNull(textoEntradaArea.getText());

        logger.info("Solicitado DESCIFRADO. Algoritmo={}, longitudEntrada={}", algoritmo, textoCifrado.length());

        if (textoCifrado.isBlank()) {
            logger.warn("Intento de descifrado con texto vacío");
            mostrarError("No hay texto para descifrar");
            return;
        }
        if (clave.isBlank()) {
            logger.warn("Intento de descifrado con clave vacía");
            mostrarError("La clave no puede estar vacía");
            return;
        }

        try {
            String resultado;

            if ("Vigenère".equals(algoritmo)) {
                // Vigenère → logging propio fuera del controlador
                resultado = APIClient.descifrarVigenere(textoCifrado, clave);
                actualizarStatus("Texto descifrado con Vigenère");
            } else {
                // AES local
                try {
                    resultado = AESCryptoService.descifrar(textoCifrado, clave);
                    actualizarStatus("Texto descifrado con AES");
                } catch (CryptoException e) {
                    logger.warn("Falló el descifrado AES: contraseña incorrecta o datos corruptos", e);
                    mostrarError("Contraseña incorrecta o datos corruptos");
                    return;
                }
            }

            textoSalidaArea.setText(resultado);

        } catch (Exception e) {
            logger.error("Error al descifrar", e);
            mostrarError("Error al descifrar: " + e.getMessage());
        }
    }

    @FXML
    private void handleCargarArchivo() {
        logger.info("Acción: cargar archivo de entrada");
        try {
            FileChooser fc = new FileChooser();
            fc.setTitle("Abrir archivo de entrada");
            File f = fc.showOpenDialog(getWindow());
            if (f != null) {
                String content = Files.readString(f.toPath(), StandardCharsets.UTF_8);
                textoEntradaArea.setText(content);
                actualizarStatus("Archivo cargado: " + f.getName());
                logger.info("Archivo cargado correctamente: {}", f.getAbsolutePath());
            } else {
                logger.info("Carga de archivo cancelada por el usuario");
                actualizarStatus("Carga de archivo cancelada");
            }
        } catch (Exception e) {
            logger.error("No se pudo leer el archivo", e);
            mostrarError("No se pudo leer el archivo: " + e.getMessage());
        }
    }

    @FXML
    private void handleGuardarResultado() {
        logger.info("Acción: guardar resultado en archivo");
        try {
            FileChooser fc = new FileChooser();
            fc.setTitle("Guardar resultado");
            File f = fc.showSaveDialog(getWindow());
            if (f != null) {
                Files.writeString(f.toPath(), nonNull(textoSalidaArea.getText()), StandardCharsets.UTF_8);
                actualizarStatus("Resultado guardado en: " + f.getName());
                logger.info("Resultado guardado correctamente en {}", f.getAbsolutePath());
            } else {
                logger.info("Guardado de archivo cancelado por el usuario");
                actualizarStatus("Guardado cancelado");
            }
        } catch (Exception e) {
            logger.error("No se pudo guardar el archivo", e);
            mostrarError("No se pudo guardar el archivo: " + e.getMessage());
        }
    }

    @FXML
    private void handleCopiar() {
        logger.info("Acción: copiar resultado al portapapeles");
        String texto = nonNull(textoSalidaArea.getText());
        if (!texto.isBlank()) {
            ClipboardContent content = new ClipboardContent();
            content.putString(texto);
            Clipboard.getSystemClipboard().setContent(content);
            actualizarStatus("Resultado copiado al portapapeles");
            logger.info("Resultado copiado al portapapeles (longitud={})", texto.length());
        } else {
            actualizarStatus("Nada que copiar");
            logger.warn("Intento de copiar resultado vacío");
        }
    }

    @FXML
    private void handlePegarEntrada() {
        logger.info("Acción: pegar texto en entrada desde portapapeles");
        Clipboard clipboard = Clipboard.getSystemClipboard();
        if (clipboard.hasString()) {
            String texto = clipboard.getString();
            textoEntradaArea.setText(texto);
            actualizarStatus("Texto pegado desde el portapapeles");
            logger.info("Texto pegado desde portapapeles (longitud={})", texto.length());
        } else {
            actualizarStatus("No hay texto en el portapapeles");
            logger.warn("No se encontró texto en el portapapeles al intentar pegar");
        }
    }

    @FXML
    private void handleLimpiarEntrada() {
        logger.info("Acción: limpiar entrada");
        if (textoEntradaArea != null) {
            textoEntradaArea.clear();
        }
        actualizarStatus("Entrada limpiada");
    }

    @FXML
    private void handleLimpiarSalida() {
        logger.info("Acción: limpiar salida");
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
        logger.debug("Status actualizado: {}", msg);
    }

    private void mostrarError(String msg) {
         Alert a = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        a.showAndWait();
    }

    private String safeGet(ComboBox<String> combo){
        if (combo == null || combo.getValue() == null){
            return "";
        }
        return combo.getValue();
    }
    private String nonNull (String s){
        return s == null ? "" : s;
    }

    private Window getWindow() {
        return statusLabel != null ? statusLabel.getScene().getWindow() : null;
    }

    private void mostrarErrorAlert(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Ha ocurrido un error");
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

        @FXML
        private void handleSalir() {
            Platform.exit();  // Cierra la aplicación
        }

        @FXML
        private void handleCambiarEspañol() {
            // Aquí deberías implementar el cambio de idioma a Español
            // Por ahora, avisamos al usuario
            showInfoAlert("Idioma cambiado a Español");
        }

        @FXML
        private void handleCambiarIngles() {
            // Aquí deberías implementar el cambio de idioma a Inglés
            showInfoAlert("Language changed to English");
        }

        @FXML
        private void handleManual() {
            // Abre el manual. Ejemplo: mostrar alerta o ventana con instrucciones
            showInfoAlert("Manual:\nUsa este programa para cifrar o descifrar texto.");
        }

        @FXML
        private void handleSobreNosotros() {
            // Muestra una ventana o diálogo con info sobre el equipo o proyecto
            showInfoAlert("Sobre Nosotros:\nEste software fue desarrollado por Gaizka y Diego.");
        }

        // Método auxiliar para mostrar un alert informativo
        private void showInfoAlert(String message) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Información");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        }

        // Aquí irían el resto de tus métodos como handleCifrar(), handleDescifrar(), etc.
}




