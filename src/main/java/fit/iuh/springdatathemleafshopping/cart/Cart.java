package fit.iuh.springdatathemleafshopping.cart;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class Cart {
    private final Map<Long, CartItem> items = new LinkedHashMap<>();

    public Collection<CartItem> getItems() {
        return new ArrayList<>(items.values());
    }

    public void addItem(CartItem item) {
        if (item == null || item.getId() == null) return;
        CartItem existing = items.get(item.getId());
        if (existing == null) {
            if (item.getQuantity() <= 0) item.setQuantity(1);
            items.put(item.getId(), item);
        } else {
            int qty = existing.getQuantity() + Math.max(item.getQuantity(), 1);
            existing.setQuantity(qty);
            if (item.getName() != null) existing.setName(item.getName());
            if (item.getPrice() != null) existing.setPrice(item.getPrice());
            if (item.getImageUrl() != null) existing.setImageUrl(item.getImageUrl());
        }
    }

    public void updateQuantity(Long id, int quantity) {
        if (id == null) return;
        CartItem existing = items.get(id);
        if (existing == null) return;
        if (quantity <= 0) {
            items.remove(id);
        } else {
            existing.setQuantity(quantity);
        }
    }

    public void removeItem(Long id) {
        if (id == null) return;
        items.remove(id);
    }

    public void clear() {
        items.clear();
    }

    public int getTotalQuantity() {
        return items.values().stream().mapToInt(CartItem::getQuantity).sum();
    }

    public BigDecimal getTotalPrice() {
        return items.values().stream()
                .map(CartItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}

