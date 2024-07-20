package ru.practicum.ewm.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.ewm.model.Event;
import ru.practicum.ewm.util.enums.EventState;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepo extends JpaRepository<Event, Long> {
    Boolean findByCategoryId(Long catId);

    Optional<Event> findByIdAndInitiatorId(Long eventId, Long userId);

    Optional<Event> findByIdAndPublishedOnIsNotNull(Long id);

    Page<Event> findAllByInitiatorId(Long userId, Pageable page);

    List<Event> findAllByCategoryId(Long id);

    @Query("select e from Event e " +
            "JOIN FETCH e.initiator " +
            "JOIN FETCH e.category " +
            "JOIN fetch e.location " +
            "WHERE e.id in :ids")
    List<Event> findAllByIdIn(List<Long> ids);

    @Query("SELECT e FROM Event e " +
            "WHERE (:categories IS NULL OR e.category.id IN :categories) " +
            "AND (:users IS NULL OR e.initiator.id IN :users) " +
            "AND (:states IS NULL OR e.state IN :states) " +
            "AND (:rangeStart IS NULL OR e.eventDate >= :rangeStart) " +
            "AND (:rangeEnd IS NULL OR e.eventDate <= :rangeEnd)")
    List<Event> findEventsWithParamsByAdmin(
            @Param("users") List<Long> users,
            @Param("states") EventState states,
            @Param("categories") List<Long> categories,
            @Param("rangeStart") LocalDateTime rangeStart,
            @Param("rangeEnd") LocalDateTime rangeEnd,
            Pageable pageable);

    @Query("select e from Event e " +
            "JOIN FETCH e.initiator " +
            "JOIN FETCH e.category " +
            "JOIN fetch e.location " +
            "WHERE e.id = :id")
    Optional<Event> findByIdWithAllParams(@Param("id") Long id);

    @Query(value = "SELECT e FROM Event AS e " +
            "WHERE (e.state = 'PUBLISHED') " +
            "AND (:text IS NULL) " +
            "OR (LOWER(e.annotation) LIKE LOWER(CONCAT('%', :text, '%'))) " +
            "OR (LOWER(e.description) LIKE LOWER(CONCAT('%', :text, '%'))) " +
            "OR (LOWER(e.title) LIKE LOWER(CONCAT('%', :text, '%'))) " +
            "AND (:categories IS NULL OR e.category.id IN :categories) " +
            "AND (:paid IS NULL OR e.paid = :paid) " +
            "OR (CAST(:rangeStart AS date) IS NULL AND CAST(:rangeStart AS date) IS NULL)" +
            "OR (CAST(:rangeStart AS date) IS NULL AND e.eventDate < CAST(:rangeEnd AS date)) " +
            "OR (CAST(:rangeEnd AS date) IS NULL AND e.eventDate > CAST(:rangeStart AS date)) " +
            "AND (e.confirmedRequests < e.participantLimit OR :onlyAvailable = FALSE)" +
            "GROUP BY e.id " +
            "ORDER BY LOWER(:sort) ASC")
    List<Event> findEventsWithParamsByUser(@Param("text") String text,
                                           @Param("categories") List<Long> categories,
                                           @Param("paid") Boolean paid,
                                           @Param("rangeStart") LocalDateTime rangeStart,
                                           @Param("rangeEnd") LocalDateTime rangeEnd,
                                           @Param("onlyAvailable") Boolean onlyAvailable,
                                           @Param("sort") String sort,
                                           PageRequest pageRequest);

    @Query("SELECT e from Event e " +
            "WHERE (:users is null or e.initiator.id in :users) " +
            "AND (:states is null or e.state in :states) " +
            "AND (:categories is null or e.category.id in :categories) " +
            "AND e.eventDate > :rangeStart " +
            "AND e.eventDate < :rangeEnd")
    List<Event> findEventsWithParams(List<Long> users, List<EventState> states, List<Long> categories,
                                     LocalDateTime rangeStart, LocalDateTime rangeEnd, Pageable page);
}
