package ru.practicum.ewm.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.client.StatClient;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.ewm.dao.CategoryRepo;
import ru.practicum.ewm.dao.EventRepo;
import ru.practicum.ewm.dao.LocationRepo;
import ru.practicum.ewm.dao.UserRepo;
import ru.practicum.ewm.dto.event.*;
import ru.practicum.ewm.exception.DataConflictException;
import ru.practicum.ewm.exception.DataNotFoundException;
import ru.practicum.ewm.exception.ValidationException;
import ru.practicum.ewm.mapper.EventMapper;
import ru.practicum.ewm.mapper.LocationMapper;
import ru.practicum.ewm.model.Category;
import ru.practicum.ewm.model.Event;
import ru.practicum.ewm.model.Location;
import ru.practicum.ewm.model.User;
import ru.practicum.ewm.service.EventService;
import ru.practicum.ewm.util.DateTimePattern;
import ru.practicum.ewm.util.enums.EventState;
import ru.practicum.ewm.util.enums.StateAction;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventServiceImpl implements EventService {
    private final UserRepo userRepo;
    private final EventRepo eventRepo;
    private final CategoryRepo categoryRepo;
    private final EventMapper mapper;
    private final StatClient statClient;
    private final LocationRepo locationRepo;
    private final LocationMapper locationMapper;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(DateTimePattern.PATTERN);

    @Override
    public EventFullDto saveEvent(Long userId, NewEventDto newEventDto) {
        Category category = categoryRepo.findById(newEventDto.getCategory())
                .orElseThrow(() -> new DataNotFoundException("Category not found."));
        LocalDateTime eventDate = newEventDto.getEventDate();
        if (eventDate.isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ValidationException("EventDate violation. Should be at least 2 hours ahead of current time");
        }
        User user = findUser(userId);
        Location location = locationMapper.toLocation(newEventDto.getLocation());
        Event event = mapper.toEvent(newEventDto);
        if (newEventDto.getPaid() == null) {
            event.setPaid(false);
        }
        if (newEventDto.getRequestModeration() == null) {
            event.setRequestModeration(true);
        }
        event.setCreatedOn(LocalDateTime.now());
        event.setState(EventState.PENDING);
        event.setInitiator(user);
        event.setCategory(category);
        event.setLocation(location);
        locationRepo.save(location);
        log.info("Event {} by user with id {} saved.", newEventDto.getTitle(), userId);
        return mapper.toEventFullDto(eventRepo.save(event));
    }

    @Override
    public EventFullDto getEventByUserId(Long userId, Long eventId) {
        findUser(userId);
        findEvent(eventId);
        log.info("Getting event {} by user {}", eventId, userId);
        return mapper.toEventFullDto(eventRepo.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new DataNotFoundException(String.format("Event with id %d not found.", eventId))));
    }

    @Override
    public List<EventShortDto> getEvents(Long userId, Integer from, Integer size) {
        findUser(userId);
        Pageable page = PageRequest.of(from / size, size);
        log.info("Getting events list for user id {} pageable.", userId);
        return mapper.toEventShortDtoList(eventRepo.findAllByInitiatorId(userId, page).toList());
    }

    @Override
    public EventFullDto updateEventByUserId(Long userId, Long eventId, UpdateEventUserRequest updateEventUserRequest) {
        findUser(userId);
        Event event = findEvent(eventId);

        if (!(event.getState().equals(EventState.PENDING) || event.getState().equals(EventState.CANCELED))) {
            throw new DataConflictException("Only pending or cancelled events can be modified.");
        }
        if (updateEventUserRequest.getStateAction() != null) {
            event.setState(updateEventUserRequest.getStateAction()
                    .equals(StateAction.SEND_TO_REVIEW) ? EventState.PENDING : EventState.CANCELED);
        }

        if (updateEventUserRequest.getEventDate() != null) {
            LocalDateTime eventDateTime = updateEventUserRequest.getEventDate();
            if (eventDateTime.isBefore(LocalDateTime.now().plusHours(2))) {
                throw new ValidationException("Start date should be at least 2 hours ahead of current time.");
            }
            event.setEventDate(eventDateTime);
        }

        if (updateEventUserRequest.getAnnotation() != null) {
            event.setAnnotation(updateEventUserRequest.getAnnotation());
        }
        if (updateEventUserRequest.getCategory() != null) {
            Category category = categoryRepo.findById(updateEventUserRequest.getCategory())
                    .orElseThrow(() -> new DataNotFoundException("Category not found."));
            event.setCategory(category);
        }
        if (updateEventUserRequest.getDescription() != null) {
            event.setDescription(updateEventUserRequest.getDescription());
        }

        if (updateEventUserRequest.getLocation() != null) {
            event.setLocation(locationMapper.toLocation(updateEventUserRequest.getLocation()));
        }
        if (updateEventUserRequest.getPaid() != null) {
            event.setPaid(updateEventUserRequest.getPaid());
        }
        if (updateEventUserRequest.getParticipantLimit() != null) {
            event.setParticipantLimit(updateEventUserRequest.getParticipantLimit());
        }
        if (updateEventUserRequest.getRequestModeration() != null) {
            event.setRequestModeration(updateEventUserRequest.getRequestModeration());
        }
        if (updateEventUserRequest.getTitle() != null) {
            event.setTitle(updateEventUserRequest.getTitle());
        }

        log.info("Event with id {} updated by user with id {}", eventId, userId);
        return mapper.toEventFullDto(eventRepo.save(event));
    }

    @Override
    public EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest updateEvent) {
        Event event = findEvent(eventId);

        if (updateEvent.getEventDate() != null) {
            LocalDateTime date = updateEvent.getEventDate();
            if (date.isBefore(LocalDateTime.now().plusHours(1))) {
                throw new ValidationException("Start date of the event should be at least 1 hour ahead of current time.");
            }
            event.setEventDate(date);
        }

        if (updateEvent.getStateAction() != null) {
            switch (updateEvent.getStateAction()) {

                case PUBLISH_EVENT:
                    if (!event.getState().equals(EventState.PENDING)) {
                        throw new DataConflictException("Event state should be PENDING.");
                    }
                    event.setState(EventState.PUBLISHED);
                    event.setPublishedOn(LocalDateTime.now());
                    break;
                case CANCEL_REVIEW:
                    event.setState(EventState.CANCELED);
                    break;
                case REJECT_EVENT:
                    if (event.getState().equals(EventState.PUBLISHED)) {
                        throw new DataConflictException("Event already published.");
                    }
                    event.setState(EventState.CANCELED);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown event status action.");
            }
        }

        if (updateEvent.getAnnotation() != null) {
            if (updateEvent.getAnnotation().length() < 20 || updateEvent.getAnnotation().length() > 2000) {
                throw new ValidationException("Annotation length violation. Must be from 20 to 2000.");
            }
            event.setAnnotation(updateEvent.getAnnotation());
        }
        if (updateEvent.getCategory() != null) {
            Category category = categoryRepo.findById(updateEvent.getCategory())
                    .orElseThrow(() -> new DataNotFoundException("Category not found."));
            event.setCategory(category);
        }
        if (updateEvent.getDescription() != null) {
            if (updateEvent.getDescription().length() < 20 || updateEvent.getDescription().length() > 7000) {
                throw new ValidationException("Description length violation. Must be from 20 to 7000.");
            }
            event.setDescription(updateEvent.getDescription());
        }

        if (updateEvent.getLocation() != null) {
            Location location = locationMapper.toLocation(updateEvent.getLocation());
            event.setLocation(locationRepo.save(location));
        }
        if (updateEvent.getPaid() != null) {
            event.setPaid(updateEvent.getPaid());
        }
        if (updateEvent.getParticipantLimit() != null) {
            event.setParticipantLimit(updateEvent.getParticipantLimit());
        }
        if (updateEvent.getRequestModeration() != null) {
            event.setRequestModeration(updateEvent.getRequestModeration());
        }

        if (updateEvent.getTitle() != null) {
            if (updateEvent.getTitle().length() < 3 || updateEvent.getTitle().length() > 120) {
                throw new ValidationException("Title length violation. Must be from 3 to 120.");
            }
            event.setTitle(updateEvent.getTitle());
        }
        log.info("Event with id {} updated by admin.", eventId);
        return mapper.toEventFullDto(eventRepo.save(event));
    }

    @Override
    public List<EventFullDto> getEventsWithParamsByAdmin(List<Long> users, List<EventState> states, List<Long> categories,
                                                         LocalDateTime rangeStart, LocalDateTime rangeEnd, PageRequest page) {
        if (rangeStart == null) {
            rangeStart = LocalDateTime.now();
        }
        if (rangeEnd == null) {
            rangeEnd = LocalDateTime.now().plusYears(100);
        }
        if (rangeStart.isAfter(rangeEnd)) {
            throw new ValidationException("The start of the event cannot be after the end of the event");
        }
        List<Event> events = eventRepo.findEventsWithParams(users, states, categories, rangeStart, rangeEnd, page);
        return mapper.toEventFullDtoList(events);
    }

    @Override
    public List<EventShortDto> getEventsWithParamsByUser(String text, List<Long> categories, Boolean paid,
                                                         LocalDateTime rangeStart,
                                                         LocalDateTime rangeEnd, Boolean onlyAvailable,
                                                         String sort, Integer from, Integer size,
                                                         HttpServletRequest request) {
        if (rangeStart != null && rangeEnd != null) {
            if (rangeStart.isAfter(rangeEnd)) {
                throw new ValidationException("Start time have to be after end time.");
            }
        }

        PageRequest pageRequest = PageRequest.of(from / size, size);
        List<Event> events = eventRepo.findEventsWithParamsByUser(text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort, pageRequest);

        List<String> uris = events.stream()
                .map(event -> "/events/" + event.getId())
                .collect(Collectors.toList());

        String start = rangeStart != null ? rangeStart.format(dateFormatter) : "2000-01-01 00:00:00";
        String end = rangeEnd != null ? rangeEnd.format(dateFormatter) : LocalDateTime.now().format(dateFormatter);

        List<ViewStatsDto> stats = statClient.getStats(start, end, uris, false);

        for (Event event : events) {
            String uri = "/events/" + event.getId();
            long views = stats.stream()
                    .filter(stat -> stat.getUri().equals(uri))
                    .map(ViewStatsDto::getHits)
                    .findFirst()
                    .orElse(0L);
            event.setViews(views);
            eventRepo.save(event);
        }

        return mapper.toEventShortDtoList(events);
    }

    @Override
    public EventFullDto getEvent(Long eventId, HttpServletRequest request) {
        Event event = findEvent(eventId);
        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new DataNotFoundException(String.format("Event with id %d not published.", eventId));
        }

        EndpointHitDto endpointHitDto = createEndpointHitDto(eventId, request);
        statClient.addStats(endpointHitDto);

        String start = event.getCreatedOn().format(dateFormatter);
        String end = LocalDateTime.now().format(dateFormatter);
        List<ViewStatsDto> stats = statClient.getStats(start, end, List.of("/events/" + event.getId()), true);

        if (stats.isEmpty()) {
            event.setViews(0L);
        } else {
            event.setViews(stats.get(0).getHits());
        }

        log.info("Get event with id {}", eventId);
        return mapper.toEventFullDto(event);
    }

    private User findUser(Long userId) {
        return userRepo.findById(userId)
                .orElseThrow(() -> new DataNotFoundException(String.format("User with id %d not found.", userId)));
    }

    private Event findEvent(Long eventId) {
        return eventRepo.findByIdWithAllParams(eventId)
                .orElseThrow(() -> new DataNotFoundException(String.format("Event with id %d not found.", eventId)));
    }

    private EndpointHitDto createEndpointHitDto(Long eventId, HttpServletRequest request) {
        return EndpointHitDto.builder()
                .app("ewm-service") // Имя вашего приложения
                .uri(eventId != null ? "/events/" + eventId : "/events") // URI с eventId если доступен
                .ip(request.getRemoteAddr()) // IP-адрес пользователя
                .timestamp(LocalDateTime.now()) // Временная метка запроса
                .build();
    }
}



