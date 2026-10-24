package com.example.autthentication2.repository;

import com.example.autthentication2.entity.Assignment;
import com.example.autthentication2.entity.AssignmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, Long> {

    @EntityGraph(attributePaths = {"lecturer", "assignedStudents"})
    Page<Assignment> findByLecturer_Username(String lecturerUsername, Pageable pageable);

    @EntityGraph(attributePaths = {"lecturer", "assignedStudents"})
    Optional<Assignment> findByIdAndLecturer_Username(Long id, String lecturerUsername);

    @Override
    @EntityGraph(attributePaths = {"lecturer", "assignedStudents"})
    Page<Assignment> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"lecturer", "assignedStudents"})
    @Query("SELECT a FROM Assignment a JOIN a.assignedStudents s WHERE s.username = :studentUsername AND a.status = 'OPEN'")
    Page<Assignment> findOpenAssignmentsForStudent(@Param("studentUsername") String studentUsername, Pageable pageable);

    @EntityGraph(attributePaths = {"lecturer", "assignedStudents"})
    @Query("SELECT a FROM Assignment a JOIN a.assignedStudents s WHERE a.id = :id AND s.username = :studentUsername")
    Optional<Assignment> findByIdAndAssignedStudent(@Param("id") Long id, @Param("studentUsername") String studentUsername);

    long countByStatus(AssignmentStatus status);
}
