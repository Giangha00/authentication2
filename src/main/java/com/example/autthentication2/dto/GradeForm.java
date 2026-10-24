package com.example.autthentication2.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class GradeForm {

    @NotNull(message = "Vui lòng nhập điểm số")
    @DecimalMin(value = "0.00", message = "Điểm không được nhỏ hơn 0.00")
    @DecimalMax(value = "10.00", message = "Điểm tối đa là 10.00")
    @Digits(integer = 2, fraction = 2, message = "Điểm chỉ được tối đa 2 chữ số thập phân")
    private BigDecimal score;

    @NotBlank(message = "Vui lòng nhập nhận xét cho học viên")
    @Size(max = 2000, message = "Nhận xét tối đa 2000 ký tự")
    private String feedback = "";
}
