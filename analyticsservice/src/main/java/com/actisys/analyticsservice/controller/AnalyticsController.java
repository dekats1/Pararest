package com.actisys.analyticsservice.controller;

import com.actisys.analyticsservice.dto.AnalyticsDTO;
import com.actisys.analyticsservice.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/monthly")
    public Mono<ResponseEntity<AnalyticsDTO>> getMonthlyAnalytics() {
        log.info("GET /api/v1/analytics/monthly");
        return analyticsService.getMonthlyAnalytics()
                .map(ResponseEntity::ok)
                .onErrorResume(e -> {
                    log.error("Failed to assemble analytics: {}", e.getMessage());
                    return Mono.just(ResponseEntity.internalServerError().build());
                });
    }
}