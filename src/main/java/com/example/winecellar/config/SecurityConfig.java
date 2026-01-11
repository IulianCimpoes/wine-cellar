package com.example.winecellar.config;

import com.example.winecellar.common.logging.RequestIdFilter;
import com.example.winecellar.common.logging.RequestLoggingFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

@Configuration
@EnableMethodSecurity // enables @PreAuthorize
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http,
                                            RequestIdFilter requestIdFilter,
                                            RequestLoggingFilter requestLoggingFilter) throws Exception {
        http
                // Make sure RequestId is set even when authentication fails (401)
                .addFilterBefore(requestIdFilter, BasicAuthenticationFilter.class)
                // Optional: access log after requestId is in MDC/header
                .addFilterAfter(requestLoggingFilter, RequestIdFilter.class)
                .csrf(csrf -> csrf.disable())
                // H2 console needs frames
                .headers(headers -> headers.frameOptions(frame -> frame.disable()))
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .httpBasic(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth
                        // allow swagger endpoints without auth (optional but nice)
                        .requestMatchers("/v3/api-docs/**"
                                , "/swagger-ui/**"
                                , "/swagger-ui.html"
                                , "/h2-console/**"
                                , "/actuator/health/**")
                        .permitAll()
                        // everything else requires authentication; authorization is enforced via @PreAuthorize
                        .anyRequest()
                        .authenticated());

        return http.build();
    }

    @Bean
    UserDetailsService userDetailsService(PasswordEncoder encoder) {
        UserDetails user = User.withUsername("user")
                               .password(encoder.encode("userpass"))
                               .roles("USER")
                               .build();

        UserDetails admin = User.withUsername("admin")
                                .password(encoder.encode("adminpass"))
                                .roles("ADMIN")
                                .build();

        return new InMemoryUserDetailsManager(user, admin);
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
