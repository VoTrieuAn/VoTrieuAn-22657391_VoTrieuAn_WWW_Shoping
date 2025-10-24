package fit.iuh.springdatathemleafshopping.repository;

import fit.iuh.springdatathemleafshopping.enitity.OrderLine;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderLineRepository extends JpaRepository<OrderLine, Long> {}