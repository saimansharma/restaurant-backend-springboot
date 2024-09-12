package com.restaurant.RMS.controller;

import com.restaurant.RMS.entity.User;
import com.restaurant.RMS.repository.UserRepository;
import com.restaurant.RMS.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;

@Controller
public class HomeController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @ModelAttribute
    public void commonUser(Principal p, Model m) {
        if (p != null) {
            String email = p.getName();
            User user = userRepository.findByEmail(email);
            m.addAttribute("user", user);
        }

    }

    @GetMapping("/")
    public String index() {
        return "redirect:/admin/dashboard";
    }


    @GetMapping("/signin")
    public String login(@RequestParam(value = "error", required = false) String error,
                        Model model) {
        if (error != null) {
            model.addAttribute("errorMessage", "Invalid username or password.");
        }

        return "login";
    }

}
