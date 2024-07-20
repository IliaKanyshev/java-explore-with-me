package ru.practicum.ewm.controller.admin_controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.user.UserDto;
import ru.practicum.ewm.service.UserService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@RequestMapping(path = "/admin/users")
@RequiredArgsConstructor
@Slf4j
public class AdminUserController {

    private final UserService userService;

    @PostMapping
    @ResponseStatus(value = HttpStatus.CREATED)
    public UserDto createUser(@Valid @RequestBody UserDto userDto) {
        log.info("POST request /admin/users");
        return userService.saveUser(userDto);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<UserDto> getUsers(@RequestParam(required = false, name = "ids") List<Long> ids,
                                  @PositiveOrZero
                                  @RequestParam(name = "from", defaultValue = "0") Integer from,
                                  @Positive
                                  @RequestParam(name = "size", defaultValue = "10") Integer size) {
        log.info("GET request /admin/users");
        return userService.getUsers(ids, from, size);

    }

    @DeleteMapping("/{id}")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long id) {
        log.info("DELETE request /admin/users/{id}");
        userService.deleteUser(id);
    }
}
