package ru.practicum.ewm.mapper;

import org.mapstruct.Mapper;
import ru.practicum.ewm.dto.category.CategoryDto;
import ru.practicum.ewm.model.Category;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    Category toCategory(CategoryDto categoryDto);

    CategoryDto toCategoryDto(Category category);

    List<CategoryDto> toCategoryDtoList(List<Category> categories);
}
