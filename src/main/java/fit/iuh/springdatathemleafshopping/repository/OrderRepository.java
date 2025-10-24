package fit.iuh.springdatathemleafshopping.repository;

import fit.iuh.springdatathemleafshopping.enitity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Integer> {
    List<Order> findByCustomer_Id(Integer customerId);
}
