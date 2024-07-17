package ru.practicum.ewm.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.dao.CategoryRepo;
import ru.practicum.ewm.dao.EventRepo;
import ru.practicum.ewm.dto.category.CategoryDto;
import ru.practicum.ewm.exception.DataConflictException;
import ru.practicum.ewm.exception.DataNotFoundException;
import ru.practicum.ewm.mapper.CategoryMapper;
import ru.practicum.ewm.model.Category;
import ru.practicum.ewm.service.CategoryService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImpl implements CategoryService {
    private final CategoryMapper mapper;
    private final CategoryRepo categoryRepo;
    private final EventRepo eventRepo;

    @Override
    public CategoryDto saveCategory(CategoryDto CategoryDto) {
//        if (categoryRepo.existsByName(CategoryDto.getName())) {
//            throw new DataConflictException(String.format("Category name %s is already in use.", CategoryDto.getName()));
//        }
        log.info("Category with name {} was created.", CategoryDto.getName());
        return mapper.toCategoryDto(categoryRepo.save(mapper.toCategory(CategoryDto)));
    }

    @Override
    public CategoryDto getCategory(Long catId) {
        Category category = categoryRepo.findById(catId)
                .orElseThrow(() -> new DataNotFoundException(String.format("Category with id %d not found.", catId)));
        log.info("Getting category with id {}", catId);
        return mapper.toCategoryDto(category);
    }

    @Override
    public void deleteCategory(Long catId) {
            categoryRepo.findById(catId)
                    .orElseThrow(() -> new DataNotFoundException(String.format("Category with id %d not found.",catId)));
        //        if (eventRepo.findByCategoryId(catId)) {
//            throw new DataConflictException("Category is not empty.");
//        }
        if (!eventRepo.findAllByCategoryId(catId).isEmpty()) {
            throw new DataConflictException(String.format("Category with id %d isn't empty.",catId));
        }
        log.info("Category with id {} was deleted.", catId);
        categoryRepo.deleteById(catId);
    }

    @Override
    public CategoryDto updateCategory(Long catId, CategoryDto categoryDto) {
        Category category = categoryRepo.findById(catId)
                .orElseThrow(() -> new DataNotFoundException(String.format("Category with id %d not found.", catId)));
//        if (categoryRepo.existsByName(categoryDto.getName())) {
//            throw new DataConflictException(String.format("Category name %s is already in use.", categoryDto.getName()));
//        }
        category.setName(categoryDto.getName());
        log.info("Category {} was updated.", category.getName());
        return mapper.toCategoryDto(categoryRepo.save(category));
    }

    @Override
    public List<CategoryDto> getCategories(Integer from, Integer size) {
        Pageable page = PageRequest.of(from / size, size);
        log.info("Getting categories list.");
        return mapper.toCategoryDtoList(categoryRepo.findAll(page).toList());
    }






}
