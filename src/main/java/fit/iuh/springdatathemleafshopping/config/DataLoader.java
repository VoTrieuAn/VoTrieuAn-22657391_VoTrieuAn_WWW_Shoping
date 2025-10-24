package fit.iuh.springdatathemleafshopping.config;

import fit.iuh.springdatathemleafshopping.enitity.Customer;
import fit.iuh.springdatathemleafshopping.enitity.Product;
import fit.iuh.springdatathemleafshopping.repository.CustomerRepository;
import fit.iuh.springdatathemleafshopping.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.stream.IntStream;

@Configuration
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;

    @Override
    public void run(String... args) {
        if (productRepository.count() == 0) {
            IntStream.rangeClosed(1, 40).forEach(i -> {
                productRepository.save(Product.builder()
                        .name("Sản phẩm " + i)
                        .price(BigDecimal.valueOf(100000L * i))
                        .inStock(true)
                        .stock(10 + (i % 5))
                        .build());
            });
        }

        if (customerRepository.count() == 0) {
            customerRepository.save(Customer.builder()
                    .name("customer")
                    .customerSince(LocalDate.now())
                    .build());
            customerRepository.save(Customer.builder()
                    .name("Alice")
                    .customerSince(LocalDate.now())
                    .build());
        }
    }
}
