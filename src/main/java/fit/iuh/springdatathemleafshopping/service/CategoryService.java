// src/main/java/demo/shop/service/CategoryService.java
package fit.iuh.springdatathemleafshopping.service;

import fit.iuh.springdatathemleafshopping.enitity.Category;
import fit.iuh.springdatathemleafshopping.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CategoryService {
    private final CategoryRepository repo;
    public CategoryService(CategoryRepository repo){ this.repo = repo; }

    public List<Category> findAll(){ return repo.findAll(); }
}
