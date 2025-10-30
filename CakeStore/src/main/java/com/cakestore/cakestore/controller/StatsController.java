// src/main/java/com/cakestore/cakestore/controller/StatsController.java
package com.cakestore.cakestore.controller;

import com.cakestore.cakestore.service.StatsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/stats")
public class StatsController {

    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping
    public String showStatsDashboard(Model model) {
        // This correctly gets the detailed stats map, including the processed revenue data
        model.addAllAttributes(statsService.getDetailedStats());
        return "admin/stats";
    }
}