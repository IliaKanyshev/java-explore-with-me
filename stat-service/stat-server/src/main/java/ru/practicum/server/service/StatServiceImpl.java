package ru.practicum.server.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.server.dao.StatRepo;
import ru.practicum.server.mapper.EndpointHitMapper;
import ru.practicum.server.model.EndpointHit;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatServiceImpl implements StatService {
    private final StatRepo statRepo;
    private final EndpointHitMapper hitMapper;

    @Override
    public EndpointHitDto createEndpointHitDto(EndpointHitDto dto) {
        EndpointHit hit = hitMapper.toEntity(dto);
        statRepo.save(hit);
        return hitMapper.toEndpointHitDto(hit);
    }

    @Override
    public List<ViewStatsDto> getStats(boolean unique, LocalDateTime start, LocalDateTime end, List<String> uris) {
        if (unique) {
            return viewStatsByHits(statRepo
                    .findFirstDistinctByUriInAndTimestampBetween(uris, start, end));
        }
        if (!uris.isEmpty()) {
            return viewStatsByHits(statRepo.findAllByUriInAndTimestampBetween(uris, start, end));
        } else {
            return viewStatsWithoutUris(statRepo.findAllByTimestampBetween(start, end));
        }
    }

    private List<ViewStatsDto> viewStatsWithoutUris(List<EndpointHit> hits) {
        Map<String, Long> urisCount = hits.stream()
                .collect(Collectors.groupingBy(EndpointHit::getUri, Collectors.counting()));

        List<ViewStatsDto> stats = new ArrayList<>();
        for (EndpointHit hit : hits) {
            ViewStatsDto stat = hitMapper.fromHitToViewStatsDto(hit);
            long uriCount = urisCount.get(stat.getUri());
            stat.setHits(uriCount);
            stats.add(stat);
        }

        return stats.stream()
                .sorted(Comparator.comparing(ViewStatsDto::getHits).reversed())
                .collect(Collectors.toList());
    }

    private List<ViewStatsDto> viewStatsByHits(List<EndpointHit> hits) {
        Map<String, Long> urisCount = hits.stream()
                .collect(Collectors.groupingBy(EndpointHit::getUri, Collectors.counting()));

        Set<ViewStatsDto> stats = new TreeSet<>(Comparator.comparing(ViewStatsDto::getHits).reversed());
        for (EndpointHit hit : hits) {
            ViewStatsDto stat = hitMapper.fromHitToViewStatsDto(hit);
            long uriCount = urisCount.get(stat.getUri());
            stat.setHits(uriCount);
            stats.add(stat);
        }

        return new ArrayList<>(stats);
    }
}
