package ru.practicum.ewm.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.ewm.model.User;

@Repository
public interface UserRepo extends JpaRepository<User, Long> {
}
