package ru.practicum.server.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import ru.practicum.server.util.DateTimePattern;

import javax.persistence.*;
import java.time.LocalDateTime;


@Entity
@Table(name = "hits", schema = "public")
@Getter
@Setter
public class EndpointHit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String app;
    private String uri;
    private String ip;
    @DateTimeFormat(pattern = DateTimePattern.PATTERN)
    private LocalDateTime timestamp;
}
