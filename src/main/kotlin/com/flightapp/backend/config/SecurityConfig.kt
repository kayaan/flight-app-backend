package com.flightapp.backend.config

import com.flightapp.backend.auth.FlightAppOidcUserService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.SecurityFilterChain

@Configuration
class SecurityConfig(
    private val oidcUserService: FlightAppOidcUserService
) {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        return http
            .csrf { csrf -> csrf.disable() }
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers("/api/health").permitAll()
                    .requestMatchers("/api/test/users/**").permitAll()
                    .requestMatchers("/api/oauth/failure").permitAll()
                    .requestMatchers("/oauth2/**").permitAll()
                    .requestMatchers("/login/**").permitAll()
                    .requestMatchers("/error").permitAll()
                    .requestMatchers("/").permitAll()
                    .anyRequest().authenticated()
            }
            .oauth2Login { oauth ->
                oauth
                    .userInfoEndpoint { userInfo ->
                        userInfo.oidcUserService(oidcUserService)
                    }
                    .defaultSuccessUrl("/api/health", true)
                    .failureUrl("/api/oauth/failure")
            }
            .logout { logout ->
                logout.logoutSuccessUrl("/")
            }
            .build()
    }
}