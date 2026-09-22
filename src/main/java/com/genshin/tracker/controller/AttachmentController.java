package com.genshin.tracker.controller;

import com.genshin.tracker.service.StorageService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.nio.file.Files;

@Controller
@RequestMapping("/attachments")
public class AttachmentController {

    private final StorageService storageService;

    public AttachmentController(StorageService storageService) {
        this.storageService = storageService;
    }

    @GetMapping("/{storedName}")
    public ResponseEntity<Resource> viewAttachment(@PathVariable String storedName) throws IOException {
        Resource resource = storageService.loadAsResource(storedName);
        String contentType = Files.probeContentType(storageService.getFilePath(storedName));
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @GetMapping("/{storedName}/download")
    public ResponseEntity<Resource> downloadAttachment(@PathVariable String storedName) throws IOException {
        Resource resource = storageService.loadAsResource(storedName);
        String contentType = Files.probeContentType(storageService.getFilePath(storedName));
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
}
