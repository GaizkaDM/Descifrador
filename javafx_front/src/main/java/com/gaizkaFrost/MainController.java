package com.gaizkaFrost;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

public class MainController {

    private static final Logger logger = LoggerFactory.getLogger(MainController.class);

    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int IV_LENGTH = 12;   // 12 bytes típico GCM
    private static final int TAG_LENGTH = 128; // bits

    // ====== Controles mapeados 1:1 con el FXML ======

    @FXML
    private TextField claveField;          // fx:id="claveField"

    @FXML
    private ComboBox<String> algoritmoCombo; // fx:id="algoritmoCombo"

    @FXML
    private TextArea textoEntradaArea;     // fx:id="textoEntradaArea"

    @FXML
    private TextArea textoSalidaArea;      // fx:id="textoSalidaArea"

    @FXML
    private Label statusLabel;             // fx:id="statusLabel"

    @FXML
    private Button cifrarBtn;              // fx:id="cifrarBtn"

    @FXML
    private Button descifrarBtn;           // fx:id="descifrarBtn"

    // Si no es null, estamos trabajando con imagen
    private Path rutaImagenSeleccionada;

    @FXML
    public void initialize() {
        logger.info("Inicializando MainController");
        actualizarStatus("Listo");

        // Opcional: seleccionar AES por defecto
        if (algoritmoCombo != null && algoritmoCombo.getValue() == null) {
            algoritmoCombo.getSelectionModel().select("AES");
        }
    }

    private void actualizarStatus(String mensaje) {
        if (statusLabel != null) {
            statusLabel.setText(mensaje);
        }
        logger.debug("Status actualizado: {}", mensaje);
    }

    // ============================================================
    // CARGAR ARCHIVO (texto o imagen) - botón "Cargar archivo"
    // ============================================================
    @FXML
    public void handleCargarArchivo(ActionEvent event) {
        logger.info("Acción: cargar archivo de entrada");

        // Obtenemos la ventana desde el botón que disparó el evento
        Window window = ((Button) event.getSource()).getScene().getWindow();

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Selecciona archivo de entrada");

        File selectedFile = fileChooser.showOpenDialog(window);

        if (selectedFile != null) {
            try {
                String fileName = selectedFile.getName().toLowerCase();

                boolean isImage = fileName.endsWith(".png")
                        || fileName.endsWith(".jpg")
                        || fileName.endsWith(".jpeg")
                        || fileName.endsWith(".bmp");

                if (isImage) {
                    // MODO IMAGEN: no leemos como texto
                    rutaImagenSeleccionada = selectedFile.toPath();
                    textoEntradaArea.clear(); // así se ve claro que no hay texto
                    logger.info("Imagen seleccionada: {}", rutaImagenSeleccionada);
                    actualizarStatus("Imagen cargada correctamente");
                } else {
                    // MODO TEXTO
                    rutaImagenSeleccionada = null; // salimos de modo imagen
                    String contenido = Files.readString(selectedFile.toPath());
                    textoEntradaArea.setText(contenido);
                    logger.info("Archivo de texto cargado correctamente");
                    actualizarStatus("Archivo de texto cargado correctamente");
                }

            } catch (IOException e) {
                logger.error("No se pudo leer el archivo", e);
                actualizarStatus("Error al leer el archivo");
            }
        }
    }

    // ============================================================
    // CIFRAR (texto o imagen) - botón "Cifrar"
    // ============================================================
    @FXML
    public void handleCifrar(ActionEvent event) {
        try {
            String algoritmo = algoritmoCombo != null ? algoritmoCombo.getValue() : null;
            if (algoritmo == null) {
                actualizarStatus("Selecciona un algoritmo");
                showInfoAlert("Selecciona un algoritmo (AES / Vigenère).");
                return;
            }

            if (!"AES".equals(algoritmo)) {
                actualizarStatus("Algoritmo no soportado todavía: " + algoritmo);
                showInfoAlert("Por ahora solo está implementado AES (texto e imágenes).");
                return;
            }

            String texto = textoEntradaArea != null ? textoEntradaArea.getText() : "";

            if (texto != null && !texto.isBlank()) {
                // Si hay texto, mandamos a cifrar texto y salimos de "modo imagen"
                rutaImagenSeleccionada = null;
                cifrarTexto();
            } else if (rutaImagenSeleccionada != null) {
                // No hay texto, pero sí imagen seleccionada
                cifrarImagen();
            } else {
                actualizarStatus("No hay texto ni imagen para cifrar");
                showInfoAlert("Escribe un texto o carga una imagen antes de cifrar.");
            }

        } catch (Exception e) {
            logger.error("Error al cifrar", e);
            actualizarStatus("Error al cifrar");
            showInfoAlert("Se ha producido un error al cifrar. Revisa la clave y el contenido.");
        }
    }

    // ============================================================
    // DESCIFRAR (texto o imagen) - botón "Descifrar"
    // ============================================================
    @FXML
    public void handleDescifrar(ActionEvent event) {
        try {
            String algoritmo = algoritmoCombo != null ? algoritmoCombo.getValue() : null;
            if (algoritmo == null) {
                actualizarStatus("Selecciona un algoritmo");
                showInfoAlert("Selecciona un algoritmo (AES / Vigenère).");
                return;
            }

            if (!"AES".equals(algoritmo)) {
                actualizarStatus("Algoritmo no soportado todavía: " + algoritmo);
                showInfoAlert("Por ahora solo está implementado AES (texto e imágenes).");
                return;
            }

            String texto = textoEntradaArea != null ? textoEntradaArea.getText() : "";

            if (texto != null && !texto.isBlank()) {
                // Hay texto en la entrada → lo tratamos como texto cifrado
                descifrarTextoDesdeTextArea();
            } else {
                // No hay texto → asumimos que quieres descifrar una imagen .enc
                descifrarImagenConFileChooser();
            }

        } catch (Exception e) {
            logger.error("Error al descifrar", e);
            actualizarStatus("Error al descifrar");
            showInfoAlert("Se ha producido un error al descifrar. Revisa la clave y el contenido.");
        }
    }

    // ============================================================
    // LÓGICA AES PARA TEXTO
    // ============================================================
    private SecretKey deriveKeyFromString(String key) throws NoSuchAlgorithmException {
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        byte[] keyBytes = sha.digest(key.getBytes(StandardCharsets.UTF_8));
        keyBytes = Arrays.copyOf(keyBytes, 16); // 128 bits
        return new SecretKeySpec(keyBytes, "AES");
    }

    private String cifrarTextoAES(String textoPlano, String clave) throws Exception {
        SecretKey secretKey = deriveKeyFromString(clave);

        // IV aleatorio
        byte[] iv = new byte[IV_LENGTH];
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);

        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        GCMParameterSpec gcmSpec = new GCMParameterSpec(TAG_LENGTH, iv);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmSpec);

        byte[] cifrado = cipher.doFinal(textoPlano.getBytes(StandardCharsets.UTF_8));

        // Guardamos IV + cifrado en Base64
        byte[] ivMasCifrado = new byte[IV_LENGTH + cifrado.length];
        System.arraycopy(iv, 0, ivMasCifrado, 0, IV_LENGTH);
        System.arraycopy(cifrado, 0, ivMasCifrado, IV_LENGTH, cifrado.length);

        return Base64.getEncoder().encodeToString(ivMasCifrado);
    }

    private String descifrarTextoAES(String textoCifradoBase64, String clave) throws Exception {
        SecretKey secretKey = deriveKeyFromString(clave);

        byte[] ivMasCifrado = Base64.getDecoder().decode(textoCifradoBase64);

        if (ivMasCifrado.length < IV_LENGTH) {
            throw new IllegalArgumentException("Datos cifrados inválidos para texto.");
        }

        byte[] iv = Arrays.copyOfRange(ivMasCifrado, 0, IV_LENGTH);
        byte[] cifrado = Arrays.copyOfRange(ivMasCifrado, IV_LENGTH, ivMasCifrado.length);

        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        GCMParameterSpec gcmSpec = new GCMParameterSpec(TAG_LENGTH, iv);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec);

        byte[] plano = cipher.doFinal(cifrado);

        return new String(plano, StandardCharsets.UTF_8);
    }

    private void cifrarTexto() throws Exception {
        String texto = textoEntradaArea.getText();
        if (texto == null || texto.isBlank()) {
            actualizarStatus("No hay texto para cifrar");
            return;
        }

        String clave = claveField.getText();
        if (clave == null || clave.isBlank()) {
            actualizarStatus("Introduce una clave AES para cifrar el texto");
            showInfoAlert("Introduce una clave para cifrar el texto.");
            return;
        }

        logger.info("Cifrando texto con AES...");

        String cifradoBase64 = cifrarTextoAES(texto, clave);

        textoSalidaArea.setText(cifradoBase64);
        actualizarStatus("Texto cifrado correctamente");
    }

    private void descifrarTextoDesdeTextArea() throws Exception {
        String textoCifrado = textoEntradaArea.getText();
        if (textoCifrado == null || textoCifrado.isBlank()) {
            actualizarStatus("No hay texto cifrado para descifrar");
            return;
        }

        String clave = claveField.getText();
        if (clave == null || clave.isBlank()) {
            actualizarStatus("Introduce una clave AES para descifrar el texto");
            showInfoAlert("Introduce una clave para descifrar el texto.");
            return;
        }

        logger.info("Descifrando texto con AES...");

        String textoPlano = descifrarTextoAES(textoCifrado.trim(), clave);

        textoSalidaArea.setText(textoPlano);
        actualizarStatus("Texto descifrado correctamente");
    }

    // ============================================================
    // LÓGICA PARA IMÁGENES (usa AESImageService)
    // ============================================================
    private void cifrarImagen() throws Exception {
        if (rutaImagenSeleccionada == null) {
            actualizarStatus("Primero selecciona una imagen");
            showInfoAlert("Primero selecciona una imagen con el botón 'Cargar archivo'.");
            return;
        }

        String clave = claveField.getText();
        if (clave == null || clave.isBlank()) {
            actualizarStatus("Introduce una clave AES para cifrar la imagen");
            showInfoAlert("Introduce una clave para cifrar la imagen.");
            return;
        }

        logger.info("Cifrando imagen: {}", rutaImagenSeleccionada);

        AESImageService imageService = new AESImageService(clave);

        Path inputPath = rutaImagenSeleccionada;
        Path outputPath = inputPath.resolveSibling(inputPath.getFileName().toString() + ".enc");

        imageService.encryptImage(inputPath, outputPath);

        logger.info("Imagen cifrada en: {}", outputPath);
        actualizarStatus("Imagen cifrada en: " + outputPath);
    }

    private void descifrarImagenConFileChooser() throws Exception {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Selecciona imagen cifrada (.enc)");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Archivos cifrados", "*.enc")
        );

        Window window = descifrarBtn.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(window);
        if (selectedFile == null) {
            return;
        }

        Path encryptedPath = selectedFile.toPath();

        String clave = claveField.getText();
        if (clave == null || clave.isBlank()) {
            actualizarStatus("Introduce una clave AES para descifrar la imagen");
            showInfoAlert("Introduce una clave para descifrar la imagen.");
            return;
        }

        logger.info("Descifrando imagen cifrada: {}", encryptedPath);

        AESImageService imageService = new AESImageService(clave);

        String originalName = encryptedPath.getFileName().toString();
        String baseName = originalName.endsWith(".enc")
                ? originalName.substring(0, originalName.length() - 4)
                : originalName;

        Path outputPath = encryptedPath.resolveSibling(baseName + "_descifrada.png");

        imageService.decryptImage(encryptedPath, outputPath);

        logger.info("Imagen descifrada en: {}", outputPath);
        actualizarStatus("Imagen descifrada en: " + outputPath);
    }

    // ============================================================
    // MENÚS Y BOTONES EXTRA DEL FXML
    // ============================================================

    @FXML
    private void handleSalir() {
        Platform.exit();
    }

    @FXML
    private void handleCambiarEspañol() {
        showInfoAlert("Idioma cambiado a Español (placeholder).");
    }

    @FXML
    private void handleCambiarIngles() {
        showInfoAlert("Language changed to English (placeholder).");
    }

    @FXML
    private void handleManual() {
        showInfoAlert("Manual:\nUsa este programa para cifrar o descifrar texto e imágenes usando AES.");
    }

    @FXML
    private void handleSobreNosotros() {
        showInfoAlert("Sobre Nosotros:\nEste software fue desarrollado por Gaizka y Diego.");
    }

    @FXML
    private void handleLimpiarEntrada() {
        if (textoEntradaArea != null) {
            textoEntradaArea.clear();
        }
        rutaImagenSeleccionada = null; // muy importante
        actualizarStatus("Entrada limpiada");
    }

    @FXML
    private void handleGuardarResultado() {
        String contenido = textoSalidaArea.getText();
        if (contenido == null || contenido.isBlank()) {
            actualizarStatus("No hay resultado para guardar");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar resultado");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Archivo de texto", "*.txt")
        );
        fileChooser.setInitialFileName("resultado.txt");

        Window window = cifrarBtn.getScene().getWindow();
        File file = fileChooser.showSaveDialog(window);
        if (file == null) {
            actualizarStatus("Guardado cancelado");
            return;
        }

        try {
            Files.writeString(file.toPath(), contenido, StandardCharsets.UTF_8);
            logger.info("Resultado guardado en: {}", file.toPath());
            actualizarStatus("Resultado guardado en: " + file.getAbsolutePath());
        } catch (IOException e) {
            logger.error("Error al guardar el resultado", e);
            actualizarStatus("Error al guardar el resultado");
        }
    }

    @FXML
    private void handleCopiar() {
        String contenido = textoSalidaArea.getText();
        if (contenido == null || contenido.isBlank()) {
            actualizarStatus("Nada que copiar");
            return;
        }

        ClipboardContent clipboardContent = new ClipboardContent();
        clipboardContent.putString(contenido);
        Clipboard.getSystemClipboard().setContent(clipboardContent);

        actualizarStatus("Resultado copiado al portapapeles");
    }

    // ============================================================
    // Métodos auxiliares
    // ============================================================
    private void showInfoAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Información");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
