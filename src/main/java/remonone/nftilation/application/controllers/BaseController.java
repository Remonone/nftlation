package remonone.nftilation.application.controllers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import lombok.Builder;
import remonone.nftilation.utils.HttpRequestMethod;
import remonone.nftilation.utils.Logger;
import remonone.nftilation.utils.ResponseBody;
import remonone.nftilation.utils.annotations.BodyContent;
import remonone.nftilation.utils.annotations.EndPointListener;
import remonone.nftilation.utils.annotations.NftilationException;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Objects;

public abstract class BaseController {

    public static void StartContext(HttpServer server, Class<? extends BaseController> controller) throws InstantiationException, IllegalAccessException {
        Object obj = controller.newInstance();
        for(Method method : controller.getDeclaredMethods()) {
            if(method.isAnnotationPresent(EndPointListener.class)) {
                method.setAccessible(true);
                EndPointListener annotation = method.getAnnotation(EndPointListener.class);
                Class<?> bodyData = null;
                HttpRequestMethod requestMethod = annotation.method();
                if (Objects.requireNonNull(requestMethod) == HttpRequestMethod.POST) {
                    Parameter[] params = method.getParameters();
                    for(Parameter param : params) {
                        if(param.getAnnotation(BodyContent.class) != null) {
                            if(bodyData == null) {
                                Logger.debug("Inserting body data container: " + param.getType().getName() + " for: " + method.getName());
                                bodyData = param.getType();
                            } else {
                                throw new IllegalStateException("Creating 2 body for a single request is not allowed!");
                            }
                        }
                    }
                }
                ControllerHandler handler = ControllerHandler.builder()
                        .body(bodyData)
                        .requestMethod(requestMethod)
                        .controller(obj)
                        .path(annotation.path())
                        .method(method)
                        .build();
                server.createContext(annotation.path(), handler);
            }
        }
    }

    private static void sendAnswer(Gson gson, HttpExchange exchange, Object raw) throws IOException {
        ResponseBody<?> responseBody = (ResponseBody<?>) raw;
        Object response;
        if(responseBody.getData() == null) {
            if(responseBody.getErrorMessage() == null) {
                exchange.sendResponseHeaders(500, 0);
                return;
            }
            response = responseBody.getErrorMessage();
        } else {
            response = responseBody.getData();
        }

        String json = gson.toJson(response);
        exchange.sendResponseHeaders(responseBody.getResponseCode(), json.length());
        OutputStream stream = exchange.getResponseBody();
        stream.write(json.getBytes());
        stream.flush();
        stream.close();
    }

    private static void verifyResponse(Object raw) {
        if(!(raw instanceof ResponseBody)) {
            throw new IllegalStateException("Response have invalid return type!");
        }
    }

    @Builder
    private static class ControllerHandler implements HttpHandler {

        private static Gson gson = new Gson();
        private Class<?> body;
        private HttpRequestMethod requestMethod;
        private String path;
        private Method method;
        private Object controller;

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Logger.log("Received an request for: " + path);
            if(!requestMethod.toString().equals(exchange.getRequestMethod())) {
                Logger.debug("Incorrect request method: " + requestMethod);
                exchange.sendResponseHeaders(405, -1);
                exchange.close();
                return;
            }
            if(this.body != null) {
                String requestBody = buildJsonString(exchange.getRequestBody());
                Logger.log("Handling post data: " + requestBody);
                Object body = gson.fromJson(requestBody, this.body);
                Logger.debug("Request body: " + body.toString());
                method.setAccessible(true);
                try {
                    Logger.debug("Invoking method...");
                    Object raw = method.invoke(controller, body);
                    Logger.debug("Received answer: " + raw.toString());
                    verifyResponse(raw);
                    sendAnswer(gson, exchange, raw);
                } catch (IllegalAccessException e) {
                    HandleIllegalAccessException(e, exchange);
                    throw new RuntimeException(e);
                }
                catch(InvocationTargetException e) {
                    String errorMessage = e.getMessage();
                    if(e.getTargetException().getClass().getAnnotation(NftilationException.class) != null) {
                        exchange.sendResponseHeaders(400, errorMessage.length());
                    } else {
                        exchange.sendResponseHeaders(500, errorMessage.length());
                    }
                    OutputStream stream = exchange.getResponseBody();
                    stream.write(errorMessage.getBytes());
                    stream.flush();
                    stream.close();
                }
            } else {
                Logger.debug("Body request is null...");
                try {
                    Object raw = method.invoke(controller);
                    verifyResponse(raw);
                    sendAnswer(gson, exchange, raw);
                } catch (IllegalAccessException e) {
                    HandleIllegalAccessException(e, exchange);
                    throw new RuntimeException(e);
                }
                catch(InvocationTargetException e) {
                    String errorMessage = e.getMessage();
                    if(e.getTargetException().getClass().getAnnotation(NftilationException.class) != null) {
                        exchange.sendResponseHeaders(400, 0);
                    } else {
                        exchange.sendResponseHeaders(500, 0);
                    }
                    OutputStream stream = exchange.getResponseBody();
                    stream.write(gson.toJson(errorMessage).getBytes());
                    stream.flush();
                }
            }
            exchange.close();
        }

        private void HandleIllegalAccessException(IllegalAccessException e, HttpExchange exchange) {
            String message = "{\"message\": \"Something went wrong during handling an request\"}";
            try {
                exchange.sendResponseHeaders(500, message.length());
                exchange.getResponseBody().write(message.getBytes());
                exchange.getResponseBody().flush();
                exchange.getResponseBody().close();
                exchange.close();
            } catch (IOException ex) {
                Logger.error("Connection was interrupted. Aborting...");
            }
        }

        private String buildJsonString(InputStream requestBody) throws IOException {
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
    }
}
