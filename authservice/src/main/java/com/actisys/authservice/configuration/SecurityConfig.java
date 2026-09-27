package com.actisys.authservice.configuration;

import com.actisys.authservice.oauth2.OAuth2ReactiveAuthenticationSuccessHandler;
import com.actisys.authservice.oauth2.CustomReactiveOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final OAuth2ReactiveAuthenticationSuccessHandler oauth2AuthenticationSuccessHandler;
    private final CustomReactiveOAuth2UserService customReactiveOAuth2UserService;

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/api/v1/auth/**").permitAll()
                        .pathMatchers("/oauth2/**", "/login/oauth2/**").permitAll()
                        .pathMatchers("/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**", "/webjars/swagger-ui/**").permitAll()
                        .pathMatchers("/actuator/**").permitAll()
                        .pathMatchers("/error").permitAll()
                        .anyExchange().permitAll()
                )
                .oauth2Login(oauth2 -> oauth2
                        .authenticationSuccessHandler(oauth2AuthenticationSuccessHandler)
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
