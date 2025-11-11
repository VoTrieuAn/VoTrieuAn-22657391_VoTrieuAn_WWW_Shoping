package fit.iuh.springdatathemleafshopping.enitity.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import jakarta.validation.constraints.Min;

@Data
public class ProductForm {
    @NotBlank(message = "Tên không được để trống")
    @Size(max = 255, message = "Tên tối đa 255 ký tự")
    private String name;

    @NotNull(message = "Giá không được để trống")
    @DecimalMin(value = "0.0", inclusive = true, message = "Giá phải ≥ 0")
    @Digits(integer = 12, fraction = 2, message = "Giá không hợp lệ")
    private BigDecimal price;

    private boolean inStock;

    @NotNull(message = "Tồn kho không được để trống")
    @Min(value = 0, message = "Tồn kho phải ≥ 0")
    private Integer stock;
}
