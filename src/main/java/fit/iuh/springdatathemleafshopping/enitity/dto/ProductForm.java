package fit.iuh.springdatathemleafshopping.enitity.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;
import jakarta.validation.constraints.Min;

@Data
public class ProductForm {
    @NotBlank(message = "Tên không được để trống")
    private String name;

    @DecimalMin(value = "0.0", inclusive = true, message = "Giá phải ≥ 0")
    private BigDecimal price;

    private boolean inStock;

    @Min(value = 0, message = "Tồn kho phải ≥ 0")
    private Integer stock;
}
