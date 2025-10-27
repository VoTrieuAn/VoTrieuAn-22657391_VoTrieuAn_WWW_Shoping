package fit.iuh.springdatathemleafshopping.config;

import fit.iuh.springdatathemleafshopping.service.CartService;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class CartAdvice {
    private final CartService cartService;

    public CartAdvice(CartService cartService) {
        this.cartService = cartService;
    }

    @ModelAttribute("cartCount")
    public int cartCount() {
        try {
            return cartService.getTotalQuantity();
        } catch (Exception e) {
            return 0;
        }
    }
}

