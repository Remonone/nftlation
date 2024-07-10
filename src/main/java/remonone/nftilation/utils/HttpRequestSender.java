package remonone.nftilation.utils;

import com.google.gson.Gson;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpRequestSender {
    
    public static <T, K> T post(String request, K data, Class<T> container) throws Exception {
        HttpURLConnection connection = EstablishConnection(request);
        Gson convertor = new Gson();
        connection.setRequestMethod("POST");

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
        
        String raw = connection.getResponseMessage();
        if(connection.getResponseCode() > 299) {
            throw new IllegalArgumentException(raw);
        }
        return convertor.fromJson(raw, container);
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
