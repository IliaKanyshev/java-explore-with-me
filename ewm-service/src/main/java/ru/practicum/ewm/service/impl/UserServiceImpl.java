package ru.practicum.ewm.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.dao.UserRepo;
import ru.practicum.ewm.dto.user.UserDto;
import ru.practicum.ewm.exception.DataConflictException;
import ru.practicum.ewm.mapper.UserMapper;
import ru.practicum.ewm.service.UserService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepo userRepo;
    private final UserMapper mapper;

    @Override
    public UserDto saveUser(UserDto userDto) {
        if (userRepo.existsByName(userDto.getName())) {
            log.info("User name {} already in use.", userDto.getName());
            throw new DataConflictException(String.format("User name %s already in use.",
                    userDto.getName()));
        }
        log.info("User with name {} was created.", userDto.getName());
        return mapper.toUserDto(userRepo.save(mapper.toUser(userDto)));
    }

    @Override
    public void deleteUser(Long id) {
        log.info("User with id {} was deleted.", id);
        userRepo.deleteById(id);
    }

    @Override
    public List<UserDto> getUsers(List<Long> ids, Integer from, Integer size) {
        log.info("Getting users.");
        Pageable page = PageRequest.of(from / size, size);
        return ids != null ? mapper.toUserDtoList(userRepo.findAllById(ids))
                : mapper.toUserDtoList(userRepo.findAll(page).toList());
    }
}
