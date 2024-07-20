package ru.practicum.ewm.service;

import org.springframework.data.domain.PageRequest;
import ru.practicum.ewm.dto.event.*;
import ru.practicum.ewm.util.enums.EventState;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

public interface EventService {
    EventFullDto saveEvent(Long userId, NewEventDto newEventDto);

    EventFullDto getEvent(Long eventId, HttpServletRequest request);

    List<EventShortDto> getEvents(Long userId, Integer from, Integer size);

    EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest updateEventAdminRequest);

    EventFullDto updateEventByUserId(Long userId, Long eventId, UpdateEventUserRequest updateEventUserRequest);

    EventFullDto getEventByUserId(Long userId, Long eventId);

    List<EventFullDto> getEventsWithParamsByAdmin(List<Long> users, List<EventState> states, List<Long> categories,
                                                  LocalDateTime rangeStart, LocalDateTime rangeEnd, PageRequest page);

    List<EventShortDto> getEventsWithParamsByUser(String text, List<Long> categories, Boolean paid, LocalDateTime rangeStart,
                                                  LocalDateTime rangeEnd, Boolean onlyAvailable, String sort,
                                                  Integer from, Integer size, HttpServletRequest request);
}
