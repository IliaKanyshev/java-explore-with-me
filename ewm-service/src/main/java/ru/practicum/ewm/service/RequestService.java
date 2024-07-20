package ru.practicum.ewm.service;

import ru.practicum.ewm.dto.request.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.dto.request.EventRequestStatusUpdateResult;
import ru.practicum.ewm.dto.request.RequestDto;

import java.util.List;

public interface RequestService {
    RequestDto saveRequest(Long userId, Long eventId);

    RequestDto cancelRequest(Long userId, Long requestId);

    EventRequestStatusUpdateResult updateRequests(Long userId, Long eventId, EventRequestStatusUpdateRequest requestStatusUpdate);

    List<RequestDto> getRequestsByOwner(Long userId, Long eventId);

    List<RequestDto> getCurrentUserRequests(Long userId);
}
