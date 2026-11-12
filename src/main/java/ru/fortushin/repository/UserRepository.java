package ru.fortushin.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.fortushin.entity.User;
import ru.fortushin.entity.enums.Role;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    List<User> findAllByRole(Role role);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.profile WHERE u.id = :id")
    Optional<User> findByIdWithProfile(@Param("id") Long id);


    @EntityGraph(attributePaths = {"profile"})
    @Query("SELECT u FROM User u WHERE u.email = :email")
    Optional<User> findByEmailWithProfile(@Param("email") String email);
}