package ru.practicum.server.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.server.service.StatService;
import ru.practicum.server.util.DateTimePattern;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class StatController {

    private final StatService service;

    @PostMapping("/hit")
    @ResponseStatus(HttpStatus.CREATED)
    public void saveHit(@RequestBody EndpointHitDto endpointHitDto) {
        service.saveHit(endpointHitDto);
    }

    @GetMapping("/stats")
    public List<ViewStatsDto> getStats(@DateTimeFormat(pattern = DateTimePattern.Pattern)
                                       @RequestParam(value = "start") LocalDateTime start,
                                       @DateTimeFormat(pattern = DateTimePattern.Pattern)
                                       @RequestParam(value = "end") LocalDateTime end,
                                       @RequestParam List<String> uris,
                                       @RequestParam(required = false, defaultValue = "false") Boolean unique) {
        return service.getStats(start, end, uris, unique);
    }
}
