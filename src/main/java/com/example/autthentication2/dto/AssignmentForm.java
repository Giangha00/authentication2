package com.example.autthentication2.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AssignmentForm {

    @NotBlank(message = "Vui lòng nhập tiêu đề bài tập")
    @Size(max = 150, message = "Tiêu đề tối đa 150 ký tự")
    private String title = "";

    @NotBlank(message = "Vui lòng nhập mô tả hoặc yêu cầu bài tập")
    @Size(max = 4000, message = "Mô tả tối đa 4000 ký tự")
    private String description = "";

    @NotBlank(message = "Vui lòng chọn thời hạn nộp bài")
    private String deadline = ""; // Format: yyyy-MM-dd'T'HH:mm (from HTML datetime-local)
}
