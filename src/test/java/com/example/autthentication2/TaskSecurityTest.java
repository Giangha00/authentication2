package com.example.autthentication2;

import com.example.autthentication2.dto.TaskForm;
import com.example.autthentication2.entity.AppUser;
import com.example.autthentication2.entity.Role;
import com.example.autthentication2.entity.Task;
import com.example.autthentication2.entity.TaskStatus;
import com.example.autthentication2.repository.RoleRepository;
import com.example.autthentication2.repository.TaskRepository;
import com.example.autthentication2.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class TaskSecurityTest {

    @Autowired MockMvc mvc;
    @Autowired UserRepository users;
    @Autowired TaskRepository tasks;
    @Autowired RoleRepository roles;

    Long aliceTaskId;

    @BeforeEach
    void setup() {
        Role userRole = roles.findByName("ROLE_USER")
                .orElseGet(() -> roles.save(new Role("ROLE_USER")));
        Role adminRole = roles.findByName("ROLE_ADMIN")
                .orElseGet(() -> roles.save(new Role("ROLE_ADMIN")));

        var alice = new AppUser("task_alice", "unused");
        alice.getRoles().add(userRole);
        alice = users.saveAndFlush(alice);

        var bob = new AppUser("task_bob", "unused");
        bob.getRoles().add(userRole);
        users.saveAndFlush(bob);

        var admin = new AppUser("task_admin", "unused");
        admin.getRoles().add(adminRole);
        users.saveAndFlush(admin);

        TaskForm f = new TaskForm();
        f.setTitle("Alice private task");
        f.setDueDate(LocalDate.now().plusDays(1));
        aliceTaskId = tasks.saveAndFlush(new Task(alice, f)).getId();
    }

    @Test
    void anonymousMustLogin() throws Exception {
        mvc.perform(get("/tasks"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void bobCannotEditAliceTask() throws Exception {
        mvc.perform(get("/tasks/{id}/edit", aliceTaskId)
                .with(user("task_bob").roles("USER")))
                .andExpect(status().isNotFound());
    }

    @Test
    void bobCannotUpdateAliceTask() throws Exception {
        mvc.perform(post("/tasks/{id}", aliceTaskId)
                .with(user("task_bob").roles("USER")).with(csrf())
                .param("title", "Hacked").param("description", "")
                .param("dueDate", LocalDate.now().plusDays(2).toString())
                .param("status", "DONE"))
                .andExpect(status().isNotFound());

        assertEquals("Alice private task",
                tasks.findById(aliceTaskId).orElseThrow().getTitle());
    }

    @Test
    void bobCannotDeleteAliceTask() throws Exception {
        mvc.perform(post("/tasks/{id}/delete", aliceTaskId)
                .with(user("task_bob").roles("USER")).with(csrf()))
                .andExpect(status().isNotFound());

        assertTrue(tasks.existsById(aliceTaskId));
    }

    @Test
    void deleteWithoutCsrfIsRejected() throws Exception {
        mvc.perform(post("/tasks/{id}/delete", aliceTaskId)
                .with(user("task_alice").roles("USER")))
                .andExpect(status().isForbidden());

        assertTrue(tasks.existsById(aliceTaskId));
    }

    @Test
    void aliceCanUpdateOwnTask() throws Exception {
        mvc.perform(post("/tasks/{id}", aliceTaskId)
                .with(user("task_alice").roles("USER")).with(csrf())
                .param("title", "Updated").param("description", "Done")
                .param("dueDate", LocalDate.now().plusDays(2).toString())
                .param("status", "DONE"))
                .andExpect(redirectedUrl("/tasks"));

        assertEquals(TaskStatus.DONE,
                tasks.findById(aliceTaskId).orElseThrow().getStatus());
    }

    @Test
    void blankTitleIsNotSaved() throws Exception {
        long before = tasks.count();
        mvc.perform(post("/tasks")
                .with(user("task_bob").roles("USER")).with(csrf())
                .param("title", " ").param("description", "")
                .param("dueDate", LocalDate.now().plusDays(1).toString())
                .param("status", "TODO"))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("form", "title"));

        assertEquals(before, tasks.count());
    }

    @Test
    void userCannotOpenAdmin() throws Exception {
        mvc.perform(get("/admin/tasks")
                .with(user("task_bob").roles("USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanOpenAdmin() throws Exception {
        mvc.perform(get("/admin/tasks")
                .with(user("task_admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/tasks"));
    }
}
