package com.frisk.cadettsplitterspadel.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http,
                                            RestAuthEntryPoint authEntryPoint,
                                            RestAccessDeniedHandler accessDeniedHandler) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // Admin
                        .requestMatchers(
                                "/api/wigellpadel/v1/listcanceled",
                                "/api/wigellpadel/v1/listupcoming",
                                "/api/wigellpadel/v1/listpast",
                                "/api/wigellpadel/v1/addcourt",
                                "/api/wigellpadel/v1/updatecourt",
                                "/api/wigellpadel/v1/remcourt/**"
                        ).hasRole("ADMIN")
                        // User
                        .requestMatchers(
                                "/api/wigellpadel/listcourts",
                                "/api/wigellpadel/checkavailability/**",
                                "/api/wigellpadel/v1/mybookings",
                                "/api/wigellpadel/v1/booking/bookcourt",
                                "/api/wigellpadel/v1/updatebooking",
                                "/api/wigellpadel/v1/cancelbooking",
                                "/api/wigellpadel/fx/**"
                        ).hasAnyRole("USER","ADMIN")
                        .anyRequest().permitAll()
                )
                .httpBasic(Customizer.withDefaults())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                );

        return http.build();
    }

    @Bean
    UserDetailsService users() {
        UserDetails u1 = User.withUsername("niklaseinarsson").password("{noop}Ne123!").roles("USER").build();
        UserDetails u2 = User.withUsername("benjaminportsmouth").password("{noop}Bp123!").roles("USER").build();
        UserDetails u3 = User.withUsername("christofferfrisk").password("{noop}Cf123!").roles("USER").build();
        UserDetails admin = User.withUsername("admin").password("{noop}Admin123!").roles("ADMIN").build();
        return new InMemoryUserDetailsManager(u1, u2, u3, admin);
    }

}

