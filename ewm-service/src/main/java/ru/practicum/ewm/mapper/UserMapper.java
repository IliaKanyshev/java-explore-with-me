package ru.practicum.ewm.mapper;

import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.dto.user.UserDto;
import ru.practicum.ewm.model.User;

import java.util.List;

//@Component
@Mapper(componentModel = "spring")
public interface UserMapper {
    User toUser(UserDto userDto);

    UserDto toUserDto(User user);

    List<UserDto> toUserDtoList(List<User> users);
}
