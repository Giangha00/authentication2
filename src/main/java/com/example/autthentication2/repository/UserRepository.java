package com.example.autthentication2.repository;

import com.example.autthentication2.entity.AppUser;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<AppUser, Long> {

    @EntityGraph(attributePaths = "roles")
    Optional<AppUser> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    @EntityGraph(attributePaths = "roles")
    List<AppUser> findByRoles_Name(String roleName);

    @Override
    @EntityGraph(attributePaths = "roles")
    List<AppUser> findAll();

    long countByRoles_Name(String roleName);
}
