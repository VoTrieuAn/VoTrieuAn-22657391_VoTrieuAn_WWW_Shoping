// src/main/java/demo/shop/service/OrderService.java
package fit.iuh.springdatathemleafshopping.service;

import fit.iuh.springdatathemleafshopping.enitity.Order;
import fit.iuh.springdatathemleafshopping.enitity.OrderLine;
import fit.iuh.springdatathemleafshopping.repository.CustomerRepository;
import fit.iuh.springdatathemleafshopping.repository.OrderRepository;
import fit.iuh.springdatathemleafshopping.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class OrderService {
    private final OrderRepository orders;
    private final CustomerRepository customers;
    private final ProductRepository products;

    public OrderService(OrderRepository o, CustomerRepository c, ProductRepository p){
        this.orders=o; this.customers=c; this.products=p;
    }

    @Transactional
    public Order createSimple(Integer customerId, Integer productId, Integer amount){
        var customer = customers.findById(customerId).orElseThrow();
        var product  = products.findById(productId).orElseThrow();

        // Kiểm tra tồn kho
        Integer current = product.getStock() == null ? 0 : product.getStock();
        if (!product.isInStock() || current < amount){
            throw new IllegalStateException("Sản phẩm hết hàng hoặc không đủ tồn kho");
        }

        Order order = new Order();
        order.setDate(LocalDate.now());
        order.setCustomer(customer);

        OrderLine line = new OrderLine();
        line.setOrder(order);
        line.setProduct(product);
        line.setAmount(amount);
        line.setPurchasePrice(product.getPrice());

        order.getOrderLines().add(line);

        // Trừ tồn kho và cập nhật trạng thái
        product.setStock(current - amount);
        if (product.getStock() <= 0){
            product.setInStock(false);
        }
        products.save(product);

        return orders.save(order);
    }

    public List<Order> findAll(){
        return orders.findAll();
    }

    public List<Order> findByCustomer(Integer customerId){
        return orders.findByCustomer_Id(customerId);
    }

    public Optional<Order> findById(Integer id){
        return orders.findById(id);
    }
}
