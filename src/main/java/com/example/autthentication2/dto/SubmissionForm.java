package com.example.autthentication2.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubmissionForm {

    @NotBlank(message = "Nội dung bài nộp không được để trống")
    @Size(max = 5000, message = "Nội dung bài nộp tối đa 5000 ký tự")
    private String content = "";
}
