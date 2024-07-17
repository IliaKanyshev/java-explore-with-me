package ru.practicum.ewm.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private Long id;
    @NotBlank
    @NotNull
    @Email
    @Size(min = 6, max = 254)
    private String email;
    @NotBlank
    @NotNull
    @Size(min = 2, max = 250)
    private String name;
}
