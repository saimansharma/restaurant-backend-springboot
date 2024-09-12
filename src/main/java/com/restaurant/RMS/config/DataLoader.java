package com.restaurant.RMS.config;

import com.restaurant.RMS.repository.UserRepository;
import com.restaurant.RMS.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final UserService userService;

    @Value("${adpassword}")
    private String admin_password;

    public DataLoader(UserRepository userRepository, UserService userService) {
        this.userRepository = userRepository;
        this.userService = userService;
    }

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.findByEmail("superadmin@restaurant.com") == null) {
            userService.createSuperAdmin("superadmin@restaurant.com", admin_password);
        }
    }
}
