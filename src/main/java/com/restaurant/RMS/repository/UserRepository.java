package com.restaurant.RMS.repository;


import com.restaurant.RMS.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    public User findByEmail(String email);

    public User findByVerificationCode(String code);

    public User findByOtp(String otp);

    @Query("update User u set u.failedAttempt=?1 where u.email=?2")
    @Modifying
    public void updateFailedAttempt(int failedAttempt, String email);

    @Query("SELECT u FROM User u WHERE u.role = :role")
    List<User> findByRole(@Param("role") String role);

}
