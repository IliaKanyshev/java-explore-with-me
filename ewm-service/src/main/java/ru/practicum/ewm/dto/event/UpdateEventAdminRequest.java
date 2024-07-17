package ru.practicum.ewm.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import ru.practicum.ewm.dto.location.LocationDto;
import ru.practicum.ewm.model.Location;
import ru.practicum.ewm.util.DateTimePattern;
import ru.practicum.ewm.util.enums.StateAction;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEventAdminRequest {
//    @Size(min = 20, max = 2000)
    private String annotation;
    private Long category;
//    @Size(min = 20, max = 7000)
    private String description;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateTimePattern.PATTERN)
    private LocalDateTime eventDate;
    private LocationDto location;
    private Boolean paid;
//    @PositiveOrZero
    private Integer participantLimit;
    private Boolean requestModeration;
    private StateAction stateAction;
//    @Size(min = 3, max = 120)
    private String title;
}
