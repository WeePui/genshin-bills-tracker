package com.genshin.tracker.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TreeMap;

@RestController
@RequestMapping("/api/i18n")
public class I18nApiController {

    @GetMapping("/{lang}")
    public ResponseEntity<Map<String, String>> getTranslations(@PathVariable("lang") String lang) {
        Locale locale = "vi".equalsIgnoreCase(lang) ? Locale.forLanguageTag("vi") : Locale.ENGLISH;
        ResourceBundle bundle = ResourceBundle.getBundle("messages", locale, 
                ResourceBundle.Control.getControl(ResourceBundle.Control.FORMAT_PROPERTIES));
        Map<String, String> map = new TreeMap<>();
        Enumeration<String> keys = bundle.getKeys();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            map.put(key, bundle.getString(key));
        }
        return ResponseEntity.ok(map);
    }
}
