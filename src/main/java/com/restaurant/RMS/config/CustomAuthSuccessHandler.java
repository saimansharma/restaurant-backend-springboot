package com.restaurant.RMS.config;

import com.restaurant.RMS.entity.User;
import com.restaurant.RMS.service.JWTUtils;
import com.restaurant.RMS.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Set;

@Component
public class CustomAuthSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserService userService;

    private JWTUtils jwtUtils;

    @Autowired
    public CustomAuthSuccessHandler(@Lazy UserService userService, JWTUtils jwtUtils)
    {
        this.userService = userService;
        this.jwtUtils = jwtUtils;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        Set<String> roles = AuthorityUtils.authorityListToSet(authentication.getAuthorities());

        CustomUser userDetails = (CustomUser) authentication.getPrincipal();

        if (userDetails != null) {

            String token = jwtUtils.generateToken(userDetails);

            //Set access token in Http-Only-Cookie
            Cookie accessTokenCookie = new Cookie("accessToken", token);
            accessTokenCookie.setHttpOnly(true);
            accessTokenCookie.setSecure(false);
            accessTokenCookie.setPath("/");
            accessTokenCookie.setMaxAge(60*60); //1 hour
            response.addCookie(accessTokenCookie);

            //Set refresh token in Http-Only-Cookie
            Cookie refreshTokenCookie = new Cookie("refreshToken", token);
            refreshTokenCookie.setHttpOnly(true);
            refreshTokenCookie.setSecure(false);
            refreshTokenCookie.setPath("/");
            refreshTokenCookie.setMaxAge(24*60*60); //1 hour
            response.addCookie(refreshTokenCookie);

            User user = userDetails.getUser();

            if (user.getFailedAttempt() > 0) {
                userService.resetFailedAttempt(user.getEmail());
            }

            if (roles.contains("ROLE_ADMIN") || roles.contains("ROLE_SUPERADMIN")) {
                response.sendRedirect("/admin/dashboard");
            }

            super.onAuthenticationSuccess(request, response, authentication);
        }
    }

}
