package ru.practicum.server.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.server.model.EndpointHit;
import ru.practicum.server.util.DateTimePattern;

@Mapper(componentModel = "spring")
public interface EndpointHitMapper {
    @Mapping(target = "timestamp", source = "timestamp", dateFormat = DateTimePattern.Pattern)
    EndpointHit toEntity(EndpointHitDto endpointHitDto);
}
