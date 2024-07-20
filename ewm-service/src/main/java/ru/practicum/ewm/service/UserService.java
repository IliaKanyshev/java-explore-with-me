package ru.practicum.ewm.service;

import ru.practicum.ewm.dto.user.UserDto;

import java.util.List;

public interface UserService {
    UserDto saveUser(UserDto userDto);

    void deleteUser(Long id);

    List<UserDto> getUsers(List<Long> ids, Integer from, Integer size);
}
