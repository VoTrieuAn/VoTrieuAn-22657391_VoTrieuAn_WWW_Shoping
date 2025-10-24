// src/main/java/demo/shop/web/OrderController.java
package fit.iuh.springdatathemleafshopping.controller;

import fit.iuh.springdatathemleafshopping.enitity.Order;
import fit.iuh.springdatathemleafshopping.repository.CustomerRepository;
import fit.iuh.springdatathemleafshopping.service.*;
import fit.iuh.springdatathemleafshopping.service.CustomerService;
import fit.iuh.springdatathemleafshopping.service.OrderService;
import fit.iuh.springdatathemleafshopping.service.ProductService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/orders")
public class OrderController {
    private final OrderService orders;
    private final CustomerService customers;
    private final ProductService products;
    private final CustomerRepository customerRepository;

    public OrderController(OrderService o, CustomerService c, ProductService p, CustomerRepository cr){
        this.orders=o; this.customers=c; this.products=p; this.customerRepository = cr;
    }

    @GetMapping
    public String list(Model model, Authentication auth){
        List<Order> list;
        boolean isAdmin = auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (isAdmin) {
            list = orders.findAll();
        } else {
            Integer cid = resolveCurrentCustomerId(auth);
            list = orders.findByCustomer(cid);
        }
        model.addAttribute("orders", list);
        return "orders/list";
    }

    @GetMapping("/new")
    public String form(@RequestParam(required = false) Integer productId,
                       @RequestParam(required = false) String error,
                       Model model, Authentication auth){
        boolean isAdmin = auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (isAdmin) {
            model.addAttribute("customers", customers.findAll());
        } else {
            model.addAttribute("currentCustomerId", resolveCurrentCustomerId(auth));
        }
        model.addAttribute("products", products.findAll());
        model.addAttribute("preselectProductId", productId);
        if (error != null) model.addAttribute("error", error);
        return "orders/form";
    }

    @PostMapping
    public String create(Authentication auth,
                         @RequestParam(required = false) Integer customerId,
                         @RequestParam Integer productId,
                         @RequestParam Integer amount){
        boolean isAdmin = auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        Integer cid = isAdmin ? customerId : resolveCurrentCustomerId(auth);
        try {
            orders.createSimple(cid, productId, amount);
            return "redirect:/orders";
        } catch (IllegalStateException ex){
            return "redirect:/orders/new?error=" + java.net.URLEncoder.encode(ex.getMessage(), java.nio.charset.StandardCharsets.UTF_8);
        }
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Integer id, Model model){
        Order order = orders.findById(id).orElseThrow();
        model.addAttribute("order", order);
        return "orders/detail";
    }

    private Integer resolveCurrentCustomerId(Authentication auth){
        // Very simple mapping: username "customer" -> Customer with name "customer"; fallback to the first.
        String username = auth != null ? auth.getName() : null;
        Integer cid = customerRepository.findByName(username != null ? username : "customer")
                .map(c -> c.getId())
                .orElseGet(() -> customerRepository.findAll().stream().findFirst().map(c -> c.getId()).orElse(null));
        if (cid == null) throw new IllegalStateException("No customer available to assign order");
        return cid;
    }
}
