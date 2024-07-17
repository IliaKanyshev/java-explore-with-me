package ru.practicum.ewm.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.ewm.model.Category;

@Repository
public interface CategoryRepo extends JpaRepository<Category, Long> {
    Boolean existsByName(String name);
}
