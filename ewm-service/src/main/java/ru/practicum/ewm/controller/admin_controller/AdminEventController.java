package ru.practicum.ewm.controller.admin_controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.event.EventFullDto;
import ru.practicum.ewm.dto.event.UpdateEventAdminRequest;
import ru.practicum.ewm.service.EventService;
import ru.practicum.ewm.util.enums.EventState;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/admin/events")
@RequiredArgsConstructor
@Slf4j
public class AdminEventController {
    private final EventService eventService;

    @PatchMapping("/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public EventFullDto updateEvent(@PathVariable(name = "eventId") Long eventId, @Valid @RequestBody UpdateEventAdminRequest updateEventAdminRequest) {
        log.info("PATCH request /admin/events/{eventId}");
        return eventService.updateEventByAdmin(eventId, updateEventAdminRequest);

    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<EventFullDto> getEvents(@RequestParam(name = "users", required = false) List<Long> users,
                                        @RequestParam(name = "states", required = false) List<EventState> states,
                                        @RequestParam(name = "categories", required = false) List<Long> categoriesId,
                                        @RequestParam(name = "rangeStart", required = false) LocalDateTime rangeStart,
                                        @RequestParam(name = "rangeEnd", required = false) LocalDateTime rangeEnd,
                                        @RequestParam(name = "from", required = false, defaultValue = "0") Integer from,
                                        @RequestParam(name = "size", required = false, defaultValue = "10") Integer size) {
        PageRequest pageRequest = PageRequest.of(from / size, size);
        log.info("GET request /admin/events");
        return eventService.getEventsWithParamsByAdmin(users, states, categoriesId, rangeStart, rangeEnd, pageRequest);
    }
}
