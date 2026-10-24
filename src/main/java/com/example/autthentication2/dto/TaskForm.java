package com.example.autthentication2.dto;

import com.example.autthentication2.entity.TaskStatus;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@Setter
public class TaskForm {

    @NotBlank(message = "Vui lòng nhập tiêu đề")
    @Size(max = 150, message = "Tiêu đề tối đa 150 ký tự")
    private String title = "";

    @NotNull
    @Size(max = 2000, message = "Mô tả tối đa 2000 ký tự")
    private String description = "";

    @NotNull(message = "Vui lòng chọn hạn hoàn thành")
    @FutureOrPresent(message = "Hạn không được trước hôm nay")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dueDate = LocalDate.now();

    @NotNull(message = "Vui lòng chọn trạng thái")
    private TaskStatus status = TaskStatus.TODO;

    public static TaskForm from(TaskView task) {
        TaskForm f = new TaskForm();
        f.setTitle(task.title());
        f.setDescription(task.description());
        f.setDueDate(task.dueDate());
        f.setStatus(task.status());
        return f;
    }
}
