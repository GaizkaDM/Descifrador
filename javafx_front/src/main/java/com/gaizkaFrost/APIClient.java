package com.gaizkaFrost;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.json.JSONObject;

/**
 * Cliente HTTP para consumir la API REST de cifrado Vigenère
 */
public class APIClient {

    private static final String API_BASE_URL = "http://172.20.106.20:5000/api/vigenere/";
    private static final HttpClient client = HttpClient.newHttpClient();

    /**
     * Cifra usando Vigenère mediante API REST
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
     * Descifra usando Vigenère mediante API REST
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

