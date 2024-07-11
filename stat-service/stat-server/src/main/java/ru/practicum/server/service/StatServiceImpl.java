package ru.practicum.server.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.server.dao.StatRepo;
import ru.practicum.server.mapper.EndpointHitMapper;
import ru.practicum.server.mapper.ViewStatsMapper;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatServiceImpl implements StatService {
    private final StatRepo statRepo;
    private final EndpointHitMapper hitMapper;
    private final ViewStatsMapper viewStatsMapper;

    @Override
    public void saveHit(EndpointHitDto endpointHitDto) {
        log.info("Add hit from app - " + endpointHitDto.getApp());
        statRepo.save(hitMapper.toEntity(endpointHitDto));
    }

    @Override
    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        log.info("Get stats.");
        return unique ? viewStatsMapper.toEntityList(statRepo.getStatsByUrisAndIp(start, end, uris))
                : viewStatsMapper.toEntityList(statRepo.getStatsByUris(start, end, uris));
    }
}
