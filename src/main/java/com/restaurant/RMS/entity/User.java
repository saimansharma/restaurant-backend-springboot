package com.restaurant.RMS.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String name;

    private String mobileNo;

    private String email;

    private String password;

    private String role;

    private boolean enable;

    private String otp;

    private String verificationCode;

    @Column(name = "account_non_locked")
    private boolean accountNonLocked;

    @Column(name = "failed_attempt")
    private int failedAttempt;

    @Column(name = "lock_time")
    private Date lockTime;

}
