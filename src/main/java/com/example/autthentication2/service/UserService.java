package com.example.autthentication2.service;

import com.example.autthentication2.dto.RegistrationDto;
import com.example.autthentication2.dto.UserView;
import com.example.autthentication2.entity.AppUser;
import com.example.autthentication2.entity.Role;
import com.example.autthentication2.repository.RoleRepository;
import com.example.autthentication2.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public AppUser registerStudent(RegistrationDto dto) {
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new IllegalArgumentException("Mật khẩu xác nhận không khớp");
        }
        if (userRepository.existsByUsername(dto.getUsername().trim())) {
            throw new IllegalArgumentException("Tên đăng nhập đã tồn tại trong hệ thống");
        }
        if (dto.getEmail() != null && !dto.getEmail().isBlank() && userRepository.existsByEmail(dto.getEmail().trim())) {
            throw new IllegalArgumentException("Email đã được đăng ký cho tài khoản khác");
        }

        AppUser user = new AppUser();
        user.setUsername(dto.getUsername().trim());
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        user.setFullName(dto.getFullName().trim());
        user.setEmail(dto.getEmail() != null ? dto.getEmail().trim() : null);
        user.setEnabled(true);
        user.setCreatedAt(Instant.now());

        // Hardcoded: Public registration ONLY receives ROLE_STUDENT
        Role studentRole = roleRepository.findByName("ROLE_STUDENT")
                .orElseGet(() -> roleRepository.save(new Role("ROLE_STUDENT")));
        user.getRoles().add(studentRole);

        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public List<AppUser> findAllStudents() {
        return userRepository.findByRoles_Name("ROLE_STUDENT");
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserView> findAllUsers() {
        return userRepository.findAll().stream().map(UserView::from).toList();
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void assignRole(Long userId, String roleName) {
        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy người dùng"));

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vai trò không hợp lệ: " + roleName));

        user.getRoles().add(role);
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public AppUser findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy người dùng"));
    }
}
