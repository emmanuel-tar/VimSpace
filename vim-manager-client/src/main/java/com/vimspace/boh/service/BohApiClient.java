package com.vimspace.boh.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vimspace.boh.config.AppConfig;
import com.vimspace.contract.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

public class BohApiClient {

    private final HttpClient client;
    private final ObjectMapper mapper;
    private final AppConfig config;

    public BohApiClient(AppConfig config) {
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        this.mapper = new ObjectMapper();
        this.config = config;
    }

    private HttpRequest.Builder requestBuilder(String method, String path, String jsonBody) {
        URI uri = URI.create(config.getBaseUrl() + path);
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(uri)
                .timeout(Duration.ofSeconds(10))
                .header("Accept", "application/json");
        if (config.getAuthToken() != null && !config.getAuthToken().isBlank()) {
            builder.header("Authorization", "Bearer " + config.getAuthToken());
        }
        if (jsonBody != null) {
            builder.header("Content-Type", "application/json")
                    .method(method, HttpRequest.BodyPublishers.ofString(jsonBody));
        } else {
            builder.method(method, HttpRequest.BodyPublishers.noBody());
        }
        return builder;
    }

    /**
     * Sends the request, translating the checked {@link IOException} into the
     * unchecked {@link ApiException} so callers only have to handle one type.
     */
    private HttpResponse<String> send(HttpRequest request) {
        try {
            return client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException("Request interrupted: " + e.getMessage(), e, null);
        } catch (IOException e) {
            throw new ApiException("Network error contacting " + config.getBaseUrl() + ": " + e.getMessage(), e, null);
        }
    }

    /** Serialises a request body, wrapping any Jackson failure. */
    private String writeJson(Object value) {
        try {
            return mapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new ApiException("Could not serialise request body: " + e.getMessage(), e, null);
        }
    }

    /** Deserialises a response body into a concrete type. */
    private <T> T readJson(String body, Class<T> type) {
        try {
            return mapper.readValue(body, type);
        } catch (JsonProcessingException e) {
            throw new ApiException("Malformed response from server: " + e.getMessage(), e, null);
        }
    }

    /** Deserialises a response body into a generic type. */
    private <T> T readJson(String body, TypeReference<T> type) {
        try {
            return mapper.readValue(body, type);
        } catch (JsonProcessingException e) {
            throw new ApiException("Malformed response from server: " + e.getMessage(), e, null);
        }
    }

    /** URL-encodes a query parameter. */
    private String encode(String value) {
        try {
            return java.net.URLEncoder.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new ApiException("Could not encode parameter: " + e.getMessage(), e, null);
        }
    }

    private void checkStatus(HttpResponse<String> resp) throws ApiException {
        if (resp.statusCode() < 200 || resp.statusCode() >= 300) {
            throw new ApiException(buildErrorMessage(resp), resp.statusCode());
        }
    }

    /**
     * Builds a human readable message from an error response. The backend
     * returns a structured {@link ErrorResponseDTO} for 4xx/5xx responses, so
     * the server-side message is preferred over the raw JSON body.
     */
    private String buildErrorMessage(HttpResponse<String> resp) {
        String body = resp.body() == null ? "" : resp.body().trim();
        if (!body.isEmpty() && body.startsWith("{")) {
            try {
                ErrorResponseDTO err = mapper.readValue(body, ErrorResponseDTO.class);
                if (err.getMessage() != null && !err.getMessage().isBlank()) {
                    return err.getMessage();
                }
                if (err.getError() != null && !err.getError().isBlank()) {
                    return err.getError();
                }
            } catch (Exception ignored) {
                // Fall through to the raw body below.
            }
        }
        return "HTTP " + resp.statusCode() + ": " + body;
    }

    public List<ProductDTO> getProducts(boolean activeOnly) throws ApiException {
        HttpRequest req = requestBuilder("GET", "/products?activeOnly=" + activeOnly, null).build();
        HttpResponse<String> resp = send(req);
        checkStatus(resp);
        return readJson(resp.body(), new TypeReference<List<ProductDTO>>() {});
    }

    public List<String> getCategories() throws ApiException {
        HttpRequest req = requestBuilder("GET", "/products/categories", null).build();
        HttpResponse<String> resp = send(req);
        checkStatus(resp);
        return readJson(resp.body(), new TypeReference<List<String>>() {});
    }

    public List<ProductDTO> searchProducts(String query) throws ApiException {
        String encoded = encode(query);
        HttpRequest req = requestBuilder("GET", "/products/search?q=" + encoded, null).build();
        HttpResponse<String> resp = send(req);
        checkStatus(resp);
        return readJson(resp.body(), new TypeReference<List<ProductDTO>>() {});
    }

    public ProductDTO saveProduct(CreateProductRequest request) throws ApiException {
        String json = writeJson(request);
        HttpRequest req = requestBuilder("POST", "/products", json).build();
        HttpResponse<String> resp = send(req);
        checkStatus(resp);
        return readJson(resp.body(), ProductDTO.class);
    }

    public ProductDTO updateProduct(Long id, CreateProductRequest request) throws ApiException {
        String json = writeJson(request);
        HttpRequest req = requestBuilder("PUT", "/products/" + id, json).build();
        HttpResponse<String> resp = send(req);
        checkStatus(resp);
        return readJson(resp.body(), ProductDTO.class);
    }

    public void deleteProduct(Long id) throws ApiException {
        HttpRequest req = requestBuilder("DELETE", "/products/" + id, null).build();
        HttpResponse<String> resp = send(req);
        checkStatus(resp);
    }

    public BarcodeGenerateResponse generateBarcode(String format, String prefix) throws ApiException {
        BarcodeGenerateRequest reqObj = new BarcodeGenerateRequest(format, prefix);
        String json = writeJson(reqObj);
        HttpRequest req = requestBuilder("POST", "/products/generate-barcode", json).build();
        HttpResponse<String> resp = send(req);
        checkStatus(resp);
        return readJson(resp.body(), BarcodeGenerateResponse.class);
    }

    public ProductDTO adjustStock(StockAdjustmentRequest request) throws ApiException {
        String json = writeJson(request);
        HttpRequest req = requestBuilder("POST", "/products/adjust-stock", json).build();
        HttpResponse<String> resp = send(req);
        checkStatus(resp);
        return readJson(resp.body(), ProductDTO.class);
    }

    public List<InventoryMovementDTO> getStockMovements() throws ApiException {
        HttpRequest req = requestBuilder("GET", "/products/movements", null).build();
        HttpResponse<String> resp = send(req);
        checkStatus(resp);
        return readJson(resp.body(), new TypeReference<List<InventoryMovementDTO>>() {});
    }

    public List<SaleDetailDTO> getAllSales() throws ApiException {
        HttpRequest req = requestBuilder("GET", "/sales", null).build();
        HttpResponse<String> resp = send(req);
        checkStatus(resp);
        return readJson(resp.body(), new TypeReference<List<SaleDetailDTO>>() {});
    }

    // === SUPPLIER METHODS ===

    public List<SupplierDTO> getSuppliers() throws ApiException {
        HttpRequest req = requestBuilder("GET", "/suppliers", null).build();
        HttpResponse<String> resp = send(req);
        checkStatus(resp);
        return readJson(resp.body(), new TypeReference<List<SupplierDTO>>() {});
    }

    public List<SupplierDTO> searchSuppliers(String q) throws ApiException {
        String encoded = encode(q);
        HttpRequest req = requestBuilder("GET", "/suppliers?q=" + encoded, null).build();
        HttpResponse<String> resp = send(req);
        checkStatus(resp);
        return readJson(resp.body(), new TypeReference<List<SupplierDTO>>() {});
    }

    public SupplierDTO getSupplierById(Long id) throws ApiException {
        HttpRequest req = requestBuilder("GET", "/suppliers/" + id, null).build();
        HttpResponse<String> resp = send(req);
        checkStatus(resp);
        return readJson(resp.body(), SupplierDTO.class);
    }

    public SupplierDTO createSupplier(CreateSupplierRequest req) throws ApiException {
        String json = writeJson(req);
        HttpRequest req2 = requestBuilder("POST", "/suppliers", json).build();
        HttpResponse<String> resp = send(req2);
        checkStatus(resp);
        return readJson(resp.body(), SupplierDTO.class);
    }

    public SupplierDTO updateSupplier(Long id, CreateSupplierRequest req) throws ApiException {
        String json = writeJson(req);
        HttpRequest req2 = requestBuilder("PUT", "/suppliers/" + id, json).build();
        HttpResponse<String> resp = send(req2);
        checkStatus(resp);
        return readJson(resp.body(), SupplierDTO.class);
    }

    public void deleteSupplier(Long id) throws ApiException {
        HttpRequest req = requestBuilder("DELETE", "/suppliers/" + id, null).build();
        HttpResponse<String> resp = send(req);
        checkStatus(resp);
    }

    public List<SupplierDTO> getActiveSuppliers() throws ApiException {
        HttpRequest req = requestBuilder("GET", "/suppliers/active", null).build();
        HttpResponse<String> resp = send(req);
        checkStatus(resp);
        return readJson(resp.body(), new TypeReference<List<SupplierDTO>>() {});
    }

    public List<PurchaseOrderDTO> getPurchaseOrders(String status) throws ApiException {
        HttpRequest req = requestBuilder("GET", "/purchase-orders?status=" + status, null).build();
        HttpResponse<String> resp = send(req);
        checkStatus(resp);
        return readJson(resp.body(), new TypeReference<List<PurchaseOrderDTO>>() {});
    }

    public List<PurchaseOrderDTO> getPurchaseOrdersBySupplier(Long supplierId) throws ApiException {
        HttpRequest req = requestBuilder("GET", "/purchase-orders/supplier/" + supplierId, null).build();
        HttpResponse<String> resp = send(req);
        checkStatus(resp);
        return readJson(resp.body(), new TypeReference<List<PurchaseOrderDTO>>() {});
    }

    public PurchaseOrderDTO getPurchaseOrder(String poNumber) throws ApiException {
        HttpRequest req = requestBuilder("GET", "/purchase-orders/po-number/" + poNumber, null).build();
        HttpResponse<String> resp = send(req);
        checkStatus(resp);
        return readJson(resp.body(), PurchaseOrderDTO.class);
    }

    public List<PurchaseOrderDTO> getOpenPurchaseOrders() throws ApiException {
        HttpRequest req = requestBuilder("GET", "/purchase-orders/open", null).build();
        HttpResponse<String> resp = send(req);
        checkStatus(resp);
        return readJson(resp.body(), new TypeReference<List<PurchaseOrderDTO>>() {});
    }

    public PurchaseOrderDTO createPurchaseOrder(CreatePurchaseOrderRequest req) throws ApiException {
        String json = writeJson(req);
        HttpRequest req2 = requestBuilder("POST", "/purchase-orders", json).build();
        HttpResponse<String> resp = send(req2);
        checkStatus(resp);
        return readJson(resp.body(), PurchaseOrderDTO.class);
    }

    public PurchaseOrderDTO receiveGoods(RequestPurchaseOrderRequest req) throws ApiException {
        HttpRequest req2 = requestBuilder("PUT", "/purchase-orders/" + req.getPoId() + "/receive", writeJson(req)).build();
        HttpResponse<String> resp = send(req2);
        checkStatus(resp);
        return readJson(resp.body(), PurchaseOrderDTO.class);
    }

    public PurchaseOrderDTO cancelPurchaseOrder(Long poId) throws ApiException {
        HttpRequest req = requestBuilder("PUT", "/purchase-orders/" + poId + "/cancel", null).build();
        HttpResponse<String> resp = send(req);
        checkStatus(resp);
        return readJson(resp.body(), PurchaseOrderDTO.class);
    }

    public DailySalesSummaryDTO getDailySummary() throws ApiException {
        HttpRequest req = requestBuilder("GET", "/sales/daily-summary", null).build();
        HttpResponse<String> resp = send(req);
        checkStatus(resp);
        return readJson(resp.body(), DailySalesSummaryDTO.class);
    }

    public List<UserDTO> getAllUsers() throws ApiException {
        HttpRequest req = requestBuilder("GET", "/auth/users", null).build();
        HttpResponse<String> resp = send(req);
        checkStatus(resp);
        return readJson(resp.body(), new TypeReference<List<UserDTO>>() {});
    }

    public UserDTO createUser(CreateUserRequest request) throws ApiException {
        String json = writeJson(request);
        HttpRequest req = requestBuilder("POST", "/auth/users", json).build();
        HttpResponse<String> resp = send(req);
        checkStatus(resp);
        return readJson(resp.body(), UserDTO.class);
    }

    public UserDTO toggleUserActive(Long userId) throws ApiException {
        HttpRequest req = requestBuilder("POST", "/auth/users/" + userId + "/toggle", null).build();
        HttpResponse<String> resp = send(req);
        checkStatus(resp);
        return readJson(resp.body(), UserDTO.class);
    }

    // ------------------------------------------------------- till sessions

    /** Opens a new till session (cash shift) for a terminal. */
    public TillSessionDTO openTillSession(OpenTillSessionRequest request) throws ApiException {
        String json = writeJson(request);
        HttpRequest req = requestBuilder("POST", "/till-sessions", json).build();
        HttpResponse<String> resp = send(req);
        checkStatus(resp);
        return readJson(resp.body(), TillSessionDTO.class);
    }

    /** All currently open till sessions. */
    public List<TillSessionDTO> getActiveTillSessions() throws ApiException {
        HttpRequest req = requestBuilder("GET", "/till-sessions/active", null).build();
        HttpResponse<String> resp = send(req);
        checkStatus(resp);
        return readJson(resp.body(), new TypeReference<List<TillSessionDTO>>() {});
    }

    /** The open session for a terminal, or {@code null} when there is none. */
    public TillSessionDTO getActiveTillSession(String terminalId) throws ApiException {
        String encoded = encode(terminalId);
        HttpRequest req = requestBuilder("GET", "/till-sessions/active/" + encoded, null).build();
        HttpResponse<String> resp = send(req);
        if (resp.statusCode() == 204 || resp.statusCode() == 404) {
            return null;
        }
        checkStatus(resp);
        if (resp.body() == null || resp.body().isBlank()) {
            return null;
        }
        return readJson(resp.body(), TillSessionDTO.class);
    }

    /** Till session history, optionally filtered by status (OPEN / CLOSED). */
    public List<TillSessionDTO> getTillSessionHistory(String status) throws ApiException {
        String path = "/till-sessions";
        if (status != null && !status.isBlank()) {
            path += "?status=" + encode(status.trim());
        }
        HttpRequest req = requestBuilder("GET", path, null).build();
        HttpResponse<String> resp = send(req);
        checkStatus(resp);
        return readJson(resp.body(), new TypeReference<List<TillSessionDTO>>() {});
    }

    /** A single till session by id. */
    public TillSessionDTO getTillSession(Long sessionId) throws ApiException {
        HttpRequest req = requestBuilder("GET", "/till-sessions/" + sessionId, null).build();
        HttpResponse<String> resp = send(req);
        checkStatus(resp);
        return readJson(resp.body(), TillSessionDTO.class);
    }

    /** Closes a session with the counted cash and returns the reconciliation. */
    public CashReconciliationDTO closeTillSession(CloseTillSessionRequest request) throws ApiException {
        String json = writeJson(request);
        HttpRequest req = requestBuilder("POST", "/till-sessions/close", json).build();
        HttpResponse<String> resp = send(req);
        checkStatus(resp);
        return readJson(resp.body(), CashReconciliationDTO.class);
    }

    /** Cash reconciliation for a session (works for open and closed sessions). */
    public CashReconciliationDTO getCashReconciliation(Long sessionId) throws ApiException {
        HttpRequest req = requestBuilder("GET", "/till-sessions/" + sessionId + "/reconciliation", null).build();
        HttpResponse<String> resp = send(req);
        checkStatus(resp);
        return readJson(resp.body(), CashReconciliationDTO.class);
    }

    public LoginResponse login(LoginRequest request) throws ApiException {
        String json = writeJson(request);
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(config.getBaseUrl() + "/auth/login"))
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        HttpResponse<String> resp = send(req);
        checkStatus(resp);
        return readJson(resp.body(), LoginResponse.class);
    }
}
