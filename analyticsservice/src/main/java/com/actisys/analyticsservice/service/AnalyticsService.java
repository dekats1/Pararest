package com.actisys.analyticsservice.service;

import com.actisys.analyticsservice.dto.AnalyticsDTO;
import reactor.core.publisher.Mono;

public interface AnalyticsService {
    Mono<AnalyticsDTO> getMonthlyAnalytics();
}