// src/main/java/demo/shop/web/HomeController.java
package fit.iuh.springdatathemleafshopping.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    @GetMapping("/") public String home(){ return "index"; }
}
