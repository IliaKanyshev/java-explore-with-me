package ru.practicum.ewm.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.dto.category.CategoryDto;
import ru.practicum.ewm.dto.location.LocationDto;
import ru.practicum.ewm.dto.user.UserShortDto;
import ru.practicum.ewm.util.DateTimePattern;
import ru.practicum.ewm.util.enums.EventState;

import javax.validation.constraints.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventFullDto {
    private Long id;
    @NotNull
    @NotEmpty
    @NotBlank
    @Size(min = 20, max = 2000)
    private String annotation;
    private UserShortDto initiator;
    @NotNull
    private CategoryDto category;
    private Integer confirmedRequests;
    @NotNull
    @NotEmpty
    @NotBlank
    @Size(min = 20, max = 7000)
    private String description;
    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateTimePattern.PATTERN)
    private LocalDateTime eventDate;
    @NotNull
    private LocationDto location;
    private Boolean paid;
    @PositiveOrZero
    private Integer participantLimit;
    private Boolean requestModeration;
    @NotNull
    @Size(min = 3, max = 120)
    private String title;
    private EventState state;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateTimePattern.PATTERN)
    private LocalDateTime createdOn;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateTimePattern.PATTERN)
    private LocalDateTime publishedOn;
    private Long views;
}
