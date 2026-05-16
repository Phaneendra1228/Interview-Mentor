package com.interviewmentor.service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.awt.Desktop;
import java.io.*;
import java.net.*;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

public class OAuthService {

    // ====================================================================================
    // IMPORTANT: TO MAKE THIS WORK, YOU MUST REGISTER YOUR APP IN GOOGLE & GITHUB!
    // 1. Google Cloud Console -> Create Credentials -> OAuth Client ID (Desktop
    // App)
    // 2. GitHub Developer Settings -> OAuth Apps -> New OAuth App
    // Use "http://localhost:8080/callback" as the Redirect URI for both.
    // ====================================================================================
    private static final String GOOGLE_CLIENT_ID = "YOUR_GOOGLE_CLIENT_ID";
    private static final String GOOGLE_CLIENT_SECRET = "YOUR_GOOGLE_CLIENT_SECRET";

    private static final String GITHUB_CLIENT_ID = "YOUR_GITHUB_CLIENT_ID";
    private static final String GITHUB_CLIENT_SECRET = "YOUR_GITHUB_CLIENT_SECRET";

    private static final int PORT = 8080;
    private static final String REDIRECT_URI = "http://localhost:8080/callback";

    private HttpServer server;
    private String currentProvider;
    private OAuthCallback callbackListener;

    public interface OAuthCallback {
        void onSuccess(String email, String name);

        void onError(String error);
    }

    public void startOAuthFlow(String provider, OAuthCallback callback) {
        this.currentProvider = provider;
        this.callbackListener = callback;

        try {
            if (server != null) {
                server.stop(0);
            }
            server = HttpServer.create(new InetSocketAddress(PORT), 0);
            server.createContext("/callback", new CallbackHandler());
            server.setExecutor(null);
            server.start();

            String authUrl = "";
            if (provider.equals("Google")) {
                authUrl = "https://accounts.google.com/o/oauth2/v2/auth?" +
                        "client_id=" + GOOGLE_CLIENT_ID +
                        "&redirect_uri=" + REDIRECT_URI +
                        "&response_type=code" +
                        "&scope=email%20profile";
            } else if (provider.equals("GitHub")) {
                authUrl = "https://github.com/login/oauth/authorize?" +
                        "client_id=" + GITHUB_CLIENT_ID +
                        "&redirect_uri=" + REDIRECT_URI +
                        "&scope=user:email";
            }

            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(authUrl));
            } else {
                callbackListener.onError("Desktop browser not supported.");
            }

        } catch (Exception e) {
            callbackListener.onError("Failed to start OAuth flow: " + e.getMessage());
        }
    }

    private class CallbackHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String query = exchange.getRequestURI().getQuery();
            String responseText = "<html><body><h2>Authentication successful! You can close this tab and return to InterviewMentor.</h2></body></html>";

            exchange.sendResponseHeaders(200, responseText.length());
            OutputStream os = exchange.getResponseBody();
            os.write(responseText.getBytes());
            os.close();

            // Stop server after getting the callback
            new Thread(() -> server.stop(1)).start();

            if (query != null && query.contains("code=")) {
                String code = query.split("code=")[1].split("&")[0];
                try {
                    processAuthorizationCode(code);
                } catch (Exception e) {
                    callbackListener.onError("Failed to fetch user data: " + e.getMessage());
                }
            } else {
                callbackListener.onError("Authorization denied by user.");
            }
        }
    }

    private void processAuthorizationCode(String code) throws Exception {
        if (currentProvider.equals("Google")) {
            // Check if placeholders are still present
            if (GOOGLE_CLIENT_ID.startsWith("YOUR_")) {
                callbackListener.onError("Please enter your actual Google Client ID in OAuthService.java");
                return;
            }
            // 1. Exchange Code for Access Token
            String tokenUrl = "https://oauth2.googleapis.com/token";
            String params = "client_id=" + GOOGLE_CLIENT_ID +
                    "&client_secret=" + GOOGLE_CLIENT_SECRET +
                    "&code=" + code +
                    "&redirect_uri=" + REDIRECT_URI +
                    "&grant_type=authorization_code";

            String tokenResponse = sendPostRequest(tokenUrl, params);
            JsonObject json = JsonParser.parseString(tokenResponse).getAsJsonObject();
            String accessToken = json.get("access_token").getAsString();

            // 2. Fetch User Profile
            String profileUrl = "https://www.googleapis.com/oauth2/v2/userinfo";
            String profileResponse = sendGetRequest(profileUrl, accessToken);
            JsonObject profileJson = JsonParser.parseString(profileResponse).getAsJsonObject();

            String email = profileJson.get("email").getAsString();
            String name = profileJson.has("name") ? profileJson.get("name").getAsString() : "GoogleUser";

            callbackListener.onSuccess(email, name);

        } else if (currentProvider.equals("GitHub")) {
            if (GITHUB_CLIENT_ID.startsWith("YOUR_")) {
                callbackListener.onError("Please enter your actual GitHub Client ID in OAuthService.java");
                return;
            }
            // 1. Exchange Code for Access Token
            String tokenUrl = "https://github.com/login/oauth/access_token";
            String params = "client_id=" + GITHUB_CLIENT_ID +
                    "&client_secret=" + GITHUB_CLIENT_SECRET +
                    "&code=" + code +
                    "&redirect_uri=" + REDIRECT_URI;

            String tokenResponse = sendPostRequest(tokenUrl, params);
            
            // Because sendPostRequest uses Accept: application/json, GitHub returns JSON.
            JsonObject json = JsonParser.parseString(tokenResponse).getAsJsonObject();
            
            if (json.has("error")) {
                throw new Exception("GitHub OAuth Error: " + json.get("error_description").getAsString());
            }
            
            String accessToken = json.get("access_token").getAsString();

            // 2. Fetch User Profile
            String profileUrl = "https://api.github.com/user";
            String profileResponse = sendGetRequest(profileUrl, accessToken);
            JsonObject profileJson = JsonParser.parseString(profileResponse).getAsJsonObject();

            String login = profileJson.get("login").getAsString();
            String email = login + "@users.noreply.github.com"; // GitHub email might be private

            // Try fetching public email if possible
            if (profileJson.has("email") && !profileJson.get("email").isJsonNull()) {
                email = profileJson.get("email").getAsString();
            }

            callbackListener.onSuccess(email, login);
        }
    }

    private String sendPostRequest(String urlString, String urlParameters) throws Exception {
        URL url = URI.create(urlString).toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty("Accept", "application/json");
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = urlParameters.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        return readResponse(conn);
    }

    private String sendGetRequest(String urlString, String token) throws Exception {
        URL url = URI.create(urlString).toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Authorization", "Bearer " + token);
        conn.setRequestProperty("Accept", "application/json");

        return readResponse(conn);
    }

    private String readResponse(HttpURLConnection conn) throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(
                conn.getResponseCode() >= 400 ? conn.getErrorStream() : conn.getInputStream(), "utf-8"));
        StringBuilder response = new StringBuilder();
        String responseLine;
        while ((responseLine = br.readLine()) != null) {
            response.append(responseLine.trim());
        }
        return response.toString();
    }
}
