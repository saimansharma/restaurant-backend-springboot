package com.restaurant.RMS.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableScheduling
public class SecurityConfig {

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService getDetailsService() {
        return new CustomUserDetailsService();
    }


    @Autowired
    private JWTAuthFilter jwtAuthFilter;

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception{
        return authenticationConfiguration.getAuthenticationManager();
    }


    @Bean
    public DaoAuthenticationProvider getAuthenticationProvider() {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setUserDetailsService(getDetailsService());
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
        return daoAuthenticationProvider;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable) //disabling csrf
                .authorizeHttpRequests(authorize -> authorize //using lambdas removes the need to chain the authentication with .and()
                        .requestMatchers("/admin/dashboard/admins/**").hasRole("SUPERADMIN")
                        .requestMatchers("/admin/dashboard/**").hasAnyRole("ADMIN", "SUPERADMIN")
                        .requestMatchers("api/auth/user/**").hasRole("USER")
                        .requestMatchers("api/auth/login", "api/auth/register", "/api/**", "/awardImages/**").permitAll()
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/signin")
                        .loginProcessingUrl("/userLogin")
                        .failureHandler(failureHandler)
                        .successHandler(successHandler)
                        .failureUrl("/signin?error=true")  // redirects to the login page with the error parameter
                        .permitAll())
                .logout(logout -> logout
                        .deleteCookies("accessToken", "refreshToken")
                        .logoutSuccessUrl("/signin?logout=true")  // redirects to the login page with the logout parameter
                )
                .sessionManagement(manager->manager.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(getAuthenticationProvider()).addFilterBefore(
                        jwtAuthFilter, UsernamePasswordAuthenticationFilter.class
                );
        return http.build();
    }

    @Autowired
    public CustomAuthSuccessHandler successHandler;

    @Autowired
    public CustomFailureHandler failureHandler;

}

