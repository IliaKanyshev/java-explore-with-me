package ru.practicum.ewm.model;

import lombok.*;
import org.hibernate.annotations.Formula;
import ru.practicum.ewm.util.enums.EventState;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String annotation;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "initiator_id", nullable = false)
    @ToString.Exclude
    private User initiator;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    @ToString.Exclude
    private Category category;
    @Formula(value = "(select count(r.id) from requests as r where r.event_id = id and r.status like 'CONFIRMED')")
    private int confirmedRequests;
    private String description;
    private LocalDateTime eventDate;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", nullable = false)
    @ToString.Exclude
    private Location location;
    private Boolean paid;
    private int participantLimit;
    private Boolean requestModeration;
    private String title;
    @Enumerated(EnumType.STRING)
    private EventState state;
    private LocalDateTime createdOn;
    private LocalDateTime publishedOn;
    @Transient
    private Long views;
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "compilations_events",
            joinColumns = {@JoinColumn(name = "event_id", referencedColumnName = "id")},
            inverseJoinColumns = {@JoinColumn(name = "compilation_id", referencedColumnName = "id")}
    )
    @ToString.Exclude
    private List<Compilation> compilations;
}
