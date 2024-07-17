package ru.practicum.ewm.mapper;

import org.mapstruct.Mapper;
import ru.practicum.ewm.dto.compilation.CompilationDto;
import ru.practicum.ewm.model.Compilation;

import java.util.List;

//@Component
@Mapper(componentModel = "spring")
public interface CompilationMapper {
    CompilationDto ToCompilationDto(Compilation compilation);

    List<CompilationDto> ToListCompilationDto(List<Compilation> compilations);
}
