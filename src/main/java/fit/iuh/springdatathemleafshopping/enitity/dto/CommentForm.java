package fit.iuh.springdatathemleafshopping.enitity.dto;


import jakarta.validation.constraints.NotBlank;
import lombok.Data;


@Data
public class CommentForm {
@NotBlank(message = "Nội dung không được để trống")
private String text;
}