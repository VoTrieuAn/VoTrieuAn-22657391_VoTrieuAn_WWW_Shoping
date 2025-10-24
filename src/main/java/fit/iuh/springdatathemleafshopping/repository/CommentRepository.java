package fit.iuh.springdatathemleafshopping.repository;

import fit.iuh.springdatathemleafshopping.enitity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Integer> {}