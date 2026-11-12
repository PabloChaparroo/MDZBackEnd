package com.Capinteria.carpinteria.Config;


import com.Capinteria.carpinteria.Jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

// El análisis de nulos de Eclipse marca los valores sin anotar que se pasan a APIs de Spring
// anotadas @NonNull (MediaType, String, ids...). Nunca son null acá; mismo criterio que el resto del proyecto.
@SuppressWarnings("null")
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
            .cors(Customizer.withDefaults())
            .authorizeHttpRequests(authRequest -> authRequest

                //.requestMatchers(new AntPathRequestMatcher("/**")).permitAll()
                // === RUTAS DE AUTENTICACIÓN ===
                
                .requestMatchers(new AntPathRequestMatcher("/auth/**")).permitAll()

                // === PÁGINA DE ERROR DE SPRING ===
                // Cuando un controller responde con error (ResponseStatusException, JSON inválido, etc.)
                // el servidor reenvía internamente a /error. Si /error no es público, la seguridad lo
                // bloquea y el cliente recibe 403 en vez del 400/404/409 real. No expone mensajes ni trazas.
                .requestMatchers(new AntPathRequestMatcher("/error")).permitAll()

                // === PING (health check y keep-alive, no toca la base) ===
                .requestMatchers(new AntPathRequestMatcher("/ping", "GET")).permitAll()

                // === ARCHIVOS ESTÁTICOS ===
                .requestMatchers(new AntPathRequestMatcher("/static/**")).permitAll()

                // === MUEBLES - GET público, PUT/POST/DELETE solo ADMIN ===
                .requestMatchers(new AntPathRequestMatcher("/api/v1/mueble", "GET")).hasAuthority("ADMIN")
                .requestMatchers(new AntPathRequestMatcher("/api/v1/mueble/**", "GET")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/api/v1/mueble/**", "POST")).hasAuthority("ADMIN")
                .requestMatchers(new AntPathRequestMatcher("/api/v1/mueble/**", "PUT")).hasAuthority("ADMIN")
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


                // === CONSULTAS ===
                .requestMatchers(new AntPathRequestMatcher("/api/v1/consultas/crear")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/api/v1/consultas/**", "GET")).hasAuthority("ADMIN")
                .requestMatchers(new AntPathRequestMatcher("/api/v1/consultas/**", "POST")).hasAuthority("ADMIN")
                .requestMatchers(new AntPathRequestMatcher("/api/v1/consultas/**", "PUT")).hasAuthority("ADMIN")
                .requestMatchers(new AntPathRequestMatcher("/api/v1/consultas/**", "DELETE")).hasAuthority("ADMIN")

                .requestMatchers(new AntPathRequestMatcher("/api/v1/cliente/**")).hasAuthority("ADMIN")

               
                .requestMatchers(new AntPathRequestMatcher("/api/v1/usuarios/**")).hasAuthority("ADMIN")

                // === NOTIFICACIONES (casillas que reciben el aviso de consulta nueva) ===
                .requestMatchers(new AntPathRequestMatcher("/api/v1/notificaciones/**")).hasAuthority("ADMIN")

                // === CUALQUIER OTRA RUTA - REQUIERE AUTENTICACIÓN ===
                .anyRequest().authenticated()
            )
            .sessionManagement(sessionManager -> sessionManager.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authenticationProvider(authProvider)
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
    /**
     * Única definición de CORS del backend. Los orígenes permitidos salen de la propiedad
     * cors.allowed-origins (en producción, la variable de entorno CORS_ALLOWED_ORIGINS,
     * separada por comas). Si está vacía no se permite ningún origen (falla cerrado).
     * Sin credenciales: el front manda el JWT en el header Authorization, no en cookies.
     */
    @Bean
    public UrlBasedCorsConfigurationSource corsConfigurationSource(@Value("${cors.allowed-origins:}") List<String> allowedOrigins) {
        CorsConfiguration configuration = new CorsConfiguration();
        // Patrones (no solo orígenes exactos): Cloudflare Pages da a cada deploy una dirección propia
        // (https://<hash>.mdzmuebles.pages.dev), así que se admite https://*.mdzmuebles.pages.dev.
        // Un origen sin * se compara exacto, igual que antes.
        configuration.setAllowedOriginPatterns(allowedOrigins.stream().map(String::trim).filter(o -> !o.isEmpty()).toList());
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        configuration.setAllowCredentials(false);
        configuration.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }


}
