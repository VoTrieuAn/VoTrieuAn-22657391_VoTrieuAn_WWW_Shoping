// src/main/java/demo/shop/service/CustomerService.java
package fit.iuh.springdatathemleafshopping.service;

import fit.iuh.springdatathemleafshopping.enitity.Customer;
import fit.iuh.springdatathemleafshopping.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CustomerService {
    private final CustomerRepository repo;
    public CustomerService(CustomerRepository repo){ this.repo = repo; }

    public List<Customer> findAll(){ return repo.findAll(); }
}
