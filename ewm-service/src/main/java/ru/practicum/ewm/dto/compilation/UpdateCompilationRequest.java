package ru.practicum.ewm.dto.compilation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Size;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCompilationRequest {
    private Boolean pinned;
    //    @NotNull
//    @NotBlank
    @Size(min = 1, max = 50)
    private String title;
    private List<Long> events;
}
