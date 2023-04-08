package com.corporate.online.learning.platform.config.security;

import com.corporate.online.learning.platform.config.security.logout.CustomLogoutHandler;
import com.corporate.online.learning.platform.config.security.logout.CustomLogoutSuccessHandler;
import com.corporate.online.learning.platform.model.account.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final AuthenticationProvider authenticationProvider;
    private final CustomLogoutHandler customLogoutHandler;
    private final CustomLogoutSuccessHandler customLogoutSuccessHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.cors()
                .and()
                .csrf()
                .disable()
                .authorizeHttpRequests()
                .requestMatchers("/api/v1/admin/**")
                .hasAnyAuthority(Role.ADMIN.toString())
                .requestMatchers("/api/v1/hr/**")
                .hasAnyAuthority(Role.HR_REP.toString())
                .requestMatchers("/api/v1/trainee/**")
                .hasAnyAuthority(Role.TRAINEE.toString())
                .requestMatchers("/api/v1/trainer/**")
                .hasAnyAuthority(Role.TRAINER.toString())
                .requestMatchers("/api/v1/auth/**")
                .permitAll()
                .anyRequest()
                .authenticated()
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .logout()
                .logoutUrl("/api/v1/auth/logout")
                .addLogoutHandler(customLogoutHandler)
                .logoutSuccessHandler(customLogoutSuccessHandler);

        return httpSecurity.build();
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/v1/**")
                        .allowedOrigins("*")
                        .allowedMethods("*");
            }
        };
    }
}
