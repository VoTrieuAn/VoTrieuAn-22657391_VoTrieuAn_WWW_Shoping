// src/main/java/demo/shop/service/ProductService.java
package fit.iuh.springdatathemleafshopping.service;

import fit.iuh.springdatathemleafshopping.enitity.Product;
import fit.iuh.springdatathemleafshopping.repository.ProductRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {
    private final ProductRepository repo;
    public ProductService(ProductRepository repo){ this.repo = repo; }

    public List<Product> findAll(){ return repo.findAll(); }
    public Optional<Product> findById(Integer id){ return repo.findById(id); }
    public Product save(Product p){ return repo.save(p); }
}
