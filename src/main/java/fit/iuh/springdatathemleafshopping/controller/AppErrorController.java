package fit.iuh.springdatathemleafshopping.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AppErrorController {
    @GetMapping("/403")
    public String accessDenied(Model model) {
        model.addAttribute("title", "403 - Không có quyền");
        model.addAttribute("message", "Bạn không có quyền truy cập trang này.");
        return "error/403";
    }
}

