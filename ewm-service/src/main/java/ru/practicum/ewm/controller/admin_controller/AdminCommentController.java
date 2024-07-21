package ru.practicum.ewm.controller.admin_controller;

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
@RequestMapping("/admin/comments")
@RequiredArgsConstructor
@Slf4j
public class AdminCommentController {
    private final CommentService service;

    @PatchMapping("/{commentId}")
    @ResponseStatus(HttpStatus.OK)
    public CommentDto updateComment(@PathVariable(value = "commentId") Long commentId, @Valid @RequestBody NewCommentDto dto) {
        log.info("PATCH request /admin/comments/{commentId}.");
        return service.updateCommentByAdmin(commentId, dto);
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable(value = "commentId") Long commentId) {
        log.info("DELETE request /admin/comments/{commentId}");
        service.deleteCommentByAdmin(commentId);
    }

    @GetMapping("/{commentId}")
    @ResponseStatus(HttpStatus.OK)
    public CommentDto getComment(@PathVariable(value = "commentId") Long commentId) {
        log.info("GET request /admin/comments/{commentId}.");
        return service.getCommentByAdmin(commentId);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<CommentDto> getCommentsByEventId(@RequestParam(value = "eventId") Long eventId,
                                                 @PositiveOrZero
                                                 @RequestParam(value = "from", defaultValue = "0") Integer from,
                                                 @Positive
                                                 @RequestParam(value = "size", defaultValue = "10") Integer size) {
        log.info("GET request /admin/comments.");
        return service.getCommentsByEventId(eventId, from, size);
    }
}
