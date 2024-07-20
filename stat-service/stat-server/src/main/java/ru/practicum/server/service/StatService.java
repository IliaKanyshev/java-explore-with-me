package ru.practicum.server.service;

import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.util.List;

public interface StatService {
    EndpointHitDto createEndpointHitDto(EndpointHitDto dto);

    List<ViewStatsDto> getStats(boolean unique, LocalDateTime start, LocalDateTime end, List<String> uris);
}
