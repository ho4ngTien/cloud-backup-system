package com.cloudsafe.cli;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.*;
import java.net.*;
import java.net.http.*;
import java.nio.file.*;
import java.time.Duration;
import java.util.Map;

/**
 * HTTP client wrapper that calls the CloudSafe REST API.
 * Reads/writes the JWT token from the local config file (~/.cloudsafe/config.json).
 */
public class ApiClient {

    private static final String CONFIG_DIR  = System.getProperty("user.home") + "/.cloudsafe";
    private static final String CONFIG_FILE = CONFIG_DIR + "/config.json";

    private final String baseUrl;
    private final HttpClient httpClient;
    private final ObjectMapper mapper;

    public ApiClient() {
        this.baseUrl = loadConfig().getOrDefault("serverUrl", "http://localhost:8080");
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.mapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    public ApiClient(String baseUrl) {
        this.baseUrl = baseUrl;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.mapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    // =================== Auth ===================

    public Map<String, Object> login(String email, String password) throws Exception {
        Map<String, String> body = Map.of("email", email, "password", password);
        Map<String, Object> response = post("/api/auth/login", body, null);
        saveToken((String) response.get("accessToken"), email);
        return response;
    }

    // =================== Backup ===================

    public Map<String, Object> startBackup(String deviceId, String backupType,
                                           String sourcePath) throws Exception {
        Map<String, String> body = Map.of(
                "deviceId", deviceId,
                "backupType", backupType,
                "sourcePath", sourcePath
        );
        return post("/api/backups/start", body, getToken());
    }

    public Map<String, Object> commitBackup(Object commitRequest) throws Exception {
        return post("/api/backups/commit", commitRequest, getToken());
    }

    public void failBackup(String jobId, String errorMessage) throws Exception {
        post("/api/backups/" + jobId + "/fail",
                Map.of("errorMessage", errorMessage), getToken());
    }

    // =================== Restore ===================

    public Map<String, Object> startRestore(Object restoreRequest) throws Exception {
        return post("/api/restore/start", restoreRequest, getToken());
    }

    public void completeRestore(String restoreId, boolean success,
                                String errorMsg) throws Exception {
        post("/api/restore/" + restoreId + "/complete",
                Map.of("success", success, "errorMessage",
                        errorMsg != null ? errorMsg : ""), getToken());
    }

    // =================== Query ===================

    public Map<String, Object> getStorage() throws Exception {
        return get("/api/storage/usage", getToken());
    }

    public Map<String, Object> getHistory() throws Exception {
        return get("/api/backups?page=0&size=20", getToken());
    }

    public Map<String, Object> getDevices() throws Exception {
        return get("/api/devices", getToken());
    }

    public Map<String, Object> getLatestCompletedFiles(String deviceId) throws Exception {
        return get("/api/backups/latest-completed-files?deviceId=" + deviceId, getToken());
    }

    // =================== HTTP helpers ===================

    @SuppressWarnings("unchecked")
    public Map<String, Object> post(String path, Object body, String token) throws Exception {
        String json = mapper.writeValueAsString(body);
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .timeout(Duration.ofSeconds(30))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json));
        if (token != null) builder.header("Authorization", "Bearer " + token);
        HttpResponse<String> resp = httpClient.send(builder.build(),
                HttpResponse.BodyHandlers.ofString());
        checkStatus(resp);
        return mapper.readValue(resp.body(), new TypeReference<>() {});
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> get(String path, String token) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .timeout(Duration.ofSeconds(30))
                .GET();
        if (token != null) builder.header("Authorization", "Bearer " + token);
        HttpResponse<String> resp = httpClient.send(builder.build(),
                HttpResponse.BodyHandlers.ofString());
        checkStatus(resp);
        return mapper.readValue(resp.body(), new TypeReference<>() {});
    }

    // =================== Config / Token ===================

    public String getToken() {
        return loadConfig().getOrDefault("accessToken", null);
    }

    public boolean isLoggedIn() {
        String token = getToken();
        return token != null && !token.isBlank();
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> loadConfig() {
        try {
            Path p = Path.of(CONFIG_FILE);
            if (Files.exists(p)) {
                return mapper.readValue(p.toFile(), Map.class);
            }
        } catch (Exception ignored) {}
        return new java.util.HashMap<>();
    }

    private void saveToken(String token, String email) throws IOException {
        Files.createDirectories(Path.of(CONFIG_DIR));
        Map<String, String> cfg = new java.util.HashMap<>(loadConfig());
        cfg.put("accessToken", token);
        cfg.put("email", email);
        cfg.put("serverUrl", baseUrl);
        mapper.writerWithDefaultPrettyPrinter()
              .writeValue(new File(CONFIG_FILE), cfg);
    }

    private void checkStatus(HttpResponse<String> resp) throws IOException {
        if (resp.statusCode() >= 400) {
            throw new IOException("API error " + resp.statusCode() + ": " + resp.body());
        }
    }

    public ObjectMapper getMapper() { return mapper; }
}
