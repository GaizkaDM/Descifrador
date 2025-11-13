package com.gaizkaFrost;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.json.JSONObject;

/**
 * <h2>Cliente HTTP para la API Vigenère</h2>
 *
 * <p>
 * Esta clase proporciona métodos estáticos para consumir la API REST encargada de
 * cifrar y descifrar texto usando el algoritmo Vigenère. La comunicación se realiza
 * mediante peticiones HTTP POST enviadas al backend Python, que responde en formato JSON.
 * </p>
 *
 * <p>
 * Cada método construye un cuerpo JSON con el texto y la clave proporcionados por
 * el usuario, envía la petición a los endpoints correspondientes y procesa la respuesta
 * devolviendo únicamente el texto cifrado o descifrado.
 * </p>
 *
 * @see <a href="http://localhost:5000/api/vigenere/">API Vigenère Backend</a>
 * @author Gaizka
 * @author Diego
 * @version 1.0
 * @since 2025
 */
public class APIClient {

    /**
     * URL base del backend que ofrece los endpoints de cifrado/descifrado Vigenère.
     */
    private static final String API_BASE_URL = "http://localhost:5000/api/vigenere/";

    /**
     * Cliente HTTP reutilizable proporcionado por la API estándar de Java.
     */
    private static final HttpClient client = HttpClient.newHttpClient();

    /**
     * <h3>Método de cifrado Vigenère</h3>
     *
     * Envía una petición HTTP POST al endpoint <b>/cifrar</b> para obtener
     * el texto cifrado utilizando el algoritmo Vigenère.
     *
     * <p>El proceso consiste en:</p>
     * <ul>
     *     <li>Crear un objeto JSON con el texto y la clave.</li>
     *     <li>Enviar la petición POST al backend.</li>
     *     <li>Validar el código de estado HTTP recibido.</li>
     *     <li>Devolver el campo {@code "texto_cifrado"} del JSON de respuesta.</li>
     * </ul>
     *
     * @param texto Texto plano que se desea cifrar.
     * @param clave Clave Vigenère utilizada para el cifrado.
     * @return El texto cifrado devuelto por la API.
     * @throws Exception Si ocurre un error de red o la API devuelve un mensaje de error.
     */
    public static String cifrarVigenere(String texto, String clave) throws Exception {

        JSONObject json = new JSONObject();
        json.put("texto", texto);
        json.put("clave", clave);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + "cifrar"))
                .POST(HttpRequest.BodyPublishers.ofString(json.toString()))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        if (response.statusCode() != 200) {
            JSONObject errorJson = new JSONObject(response.body());
            throw new Exception(errorJson.getString("error"));
        }

        JSONObject responseJson = new JSONObject(response.body());
        return responseJson.getString("texto_cifrado");
    }

    /**
     * <h3>Método de descifrado Vigenère</h3>
     *
     * Envía una petición HTTP POST al endpoint <b>/descifrar</b> para obtener
     * el texto plano correspondiente a un mensaje previamente cifrado.
     *
     * <p>Proceso:</p>
     * <ul>
     *     <li>Construcción del JSON con el texto cifrado y la clave.</li>
     *     <li>Envío de la petición POST.</li>
     *     <li>Control de errores basado en el código HTTP.</li>
     *     <li>Extracción del valor {@code "texto_descifrado"}.</li>
     * </ul>
     *
     * @param texto Texto cifrado que se desea descifrar.
     * @param clave Clave Vigenère usada originalmente para cifrar.
     * @return El texto descifrado devuelto por la API.
     * @throws Exception Si el backend devuelve un mensaje de error o falla la conexión.
     */
    public static String descifrarVigenere(String texto, String clave) throws Exception {

        JSONObject json = new JSONObject();
        json.put("texto", texto);
        json.put("clave", clave);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + "descifrar"))
                .POST(HttpRequest.BodyPublishers.ofString(json.toString()))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        if (response.statusCode() != 200) {
            JSONObject errorJson = new JSONObject(response.body());
            throw new Exception(errorJson.getString("error"));
        }

        JSONObject responseJson = new JSONObject(response.body());
        return responseJson.getString("texto_descifrado");
    }
}