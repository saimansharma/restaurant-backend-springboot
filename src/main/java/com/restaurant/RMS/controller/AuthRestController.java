package com.restaurant.RMS.controller;

import com.restaurant.RMS.dto.AuthResponse;
import com.restaurant.RMS.dto.EmailDTO;
import com.restaurant.RMS.dto.UpdateEmailDTO;
import com.restaurant.RMS.dto.UserProfileDTO;
import com.restaurant.RMS.entity.User;
import com.restaurant.RMS.service.JWTUtils;
import com.restaurant.RMS.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthRestController {

    @Autowired
    private UserService userService;

    @Autowired
    private JWTUtils jwtUtils;

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody User user, HttpServletRequest request) {
        String url = request.getRequestURL().toString().replace(request.getServletPath(), "");
        User savedUser = userService.saveUser(user, url);

        if (savedUser != null) {
            return ResponseEntity.ok("Registered successfully");
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Something went wrong on the server");
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<AuthResponse> verifyUser(@RequestBody User user, HttpServletResponse response) {
        AuthResponse authResponse = userService.verifyAccountUsingOtp(user.getOtp());

        if (authResponse.getStatusCode() == 200) {

            //Set access token in Http-Only-Cookie
            Cookie accessTokenCookie = new Cookie("accessToken", authResponse.getToken());
            accessTokenCookie.setHttpOnly(true);
            accessTokenCookie.setSecure(true);
            accessTokenCookie.setPath("/");
            accessTokenCookie.setMaxAge(60*60); //1 hour
            accessTokenCookie.setAttribute("SameSite", "None"); // Cross-site cookies allowed
            response.addCookie(accessTokenCookie);

            //Set refresh token in Http-Only-Cookie
            Cookie refreshTokenCookie = new Cookie("refreshToken", authResponse.getRefreshToken());
            refreshTokenCookie.setHttpOnly(true);
            refreshTokenCookie.setSecure(true);
            refreshTokenCookie.setPath("/");
            refreshTokenCookie.setMaxAge(24*60*60); //1 hour
            refreshTokenCookie.setAttribute("SameSite", "None"); // Cross-site cookies allowed
            response.addCookie(refreshTokenCookie);

            return ResponseEntity.ok(authResponse);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(authResponse);
        }
    }

    @PostMapping("/update-email")
    public ResponseEntity<UpdateEmailDTO> updateEmail(@RequestBody UpdateEmailDTO updateEmailDTO, HttpServletRequest request) {
        try{
        String url = request.getRequestURL().toString().replace(request.getServletPath(), "");
        UpdateEmailDTO response = userService.updateEmail(updateEmailDTO, url);

        return ResponseEntity.status(HttpStatus.valueOf(response.getStatusCode())).body(response);
    }
        catch (Exception e){
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new UpdateEmailDTO(500, "Internal Server Error"));
    }
    }

    @PostMapping("/send-otp")
    public ResponseEntity<String> sendVerification(@RequestBody EmailDTO emailDTO, HttpServletRequest request) {
        String url = request.getRequestURL().toString().replace(request.getServletPath(), "");
        System.out.println(emailDTO);
        User updatedUser = userService.sendVerificationCode(emailDTO, url);

        if (updatedUser != null) {
            return ResponseEntity.ok("Resent Verification Code Succesfully");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Something went wrong! Try again");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> loginUser(@RequestBody User user, HttpServletResponse response) {
        AuthResponse authResponse = userService.authenticateUser(user.getEmail(), user.getPassword());

        if (authResponse.getStatusCode() == 200) {

            //Set access token in Http-Only-Cookie
            Cookie accessTokenCookie = new Cookie("accessToken", authResponse.getToken());
            accessTokenCookie.setHttpOnly(true);
            accessTokenCookie.setSecure(true);
            accessTokenCookie.setPath("/");
            accessTokenCookie.setMaxAge(60*60); //1 hour
            accessTokenCookie.setAttribute("SameSite", "None"); // Cross-site cookies allowed
            response.addCookie(accessTokenCookie);

            //Set refresh token in Http-Only-Cookie
            Cookie refreshTokenCookie = new Cookie("refreshToken", authResponse.getRefreshToken());
            refreshTokenCookie.setHttpOnly(true);
            refreshTokenCookie.setSecure(true);
            refreshTokenCookie.setPath("/");
            refreshTokenCookie.setMaxAge(24*60*60); //1 hour
            refreshTokenCookie.setAttribute("SameSite", "None"); // Cross-site cookies allowed
            response.addCookie(refreshTokenCookie);

            return ResponseEntity.ok(authResponse);

        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(authResponse);
        }
    }

    @PostMapping("/user/reset-password")
    public ResponseEntity<UserProfileDTO> resetPassword(@RequestBody UserProfileDTO userProfileDTO, HttpServletRequest request) {
        try {
            String token = getTokenFromCookies(request.getCookies());

            if (token == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new UserProfileDTO(401, "Unauthorized"));
            }

            String email = jwtUtils.extractUsername(token);

            UserProfileDTO response = userService.resetPassword(userProfileDTO, email);

            return ResponseEntity.status(HttpStatus.valueOf(response.getStatusCode())).body(response);
        }
        catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new UserProfileDTO(500, "Internal Server Error"));
        }
    }

        @PostMapping("/user/profile")
    public ResponseEntity<UserProfileDTO> getUserProfile(HttpServletRequest request) {
        try{
            String token = getTokenFromCookies(request.getCookies());

            if (token == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new UserProfileDTO(401, "Unauthorized"));
            }

            String email = jwtUtils.extractUsername(token);

            UserProfileDTO response = userService.getUserProfile(email);

            return ResponseEntity.status(HttpStatus.valueOf(response.getStatusCode())).body(response);
            }
        catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new UserProfileDTO(500, "Internal Server Error"));
        }
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<String> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refreshToken".equals(cookie.getName())){
                    String refreshToken = cookie.getValue();
                    if(jwtUtils.validateRefreshToken(refreshToken)){
                        String newAccessToken = userService.generateNewAccessToken(refreshToken);

                        Cookie newAccessTokenCookie = new Cookie("accessToken", newAccessToken);
                        newAccessTokenCookie.setHttpOnly(true);
                        newAccessTokenCookie.setSecure(true);
                        newAccessTokenCookie.setPath("/");
                        newAccessTokenCookie.setMaxAge(60*60); //1 hour
                        newAccessTokenCookie.setAttribute("SameSite", "None"); // Cross-site cookies allowed
                        response.addCookie(newAccessTokenCookie);

                        return ResponseEntity.ok("Access Token Refreshed");
                    }
                }
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Refresh Token");
    }

    @PostMapping("/user/update-profile")
    public ResponseEntity<UserProfileDTO> updateProfile(@RequestBody UserProfileDTO userProfileDTO, HttpServletRequest request) {
        try {
            String token = getTokenFromCookies(request.getCookies());

            if (token == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new UserProfileDTO(401, "Unauthorized"));
            }

            String email = jwtUtils.extractUsername(token);

            UserProfileDTO response = userService.updateUserDetails(userProfileDTO, email);

            return ResponseEntity.status(HttpStatus.valueOf(response.getStatusCode())).body(response);
        }
        catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new UserProfileDTO(500, "Internal Server Error"));
        }
    }

    @PostMapping("/user/update-password")
    public ResponseEntity<UserProfileDTO> updatePassword(@RequestBody UserProfileDTO userProfileDTO, HttpServletRequest request) {
        try {
            String token = getTokenFromCookies(request.getCookies());

            if (token == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new UserProfileDTO(401, "Unauthorized"));
            }

            String email = jwtUtils.extractUsername(token);

            UserProfileDTO response = userService.updateUserPassword(userProfileDTO, email);

            return ResponseEntity.status(HttpStatus.valueOf(response.getStatusCode())).body(response);
        }
        catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new UserProfileDTO(500, "Internal Server Error"));
        }
    }

    @PostMapping("/user/logout")
    public void logout(HttpServletRequest request, HttpServletResponse response) {

        request.getSession().invalidate();

        //Set access token in Http-Only-Cookie
        Cookie accessTokenCookie = new Cookie("accessToken", null);
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setSecure(true);
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge(0); //1 hour
        accessTokenCookie.setAttribute("SameSite", "None"); // Cross-site cookies allowed
        response.addCookie(accessTokenCookie);

        //Set refresh token in Http-Only-Cookie
        Cookie refreshTokenCookie = new Cookie("refreshToken", null);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(true);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(0); //1 hour
        refreshTokenCookie.setAttribute("SameSite", "None"); // Cross-site cookies allowed
        response.addCookie(refreshTokenCookie);

        response.setStatus(HttpServletResponse.SC_OK);

    }

    private String getTokenFromCookies(Cookie[] cookies) {
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("accessToken")) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

}

