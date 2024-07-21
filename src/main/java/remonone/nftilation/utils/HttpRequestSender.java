package remonone.nftilation.utils;

import com.google.gson.Gson;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpRequestSender {
    
    public static <T, K> T post(String request, K data, Class<T> container) throws Exception {
        HttpURLConnection connection = EstablishConnection(request);
        Gson convertor = new Gson();
        connection.setRequestMethod("POST");

        String dataToSend = convertor.toJson(data);
        Logger.debug(dataToSend);
        DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
        outputStream.writeBytes(dataToSend);
        outputStream.flush();
        outputStream.close();
        InputStream stream = connection.getInputStream();
        String response = buildJsonString(stream);
        Logger.debug(response);
        if(connection.getResponseCode() > 299) {
            Logger.error("[" + connection.getResponseCode() + "] " + connection.getResponseMessage());
            throw new IllegalArgumentException(response);
        }
        return convertor.fromJson(response, container);
    }

    public static <T, K> T postWithAuthorization(String request, String auth, K data, Class<T> container) throws Exception {
        HttpURLConnection connection = EstablishConnection(request);
        Gson convertor = new Gson();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Authorization", "Bearer " + auth);

        String dataToSend = convertor.toJson(data);
        DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
        outputStream.writeBytes(dataToSend);
        outputStream.flush();
        outputStream.close();
        String raw = connection.getResponseMessage();
        if(connection.getResponseCode() > 299) {
            throw new IllegalArgumentException(raw);
        }
        return convertor.fromJson(raw, container);
    }
    
    public static <T> T get(String request, Class<T> container) throws Exception {
        HttpURLConnection connection = EstablishConnection(request);
        Gson convertor = new Gson();
        connection.setRequestMethod("GET");
        
        String raw = buildJsonString(connection.getInputStream());
        Logger.debug(raw);
        if(connection.getResponseCode() > 299) {
            throw new IllegalArgumentException(raw);
        }
        return convertor.fromJson(raw, container);
    }

    public static <T> T getWithAuthorization(String request, String auth, Class<T> container) throws Exception {
        HttpURLConnection connection = EstablishConnection(request);
        Gson convertor = new Gson();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Authorization", "Bearer " + auth);
        
        String raw = buildJsonString(connection.getInputStream());
        if(connection.getResponseCode() > 299) {
            throw new IllegalArgumentException(raw);
        }
        return convertor.fromJson(raw, container);
    }

    private static String buildJsonString(InputStream requestBody) throws IOException {
        InputStreamReader reader = new InputStreamReader(requestBody);
        BufferedReader br = new BufferedReader(reader);

        int b;
        StringBuilder buf = new StringBuilder(512);
        while((b = br.read()) != -1) {
            buf.append((char) b);
        }

        br.close();
        reader.close();
        return buf.toString();
    }
    
    private static HttpURLConnection EstablishConnection(String request) throws Exception {
        URL url = new URL(request);
        
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);
        connection.setDoOutput(true);
        return connection;
    }
}
