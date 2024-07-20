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

        if (event.getParticipantLimit() != 0 && event.getParticipantLimit() <= event.getConfirmedRequests()) {
            throw new DataConflictException("Participants limit reached.");
        }

        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new DataConflictException("Event is not published.");
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
        if (savedRequest.getStatus().equals(RequestStatus.CONFIRMED)) {
            event.setConfirmedRequests(requestRepo.countAllByEventIdAndStatus(eventId, RequestStatus.CONFIRMED));
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
        Event event = eventRepo.findByIdWithAllParams(eventId)
                .orElseThrow(() -> new DataNotFoundException(String.format("Event with id %d not found.", eventId)));

        if (!event.getInitiator().getId().equals(userId)) {
            throw new DataConflictException(String.format("User %d is not the initiator of the event %d.", userId, eventId));
        }

        List<Request> requestsList = requestRepo.findAllByIdIn(request.getRequestIds());
        List<Request> confirmedRequests = new ArrayList<>();
        List<Request> rejectedRequests = new ArrayList<>();

        EventRequestStatusUpdateResult result = EventRequestStatusUpdateResult.builder()
                .confirmedRequests(Collections.emptyList())
                .rejectedRequests(Collections.emptyList())
                .build();

        for (Request req : requestsList) {
            if (req.getStatus().equals(RequestStatus.CONFIRMED)) {
                throw new DataConflictException("Request already confirmed.");
            }
            if (request.getStatus().equals(RequestStatus.REJECTED)) {
                req.setStatus(RequestStatus.REJECTED);
                rejectedRequests.add(req);
            } else if (request.getStatus().equals(RequestStatus.CONFIRMED) && req.getStatus().equals(RequestStatus.PENDING)) {
                if (event.getConfirmedRequests() >= event.getParticipantLimit()) {
                    throw new DataConflictException("Participant limit has been violated.");
                }
                req.setStatus(RequestStatus.CONFIRMED);
                confirmedRequests.add(req);
                event.setConfirmedRequests(event.getConfirmedRequests() + 1);
            }
        }

        eventRepo.save(event);
        requestRepo.saveAll(requestsList);

        result.setConfirmedRequests(mapper.toRequestDtoList(confirmedRequests));
        result.setRejectedRequests(mapper.toRequestDtoList(rejectedRequests));

        return result;
    }
}
