package com.restaurant.RMS.config;

import com.restaurant.RMS.entity.User;
import com.restaurant.RMS.repository.UserRepository;
import com.restaurant.RMS.service.UserService;
import com.restaurant.RMS.service.UserServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomFailureHandler extends SimpleUrlAuthenticationFailureHandler {
    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        System.out.println("Failure Handler");
        String email = request.getParameter("username");
        User user = userRepository.findByEmail(email);

        if(user != null) {
            if(user.isEnable()) {

                if(user.isAccountNonLocked()) {
                    if (user.getFailedAttempt() < UserServiceImpl.MAX_FAILED_ATTEMPTS - 1) {
                        userService.increaseFailedAttempt(user);
                    }
                    else {
                        userService.lock(user);
                        exception = new LockedException(
                                "Account locked 3 failed attempt ! It will be unlocked after 1 minutes"
                        );
                    }
                } else if (!user.isAccountNonLocked()) {
                        if (userService.unlockWhenTimeExpired(user)) {
                            exception = new LockedException("Your account is unlocked, Again login");
                        } else {
                            exception = new LockedException("Your account is locked, please try again after some time.");
                        }
                    }
            } else {
                    exception = new LockedException("Your account is inactive, please verify");
            }
        }
        super.setDefaultFailureUrl("/signin?error");
        super.onAuthenticationFailure(request, response, exception);
    }

}

