package ru.practicum.ewm.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.ewm.model.Request;
import ru.practicum.ewm.util.enums.RequestStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface RequestRepo extends JpaRepository<Request, Long> {
    Request findByRequesterIdAndEventId(Long userId, Long eventId);

    Optional<Request> findByRequesterIdAndId(Long userId, Long requestId);

    List<Request> findAllByRequesterId(Long userId);

    List<Request> findAllByIdIn(List<Long> ids);

    Long countByEventId(Long eventId);

    int countAllByEventIdAndStatus(Long eventId, RequestStatus status);

    @Query("select r from Request as r " +
            "join Event as e ON r.event.id = e.id " +
            "where r.event.id = :eventId and e.initiator.id = :userId")
    List<Request> findAllByEventWithInitiator(@Param(value = "userId") Long userId,
                                              @Param("eventId") Long eventId);

//    @Query("select r from Request r " +
//            "join r.event e " +
//            "where e.id = :eventId and e.initiator.id = :userId")
//    List<Request> findAllByEventWithInitiator(@Param("userId") Long userId,
//                                              @Param("eventId") Long eventId);
}
