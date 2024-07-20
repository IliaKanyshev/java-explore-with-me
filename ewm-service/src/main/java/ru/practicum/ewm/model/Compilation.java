package ru.practicum.ewm.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "compilations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Compilation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Boolean pinned;
    private String title;
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "compilations_events",
            joinColumns = {@JoinColumn(name = "compilation_id", referencedColumnName = "id")},
            inverseJoinColumns = {@JoinColumn(name = "event_id", referencedColumnName = "id")}
    )
    List<Event> events;
}
