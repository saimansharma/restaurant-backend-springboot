package com.restaurant.RMS.service;

import com.restaurant.RMS.dto.AuthResponse;
import com.restaurant.RMS.dto.EmailDTO;
import com.restaurant.RMS.dto.UpdateEmailDTO;
import com.restaurant.RMS.dto.UserProfileDTO;
import com.restaurant.RMS.entity.User;
import jakarta.mail.MessagingException;
import org.springframework.mail.MailSendException;

import javax.net.ssl.SSLHandshakeException;
import java.util.List;

public interface UserService {

    public List<User> getAllUsers();

    public User getUserById(long id);

    public List<User> getAdminUsers();

    public User saveUser(User user, String url);

    public UserProfileDTO updateUserDetails(UserProfileDTO userProfileDTO, String email);

    public UserProfileDTO updateUserPassword(UserProfileDTO userProfileDTO, String email);

    public UserProfileDTO resetPassword(UserProfileDTO userProfileDTO, String email);

    public UpdateEmailDTO updateEmail(UpdateEmailDTO updateEmailDTO, String url);

    public User sendVerificationCode(EmailDTO emailDTO, String url);

    public void deleteUserById(long id);

    public User saveAdmin(User user);

    public void removeSessionMessage();

    public void sendEmail(User user, String url);

    public boolean verifyAccount(String verificationCode, String otp);

    public AuthResponse verifyAccountUsingOtp(String otp);

    public void increaseFailedAttempt(User user);

    public void resetFailedAttempt(String email);

    public void lock(User user);

    public boolean unlockWhenTimeExpired(User user);

    public void createSuperAdmin(String email, String rawPassword);

    public AuthResponse authenticateUser(String email, String password);

    public UserProfileDTO getUserProfile(String email);

    public String generateNewAccessToken(String refreshToken);
}
