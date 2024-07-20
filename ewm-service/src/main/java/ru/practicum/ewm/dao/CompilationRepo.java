package ru.practicum.ewm.dao;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.ewm.model.Compilation;

import java.util.List;

@Repository
public interface CompilationRepo extends JpaRepository<Compilation, Long> {
    @Query("SELECT c FROM Compilation c WHERE (:pinned IS NULL OR c.pinned = :pinned)")
    List<Compilation> findCompilations(@Param("pinned") Boolean pinned, Pageable pageable);
}
