package com.gaizkaFrost;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
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
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;
import javafx.concurrent.Task;
import javafx.scene.web.WebView;

/**
 * <h2>Controlador principal de la aplicación</h2>
 * Clase encargada de gestionar la lógica de la interfaz gráfica:
 * carga de archivos, cifrado y descifrado de texto e imágenes (AES / Vigenère),
 * gestión de idioma, menús y actualizaciones de estado.
 *
 * <p>Se comunica con servicios auxiliares como {@code AESImageService} y
 * {@code APIClient} para realizar las operaciones criptográficas.</p>
 *
 * @author Gaizka
 * @author Diego
 * @version 1.0
 * @since 2025
 */
public class MainController {

    /**
     * Logger de la clase para registrar información, advertencias y errores.
     */
    private static final Logger logger = LoggerFactory.getLogger(MainController.class);

    /**
     * Transformación criptográfica utilizada para AES en modo GCM sin padding.
     */
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";

    /**
     * Longitud del vector de inicialización (IV) en bytes para GCM.
     */
    private static final int IV_LENGTH = 12;   // 12 bytes típico GCM

    /**
     * Longitud de la etiqueta de autenticación GCM en bits.
     */
    private static final int TAG_LENGTH = 128; // bits

    // ====== Controles mapeados 1:1 con el FXML ======

    @FXML
    private ComboBox<String> algoritmoCombo; // fx:id="algoritmoCombo"
    // === FXML ===
    @FXML private Menu archivoMenu;
    @FXML private MenuItem salirMenuItem;
    @FXML private Menu idiomaMenu;
    @FXML private MenuItem espanolMenuItem;
    @FXML private MenuItem inglesMenuItem;
    @FXML private Menu infoMenu;
    @FXML private MenuItem manualMenuItem;
    @FXML private MenuItem sobreNosotrosMenuItem;

    // Campos y botones principales
    @FXML private Label claveLabel;
    @FXML private TextField claveField;
    @FXML private Label algoritmoLabel;
    @FXML private Button cifrarBtn;
    @FXML private Button descifrarBtn;

    @FXML private Label entradaLabel;
    @FXML private TextArea textoEntradaArea;
    @FXML private Button cargarBtn;
    @FXML private Button limpiarBtn;

    @FXML private Label salidaLabel;
    @FXML private TextArea textoSalidaArea;
    @FXML private Button guardarBtn;
    @FXML private Button copiarBtn;

    @FXML private Label statusLabel;
    // Añade más controles que necesites actualizar

    /**
     * Locale actual de la interfaz (idioma seleccionado).
     */
    Locale currentLocale;

    /**
     * Recurso de mensajes internacionalizados (i18n) para los textos de la interfaz.
     */
    ResourceBundle bundle = ResourceBundle.getBundle("messages", new Locale("es", "ES"));
    // fx:id="descifrarBtn"

    /**
     * Si no es null, indica que se está trabajando con una imagen
     * (original o cifrada) en lugar de texto.
     */
    // Si no es null, estamos trabajando con imagen
    private Path rutaImagenSeleccionada;

    /**
     * <h3>Método de inicialización del controlador</h3>
     * Se ejecuta automáticamente tras la carga del FXML.
     * Configura el idioma por defecto, inicializa el combo de algoritmos
     * y establece el estado inicial de la interfaz.
     */
    @FXML
    public void initialize() {
        logger.info("Inicializando MainController");
        cambiarIdioma(new Locale("es", "ES"));
        if (algoritmoCombo != null && (algoritmoCombo.getValue() == null || algoritmoCombo.getValue().isBlank())) {
            algoritmoCombo.getItems().setAll("Vigenère", "AES");
            algoritmoCombo.setValue("AES");
        }
        actualizarStatus("Listo");

        // Opcional: seleccionar AES por defecto
        if (algoritmoCombo != null && algoritmoCombo.getValue() == null) {
            algoritmoCombo.getSelectionModel().select("AES");
        }
    }

    /**
     * Actualiza el texto del label de estado y registra el mensaje en el logger.
     *
     * @param mensaje Mensaje a mostrar en la barra de estado.
     */
    private void actualizarStatus(String mensaje) {
        if (statusLabel != null) {
            statusLabel.setText(mensaje);
        }
        logger.debug("Status actualizado: {}", mensaje);
    }

    // ============================================================
    // CARGAR ARCHIVO (texto o imagen) - botón "Cargar archivo"
    // ============================================================

    /**
     * Maneja la acción de cargar un archivo de entrada.
     * Permite seleccionar archivos de texto o imágenes. Si es imagen o archivo cifrado,
     * se marca para cifrado/descifrado de imagen; si es texto, se carga en el área de entrada.
     *
     * @param event Evento de acción disparado por el botón "Cargar archivo".
     */
    @FXML
    public void handleCargarArchivo(ActionEvent event) {
        logger.info("Acción: cargar archivo de entrada");

        Window window = ((Button) event.getSource()).getScene().getWindow();

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Selecciona archivo de entrada");

        File selectedFile = fileChooser.showOpenDialog(window);

        if (selectedFile != null) {
            String fileName = selectedFile.getName().toLowerCase();

            boolean isImage = fileName.endsWith(".png")
                    || fileName.endsWith(".jpg")
                    || fileName.endsWith(".jpeg")
                    || fileName.endsWith(".bmp");

            boolean isEncryptedImage = fileName.endsWith(".enc");

            if (isImage || isEncryptedImage) {
                rutaImagenSeleccionada = selectedFile.toPath();
                textoEntradaArea.clear();
                textoEntradaArea.setText("🖼️ Imagen cargada: " + selectedFile.getName());
                logger.info("Archivo de imagen o cifrado seleccionado: {}", rutaImagenSeleccionada);
                actualizarStatus("Archivo de imagen cargado correctamente");
                showInfoAlert("Archivo de imagen cargado correctamente");
            } else {
                rutaImagenSeleccionada = null;
                try {
                    String contenido = Files.readString(selectedFile.toPath());
                    textoEntradaArea.setText(contenido);
                    logger.info("Archivo de texto cargado correctamente");
                    actualizarStatus("Archivo de texto cargado correctamente");
                } catch (IOException e) {
                    logger.error("No se pudo leer el archivo", e);
                    actualizarStatus("Error al leer el archivo");
                    showInfoAlert("No se pudo leer el archivo seleccionado como texto.");
                }
            }
        }
    }

    // ============================================================
    // CIFRAR (texto o imagen) - botón "Cifrar"
    // ============================================================

    /**
     * Maneja la acción del botón "Cifrar".
     * Según el algoritmo seleccionado (AES o Vigenère) y el tipo de entrada
     * (texto o imagen), realiza el cifrado correspondiente.
     *
     * <ul>
     *     <li>Vigenère: solo texto, vía API externa ({@code APIClient}).</li>
     *     <li>AES: texto o imagen, cifrado local.</li>
     * </ul>
     *
     * @param event Evento de acción generado al pulsar el botón "Cifrar".
     */
    @FXML
    public void handleCifrar(ActionEvent event) {
        try {
            String algoritmo = algoritmoCombo != null ? algoritmoCombo.getValue() : null;
            String clave = claveField != null ? claveField.getText() : "";

            if (algoritmo == null) {
                actualizarStatus("Selecciona un algoritmo");
                showInfoAlert("Selecciona un algoritmo (AES / Vigenère).");
                return;
            }

            if (clave == null || clave.isBlank()) {
                actualizarStatus("Introduce una clave para cifrar");
                showInfoAlert("Introduce una clave antes de cifrar.");
                return;
            }

            if ("Vigenère".equalsIgnoreCase(algoritmo)) {
                if (rutaImagenSeleccionada != null) {
                    actualizarStatus("Vigenère no soporta cifrar imágenes");
                    showInfoAlert("Por ahora la encriptación de imágenes solo está soportada con AES.");
                    return;
                }
                String texto = textoEntradaArea != null ? textoEntradaArea.getText() : "";
                if (texto == null || texto.isBlank()) {
                    actualizarStatus("No hay texto para cifrar");
                    showInfoAlert("Escribe un texto antes de cifrar.");
                    return;
                }
                actualizarStatus("Cifrando texto con Vigenère (API)...");
                Task<String> task = new Task<>() {
                    @Override
                    protected String call() throws Exception {
                        return APIClient.cifrarVigenere(texto, clave);
                    }
                };
                task.setOnSucceeded(e -> {
                    textoSalidaArea.setText(task.getValue());
                    actualizarStatus("Texto cifrado con Vigenère correctamente");
                });
                task.setOnFailed(e -> {
                    actualizarStatus("Error cifrando texto con Vigenère");
                    showInfoAlert("Error al cifrar con Vigenère: " + task.getException().getMessage());
                });
                new Thread(task).start();
            } else if ("AES".equalsIgnoreCase(algoritmo)) {
                if (rutaImagenSeleccionada != null) {
                    cifrarImagen();
                } else {
                    String texto = textoEntradaArea != null ? textoEntradaArea.getText() : "";
                    if (texto == null || texto.isBlank()) {
                        actualizarStatus("No hay texto para cifrar");
                        showInfoAlert("Escribe un texto antes de cifrar.");
                        return;
                    }
                    cifrarTexto();
                }
            } else {
                actualizarStatus("Algoritmo no soportado: " + algoritmo);
                showInfoAlert("Solo se soportan AES y Vigenère actualmente.");
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

    /**
     * Maneja la acción del botón "Descifrar".
     * Según el algoritmo seleccionado y el tipo de entrada, realiza el descifrado
     * de texto o imagen, ya sea mediante la API (Vigenère) o lógica local (AES).
     *
     * @param event Evento de acción generado al pulsar el botón "Descifrar".
     */
    @FXML
    public void handleDescifrar(ActionEvent event) {
        try {
            String algoritmo = algoritmoCombo != null ? algoritmoCombo.getValue() : null;
            String clave = claveField != null ? claveField.getText() : "";

            if (algoritmo == null) {
                actualizarStatus("Selecciona un algoritmo");
                showInfoAlert("Selecciona un algoritmo (AES / Vigenère).");
                return;
            }

            if (clave == null || clave.isBlank()) {
                actualizarStatus("Introduce una clave para descifrar");
                showInfoAlert("Introduce una clave antes de descifrar.");
                return;
            }

            if ("Vigenère".equalsIgnoreCase(algoritmo)) {
                String texto = textoEntradaArea != null ? textoEntradaArea.getText() : "";
                if (texto == null || texto.isBlank() || texto.startsWith("🖼️ Imagen cargada")) {
                    actualizarStatus("No hay texto para descifrar");
                    showInfoAlert("Escribe un texto válido para descifrar con Vigenère.");
                    return;
                }
                actualizarStatus("Descifrando texto con Vigenère (API)...");
                Task<String> task = new Task<>() {
                    @Override
                    protected String call() throws Exception {
                        return APIClient.descifrarVigenere(texto, clave);
                    }
                };
                task.setOnSucceeded(e -> {
                    textoSalidaArea.setText(task.getValue());
                    actualizarStatus("Texto descifrado con Vigenère correctamente");
                });
                task.setOnFailed(e -> {
                    actualizarStatus("Error descifrando texto con Vigenère");
                    showInfoAlert("Error al descifrar con Vigenère: " + task.getException().getMessage());
                });
                new Thread(task).start();
            } else if ("AES".equalsIgnoreCase(algoritmo)) {
                if (rutaImagenSeleccionada != null) {
                    descifrarImagenDesdeRuta(rutaImagenSeleccionada, clave);
                } else {
                    String texto = textoEntradaArea != null ? textoEntradaArea.getText() : "";
                    if (texto == null || texto.isBlank() || texto.startsWith("🖼️ Imagen cargada")) {
                        actualizarStatus("No hay texto para descifrar");
                        showInfoAlert("Escribe un texto para descifrar, o carga una imagen.");
                        return;
                    }
                    descifrarTextoDesdeTextArea();
                }
            } else {
                actualizarStatus("Algoritmo no soportado: " + algoritmo);
                showInfoAlert("Solo se soportan AES y Vigenère actualmente.");
            }
        } catch (Exception e) {
            logger.error("Error al descifrar", e);
            actualizarStatus("Error al descifrar");
            showInfoAlert("Se ha producido un error al descifrar. Revisa la clave y el contenido.");
        }
    }

    /**
     * Descifra una imagen cifrada con AES a partir de una ruta dada
     * y una clave proporcionada por el usuario.
     *
     * @param encryptedPath Ruta del archivo de imagen cifrado (.enc).
     * @param clave         Clave de cifrado AES introducida por el usuario.
     */
    private void descifrarImagenDesdeRuta(Path encryptedPath, String clave) {
        try {
            logger.info("Descifrando imagen desde ruta: {}", encryptedPath);

            AESImageService imageService = new AESImageService(clave);

            String originalName = encryptedPath.getFileName().toString();
            String baseName = originalName.endsWith(".enc")
                    ? originalName.substring(0, originalName.length() - 4)
                    : originalName;

            Path outputPath = encryptedPath.resolveSibling(baseName + "_descifrada.png");

            imageService.decryptImage(encryptedPath, outputPath);

            logger.info("Imagen descifrada en: {}", outputPath);
            actualizarStatus("Imagen descifrada en: " + outputPath);
            showInfoAlert("Imagen descifrada en: " + outputPath);
            resetCampos();
        } catch (Exception e) {
            logger.error("Error al descifrar la imagen desde ruta", e);
            actualizarStatus("Error al descifrar la imagen");
            showInfoAlert("Error al descifrar la imagen: " + e.getMessage());
        }
    }

    // ============================================================
    // LÓGICA AES PARA TEXTO
    // ============================================================

    /**
     * Deriva una clave AES de 128 bits a partir de una cadena de texto.
     * Se utiliza SHA-256 y se toman los primeros 16 bytes del hash.
     *
     * @param key Cadena de texto introducida como clave.
     * @return Clave secreta AES derivada.
     * @throws NoSuchAlgorithmException Si no se encuentra el algoritmo SHA-256.
     */
    private SecretKey deriveKeyFromString(String key) throws NoSuchAlgorithmException {
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        byte[] keyBytes = sha.digest(key.getBytes(StandardCharsets.UTF_8));
        keyBytes = Arrays.copyOf(keyBytes, 16); // 128 bits
        return new SecretKeySpec(keyBytes, "AES");
    }

    /**
     * Cifra un texto plano utilizando AES/GCM/NoPadding.
     * Devuelve el resultado como Base64 incluyendo IV + datos cifrados.
     *
     * @param textoPlano Texto original a cifrar.
     * @param clave      Clave de cifrado proporcionada por el usuario.
     * @return Cadena Base64 que contiene IV + texto cifrado.
     * @throws Exception Si se produce algún error criptográfico.
     */
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

    /**
     * Descifra un texto cifrado con AES/GCM/NoPadding, donde el contenido Base64
     * incluye IV + datos cifrados.
     *
     * @param textoCifradoBase64 Cadena Base64 con IV + texto cifrado.
     * @param clave              Clave de cifrado AES.
     * @return Texto plano resultante tras el descifrado.
     * @throws Exception Si el formato es inválido o la clave no es correcta.
     */
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

    /**
     * Cifra el contenido del área de texto de entrada usando AES
     * y coloca el resultado cifrado en el área de texto de salida.
     *
     * @throws Exception Si ocurre algún problema durante el cifrado.
     */
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

    /**
     * Descifra el contenido cifrado presente en el área de texto de entrada,
     * usando AES, y muestra el texto plano en el área de salida.
     *
     * @throws Exception Si el texto no es válido o la clave es incorrecta.
     */
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

    /**
     * Cifra una imagen seleccionada usando {@link AESImageService}.
     * El resultado se guarda en un archivo con extensión {@code .enc}
     * en la misma carpeta que la imagen original.
     *
     * @throws Exception Si no hay imagen seleccionada o hay errores de cifrado.
     */
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
        showInfoAlert("Imagen cifrada en: " + outputPath);
        resetCampos();

    }

    /**
     * Permite al usuario escoger un archivo de imagen cifrada (.enc) mediante
     * un {@link FileChooser} y, usando {@link AESImageService}, lo descifra
     * a un archivo PNG en la misma carpeta.
     *
     * @throws Exception Si se cancela la selección o falla el descifrado.
     */
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

    /**
     * Cierra la aplicación cuando se selecciona la opción de menú "Salir".
     */
    @FXML
    private void handleSalir() {
        Platform.exit();
    }

    /**
     * Cambia el idioma de la interfaz a español y muestra un aviso informativo.
     */
    @FXML
    private void handleCambiarEspañol() {
        cambiarIdioma(new Locale("es", "ES"));
        showInfoAlert("Idioma cambiado a Español");
    }

    /**
     * Cambia el idioma de la interfaz a inglés y muestra un aviso informativo.
     */
    @FXML
    private void handleCambiarIngles() {
        cambiarIdioma(new Locale("en", "EN"));
        showInfoAlert("Language changed to English");
    }

    /**
     * Muestra un mensaje con instrucciones básicas de uso del programa.
     */
    @FXML
    private void handleManual() {
        // Abre el manual. Ejemplo: mostrar alerta o ventana con instrucciones
        showInfoAlert("Manual:\nUsa este programa para cifrar o descifrar texto.");
    }

    /**
     * Muestra información sobre el equipo desarrollador del software.
     */
    @FXML
    private void handleSobreNosotros() {
        // Muestra una ventana o diálogo con info sobre el equipo o proyecto
        showInfoAlert("Sobre Nosotros:\nEste software fue desarrollado por Gaizka y Diego.");
    }

    /**
     * Limpia el área de texto de entrada y desasocia cualquier imagen seleccionada.
     */
    @FXML
    private void handleLimpiarEntrada() {
        if (textoEntradaArea != null) {
            textoEntradaArea.clear();
            textoSalidaArea.clear();
        }
        rutaImagenSeleccionada = null; // muy importante
        actualizarStatus("Entrada limpiada");
    }

    /**
     * Guarda el contenido del área de texto de salida en un archivo .txt
     * seleccionado por el usuario mediante un {@link FileChooser}.
     */
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

    /**
     * Copia el contenido del área de texto de salida al portapapeles del sistema.
     */
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

    /**
     * Muestra una alerta informativa con el mensaje proporcionado.
     *
     * @param message Texto a mostrar en la ventana de información.
     */
    private void showInfoAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Información");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Cambia el idioma de la interfaz, actualizando los textos mediante un
     * {@link ResourceBundle}.
     *
     * @param locale Idioma y región a aplicar en la interfaz.
     */
    public void cambiarIdioma(Locale locale) {
        currentLocale = locale;
        bundle = ResourceBundle.getBundle("messages", currentLocale);
        actualizarTextos(bundle);
    }

    /**
     * Actualiza todos los textos de los controles de la interfaz
     * usando el {@link ResourceBundle} indicado.
     *
     * @param bundle Recurso con los textos internacionalizados.
     */
    // Método para actualizar todos los textos con el ResourceBundle elegido
    private void actualizarTextos(ResourceBundle bundle) {
        archivoMenu.setText(bundle.getString("archivo"));
        salirMenuItem.setText(bundle.getString("salir"));
        idiomaMenu.setText(bundle.getString("idioma"));
        espanolMenuItem.setText(bundle.getString("espanol"));
        inglesMenuItem.setText(bundle.getString("ingles"));
        infoMenu.setText(bundle.getString("informacion"));
        manualMenuItem.setText(bundle.getString("manual"));
        sobreNosotrosMenuItem.setText(bundle.getString("sobre_nosotros"));
        claveLabel.setText(bundle.getString("clave"));
        claveField.setPromptText(bundle.getString("introduce_wizard_key"));
        algoritmoLabel.setText(bundle.getString("algoritmo"));
        algoritmoCombo.getItems().setAll(bundle.getString("vigenere"), bundle.getString("aes"));
        cifrarBtn.setText(bundle.getString("cifrar"));
        descifrarBtn.setText(bundle.getString("descifrar"));
        entradaLabel.setText(bundle.getString("texto_entrada"));
        textoEntradaArea.setPromptText(bundle.getString("escribe_aqui"));
        cargarBtn.setText(bundle.getString("cargar_archivo"));
        limpiarBtn.setText(bundle.getString("limpiar"));
        salidaLabel.setText(bundle.getString("texto_salida"));
        textoSalidaArea.setPromptText(bundle.getString("resultado_aqui"));
        guardarBtn.setText(bundle.getString("guardar_resultado"));
        copiarBtn.setText(bundle.getString("copiar"));

    }

    /**
     * Restablece los campos principales de la interfaz a su estado inicial:
     * limpia texto de entrada/salida, la clave y la ruta de imagen seleccionada.
     */
    private void resetCampos() {
        if (textoEntradaArea != null) textoEntradaArea.clear();
        if (textoSalidaArea != null) textoSalidaArea.clear();
        if (claveField != null) claveField.clear();
        rutaImagenSeleccionada = null;
        actualizarStatus("Listo");
    }

    /**
     * Abre una nueva ventana con el manual de usuario cargado desde
     * el recurso {@code manual-usuario.html}. Si no se encuentra, muestra un mensaje básico.
     */
    @FXML
    private void handleMostrarManual() {
        Stage stage = new Stage();
        stage.setTitle("Manual de Usuario - Descifrador");

        WebView webView = new WebView();
        URL url = getClass().getResource("/manual-usuario.html");
        if (url != null) {
            webView.getEngine().load(url.toExternalForm());
        } else {
            webView.getEngine().loadContent("<h1>Manual no disponible</h1>");
        }

        Scene scene = new Scene(webView, 800, 600);
        stage.setScene(scene);
        stage.show();
    }

}