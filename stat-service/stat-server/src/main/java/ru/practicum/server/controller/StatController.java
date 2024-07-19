package ru.practicum.server.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.server.service.StatService;
import ru.practicum.server.util.DateTimePattern;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class StatController {

    private final StatService service;

    @PostMapping("/hit")
    @ResponseStatus(HttpStatus.CREATED)
    public EndpointHitDto saveHit(@RequestBody EndpointHitDto dto) {
        log.info("POST request to create a hit of {}", dto.toString());
        return service.createEndpointHitDto(dto);
    }

    @GetMapping("/stats")
    public List<ViewStatsDto> getStats(@RequestParam(defaultValue = "false") boolean unique,
                                       @RequestParam(name = "start") @DateTimeFormat(pattern = DateTimePattern.PATTERN)
                                       LocalDateTime start,
                                       @RequestParam(name = "end") @DateTimeFormat(pattern = DateTimePattern.PATTERN)
                                       LocalDateTime end,
                                       @RequestParam(required = false) List<String> uris) {
        log.info("GET request to find stats with unique={}, start={}, end={}, uris={}", unique,
                start.toString(), end.toString(), uris);
        List<String> urisList = uris != null ? uris : new ArrayList<>();
        return service.getStats(unique, start, end, urisList);
    }
}
