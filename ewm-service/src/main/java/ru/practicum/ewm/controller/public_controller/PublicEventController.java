package ru.practicum.ewm.controller.public_controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.event.EventFullDto;
import ru.practicum.ewm.dto.event.EventShortDto;
import ru.practicum.ewm.service.EventService;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
@Slf4j
public class PublicEventController {
    private final EventService eventService;

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public EventFullDto getEvent(@PathVariable Long id, HttpServletRequest request) {
        log.info("GET request /events/{id}");
        return eventService.getEvent(id, request);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<EventShortDto> getEventsWithParamsByUser(@Size(min = 1, max = 7000)
                                                         @RequestParam(required = false) String text,
                                                         @RequestParam(required = false) List<Long> categories,
                                                         @RequestParam(required = false) Boolean paid,
                                                         @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                                                         @RequestParam(required = false) LocalDateTime rangeStart,
                                                         @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                                                         @RequestParam(required = false) LocalDateTime rangeEnd,
                                                         @RequestParam(required = false) Boolean onlyAvailable,
                                                         @RequestParam(required = false) String sort,
                                                         @PositiveOrZero
                                                         @RequestParam(required = false, defaultValue = "0") Integer from,
                                                         @Positive
                                                         @RequestParam(required = false, defaultValue = "10") Integer size,
                                                         HttpServletRequest request) {
        log.info("GET request /events");
        return eventService.getEventsWithParamsByUser(text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort, from, size, request);
    }
}
