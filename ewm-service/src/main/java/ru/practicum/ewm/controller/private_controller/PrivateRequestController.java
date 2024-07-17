package ru.practicum.ewm.controller.private_controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.request.RequestDto;
import ru.practicum.ewm.service.RequestService;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}/requests")
@RequiredArgsConstructor
@Slf4j
public class PrivateRequestController {
    private final RequestService requestService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RequestDto createRequest(@PathVariable(name = "userId") Long userId, @RequestParam(name = "eventId") Long eventId) {
        log.info("POST request /users/{userId}/requests");
        return requestService.saveRequest(userId, eventId);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<RequestDto> getCurrentUserRequests(@PathVariable(name = "userId") Long userId) {
        log.info("GET request /users/{userId}/requests");
        return requestService.getCurrentUserRequests(userId);
    }

    @PatchMapping("/{requestId}/cancel")
    @ResponseStatus(HttpStatus.OK)
    public RequestDto cancelRequest(@PathVariable(name = "userId") Long userId, @PathVariable Long requestId) {
        log.info("PATCH request /users/{userId}/requests/{requestId}/cancel");
        return requestService.cancelRequest(userId, requestId);
    }
}
