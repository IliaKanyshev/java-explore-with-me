package ru.practicum.ewm.service;


import ru.practicum.ewm.dto.comment.CommentDto;
import ru.practicum.ewm.dto.comment.NewCommentDto;

import java.util.List;

public interface CommentService {
    CommentDto saveComment(Long userId, Long eventId, NewCommentDto dto);

    CommentDto updateCommentByUser(Long userId, Long commentId, NewCommentDto dto);

    CommentDto updateCommentByAdmin(Long commentId, NewCommentDto dto);

    CommentDto getCommentByUser(Long commentId, Long userId);

    CommentDto getCommentByAdmin(Long commentId);

    void deleteCommentByUser(Long userId, Long commentId);

    void deleteCommentByAdmin(Long commentId);

    List<CommentDto> getCommentsByEventId(Long eventId, Integer from, Integer size);

    List<CommentDto> getUserComments(Long userId, Integer from, Integer size);
}
