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
    private static final String API_BASE_URL = "http://127.0.0.1:5000/api/vigenere/";
    private static final HttpClient client = HttpClient.newHttpClient();

    /**
     * Cifra un texto usando el algoritmo Vigenère a través de la API Python
     *
     * @param texto Texto a cifrar
     * @param clave Clave de cifrado
     * @return Texto cifrado
     * @throws Exception Si hay un error en la comunicación con la API
     */
    public static String cifrarVigenere(String texto, String clave) throws Exception {
        JSONObject json = new JSONObject();
        json.put("texto", texto);
        json.put("clave", clave);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + "cifrar"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json.toString()))
                .build();

        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            JSONObject errorJson = new JSONObject(response.body());
            throw new Exception(errorJson.getString("error"));
        }

        JSONObject responseJson = new JSONObject(response.body());
        return responseJson.getString("texto_cifrado");
    }

    /**
     * Descifra un texto usando el algoritmo Vigenère a través de la API Python
     *
     * @param textoCifrado Texto cifrado a descifrar
     * @param clave Clave de descifrado
     * @return Texto descifrado
     * @throws Exception Si hay un error en la comunicación con la API
     */
    public static String descifrarVigenere(String textoCifrado, String clave) throws Exception {
        JSONObject json = new JSONObject();
        json.put("texto", textoCifrado);
        json.put("clave", clave);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + "descifrar"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json.toString()))
                .build();

        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            JSONObject errorJson = new JSONObject(response.body());
            throw new Exception(errorJson.getString("error"));
        }

        JSONObject responseJson = new JSONObject(response.body());
        return responseJson.getString("texto_descifrado");
    }
}

