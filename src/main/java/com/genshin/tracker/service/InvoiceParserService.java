package com.genshin.tracker.service;

import com.genshin.tracker.dto.ScanResultDTO;
import com.genshin.tracker.model.ItemCategory;
import com.genshin.tracker.model.PlatformStore;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class InvoiceParserService {

    // Regex for Order IDs
    private static final Pattern GOOGLE_PLAY_ORDER_PATTERN =
            Pattern.compile("\\b(GPA\\.\\d{4}-\\d{4}-\\d{4}-\\d{5})\\b", Pattern.CASE_INSENSITIVE);

    private static final Pattern APPLE_ORDER_PATTERN =
            Pattern.compile("(?:Order ID|Order Number|Document No\\.?|Doc No\\.?|Invoice No\\.?)[:\\s#]*([A-Z0-9]{9,15}|\\d{10,14})\\b", Pattern.CASE_INSENSITIVE);

    private static final Pattern PAYPAL_TRANS_PATTERN =
            Pattern.compile("(?:Transaction ID|Trans ID|ID giao dịch|Transaktionscode)[:\\s]*([A-Z0-9]{17})\\b", Pattern.CASE_INSENSITIVE);

    private static final Pattern CODASHOP_ORDER_PATTERN =
            Pattern.compile("(?:Codashop|Order ID|Txn ID)[:\\s#]*([0-9]{10,20})\\b", Pattern.CASE_INSENSITIVE);

    private static final Pattern RAZER_TRANS_PATTERN =
            Pattern.compile("(?:Razer|Reference ID|Transaction ID)[:\\s#]*([A-Z0-9-]{12,24})\\b", Pattern.CASE_INSENSITIVE);

    // Regex for Amounts with currencies
    private static final Pattern USD_PRICE_PATTERN =
            Pattern.compile("(?:\\$|USD\\s*)\\s*([0-9]+(?:\\.[0-9]{2})?)", Pattern.CASE_INSENSITIVE);

    private static final Pattern VND_PRICE_PATTERN =
            Pattern.compile("([0-9]{1,3}(?:[.,][0-9]{3})+)\\s*(?:₫|VND|d|dong)", Pattern.CASE_INSENSITIVE);

    private static final Pattern EUR_PRICE_PATTERN =
            Pattern.compile("(?:€|EUR\\s*)\\s*([0-9]+(?:[.,][0-9]{2})?)", Pattern.CASE_INSENSITIVE);

    private static final Pattern JPY_PRICE_PATTERN =
            Pattern.compile("(?:¥|JPY\\s*)\\s*([0-9,]+)", Pattern.CASE_INSENSITIVE);

    private static final Pattern GENERIC_PRICE_PATTERN =
            Pattern.compile("(?:Total|Amount|Paid|Subtotal|Giá|Tổng cộng|Số tiền)[:\\s]*([A-Za-z$€₫¥£]+)?\\s*([0-9]+(?:[.,][0-9]{2,3})*)", Pattern.CASE_INSENSITIVE);

    public ScanResultDTO parseText(String rawText) {
        ScanResultDTO result = new ScanResultDTO();
        result.setRawText(rawText);

        if (rawText == null || rawText.isBlank()) {
            result.setSuccess(false);
            result.setMessage("No text could be extracted from the invoice.");
            return result;
        }

        extractOrderIdAndPlatform(rawText, result);
        extractItemCategory(rawText, result);
        extractAmountAndCurrency(rawText, result);
        extractPurchaseDate(rawText, result);
        extractPaymentMethod(rawText, result);

        // Autofill amount if item category was detected with default USD price and amount was missing
        if (result.getAmount() == null && result.getItemCategory() != null && result.getItemCategory().getDefaultUsdPrice() != null) {
            result.setAmount(result.getItemCategory().getDefaultUsdPrice());
            if (result.getCurrency() == null) {
                result.setCurrency("USD");
            }
            result.addDetectedField("amount (preset)");
        }

        result.setSuccess(!result.getDetectedFields().isEmpty());
        if (result.isSuccess()) {
            result.setMessage("Successfully scanned invoice details!");
        } else {
            result.setMessage("Scanned text, but could not detect specific bill patterns.");
        }

        return result;
    }

    private void extractOrderIdAndPlatform(String text, ScanResultDTO result) {
        // 1. Check Google Play
        Matcher gpaMatcher = GOOGLE_PLAY_ORDER_PATTERN.matcher(text);
        if (gpaMatcher.find()) {
            result.setOrderId(gpaMatcher.group(1).toUpperCase());
            result.setPlatform(PlatformStore.GOOGLE_PLAY);
            result.addDetectedField("orderId");
            result.addDetectedField("platform");
            return;
        }

        // 2. Check PayPal
        Matcher paypalMatcher = PAYPAL_TRANS_PATTERN.matcher(text);
        if (paypalMatcher.find()) {
            result.setOrderId(paypalMatcher.group(1).toUpperCase());
            result.setPaymentMethod("PayPal");
            result.addDetectedField("orderId");
            result.addDetectedField("paymentMethod");
        }

        // 3. Check Apple App Store
        Matcher appleMatcher = APPLE_ORDER_PATTERN.matcher(text);
        if (appleMatcher.find()) {
            result.setOrderId(appleMatcher.group(1).toUpperCase());
            result.setPlatform(PlatformStore.APPLE_APP_STORE);
            result.addDetectedField("orderId");
            result.addDetectedField("platform");
            return;
        }

        // 4. Check Codashop
        if (text.toLowerCase().contains("codashop")) {
            result.setPlatform(PlatformStore.CODASHOP);
            result.addDetectedField("platform");
            Matcher codaMatcher = CODASHOP_ORDER_PATTERN.matcher(text);
            if (codaMatcher.find() && result.getOrderId() == null) {
                result.setOrderId(codaMatcher.group(1));
                result.addDetectedField("orderId");
            }
            return;
        }

        // 5. Check Razer Gold
        if (text.toLowerCase().contains("razer")) {
            result.setPlatform(PlatformStore.RAZER_GOLD);
            result.addDetectedField("platform");
            Matcher razerMatcher = RAZER_TRANS_PATTERN.matcher(text);
            if (razerMatcher.find() && result.getOrderId() == null) {
                result.setOrderId(razerMatcher.group(1));
                result.addDetectedField("orderId");
            }
            return;
        }

        // General platform mentions
        String lower = text.toLowerCase();
        if (lower.contains("google play")) {
            result.setPlatform(PlatformStore.GOOGLE_PLAY);
            result.addDetectedField("platform");
        } else if (lower.contains("app store") || lower.contains("itunes") || lower.contains("apple")) {
            result.setPlatform(PlatformStore.APPLE_APP_STORE);
            result.addDetectedField("platform");
        } else if (lower.contains("epic games")) {
            result.setPlatform(PlatformStore.EPIC_GAMES);
            result.addDetectedField("platform");
        } else if (lower.contains("playstation") || lower.contains("sony interactive")) {
            result.setPlatform(PlatformStore.PLAYSTATION_NETWORK);
            result.addDetectedField("platform");
        } else if (lower.contains("worldpay") || lower.contains("cognosphere") || lower.contains("mihoyo")) {
            result.setPlatform(PlatformStore.PC_WORLDPAY);
            result.addDetectedField("platform");
        }
    }

    private void extractItemCategory(String text, ScanResultDTO result) {
        String lower = text.toLowerCase();

        if (lower.contains("welkin") || lower.contains("blessing") || lower.contains("khong nguyet") || lower.contains("không nguyệt")) {
            result.setItemCategory(ItemCategory.WELKIN_MOON);
            result.addDetectedField("itemCategory");
        } else if (lower.contains("chorus")) {
            result.setItemCategory(ItemCategory.BATTLE_PASS_CHORUS);
            result.addDetectedField("itemCategory");
        } else if (lower.contains("hymn") || lower.contains("battle pass") || lower.contains("nhat ky hanh trinh") || lower.contains("nhật ký")) {
            result.setItemCategory(ItemCategory.BATTLE_PASS_HYMN);
            result.addDetectedField("itemCategory");
        } else if (lower.contains("6480") || lower.contains("6,480")) {
            result.setItemCategory(ItemCategory.GENESIS_6480);
            result.addDetectedField("itemCategory");
        } else if (lower.contains("3280") || lower.contains("3,280")) {
            result.setItemCategory(ItemCategory.GENESIS_3280);
            result.addDetectedField("itemCategory");
        } else if (lower.contains("1980") || lower.contains("1,980")) {
            result.setItemCategory(ItemCategory.GENESIS_1980);
            result.addDetectedField("itemCategory");
        } else if (lower.contains("980")) {
            result.setItemCategory(ItemCategory.GENESIS_980);
            result.addDetectedField("itemCategory");
        } else if (lower.contains("300")) {
            result.setItemCategory(ItemCategory.GENESIS_300);
            result.addDetectedField("itemCategory");
        } else if (lower.contains("60")) {
            result.setItemCategory(ItemCategory.GENESIS_60);
            result.addDetectedField("itemCategory");
        } else if (lower.contains("outfit") || lower.contains("skin") || lower.contains("trang phục")) {
            result.setItemCategory(ItemCategory.CHARACTER_OUTFIT);
            result.addDetectedField("itemCategory");
        }
    }

    private void extractAmountAndCurrency(String text, ScanResultDTO result) {
        // 1. VND pattern: e.g. 109.000 ₫
        Matcher vndMatcher = VND_PRICE_PATTERN.matcher(text);
        if (vndMatcher.find()) {
            String rawAmount = vndMatcher.group(1).replaceAll("[.,]", "");
            try {
                result.setAmount(new BigDecimal(rawAmount));
                result.setCurrency("VND");
                result.addDetectedField("amount");
                result.addDetectedField("currency");
                return;
            } catch (Exception ignored) {}
        }

        // 2. USD pattern: e.g. $4.99
        Matcher usdMatcher = USD_PRICE_PATTERN.matcher(text);
        if (usdMatcher.find()) {
            try {
                result.setAmount(new BigDecimal(usdMatcher.group(1)));
                result.setCurrency("USD");
                result.addDetectedField("amount");
                result.addDetectedField("currency");
                return;
            } catch (Exception ignored) {}
        }

        // 3. EUR pattern: e.g. €5.99
        Matcher eurMatcher = EUR_PRICE_PATTERN.matcher(text);
        if (eurMatcher.find()) {
            try {
                String cleanVal = eurMatcher.group(1).replace(',', '.');
                result.setAmount(new BigDecimal(cleanVal));
                result.setCurrency("EUR");
                result.addDetectedField("amount");
                result.addDetectedField("currency");
                return;
            } catch (Exception ignored) {}
        }

        // 4. JPY pattern: e.g. ¥610
        Matcher jpyMatcher = JPY_PRICE_PATTERN.matcher(text);
        if (jpyMatcher.find()) {
            try {
                String cleanVal = jpyMatcher.group(1).replace(",", "");
                result.setAmount(new BigDecimal(cleanVal));
                result.setCurrency("JPY");
                result.addDetectedField("amount");
                result.addDetectedField("currency");
                return;
            } catch (Exception ignored) {}
        }

        // 5. Generic Total pattern
        Matcher genericMatcher = GENERIC_PRICE_PATTERN.matcher(text);
        if (genericMatcher.find()) {
            try {
                String symbol = genericMatcher.group(1);
                String val = genericMatcher.group(2).replace(',', '.');
                result.setAmount(new BigDecimal(val));
                result.addDetectedField("amount");
                if (symbol != null && !symbol.isBlank()) {
                    result.setCurrency(normalizeCurrencySymbol(symbol));
                    result.addDetectedField("currency");
                }
            } catch (Exception ignored) {}
        }
    }

    private String normalizeCurrencySymbol(String symbol) {
        symbol = symbol.trim().toUpperCase();
        if (symbol.contains("$")) return "USD";
        if (symbol.contains("€")) return "EUR";
        if (symbol.contains("₫") || symbol.contains("VND")) return "VND";
        if (symbol.contains("¥") || symbol.contains("JPY")) return "JPY";
        if (symbol.contains("£")) return "GBP";
        return symbol;
    }

    private void extractPurchaseDate(String text, ScanResultDTO result) {
        // Regex patterns for dates
        // e.g. "Sep 28, 2021", "28 Sep 2021", "2021-09-28", "28/09/2021", "09/28/2021"
        Pattern[] datePatterns = {
                Pattern.compile("\\b([A-Za-z]{3,9}\\s+\\d{1,2},\\s*\\d{4})\\b"),
                Pattern.compile("\\b(\\d{1,2}\\s+[A-Za-z]{3,9}\\s+\\d{4})\\b"),
                Pattern.compile("\\b(\\d{4}-\\d{2}-\\d{2})\\b"),
                Pattern.compile("\\b(\\d{2}/\\d{2}/\\d{4})\\b")
        };

        for (Pattern pattern : datePatterns) {
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                String dateStr = matcher.group(1);
                LocalDate parsedDate = tryParseDate(dateStr);
                if (parsedDate != null) {
                    result.setPurchaseDate(parsedDate.atTime(LocalTime.of(12, 0)));
                    result.addDetectedField("purchaseDate");
                    return;
                }
            }
        }
    }

    private LocalDate tryParseDate(String dateStr) {
        String[] formats = {
                "MMM d, yyyy", "MMMM d, yyyy", "d MMM yyyy", "d MMMM yyyy",
                "yyyy-MM-dd", "dd/MM/yyyy", "MM/dd/yyyy"
        };
        for (String fmt : formats) {
            try {
                DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                        .parseCaseInsensitive()
                        .appendPattern(fmt)
                        .toFormatter(Locale.ENGLISH);
                return LocalDate.parse(dateStr, formatter);
            } catch (DateTimeParseException ignored) {}
        }
        return null;
    }

    private void extractPaymentMethod(String text, ScanResultDTO result) {
        if (result.getPaymentMethod() != null) return;
        String lower = text.toLowerCase();

        if (lower.contains("visa")) {
            result.setPaymentMethod("Visa");
            result.addDetectedField("paymentMethod");
        } else if (lower.contains("mastercard")) {
            result.setPaymentMethod("Mastercard");
            result.addDetectedField("paymentMethod");
        } else if (lower.contains("paypal")) {
            result.setPaymentMethod("PayPal");
            result.addDetectedField("paymentMethod");
        } else if (lower.contains("momo")) {
            result.setPaymentMethod("MoMo");
            result.addDetectedField("paymentMethod");
        } else if (lower.contains("google play balance") || lower.contains("số dư google play")) {
            result.setPaymentMethod("Google Play Balance");
            result.addDetectedField("paymentMethod");
        } else if (lower.contains("apple pay")) {
            result.setPaymentMethod("Apple Pay");
            result.addDetectedField("paymentMethod");
        }
    }
}
