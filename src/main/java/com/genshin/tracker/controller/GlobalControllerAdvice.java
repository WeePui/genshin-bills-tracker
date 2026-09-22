package com.genshin.tracker.controller;

import com.genshin.tracker.service.BackgroundService;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

@ControllerAdvice
public class GlobalControllerAdvice {

    private final BackgroundService backgroundService;

    public GlobalControllerAdvice(BackgroundService backgroundService) {
        this.backgroundService = backgroundService;
    }

    @ModelAttribute("currentBackground")
    public String currentBackground() {
        return backgroundService.getActiveBackground();
    }

    @ModelAttribute("allBackgrounds")
    public List<String> allBackgrounds() {
        return backgroundService.getAllBackgrounds();
    }
}
