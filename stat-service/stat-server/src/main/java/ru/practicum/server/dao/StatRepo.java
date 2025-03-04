package ru.practicum.server.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.server.model.EndpointHit;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StatRepo extends JpaRepository<EndpointHit, Long> {

    List<EndpointHit> findAllByUriInAndTimestampBetween(List<String> uris, LocalDateTime start, LocalDateTime end);

    List<EndpointHit> findAllByTimestampBetween(LocalDateTime start, LocalDateTime end);

    List<EndpointHit> findFirstDistinctByUriInAndTimestampBetween(List<String> uris, LocalDateTime start,
                                                                  LocalDateTime end);
}
