package ru.practicum.ewm.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dao.CompilationRepo;
import ru.practicum.ewm.dao.EventRepo;
import ru.practicum.ewm.dto.compilation.CompilationDto;
import ru.practicum.ewm.dto.compilation.NewCompilationDto;
import ru.practicum.ewm.dto.compilation.UpdateCompilationRequest;
import ru.practicum.ewm.exception.DataNotFoundException;
import ru.practicum.ewm.mapper.CompilationMapper;
import ru.practicum.ewm.model.Compilation;
import ru.practicum.ewm.model.Event;
import ru.practicum.ewm.service.CompilationService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompilationServiceImpl implements CompilationService {
    private final CompilationRepo compilationRepo;
    private final EventRepo eventRepo;
    private final CompilationMapper mapper;

    @Override
    @Transactional
    public CompilationDto saveCompilation(NewCompilationDto newCompilationDto) {
        List<Event> events = eventRepo.findAllByIdIn(newCompilationDto.getEvents());
        Compilation compilation = new Compilation();
        compilation.setEvents(events);
        compilation.setPinned(newCompilationDto.getPinned() != null && newCompilationDto.getPinned());
        compilation.setTitle(newCompilationDto.getTitle());
        Compilation savedComp = compilationRepo.save(compilation);
        log.info("Compilation was created");
        return mapper.ToCompilationDto(savedComp);
    }

    @Override
    public CompilationDto getCompilation(Long compId) {
        Compilation compilation = compilationRepo.findById(compId)
                .orElseThrow(() -> new DataNotFoundException(String.format("Compilation with id %d not found.", compId)));
        log.info("Get compilation with id {}", compId);
        return mapper.ToCompilationDto(compilation);
    }

    @Override
    @Transactional
    public void deleteCompilation(Long compId) {
        compilationRepo.findById(compId)
                .orElseThrow(() -> new DataNotFoundException(String.format("Compilation with id %d not found.", compId)));
        compilationRepo.deleteById(compId);
        log.info("Compilation with id {} deleted", compId);
    }

    @Override
    public List<CompilationDto> getCompilations(Boolean pinned, Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from / size, size);
        List<Compilation> compilations = compilationRepo.findCompilations(pinned, pageable);
        log.info("Getting compilations list.");
        return mapper.ToListCompilationDto(compilations);
    }

    @Override
    @Transactional
    public CompilationDto updateCompilation(Long compId, UpdateCompilationRequest request) {
        Compilation compilation = compilationRepo.findById(compId)
                .orElseThrow(() -> new DataNotFoundException(String.format("Compilation with id %d not found.", compId)));
        if (request.getPinned() != null) {
            compilation.setPinned(request.getPinned());
        }
        if (request.getTitle() != null) {
            compilation.setTitle(request.getTitle());
        }
        compilation.setEvents(eventRepo.findAllByIdIn(request.getEvents()));
        log.info("Compilation with id {} updated", compId);
        return mapper.ToCompilationDto(compilationRepo.save(compilation));
    }
}
