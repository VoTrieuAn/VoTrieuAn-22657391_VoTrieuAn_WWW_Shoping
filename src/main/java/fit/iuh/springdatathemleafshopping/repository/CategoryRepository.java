package fit.iuh.springdatathemleafshopping.repository;

import fit.iuh.springdatathemleafshopping.enitity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Integer> {}