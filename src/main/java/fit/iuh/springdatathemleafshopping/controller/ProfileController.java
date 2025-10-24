package fit.iuh.springdatathemleafshopping.controller;

import fit.iuh.springdatathemleafshopping.enitity.Customer;
import fit.iuh.springdatathemleafshopping.enitity.dto.ProfileForm;
import fit.iuh.springdatathemleafshopping.repository.CustomerRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class ProfileController {
    private final CustomerRepository customerRepository;
    private final UserDetailsManager userDetailsManager;

    @GetMapping("/profile")
    public String profile(Authentication auth, Model model){
        if (auth != null){
            List<String> roles = auth.getAuthorities().stream().map(a -> a.getAuthority()).collect(Collectors.toList());
            String username = auth.getName();
            Optional<Customer> customerOpt = customerRepository.findByName(username);
            customerOpt.ifPresent(c -> model.addAttribute("customerId", c.getId()));
            model.addAttribute("username", username);
            model.addAttribute("roles", roles);
        }
        return "profile/index";
    }

    @GetMapping("/profile/edit")
    @PreAuthorize("hasRole('CUSTOMER')")
    public String editProfile(Authentication auth, Model model, RedirectAttributes redirectAttributes){
        if (auth == null){
            return "redirect:/login";
        }
        String username = auth.getName();
        Optional<Customer> customerOpt = customerRepository.findByName(username);
        if (customerOpt.isEmpty()){
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy thông tin khách hàng");
            return "redirect:/profile";
        }
        if (!model.containsAttribute("profileForm")){
            Customer customer = customerOpt.get();
            ProfileForm form = new ProfileForm();
            form.setUsername(username);
            form.setCustomerSince(customer.getCustomerSince());
            model.addAttribute("profileForm", form);
        }
        return "profile/edit";
    }

    @PostMapping("/profile/edit")
    @PreAuthorize("hasRole('CUSTOMER')")
    public String updateProfile(@Valid @ModelAttribute("profileForm") ProfileForm form,
                                BindingResult binding,
                                Authentication auth,
                                RedirectAttributes redirectAttributes){
        if (auth == null){
            return "redirect:/login";
        }
        String currentUsername = auth.getName();
        Optional<Customer> customerOpt = customerRepository.findByName(currentUsername);
        if (customerOpt.isEmpty()){
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy thông tin khách hàng hiện tại");
            return "redirect:/profile";
        }

        Customer customer = customerOpt.get();

        if (!currentUsername.equals(form.getUsername()) && userDetailsManager.userExists(form.getUsername())){
            binding.rejectValue("username", "exists", "Tên đăng nhập đã được sử dụng");
        }

        if (form.getCustomerSince() == null){
            form.setCustomerSince(customer.getCustomerSince());
        }

        if (binding.hasErrors()){
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.profileForm", binding);
            redirectAttributes.addFlashAttribute("profileForm", form);
            return "redirect:/profile/edit";
        }

        customer.setName(form.getUsername());
        customer.setCustomerSince(form.getCustomerSince());
        customerRepository.save(customer);

        if (!currentUsername.equals(form.getUsername())){
            UserDetails currentUser = userDetailsManager.loadUserByUsername(currentUsername);
            UserDetails updatedUser = User.withUserDetails(currentUser)
                    .username(form.getUsername())
                    .build();
            userDetailsManager.createUser(updatedUser);
            userDetailsManager.deleteUser(currentUsername);
            Authentication newAuth = new UsernamePasswordAuthenticationToken(
                    updatedUser, auth.getCredentials(), updatedUser.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(newAuth);
        }

        redirectAttributes.addFlashAttribute("success", "Cập nhật hồ sơ thành công");
        return "redirect:/profile";
    }
}
