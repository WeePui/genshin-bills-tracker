package com.genshin.tracker.controller;

import com.genshin.tracker.dto.ScanResultDTO;
import com.genshin.tracker.service.InvoiceParserService;
import com.genshin.tracker.service.VisionScanService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/scan")
public class ScanController {

    private final InvoiceParserService invoiceParserService;
    private final VisionScanService visionScanService;

    public ScanController(InvoiceParserService invoiceParserService, VisionScanService visionScanService) {
        this.invoiceParserService = invoiceParserService;
        this.visionScanService = visionScanService;
    }

    @PostMapping("/text")
    public ResponseEntity<ScanResultDTO> parseText(@RequestBody Map<String, String> payload) {
        String text = payload.get("text");
        ScanResultDTO result = invoiceParserService.parseText(text);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/vision")
    public ResponseEntity<ScanResultDTO> scanWithVision(@RequestParam("file") MultipartFile file) {
        ScanResultDTO result = visionScanService.scanWithGemini(file);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getScanStatus() {
        return ResponseEntity.ok(Map.of(
                "visionConfigured", visionScanService.isConfigured(),
                "localOcrEnabled", true
        ));
    }
}
