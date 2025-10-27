package fit.iuh.springdatathemleafshopping.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfiguration {

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public UserDetailsManager userDetailsManager(PasswordEncoder encoder) {
                UserDetails admin = User.withUsername("admin")
                                .password(encoder.encode("123"))
                                .roles("ADMIN")
                                .build();
                UserDetails customer = User.withUsername("customer")
                                .password(encoder.encode("111"))
                                .roles("CUSTOMER")
                                .build();
                return new InMemoryUserDetailsManager(admin, customer);
        }

        @Bean
        SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers("/", "/login", "/register", "/css/**", "/js/**",
                                                                "/images/**", "/resources/**")
                                                .permitAll()

                                                .requestMatchers("/products/new", "/products/*/edit").hasRole("ADMIN")
                                                .requestMatchers(HttpMethod.POST, "/products").hasRole("ADMIN")
                                                .requestMatchers(HttpMethod.POST, "/products/*").hasRole("ADMIN")
                                                .requestMatchers(HttpMethod.POST, "/products/*/delete").hasRole("ADMIN")

                                                .requestMatchers("/orders/**").hasAnyRole("CUSTOMER", "ADMIN")
                                                .requestMatchers(HttpMethod.GET, "/cart/checkout").authenticated()
                                                .requestMatchers(HttpMethod.POST, "/cart/checkout").authenticated()
                                                .requestMatchers("/cart/**").permitAll()
                                                .requestMatchers("/comments/**").hasAnyRole("CUSTOMER", "ADMIN")
                                                .requestMatchers(HttpMethod.POST, "/products/*/comments")
                                                .hasAnyRole("CUSTOMER", "ADMIN")
                                                .requestMatchers("/customers/**").hasRole("ADMIN")

                                                .requestMatchers(HttpMethod.GET, "/products/**").permitAll()

                                                .anyRequest().authenticated())
                                .formLogin(form -> form
                                                // .loginPage("/login")
                                                .defaultSuccessUrl("/products", true)
                                                .permitAll())
                                .exceptionHandling(ex -> ex
                                                .accessDeniedPage("/403")
                                )
                                .logout(logout -> logout
                                                .logoutUrl("/logout")
                                                .logoutSuccessUrl("/login?logout=true")
                                                .invalidateHttpSession(true)
                                                .deleteCookies("JSESSIONID")
                                                .permitAll());

                return http.build();
        }
}
