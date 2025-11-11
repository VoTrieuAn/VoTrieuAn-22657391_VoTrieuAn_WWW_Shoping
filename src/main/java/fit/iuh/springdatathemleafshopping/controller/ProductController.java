package fit.iuh.springdatathemleafshopping.controller;

import fit.iuh.springdatathemleafshopping.enitity.Product;
import fit.iuh.springdatathemleafshopping.enitity.dto.CommentForm;
import fit.iuh.springdatathemleafshopping.enitity.dto.ProductForm;
import fit.iuh.springdatathemleafshopping.repository.ProductRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductRepository productRepository;

    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size,
            @RequestParam(defaultValue = "") String q,
            Model model) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        Page<Product> productPage = q.isBlank()
                ? productRepository.findAll(pageable)
                : productRepository.findByNameContainingIgnoreCase(q, pageable);

        model.addAttribute("productPage", productPage);
        model.addAttribute("q", q);
        return "products/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Integer id, Model model,
            @ModelAttribute("commentForm") CommentForm form) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        model.addAttribute("product", product);
        if (form.getText() == null)
            model.addAttribute("commentForm", new CommentForm());
        return "products/detail";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("title", "Thêm sản phẩm");
        model.addAttribute("productForm", new ProductForm());
        return "products/product_form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("productForm") ProductForm form,
            BindingResult binding,
            RedirectAttributes ra) {
        if (binding.hasErrors())
            return "products/product_form";
        Product p = Product.builder()
                .name(form.getName())
                .price(form.getPrice())
                .inStock(form.isInStock())
                .stock(form.getStock())
                .build();
        if (p.getStock() != null && p.getStock() == 0)
            p.setInStock(false);
        productRepository.save(p);
        ra.addFlashAttribute("success", "Đã thêm sản phẩm mới");
        return "redirect:/products";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Integer id, Model model) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        ProductForm form = new ProductForm();
        form.setName(p.getName());
        form.setPrice(p.getPrice());
        form.setInStock(p.isInStock());
        form.setStock(p.getStock());
        model.addAttribute("title", "Sửa sản phẩm");
        model.addAttribute("product", p);
        model.addAttribute("productForm", form);
        return "products/product_form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Integer id,
            @Valid @ModelAttribute("productForm") ProductForm form,
            BindingResult binding,
            RedirectAttributes ra,
            Model model) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        if (binding.hasErrors()) {
            model.addAttribute("product", p);
            return "products/product_form";
        }
        p.setName(form.getName());
        p.setPrice(form.getPrice());
        p.setInStock(form.isInStock());
        p.setStock(form.getStock());
        if (p.getStock() != null && p.getStock() == 0)
            p.setInStock(false);
        productRepository.save(p);
        ra.addFlashAttribute("success", "Đã cập nhật sản phẩm");
        return "redirect:/products";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Integer id, RedirectAttributes ra) {
        if (productRepository.existsById(id)) {
            productRepository.deleteById(id);
            ra.addFlashAttribute("success", "Đã xoá sản phẩm");
        } else {
            ra.addFlashAttribute("error", "Sản phẩm không tồn tại");
        }
        return "redirect:/products";
    }
}
