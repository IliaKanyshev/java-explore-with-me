package ru.practicum.ewm.dao;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.ewm.model.Comment;
import ru.practicum.ewm.model.Event;
import ru.practicum.ewm.model.User;

import java.util.List;

@Repository
public interface CommentRepo  extends JpaRepository<Comment, Long> {
  List<Comment> findAllByEventId(Long eventId, Pageable page);

  List<Comment> findAllByAuthorId(Long userId, Pageable pageable);
}
