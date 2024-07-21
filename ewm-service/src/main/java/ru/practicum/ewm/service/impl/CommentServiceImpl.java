package ru.practicum.ewm.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dao.CommentRepo;
import ru.practicum.ewm.dao.EventRepo;
import ru.practicum.ewm.dao.UserRepo;
import ru.practicum.ewm.dto.comment.CommentDto;
import ru.practicum.ewm.dto.comment.NewCommentDto;
import ru.practicum.ewm.exception.DataConflictException;
import ru.practicum.ewm.exception.DataNotFoundException;
import ru.practicum.ewm.mapper.CommentMapper;
import ru.practicum.ewm.model.Comment;
import ru.practicum.ewm.model.Event;
import ru.practicum.ewm.model.User;
import ru.practicum.ewm.service.CommentService;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentServiceImpl implements CommentService {

    private final UserRepo userRepo;
    private final EventRepo eventRepo;
    private final CommentRepo commentRepo;
    private final CommentMapper mapper;

@Override
    public CommentDto saveComment(Long userId, Long eventId, NewCommentDto dto) {
        User user = findUser(userId);
        Event event = findEvent(eventId);

    Comment comment = new Comment();
    comment.setText(dto.getText());
    comment.setAuthor(user);
    comment.setEvent(event);
    comment.setCreatedOn(LocalDateTime.now());
    log.info("New comment from user {} for event {} was added.", userId, eventId);
    return mapper.toCommentDto(commentRepo.save(comment));
}

@Override
@Transactional
public CommentDto updateCommentByUser(Long userId, Long commentId, NewCommentDto dto) {
    findUser(userId);
    Comment comment = findComment(commentId);
    if (!comment.getAuthor().getId().equals(userId)) {
        throw new DataConflictException("Only author can edit comment.");
    }
    comment.setText(dto.getText());
    log.info("Comment with id {} was updated.", commentId);
    return mapper.toCommentDto(commentRepo.save(comment));
}

@Override
@Transactional
public CommentDto updateCommentByAdmin(Long commentId, NewCommentDto dto) {
    Comment comment = findComment(commentId);
    comment.setText(dto.getText());
    log.info("Comment with id {} was updated by admin.", commentId);
    return mapper.toCommentDto(commentRepo.save(comment));
}

@Override
public CommentDto getCommentByUser(Long commentId, Long userId) {
    findUser(userId);
    Comment comment = findComment(commentId);
    if (!comment.getAuthor().getId().equals(userId)) {
        throw new DataConflictException("Cant get another user comment.");
    }
    log.info("User {} comment.", userId);
    return mapper.toCommentDto(comment);
}

@Override
public   CommentDto getCommentByAdmin(Long commentId) {
   Comment comment = findComment(commentId);
    log.info("Comment with id {}", commentId);
    return mapper.toCommentDto(comment);
}

@Override
public void deleteCommentByUser(Long userId, Long commentId) {
    findUser(userId);
    Comment comment = findComment(commentId);
    if (!comment.getAuthor().getId().equals(userId)) {
        throw new DataConflictException("Only author can delete comment.");
    }
    log.info("Comment {} was deleted.", commentId);
    commentRepo.delete(comment);
}

@Override
public void deleteCommentByAdmin(Long commentId) {
    findComment(commentId);
    log.info("Comment {} was deleted by admin", commentId);
    commentRepo.deleteById(commentId);
}

@Override
public  List<CommentDto> getCommentsByEventId(Long eventId, Integer from, Integer size) {
    findEvent(eventId);
    Pageable page = PageRequest.of(from/size, size);
    List<Comment> comments = commentRepo.findAllByEventId(eventId,page);
    log.info("Getting comments list for event {}.",eventId);
    return mapper.toCommentDtoList(comments);
}

@Override
public List<CommentDto> getUserComments(Long userId, Integer from, Integer size) {
    findUser(userId);
    Pageable page = PageRequest.of(from/size, size);
    List<Comment> comments = commentRepo.findAllByAuthorId(userId,page);
    log.info("Getting comments for user {}",userId);
    return mapper.toCommentDtoList(comments);
}

private User findUser(Long userId) {
    return userRepo.findById(userId)
            .orElseThrow(() -> new DataNotFoundException(String.format("User with id %d not found.", userId)));
}
private Event findEvent(Long eventId) {
    return eventRepo.findById(eventId)
            .orElseThrow(() -> new DataNotFoundException(String.format("Event with id %d not found.", eventId)));
}
private Comment findComment(Long commentId) {
    return commentRepo.findById(commentId)
            .orElseThrow(() -> new DataNotFoundException(String.format("Comment with id %d not found.", commentId)));
}
}
