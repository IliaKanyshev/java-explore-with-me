package ru.practicum.ewm.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dao.EventRepo;
import ru.practicum.ewm.dao.RequestRepo;
import ru.practicum.ewm.dao.UserRepo;
import ru.practicum.ewm.dto.request.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.dto.request.EventRequestStatusUpdateResult;
import ru.practicum.ewm.dto.request.RequestDto;
import ru.practicum.ewm.exception.DataConflictException;
import ru.practicum.ewm.exception.DataNotFoundException;
import ru.practicum.ewm.mapper.RequestMapper;
import ru.practicum.ewm.model.Event;
import ru.practicum.ewm.model.Request;
import ru.practicum.ewm.model.User;
import ru.practicum.ewm.service.RequestService;
import ru.practicum.ewm.util.enums.EventState;
import ru.practicum.ewm.util.enums.RequestStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RequestServiceImpl implements RequestService {
    private final RequestRepo requestRepo;
    private final EventRepo eventRepo;
    private final UserRepo userRepo;
    private final RequestMapper mapper;

    @Override
    public RequestDto saveRequest(Long userId, Long eventId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new DataNotFoundException("User not found."));

        Event event = eventRepo.findByIdWithAllParams(eventId)
                .orElseThrow(() -> new DataNotFoundException(String.format("Event with id %d not found.", eventId)));

        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new DataConflictException("Event is not published.");
        }

        long requestCount = requestRepo.countByEventId(eventId);
        if (event.getParticipantLimit() != 0 && requestCount >= event.getParticipantLimit()) {
            throw new DataConflictException("Participant limit reached.");
        }

        if (event.getInitiator().getId().equals(userId)) {
            throw new DataConflictException("Initiator cannot create a request.");
        }

        if (requestRepo.findByRequesterIdAndEventId(userId, eventId) != null) {
            throw new DataConflictException("Request already exists.");
        }

        Request request = new Request();
        request.setCreated(LocalDateTime.now());
        request.setEvent(event);
        request.setRequester(user);
        request.setStatus(event.getRequestModeration() && event.getParticipantLimit() != 0 ? RequestStatus.PENDING : RequestStatus.CONFIRMED);
        final Request savedRequest = requestRepo.save(request);
        if (request.getStatus().equals(RequestStatus.CONFIRMED)) {
            event.setConfirmedRequests(event.getConfirmedRequests() + 1);
            eventRepo.save(event);
        }
        log.info("Request from user with id {} for event with id {} saved.", userId, eventId);
        return mapper.toRequestDto(savedRequest);
    }

    @Override
    public List<RequestDto> getCurrentUserRequests(Long userId) {
        userRepo.findById(userId).orElseThrow(() -> new DataNotFoundException(String.format("User with id %d  not found", userId)));
        log.info("Getting requests list for user with id {}", userId);
        return mapper.toRequestDtoList(requestRepo.findAllByRequesterId(userId));
    }

    @Override
    public List<RequestDto> getRequestsByOwner(Long userId, Long eventId) {
        log.info("Getting requests list for event with id {} by owner with id {}.", eventId, userId);
        return mapper.toRequestDtoList(requestRepo.findAllByEventWithInitiator(userId, eventId));
    }

    @Override
    public RequestDto cancelRequest(Long userId, Long requestId) {
        Request request = requestRepo.findByRequesterIdAndId(userId, requestId)
                .orElseThrow(() -> new DataNotFoundException(String.format("Request with id %d not found.", requestId)));
        request.setStatus(RequestStatus.CANCELED);
        return mapper.toRequestDto(requestRepo.save(request));
    }

    @Transactional
    @Override
    public EventRequestStatusUpdateResult updateRequests(Long userId, Long eventId, EventRequestStatusUpdateRequest request) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new DataNotFoundException("User not found."));
        Event event = eventRepo.findByIdWithAllParams(eventId)
                .orElseThrow(() -> new DataNotFoundException(String.format("Event with id %d not found.", eventId)));

        if (!user.getId().equals(event.getInitiator().getId())) {
            throw new DataConflictException(String.format("User %d is not the initiator of the event %d.", userId, eventId));
        }

        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            return EventRequestStatusUpdateResult.builder()
                    .confirmedRequests(Collections.emptyList())
                    .rejectedRequests(Collections.emptyList())
                    .build();
        }

        long availableSpaces = event.getParticipantLimit() - event.getConfirmedRequests();
        if (availableSpaces <= 0) {
            throw new DataConflictException("Participant limit has been violated.");
        }

        List<Request> requestsList = requestRepo.findAllByIdIn(request.getRequestIds());

        List<Request> confirmedRequests = new ArrayList<>();
        List<Request> rejectedRequests = new ArrayList<>();

        for (Request r : requestsList) {
            if (!r.getStatus().equals(RequestStatus.PENDING)) {
                throw new DataConflictException("Request status must be PENDING.");
            }

            if (request.getStatus().equals(RequestStatus.CONFIRMED) && availableSpaces > 0) {
                r.setStatus(RequestStatus.CONFIRMED);
                confirmedRequests.add(r);
                availableSpaces--;
            } else {
                r.setStatus(RequestStatus.REJECTED);
                rejectedRequests.add(r);
            }
        }

        event.setConfirmedRequests(requestRepo.countAllByEventIdAndStatus(eventId, RequestStatus.CONFIRMED));

        EventRequestStatusUpdateResult result = EventRequestStatusUpdateResult.builder()
                .confirmedRequests(mapper.toRequestDtoList(confirmedRequests))
                .rejectedRequests(mapper.toRequestDtoList(rejectedRequests))
                .build();

        eventRepo.save(event);
        requestRepo.saveAll(requestsList);

        return result;
    }
//        Event event = eventRepo.findById(eventId).orElseThrow(() -> new DataNotFoundException("Event doesn't exist"));
//        EventRequestStatusUpdateResult result = new EventRequestStatusUpdateResult();
//
//        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
//            return result;
//        }
//
//        List<Request> requests = requestRepo.findAllByEventWithInitiator(userId, eventId);
//        List<Request> requestsToUpdate = requests.stream().filter(r -> request.getRequestIds().contains(r.getId())).collect(Collectors.toList());
//
//        if (requestsToUpdate.stream().anyMatch(r -> r.getStatus().equals(RequestStatus.CONFIRMED) && request.getStatus().equals(RequestStatus.REJECTED))) {
//            throw new DataConflictException("request already confirmed");
//        }
//
//        if (event.getConfirmedRequests() + requestsToUpdate.size() > event.getParticipantLimit() && request.getStatus().equals(RequestStatus.CONFIRMED)) {
//            throw new DataConflictException("exceeding the limit of participants");
//        }
//
//        for (Request req : requestsToUpdate) {
//            req.setStatus(RequestStatus.valueOf(request.getStatus().toString()));
//        }
//
//        requestRepo.saveAll(requestsToUpdate);
//
//        if (request.getStatus().equals(RequestStatus.CONFIRMED)) {
//            event.setConfirmedRequests(event.getConfirmedRequests() + requestsToUpdate.size());
//        }
//
//        eventRepo.save(event);
//
//        if (request.getStatus().equals(RequestStatus.CONFIRMED)) {
//            result.setConfirmedRequests(mapper.toRequestDtoList(requestsToUpdate));
//        }
//
//        if (request.getStatus().equals(RequestStatus.REJECTED)) {
//            result.setRejectedRequests(mapper.toRequestDtoList(requestsToUpdate));
//        }
//
//        return result;
//    }
}
