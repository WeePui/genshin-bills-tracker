package com.genshin.tracker.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.genshin.tracker.dto.ScanResultDTO;
import com.genshin.tracker.model.AppSettings;
import com.genshin.tracker.model.ItemCategory;
import com.genshin.tracker.model.PlatformStore;
import com.genshin.tracker.repository.AppSettingsRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class VisionScanService {

    private final AppSettingsRepository appSettingsRepository;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public VisionScanService(AppSettingsRepository appSettingsRepository, ObjectMapper objectMapper) {
        this.appSettingsRepository = appSettingsRepository;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .build();
    }

    public boolean isConfigured() {
        AppSettings settings = appSettingsRepository.findById(1L).orElse(null);
        return settings != null && settings.getGeminiApiKey() != null && !settings.getGeminiApiKey().isBlank();
    }

    public ScanResultDTO scanWithGemini(MultipartFile file) {
        ScanResultDTO result = new ScanResultDTO();
        AppSettings settings = appSettingsRepository.findById(1L).orElse(null);
        if (settings == null || settings.getGeminiApiKey() == null || settings.getGeminiApiKey().isBlank()) {
            result.setSuccess(false);
            result.setMessage("Gemini API key is not configured in Settings. Please set your API key to use AI Vision.");
            return result;
        }

        try {
            byte[] fileBytes = file.getBytes();
            String mimeType = file.getContentType() != null ? file.getContentType() : "image/jpeg";
            String base64Data = Base64.getEncoder().encodeToString(fileBytes);

            String prompt = "You are an expert invoice parser for Genshin Impact / HoYoverse receipts.\n" +
                    "Analyze this receipt image and extract these exact fields as JSON:\n" +
                    "{\n" +
                    "  \"orderId\": \"Order number or transaction ID (e.g. GPA.xxxx-xxxx-xxxx-xxxxx, Apple Order ID, PayPal transaction ID)\",\n" +
                    "  \"purchaseDate\": \"YYYY-MM-DDTHH:mm:ss or YYYY-MM-DD\",\n" +
                    "  \"itemCategory\": \"One of: WELKIN_MOON, BATTLE_PASS_HYMN, BATTLE_PASS_CHORUS, BATTLE_PASS_UPGRADE, GENESIS_60, GENESIS_300, GENESIS_980, GENESIS_1980, GENESIS_3280, GENESIS_6480, CHARACTER_OUTFIT, OTHER\",\n" +
                    "  \"customItemName\": \"Item title as printed on invoice\",\n" +
                    "  \"platform\": \"One of: GOOGLE_PLAY, APPLE_APP_STORE, PC_WORLDPAY, CODASHOP, RAZER_GOLD, EPIC_GAMES, PLAYSTATION_NETWORK, OTHER\",\n" +
                    "  \"paymentMethod\": \"e.g. Visa, Mastercard, PayPal, MoMo, Apple Pay, Google Pay\",\n" +
                    "  \"amount\": \"Numerical value as number or decimal (e.g. 4.99 or 109000)\",\n" +
                    "  \"currency\": \"3-letter currency code (e.g. USD, VND, EUR, JPY, GBP)\"\n" +
                    "}\n" +
                    "Return ONLY raw JSON, without markdown formatting or backticks.";

            Map<String, Object> inlineData = Map.of(
                    "mime_type", mimeType,
                    "data", base64Data
            );
            Map<String, Object> part1 = Map.of("text", prompt);
            Map<String, Object> part2 = Map.of("inline_data", inlineData);

            Map<String, Object> content = Map.of("parts", List.of(part1, part2));
            Map<String, Object> requestBodyMap = Map.of("contents", List.of(content));

            String requestBodyJson = objectMapper.writeValueAsString(requestBodyMap);

            String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + settings.getGeminiApiKey();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBodyJson))
                    .timeout(Duration.ofSeconds(30))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode root = objectMapper.readTree(response.body());
                JsonNode textNode = root.at("/candidates/0/content/parts/0/text");
                if (!textNode.isMissingNode()) {
                    String rawReply = textNode.asText().trim();
                    // Clean possible markdown code fences
                    if (rawReply.startsWith("```json")) {
                        rawReply = rawReply.substring(7);
                    }
                    if (rawReply.startsWith("```")) {
                        rawReply = rawReply.substring(3);
                    }
                    if (rawReply.endsWith("```")) {
                        rawReply = rawReply.substring(0, rawReply.length() - 3);
                    }
                    rawReply = rawReply.trim();

                    JsonNode parsedJson = objectMapper.readTree(rawReply);

                    if (parsedJson.hasNonNull("orderId")) {
                        result.setOrderId(parsedJson.get("orderId").asText());
                        result.addDetectedField("orderId");
                    }
                    if (parsedJson.hasNonNull("purchaseDate")) {
                        String dateStr = parsedJson.get("purchaseDate").asText();
                        try {
                            if (dateStr.length() == 10) {
                                result.setPurchaseDate(LocalDateTime.parse(dateStr + "T12:00:00"));
                            } else {
                                result.setPurchaseDate(LocalDateTime.parse(dateStr));
                            }
                            result.addDetectedField("purchaseDate");
                        } catch (Exception ignored) {}
                    }
                    if (parsedJson.hasNonNull("itemCategory")) {
                        try {
                            result.setItemCategory(ItemCategory.valueOf(parsedJson.get("itemCategory").asText()));
                            result.addDetectedField("itemCategory");
                        } catch (Exception ignored) {}
                    }
                    if (parsedJson.hasNonNull("customItemName")) {
                        result.setCustomItemName(parsedJson.get("customItemName").asText());
                    }
                    if (parsedJson.hasNonNull("platform")) {
                        try {
                            result.setPlatform(PlatformStore.valueOf(parsedJson.get("platform").asText()));
                            result.addDetectedField("platform");
                        } catch (Exception ignored) {}
                    }
                    if (parsedJson.hasNonNull("paymentMethod")) {
                        result.setPaymentMethod(parsedJson.get("paymentMethod").asText());
                        result.addDetectedField("paymentMethod");
                    }
                    if (parsedJson.hasNonNull("amount")) {
                        result.setAmount(new BigDecimal(parsedJson.get("amount").asText()));
                        result.addDetectedField("amount");
                    }
                    if (parsedJson.hasNonNull("currency")) {
                        result.setCurrency(parsedJson.get("currency").asText().toUpperCase());
                        result.addDetectedField("currency");
                    }

                    result.setSuccess(true);
                    result.setMessage("AI Vision successfully parsed receipt details!");
                    result.setRawText(rawReply);
                    return result;
                }
            }

            result.setSuccess(false);
            result.setMessage("Gemini API error: HTTP " + response.statusCode());
        } catch (Exception e) {
            result.setSuccess(false);
            result.setMessage("Failed to scan with AI Vision: " + e.getMessage());
        }

        return result;
    }
}
