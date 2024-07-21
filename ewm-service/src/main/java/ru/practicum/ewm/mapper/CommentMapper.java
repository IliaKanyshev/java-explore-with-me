package ru.practicum.ewm.mapper;

import org.mapstruct.Mapper;
import ru.practicum.ewm.dto.comment.CommentDto;
import ru.practicum.ewm.model.Comment;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CommentMapper {
    CommentDto toCommentDto(Comment comment);
    List<CommentDto> toCommentDtoList(List<Comment> comments);
}
