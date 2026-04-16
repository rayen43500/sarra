package org.example.backend.service;

import org.example.backend.web.dto.dashboard.DashboardStatsDto;

public interface DashboardService {
    DashboardStatsDto getStats();
}
