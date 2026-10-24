package com.example.autthentication2.repository;

import com.example.autthentication2.entity.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    @EntityGraph(attributePaths = "owner")
    Page<Task> findByOwner_Username(String username, Pageable pageable);

    @EntityGraph(attributePaths = "owner")
    Optional<Task> findByIdAndOwner_Username(Long id, String username);

    @Override
    @EntityGraph(attributePaths = "owner")
    Page<Task> findAll(Pageable pageable);
}
