package fit.iuh.springdatathemleafshopping.service;

import fit.iuh.springdatathemleafshopping.cart.Cart;
import fit.iuh.springdatathemleafshopping.cart.CartItem;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.math.BigDecimal;
import java.util.Collection;

@Service
public class CartService {
    public static final String CART_SESSION_KEY = "CART_SESSION";

    private HttpSession currentSession() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        return attrs.getRequest().getSession(true);
    }

    private Cart getOrCreateCart() {
        HttpSession session = currentSession();
        Cart cart = (Cart) session.getAttribute(CART_SESSION_KEY);
        if (cart == null) {
            cart = new Cart();
            session.setAttribute(CART_SESSION_KEY, cart);
        }
        return cart;
    }

    public Collection<CartItem> findAll() {
        return getOrCreateCart().getItems();
    }

    public int getTotalQuantity() {
        return getOrCreateCart().getTotalQuantity();
    }

    public BigDecimal getTotalPrice() {
        return getOrCreateCart().getTotalPrice();
    }

    public void add(Long id, String name, BigDecimal price, int quantity, String imageUrl) {
        CartItem item = new CartItem(id, name, price, quantity, imageUrl);
        getOrCreateCart().addItem(item);
    }

    public void updateQuantity(Long id, int quantity) {
        getOrCreateCart().updateQuantity(id, quantity);
    }

    public void remove(Long id) {
        getOrCreateCart().removeItem(id);
    }

    public void clear() {
        getOrCreateCart().clear();
    }
}
