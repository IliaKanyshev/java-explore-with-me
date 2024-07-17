package ru.practicum.ewm.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
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
import ru.practicum.ewm.util.enums.SortValue;
import ru.practicum.ewm.util.enums.StateAction;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

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
        if (event.getPublishedOn() != null) {
            throw new DataConflictException("Event already published.");
        }
        if (updateEventUserRequest == null) {
            return mapper.toEventFullDto(event);
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
        if (updateEventUserRequest.getEventDate() != null) {
            LocalDateTime eventDateTime = updateEventUserRequest.getEventDate();
            if (eventDateTime.isBefore(LocalDateTime.now().plusHours(2))) {
                throw new ValidationException("Start date should be at least 2 hours ahead of current time.");
            }
            event.setEventDate(eventDateTime);
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
        if (updateEventUserRequest.getStateAction() != null) {
            event.setState(updateEventUserRequest.getStateAction()
                    .equals(StateAction.SEND_TO_REVIEW) ? EventState.PENDING : EventState.CANCELED);
        }
        log.info("Event with id {} updated by user with id {}", eventId, userId);
        return mapper.toEventFullDto(eventRepo.save(event));
    }

    @Override
    public EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest updateEvent) {
        Event event = findEvent(eventId);
        if (updateEvent.getAnnotation() != null) {
            if (updateEvent.getAnnotation().length() < 20 || updateEvent.getAnnotation().length() > 2000) {
                throw new ValidationException("Annotation length violation. Must be from 20 to 2000.");
            }
            event.setAnnotation(updateEvent.getAnnotation());
        }
        if (updateEvent.getCategory() != null) {
            Category category = categoryRepo.findById(updateEvent.getCategory()).orElseThrow(() -> new DataNotFoundException("Category not found."));
            event.setCategory(category);
        }
        if (updateEvent.getDescription() != null) {
            if (updateEvent.getDescription().length() < 20 || updateEvent.getDescription().length() > 7000) {
                throw new ValidationException("Description length violation. Must be from 20 to 7000.");
            }
            event.setDescription(updateEvent.getDescription());
        }
        if (updateEvent.getEventDate() != null) {
            LocalDateTime date = updateEvent.getEventDate();
            if (date.isBefore(LocalDateTime.now().plusHours(1))) {
                throw new ValidationException("Start date of the event should be at least 1 hour ahead of current time.");
            }
            event.setEventDate(date);
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
        if (updateEvent.getStateAction() != null) {
            switch (updateEvent.getStateAction()) {
                case SEND_TO_REVIEW:
                    event.setState(EventState.PENDING);
                    break;
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
            }
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
//    public List<EventFullDto> getEventsWithParamsByAdmin(List<Long> users, EventState states, List<Long> categoriesId, String rangeStart, String rangeEnd, Integer from, Integer size) {
//        LocalDateTime start = rangeStart != null ? LocalDateTime.parse(rangeStart, dateFormatter) : null;
//        LocalDateTime end = rangeEnd != null ? LocalDateTime.parse(rangeEnd, dateFormatter) : null;
//
//        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
//        CriteriaQuery<Event> query = builder.createQuery(Event.class);
//        Root<Event> root = query.from(Event.class);
//
//        List<Predicate> predicates = new ArrayList<>();
//
//        if (!CollectionUtils.isEmpty(categoriesId)) {
//            predicates.add(root.get("category").in(categoriesId));
//        }
//
//        if (!CollectionUtils.isEmpty(users)) {
//            predicates.add(root.get("initiator").in(users));
//        }
//
//        if (states != null) {
//            predicates.add(root.get("state").in(states));
//        }
//
//        if (start != null) {
//            predicates.add(builder.greaterThanOrEqualTo(root.get("eventDate").as(LocalDateTime.class), start));
//        }
//
//        if (end != null) {
//            predicates.add(builder.lessThanOrEqualTo(root.get("eventDate").as(LocalDateTime.class), end));
//        }
//
//        query.select(root).where(builder.and(predicates.toArray(new Predicate[0])));
//
//        List<Event> events = entityManager.createQuery(query)
//                .setFirstResult(from)
//                .setMaxResults(size)
//                .getResultList();
//
//        if (events.isEmpty()) {
//            return new ArrayList<>();
//        }
//
//        events.forEach(this::setViews);
//        return mapper.toEventFullDtoList(events);
//    }
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
        events.forEach(this::setViews);
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

//        sendInfo(uri, ip);
        for (Event event : events) {
//            event.setViews(getViewsEventById(event.getId()));
            eventRepo.save(event);
        }

        return mapper.toEventShortDtoList(events);
    }

//    public List<EventFullDto> getEventsWithParamsByUser(String text, List<Long> categories, Boolean paid, String rangeStart,
//                                                        String rangeEnd, Boolean onlyAvailable, SortValue sort, Integer from, Integer size, HttpServletRequest request) {
//        LocalDateTime start = rangeStart != null ? LocalDateTime.parse(rangeStart, dateFormatter) : null;
//        LocalDateTime end = rangeEnd != null ? LocalDateTime.parse(rangeEnd, dateFormatter) : null;
//
//        Sort sortCriteria = Sort.by(Sort.Direction.ASC, "eventDate");
//        if (sort != null && sort.equals(SortValue.VIEWS)) {
//            sortCriteria = Sort.by(Sort.Direction.DESC, "views");
//        }
//
//        Pageable pageable = PageRequest.of(from / size, size, sortCriteria);
//
//        List<Event> events = eventRepository.findEventsWithParams(text, categories, paid, start, end, pageable);
//
//        if (onlyAvailable) {
//            events = events.stream()
//                    .filter(event -> event.getConfirmedRequests() < (long) event.getParticipantLimit())
//                    .collect(Collectors.toList());
//        }
//
//        if (events.isEmpty()) {
//            return Collections.emptyList();
//        }
//
//        setView(events);
//        sendStat(events, request);
//        return eventMapper.toEventFullDtoList(events);
//    }

    @Override
    public EventFullDto getEvent(Long eventId, HttpServletRequest request) {
        Event event = findEvent(eventId);
        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new DataNotFoundException(String.format("Event with id %d not published.", eventId));
        }
        createHit(request.getRequestURI(), request.getRemoteAddr());
        setViews(event);
        log.info("Get event with id {}", eventId);
        return mapper.toEventFullDto(event);
    }

    private void createHit(String uri, String ip) {
        EndpointHitDto hit = new EndpointHitDto();
        hit.setApp("ewm-main-service");
        hit.setIp(ip);
        hit.setUri(uri);
        hit.setTimestamp(LocalDateTime.now());
        statClient.addStats(hit);
    }

    private void setViews(Event event) {
        String start = event.getCreatedOn().format(dateFormatter);
        String end = LocalDateTime.now().format(dateFormatter);
        List<ViewStatsDto> stats = statClient.getStats(start, end, List.of("/events/" + event.getId()), true);
        if (stats.isEmpty()) {
            event.setViews(0);
        } else {
            event.setViews(stats.get(0).getHits());
        }
    }

    public void saveStat(Event event, HttpServletRequest request) {
        String address = request.getRemoteAddr();
        String name = "main-service";
        EndpointHitDto requestDto = createEndpointHitDto(LocalDateTime.now(), "/events", name, address);
        statClient.addStats(requestDto);
//        saveStatForEvent(event.getId(), address, LocalDateTime.now(), name);

    }

    public void saveStat(List<Event> events, HttpServletRequest request) {
        String address = request.getRemoteAddr();
        String name = "main-service";
        EndpointHitDto requestDto = createEndpointHitDto(LocalDateTime.now(), "/events", name, address);
        statClient.addStats(requestDto);
        events.forEach(event -> saveStatForEvent(event.getId(), address, LocalDateTime.now(), name));
    }

    private void saveStatForEvent(Long eventId, String address, LocalDateTime now, String name) {
        EndpointHitDto requestDto = createEndpointHitDto(now, "/events/" + eventId, name, address);
        statClient.addStats(requestDto);
    }

    private EndpointHitDto createEndpointHitDto(LocalDateTime timestamp, String uri, String app, String ip) {
        EndpointHitDto dto = new EndpointHitDto();
        dto.setTimestamp(timestamp);
        dto.setUri(uri);
        dto.setApp(app);
        dto.setIp(ip);
        return dto;
    }



//    public void setView(List<Event> events) {
//        if (events.isEmpty()) {
//            return;
//        }
//        LocalDateTime start = events.stream().map(Event::getCreatedOn).min(LocalDateTime::compareTo).orElse(LocalDateTime.now());
//        List<String> uris = events.stream().map(event -> "/events/" + event.getId()).collect(Collectors.toList());
//        Map<String, Event> eventsUri = events.stream().collect(Collectors.toMap(event -> "/events/" + event.getId(), event -> event));
//        events.forEach(event -> event.setViews(0L));
//        String startTime = start.format(dateFormatter);
//        String endTime = LocalDateTime.now().format(dateFormatter);
//        List<ViewStatsDto> stats = getStats(startTime, endTime, uris);
//        stats.forEach(stat -> eventsUri.get(stat.getUri()).setViews(stat.getHits()));
//    }

//    public void setView(Event event) {
//        String startTime = event.getCreatedOn().format(dateFormatter);
//        String endTime = LocalDateTime.now().format(dateFormatter);
//        List<String> uris = List.of("/events/" + event.getId());
//        List<ViewStatsDto> stats = getStats(startTime, endTime, uris);
//        event.setViews(stats.isEmpty() ? 0L : stats.get(0).getHits());
//    }

//    private List<ViewStatsDto> getStats(String startTime, String endTime, List<String> uris) {
//        return statClient.getStats(startTime, endTime, uris, false);
//    }

    private User findUser(Long userId) {
        return userRepo.findById(userId)
                .orElseThrow(() -> new DataNotFoundException(String.format("User with id %d not found.", userId)));
    }

    private Event findEvent(Long eventId) {
        return eventRepo.findByIdWithAllParams(eventId)
                .orElseThrow(() -> new DataNotFoundException(String.format("Event with id %d not found.", eventId)));
    }

    private void validateTime(Event event) {
        if (event.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new DataConflictException("Event date should be 2 hours ahead creation date.");
        }
    }




//        public void sendStat(Event event, HttpServletRequest request) {
//            String remoteAddr = request.getRemoteAddr();
//            String nameService = "main-service";
//            LocalDateTime now = LocalDateTime.now();
//
//            sendStat("/events", remoteAddr, nameService, now);
//            sendStat("/events/" + event.getId(), remoteAddr, nameService, now);
//        }
//
//        public void sendStat(List<Event> events, HttpServletRequest request) {
//            String remoteAddr = request.getRemoteAddr();
//            String nameService = "main-service";
//            LocalDateTime now = LocalDateTime.now();
//
//            sendStat("/events", remoteAddr, nameService, now);
//            events.forEach(event -> sendStat("/events/" + event.getId(), remoteAddr, nameService, now));
//        }
//
//        private void sendStat(String uri, String remoteAddr, String nameService, LocalDateTime now) {
//            EndpointHitDto requestDto = new EndpointHitDto();
//            requestDto.setTimestamp(LocalDateTime.parse(now.format(dateFormatter)));
//            requestDto.setUri(uri);
//            requestDto.setApp(nameService);
//            requestDto.setIp(remoteAddr);
//            statClient.addStats(requestDto);
//        }
//
//        public void setView(List<Event> events) {
//            if (events.isEmpty()) {
//                return;
//            }
//
//            LocalDateTime start = events.stream()
//                    .map(Event::getCreatedOn)
//                    .min(LocalDateTime::compareTo)
//                    .orElse(LocalDateTime.now());
//
//            List<String> uris = new ArrayList<>();
//            Map<String, Event> eventsUri = new HashMap<>();
//            events.forEach(event -> {
//                String uri = "/events/" + event.getId();
//                uris.add(uri);
//                eventsUri.put(uri, event);
//                event.setViews(0L);
//            });
//
//            String startTime = start.format(dateFormatter);
//            String endTime = LocalDateTime.now().format(dateFormatter);
//
//            List<ViewStatsDto> stats = getStats(startTime, endTime, uris);
//            stats.forEach(stat -> eventsUri.get(stat.getUri()).setViews(stat.getHits()));
//        }
//
//        public void setView(Event event) {
//            String startTime = event.getCreatedOn().format(dateFormatter);
//            String endTime = LocalDateTime.now().format(dateFormatter);
//            List<String> uris = List.of("/events/" + event.getId());
//
//            List<ViewStatsDto> stats = getStats(startTime, endTime, uris);
//            event.setViews(stats.stream().findFirst().map(ViewStatsDto::getHits).orElse(0L));
//        }
//
//        private List<ViewStatsDto> getStats(String startTime, String endTime, List<String> uris) {
//            return statClient.getStats(startTime, endTime, uris, false);
//        }

    public void sendStat(List<Event> events, HttpServletRequest request) {
        LocalDateTime now = LocalDateTime.now();
        String remoteAddr = request.getRemoteAddr();
        String nameService = "main-service";

        EndpointHitDto requestDto = new EndpointHitDto();
        requestDto.setTimestamp(LocalDateTime.parse(now.format(dateFormatter)));
        requestDto.setUri("/events");
        requestDto.setApp(nameService);
        requestDto.setIp(request.getRemoteAddr());
        statClient.addStats(requestDto);
        sendStatForEveryEvent(events, remoteAddr, LocalDateTime.now(), nameService);
    }

    public void sendStat(Event event, HttpServletRequest request) {
        LocalDateTime now = LocalDateTime.now();
        String remoteAddr = request.getRemoteAddr();
        String nameService = "main-service";

        EndpointHitDto requestDto = new EndpointHitDto();
        requestDto.setTimestamp(now);
        requestDto.setUri("/events");
        requestDto.setApp(nameService);
        requestDto.setIp(remoteAddr);
        statClient.addStats(requestDto);
        sendStatForTheEvent(event.getId(), remoteAddr, now, nameService);
    }

    private void sendStatForTheEvent(Long eventId, String remoteAddr, LocalDateTime now,
                                     String nameService) {
        EndpointHitDto requestDto = new EndpointHitDto();
        requestDto.setTimestamp(now);
        requestDto.setUri("/events/" + eventId);
        requestDto.setApp(nameService);
        requestDto.setIp(remoteAddr);
        statClient.addStats(requestDto);
    }

    private void sendStatForEveryEvent(List<Event> events, String remoteAddr, LocalDateTime now,
                                       String nameService) {
        for (Event event : events) {
            EndpointHitDto requestDto = new EndpointHitDto();
            requestDto.setTimestamp(now);
            requestDto.setUri("/events/" + event.getId());
            requestDto.setApp(nameService);
            requestDto.setIp(remoteAddr);
            statClient.addStats(requestDto);
        }
    }

    public void setView(List<Event> events) {
        LocalDateTime start = events.get(0).getCreatedOn();
        List<String> uris = new ArrayList<>();
        Map<String, Event> eventsUri = new HashMap<>();
        String uri = "";
        for (Event event : events) {
            if (start.isBefore(event.getCreatedOn())) {
                start = event.getCreatedOn();
            }
            uri = "/events/" + event.getId();
            uris.add(uri);
            eventsUri.put(uri, event);
            event.setViews(0L);
        }

        String startTime = start.format(DateTimeFormatter.ofPattern(DateTimePattern.PATTERN));
        String endTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern(DateTimePattern.PATTERN));

        List<ViewStatsDto> stats = getStats(startTime, endTime, uris);
        stats.forEach((stat) ->
                eventsUri.get(stat.getUri()).setViews(stat.getHits()));
    }

    public void setView(Event event) {
        String startTime = event.getCreatedOn().format(dateFormatter);
        String endTime = LocalDateTime.now().format(dateFormatter);
        List<String> uris = List.of("/events/" + event.getId());

        List<ViewStatsDto> stats = getStats(startTime, endTime, uris);
        if (stats.size() == 1) {
            event.setViews(stats.get(0).getHits());
        } else {
            event.setViews(0L);
        }
    }

    private List<ViewStatsDto> getStats(String startTime, String endTime, List<String> uris) {
        return statClient.getStats(startTime, endTime, uris, false);
    }
//    public List<ViewStatsDto> getEventStats(Long eventId) {
//        String uri = "/events/" + eventId;
//        // Example: start and end dates are hardcoded, adjust as necessary
//        String start = "2023-01-01T00:00:00";
//        String end = "2023-12-31T23:59:59";
//        return getStats(start, end, Collections.singletonList(uri), true);
//    }

    }



