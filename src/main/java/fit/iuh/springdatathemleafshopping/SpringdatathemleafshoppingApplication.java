package fit.iuh.springdatathemleafshopping;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import fit.iuh.springdatathemleafshopping.config.GeminiProperties;

@SpringBootApplication
@EnableConfigurationProperties(GeminiProperties.class)
public class SpringdatathemleafshoppingApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringdatathemleafshoppingApplication.class, args);
    }

}
