package com.example.autthentication2.repository;

import com.example.autthentication2.entity.Submission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Long> {

    @EntityGraph(attributePaths = {"assignment", "student"})
    Optional<Submission> findByAssignment_IdAndStudent_Username(Long assignmentId, String studentUsername);

    @EntityGraph(attributePaths = {"assignment", "student"})
    Optional<Submission> findByIdAndStudent_Username(Long id, String studentUsername);

    @EntityGraph(attributePaths = {"assignment", "student"})
    List<Submission> findByAssignment_Id(Long assignmentId);

    @EntityGraph(attributePaths = {"assignment", "student"})
    Page<Submission> findByStudent_Username(String studentUsername, Pageable pageable);

    long countByScoreIsNotNull();
}
