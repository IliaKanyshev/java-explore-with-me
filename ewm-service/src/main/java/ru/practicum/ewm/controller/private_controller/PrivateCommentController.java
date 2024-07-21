package ru.practicum.ewm.controller.private_controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.comment.CommentDto;
import ru.practicum.ewm.dto.comment.NewCommentDto;
import ru.practicum.ewm.service.CommentService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@RequestMapping("/users/{userId}/comments")
@RequiredArgsConstructor
@Slf4j
public class PrivateCommentController {
    private final CommentService service;

    @PostMapping("/events/{eventId}")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto saveComment(@PathVariable(value = "userId") Long userId,
                                  @PathVariable(value = "eventId") Long eventId,
                                  @Valid @RequestBody NewCommentDto dto) {
        log.info("POST request /users/{userId}/comments/{eventId}");
        return service.saveComment(userId, eventId, dto);
    }

    @PatchMapping("/{commentId}")
    @ResponseStatus(HttpStatus.OK)
    public CommentDto updateComment(@PathVariable(value = "userId") Long userId,
                                    @PathVariable(value = "commentId") Long commentId,
                                    @Valid @RequestBody NewCommentDto dto) {
        log.info("PATCH request /users/{userId}/comments/{commentId}");
        return service.updateCommentByUser(userId, commentId, dto);
    }

    @GetMapping("/{commentId}")
    @ResponseStatus(HttpStatus.OK)
    public CommentDto getCommentById(@PathVariable(value = "commentId") Long commentId,
                                     @PathVariable(value = "userId") Long userId) {
        log.info("GET request /users/{userId}/comments/{commentId}");
        return service.getCommentByUser(commentId, userId);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<CommentDto> getUserComments(@PathVariable Long userId,
                                            @PositiveOrZero
                                            @RequestParam(value = "from", defaultValue = "0") Integer from,
                                            @Positive
                                            @RequestParam(value = "size", defaultValue = "10") Integer size) {
        log.info("GET request /users/{userId}/comments");
        return service.getUserComments(userId, from, size);
    }


    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCommentByUser(@PathVariable(value = "userId") Long userId,
                                    @PathVariable(value = "commentId") Long commentId) {
        log.info("DELETE request /users/{userId}/comments/{commentId}");
        service.deleteCommentByUser(userId, commentId);
    }

}
