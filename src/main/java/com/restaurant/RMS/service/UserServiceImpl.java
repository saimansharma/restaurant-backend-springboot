package com.restaurant.RMS.service;

import com.restaurant.RMS.config.CustomUser;
import com.restaurant.RMS.dto.AuthResponse;
import com.restaurant.RMS.dto.EmailDTO;
import com.restaurant.RMS.dto.UpdateEmailDTO;
import com.restaurant.RMS.dto.UserProfileDTO;
import com.restaurant.RMS.entity.User;
import com.restaurant.RMS.repository.UserRepository;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.security.SecureRandom;
import java.util.*;

@Service
@Transactional
public class UserServiceImpl implements UserService{


    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;
    @Autowired
    private JWTUtils jwtUtils;


    @Autowired
    public UserServiceImpl(UserRepository userRepository, @Lazy BCryptPasswordEncoder passwordEncoder, JavaMailSender mailSender) {  // Lazy annotation for bcrypt because the bean is being created in security config and user service instance is used in custom success handler leading to circular reference.
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailSender = mailSender;
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User getUserById(long id) {
        Optional<User> result = userRepository.findById(id);

        User user = null;
        if(result.isPresent()){
            user = result.get();
        }
        else{
            throw new RuntimeException("Did not find the wine with id: "+ id);
        }
        return user;
    }

    @Override
    public User saveUser(User user, String url) {

        String password = passwordEncoder.encode(user.getPassword());
        user.setPassword(password);
        user.setRole("ROLE_USER");
        user.setEnable(false);
        user.setVerificationCode(UUID.randomUUID().toString());
        user.setOtp(generateSecureOTP());
        user.setAccountNonLocked(true);
        user.setFailedAttempt(0);
        user.setLockTime(null);

        User newuser = userRepository.save(user);

        sendEmail(user, url); //sending verfication mail along with base url

        return newuser;
    }

    @Override
    public UserProfileDTO updateUserDetails(UserProfileDTO userProfileDTO, String email) {
        User existingUser = userRepository.findByEmail(email);

        if (existingUser == null) {
            return new UserProfileDTO(404, "User not found");
        }

        if (userProfileDTO.getName() == null || userProfileDTO.getName().isEmpty() ||
            userProfileDTO.getMobileNo() == null || userProfileDTO.getMobileNo().isEmpty()) {
            return new UserProfileDTO(400, "Empty Field");
        }

        existingUser.setName(userProfileDTO.getName());
        existingUser.setMobileNo(userProfileDTO.getMobileNo());
        userRepository.save(existingUser);

        return new UserProfileDTO(200, "User Profile Updated Successfully");
    }

    @Override
    public UserProfileDTO updateUserPassword(UserProfileDTO userProfileDTO, String email) {
        User existingUser = userRepository.findByEmail(email);

        if(existingUser == null) {
            return new UserProfileDTO(404, "User not found");
        }

        if (userProfileDTO.getOldPassword() == null || userProfileDTO.getOldPassword().isEmpty() ||
            userProfileDTO.getNewPassword() == null || userProfileDTO.getNewPassword().isEmpty()) {
            return new UserProfileDTO(400, "Empty Field");
        }

        if(!passwordEncoder.matches(userProfileDTO.getOldPassword(), existingUser.getPassword())){
            return new UserProfileDTO(401, "Wrong Current Password");
        }

        existingUser.setPassword(passwordEncoder.encode(userProfileDTO.getNewPassword()));
        userRepository.save(existingUser);

        return new UserProfileDTO(200, "Updated Password Successfully");
    }

    @Override
    public UserProfileDTO resetPassword(UserProfileDTO userProfileDTO, String email) {
        User existingUser = userRepository.findByEmail(email);

        if(existingUser == null) {
            return new UserProfileDTO(404, "User not found");
        }

        if (userProfileDTO.getNewPassword() == null || userProfileDTO.getNewPassword().isEmpty()) {
            return new UserProfileDTO(400, "Empty Field");
        }

        existingUser.setPassword(passwordEncoder.encode(userProfileDTO.getNewPassword()));
        userRepository.save(existingUser);

        return new UserProfileDTO(200, "Password Reset Successful");
    }

    @Override
    public UpdateEmailDTO updateEmail(UpdateEmailDTO updateEmailDTO, String url) {
        User existingUser = userRepository.findByEmail(updateEmailDTO.getCurrentEmail());

        if(existingUser == null) {
            return new UpdateEmailDTO(404, "User not found");
        }

        existingUser.setEmail(updateEmailDTO.getNewEmail());
        existingUser.setEnable(false);
        existingUser.setVerificationCode(UUID.randomUUID().toString());
        existingUser.setOtp(generateSecureOTP());
        userRepository.save(existingUser);

        sendEmail(existingUser, url);

        return new UpdateEmailDTO(200, "Updated Email Successfully");
    }

    @Override
    public User sendVerificationCode(EmailDTO emailDTO, String url) {
        User existingUser = userRepository.findByEmail(emailDTO.getEmail());

        if (existingUser != null) {
            existingUser.setVerificationCode(UUID.randomUUID().toString());
            existingUser.setOtp(generateSecureOTP());
            userRepository.save(existingUser);

            sendEmail(existingUser, url);

            return existingUser;
        } else {
            throw new RuntimeException("User not found");
        }
    }


    private String generateSecureOTP() {
        SecureRandom secureRandom = new SecureRandom();
        int otpValue = 100000 + secureRandom.nextInt(900000);
        return String.valueOf(otpValue);
    }


    @Override
    public void deleteUserById(long id) {
        userRepository.deleteById(id);
    }

    @Override
    public List<User> getAdminUsers() {
        return userRepository.findByRole("ROLE_ADMIN");
    }

    @Override
    public User saveAdmin(User user) {

        if (user.getId() == 0) { // New admin
            String defaultPassword = passwordEncoder.encode("admin");
            user.setPassword(defaultPassword); // Set default password for new admin
            user.setEnable(true);
        }
        else {
            User existingUser = userRepository.findById(user.getId()).orElseThrow(() -> new IllegalArgumentException("Invalid user ID: " + user.getId()));
            user.setPassword(existingUser.getPassword());
        }
        user.setRole("ROLE_ADMIN");
        user.setMobileNo(null);
        user.setVerificationCode(null);
        user.setAccountNonLocked(true);
        user.setFailedAttempt(0);
        user.setLockTime(null);

        User newuser = userRepository.save(user);

        return newuser;
    }

    @Override
    public void sendEmail(User user, String url) {

        String from = "smtprestaurant@gmail.com";
        String to = user.getEmail();
        String subject = "Account Verification";
        String content = "Dear [[name]],<br>" +
        "Please click the link below to verify your registration:<br>" +
                "<h3><a href=\"[[URL]]\" target=\"_self\">VERIFY</a></h3>" +
                "<br>Or enter the following OTP in the verification page:<br>" +
                "<h3>[[OTP]]</h3>" +
                "Thank you,<br>" +
                "Gericht Restaurant";

        try{

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message);

            helper.setFrom(from, "Gericht Restaurant");
            helper.setTo(to);
            helper.setSubject(subject);

            content = content.replace("[[name]]", user.getName());
            String siteUrl = url + "/verify?code=" + user.getVerificationCode();
            content = content.replace("[[URL]]", siteUrl);
            content = content.replace("[[OTP]]", user.getOtp());

            helper.setText(content, true);

            mailSender.send(message);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public boolean verifyAccount(String verificationCode, String otp) {

        User user = null;

        if(verificationCode != null){
            user = userRepository.findByVerificationCode(verificationCode);

        } else if (otp != null) {
            user = userRepository.findByOtp(otp);
        }

        if (user == null) {
            return false;
        } else {
            user.setEnable(true);
            user.setVerificationCode(null);
            user.setOtp(null);
            userRepository.save(user);

            return true;
        }
    }

    @Override
    public AuthResponse verifyAccountUsingOtp(String otp) {
        User user = userRepository.findByOtp(otp);
        AuthResponse resp = new AuthResponse();

        if (user != null) {
            user.setEnable(true);
            user.setVerificationCode(null);
            user.setOtp(null);
            userRepository.save(user);

            CustomUser customUser = new CustomUser(user);

            var jwtToken = jwtUtils.generateToken(customUser);
            var refreshToken = jwtUtils.generateRefreshToken(new HashMap<>(),customUser);

            resp.setMessage("Account Verified Succesfully");
            resp.setToken(jwtToken); // Was using when storing in the local storage
            resp.setRefreshToken(refreshToken);
//            resp.setExpirationTime("1 Hour");
            resp.setStatusCode(200);
            resp.setUsername(user.getName());

        }
        else{
            resp.setStatusCode(500);
            resp.setError("Verification Unsuccessful");
        }
        return resp;
    }


    @Override
    public void removeSessionMessage() {

        HttpSession session =
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                        .getRequest()
                        .getSession();

        session.removeAttribute("msg");

    }

    public static final int MAX_FAILED_ATTEMPTS = 3;

    private static final long LOCK_TIME_DURATION = 30000;

    @Override
    public void increaseFailedAttempt(User user) {
        int newFailedAttempt = user.getFailedAttempt() + 1;
        userRepository.updateFailedAttempt(newFailedAttempt, user.getEmail());
    }

    @Override
    public void resetFailedAttempt(String email) {
        userRepository.updateFailedAttempt(0,email);

    }

    @Override
    public void lock(User user) {
        user.setAccountNonLocked(false);
        user.setLockTime(new Date());
        userRepository.save(user);
    }

    @Override
    public boolean unlockWhenTimeExpired(User user) {
        long lockTimeInMillis = user.getLockTime().getTime();
        long currentTimeInMillis = System.currentTimeMillis();

        if(lockTimeInMillis + LOCK_TIME_DURATION < currentTimeInMillis){
            user.setAccountNonLocked(true);
            user.setFailedAttempt(0);
            user.setLockTime(null);
            userRepository.save(user);
            return true;
        }
        return false;
    }

    @Override
    public void createSuperAdmin(String email, String rawPassword) {
        User superAdmin = new User();
        superAdmin.setEmail(email);
        superAdmin.setPassword(passwordEncoder.encode(rawPassword));
        superAdmin.setRole("ROLE_SUPERADMIN");
        superAdmin.setMobileNo(null);
        superAdmin.setEnable(true);
        superAdmin.setVerificationCode(null);
        superAdmin.setAccountNonLocked(true);
        superAdmin.setFailedAttempt(0);
        superAdmin.setLockTime(null);

        userRepository.save(superAdmin);
    }

    @Override
    public AuthResponse authenticateUser(String email, String password) {
        User user = userRepository.findByEmail(email);
        AuthResponse resp = new AuthResponse();
        if(user != null && passwordEncoder.matches(password, user.getPassword()) && user.isEnable() ) {
            CustomUser customUser = new CustomUser(user);

            var jwtToken = jwtUtils.generateToken(customUser);
            var refreshToken = jwtUtils.generateRefreshToken(new HashMap<>(),customUser);

            resp.setMessage("User Authenticated Successfully");
            resp.setToken(jwtToken);
            resp.setRefreshToken(refreshToken);
            resp.setStatusCode(200);
            resp.setUsername(user.getName());
        }
        else{
            resp.setStatusCode(500);
            resp.setError("Invalid email or password");
        }
        return resp;
        }

    @Override
    public UserProfileDTO getUserProfile(String email) {
        User user = userRepository.findByEmail(email);

        if(user == null) {
            return new UserProfileDTO(404, "User not found");
        }

        String userName = user.getName();
        String userEmail = user.getEmail();
        String userMobileNo = user.getMobileNo();

        return new UserProfileDTO(200, "Account Verified Successfully", userName, userEmail, userMobileNo);
    }

    @Override
    public String generateNewAccessToken(String refreshToken) {
        String username = jwtUtils.extractUsername(refreshToken);
        User user = userRepository.findByEmail(username);

        if(user != null) {
            CustomUser customUser = new CustomUser(user);

            return jwtUtils.generateToken(customUser);
        }
        else{
            throw new RuntimeException("User not found for the provided refresh token");
        }
    }

}


