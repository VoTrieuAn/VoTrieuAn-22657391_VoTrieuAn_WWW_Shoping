package fit.iuh.springdatathemleafshopping.controller;

import fit.iuh.springdatathemleafshopping.enitity.Comment;
import fit.iuh.springdatathemleafshopping.enitity.Product;
import fit.iuh.springdatathemleafshopping.enitity.dto.CommentForm;
import fit.iuh.springdatathemleafshopping.repository.CommentRepository;
import fit.iuh.springdatathemleafshopping.repository.ProductRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping
public class CommentController {

    private final CommentRepository commentRepository;
    private final ProductRepository productRepository;

    // Tạo comment: POST /products/{productId}/comments
    @PostMapping("/products/{productId}/comments")
    public String create(@PathVariable Integer productId,
                         @Valid @ModelAttribute("commentForm") CommentForm form,
                         BindingResult binding,
                         RedirectAttributes ra) {
        Product product = productRepository.findById(productId)
                .orElse(null);
        if (product == null) {
            ra.addFlashAttribute("error", "Sản phẩm không tồn tại");
            return "redirect:/products";
        }
        if (binding.hasErrors()) {
            ra.addFlashAttribute("org.springframework.validation.BindingResult.commentForm", binding);
            ra.addFlashAttribute("commentForm", form);
            return "redirect:/products/" + productId;
        }
        Comment c = Comment.builder()
                .text(form.getText())
                .product(product)
                .build();
        commentRepository.save(c);
        ra.addFlashAttribute("success", "Đã thêm bình luận");
        return "redirect:/products/" + productId;
    }

    // Form sửa comment: GET /comments/{id}/edit
    @GetMapping("/comments/{id}/edit")
    public String editForm(@PathVariable Integer id, Model model, RedirectAttributes ra) {
        Comment c = commentRepository.findById(id).orElse(null);
        if (c == null) {
            ra.addFlashAttribute("error", "Bình luận không tồn tại");
            return "redirect:/products";
        }
        CommentForm form = new CommentForm();
        form.setText(c.getText());
        model.addAttribute("comment", c);
        model.addAttribute("commentForm", form);
        return "comments/comment_form"; // tạo template dưới
    }

    // Cập nhật comment: POST /comments/{id}
    @PostMapping("/comments/{id}")
    public String update(@PathVariable Integer id,
                         @Valid @ModelAttribute("commentForm") CommentForm form,
                         BindingResult binding,
                         RedirectAttributes ra,
                         Model model) {
        Comment c = commentRepository.findById(id).orElse(null);
        if (c == null) {
            ra.addFlashAttribute("error", "Bình luận không tồn tại");
            return "redirect:/products";
        }
        if (binding.hasErrors()) {
            model.addAttribute("comment", c);
            return "comments/comment_form";
        }
        c.setText(form.getText());
        commentRepository.save(c);
        ra.addFlashAttribute("success", "Đã cập nhật bình luận");
        return "redirect:/products/" + c.getProduct().getId();
    }

    // Xoá comment: POST /comments/{id}/delete
    @PostMapping("/comments/{id}/delete")
    public String delete(@PathVariable Integer id, RedirectAttributes ra) {
        Comment c = commentRepository.findById(id).orElse(null);
        if (c == null) {
            ra.addFlashAttribute("error", "Bình luận không tồn tại");
            return "redirect:/products";
        }
        Integer productId = c.getProduct().getId();
        commentRepository.delete(c);
        ra.addFlashAttribute("success", "Đã xoá bình luận");
        return "redirect:/products/" + productId;
    }
}
