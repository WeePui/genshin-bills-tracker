package com.genshin.tracker.service;

import com.genshin.tracker.model.AppSettings;
import com.genshin.tracker.repository.AppSettingsRepository;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class BackgroundService {

    private final AppSettingsRepository settingsRepository;
    private final List<String> availableBackgrounds = new ArrayList<>();

    public BackgroundService(AppSettingsRepository settingsRepository) {
        this.settingsRepository = settingsRepository;
        loadAvailableBackgrounds();
    }

    private void loadAvailableBackgrounds() {
        try {
            ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources("classpath:static/images/background_*");
            for (Resource res : resources) {
                if (res.getFilename() != null) {
                    availableBackgrounds.add(res.getFilename());
                }
            }
            // Sort nicely by natural number if possible
            availableBackgrounds.sort(Comparator.comparingInt(this::extractNumber));
        } catch (IOException e) {
            // Fallback list
            for (int i = 1; i <= 38; i++) {
                availableBackgrounds.add("background_" + i + ".png");
            }
            availableBackgrounds.add("background_39.jpg");
        }
    }

    private int extractNumber(String filename) {
        try {
            String numStr = filename.replaceAll("\\D+", "");
            return numStr.isEmpty() ? 0 : Integer.parseInt(numStr);
        } catch (Exception e) {
            return 0;
        }
    }

    public List<String> getAllBackgrounds() {
        return Collections.unmodifiableList(availableBackgrounds);
    }

    public String getActiveBackground() {
        AppSettings settings = settingsRepository.findById(1L).orElse(null);
        if (settings != null && "STATIC".equalsIgnoreCase(settings.getBackgroundMode())) {
            String staticBg = settings.getStaticBackground();
            if (staticBg != null && !staticBg.isBlank() && availableBackgrounds.contains(staticBg)) {
                return staticBg;
            }
        }
        return getRandomBackground();
    }

    public String getRandomBackground() {
        if (availableBackgrounds.isEmpty()) {
            return "background_1.png";
        }
        int index = ThreadLocalRandom.current().nextInt(availableBackgrounds.size());
        return availableBackgrounds.get(index);
    }
}
