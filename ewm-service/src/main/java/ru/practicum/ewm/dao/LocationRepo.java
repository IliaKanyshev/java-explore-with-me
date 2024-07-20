package ru.practicum.ewm.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.ewm.model.Location;

@Repository
public interface LocationRepo extends JpaRepository<Location, Long> {
}
