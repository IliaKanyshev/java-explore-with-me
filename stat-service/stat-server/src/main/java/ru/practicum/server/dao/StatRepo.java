package ru.practicum.server.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.server.model.EndpointHit;
import ru.practicum.server.model.ViewStats;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StatRepo extends JpaRepository<EndpointHit, Long> {
    @Query(value = "select new ru.practicum.server.model.ViewStats(e.app, e.uri, count(e.ip)) " +
            "from  EndpointHit e " +
            "where e.timestamp between :start and :end " +
            "and e.uri in (:uris) " +
            "group by e.app, e.uri " +
            "order by count(distinct e.ip) desc")
    List<ViewStats> getStatsByUrisAndIp(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, @Param("uris") List<String> uris);

    @Query(value = "select new ru.practicum.server.model.ViewStats(e.app, e.uri, count(e.ip)) " +
            "from  EndpointHit e " +
            "where e.timestamp between :start and :end " +
            "and e.uri in (:uris) " +
            "group by e.app, e.uri " +
            "order by count(e.ip) desc")
    List<ViewStats> getStatsByUris(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, @Param("uris") List<String> uris);
}
