package ru.practicum.ewm.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.dto.location.LocationDto;
import ru.practicum.ewm.util.DateTimePattern;
import ru.practicum.ewm.util.enums.StateAction;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEventAdminRequest {
    private String annotation;
    private Long category;
    private String description;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateTimePattern.PATTERN)
    private LocalDateTime eventDate;
    private LocationDto location;
    private Boolean paid;
    private Integer participantLimit;
    private Boolean requestModeration;
    private StateAction stateAction;
    private String title;
}
