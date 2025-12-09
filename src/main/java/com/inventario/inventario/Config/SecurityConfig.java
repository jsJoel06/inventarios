package com.inventario.inventario.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                //  ACTIVAR CORS GLOBAL
                .cors(cors -> {})

                //  DESACTIVAR CSRF
                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(auth -> auth

                        //  PERMITIR PETICIONES PREFLIGHT
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        //  PERMITIR TODO SIN AUTENTICACIÓN
                        .requestMatchers("/movimientos/**").permitAll()
                        .requestMatchers("/login/**").permitAll()
                        .requestMatchers("/productos/**").permitAll()
                        .requestMatchers("/facturas/**").permitAll()

                        //  EL RESTO TAMBIÉN PERMITIDO
                        .anyRequest().permitAll()
                )
                .build();
    }

    //  CORS GLOBAL REAL PARA REACT
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOrigins(List.of("https://inventario-7yzy.onrender.com"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }

    //  PASSWORD ENCODER
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
