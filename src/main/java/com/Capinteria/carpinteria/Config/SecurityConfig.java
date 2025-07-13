package com.Capinteria.carpinteria.Config;


import com.Capinteria.carpinteria.Jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final AuthenticationProvider authProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeHttpRequests(authRequest -> authRequest

                //.requestMatchers(new AntPathRequestMatcher("/**")).permitAll()
                // === RUTAS DE AUTENTICACIÓN ===
                
                .requestMatchers(new AntPathRequestMatcher("/auth/**")).permitAll()

                // === ARCHIVOS ESTÁTICOS ===
                .requestMatchers(new AntPathRequestMatcher("/test-profile.html")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/static/**")).permitAll()

                // === MUEBLES - GET público, PUT/POST/DELETE solo ADMIN ===
                .requestMatchers(new AntPathRequestMatcher("/api/v1/mueble", "GET")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/api/v1/mueble/**", "GET")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/api/v1/mueble/**", "POST")).hasAuthority("ADMIN")
                .requestMatchers(new AntPathRequestMatcher("/api/v1/mueble/**", "PUT")).hasAuthority("ADMIN")
                .requestMatchers(new AntPathRequestMatcher("/api/v1/mueble/**", "PUT")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/api/v1/mueble/**", "DELETE")).hasAuthority("ADMIN")

                // === CATEGORÍAS - GET público, PUT/POST/DELETE solo ADMIN ===
                .requestMatchers(new AntPathRequestMatcher("/api/v1/categoria", "GET")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/api/v1/categoria/**", "GET")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/api/v1/categoria/**", "POST")).hasAuthority("ADMIN")
                .requestMatchers(new AntPathRequestMatcher("/api/v1/categoria/**", "PUT")).hasAuthority("ADMIN")
                .requestMatchers(new AntPathRequestMatcher("/api/v1/categoria/**", "DELETE")).hasAuthority("ADMIN")

                // === IMÁGENES DE MUEBLES - GET público, PUT/POST/DELETE solo ADMIN ===
                .requestMatchers(new AntPathRequestMatcher("/api/v1/muebleImagenes", "GET")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/api/v1/muebleImagenes/**", "GET")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/api/v1/muebleImagenes/**", "POST")).hasAuthority("ADMIN")
                .requestMatchers(new AntPathRequestMatcher("/api/v1/muebleImagenes/**", "PUT")).hasAuthority("ADMIN")
                .requestMatchers(new AntPathRequestMatcher("/api/v1/muebleImagenes/**", "DELETE")).hasAuthority("ADMIN")


                // === SOLICITAR VISITA ===
                .requestMatchers(new AntPathRequestMatcher("/api/v1/solicitarVisita/crearConsulta")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/api/v1/solicitarVisita/createSolicitarVisita")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/api/v1/solicitarVisita/obtener-consultas/**", "GET")).authenticated()
                .requestMatchers(new AntPathRequestMatcher("/api/v1/solicitarVisita/**", "GET")).hasAuthority("ADMIN")
                .requestMatchers(new AntPathRequestMatcher("/api/v1/solicitarVisita/**", "PUT")).hasAuthority("ADMIN")

            
                .requestMatchers(new AntPathRequestMatcher("/api/v1/cliente/**")).hasAuthority("ADMIN")

               
                .requestMatchers(new AntPathRequestMatcher("/api/v1/usuarios/**")).hasAuthority("ADMIN")

                // === CUALQUIER OTRA RUTA - REQUIERE AUTENTICACIÓN ===
                .anyRequest().authenticated()
            )
            .sessionManagement(sessionManager -> sessionManager.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authenticationProvider(authProvider)
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
@Bean
CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With"));
    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "HEAD", "OPTIONS"));
    configuration.setAllowedOrigins(Arrays.asList(
        "http://localhost:8080",
        "http://localhost:5173",
        "http://localhost:5175",
        "https://mdzmuebles.up.railway.app"
    ));
    configuration.setAllowCredentials(true);
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
}


}
