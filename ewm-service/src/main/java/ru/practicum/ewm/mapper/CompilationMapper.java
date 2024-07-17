package ru.practicum.ewm.mapper;

import org.mapstruct.Mapper;
import ru.practicum.ewm.dto.compilation.CompilationDto;
import ru.practicum.ewm.model.Compilation;

import java.util.List;

//@Component
@Mapper(componentModel = "spring")
public interface CompilationMapper {
    CompilationDto toCompilationDto(Compilation compilation);

    List<CompilationDto> toListCompilationDto(List<Compilation> compilations);
}
