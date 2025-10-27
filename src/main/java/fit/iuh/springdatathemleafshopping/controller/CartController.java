package fit.iuh.springdatathemleafshopping.controller;

import fit.iuh.springdatathemleafshopping.cart.CartItem;
import fit.iuh.springdatathemleafshopping.service.CartService;
import fit.iuh.springdatathemleafshopping.service.OrderService;
import fit.iuh.springdatathemleafshopping.repository.CustomerRepository;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;
    private final OrderService orderService;
    private final CustomerRepository customerRepository;

    public CartController(CartService cartService, OrderService orderService, CustomerRepository customerRepository) {
        this.cartService = cartService;
        this.orderService = orderService;
        this.customerRepository = customerRepository;
    }

    @GetMapping
    public String viewCart(Model model) {
        model.addAttribute("items", cartService.findAll());
        model.addAttribute("totalQuantity", cartService.getTotalQuantity());
        model.addAttribute("totalPrice", cartService.getTotalPrice());
        return "cart"; // templates/cart.html
    }

    @PostMapping(path = "/add")
    public String addToCart(@RequestParam("id") Long id,
                            @RequestParam(value = "name", required = false) String name,
                            @RequestParam(value = "price", required = false) BigDecimal price,
                            @RequestParam(value = "quantity", required = false, defaultValue = "1") int quantity,
                            @RequestParam(value = "imageUrl", required = false) String imageUrl,
                            @RequestParam(value = "redirect", required = false) String redirect) {
        // price may be null; set to 0 if missing to avoid NPE
        if (price == null) price = BigDecimal.ZERO;
        cartService.add(id, name, price, quantity, imageUrl);
        if (redirect != null && !redirect.isBlank()) {
            return "redirect:" + redirect;
        }
        return "redirect:/cart";
    }

    @PostMapping(path = "/update")
    public String updateQuantity(@RequestParam("id") Long id,
                                 @RequestParam("quantity") int quantity,
                                 @RequestParam(value = "redirect", required = false) String redirect) {
        cartService.updateQuantity(id, quantity);
        if (redirect != null && !redirect.isBlank()) {
            return "redirect:" + redirect;
        }
        return "redirect:/cart";
    }

    @PostMapping(path = "/remove")
    public String removeItem(@RequestParam("id") Long id,
                             @RequestParam(value = "redirect", required = false) String redirect) {
        cartService.remove(id);
        if (redirect != null && !redirect.isBlank()) {
            return "redirect:" + redirect;
        }
        return "redirect:/cart";
    }

    @PostMapping(path = "/clear")
    public String clearCart(@RequestParam(value = "redirect", required = false) String redirect) {
        cartService.clear();
        if (redirect != null && !redirect.isBlank()) {
            return "redirect:" + redirect;
        }
        return "redirect:/cart";
    }

    @PostMapping(path = "/checkout")
    public String checkout(Authentication auth,
                           @RequestParam(required = false) String fullName,
                           @RequestParam(required = false) String phone,
                           @RequestParam(required = false) String address,
                           @RequestParam(required = false) String note,
                           RedirectAttributes ra) {
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }
        Integer cid = resolveCurrentCustomerId(auth);
        try {
            var order = orderService.createFromCart(cid, cartService.findAll());
            cartService.clear();
            ra.addFlashAttribute("success", "Đặt hàng thành công");
            return "redirect:/orders/" + order.getId();
        } catch (IllegalStateException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
            return "redirect:/cart";
        }
    }

    @GetMapping(path = "/checkout")
    public String checkoutForm(Authentication auth, Model model, RedirectAttributes ra) {
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }
        var items = cartService.findAll();
        if (items == null || items.isEmpty()) {
            ra.addFlashAttribute("error", "Giỏ hàng trống");
            return "redirect:/cart";
        }
        model.addAttribute("items", items);
        model.addAttribute("totalQuantity", cartService.getTotalQuantity());
        model.addAttribute("totalPrice", cartService.getTotalPrice());
        return "cart_checkout";
    }

    private Integer resolveCurrentCustomerId(Authentication auth){
        var username = auth != null ? auth.getName() : null;
        var cid = customerRepository.findByName(username != null ? username : "customer")
                .map(c -> c.getId())
                .orElseGet(() -> customerRepository.findAll().stream().findFirst().map(c -> c.getId()).orElse(null));
        if (cid == null) throw new IllegalStateException("No customer available to assign order");
        return cid;
    }

    // Lightweight JSON endpoints for async UI updates
    @PostMapping(path = "/api/add", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @ResponseBody
    public Map<String, Object> apiAdd(@RequestParam("id") Long id,
                                      @RequestParam(value = "name", required = false) String name,
                                      @RequestParam(value = "price", required = false) BigDecimal price,
                                      @RequestParam(value = "quantity", required = false, defaultValue = "1") int quantity,
                                      @RequestParam(value = "imageUrl", required = false) String imageUrl) {
        if (price == null) price = BigDecimal.ZERO;
        cartService.add(id, name, price, quantity, imageUrl);
        Map<String, Object> res = new HashMap<>();
        res.put("ok", true);
        res.put("totalQuantity", cartService.getTotalQuantity());
        res.put("totalPrice", cartService.getTotalPrice());
        return res;
    }

    @GetMapping(path = "/api/summary")
    @ResponseBody
    public Map<String, Object> apiSummary() {
        Map<String, Object> res = new HashMap<>();
        res.put("totalQuantity", cartService.getTotalQuantity());
        res.put("totalPrice", cartService.getTotalPrice());
        return res;
    }
}
