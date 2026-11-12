package ru.fortushin.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.fortushin.entity.Tag;

import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {
    Optional<Tag> findByName(String name);
    boolean existsByName(String name);
}