package fit.iuh.springdatathemleafshopping.controller;

import fit.iuh.springdatathemleafshopping.enitity.Customer;
import fit.iuh.springdatathemleafshopping.enitity.dto.RegistrationForm;
import fit.iuh.springdatathemleafshopping.repository.CustomerRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequiredArgsConstructor
public class AuthController {
    private final UserDetailsManager userDetailsManager;
    private final PasswordEncoder passwordEncoder;
    private final CustomerRepository customerRepository;

    @GetMapping("/register")
    public String registerForm(Model model) {
        if (!model.containsAttribute("registrationForm")) {
            model.addAttribute("registrationForm", new RegistrationForm());
        }
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("registrationForm") RegistrationForm form,
                           BindingResult binding,
                           RedirectAttributes redirectAttributes) {
        if (userDetailsManager.userExists(form.getUsername())) {
            binding.rejectValue("username", "exists", "Username is already in use");
        }
        customerRepository.findByName(form.getUsername()).ifPresent(c ->
                binding.rejectValue("username", "customerExists", "A customer with this name already exists"));

        if (binding.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.registrationForm", binding);
            redirectAttributes.addFlashAttribute("registrationForm", form);
            return "redirect:/register";
        }

        UserDetails user = User.withUsername(form.getUsername())
                .password(passwordEncoder.encode(form.getPassword()))
                .roles("CUSTOMER")
                .build();
        userDetailsManager.createUser(user);

        Customer customer = Customer.builder()
                .name(form.getUsername())
                .customerSince(LocalDate.now())
                .build();
        customerRepository.save(customer);

        redirectAttributes.addFlashAttribute("success", "Registration successful. Please log in.");
        return "redirect:/register";
    }
}
