package fit.iuh.springdatathemleafshopping.controller;

import fit.iuh.springdatathemleafshopping.enitity.Order;
import fit.iuh.springdatathemleafshopping.repository.CustomerRepository;
import fit.iuh.springdatathemleafshopping.service.*;
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
    private final CartService cartService;

    public OrderController(OrderService o, CustomerService c, ProductService p, CustomerRepository cr, CartService cartService){
        this.orders=o; this.customers=c; this.products=p; this.customerRepository = cr; this.cartService = cartService;
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
                       @RequestParam(required = false, defaultValue = "false") boolean quick,
                       Model model, Authentication auth){
        boolean isAdmin = auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (isAdmin) {
            model.addAttribute("customers", customers.findAll());
        } else {
            model.addAttribute("currentCustomerId", resolveCurrentCustomerId(auth));
        }
        model.addAttribute("products", products.findAll());
        model.addAttribute("preselectProductId", productId);
        model.addAttribute("quick", quick);
        if (error != null) model.addAttribute("error", error);
        return "orders/form";
    }

    @PostMapping
    public String create(Authentication auth,
                         @RequestParam(required = false) Integer customerId,
                         @RequestParam Integer productId,
                         @RequestParam Integer amount,
                         @RequestParam(required = false, defaultValue = "false") boolean quick){
        boolean isAdmin = auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        Integer cid = isAdmin ? customerId : resolveCurrentCustomerId(auth);
        try {
            if (quick) {
                // Thêm vào giỏ theo số lượng đã chọn và chuyển sang checkout
                var product = products.findById(productId).orElseThrow();
                cartService.add(product.getId().longValue(), product.getName(), product.getPrice(), amount, null);
                return "redirect:/cart/checkout";
            } else {
                orders.createSimple(cid, productId, amount);
                return "redirect:/orders";
            }
        } catch (IllegalStateException ex){
            String msg = java.net.URLEncoder.encode(ex.getMessage(), java.nio.charset.StandardCharsets.UTF_8);
            return "redirect:/orders/new?productId=" + productId + (quick ? "&quick=true" : "") + "&error=" + msg;
        }
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Integer id, Model model){
        var order = orders.findById(id).orElseThrow();
        model.addAttribute("order", order);
        return "orders/detail";
    }

    private Integer resolveCurrentCustomerId(Authentication auth){
        // Very simple mapping: username "customer" -> Customer with name "customer"; fallback to the first.
        var username = auth != null ? auth.getName() : null;
        var cid = customerRepository.findByName(username != null ? username : "customer")
                .map(c -> c.getId())
                .orElseGet(() -> customerRepository.findAll().stream().findFirst().map(c -> c.getId()).orElse(null));
        if (cid == null) throw new IllegalStateException("No customer available to assign order");
        return cid;
    }

    // moved to CustomerController for path /customers/{id}/orders

    // No extra finders needed now
}
