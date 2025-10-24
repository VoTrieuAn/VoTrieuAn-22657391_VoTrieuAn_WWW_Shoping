package fit.iuh.springdatathemleafshopping.controller;

import fit.iuh.springdatathemleafshopping.enitity.Customer;
import fit.iuh.springdatathemleafshopping.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequestMapping("/customers")
@RequiredArgsConstructor
public class CustomerController {
    private final CustomerRepository customerRepository;
    private final fit.iuh.springdatathemleafshopping.service.OrderService orderService;

    @GetMapping
    public String list(Model model){
        model.addAttribute("customers", customerRepository.findAll());
        return "customers/list";
    }

    @GetMapping("/new")
    public String newForm(Model model){
        model.addAttribute("customer", new Customer());
        return "customers/form";
    }

    @PostMapping
    public String create(@ModelAttribute Customer c, RedirectAttributes ra){
        if (c.getCustomerSince() == null) c.setCustomerSince(LocalDate.now());
        customerRepository.save(c);
        ra.addFlashAttribute("success","Đã thêm khách hàng");
        return "redirect:/customers";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Integer id, Model model){
        var c = customerRepository.findById(id).orElseThrow();
        model.addAttribute("customer", c);
        return "customers/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Integer id, @ModelAttribute Customer form, RedirectAttributes ra){
        var c = customerRepository.findById(id).orElseThrow();
        c.setName(form.getName());
        c.setCustomerSince(form.getCustomerSince());
        customerRepository.save(c);
        ra.addFlashAttribute("success","Đã cập nhật khách hàng");
        return "redirect:/customers";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Integer id, RedirectAttributes ra){
        if (customerRepository.existsById(id)){
            customerRepository.deleteById(id);
            ra.addFlashAttribute("success","Đã xoá khách hàng");
        } else {
            ra.addFlashAttribute("error","Khách hàng không tồn tại");
        }
        return "redirect:/customers";
    }

    // Xem lịch sử đơn hàng của khách hàng (ADMIN)
    @GetMapping("/{id}/orders")
    public String orders(@PathVariable Integer id, Model model){
        var list = orderService.findByCustomer(id);
        model.addAttribute("orders", list);
        return "orders/list";
    }
}
